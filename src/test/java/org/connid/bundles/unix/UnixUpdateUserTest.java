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
package org.connid.bundles.unix;

import org.connid.bundles.unix.utilities.AttributesTestValue;
import org.connid.bundles.unix.utilities.SharedTestMethods;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UnixUpdateUserTest extends SharedTestMethods {

    private UnixConnector connector = null;

    private Name name = null;

    private Uid newAccount = null;

    private AttributesTestValue attrs = null;

    private final static boolean ACTIVE_USER = true;

    private final static boolean INACTIVE_USER = false;

    @BeforeTest
    public final void initTest() {
        attrs = new AttributesTestValue();
        connector = new UnixConnector();
        connector.init(createConfiguration());
        name = new Name(attrs.getUsername());
    }

    @Test
    public final void updateAndAuthenticateWithNewPassword() {
    	printTestTitle("updateAndAuthenticateWithNewPassword");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), ACTIVE_USER),
                null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(name, attrs.getNewPassword(), ACTIVE_USER), null);
        final Uid authUid = connector.authenticate(ObjectClass.ACCOUNT, name.getNameValue(),
                attrs.getNewGuardedPassword(), null);
        Assert.assertEquals(newAccount.getUidValue(), authUid.getUidValue());
    }
    
    @Test
    public final void updateOnlyPassword() {
    	printTestTitle("updateOnlyPassword");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), ACTIVE_USER),
                null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createPasswordChange(attrs.getNewPassword()), null);
        final Uid authUid = connector.authenticate(ObjectClass.ACCOUNT, name.getNameValue(),
                attrs.getNewGuardedPassword(), null);
        Assert.assertEquals(newAccount.getUidValue(), authUid.getUidValue());
    }

    @Test
    public final void updateAndAuthenticateWithNewUsernameAndNewPassword() {
    	printTestTitle("updateAndAuthenticateWithNewUsernameAndNewPassword");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        Name newName = new Name(attrs.getNewUsername());
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(newName, attrs.getNewPassword(),
                ACTIVE_USER), null);
        final Uid authUid = connector.authenticate(ObjectClass.ACCOUNT, newName.getNameValue(),
                attrs.getNewGuardedPassword(), null);
        Assert.assertEquals(newName.getNameValue(), authUid.getUidValue());
        newAccount = new Uid(newName.getNameValue());
    }

    @Test
    public final void updateLockedUser() {
    	printTestTitle("updateLockedUser");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                INACTIVE_USER), null);
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        final Uid authUid = connector.authenticate(ObjectClass.ACCOUNT, newAccount.getUidValue(),
                attrs.getGuardedPassword(), null);
        Assert.assertEquals(newAccount.getUidValue(), authUid.getUidValue());

    }

//    @Test(expected = ConnectorException.class)
    public final void updateUnlockedUser() {
    	printTestTitle("updateUnlockedUser");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        connector.authenticate(ObjectClass.ACCOUNT, newAccount.getUidValue(),
                attrs.getGuardedPassword(), null);
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(name, attrs.getPassword(),
                INACTIVE_USER), null);
        connector.authenticate(ObjectClass.ACCOUNT, newAccount.getUidValue(),
                attrs.getGuardedPassword(), null);
    }

    @Test(expectedExceptions = ConnectorException.class)
    public final void updateAndAuthenticateWithOldPassword() {
    	printTestTitle("updateAndAuthenticateWithOldPassword");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(name, attrs.getNewPassword(),
                ACTIVE_USER), null);
        connector.authenticate(ObjectClass.ACCOUNT, name.getNameValue(),
                attrs.getGuardedPassword(), null);
    }

    @Test(expectedExceptions = ConnectorException.class)
    public final void updateNotExistsUser() {
    	printTestTitle("updateNotExistsUser");
        connector.update(ObjectClass.ACCOUNT, new Uid(attrs.getWrongUsername()),
                createSetOfAttributes(name, attrs.getNewPassword(),
                ACTIVE_USER), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithWrongObjectClass() {
    	printTestTitle("updateWithWrongObjectClass");
    	newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        connector.update(attrs.getWrongObjectClass(), newAccount,
                createSetOfAttributes(name, attrs.getNewPassword(),
                ACTIVE_USER), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullObjectClass() {
    	printTestTitle("updateWithNullObjectClass");
        connector.update(null, newAccount,
                createSetOfAttributes(name, attrs.getNewPassword(),
                ACTIVE_USER), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullUid() {
    	printTestTitle("updateWithNullUid");
        connector.update(ObjectClass.ACCOUNT, null,
                createSetOfAttributes(name, attrs.getNewPassword(),
                ACTIVE_USER), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullSet() {
    	printTestTitle("updateWithNullSet");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        connector.update(ObjectClass.ACCOUNT, newAccount, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullPwd() {
    	printTestTitle("updateWithNullPwd");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(name, null, true), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullUsername() {
    	printTestTitle("updateWithNullUsername");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(),
                ACTIVE_USER), null);
        connector.update(ObjectClass.ACCOUNT, newAccount,
                createSetOfAttributes(null, attrs.getPassword(),
                ACTIVE_USER), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithAllNull() {
    	printTestTitle("updateWithAllNull");
        connector.update(null, null, null, null);
    }

    @AfterTest
    public final void close() {
        if (newAccount != null) {
            connector.delete(ObjectClass.ACCOUNT, newAccount, null);
        }
        connector.dispose();
    }
}
