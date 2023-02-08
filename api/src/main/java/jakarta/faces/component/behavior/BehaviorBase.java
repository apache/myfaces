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
package jakarta.faces.component.behavior;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.BehaviorEvent;
import jakarta.faces.event.BehaviorListener;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * @since 2.0
 */
public class BehaviorBase implements Behavior, PartialStateHolder
{
    private _DeltaList<BehaviorListener> _behaviorListeners;
    
    private boolean _initialState;
    
    private transient boolean _transient;

    public BehaviorBase()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcast(BehaviorEvent event) throws AbortProcessingException
    {
        Assert.notNull(event, "event");
        
        if (_behaviorListeners != null)
        {
            // This code prevent listeners from unregistering themselves while processing the event.
            // I believe it should always be alright in this case. However, the need rise, then it 
            // should be possible to remove that limitation by using a clone for the looping
            for (int i = 0; i < _behaviorListeners.size() ; i++)
            {
                BehaviorListener listener = _behaviorListeners.get(i);
                if (event.isAppropriateListener(listener))
                {
                    event.processListener(listener);
                }
            }
        }
    }

    @Override
    public void clearInitialState()
    {
        _initialState = false;
        if (_behaviorListeners != null)
        {
            _behaviorListeners.clearInitialState();
        }
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialState;
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }

    @Override
    public void markInitialState()
    {
        _initialState = true;
        if (_behaviorListeners != null)
        {
            _behaviorListeners.markInitialState();
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }
        else if (state instanceof _AttachedDeltaWrapper)
        {
            //Delta: check for null is not necessary since _behaviorListener field
            //is only set once and never reset
            ((StateHolder)_behaviorListeners).restoreState(context,
                    ((_AttachedDeltaWrapper) state).getWrappedStateObject());
        }
        else
        {
            //Full
            _behaviorListeners = (_DeltaList<BehaviorListener>) restoreAttachedState(context, state);
        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        return saveBehaviorListenersList(context);
    }
    
    private Object saveBehaviorListenersList(FacesContext facesContext)
    {
        PartialStateHolder holder = _behaviorListeners;
        if (initialStateMarked() && _behaviorListeners != null && holder.initialStateMarked())
        {                
            Object attachedState = holder.saveState(facesContext);
            if (attachedState != null)
            {
                return new _AttachedDeltaWrapper(_behaviorListeners.getClass(), attachedState);
            }
            //_behaviorListeners instances once is created never changes, we can return null
            return null;
        }
        else
        {
            return saveAttachedState(facesContext,_behaviorListeners);
        }
    }

    private static Object saveAttachedState(FacesContext context, Object attachedObject)
    {
        if (context == null)
        {
            throw new NullPointerException ("context");
        }
        
        if (attachedObject == null)
        {
            return null;
        }
        // StateHolder interface should take precedence over
        // List children
        if (attachedObject instanceof StateHolder)
        {
            StateHolder holder = (StateHolder) attachedObject;
            if (holder.isTransient())
            {
                return null;
            }

            return new _AttachedStateWrapper(attachedObject.getClass(), holder.saveState(context));
        }        
        else if (attachedObject instanceof List)
        {
            List<Object> lst = new ArrayList<>(((List<?>) attachedObject).size());
            for (Object item : (List<?>) attachedObject)
            {
                if (item != null)
                {
                    lst.add(saveAttachedState(context, item));
                }
            }

            return new _AttachedListStateWrapper(lst);
        }
        else if (attachedObject instanceof Serializable)
        {
            return attachedObject;
        }
        else
        {
            return new _AttachedStateWrapper(attachedObject.getClass(), null);
        }
    }

    private static Object restoreAttachedState(FacesContext context, Object stateObj) throws IllegalStateException
    {
        Assert.notNull(context, "context");

        if (stateObj == null)
        {
            return null;
        }
        if (stateObj instanceof _AttachedListStateWrapper)
        {
            List<Object> lst = ((_AttachedListStateWrapper) stateObj).getWrappedStateList();
            List<Object> restoredList = new ArrayList<>(lst.size());
            for (Object item : lst)
            {
                restoredList.add(restoreAttachedState(context, item));
            }
            return restoredList;
        }
        else if (stateObj instanceof _AttachedStateWrapper)
        {
            Class<?> clazz = ((_AttachedStateWrapper) stateObj).getClazz();
            Object restoredObject;
            try
            {
                restoredObject = clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException("Could not restore StateHolder of type " + clazz.getName()
                        + " (missing no-args constructor?)", e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            if (restoredObject instanceof StateHolder)
            {
                _AttachedStateWrapper wrapper = (_AttachedStateWrapper) stateObj;
                Object wrappedState = wrapper.getWrappedStateObject();

                StateHolder holder = (StateHolder) restoredObject;
                holder.restoreState(context, wrappedState);
            }
            return restoredObject;
        }
        else
        {
            return stateObj;
        }
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        _transient = newTransientValue;
    }
    
    protected void addBehaviorListener(BehaviorListener listener)
    {
        Assert.notNull(listener, "listener");
        
        if (_behaviorListeners == null)
        {
            // Lazy instanciation with size 1:
            // the only posibility how to add listener is <f:ajax listener="" /> - there is no <f:ajaxListener/> tag 
            _behaviorListeners = new _DeltaList<>(1);
        }
        
        _behaviorListeners.add(listener);
    }
    
    protected void removeBehaviorListener(BehaviorListener listener)
    {
        Assert.notNull(listener, "listener");

        if (_behaviorListeners != null)
        {
            _behaviorListeners.remove(listener);
        }
    }
}
