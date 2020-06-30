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

import javax.faces.component.UIComponent.EventListenerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.render.Renderer;
import org.junit.Assert;

public class UIComponentEventListenerWrapperTest  extends AbstractComponentTest
{
    public void testUIComponentListenerNormalState()
    {
        UIComponent component = new UIOutput();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, component);
        
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testUIComponentListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, component);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be null
        Assert.assertNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, component);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testUIComponentListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, component);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, component);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }

    public static class MyCustomRenderer extends Renderer implements ComponentSystemEventListener {

        public void processEvent(ComponentSystemEvent event)
        {
            
        }
    }
    
    public void testRendererListenerNormalState()
    {
        UIComponent component = new UIOutput();
        MyCustomRenderer renderer = new MyCustomRenderer();
        component.setRendererType("org.apache.myfaces.MyCustomRenderer");
        renderKit.addRenderer("javax.faces.Output", "org.apache.myfaces.MyCustomRenderer", renderer);
        //This case happens when @ListenerFor is attached on the renderer class like h:outputScript or h:outputStylesheet
        EventListenerWrapper wrapper = new EventListenerWrapper(component, renderer);
        
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);

        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testRendererListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        MyCustomRenderer renderer = new MyCustomRenderer();
        component.setRendererType("org.apache.myfaces.MyCustomRenderer");
        renderKit.addRenderer("javax.faces.Output", "org.apache.myfaces.MyCustomRenderer", renderer);
        //This case happens when @ListenerFor is attached on the renderer class like h:outputScript or h:outputStylesheet
        EventListenerWrapper wrapper = new EventListenerWrapper(component, renderer);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should be null
        Assert.assertNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, renderer);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testRendererListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        MyCustomRenderer renderer = new MyCustomRenderer();
        component.setRendererType("org.apache.myfaces.MyCustomRenderer");
        renderKit.addRenderer("javax.faces.Output", "org.apache.myfaces.MyCustomRenderer", renderer);
        //This case happens when @ListenerFor is attached on the renderer class like h:outputScript or h:outputStylesheet
        EventListenerWrapper wrapper = new EventListenerWrapper(component, renderer);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, renderer);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public static class MyNonSerializableListener implements ComponentSystemEventListener {

        public void processEvent(ComponentSystemEvent event)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MyNonSerializableListener;
        }
        
    }
    
    public void testNonSerializableListenerNormalState()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyNonSerializableListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testNonSerializableListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyNonSerializableListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be null
        Assert.assertNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testNonSerializableListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyNonSerializableListener();        
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }

    public static class MySerializableListener implements ComponentSystemEventListener {

        public void processEvent(ComponentSystemEvent event)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MySerializableListener;
        }
        
    }
    
    public void testSerializableListenerNormalState()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MySerializableListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testSerializableListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MySerializableListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be null
        Assert.assertNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testSerializableListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MySerializableListener();        
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public static class MyStateHolderListener implements ComponentSystemEventListener, StateHolder {

        private Integer i = 1;
        
        public void setI(int value)
        {
            i = value;
        }
        
        public void processEvent(ComponentSystemEvent event)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof MyStateHolderListener)
            {
                return (this.i == ((MyStateHolderListener)obj).i); 
            }
            return false;
        }

        public Object saveState(FacesContext context)
        {
            return i;
        }

        public void restoreState(FacesContext context, Object state)
        {
            i = (Integer) state;
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
        }
        
    }
    
    public void testStateHolderListenerNormalState()
    {
        UIComponent component = new UIOutput();
        MyStateHolderListener listener = new MyStateHolderListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        listener.setI(2);
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testStateHolderListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyStateHolderListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be not null, because it implements StateHolder
        Assert.assertNotNull(state);
        
        MyStateHolderListener listener2 = new MyStateHolderListener();
        listener2.setI(2);
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener2);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testStateHolderListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyStateHolderListener();        
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public static class MyPartialStateHolderListener implements ComponentSystemEventListener, PartialStateHolder {

        private Integer i = 1;
        
        private boolean markInitialState;
        
        public MyPartialStateHolderListener()
        {
        }

        public void setI(int value)
        {
            i = value;
        }
        
        public void processEvent(ComponentSystemEvent event)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof MyPartialStateHolderListener)
            {
                return (this.i == ((MyPartialStateHolderListener)obj).i); 
            }
            return false;
        }

        public Object saveState(FacesContext context)
        {
            if (!initialStateMarked())
            {
                return i;
            }
            else
            {
                return i == 1 ? null : i;
            }
        }

        public void restoreState(FacesContext context, Object state)
        {
            if (state == null)
            {
                return;
            }
            i = (Integer) state;
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
        }

        public void clearInitialState()
        {
            markInitialState = false;
        }

        public boolean initialStateMarked()
        {
            return markInitialState;
        }

        public void markInitialState()
        {
            markInitialState = true;
        }
        
    }
    
    public void testPartialStateHolderListenerNormalState()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyPartialStateHolderListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testPartialStateHolderListenerWithPSS()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyPartialStateHolderListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be null
        Assert.assertNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }

    public void testPartialStateHolderListenerWithPSS2()
    {
        UIComponent component = new UIOutput();
        MyPartialStateHolderListener listener = new MyPartialStateHolderListener();
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        
        listener.setI(2);
        Object state = wrapper.saveState(facesContext);
        
        //In this case state should be not null, because something changed inside the listener
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, new MyPartialStateHolderListener());
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
    
    public void testPartialStateHolderListenerWithPSSFull()
    {
        UIComponent component = new UIOutput();
        ComponentSystemEventListener listener = new MyPartialStateHolderListener();        
        //This case happens when @ListenerFor is attached on the component class
        EventListenerWrapper wrapper = new EventListenerWrapper(component, listener);
        
        wrapper.markInitialState();
        
        wrapper.clearInitialState();
        Object state = wrapper.saveState(facesContext);

        //In this case state should not be null, because state should be saved fully
        Assert.assertNotNull(state);
        
        EventListenerWrapper wrapper2 = new EventListenerWrapper(component, listener);
        wrapper.markInitialState();
        //For restore we need to setup the context first
        component.pushComponentToEL(facesContext, component);
        wrapper2.restoreState(facesContext, state);
        component.popComponentFromEL(facesContext);
        
        Assert.assertNotNull(wrapper2.getComponentSystemEventListener());
        Assert.assertEquals(wrapper.getComponentSystemEventListener(), wrapper2.getComponentSystemEventListener());
    }
}
