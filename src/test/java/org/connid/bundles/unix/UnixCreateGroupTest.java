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

import java.util.HashSet;
import java.util.Set;

import org.connid.bundles.unix.schema.SchemaGroupAttribute;
import org.connid.bundles.unix.search.Operand;
import org.connid.bundles.unix.search.Operator;
import org.connid.bundles.unix.utilities.AttributesTestValue;
import org.connid.bundles.unix.utilities.SharedTestMethods;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UnixCreateGroupTest extends SharedTestMethods {

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
    public final void createExistsGroup() {
        boolean groupExists = false;
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        try {
            connector.create(ObjectClass.GROUP,
                    createSetOfAttributes(name, attrs.getPassword(), true),
                    null);
        } catch (Exception e) {
            groupExists = true;
        }
        Assert.assertTrue(groupExists);
        connector.delete(ObjectClass.GROUP, newAccount, null);
    }
    
    @Test
    public final void createGroupWithPermissions() {
        
        Set<Attribute> attributes = createSetOfAttributes(name, attrs.getPassword(), true);
        StringBuilder permissions = new StringBuilder("HOST=(ALL) NOPASSWD: /usr/sbin/useradd,/usr/sbin/usermod,/usr/sbin/userdel,/usr/sbin/groupadd,/usr/sbin/groupmod,/usr/sbin/groupdel,/bin/mv,/usr/bin/passwd,/usr/bin/getent,/bin/echo,/usr/bin/tee,/bin/chown,/bin/chmod,/bin/mkdir,/usr/bin/groups,/usr/bin/id,/usr/bin/replace,/bin/rm,/bin/sudo");

		attributes.add(AttributeBuilder.build(SchemaGroupAttribute.PERMISSIONS.getName(), permissions.toString()));
		
        newAccount = connector.create(ObjectClass.GROUP, attributes, null);
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.GROUP,
				new Operand(Operator.EQ, Uid.NAME, newAccount.getUidValue(), false), new ResultsHandler() {

					@Override
					public boolean handle(final ConnectorObject connObj) {
						actual.add(connObj);
						return true;
					}
				}, null);
		for (ConnectorObject connObj : actual) {
			Assert.assertEquals(name.getNameValue(), connObj.getName().getNameValue());
		}
        
        connector.delete(ObjectClass.GROUP, newAccount, null);
    }

    @AfterTest
    public final void close() {
        connector.dispose();
    }
}
