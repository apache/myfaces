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

package org.apache.myfaces.test.mock;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestAttributeEvent;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * 
 */
public class MockWebContainer 
    implements ServletContextListener, ServletContextAttributeListener,
               ServletRequestListener, ServletRequestAttributeListener,
               HttpSessionListener, HttpSessionAttributeListener
{
    
    // context
    private List<ServletContextListener> contextListeners;
    private List<ServletContextAttributeListener> contextAttributeListeners;
    
    // request
    private List<ServletRequestListener> requestListeners;
    private List<ServletRequestAttributeListener> requestAttributeListeners;
    
    // session
    private List<HttpSessionListener> sessionListeners;
    private List<HttpSessionAttributeListener> sessionAttributeListeners;

    public MockWebContainer()
    {
    }
    
    /**
     * Create an instance of the passes class and subscribe it as as servlet listener
     * 
     * @param listenerClassName
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public void subscribeListener(String listenerClassName) 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Class clazz = this.getClass().getClassLoader().loadClass(listenerClassName);
        Object instance = clazz.newInstance();
        subscribeListener(instance);
    }
    
    /**
     * Subscribe a servlet listener
     * 
     * @param listener 
     */
    public void subscribeListener(Object listener)
    {
        if (listener instanceof ServletContextListener contextListener)
        {
            if (contextListeners == null)
            {
                contextListeners = new ArrayList<ServletContextListener>();
            }
            contextListeners.add(contextListener);
        }
        if (listener instanceof ServletContextAttributeListener attributeListener)
        {
            if (contextAttributeListeners == null)
            {
                contextAttributeListeners = new ArrayList<ServletContextAttributeListener>();
            }
            contextAttributeListeners.add(attributeListener);
        }
        if (listener instanceof ServletRequestListener requestListener)
        {
            if (requestListeners == null)
            {
                requestListeners = new ArrayList<ServletRequestListener>();
            }
            requestListeners.add(requestListener);
        }
        if (listener instanceof ServletRequestAttributeListener attributeListener)
        {
            if (requestAttributeListeners == null)
            {
                requestAttributeListeners = new ArrayList<ServletRequestAttributeListener>();
            }
            requestAttributeListeners.add(attributeListener);
        }
        if (listener instanceof HttpSessionListener sessionListener)
        {
            if (sessionListeners == null)
            {
                sessionListeners = new ArrayList<HttpSessionListener>();
            }
            sessionListeners.add(sessionListener);
        }
        if (listener instanceof HttpSessionAttributeListener attributeListener)
        {
            if (sessionAttributeListeners == null)
            {
                sessionAttributeListeners = new ArrayList<HttpSessionAttributeListener>();
            }
            sessionAttributeListeners.add(attributeListener);
        }
    }
    
    public void contextInitialized(ServletContextEvent sce)
    {
        if (contextListeners != null && !contextListeners.isEmpty())
        {
            for (ServletContextListener listener : contextListeners)
            {
                listener.contextInitialized(sce);
            }
        }
    }
    
    public void contextDestroyed(ServletContextEvent sce)
    {
        if (contextListeners != null && !contextListeners.isEmpty())
        {
            for (ServletContextListener listener : contextListeners)
            {
                listener.contextDestroyed(sce);
            }
        }
    }
    
    public void attributeAdded(ServletContextAttributeEvent scab)
    {
        if (contextAttributeListeners != null && !contextAttributeListeners.isEmpty())
        {
            for (ServletContextAttributeListener listener : contextAttributeListeners)
            {
                listener.attributeAdded(scab);
            }
        }
    }

    public void attributeRemoved(ServletContextAttributeEvent scab)
    {
        if (contextAttributeListeners != null && !contextAttributeListeners.isEmpty())
        {
            for (ServletContextAttributeListener listener : contextAttributeListeners)
            {
                listener.attributeRemoved(scab);
            }
        }
    }

    public void attributeReplaced(ServletContextAttributeEvent scab)
    {
        if (contextAttributeListeners != null && !contextAttributeListeners.isEmpty())
        {
            for (ServletContextAttributeListener listener : contextAttributeListeners)
            {
                listener.attributeReplaced(scab);
            }
        }
    }
    
    public void requestInitialized ( ServletRequestEvent sre )
    {
        if (requestListeners != null && !requestListeners.isEmpty())
        {
            for (ServletRequestListener listener : requestListeners)
            {
                listener.requestInitialized(sre);
            }
        }        
    }
    
    public void requestDestroyed ( ServletRequestEvent sre )
    {
        if (requestListeners != null && !requestListeners.isEmpty())
        {
            for (ServletRequestListener listener : requestListeners)
            {
                listener.requestDestroyed(sre);
            }
        }
    }

    public void sessionCreated ( HttpSessionEvent se )
    {
        if (sessionListeners != null && !sessionListeners.isEmpty())
        {
            for (HttpSessionListener listener : sessionListeners)
            {
                listener.sessionCreated(se);
            }
        }
    }
    
    public void sessionDestroyed ( HttpSessionEvent se )
    {
        if (sessionListeners != null && !sessionListeners.isEmpty())
        {
            for (HttpSessionListener listener : sessionListeners)
            {
                listener.sessionDestroyed(se);
            }
        }        
    }

    public void attributeAdded(ServletRequestAttributeEvent srae)
    {
        if (requestAttributeListeners != null && !requestAttributeListeners.isEmpty())
        {
            for (ServletRequestAttributeListener listener : requestAttributeListeners)
            {
                listener.attributeAdded(srae);
            }
        }
    }

    public void attributeRemoved(ServletRequestAttributeEvent srae)
    {
        if (requestAttributeListeners != null && !requestAttributeListeners.isEmpty())
        {
            for (ServletRequestAttributeListener listener : requestAttributeListeners)
            {
                listener.attributeRemoved(srae);
            }
        }
    }

    public void attributeReplaced(ServletRequestAttributeEvent srae)
    {
        if (requestAttributeListeners != null && !requestAttributeListeners.isEmpty())
        {
            for (ServletRequestAttributeListener listener : requestAttributeListeners)
            {
                listener.attributeReplaced(srae);
            }
        }
    }

    public void attributeAdded(HttpSessionBindingEvent se)
    {
        if (sessionAttributeListeners != null && !sessionAttributeListeners.isEmpty())
        {
            for (HttpSessionAttributeListener listener : sessionAttributeListeners)
            {
                listener.attributeAdded(se);
            }
        }
    }

    public void attributeRemoved(HttpSessionBindingEvent se)
    {
        if (sessionAttributeListeners != null && !sessionAttributeListeners.isEmpty())
        {
            for (HttpSessionAttributeListener listener : sessionAttributeListeners)
            {
                listener.attributeRemoved(se);
            }
        }
    }

    public void attributeReplaced(HttpSessionBindingEvent se)
    {
        if (sessionAttributeListeners != null && !sessionAttributeListeners.isEmpty())
        {
            for (HttpSessionAttributeListener listener : sessionAttributeListeners)
            {
                listener.attributeReplaced(se);
            }
        }
    }
}