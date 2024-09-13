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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UnixUpdateGroupTest extends SharedTestMethods {

    private UnixConnector connector = null;
    private Name name = null;
    private Uid newAccount = null;
    private AttributesTestValue attrs = null;

    @BeforeTest
    public final void initTest() {
        attrs = new AttributesTestValue();
        connector = new UnixConnector();
        connector.init(createConfiguration());
        name = new Name(attrs.getGroupName());
    }

    @Test
    public final void updateGroup() {
    	printTestTitle("updateGroup");
        Uid account = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        newAccount = connector.update(ObjectClass.GROUP, account,
                createSetOfAttributes(new Name(attrs.getNewGroupName()), attrs.getNewPassword(), true),
                null);
//        connector.delete(ObjectClass.GROUP,
//                newGroupName, null);
    }

    @Test(expectedExceptions = ConnectorException.class)
    public final void updateNotExistsGroup() {
    	printTestTitle("updateNotExistsGroup");
        connector.update(ObjectClass.GROUP, new Uid(attrs.getWrongGroupName()),
                createSetOfAttributes(name, attrs.getNewPassword(), true),
                null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithWrongObjectClass() {
    	printTestTitle("updateWithWrongObjectClass");
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        connector.update(attrs.getWrongObjectClass(), newAccount,
                createSetOfAttributes(name, attrs.getNewPassword(), true),
                null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullUid() {
    	printTestTitle("updateWithNullUid");
        connector.update(ObjectClass.GROUP, null,
                createSetOfAttributes(name, attrs.getNewPassword(), true),
                null);
    }

   // @Test(expected = ConnectorException.class)
    public void updateWithNullSet() {
    	printTestTitle("updateWithNullSet");
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        connector.update(ObjectClass.GROUP, newAccount, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullPwd() {
    	printTestTitle("updateWithNullPwd");
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        connector.update(ObjectClass.GROUP, newAccount,
                createSetOfAttributes(name, null, true), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateWithNullUsername() {
    	printTestTitle("updateWithNullUsername");
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        connector.update(ObjectClass.GROUP, newAccount,
                createSetOfAttributes(null, attrs.getPassword(), true), null);
    }

    @AfterTest
    public final void close() {
        if (newAccount != null) {
            connector.delete(ObjectClass.GROUP, newAccount, null);
        }
        connector.dispose();
    }
}
