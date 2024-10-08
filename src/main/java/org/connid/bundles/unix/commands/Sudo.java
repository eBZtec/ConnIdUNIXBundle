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
package org.connid.bundles.unix.commands;

import org.connid.bundles.unix.utilities.Utilities;
import org.identityconnectors.common.security.GuardedString;

public class Sudo {

    private String sudoPassword = "";
    /**
     * sudo allows a permitted user to execute a command as the superuser or
     * another user, as specified in the sudoers file. The real and effective
     * uid and gid are set to match those of the target user as specified in the
     * passwd file and the group vector is initialized based on the group file
     * (unless the -P option was specified). If the invoking user is root or if
     * the target user is the same as the invoking user, no password is
     * required. Otherwise, sudo requires that users authenticate themselves
     * with a password by default (NOTE: in the default configuration this is
     * the user’s password, not the root password). Once a user has been
     * authenticated, a timestamp is updated and the user may then use sudo
     * without a password for a short period of time (5 minutes unless
     * overridden in sudoers).
     */
    private static final String SUDO_COMMAND = "sudo ";
    /**
     * When used by itself, the -k (kill) option to sudo invalidates the user’s
     * timestamp by setting the time on it to the Epoch. The next time sudo is
     * run a password will be required. This option does not require a password
     * and was added to allow a user to revoke sudo permissions from a .logout
     * file.
     * <p>
     * When used in conjunction with a command or an option that may require a
     * password, the -k option will cause sudo to ignore the user’s timestamp
     * file. As a result, sudo will prompt for a password (if one is required by
     * sudoers) and will not update the user’s timestamp file.
     */
    private static final String INVALIDATE_TIMESTAMP_OPTION = "-k";
    /**
     * If given the -v (validate) option, sudo will update the user’s timestamp,
     * prompting for the user’s password if necessary. This extends the sudo
     * timeout for another 5 minutes (or whatever the timeout is set to in
     * sudoers) but does not run a command.
     */
    private static final String VALIDATE_OPTION = "-v";
    /**
     * The -S (stdin) option causes sudo to read the password from the standard
     * input instead of the terminal device.
     */
    private static final String READ_PASSWORD_FROM_STDIN_OPTION = "-S";

    public Sudo(final GuardedString sudoPassword) {
        this.sudoPassword = Utilities.getPlainPassword(sudoPassword);
    }

    private String createSudoCommand() {
//    	return SUDO_COMMAND;

        return SUDO_COMMAND + " " + INVALIDATE_TIMESTAMP_OPTION + "; echo "
                + "'" + sudoPassword + "'" + " | " + SUDO_COMMAND + " " + VALIDATE_OPTION
                + " " + READ_PASSWORD_FROM_STDIN_OPTION + "; " + SUDO_COMMAND;
    }

    public String sudo() {
        return createSudoCommand();
    }

    public String sudo(Boolean withOptions) {
        if (!withOptions) {
            return SUDO_COMMAND;
        } else {
            return sudo();
        }
    }
}
