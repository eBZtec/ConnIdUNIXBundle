/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connid.bundles.unix.methods;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.CommunicationException;

import org.connid.bundles.unix.UnixConnection;
import org.connid.bundles.unix.UnixConnector;
import org.connid.bundles.unix.UnixResult;
import org.connid.bundles.unix.UnixResult.Operation;
import org.connid.bundles.unix.commands.General;
import org.connid.bundles.unix.files.PasswdRow;
import org.connid.bundles.unix.schema.SchemaAccountAttribute;
import org.connid.bundles.unix.utilities.EvaluateCommandsResultOutput;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

public class UnixUpdate {

    private static final Log LOG = Log.getLog(UnixUpdate.class);

    private Set<Attribute> attrs = null;

    private UnixConnection unixConnection = null;

    private Uid uid = null;

    private ObjectClass objectClass = null;

    public UnixUpdate(final ObjectClass oc, final UnixConnection unixConnection, final Uid uid,
                      final Set<Attribute> attrs) throws IOException, JSchException {
        this.uid = uid;
        this.attrs = attrs;
        this.unixConnection = unixConnection;
        objectClass = oc;
    }

    public Uid update() {
        try {
            return doUpdate(true);
        } catch (JSchException e) {
            LOG.error(e, "error during update operation");
            throw new ConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e, "error during update operation");
            throw new ConnectorException(e.getMessage(), e);
        }
    }

    public Uid removeAttributes() {
        try {
            return doUpdate(false);
        } catch (JSchException e) {
            LOG.error(e, "error during update operation");
            throw new ConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e, "error during update operation");
            throw new ConnectorException(e.getMessage(), e);
        }
    }

    private Uid doUpdate(boolean isAdd) throws IOException, JSchException {

        if (uid == null || StringUtil.isBlank(uid.getUidValue())) {
            throw new IllegalArgumentException("No Uid attribute provided in the attributes");
        }

        LOG.info("Update user: " + uid.getUidValue());

        if (!objectClass.equals(ObjectClass.ACCOUNT) && (!objectClass.equals(ObjectClass.GROUP))) {
            throw new IllegalArgumentException("Wrong object class");
        }

        Name newUserName = null;
        for (Attribute attr : attrs) {
            if (attr != null) {
                if (attr.is(Name.NAME)) {
                    newUserName = AttributeUtil.getNameFromAttributes(attrs);
                    break;
                }
            }
        }

        String newUserNameValue = null;
        if (newUserName != null && StringUtil.isNotBlank(newUserName.getNameValue())) {
            newUserNameValue = newUserName.getNameValue();
        } else {
            newUserNameValue = uid.getUidValue();
        }

        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            StringBuilder commandBuilder = new StringBuilder();
            // ChannelShell shellChannel = unixConnection.createShellChannel();
            if (!isAdd) {
                processGroupMembership(commandBuilder, newUserNameValue);
            }

            String modCommand = UnixConnector.getCommandGenerator().updateUser(uid.getUidValue(), attrs, isAdd);
            UnixCommon.appendCommand(commandBuilder, modCommand);

            String activation = UnixCommon.buildActivationCommand(unixConnection, newUserNameValue, attrs);
            UnixCommon.appendCommand(commandBuilder, activation);
            UnixCommon.appendCommand(commandBuilder,
                    UnixCommon.buildLockoutCommand(unixConnection, newUserNameValue, attrs));

            if (newUserName != null) {
                String oldUser = unixConnection
                        .executeRead(UnixConnector.getCommandGenerator().userExists(uid.getUidValue())).getOutput();
                if (StringUtil.isBlank(oldUser)) {
                    throw new UnknownUidException("User do not exists");
                }
                PasswdRow oldUserRow = EvaluateCommandsResultOutput.toPasswdRow(oldUser);
                String moveHomeDir = moveHomeDirectory(oldUserRow, unixConnection, attrs);
                UnixCommon.appendCommand(commandBuilder, moveHomeDir);
                String groupRename = UnixConnector.getCommandGenerator().renamePrimaryGroup(oldUserRow.getUsername(),
                        newUserNameValue);
                UnixCommon.appendCommand(commandBuilder, groupRename);
            }

            if (isAdd) {
                UnixCommon.appendCreateOrUpdatePublicKeyCommand(commandBuilder, newUserNameValue, attrs, false);
                UnixCommon.appendCreateOrUpdatePermissions(commandBuilder, newUserNameValue, attrs, true);
            } else {
                UnixCommon.appendDeletePublicKeyCommand(commandBuilder, newUserNameValue, attrs);
                Attribute permissions = AttributeUtil.find(SchemaAccountAttribute.PERMISIONS.getName(), attrs);
                if (permissions != null) {
                    UnixCommon.appendRemovePermissions(commandBuilder, newUserNameValue, true);
                }
            }

            if (StringUtil.isNotBlank(commandBuilder.toString())) {
                UnixResult result = unixConnection.execute(commandBuilder.toString());
                result.checkResult(Operation.USERMOD, "Could not modify user", LOG);
            }
            if (isAdd) {
                UnixCommon.processPassword(unixConnection, newUserNameValue, attrs);
            } else {
                UnixCommon.resetPassword(unixConnection, newUserNameValue, attrs);
            }

        } else if (objectClass.equals(ObjectClass.GROUP)) {
            StringBuilder commandBuilder = new StringBuilder();
            String modCommand = UnixConnector.getCommandGenerator().updateGroup(uid.getUidValue(), attrs);
            UnixCommon.appendCommand(commandBuilder, modCommand);
            UnixCommon.appendCreateOrUpdatePermissions(commandBuilder, uid.getUidValue(), attrs, false);
            UnixResult result = unixConnection.execute(commandBuilder.toString());
            result.checkResult(Operation.GROUPMOD, "Could not modify group", LOG);
        }
        return new Uid(newUserNameValue);
    }

    private void processGroupMembership(StringBuilder commandBuilder, String newUserNameValue)
            throws JSchException, IOException, ConnectException {
        Attribute attr = AttributeUtil.find(SchemaAccountAttribute.GROUPS.getName(), attrs);
        if (!UnixCommon.isEmpty(attr)) {

            List<String> groups = EvaluateCommandsResultOutput.evaluateUserGroups(
                    unixConnection.executeRead(General.searchGroupsForUser(newUserNameValue)).getOutput());

            List<Object> newGroups = new ArrayList<Object>();
            for (String group : groups) {
                if (attr.getValue().contains(group)) {
                    continue;
                }
                newGroups.add(group);
            }

            UnixCommon.appendCommand(commandBuilder,
                    UnixConnector.getCommandGenerator().buildRemoveFromGroupsCommand(newUserNameValue, newGroups));
        }

    }

    private static String moveHomeDirectory(PasswdRow oldUserRow, UnixConnection unixConnection, Set<Attribute> attrs)
            throws JSchException, IOException {

        Attribute newHomeDir = AttributeUtil.find(SchemaAccountAttribute.HOME.getName(), attrs);
        if (newHomeDir != null && newHomeDir.getValue() != null && !newHomeDir.getValue().isEmpty()) {
            return UnixConnector.getCommandGenerator().moveHomeDirectory(oldUserRow.getHomeDirectory(),
                    (String) newHomeDir.getValue().get(0));
        }
        return null;

    }
}
