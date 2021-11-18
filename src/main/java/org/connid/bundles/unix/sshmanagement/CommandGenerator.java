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
package org.connid.bundles.unix.sshmanagement;

import java.io.IOException;
import java.rmi.server.Operation;
import java.util.List;
import java.util.Set;

import org.connid.bundles.unix.UnixConfiguration;
import org.connid.bundles.unix.commands.General;
import org.connid.bundles.unix.commands.GroupAdd;
import org.connid.bundles.unix.commands.GroupDel;
import org.connid.bundles.unix.commands.GroupMod;
import org.connid.bundles.unix.commands.OptionBuilder;
import org.connid.bundles.unix.commands.Passwd;
import org.connid.bundles.unix.commands.Sudo;
import org.connid.bundles.unix.commands.Tee;
import org.connid.bundles.unix.commands.UserAdd;
import org.connid.bundles.unix.commands.UserDel;
import org.connid.bundles.unix.commands.UserMod;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.OperationOptions;

import com.jcraft.jsch.JSchException;

public class CommandGenerator {

    private UnixConfiguration unixConfiguration = null;

    public CommandGenerator(final UnixConfiguration unixConfiguration) {
        this.unixConfiguration = unixConfiguration;
    }

    public String userExists(final String username) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(General.searchUserIntoPasswdFile(username)).toString();

