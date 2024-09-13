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

import org.connid.bundles.unix.schema.SchemaAccountAttribute;
import org.connid.bundles.unix.search.Operand;
import org.connid.bundles.unix.search.Operator;
import org.connid.bundles.unix.utilities.AttributesTestValue;
import org.connid.bundles.unix.utilities.SharedTestMethods;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
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

public class UnixCreateUserTest extends SharedTestMethods {

	private UnixConnector connector = null;

	private Name name = null;

	private Uid newAccount = null;

	private AttributesTestValue attrs = null;

	@BeforeTest
	public final void initTest() {
		attrs = new AttributesTestValue();
		connector = new UnixConnector();
		connector.init(createConfiguration());
		name = new Name(attrs.getUsername());
	}

	@Test
	public final void createExistsUser() {
		printTestTitle("createExistsUser");
		boolean userExists = false;
		newAccount = connector
				.create(ObjectClass.ACCOUNT, createSetOfAttributes(name, attrs.getPassword(), true), null);
		Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
		try {
			connector.create(ObjectClass.ACCOUNT, createSetOfAttributes(name, attrs.getPassword(), true), null);
		} catch (Exception e) {
			userExists = true;
		}
		Assert.assertTrue(userExists);
	}

	@Test
	public final void createUserWithPubKey() {
		printTestTitle("createUserWithPubKey");
		Set<Attribute> attributes = createSetOfAttributes(name, attrs.getPassword(), true);
		StringBuilder publicKey = new StringBuilder("ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAi6lZ+y6mjDg1VEl6IoJXfvsOuayj7i2WMOYjwlRWZdCOzqFw6W+KptBd+2WU+EBwtKFitYSWPc+LlihSGWEW+2XInujl8WKj1zdb/UewcvFXHVOAES32/9jvyfb935xPIKYM7OqzhUp6sHPDzauZ8V9aWek5/RZ81yPaRGmhwaE=");

		attributes.add(AttributeBuilder.build(SchemaAccountAttribute.PUBLIC_KEY.getName(), publicKey.toString()));
		newAccount = connector.create(ObjectClass.ACCOUNT, attributes, null);
		Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());

	}

	@Test(expectedExceptions = ConnectorException.class)
	public final void createLockedUser() {
		printTestTitle("createLockedUser");
		newAccount = connector.create(ObjectClass.ACCOUNT, createSetOfAttributes(name, attrs.getPassword(), false),
				null);
		connector.authenticate(ObjectClass.ACCOUNT, attrs.getUsername(), attrs.getGuardedPassword(), null);
	}

	@Test
	public final void createUnLockedUser() {
		printTestTitle("createUnLockedUser");
		newAccount = connector
				.create(ObjectClass.ACCOUNT, createSetOfAttributes(name, attrs.getPassword(), true), null);
		Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
		final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
		
		System.out.println("first read");
		connector.executeQuery(ObjectClass.ACCOUNT,
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
		connector.authenticate(ObjectClass.ACCOUNT, newAccount.getUidValue(), attrs.getGuardedPassword(), null);
	}

	private void sleep(final long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (Exception ee) {
			throw new RuntimeException(ee);
//			LOG.info("Failed to sleep between reads with pollTimeout: " + 1000, ee);
		}
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createWithWrongObjectClass() {
		printTestTitle("createWithWrongObjectClass");
		connector.create(attrs.getWrongObjectClass(), createSetOfAttributes(name, attrs.getPassword(), true), null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createTestWithNull() {
		printTestTitle("createTestWithNull");
		connector.create(attrs.getWrongObjectClass(), null, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createTestWithNameNull() {
		printTestTitle("createTestWithNameNull");
		connector.create(ObjectClass.ACCOUNT, createSetOfAttributes(null, attrs.getPassword(), true), null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createTestWithPasswordNull() {
		printTestTitle("createTestWithPasswordNull");
		connector.create(attrs.getWrongObjectClass(), createSetOfAttributes(name, null, true), null);
	}

	@Test(expectedExceptions = ConnectorException.class)
	public void createTestWithAllNull() {
		printTestTitle("createTestWithAllNull");
		connector.create(null, null, null);
	}

	@AfterTest
	public final void close() {
		sleep(5000);
		if (newAccount != null) {
			connector.delete(ObjectClass.ACCOUNT, newAccount, null);
		}
//		connector.dispose();
	}
}