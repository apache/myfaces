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

package org.apache.myfaces.application;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

/**
 *
 * lu4242
 */
class _ApplicationUtils
{
    
    static SystemEvent _createEvent(FacesContext facesContext, Class<? extends SystemEvent> systemEventClass,
            Object source, SystemEvent event)
    {
        if (event == null)
        {
            try
            {
                Constructor<?>[] constructors = systemEventClass.getConstructors();
                Constructor<? extends SystemEvent> constructor = null;
             
                // try to lookup the new 2 parameter constructor
                for (Constructor<?> c : constructors)
                {
                    if (c.getParameterTypes().length == 2)
                    {
                        // Safe cast, since the constructor belongs
                        // to a class of type SystemEvent
                        constructor = (Constructor<? extends SystemEvent>) c;
                        break;
                    }
                }
                if (constructor != null)
                {
                    event = constructor.newInstance(facesContext, source);
                }
                
                // try to lookup the old 1 parameter constructor
                if (constructor == null)
                {
                    for (Constructor<?> c : constructors)
                    {
                        if (c.getParameterTypes().length == 1)
                        {
                            // Safe cast, since the constructor belongs
                            // to a class of type SystemEvent
                            constructor = (Constructor<? extends SystemEvent>) c;
                            break;
                        }
                    }
                    if (constructor != null)
                    {
                        event = constructor.newInstance(source);
                    }
                }
            }
            catch (Exception e)
            {
                throw new FacesException("Couldn't instanciate system event of type " + 
                        systemEventClass.getName(), e);
            }
        }

        return event;
    }

    static SystemEvent _traverseListenerList(FacesContext facesContext, List<? extends SystemEventListener> listeners,
                                                     Class<? extends SystemEvent> systemEventClass, Object source,
                                                     SystemEvent event)
    {
        if (listeners != null && !listeners.isEmpty())
        {
            // perf: org.apache.myfaces.application.ApplicationImpl.
            //    SystemListenerEntry.getSpecificSourceListenersNotNull(Class<?>)
            // or javax.faces.component.UIComponent.subscribeToEvent(
            //      Class<? extends SystemEvent>, ComponentSystemEventListener)
            // creates a ArrayList:
            for (int i  = 0, size = listeners.size(); i < size; i++)
            {
                SystemEventListener listener = listeners.get(i);
                // Call SystemEventListener.isListenerForSource(java.lang.Object), passing the source argument.
                // If this returns false, take no action on the listener.
                if (listener.isListenerForSource(source))
                {
                    // Otherwise, if the event to be passed to the listener instances has not yet been constructed,
                    // construct the event, passing source as the argument to the one-argument constructor that takes
                    // an Object. This same event instance must be passed to all listener instances.
                    event = _createEvent(facesContext, systemEventClass, source, event);

                    // Call SystemEvent.isAppropriateListener(javax.faces.event.FacesListener), passing the listener
                    // instance as the argument. If this returns false, take no action on the listener.
                    if (event.isAppropriateListener(listener))
                    {
                        // Call SystemEvent.processListener(javax.faces.event.FacesListener), passing the listener
                        // instance.
                        event.processListener(listener);
                    }
                }
            }
        }

        return event;
    }
    
    // Do it with a copy because the list could be changed during a event
    // see MYFACES-2935
    static SystemEvent _traverseListenerListWithCopy(FacesContext facesContext,
            List<? extends SystemEventListener> listeners,
            Class<? extends SystemEvent> systemEventClass, Object source,
            SystemEvent event)
    {
        if (listeners != null && !listeners.isEmpty())
        {
            List<SystemEventListener> listenersCopy = new ArrayList<SystemEventListener>();
            int processedListenerIndex = 0;
            
            for (int i = 0; i < listeners.size(); i++)
            {
                listenersCopy.add(listeners.get(i));
            }
            
            // If the inner for is succesful, processedListenerIndex == listenersCopy.size()
            // and the loop will be complete.
            while (processedListenerIndex < listenersCopy.size())
            {                
                for (; processedListenerIndex < listenersCopy.size(); processedListenerIndex++ )
                {
                    SystemEventListener listener = listenersCopy.get(processedListenerIndex);
                    // Call SystemEventListener.isListenerForSource(java.lang.Object), passing the source argument.
                    // If this returns false, take no action on the listener.
                    if (listener.isListenerForSource(source))
                    {
                        // Otherwise, if the event to be passed to the listener instances has not yet been constructed,
                        // construct the event, passing source as the argument
                        // to the one-argument constructor that takes
                        // an Object. This same event instance must be passed to all listener instances.
                        event = _createEvent(facesContext, systemEventClass, source, event);
    
                        // Call SystemEvent.isAppropriateListener(javax.faces.event.FacesListener), passing the listener
                        // instance as the argument. If this returns false, take no action on the listener.
                        if (event.isAppropriateListener(listener))
                        {
                            // Call SystemEvent.processListener(javax.faces.event.FacesListener), passing the listener
                            // instance.
                            event.processListener(listener);
                        }
                    }
                }
                
                boolean listChanged = false;
                if (listeners.size() == listenersCopy.size())
                {
                    for (int i = 0; i < listenersCopy.size(); i++)
                    {
                        if (listenersCopy.get(i) != listeners.get(i))
                        {
                            listChanged = true;
                            break;
                        }
                    }
                }
                else
                {
                    listChanged = true;
                }
                
                if (listChanged)
                {
                    for (int i = 0; i < listeners.size(); i++)
                    {
                        SystemEventListener listener = listeners.get(i);
                        
                        // check if listenersCopy.get(i) is valid
                        if (i < listenersCopy.size())
                        {
                            // The normal case is a listener was added, 
                            // so as heuristic, check first
                            // if we can find it at the same location
                            if (!listener.equals(listenersCopy.get(i)) &&
                                !listenersCopy.contains(listener))
                            {
                                listenersCopy.add(listener);
                            }
                        }
                        else
                        {
                            if (!listenersCopy.contains(listener))
                            {
                                listenersCopy.add(listener);
                            }
                        }
                    }
                }
            }
        }

        return event;
    }


}