        return commandToExecute.toString();
    }

    public String searchAllUser(OperationOptions options) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        return commandToExecute.append(General.getentPasswdFile(options)).toString();
    }
    
    public String searchAllGroups(OperationOptions options) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        return commandToExecute.append(General.getentGroupFile(options)).toString();
    }

    public String groupExists(final String groupname) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(General.searchGroupIntoGroupFile(groupname));
        return commandToExecute.toString();
    }

    public String userStatus(final String username) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        return commandToExecute.append(General.searchUserStatusIntoShadowFile(username)).toString();
    }
    
    public String userPermissions(final String username) {
    	StringBuilder commandToExecute = new StringBuilder();
    	if (!unixConfiguration.isRoot()){
    		Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
    		commandToExecute.append(sudoCommand.sudo());
    	}
    	
    	return commandToExecute.append(General.getUserPermissions(username)).toString();
    }
    
    public String groupPermissions(final String username) {
    	StringBuilder commandToExecute = new StringBuilder();
    	if (!unixConfiguration.isRoot()){
    		Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
    		commandToExecute.append(sudoCommand.sudo());
    	}
    	
    	return commandToExecute.append(General.getGroupPermissions(username)).toString();
    }
    
    public String buildRemoveFromGroupsCommand(final String username, final List<Object> values){
    	StringBuilder commandToExecute = new StringBuilder();
    	if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
    	return commandToExecute.append(OptionBuilder.buildRemoveFromGroupsCommand(username, values)).toString();
    }
    
    public String userGroups(final String username) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        return commandToExecute.append(General.searchGroupsForUser(username)).toString();
    }

    public String createUser(String username, Set<Attribute> attributes) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        UserAdd userAddCommand = new UserAdd(unixConfiguration, username, attributes);
        commandToExecute.append(userAddCommand.useradd());
        return commandToExecute.toString();
    }
    
    public String setPassword(String username, String password){
    	 StringBuilder commandToExecute = new StringBuilder();
    	 if (!unixConfiguration.isRoot()) {
             Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
             commandToExecute.append(sudoCommand.sudo());
         }
    	Passwd passwd = new Passwd();
    	commandToExecute.append(passwd.setPassword(username, password));
    	return commandToExecute.toString();
    }
    
    public String resetPassword(String username){
   	 StringBuilder commandToExecute = new StringBuilder();
   	 if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
   	Passwd passwd = new Passwd();
   	commandToExecute.append(passwd.resetPassword(username));
   	return commandToExecute.toString();
   }
    

    public String deleteUser(final String username) {
        UserDel userDelCommand =
                new UserDel(unixConfiguration, username);
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(userDelCommand.userdel());
        return commandToExecute.toString();
    }

   
    public String createGroup(final String groupName, Set<Attribute> attrs)
            throws IOException, JSchException {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        GroupAdd groupAddCommand = new GroupAdd(groupName, attrs);
        commandToExecute.append(groupAddCommand.groupadd());
        return commandToExecute.toString();
    }

    public String updateUser(final String actualUsername,
            Set<Attribute> attributes, boolean isAdd) {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        UserMod userModCommand = new UserMod(unixConfiguration, actualUsername, attributes);
        String mod = userModCommand.userMod(isAdd);
        if (StringUtil.isNotBlank(mod)){
        	commandToExecute.append(mod);
        	return commandToExecute.toString();
        } else {
        	return null;
        }
    }
    
    public String lockUser(final String username) {
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
    	UserMod userModCommand = new UserMod(unixConfiguration, username, null);
    	commandToExecute.append(userModCommand.lockUser(username));
        return commandToExecute.toString();
    }
    
    public String disableUser(final String username, final String disableDate) {
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
    	UserMod userModCommand = new UserMod(unixConfiguration, username, null);
    	commandToExecute.append(userModCommand.disableUser(username, disableDate));
        return commandToExecute.toString();
    }
    
    public String enableUser(final String username) {
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
    	UserMod userModCommand = new UserMod(unixConfiguration, username, null);
    	commandToExecute.append(userModCommand.enableUser(username));
        return commandToExecute.toString();
    }
    
    public String createSshKeyDir(final String username){
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(General.mkdirSsh(username));
        return commandToExecute.toString();
    }
    
    public String removeSshKeyDir(final String username){
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(General.removeDirSsh(username));
        return commandToExecute.toString();
    }
    
    public String changeSshKeyPermision(final String username, final String dirPermisions, final String keyPermisions){
    	StringBuilder commandToExecute = new StringBuilder();
        commandToExecute.append(General.changePermissionsForKeys(username, dirPermisions, keyPermisions, unixConfiguration.isRoot()));
        return commandToExecute.toString();
    }
    
    public String changeSshKeyOwner(final String username){
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(General.changeOwnerForKeys(username));
        return commandToExecute.toString();
    }
    
    public String setPublicKey(final String username, final String publicKey) {
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append("echo ").append("\"").append(publicKey).append("\"").append(" | ");
    	if (!unixConfiguration.isRoot()){
    		Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo(false));
    	}
    	String filename = new StringBuilder("/home/").append(username).append("/.ssh/authorized_keys").toString();
    	Tee teeCommand = new Tee(unixConfiguration, filename);
    	commandToExecute.append(teeCommand.tee());
        return commandToExecute.toString();
    }
    
    public String setPermissions(final String username, final String permissions, final boolean isUser) {
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append("echo ").append("\"").append(username).append(" ").append(permissions).append("\"").append(" | ");
    	if (!unixConfiguration.isRoot()){
    		Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo(false));
    	}
    	String filename = new StringBuilder("/etc/sudoers.d/").append(username).append(isUser ? "_user" : "_group").toString();
    	Tee teeCommand = new Tee(unixConfiguration, filename);
    	commandToExecute.append(teeCommand.tee());
        return commandToExecute.toString();
    }
    
    public String removePermissions(final String username, boolean isUser){
    	StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append("rm -f /etc/sudoers.d/").append(username).append(isUser ? "_user" : "_group");
        return commandToExecute.toString();
    }


    public String unlockUser(String username)
            throws IOException, JSchException {
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()){
        	Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
        	commandToExecute.append(sudoCommand.sudo());
        }
        UserMod userModCommand = new UserMod(unixConfiguration, username, null);
        commandToExecute.append(userModCommand.unlockUser(username));
        return commandToExecute.toString();
    }

    public String updateGroup(final String actualGroupName,
            final Set<Attribute> attrs) {
        GroupMod groupModCommand = new GroupMod(actualGroupName, attrs);
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        String groupMod = groupModCommand.groupMod();
        if (StringUtil.isNotBlank(groupMod)){
        	commandToExecute.append(groupMod);
        	return commandToExecute.toString();
        } 

        return null;        
    }
    
    public String renamePrimaryGroup(final String actualGroupName,
            final String newGroupName) {
        GroupMod groupModCommand = new GroupMod();
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(groupModCommand.groupRename(actualGroupName, newGroupName));
        return commandToExecute.toString();
    }

    public String deleteGroup(final String groupName) {
        GroupDel groupDelCommand = new GroupDel(groupName);
        StringBuilder commandToExecute = new StringBuilder();
        if (!unixConfiguration.isRoot()) {
            Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
            commandToExecute.append(sudoCommand.sudo());
        }
        commandToExecute.append(groupDelCommand.groupDel());
        return commandToExecute.toString();
    }

    public String moveHomeDirectory(final String oldHomeDir, final String newHomeDir) {
    	StringBuilder commandToExecute = new StringBuilder(); 
    	if (!unixConfiguration.isRoot()) {
             Sudo sudoCommand = new Sudo(unixConfiguration.getSudoPassword());
             commandToExecute.append(sudoCommand.sudo());
         }
    	commandToExecute.append("mv ").append(oldHomeDir).append(" ").append(newHomeDir);
        return commandToExecute.toString();
    }
}
