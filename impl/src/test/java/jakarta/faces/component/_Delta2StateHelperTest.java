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
package jakarta.faces.component;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.StateHelper;
import java.util.List;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class _Delta2StateHelperTest extends AbstractComponentTest
{
    
    public static class UITestComponent extends UIComponentBase
    {
        public UITestComponent()
        {
            super();
        }

        @Override
        public String getFamily()
        {
            return "jakarta.faces.Test";
        }

        private enum PropertyKeys
        {
            testProperty1,
            testProperty2
        }
        
        public String getTestProperty1()
        {
            return (String) getStateHelper().eval(PropertyKeys.testProperty1);
        }

        public void setTestProperty1(String testProperty1)
        {
            getStateHelper().put(PropertyKeys.testProperty1, testProperty1);
        }
        
        public String getTestProperty2()
        {
            return (String) getStateHelper().eval(PropertyKeys.testProperty2);
        }

        public void setTestProperty2(String testProperty2)
        {
            getStateHelper().put(PropertyKeys.testProperty2, testProperty2);
        }

        /*
        private StateHelper _stateHelper = null;
        
        public StateHelper getStateHelper(boolean create) {

            if (create && _stateHelper == null) 
            {
                _stateHelper = new _DeltaStateHelper(this);
            }
            return _stateHelper;
        }
        */
        
        public StateHelper getStateHelper()
        {
            return super.getStateHelper();
        }

        @Override
        public void restoreState(FacesContext context, Object state)
        {
            Object[] values = (Object[]) state;
            super.restoreState(context, values[0]);
            //getStateHelper().restoreState(context, values[1]);
        }

        @Override
        public Object saveState(FacesContext context)
        {
            Object[] values = new Object[2];
            values[0] = super.saveState(context);
            //values[1] = _stateHelper == null ? null : _stateHelper.saveState(context);
            return values;
        }
    }

    @Test
    public void testSimpleGetterSetter() throws Exception
    {
        UITestComponent a = new UITestComponent();
        Assertions.assertNull(a.getTestProperty1());
        a.setTestProperty1("testProperty1");
        Assertions.assertEquals("testProperty1", a.getTestProperty1());
        a.setTestProperty1(null);
        Assertions.assertNull(a.getTestProperty1());
    }
    
    @Test
    public void testEmptySaveRestore() throws Exception
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        Object state1 = (Object) a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        Assertions.assertEquals(a.getTestProperty1(), b.getTestProperty1());
        Assertions.assertEquals(a.getTestProperty2(), b.getTestProperty2());        
    }
    
    @Test
    public void testSimpleSaveRestore() throws Exception
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        a.setTestProperty1("testProperty1");
        a.setTestProperty2(null);
        Object state1 = (Object) a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        Assertions.assertEquals(a.getTestProperty1(), b.getTestProperty1());
        Assertions.assertEquals(a.getTestProperty2(), b.getTestProperty2());
    }
    
    @Test
    public void testDeltaStateSaveRestore()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        a.setTestProperty1("testProperty1");
        a.setTestProperty2(null);
        
        //Replicate the stuff to reach template status
        b.setTestProperty1("testProperty1");
        b.setTestProperty2(null);
        
        a.markInitialState();
        b.markInitialState();
        
        a.setTestProperty2("testProperty2");
        a.setTestProperty1(null);
        
        Object state1 = (Object) a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        Assertions.assertNull(a.getTestProperty1());
        Assertions.assertNull(b.getTestProperty1());        
        Assertions.assertEquals("testProperty2", a.getTestProperty2());
        Assertions.assertEquals("testProperty2", b.getTestProperty2());
    }
    
    @Test
    public void testPutPropertyStateHelper1()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        Assertions.assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.put("someProperty", "someOtherValue");
        
        Assertions.assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        Assertions.assertEquals("someOtherValue",retValue);
        
        a.clearInitialState();
        
        retValue = helper.put("someProperty", "someOtherOtherOtherValue");
        
        Assertions.assertEquals("someOtherOtherValue",retValue);
    }
    
    @Test
    public void testPutPropertyStateHelper2()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        Assertions.assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.put("someProperty", null);
        
        Assertions.assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        Assertions.assertNull(retValue);
        
        a.clearInitialState();
        
        retValue = helper.put("someProperty", null);
        
        Assertions.assertEquals("someOtherOtherValue",retValue);
    }
    
    @Test
    public void testRemovePropertyStateHelper1()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        Assertions.assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.remove("someProperty");
        
        Assertions.assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        Assertions.assertNull(retValue);
        
        a.clearInitialState();
        
        retValue = helper.remove("someProperty");
        
        Assertions.assertEquals("someOtherOtherValue",retValue);
    }
    
    @Test
    public void testAddItemOnList1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.add("somePropertyList", "someValue1");
        helperB.add("somePropertyList", "someValue1");
        a.markInitialState();
        b.markInitialState();

        helperA.add("somePropertyList", "someValue2");
        helperA.add("somePropertyList", "someValue3");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        List listA = (List) helperA.get("somePropertyList");
        
        Assertions.assertEquals("someValue1",listA.get(0));
        Assertions.assertEquals("someValue2",listA.get(1));
        Assertions.assertEquals("someValue3",listA.get(2));
        
        List listB = (List) helperB.get("somePropertyList");
        
        Assertions.assertEquals("someValue1",listB.get(0));
        Assertions.assertEquals("someValue2",listB.get(1));
        Assertions.assertEquals("someValue3",listB.get(2));
    }
    
    @Test
    public void testAddItemOnList2()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.add("somePropertyList", "someValue1");
        helperB.add("somePropertyList", "someValue1");
        a.markInitialState();
        b.markInitialState();

        helperA.add("somePropertyList", "someValue2");
        //helperA.add("somePropertyList", "someValue3");
        
        helperA.remove("somePropertyList","someValue1");
        helperA.remove("somePropertyList","someValue2");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        List listA = (List) helperA.get("somePropertyList");
        
        if (listA != null)
        {
            Assertions.assertFalse(listA.contains("someValue1"));
            Assertions.assertFalse(listA.contains("someValue2"));
        }
        
        List listB = (List) helperB.get("somePropertyList");

        if (listB != null)
        {
            Assertions.assertFalse(listB.contains("someValue2"));            
            Assertions.assertFalse(listB.contains("someValue1"));
        }
    }
    
    @Test
    public void testAddItemOnList3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.add("somePropertyList", "someValue1");
        helperB.add("somePropertyList", "someValue1");
        a.markInitialState();
        b.markInitialState();

        helperA.add("somePropertyList", "someValue2");
        helperA.add("somePropertyList", "someValue3");
        
        helperA.remove("somePropertyList","someValue1");
        helperA.remove("somePropertyList","someValue2");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        List listA = (List) helperA.get("somePropertyList");
        
        Assertions.assertTrue(listA.contains("someValue3"));
        Assertions.assertFalse(listA.contains("someValue1"));
        Assertions.assertFalse(listA.contains("someValue2"));
        
        List listB = (List) helperB.get("somePropertyList");

        Assertions.assertTrue(listB.contains("someValue3"));
        Assertions.assertFalse(listB.contains("someValue2"));            
        Assertions.assertFalse(listB.contains("someValue1"));
    }    
    
    @Test
    public void testPutItemOnMap1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.put("somePropertyMap","key1", "someValue1");
        helperB.put("somePropertyMap","key1", "someValue1");
        a.markInitialState();
        b.markInitialState();
        
        helperA.put("somePropertyMap","key2", "someValue2");
        helperA.put("somePropertyMap","key3", "someValue3");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Map mapA = (Map) helperA.get("somePropertyMap");
        
        Assertions.assertEquals("someValue1",mapA.get("key1"));
        Assertions.assertEquals("someValue2",mapA.get("key2"));
        Assertions.assertEquals("someValue3",mapA.get("key3"));        

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        Assertions.assertEquals("someValue1",mapB.get("key1"));
        Assertions.assertEquals("someValue2",mapB.get("key2"));
        Assertions.assertEquals("someValue3",mapB.get("key3"));        
    }
    
    @Test
    public void testPutRemoveItemOnMap2()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.put("somePropertyMap","key1", "someValue1");
        helperB.put("somePropertyMap","key1", "someValue1");
        a.markInitialState();
        b.markInitialState();
        
        helperA.put("somePropertyMap","key2", "someValue2");
        
        helperA.remove("somePropertyMap","key1");
        helperA.remove("somePropertyMap","key2");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Map mapA = (Map) helperA.get("somePropertyMap");
        
        if (mapA != null)
        {
            Assertions.assertNull(mapA.get("key2"));
            Assertions.assertNull(mapA.get("key1"));
        }

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        if (mapB != null)
        {
            Assertions.assertNull(mapB.get("key2"));
            Assertions.assertNull(mapB.get("key1"));
        }
    }
    
    @Test
    public void testPutRemoveItemOnMap3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        StateHelper helperA = a.getStateHelper();
        StateHelper helperB = b.getStateHelper();
        
        helperA.put("somePropertyMap","key1", "someValue1");
        helperB.put("somePropertyMap","key1", "someValue1");
        a.markInitialState();
        b.markInitialState();
        
        helperA.put("somePropertyMap","key2", "someValue2");
        helperA.put("somePropertyMap","key3", "someValue3");
        
        helperA.remove("somePropertyMap","key1");
        helperA.remove("somePropertyMap","key2");
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Map mapA = (Map) helperA.get("somePropertyMap");
        
        if (mapA != null)
        {
            Assertions.assertNull(mapA.get("key2"));
            Assertions.assertNull(mapA.get("key1"));
        }

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        if (mapB != null)
        {
            Assertions.assertNull(mapB.get("key2"));
            Assertions.assertNull(mapB.get("key1"));
        }
    }
    
    public static class TestPhaseListener1 implements PhaseListener
    {
        public TestPhaseListener1(){}
        
        @Override
        public void afterPhase(PhaseEvent event){}

        @Override
        public void beforePhase(PhaseEvent event){}

        @Override
        public PhaseId getPhaseId()
        {
            return PhaseId.ANY_PHASE;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof TestPhaseListener1)
            {
                return true;
            }
            return false;
        }
    }
    
    public static class TestPhaseListener2 implements PhaseListener
    {
        public TestPhaseListener2(){}
        
        @Override
        public void afterPhase(PhaseEvent event){}

        @Override
        public void beforePhase(PhaseEvent event){}

        @Override
        public PhaseId getPhaseId()
        {
            return PhaseId.ANY_PHASE;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof TestPhaseListener2)
            {
                return true;
            }
            return false;
        }
    }    
        
    @Test
    public void testUIViewRootPhaseListener1() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        
        PhaseListener phaseListener1 = new TestPhaseListener1();
        
        a.addPhaseListener(phaseListener1);
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Assertions.assertTrue(a.getPhaseListeners().contains(phaseListener1));
        Assertions.assertTrue(b.getPhaseListeners().contains(phaseListener1));
    }
    
    @Test
    public void testUIViewRootPhaseListener2() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        
        a.markInitialState();
        b.markInitialState();
        
        PhaseListener phaseListener1 = new TestPhaseListener1();
        
        a.addPhaseListener(phaseListener1);
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Assertions.assertTrue(a.getPhaseListeners().contains(phaseListener1));
        Assertions.assertTrue(b.getPhaseListeners().contains(phaseListener1));
    }
    
    @Test
    public void testUIViewRootPhaseListener3() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        UIViewRoot c = new UIViewRoot();

        PhaseListener phaseListener2 = new TestPhaseListener1();
        a.addPhaseListener(phaseListener2);
        b.addPhaseListener(phaseListener2);
        c.addPhaseListener(phaseListener2);
        
        a.markInitialState();
        b.markInitialState();
        c.markInitialState();
        
        PhaseListener phaseListener1 = new TestPhaseListener1();
        
        a.addPhaseListener(phaseListener1);
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        Assertions.assertTrue(a.getPhaseListeners().contains(phaseListener1));
        Assertions.assertTrue(b.getPhaseListeners().contains(phaseListener1));
        Assertions.assertTrue(a.getPhaseListeners().contains(phaseListener2));
        Assertions.assertTrue(b.getPhaseListeners().contains(phaseListener2));
        
        a.removePhaseListener(phaseListener1);
        a.removePhaseListener(phaseListener2);
        
        c.restoreState(facesContext, a.saveState(facesContext));
        
        Assertions.assertFalse(a.getPhaseListeners().contains(phaseListener1));
        Assertions.assertFalse(c.getPhaseListeners().contains(phaseListener1));
        Assertions.assertFalse(a.getPhaseListeners().contains(phaseListener2));
        Assertions.assertFalse(c.getPhaseListeners().contains(phaseListener2));
    }    
}
