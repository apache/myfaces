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

import jakarta.faces.event.FacesListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.context.FacesContext;

public class _DeltaListTest extends AbstractComponentTest
{
    public _DeltaListTest(String arg0)
    {
        super(arg0);
    }
    
    public static class UITestComponent extends UIComponentBase
    {
        public _DeltaList<FacesListener> _facesListeners = null;
        
        public UITestComponent()
        {
        }

        @Override
        public String getFamily()
        {
            return "jakarta.faces.Test";
        }
        
        public void addTestFacesListener(FacesListener listener)
        {
            if (listener == null)
            {
                throw new NullPointerException("listener");
            }
            if (_facesListeners == null)
            {
                _facesListeners = new _DeltaList<FacesListener>();
            }
            _facesListeners.add(listener);
        }
        
        public FacesListener[] getTestFacesListeners(Class clazz)
        {
            if (clazz == null)
            {
                throw new NullPointerException("Class is null");
            }
            if (!FacesListener.class.isAssignableFrom(clazz))
            {
                throw new IllegalArgumentException("Class " + clazz.getName() + " must implement " + FacesListener.class);
            }

            if (_facesListeners == null)
            {
                return (FacesListener[]) Array.newInstance(clazz, 0);
            }
            List<FacesListener> lst = null;
            for (Iterator<FacesListener> it = _facesListeners.iterator(); it.hasNext();)
            {
                FacesListener facesListener = it.next();
                if (clazz.isAssignableFrom(facesListener.getClass()))
                {
                    if (lst == null)
                    {
                        lst = new ArrayList<FacesListener>();
                    }
                    lst.add(facesListener);
                }
            }
            if (lst == null)
            {
                return (FacesListener[]) Array.newInstance(clazz, 0);
            }

            return lst.toArray((FacesListener[]) Array.newInstance(clazz, lst.size()));
        }
        
        public void removeTestFacesListener(FacesListener listener)
        {
            if (listener == null)
            {
                throw new NullPointerException("listener is null");
            }

            if (_facesListeners != null)
            {
                _facesListeners.remove(listener);
            }
        }
                
        @Override
        public void clearInitialState()
        {
            super.clearInitialState();
            if (_facesListeners != null)
            {
                _facesListeners.clearInitialState();
            }
        }

        @Override
        public void markInitialState()
        {
            super.markInitialState();
            if (_facesListeners != null)
            {
                _facesListeners.markInitialState();
            }
        }

        @Override
        public void restoreState(FacesContext facesContext, Object state)
        {
            if (state == null)
            {
                return;
            }
            
            Object[] values = (Object[])state;
            super.restoreState(facesContext,values[0]);
            if (initialStateMarked())
            {
                if (values[1] instanceof _AttachedDeltaWrapper)
                {
                    //Delta
                    if (_facesListeners != null)
                    {
                        ((StateHolder) _facesListeners).restoreState(facesContext,
                                ((_AttachedDeltaWrapper) values[1]).getWrappedStateObject());
                    }
                }
                else if (values[1] != null)
                {
                    //Full
                    _facesListeners = (_DeltaList<FacesListener>) restoreAttachedState(facesContext,values[1]);
                }
            }
            else
            {
                _facesListeners = (_DeltaList<FacesListener>) restoreAttachedState(facesContext,values[1]);
            }
        }
        
        private Object saveFacesListenersList(FacesContext facesContext)
        {
            PartialStateHolder holder = (PartialStateHolder) _facesListeners;
            if (initialStateMarked() && _facesListeners != null && holder.initialStateMarked())
            {                
                Object attachedState = holder.saveState(facesContext);
                if (attachedState != null)
                {
                    return new _AttachedDeltaWrapper(_facesListeners.getClass(),
                            attachedState);
                }
                //_facesListeners instances once is created never changes, we can return null
                return null;
            }
            else
            {
                return saveAttachedState(facesContext,_facesListeners);
            }            
        }

        @Override
        public Object saveState(FacesContext facesContext)
        {
            Object[] values = new Object[2];
            values[0] = super.saveState(facesContext);
            values[1] = saveFacesListenersList(facesContext);
            
            if (values[0] == null && values[1] == null)
            {
                //No values
                return null;
            }
            return values;
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
    
    public static class TransientStateFacesListener implements FacesListener, StateHolder
    {
        String value;
        
        public TransientStateFacesListener()
        {
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof TransientStateFacesListener)
            {
                if (value == null)
                {
                    if (((TransientStateFacesListener)obj).value == null)
                    {
                        return true;
                    }
                }
                else if (value.equals(((TransientStateFacesListener)obj).value))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean isTransient()
        {
            return true;
        }

        public void setTransient(boolean _transient)
        {
        }

        public void restoreState(FacesContext context, Object state)
        {
        }

        public Object saveState(FacesContext context)
        {
            return null;
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
        a.addTestFacesListener(listener1);
        assertTrue(a._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));        
    }
    
    public void testSimpleSaveRestore1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));        
    }
    
