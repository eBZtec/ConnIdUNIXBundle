/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://connid.googlecode.com/svn/trunk/legal/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at connid/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.connid.bundles.unix.search;

import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import org.connid.bundles.unix.UnixConfiguration;
import org.connid.bundles.unix.UnixConnection;
import org.connid.bundles.unix.UnixConnector;
import org.connid.bundles.unix.files.PasswdFile;
import org.connid.bundles.unix.files.PasswdRow;
import org.connid.bundles.unix.utilities.EvaluateCommandsResultOutput;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.*;

public class Search {

    private UnixConnection unixConnection = null;

    private Operand filter = null;

    private UnixConfiguration unixConfiguration = null;

    private ObjectClass objectClass = null;

    private ResultsHandler handler = null;

    public Search(final UnixConfiguration unixConfiguration,
            final UnixConnection unixConnection,
            final ResultsHandler handler, final ObjectClass oc,
            final Operand filter) {
        this.unixConfiguration = unixConfiguration;
        this.unixConnection = unixConnection;
        this.handler = handler;
        this.objectClass = oc;
        this.filter = filter;
    }

    public void equalSearch() throws IOException, InterruptedException, JSchException {
        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            PasswdFile passwdFile = new PasswdFile(getPasswdFileOutput(
                    unixConnection.execute(UnixConnector.getCommandGenerator().searchAllUser())));
            fillUserHandler(passwdFile.searchRowByAttribute(
                    filter.getAttributeValue(), filter.isNot()));
        } else if (objectClass.equals(ObjectClass.GROUP)) {
            fillGroupHandler();
        }
    }

    private List<String> getPasswdFileOutput(final String command) throws IOException {
        String[] a = command.split("\n");
        List<String> passwdRows = CollectionUtil.newList(a);
        return passwdRows;
    }

    public void startsWithSearch() throws IOException, InterruptedException, JSchException {
        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            PasswdFile passwdFile = new PasswdFile(getPasswdFileOutput(
                    unixConnection.execute(UnixConnector.getCommandGenerator().searchAllUser())));
            fillUserHandler(passwdFile.searchRowByStartsWithValue(
                    filter.getAttributeValue()));
        } else if (objectClass.equals(ObjectClass.GROUP)) {
            fillGroupHandler();
        }
    }

    public void endsWithSearch() throws IOException, InterruptedException, JSchException {
        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            PasswdFile passwdFile = new PasswdFile(getPasswdFileOutput(
                    unixConnection.execute(UnixConnector.getCommandGenerator().searchAllUser())));
            fillUserHandler(passwdFile.searchRowByEndsWithValue(
                    filter.getAttributeValue()));
        } else if (objectClass.equals(ObjectClass.GROUP)) {
            fillGroupHandler();
        }
    }

    public void containsSearch() throws IOException, InterruptedException, JSchException {
        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            PasswdFile passwdFile = new PasswdFile(getPasswdFileOutput(
                    unixConnection.execute(UnixConnector.getCommandGenerator().searchAllUser())));
            fillUserHandler(
                    passwdFile.searchRowByContainsValue(
                    filter.getAttributeValue()));
        } else if (objectClass.equals(ObjectClass.GROUP)) {
            fillGroupHandler();
        }
    }

    public void orSearch()
            throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void andSearch() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void fillUserHandler(final List<PasswdRow> passwdRows)
            throws ConnectException, IOException, InterruptedException, JSchException {
        if (passwdRows == null || passwdRows.isEmpty()) {
            throw new ConnectException("No results found");
        }
        for (Iterator<PasswdRow> it =
                passwdRows.iterator(); it.hasNext();) {
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            PasswdRow passwdRow = it.next();
            if (StringUtil.isNotEmpty(passwdRow.getUsername())
                    && StringUtil.isNotBlank(passwdRow.getUsername())) {
                bld.setName(passwdRow.getUsername());
                bld.setUid(passwdRow.getUsername());
            } else {
                bld.setUid("_W_R_O_N_G_");
                bld.setName("_W_R_O_N_G_");
            }
            bld.addAttribute(AttributeBuilder.build(
                    unixConfiguration.getCommentAttribute(),
                    CollectionUtil.newSet(passwdRow.getComment())));
            bld.addAttribute(AttributeBuilder.build(
                    unixConfiguration.getShellAttribute(),
                    CollectionUtil.newSet(passwdRow.getShell())));
            bld.addAttribute(AttributeBuilder.build(
                    unixConfiguration.getHomeDirectoryAttribute(),
                    CollectionUtil.newSet(passwdRow.getHomeDirectory())));
            if (filter.isUid()) {
                bld.addAttribute(OperationalAttributes.ENABLE_NAME,
                        EvaluateCommandsResultOutput.evaluateUserStatus(
                        unixConnection.execute(UnixConnector.getCommandGenerator().
                        userStatus(filter.getAttributeValue()))));
            }
            handler.handle(bld.build());
        }
    }

    private void fillGroupHandler() throws IOException, InterruptedException, JSchException {
        String nameToSearch = filter.getAttributeValue();
        if (StringUtil.isNotBlank(nameToSearch)
                && StringUtil.isNotEmpty(nameToSearch)
                && EvaluateCommandsResultOutput.evaluateUserOrGroupExists(
                unixConnection.execute(UnixConnector.getCommandGenerator().groupExists(nameToSearch)))) {
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            bld.setName(nameToSearch);
            bld.setUid(nameToSearch);
            handler.handle(bld.build());
        }
    }
}
