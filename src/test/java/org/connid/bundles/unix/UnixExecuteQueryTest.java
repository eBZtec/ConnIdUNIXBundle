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

import org.connid.bundles.unix.search.Operand;
import org.connid.bundles.unix.search.Operator;
import org.connid.bundles.unix.utilities.AttributesTestValue;
import org.connid.bundles.unix.utilities.SharedTestMethods;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.Ignore;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UnixExecuteQueryTest extends SharedTestMethods {

    private static final String TEST_CLASS = "UnixExecuteQueryTest.";

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
    public final void searchUser() {
        printTestTitle(TEST_CLASS + "searchUser()");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());

        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT,
                new Operand(
                        Operator.EQ, Uid.NAME, newAccount.getUidValue(), false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        for (ConnectorObject connObj : actual) {
            Assert.assertEquals(name.getNameValue(), connObj.getName().getNameValue());
        }
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }

    @Test
    public final void searchUserDisabled() {
        printTestTitle(TEST_CLASS + "searchUserDisabled()");

        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        newAccount = connector.create(ObjectClass.ACCOUNT, createSetOfAttributes(new Name("a.nikolic"), attrs.getPassword(), false), null);
        connector.executeQuery(ObjectClass.ACCOUNT,
                new Operand(
                        Operator.EQ, Uid.NAME, "a.nikolic", false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        for (ConnectorObject connObj : actual) {
            Assert.assertEquals(false, connObj.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0));
        }
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }


    @Test
    public final void searchStartsWithAttribute() {
        printTestTitle(TEST_CLASS + "searchStartsWithAttribute()");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());

        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT, new Operand(Operator.SW, Uid.NAME, "mid", false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        Assert.assertEquals(1, actual.size());
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }

    @Test
    public final void searchEndsWithAttribute() {
        printTestTitle(TEST_CLASS + "searchEndsWithAttribute()");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT,
                new Operand(Operator.EW, Uid.NAME,
                        newAccount.getUidValue().substring(
                                newAccount.getUidValue().length() - 3,
                                newAccount.getUidValue().length()), false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        Assert.assertEquals(1, actual.size());
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }

    @Test
    public final void searchContainsAttribute() {
        printTestTitle(TEST_CLASS + "searchContainsAttribute()");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT, new Operand(Operator.C, Uid.NAME, "point", false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        Assert.assertEquals(1, actual.size());
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }

    @Test
    @Ignore
    public final void searchNotEqualsAttribute() {
        printTestTitle(TEST_CLASS + "searchNotEqualsAttribute()");
        newAccount = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT, new Operand(Operator.EQ, Uid.NAME, "test", true),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        Assert.assertTrue(actual.size() > 1);
        connector.delete(ObjectClass.ACCOUNT, newAccount, null);
    }

    @Test(expectedExceptions = ConnectorException.class)
    public final void searchNotExistsUser() {
        printTestTitle(TEST_CLASS + "searchNotExistsUser()");
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT, new Operand(Operator.EQ, Uid.NAME, attrs.getWrongUsername(), false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
    }

    @Test
    public final void searchUserWithSameShell() {
        printTestTitle(TEST_CLASS + "searchUserWithSameShell()");
        Name name1 = new Name(attrs.getUsername());
        Uid newAccount1 = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name1, attrs.getPassword(), true), null);
        Name name2 = new Name(attrs.getUsername());
        Uid newAccount2 = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name2, attrs.getPassword(), true), null);
        Name name3 = new Name(attrs.getUsername());
        Uid newAccount3 = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name3, attrs.getPassword(), true), null);
        Name name4 = new Name(attrs.getUsername());
        Uid newAccount4 = connector.create(ObjectClass.ACCOUNT,
                createSetOfAttributes(name4, attrs.getPassword(), true), null);
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT,
                new Operand(Operator.EQ, "shell", SharedTestMethods.DEFAUL_SHELL, false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
        Assert.assertEquals(4, actual.size());
        connector.delete(ObjectClass.ACCOUNT, newAccount1, null);
        connector.delete(ObjectClass.ACCOUNT, newAccount2, null);
        connector.delete(ObjectClass.ACCOUNT, newAccount3, null);
        connector.delete(ObjectClass.ACCOUNT, newAccount4, null);
    }

    @Test
    public final void searchGroup() {
        printTestTitle(TEST_CLASS + "searchGroup()");
        newAccount = connector.create(ObjectClass.GROUP,
                createSetOfAttributes(name, attrs.getPassword(), true), null);
        Assert.assertEquals(name.getNameValue(), newAccount.getUidValue());
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.GROUP, new Operand(Operator.EQ, Uid.NAME, newAccount.getUidValue(), false),
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject co) {
                        actual.add(co);
                        return true;
                    }
                }, null);
        for (ConnectorObject connObj : actual) {
            Assert.assertEquals(name.getNameValue(), connObj.getName().getNameValue());
        }
        connector.delete(ObjectClass.GROUP, newAccount, null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void executeQueryWithWrongObjectClass() {
        printTestTitle(TEST_CLASS + "executeQueryWithWrongObjectClass()");
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(attrs.getWrongObjectClass(), null,
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void executeQueryTestWithAllNull() {
        printTestTitle(TEST_CLASS + "executeQueryTestWithAllNull()");
        connector.executeQuery(null, null, null, null);
    }

    @Test
    public void executeForAllAccounts() {
        printTestTitle(TEST_CLASS + "executeForAllAccounts()");
        final Set<ConnectorObject> actual = new HashSet<ConnectorObject>();
        connector.executeQuery(ObjectClass.ACCOUNT, null,
                new ResultsHandler() {

                    @Override
                    public boolean handle(final ConnectorObject connObj) {
                        actual.add(connObj);
                        return true;
                    }
                }, null);
    }

    @AfterTest
    public final void close() {
        connector.dispose();
    }
}
