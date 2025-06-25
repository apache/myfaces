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

import static jakarta.faces.component.UIComponent.getCurrentComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.RendererWrapper;

class _EventListenerWrapper implements SystemEventListener, PartialStateHolder
{
    static final int LISTENER_SAVE_STATE_HOLDER = 1;
    static final int LISTENER_SAVE_PARTIAL_STATE_HOLDER = 2;
    static final int LISTENER_TYPE_COMPONENT = 4;
    static final int LISTENER_TYPE_RENDERER = 8;
    static final int LISTENER_TYPE_OTHER = 16;
    
    private Class<?> componentClass;
    private ComponentSystemEventListener listener;

    private boolean _initialStateMarked;

    private int listenerCapability;
    private transient UIComponent _component;

    public _EventListenerWrapper()
    {
        //need a no-arg constructor for state saving purposes
        super();
    }

    /**
     * Note we have two cases:
     *
     * 1. listener is an instance of UIComponent. In this case we cannot save and restore
     *    it because we need to point to the real component, but we can assume the instance
     *    is the same because UIComponent.subscribeToEvent says so. Also take into account
     *    this case is the reason why we need a wrapper for UIComponent.subscribeToEvent
     * 2. listener is an instance of Renderer. In this case we can assume the same renderer
     *    used by the source component is the one used by the listener (ListenerFor). 
     * 3. listener is an instance of ComponentSystemEventListener but not from UIComponent.
     *    In this case, the instance could implement StateHolder, PartialStateHolder or do
     *    implement anything, so we have to deal with that case as usual.
     *
     * @param component
     * @param listener
     */
    public _EventListenerWrapper(UIComponent component, ComponentSystemEventListener listener)
    {
        assert component != null;
        assert listener != null;

        this.componentClass = component.getClass();
        this.listener = listener;
        this._component = component;
        initListenerCapability();
    }

