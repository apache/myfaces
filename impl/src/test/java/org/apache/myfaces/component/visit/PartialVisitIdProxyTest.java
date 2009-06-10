/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.myfaces.component.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Rev$ $Date$
 */
public class PartialVisitIdProxyTest extends TestCase {

    Collection<String> defaultSetup;
    PartialVisitIdProxy instance;

    public PartialVisitIdProxyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultSetup = new ArrayList<String>(3);

        defaultSetup.add("panel1:control1");
        defaultSetup.add("panel1:panel2:control2");
        defaultSetup.add("panel3:panel2:control1");

        instance = new PartialVisitIdProxy(':', defaultSetup);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        defaultSetup = null;
    }

    /**
     * Test of getIds method, of class PartialVisitIdProxy.
     */
    public void testGetIds() {
        Set<String> ids = instance.getIds();
        assertTrue("id size must be 3", ids.size() == 3);
        assertTrue("contains all elements given",
                ids.contains("panel1:control1") &&
                ids.contains("panel1:panel2:control2") &&
                ids.contains("panel3:panel2:control1"));
    }

    /**
     * Test of getForcedIds method, of class PartialVisitIdProxy.
     */
    public void testGetForcedIds() {
        Set<String> forcedIds = instance.getForcedIds();
        assertTrue("forced ids must be empty or null in default setup", forcedIds == null || forcedIds.isEmpty());
        //now we make a tearup with forcedIds
        instance.add("panel1:control1");
        instance.add("panel1:panel2:control2");
        instance.add("control3");
        forcedIds = instance.getForcedIds();
       
        assertTrue("now one forcedId must exist", forcedIds.size() == 1 && forcedIds.contains("control3"));
    }

    /**
     * Test of getInverseCache method, of class PartialVisitIdProxy.
     */
    public void testGetInverseCache() {
        Map<String, Collection<String>> inverseCache = instance.getInverseCache();
        
        assertTrue("inverseCache size must be 4", inverseCache.size() == 4);
        assertTrue("keys must be the named ids",
                inverseCache.containsKey("panel1") &&
                inverseCache.containsKey("panel1:panel2") &&
                inverseCache.containsKey("panel3:panel2") &&
                inverseCache.containsKey("panel3")
                );

        //element test
        assertTrue("content test", inverseCache.get("panel1").iterator().next().equals("panel1:control1"));
        assertTrue("content test2", inverseCache.get("panel1:panel2").iterator().next().equals("panel1:panel2:control2"));
        assertTrue("content test", inverseCache.get("panel3:panel2").iterator().next().equals("panel3:panel2:control1"));
        assertTrue("content test", inverseCache.get("panel3").iterator().next().equals("panel3:panel2:control1"));

    }

    public void testSize() {
        assertTrue("size must be 3", instance.size() == 3);
    }

    /**
     * Test of isEmpty method, of class PartialVisitIdProxy.
     */
    public void testIsEmpty() {
        assertTrue("empty must be false", !instance.isEmpty());
    }

    /**
     * Test of contains method, of class PartialVisitIdProxy.
     */
    public void testContains() {
        assertTrue("containes test1", instance.contains("panel1:panel2:control2"));
        assertTrue("containes test2", !instance.contains("panel1"));

    }

    /**
     * Test of iterator method, of class PartialVisitIdProxy.
     */
    public void testIterator() {
        
        Iterator<String> testIt = instance.iterator();
        int cnt = 0;
       
        while (testIt.hasNext()) {
            String element = testIt.next();
            switch (cnt) {
                case 0:
                    element.equals("panel1:control1");
                    break;
                case 1:
                    element.equals("panel1:panel2:control2");
                    break;
                case 2:
                    element.equals("panel3:panel2:control1");
                    break;
                default:
                    fail("illegal element number");
            }
            cnt++;
        }
        int oldIndexSize = instance.getInverseCache().size();

        testIt.remove();
        //now lets check the object number:
        assertTrue("proxy size must be 2", instance.size() == 2);
        assertTrue("also the other data strcutures must be reduced", instance.getIds().size() == 2);

        //we cannot rely on the order of the given ids since we have to enforce sets!
        assertTrue("also the other data strcutures must be reduced", instance.getInverseCache().size() < oldIndexSize);

    }

    /**
     * Test of toArray method, of class PartialVisitIdProxy.
     */
    public void testToArray_0args() {


        Object[] result = instance.toArray();
        assertTrue(result.length == 3);
        assertArrayCorrect(result);

    }

    /**
     * Test of toArray method, of class PartialVisitIdProxy.
     */
    public void testToArray_GenericType() {
        String[] result = instance.toArray(new String[instance.size()]);
        assertArrayCorrect(result);
    }

    /**
     * Test of add method, of class PartialVisitIdProxy.
     */
    public void testAdd() {
        instance.add("panel4:control3");
        
        assertTrue("sizes must have changed", instance.size() == 4 && instance.getInverseCache().size() == 5);
        instance.add("control4");
        assertTrue("this control goes into the forcedIds",
                instance.size() == 5 &&
                instance.getInverseCache().size() == 5 &&
                instance.getForcedIds().size() == 1);

    }

    /**
     * Test of remove method, of class PartialVisitIdProxy.
     */
    public void testRemove() {
        //we reuse testAdd to build up a more sophisticated test data structure
        testAdd();
        instance.remove("control4");

        assertTrue("sizes must have changed", instance.size() == 4 && instance.getInverseCache().size() == 5 && instance.getForcedIds().size() == 0);
        instance.remove("panel4:control3");
        assertTrue("sizes must have changed", instance.size() == 3 && instance.getInverseCache().size() == 4);
    }

    /**
     * Test of containsAll method, of class PartialVisitIdProxy.
     */
    public void testContainsAll() {
       Set<String> testSet = new HashSet<String>();
       testSet.add("panel1:control1");
       testSet.add("panel3:panel2:control1");

       assertTrue("contains all must be true", instance.containsAll(testSet) );
       testSet.add("boogy");

       assertFalse(instance.containsAll(testSet));

    }

    /**
     * Test of addAll method, of class PartialVisitIdProxy.
     */
    public void testAddAll() {
       Set<String> testSet = new HashSet<String>();
       testSet.add("panel4:control1");
       testSet.add("panel4:panel2:control1");
       testSet.add("control4");

       instance.addAll(testSet);

       assertTrue("the newly added elements must be present in various parts of the system",
               instance.getInverseCache().containsKey("panel4:panel2") &&
               instance.getForcedIds().contains("control4") &&
               instance.getIds().contains("control4") &&
               instance.getIds().contains("panel4:panel2:control1")
       );

    }

    /**
     * Test of retainAll method, of class PartialVisitIdProxy.
     */
    public void testRetainAll() {
       instance.add("control3");
 
       Set<String> testSet = new HashSet<String>();
       testSet.add("panel1:control1");
       testSet.add("panel1:panel2:control2");

       instance.retainAll(testSet);

       //now as usual the structural test
       assertTrue("only the elements in the testSet should be present",
               testSet.size() == 2 &&
               testSet.contains("panel1:control1") &&
               testSet.contains("panel1:panel2:control2")
               );



    }

    /**
     * Test of removeAll method, of class PartialVisitIdProxy.
     */
    public void testRemoveAll() {
       instance.add("control3");
       Set<String> testSet = new HashSet<String>();
       testSet.add("panel1:control1");
       testSet.add("panel1:panel2:control2");
       testSet.add("control3");

       instance.removeAll(testSet);

       assertTrue(instance.size() == 1 && instance.getForcedIds().size() == 0 &&
               instance.contains("panel3:panel2:control1"));

    }

    /**
     * Test of clear method, of class PartialVisitIdProxy.
     */
    public void testClear() {
        instance.add("control3");
        instance.clear();
        assertTrue(instance.size() == 0 && instance.getIds().size() == 0&
                instance.getInverseCache().size() == 0 &&
                instance.getForcedIds().size() == 0);
    }

    private void assertArrayCorrect(Object[] result) {
        for (Object element : result) {
            String sElement = (String) element;
            if (sElement.equals("panel3:panel2:control1")) {
                return;
            }
        }
        fail("element not found after toArray");
    }
}
