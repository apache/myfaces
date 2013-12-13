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
package javax.faces.component;

import java.util.List;
import java.util.Map;

/**
 * A generic framework less testcase for our _DeltaStateHelper class!
 */
public class _DeltaStateHelperTest extends AbstractComponentTest
{

    private static final String KEY3 = "key3";
    private static final String KEY5 = "key5";
    private static final String KEY_2_1 = "key_2_1";
    private static final String VAL1 = "val1";
    private static final String VAL2 = "val2";
    private static final String VAL3 = "val3";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY_2_2 = "key_2_2";
    private static final String VAL5 = "val5";
    ProbeDeltaStateHelper _instance = null;

    private void assertStructure()
    {
        assertTrue("check for key1", _instance.get(KEY1).equals(VAL1));
        assertTrue("check for key2", _instance.get(KEY2) instanceof Map);
        assertTrue("check for key3", _instance.get(KEY3) instanceof List);

        assertTrue("check for list size",
                ((List) _instance.get(KEY3)).size() >= 1);
        assertTrue("check for map entries", ((Map) _instance.get(KEY2)).get(
                KEY_2_2).equals(VAL3));
        assertTrue("check for map entries", ((Map) _instance.get(KEY2)).get(
                KEY_2_1).equals(VAL2));

    }

    /**
     * class needed to get a jsf less behavior
     * so that we can add a jsf less testcase here!
     */
    class ProbeDeltaStateHelper extends _DeltaStateHelper
    {

        boolean _initialStateMarked = true;

        public ProbeDeltaStateHelper()
        {
            super(null);
        }

        @Override
        protected boolean isInitialStateMarked()
        {
            return _initialStateMarked;
        }

        public void setInitialStateMarked(boolean initState)
        {
            _initialStateMarked = initState;
        }
    }

