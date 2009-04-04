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
package javax.faces.component.behavior;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.BehaviorEvent;
import javax.faces.event.BehaviorListener;

/**
 * 
 * TODO: IMPLEMENT HERE - Delta state saving support
 * 
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 15:29:14 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public class BehaviorBase implements Behavior, PartialStateHolder
{
    private List<BehaviorListener> _behaviorListeners;
    
    private boolean _initialState;
    
    private transient boolean _transient;

    /**
     * 
     */
    public BehaviorBase()
    {
    }
    
    //public abstract String getRendererType();
    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcast(BehaviorEvent event)
    {
        if (event == null)
        {
            throw new NullPointerException("event");
        }
        
        if (_behaviorListeners != null)
        {
            // This code prevent listeners from unregistering themselves while processing the event.
            // I believe it should always be alright in this case. However, the need rise, then it 
            // should be possible to remove that limitation by using a clone for the looping
            for (BehaviorListener listener : _behaviorListeners)
            {
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
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        _behaviorListeners = (List<BehaviorListener>) UIComponentBase.restoreAttachedState(context, state);
    }

    @Override
    public Object saveState(FacesContext context)
    {
        return UIComponentBase.saveAttachedState(context, _behaviorListeners);
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        _transient = newTransientValue;
    }
    
    protected void addBehaviorListener(BehaviorListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        
        if (_behaviorListeners == null)
        {
            // Lazy instanciation
            _behaviorListeners = new ArrayList<BehaviorListener>();
        }
        
        _behaviorListeners.add(listener);
    }
    
    protected void removeBehaviorListener(BehaviorListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }

        if (_behaviorListeners != null)
        {
            _behaviorListeners.remove(listener);
        }
    }
}