    public void testSimpleSaveRestore2()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        a.markInitialState();
        b.markInitialState();
        FacesListener listener1 = new NoStateFacesListener(); 
        a.addTestFacesListener(listener1);
        Object [] savedState1 = (Object[]) a.saveState(facesContext);       
        b.restoreState(facesContext, savedState1);
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));        
    }
    
    public void testSimpleSaveRestore3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener();
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);        
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));        
    }
    
    public void testSimpleSaveRestore4()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener2();
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addTestFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener2));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        a.removeTestFacesListener(listener2);
        b.removeTestFacesListener(listener2);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(a._facesListeners.contains(listener2));
        assertFalse(b._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener2));
    }
    
    public void testSimpleSaveRestore5()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        UITestComponent c = new UITestComponent();
        FacesListener listener1 = new NoStateFacesListener(); 
        FacesListener listener2 = new NoStateFacesListener2();
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addTestFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener2));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        a.removeTestFacesListener(listener2);
        b.removeTestFacesListener(listener2);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(a._facesListeners.contains(listener2));
        assertFalse(b._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener2));
        
        //Save fully
        b.clearInitialState();
        c.restoreState(facesContext, b.saveState(facesContext));
        //c._facesListeners should be empty
        assertFalse(c._facesListeners.contains(listener1));
        assertFalse(c._facesListeners.contains(listener2));
    }
    
    public void testSimpleSaveRestore6()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener(); 
        StateFacesListener listener2 = new StateFacesListener();
        listener1.setValue("value1");
        listener2.setValue("value2");
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is not null because StateFacesListener is instance of StateHolder 
        // and always needs to be saved and restored!
        assertNotNull(savedState1[1]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));        
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
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addTestFacesListener(listener2);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener2));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        a.removeTestFacesListener(listener2);
        b.removeTestFacesListener(listener2);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(a._facesListeners.contains(listener2));
        assertFalse(b._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener2));
        
        //Save fully
        b.clearInitialState();
        c.restoreState(facesContext, b.saveState(facesContext));
        //c._facesListeners should be empty
        assertFalse(c._facesListeners.contains(listener1));
        assertFalse(c._facesListeners.contains(listener2));
    }
    
    public void testSimpleSaveRestore8()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new PartialStateFacesListener(); 
        listener1.setValue("value1");
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is null because StateFacesListener is instance of PartialStateHolder 
        assertNull(savedState1 == null ? null : savedState1[1]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));        
    }
    
    public void testSimpleSaveRestore9()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new PartialStateFacesListener(); 
        listener1.setValue("value1");
        StateFacesListener listener2 = new PartialStateFacesListener(); 
        listener2.setValue("value2");        
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        a.addTestFacesListener(listener2);
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        // This is null because StateFacesListener is instance of PartialStateHolder but a
        // listener was added after markInitialState
        assertNotNull(savedState1[1]);
        b.restoreState(facesContext, savedState1);        
        assertTrue(a._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener2));
        a.removeTestFacesListener(listener1);
        b.removeTestFacesListener(listener1);
        assertFalse(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener2));
    }    
    
    public void testSimpleSaveRestoreTransient1()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addTestFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
        assertTrue(b._facesListeners.isEmpty());
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
        a.addTestFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient3()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        FacesListener listener2 = new NoStateFacesListener2();
        a.addTestFacesListener(listener2);
        b.addTestFacesListener(listener2);        
        a.markInitialState();
        b.markInitialState();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addTestFacesListener(listener1);
        b.restoreState(facesContext, a.saveState(facesContext));
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient4()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
    }
    
    public void testSimpleSaveRestoreTransient5()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        StateFacesListener listener1 = new StateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        StateFacesListener listener2 = new StateFacesListener();
        listener2.setValue("value");
        a.addTestFacesListener(listener1);
        a.addTestFacesListener(listener2);
        b.addTestFacesListener(listener1);
        b.addTestFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        listener2.setValue("value2");
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener2));
        assertEquals("value2", ((StateFacesListener)b._facesListeners.get(b._facesListeners.indexOf(listener2))).getValue());
    }
    
    public void testSimpleSaveRestoreTransient6()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        TransientStateFacesListener listener1 = new TransientStateFacesListener();
        listener1.setValue("value");
        a.addTestFacesListener(listener1);
        b.addTestFacesListener(listener1);
        a.markInitialState();
        b.markInitialState();
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
    }

    public void testSimpleSaveRestoreTransient7()
    {
        UITestComponent a = new UITestComponent();
        UITestComponent b = new UITestComponent();
        TransientStateFacesListener listener1 = new TransientStateFacesListener();
        listener1.setTransient(true);
        listener1.setValue("value");
        StateFacesListener listener2 = new StateFacesListener();
        listener2.setValue("value");
        a.addTestFacesListener(listener1);
        a.addTestFacesListener(listener2);
        b.addTestFacesListener(listener1);
        b.addTestFacesListener(listener2);
        a.markInitialState();
        b.markInitialState();
        listener2.setValue("value2");
        //Since listener1 is transient
        Object [] savedState1 = (Object[]) a.saveState(facesContext);
        b.restoreState(facesContext, savedState1);  
        assertTrue(a._facesListeners.contains(listener1));
        assertFalse(b._facesListeners.contains(listener1));
        assertTrue(a._facesListeners.contains(listener2));
        assertTrue(b._facesListeners.contains(listener2));
        assertEquals("value2", ((StateFacesListener)b._facesListeners.get(b._facesListeners.indexOf(listener2))).getValue());
    }
}
