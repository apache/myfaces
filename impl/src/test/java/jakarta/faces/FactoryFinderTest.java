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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.myfaces.test.mock.api.Mock2ApplicationFactory;
import org.apache.myfaces.test.mock.api.MockApplicationFactory;
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
    public void setUp() throws Exception
    {
        // this needs to be called *before* the first Test test is run,
        // as there may be left over FactoryFinder configurations from
        // that previous tests that may interfere with the first test here.
        FactoryFinder.releaseFactories();
    }

    @AfterEach
    public void tearDown() throws Exception
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
    public void testGetFactory() throws Exception
    {
        // no catch because if this fails the test fails, i.e. not trying to test
        // setFactory here
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        try
        {
            Object factory = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            Assertions.assertNotNull(factory);
            Assertions.assertTrue(factory.getClass().equals(MockApplicationFactory.class));
        }
        catch (IllegalStateException e)
        {
            Assertions.fail("Should not throw an illegal state exception");
        }
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    public void testGetFactoryTwice() throws Exception
    {
        // this test just makes sure that things work when the get has been called
        // more than once
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        try
        {
            Object factory1 = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            Assertions.assertNotNull(factory1);
            Assertions.assertTrue(factory1.getClass().equals(MockApplicationFactory.class));
            Object factory2 = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            Assertions.assertNotNull(factory2);
            Assertions.assertTrue(factory2.getClass().equals(MockApplicationFactory.class));
            Assertions.assertEquals(factory1, factory2);
        }
        catch (IllegalStateException e)
        {
            Assertions.fail("Should not throw an illegal state exception");
        }
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    public void testGetFactoryNoFactory() throws Exception
    {
        // no catch because if this fails the test fails, i.e. not trying to test
        // setFactory here
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
        try
        {
            FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            Assertions.fail("Should have thrown an illegal state exception");
        }
        catch (IllegalArgumentException e)
        {
            Assertions.assertNotNull(e.getMessage());
        }
    }

    /*
     * No configuration test, this should throw and deliver a useful message Test method for
     * 'jakarta.faces.FactoryFinder.getFactory(String)'
     */
    @Test
    public void testGetFactoryNoConfiguration() throws Exception
    {
        try
        {
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            Assertions.fail("Should have thrown an illegal state exception");
        }
        catch (IllegalStateException e)
        {
            Assertions.assertNotNull(e.getMessage());
            Assertions.assertTrue(e.getMessage().startsWith("No Factories configured for this Application"));
        }
    }

    /*
     * Bogus factory name test Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    public void testSetFactoryBogusName()
    {
        try
        {
            FactoryFinder.setFactory("BogusFactoryName", MockApplicationFactory.class.getName());
            Assertions.fail("Should have thrown an illegal argument exception");
        }
        catch (IllegalArgumentException e)
        {
            Assertions.assertNotNull(e.getMessage());
        }
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    public void testSetFactory() throws Exception
    {
        try
        {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                MockApplicationFactory.class.getName()));
        }
        catch (IllegalArgumentException e)
        {
            Assertions.fail("Should not throw an illegal argument exception");
        }
    }

    /*
     * If a factory has ever been handed out then setFactory is not supposed to change the factory layout. This test
     * checks to see if that is true. Test method for 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    public void testSetFactoryNoEffect() throws Exception
    {
        try
        {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                MockApplicationFactory.class.getName()));
            Assertions.assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                Mock2ApplicationFactory.class.getName()));
            // getFactory should cause setFactory to stop changing the
            // registered classes
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            // this should essentially be a no-op
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, Mock2ApplicationFactory.class.getName());
            Assertions.assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                Mock2ApplicationFactory.class.getName()));
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                MockApplicationFactory.class.getName()));
        }
        catch (IllegalArgumentException e)
        {
            Assertions.fail("Should not throw an illegal argument exception");
        }
    }

    /*
     * Adding factories should add the class name to the list of avalable class names Test method for
     * 'jakarta.faces.FactoryFinder.setFactory(String, String)'
     */
    @Test
    public void testSetFactoryAdditiveClassNames() throws Exception
    {
        try
        {
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                MockApplicationFactory.class.getName()));
            Assertions.assertFalse(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                Mock2ApplicationFactory.class.getName()));
            FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, Mock2ApplicationFactory.class.getName());
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                Mock2ApplicationFactory.class.getName()));
            Assertions.assertTrue(registeredFactoryNames(FactoryFinder.APPLICATION_FACTORY).contains(
                MockApplicationFactory.class.getName()));
        }
        catch (IllegalArgumentException e)
        {
            Assertions.fail("Should not throw an illegal argument exception");
        }
    }

    /*
     * Test method for 'jakarta.faces.FactoryFinder.releaseFactories()'
     */
    @Test
    public void testReleaseFactories()
    {

    }
}
