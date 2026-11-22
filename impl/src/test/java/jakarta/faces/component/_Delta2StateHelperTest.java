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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import org.apache.myfaces.test.el.MockValueExpression;
import org.junit.jupiter.api.Test;


class _Delta2StateHelperTest extends AbstractComponentTest
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
            testProperty2,
            testPropertyWithDefault
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

        public Long getTestPropertyWithDefault()
        {
            return (Long) getStateHelper().eval(PropertyKeys.testPropertyWithDefault, Long.MAX_VALUE);
        }

        public void setTestPropertyWithDefault(Long testPropertyWithDefault)
        {
            getStateHelper().put(PropertyKeys.testPropertyWithDefault, testPropertyWithDefault);
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
            Object[] values = new Object[3];
            values[0] = super.saveState(context);
            //values[1] = _stateHelper == null ? null : _stateHelper.saveState(context);
            return values;
        }
    }

    @Test
    void simpleGetterSetter() throws Exception
    {
        UITestComponent a = new UITestComponent();
        assertNull(a.getTestProperty1());
        a.setTestProperty1("testProperty1");
        assertEquals("testProperty1", a.getTestProperty1());
        a.setTestProperty1(null);
        assertNull(a.getTestProperty1());
    }

    @Test
    void emptySaveRestore() throws Exception
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        Object state1 = a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        assertEquals(a.getTestProperty1(), b.getTestProperty1());
        assertEquals(a.getTestProperty2(), b.getTestProperty2());        
    }

    @Test
    void simpleSaveRestore() throws Exception
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        
        a.setTestProperty1("testProperty1");
        a.setTestProperty2(null);
        Object state1 = a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        assertEquals(a.getTestProperty1(), b.getTestProperty1());
        assertEquals(a.getTestProperty2(), b.getTestProperty2());
    }

    @Test
    void deltaStateSaveRestore()
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
        
        Object state1 = a.saveState(facesContext);

        b.restoreState(facesContext, state1);

        Object state2 = b.saveState(facesContext);

        assertNull(a.getTestProperty1());
        assertNull(b.getTestProperty1());        
        assertEquals("testProperty2", a.getTestProperty2());
        assertEquals("testProperty2", b.getTestProperty2());
    }

    @Test
    void putPropertyStateHelper1()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.put("someProperty", "someOtherValue");
        
        assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        assertEquals("someOtherValue",retValue);
        
        a.clearInitialState();
        
        retValue = helper.put("someProperty", "someOtherOtherOtherValue");
        
        assertEquals("someOtherOtherValue",retValue);
    }

    // https://issues.apache.org/jira/browse/MYFACES-4590
    @Test
    void propertyWithDefault()
    {
        UITestComponent a = new UITestComponent();

        assertEquals(Long.MAX_VALUE, a.getTestPropertyWithDefault(),
                "Property should be Long.MAX_VALUE from default)");

        ValueExpression valueExpression = new MockValueExpression("#{testPropertyWithDefault}", Object.class);
        a.setValueExpression("testPropertyWithDefault", valueExpression);
        Map applicationMap = externalContext.getApplicationMap();
        applicationMap.put("testPropertyWithDefault", 100L);

        assertEquals(100L, a.getTestPropertyWithDefault(),
                "Property should be 100L from expression");

        applicationMap.remove("testPropertyWithDefault");

        assertEquals(Long.MAX_VALUE, a.getStateHelper().eval("testPropertyWithDefault", Long.MAX_VALUE),
                "Value from stateHelper should be Long.MAX_VALUE from default");
        assertEquals(Long.MAX_VALUE, a.getStateHelper().eval("testPropertyWithDefault", () -> Long.MAX_VALUE),
                "Value from stateHelper should be Long.MAX_VALUE from default");
        assertEquals(Long.MAX_VALUE, a.getTestPropertyWithDefault(),
                "Property should be Long.MAX_VALUE from default");

    }

    @Test
    void putPropertyStateHelper2()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.put("someProperty", null);
        
        assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        assertNull(retValue);
        
        a.clearInitialState();
        
        retValue = helper.put("someProperty", null);
        
        assertEquals("someOtherOtherValue",retValue);
    }

    @Test
    void removePropertyStateHelper1()
    {
        UITestComponent a = new UITestComponent();
        
        StateHelper helper = a.getStateHelper();
        
        Object retValue = helper.put("someProperty", "someValue");
        //No value previously set
        assertNull(retValue);
        
        a.markInitialState();
        
        retValue = helper.remove("someProperty");
        
        assertEquals("someValue",retValue);
        
        retValue = helper.put("someProperty", "someOtherOtherValue");
        
        assertNull(retValue);
        
        a.clearInitialState();
        
        retValue = helper.remove("someProperty");
        
        assertEquals("someOtherOtherValue",retValue);
    }

    @Test
    void addItemOnList1()
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
        
        assertEquals("someValue1",listA.get(0));
        assertEquals("someValue2",listA.get(1));
        assertEquals("someValue3",listA.get(2));
        
        List listB = (List) helperB.get("somePropertyList");
        
        assertEquals("someValue1",listB.get(0));
        assertEquals("someValue2",listB.get(1));
        assertEquals("someValue3",listB.get(2));
    }

    @Test
    void addItemOnList2()
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
            assertFalse(listA.contains("someValue1"));
            assertFalse(listA.contains("someValue2"));
        }
        
        List listB = (List) helperB.get("somePropertyList");

        if (listB != null)
        {
            assertFalse(listB.contains("someValue2"));            
            assertFalse(listB.contains("someValue1"));
        }
    }

    @Test
    void addItemOnList3()
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
        
        assertTrue(listA.contains("someValue3"));
        assertFalse(listA.contains("someValue1"));
        assertFalse(listA.contains("someValue2"));
        
        List listB = (List) helperB.get("somePropertyList");

        assertTrue(listB.contains("someValue3"));
        assertFalse(listB.contains("someValue2"));            
        assertFalse(listB.contains("someValue1"));
    }

    @Test
    void putItemOnMap1()
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
        
        assertEquals("someValue1",mapA.get("key1"));
        assertEquals("someValue2",mapA.get("key2"));
        assertEquals("someValue3",mapA.get("key3"));        

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        assertEquals("someValue1",mapB.get("key1"));
        assertEquals("someValue2",mapB.get("key2"));
        assertEquals("someValue3",mapB.get("key3"));        
    }

    @Test
    void putRemoveItemOnMap2()
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
            assertNull(mapA.get("key2"));
            assertNull(mapA.get("key1"));
        }

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        if (mapB != null)
        {
            assertNull(mapB.get("key2"));
            assertNull(mapB.get("key1"));
        }
    }

    @Test
    void putRemoveItemOnMap3()
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
            assertNull(mapA.get("key2"));
            assertNull(mapA.get("key1"));
        }

        Map mapB = (Map) helperB.get("somePropertyMap");
        
        if (mapB != null)
        {
            assertNull(mapB.get("key2"));
            assertNull(mapB.get("key1"));
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
    void uIViewRootPhaseListener1() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        
        PhaseListener phaseListener1 = new TestPhaseListener1();
        
        a.addPhaseListener(phaseListener1);
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        assertTrue(a.getPhaseListeners().contains(phaseListener1));
        assertTrue(b.getPhaseListeners().contains(phaseListener1));
    }

    @Test
    void uIViewRootPhaseListener2() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        
        a.markInitialState();
        b.markInitialState();
        
        PhaseListener phaseListener1 = new TestPhaseListener1();
        
        a.addPhaseListener(phaseListener1);
        
        b.restoreState(facesContext, a.saveState(facesContext));
        
        assertTrue(a.getPhaseListeners().contains(phaseListener1));
        assertTrue(b.getPhaseListeners().contains(phaseListener1));
    }

    @Test
    void uIViewRootPhaseListener3() throws Exception
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
        
        assertTrue(a.getPhaseListeners().contains(phaseListener1));
        assertTrue(b.getPhaseListeners().contains(phaseListener1));
        assertTrue(a.getPhaseListeners().contains(phaseListener2));
        assertTrue(b.getPhaseListeners().contains(phaseListener2));
        
        a.removePhaseListener(phaseListener1);
        a.removePhaseListener(phaseListener2);
        
        c.restoreState(facesContext, a.saveState(facesContext));
        
        assertFalse(a.getPhaseListeners().contains(phaseListener1));
        assertFalse(c.getPhaseListeners().contains(phaseListener1));
        assertFalse(a.getPhaseListeners().contains(phaseListener2));
        assertFalse(c.getPhaseListeners().contains(phaseListener2));
    }    
}