    private void initListenerCapability()
    {
        this.listenerCapability = 0;
        if (this.listener instanceof UIComponent)
        {
            this.listenerCapability = LISTENER_TYPE_COMPONENT;
        }
        else if (this.listener instanceof Renderer)
        {
            this.listenerCapability = LISTENER_TYPE_RENDERER;
        }
        else
        {
            if (this.listener instanceof PartialStateHolder)
            {
                this.listenerCapability = LISTENER_TYPE_OTHER | LISTENER_SAVE_PARTIAL_STATE_HOLDER;
            }
            else if (this.listener instanceof StateHolder)
            {
                this.listenerCapability = LISTENER_TYPE_OTHER | LISTENER_SAVE_STATE_HOLDER;
            }
            else
            {
                this.listenerCapability = LISTENER_TYPE_OTHER;
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        else if (o instanceof _EventListenerWrapper other)
        {
            return componentClass.equals(other.componentClass) && listener.equals(other.listener);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return componentClass.hashCode() + listener.hashCode();
    }

    @Override
    public boolean isListenerForSource(Object source)
    {
        // and its implementation of SystemEventListener.isListenerForSource(java.lang.Object) must return true
        // if the instance class of this UIComponent is assignable from the argument to isListenerForSource.

        return source.getClass().isAssignableFrom(componentClass);
    }

    public ComponentSystemEventListener getComponentSystemEventListener()
    {
        return listener;
    }

    @Override
    public void processEvent(SystemEvent event)
    {
        // This inner class must call through to the argument componentListener in its implementation of
        // SystemEventListener.processEvent(jakarta.faces.event.SystemEvent)
        assert event instanceof ComponentSystemEvent;

        listener.processEvent((ComponentSystemEvent) event);
    }

    @Override
    public void clearInitialState()
    {
        if ((listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0)
        {
            ((PartialStateHolder) listener).clearInitialState();
        }
        _initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        if ((listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0)
        {
            return ((PartialStateHolder) listener).initialStateMarked();
        }
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        if ((listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0)
        {
            ((PartialStateHolder) listener).markInitialState();
        }
        _initialStateMarked = true;
    }

    @Override
    public boolean isTransient()
    {
        if ((listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0 ||
                (listenerCapability & LISTENER_SAVE_STATE_HOLDER) != 0)
        {
            return ((StateHolder) listener).isTransient();
        }
        return false;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }
        Object[] values = (Object[]) state;
        componentClass = (Class) values[0];
        if (values[1] instanceof _AttachedDeltaWrapper wrapper)
        {
            ((StateHolder) listener).restoreState(context,
                    wrapper.getWrappedStateObject());
        }
        else
        {
            //Full restore
            listenerCapability = (Integer) values[2];

            _component = UIComponent.getCurrentComponent(context);
            if ((listenerCapability & LISTENER_TYPE_COMPONENT) != 0)
            {
                listener = _component;
            }
            else if ((listenerCapability & LISTENER_TYPE_RENDERER) != 0)
            {
                Renderer renderer = _component.getRenderer(context);
                Integer i = (Integer) values[1];
                if (i != null && i >= 0)
                {
                    while (i > 0)
                    {
                        renderer = ((RendererWrapper) renderer).getWrapped();
                        i--;
                    }
                }
                listener = (ComponentSystemEventListener) renderer;
            }
            else
            {
                listener = (ComponentSystemEventListener)
                        UIComponentBase.restoreAttachedState(context, values[1]);
            }
        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        if (!initialStateMarked())
        {
            Object[] state = new Object[3];
            state[0] = componentClass;
            //If this is not a component or a renderer, save it calling UIComponent.saveAttachedState
            if (!((listenerCapability & LISTENER_TYPE_COMPONENT) != 0 ||
                    (listenerCapability & LISTENER_TYPE_RENDERER) != 0))
            {
                state[1] = UIComponentBase.saveAttachedState(context, listener);
            }
            else
            {
                if ( (listenerCapability & LISTENER_TYPE_RENDERER) != 0)
                {
                    UIComponent componentRef = _component != null ? _component : getCurrentComponent(context);
                    Renderer renderer = componentRef.getRenderer(context);
                    int i = 0;
                    while (renderer != null && !renderer.getClass().equals(listener.getClass()))
                    {
                        if (renderer instanceof RendererWrapper wrapper)
                        {
                            renderer = wrapper.getWrapped();
                            i++;
                        }
                        else
                        {
                            renderer = null;
                            i = -1;
                        }
                    }
                    if (i != -1)
                    {
                        // Store the number so we can get the right wrapper to invoke the method.
                        state[1] = i;
                    }
                    else
                    {
                        state[1] = null;
                    }
                }
                else
                {
                    state[1] = null;
                }
            }
            state[2] = listenerCapability;
            return state;
        }
        else
        {
            // If initialStateMarked() == true means two things:
            // 1. PSS is being used
            if ((listenerCapability & LISTENER_TYPE_COMPONENT) != 0)
            {
                return null;
            }
            else if ((listenerCapability & LISTENER_TYPE_RENDERER) != 0)
            {
                return null;
            }
            else
            {
                if ((listenerCapability & LISTENER_SAVE_STATE_HOLDER) != 0 ||
                        (listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0)
                {
                    Object listenerSaved = ((StateHolder) listener).saveState(context);
                    if (listenerSaved == null)
                    {
                        return null;
                    }
                    return new Object[] { componentClass,
                                        new _AttachedDeltaWrapper(listener.getClass(), listenerSaved) };
                }
                else
                {
                    //This is not necessary, because the instance is considered serializable!
                    return null;
                }
            }
        }
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        if ((listenerCapability & LISTENER_SAVE_PARTIAL_STATE_HOLDER) != 0 ||
                (listenerCapability & LISTENER_SAVE_STATE_HOLDER) != 0)
        {
            ((StateHolder) listener).setTransient(newTransientValue);
        }
    }

    public int getListenerCapability()
    {
        return listenerCapability;
    }
}
