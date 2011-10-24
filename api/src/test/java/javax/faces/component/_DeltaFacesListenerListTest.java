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

import java.util.Arrays;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesListener;

public class _DeltaFacesListenerListTest extends AbstractComponentTest
{
    public _DeltaFacesListenerListTest(String arg0)
    {
        super(arg0);
    }
    
    public static class UITestComponent extends UIComponentBase
    {
        
        public UITestComponent()
        {
        }

        @Override
        public String getFamily()
        {
            return "javax.faces.Test";
        }
    }
    
    public static class NoStateFacesListener implements FacesListener
    {
        public NoStateFacesListener()
        {
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof NoStateFacesListener)
            {
                return true;
            }
            return false;
        }
    }
    
    public static class NoStateFacesListener2 implements FacesListener
    {
        public NoStateFacesListener2()
        {
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof NoStateFacesListener2)
            {
                return true;
            }
            return false;
        }
    }
    
    public static class StateFacesListener implements FacesListener, StateHolder
    {
        private boolean _transient;
        
        String value;
        
        public StateFacesListener()
        {
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof StateFacesListener)
            {
                if (value == null)
                {
                    if (((StateFacesListener)obj).value == null)
                    {
                        return true;
                    }
                }
                else if (value.equals(((StateFacesListener)obj).value))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean isTransient()
        {
            return _transient;
        }

        public void setTransient(boolean _transient)
        {
            this._transient = _transient;
        }

        public void restoreState(FacesContext context, Object state)
        {
            value = (String) state;
        }

        public Object saveState(FacesContext context)
        {
            return value;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
    
    public static class PartialStateFacesListener extends StateFacesListener implements PartialStateHolder
    {
        private boolean initialStateMarked;
        
        @Override
        public void restoreState(FacesContext context, Object state)
        {
            if (state != null)
            {
                value = (String) state;
            }
        }

        @Override
        public Object saveState(FacesContext context)
        {
            if (!initialStateMarked())
            {
                return value;
            }
            return null;
        }

        public void clearInitialState()
        {
            initialStateMarked = false;
        }

        public boolean initialStateMarked()
        {
            return initialStateMarked;
        }

        public void markInitialState()
        {
            initialStateMarked = true;
        }        
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSimpleAddRemove()
    {
        UITestComponent a = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        a.addFacesListener(listener1);
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore2()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        a.markInitialState();
        b.markInitialState();
        FacesListener listener1 = new NoStateFacesListener(); 
        a.addFacesListener(listener1);
        Object [] savedState1 = (Object[]) a.saveState(facesContext);       
        b.restoreState(facesContext, savedState1);
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener();
        a.addFacesListener(listener1);
        b.addFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        assertNull(savedState1 == null ? null : savedState1[1]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore4()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener2();
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        a.removeFacesListener(listener2);
        b.removeFacesListener(listener2);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
    }
    
    public void testSimpleSaveRestore5()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        UITestComponent c = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener2();
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        a.removeFacesListener(listener2);
        b.removeFacesListener(listener2);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        
        //Save fully
        b.clearInitialState();
        c.restoreState(facesContext, b.saveState(facesContext));
        //c._facesListeners should be empty
        assertFalse(Arrays.asList(c.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(c.getFacesListeners(FacesListener.class)).contains(listener2));
    }
    
    public void testSimpleSaveRestore6()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener(); 
        StateFacesListener listener2 = new StateFacesListener();
        listener1.setValue("value1");
        listener2.setValue("value2");
        a.addFacesListener(listener1);
        b.addFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is not null because StateFacesListener is instance of StateHolder 
        // and always needs to be saved and restored!
        assertNotNull(savedState1[0]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore7()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        UITestComponent c = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener(); 
        StateFacesListener listener2 = new StateFacesListener();
        listener1.setValue("value1");
        listener2.setValue("value2");
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        a.removeFacesListener(listener2);
        b.removeFacesListener(listener2);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        
        //Save fully
        b.clearInitialState();
        c.restoreState(facesContext, b.saveState(facesContext));
        //c._facesListeners should be empty
        assertFalse(Arrays.asList(c.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(c.getFacesListeners(FacesListener.class)).contains(listener2));
    }
    
    public void testSimpleSaveRestore8()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new PartialStateFacesListener(); 
        listener1.setValue("value1");
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is null because StateFacesListener is instance of PartialStateHolder 
        assertNull(savedState1 == null ? null : savedState1[1]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));        
    }
    
    public void testSimpleSaveRestore9()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new PartialStateFacesListener(); 
        listener1.setValue("value1");
        StateFacesListener listener2 = new PartialStateFacesListener(); 
        listener2.setValue("value2");        
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addFacesListener(listener2);
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is null because StateFacesListener is instance of PartialStateHolder but a
        // listener was added after markInitialState
        assertNotNull(savedState1[0]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        a.removeFacesListener(listener1);
        b.removeFacesListener(listener1);
        assertFalse(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
    }    
    
    public void testSimpleSaveRestoreTransient1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient2()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        a.markInitialState();
        b.markInitialState();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener2 = new NoStateFacesListener2();
        a.addFacesListener(listener2);
        b.addFacesListener(listener2);        
        a.markInitialState();
        b.markInitialState();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient4()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addFacesListener(listener1);
        b.addFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertFalse(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
    }
    
    
    public void testComplexSaveRestore1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener();
        StateFacesListener listener2 = new PartialStateFacesListener();
        StateFacesListener listener3 = new StateFacesListener();
        a.addFacesListener(listener1);
        a.addFacesListener(listener2);
        a.addFacesListener(listener3);
        b.addFacesListener(listener1);
        b.addFacesListener(listener2);
        b.addFacesListener(listener3);
        a.markInitialState();
        b.markInitialState();
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(a.getFacesListeners(FacesListener.class)).contains(listener3));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener1));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener2));
        assertTrue(Arrays.asList(b.getFacesListeners(FacesListener.class)).contains(listener3));
    }
}
