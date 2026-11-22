/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jakarta.faces;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.myfaces.test.mock.MockApplicationFactory;
import  org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FactoryFinderTest
{

    public FactoryFinderTest()
    {
    }

    @BeforeEach
    void setUp() throws Exception
    {
        // this needs to be called *before* the first Test test is run,
        // as there may be left over FactoryFinder configurations from
        // that previous tests that may interfere with the first test here.
        FactoryFinder.releaseFactories();
    }

    @AfterEach
    void tearDown() throws Exception
    {
        // call this again so there is no possibility of messing up tests that will
        // run after this one
        FactoryFinder.releaseFactories();
        releaseRegisteredFactoryNames();
    }

    private void releaseRegisteredFactoryNames() throws Exception
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Map<ClassLoader, Map<String, List<String>>> _registeredFactoryNames = getRegisteredFactoryNames();
        _registeredFactoryNames.remove(classLoader);
    }

    private List<String> registeredFactoryNames(String factoryName) throws Exception
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Map<ClassLoader, Map<String, List<String>>> _registeredFactoryNames = getRegisteredFactoryNames();
        Map<String, List<String>> map = _registeredFactoryNames.get(classLoader);
        return map.get(factoryName);
    }

    /*
     * This method allows us access to the _registeredFactoryNames field so we can test the content of that map during
     * the running of this test.
     * 
     * @return Returns the _registeredFactoryNames Map from the FactoryFinder class. @throws NoSuchFieldException
     * 
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private Map<ClassLoader, Map<String, List<String>>> getRegisteredFactoryNames() throws IllegalAccessException
    {
        Class<FactoryFinder> factoryFinderClass = FactoryFinder.class;
        Field fields[] = factoryFinderClass.getDeclaredFields();
        Field field = null;
        for (int i = 0; i < fields.length; i++)
        {
            if (fields[i].getName().equals("registeredFactoryNames"))
            {
                field = fields[i];
                field.setAccessible(true);
                break;
            }
        }

        Map<ClassLoader, Map<String, List<String>>> _registeredFactoryNames = (Map<ClassLoader, Map<String, List<String>>>) field
                                                                                                                                 .get(null);

        return _registeredFactoryNames;
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    void getFactory() throws Exception
    {
        // no catch because if this fails the test fails, i.e. not trying to test
        // setFactory here
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        Assertions.assertDoesNotThrow(() -> {
            Object factory = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            assertNotNull(factory);
            assertEquals(MockApplicationFactory.class, factory.getClass());
        }, "Should not throw an illegal state exception");
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    void getFactoryTwice() throws Exception
    {
        // this test just makes sure that things work when the get has been called
        // more than once
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        Assertions.assertDoesNotThrow(() -> {
            Object factory1 = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            assertNotNull(factory1);
            assertEquals(MockApplicationFactory.class, factory1.getClass());
            Object factory2 = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            assertNotNull(factory2);
            assertEquals(MockApplicationFactory.class, factory2.getClass());
            assertEquals(factory1, factory2);
        }, "Should not throw an illegal state exception");
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    void getFactoryNoFactory() throws Exception
    {
        // no catch because if this fails the test fails, i.e. not trying to test
        // setFactory here
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        try
        {
            FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            fail("Should have thrown an illegal state exception");
        }
        catch (IllegalArgumentException e)
        {
            assertNotNull(e.getMessage());
        }
    }

    /*
     * No configuration test, this should throw and deliver a useful message Test method for
     * 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    void getFactoryNoConfiguration() throws Exception
    {
        try
        {
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            fail("Should have thrown an illegal state exception");
        }
        catch (IllegalStateException e)
        {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().startsWith("No Factories configured for this Application"));
        }
    }

    /*
     * Bogus factory name test Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    void setFactoryBogusName()
    {
        try
        {
            FactoryFinder.setFactory("BogusFactoryName", MockApplicationFactory.class.getName());
            fail("Should have thrown an illegal argument exception");
        }
        catch (IllegalArgumentException e)
        {
            assertNotNull(e.getMessage());
        }
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    void setFactory() throws Exception
    {
        Assertions.assertDoesNotThrow(() -> {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory.class.getName()));
        }, "Should not throw an illegal argument exception");
    }

    /*
     * If a factory has ever been handed out then setFactory is not supposed to change the factory layout. This test
     * checks to see if that is true. Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    void setFactoryNoEffect() throws Exception
    {
        Assertions.assertDoesNotThrow(() -> {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory.class.getName()));
            assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory2.class.getName()));
            // getFactory should cause setFactory to stop changing the
            // registered classes
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            // this should essentially be a no-op
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory2.class.getName());
            assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory2.class.getName()));
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory.class.getName()));
        }, "Should not throw an illegal argument exception");
    }

    /*
     * Adding factories should add the class name to the list of available class names Test method for
     * 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    void setFactoryAdditiveClassNames() throws Exception
    {
        Assertions.assertDoesNotThrow(() -> {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory.class.getName()));
            assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory2.class.getName()));
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory2.class.getName());
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory2.class.getName()));
            assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                    MockApplicationFactory.class.getName()));
        }, "Should not throw an illegal argument exception");
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.releaseFactories()'
     */
    @Test
    void releaseFactories()
    {

    }

    public class MockApplicationFactory2 extends MockApplicationFactory { }
}
