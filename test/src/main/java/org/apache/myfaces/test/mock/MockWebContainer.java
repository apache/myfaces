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
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
        if (listener instanceof ServletContextListener)
        {
            if (contextListeners == null)
            {
                contextListeners = new ArrayList<ServletContextListener>();
            }
            contextListeners.add((ServletContextListener)listener);
        }
        if (listener instanceof ServletContextAttributeListener)
        {
            if (contextAttributeListeners == null)
            {
                contextAttributeListeners = new ArrayList<ServletContextAttributeListener>();
            }
            contextAttributeListeners.add((ServletContextAttributeListener)listener);
        }
        if (listener instanceof ServletRequestListener)
        {
            if (requestListeners == null)
            {
                requestListeners = new ArrayList<ServletRequestListener>();
            }
            requestListeners.add((ServletRequestListener)listener);
        }
        if (listener instanceof ServletRequestAttributeListener)
        {
            if (requestAttributeListeners == null)
            {
                requestAttributeListeners = new ArrayList<ServletRequestAttributeListener>();
            }
            requestAttributeListeners.add((ServletRequestAttributeListener)listener);
        }
        if (listener instanceof HttpSessionListener)
        {
            if (sessionListeners == null)
            {
                sessionListeners = new ArrayList<HttpSessionListener>();
            }
            sessionListeners.add((HttpSessionListener)listener);
        }
        if (listener instanceof HttpSessionAttributeListener)
        {
            if (sessionAttributeListeners == null)
            {
                sessionAttributeListeners = new ArrayList<HttpSessionAttributeListener>();
            }
            sessionAttributeListeners.add((HttpSessionAttributeListener)listener);
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