    public _DeltaStateHelperTest(String testName)
    {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception
    {

        super.setUp();

        _instance = new ProbeDeltaStateHelper();
        _instance.setInitialStateMarked(true);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        _instance = null;
    }

    /**
     * Test of isInitalStateMarked method, of class _DeltaStateHelper.
     */
    public void testIsInitalStateMarked()
    {
        assertTrue("Initial state must be marked", _instance
                .isInitialStateMarked());
        _instance.setInitialStateMarked(false);
        assertFalse("Initial state must be false", _instance
                .isInitialStateMarked());
    }

    /**
     * Test of add method, of class _DeltaStateHelper.
     */
    public void testAdd()
    {
        _instance.add(KEY1, VAL1);
        Object val = _instance.get(KEY1);
        assertTrue("Value must be list", val instanceof List);
        assertTrue("Value size must be one", ((List) val).size() == 1);

        _instance.add(KEY1, new Integer(2));
        _instance.add(KEY2, new Integer(2));

        val = _instance.get(KEY1);
        assertTrue("Value must be list", val instanceof List);
        assertTrue("Value size must be one", ((List) val).size() == 2);

        assertTrue("Value msut be of type string and must equal val1",
                ((List) val).get(0).equals(VAL1));

        assertTrue("Value msut be of type int and must equal 2", ((List) val)
                .get(1).equals(new Integer(2)));

        val = _instance.get(KEY2);
        assertTrue("Value must be list", val instanceof List);
        assertTrue("Value size must be one", ((List) val).size() == 1);
    }

    /**
     * specialized setup for our get tests
     */
    private void _setupGetTests()
    {

        _instance.put(KEY1, VAL1);
        _instance.put(KEY2, KEY_2_1, VAL2);
        _instance.put(KEY2, KEY_2_2, VAL3);

        _instance.add(KEY3, VAL3);
    }

    /**
     * Test of get method, of class _DeltaStateHelper.
     */
    public void testGet()
    {
        _setupGetTests();
        assertStructure();
    }

    /**
     * Test of put method, of class _DeltaStateHelper.
     */
    public void testPut_Serializable_Object()
    {
        _setupGetTests();

        assertTrue("check for key1", _instance.get(KEY1).equals(VAL1));

        Map entry = (Map) _instance.get(KEY2);
        assertTrue("check for key2", _instance.get(KEY2) instanceof Map);

        assertTrue("check for key2 structure", entry.size() == 2
                && entry.get(KEY_2_1).equals(VAL2)
                && entry.get(KEY_2_2).equals(VAL3));
    }

    public void testPut_null()
    {
        _instance.put(KEY1, null);
        _instance.put(KEY2, null);

        assertNull("key1 is not null", _instance.get(KEY1));
        assertNull("key2 is not null", _instance.get(KEY2));

        _setupGetTests();
        assertTrue("check for key1", _instance.get(KEY1).equals(VAL1));

        Map entry = (Map) _instance.get(KEY2);
        assertTrue("check for key2", _instance.get(KEY2) instanceof Map);

        assertTrue("check for key2 structure", entry.size() == 2
                && entry.get(KEY_2_1).equals(VAL2)
                && entry.get(KEY_2_2).equals(VAL3));

        _instance.put(KEY1, null);
        assertNull("key1 is not null", _instance.get(KEY1));
    }

    /**
     * Test of put method, of class _DeltaStateHelper.
     */
    public void testPut_3args()
    {
        //covered already by testPut_Serializable_Object()
    }

    /**
     * Test of remove method, of class _DeltaStateHelper.
     */
    public void testRemove_Serializable()
    {
        _setupGetTests();
        _instance.remove(KEY1);
        assertTrue("key 1 should not exist anymore",
                _instance.get(KEY1) == null);
        //TODO check the deleted data structure for further internal structural tests
    }

    /**
     * Test of remove method, of class _DeltaStateHelper.
     */
    public void testRemove_Serializable_Object()
    {
        _setupGetTests();
        _instance.remove(KEY2, KEY_2_1);
        _instance.remove(KEY2, KEY_2_2);

        _instance.remove(KEY3, VAL3);

        assertTrue("no key2 should exist anymore", _instance.get(KEY2) == null);
        assertTrue("key3 also should not exist anymore",
                _instance.get(KEY3) == null);
    }

    /**
     * Test of saveState method, of class _DeltaStateHelper.
     */
    public void testSaveState()
    {

        _instance.setInitialStateMarked(false);
        _setupGetTests();

        //save stating does not need a facesContext for now!
        Object retVal = _instance.saveState(facesContext);

        assertTrue("retVal must be an array", retVal instanceof Object[]);
        assertTrue("arraylength must be given", ((Object[]) retVal).length > 0);
        //only basic structural tests are done here
        //the more important ones are the ones in restorestate

        //now lets do some structural tests
        //theoretically there should be almot no data in the delta state if the full state already has been stored!
        _instance.setInitialStateMarked(true);
        _instance.put(KEY5, VAL5);
        Object[] deltaSaveState = (Object[]) _instance.saveState(facesContext);
        //only the new value should be saved as delta
        assertTrue("Delta Savestate structure", deltaSaveState.length == 2
                && deltaSaveState[0].equals(KEY5)
                && deltaSaveState[1].equals(VAL5));

    }

    /**
     * Test of restoreState method, of class _DeltaStateHelper.
     */
    public void testRestoreState()
    {
        _setupGetTests();
        _instance.setInitialStateMarked(false);
        Object serializedState = _instance.saveState(facesContext);
        _instance.restoreState(facesContext, serializedState);
        assertStructure();

        _setupGetTests();
        _instance.setInitialStateMarked(true);
        serializedState = _instance.saveState(facesContext);
        _instance.restoreState(facesContext, serializedState);
        assertStructure();

        _instance.setInitialStateMarked(true);
        _setupGetTests();
        serializedState = _instance.saveState(facesContext);
        _instance.restoreState(facesContext, serializedState);
        assertStructure();
    }

    /**
     * Test of isTransient method, of class _DeltaStateHelper.
     */
    public void testIsTransient()
    {
        _instance.setTransient(true);
        assertTrue(_instance.isTransient());
    }
}
