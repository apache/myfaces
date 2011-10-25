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
package org.apache.myfaces.webapp;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.myfaces.config.ManagedBeanDestroyer;

/**
 * Listens to
 *   - removing, replacing of attributes in context, session and request
 *   - destroying of context, session and request
 * for the ManagedBeanDestroyer to assure right destruction of managed beans in those scopes.
 * 
 * This listener is not registered in a tld or web.xml, but will be called by StartupServletContextListener.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class ManagedBeanDestroyerListener implements 
        HttpSessionAttributeListener, HttpSessionListener,
        ServletContextListener, ServletContextAttributeListener,
        ServletRequestListener, ServletRequestAttributeListener
{

    /**
     * The instance of the ManagedBeanDestroyerListener created by
     * StartupServletContextListener is stored under this key in the
     * ApplicationMap.
     */
    public static final String APPLICATION_MAP_KEY = "org.apache.myfaces.ManagedBeanDestroyerListener";

    private ManagedBeanDestroyer _destroyer = null;

    /**
     * Sets the ManagedBeanDestroyer instance to use.
     *  
     * @param destroyer
     */
    public void setManagedBeanDestroyer(ManagedBeanDestroyer destroyer)
    {
        _destroyer = destroyer;
    }

    /* Session related methods ***********************************************/
    
    public void attributeAdded(HttpSessionBindingEvent event)
    {
        // noop
    }

    public void attributeRemoved(HttpSessionBindingEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void attributeReplaced(HttpSessionBindingEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void sessionCreated(HttpSessionEvent event)
    {
        // noop
    }

    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent event)
    {
        // MYFACES-3040 @PreDestroy Has Called 2 times
        // attributeRemoved receives the event too, so it does not 
        // have sense to handle it here. Unfortunately, it is not possible to 
        // handle it first and then on attributeRemoved, so the best bet is
        // let the code in just one place.
        /*
        if (_destroyer != null)
        {
            HttpSession session = event.getSession();
            Enumeration<String> attributes = session.getAttributeNames();
            if (!attributes.hasMoreElements())
            {
                // nothing to do
                return;
            }

            while (attributes.hasMoreElements())
            {
                String name = attributes.nextElement();
                Object value = session.getAttribute(name);
                _destroyer.destroy(name, value);
            }
        }*/
    }
    
    /* Context related methods ***********************************************/
    
    public void attributeAdded(ServletContextAttributeEvent event)
    {
        // noop
    }

    public void attributeRemoved(ServletContextAttributeEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void attributeReplaced(ServletContextAttributeEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void contextInitialized(ServletContextEvent event)
    {
        // noop
    }
    
    @SuppressWarnings("unchecked")
    public void contextDestroyed(ServletContextEvent event)
    {
        if (_destroyer != null)
        {
            ServletContext ctx = event.getServletContext();
            Enumeration<String> attributes = ctx.getAttributeNames();
            if (!attributes.hasMoreElements())
            {
                // nothing to do
                return;
            }

            while (attributes.hasMoreElements())
            {
                String name = attributes.nextElement();
                Object value = ctx.getAttribute(name);
                _destroyer.destroy(name, value);
            }
        }
    }
    
    /* Request related methods ***********************************************/
    
    public void attributeAdded(ServletRequestAttributeEvent event)
    {
        // noop
    }

    public void attributeRemoved(ServletRequestAttributeEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void attributeReplaced(ServletRequestAttributeEvent event)
    {
        if (_destroyer != null)
        {
            _destroyer.destroy(event.getName(), event.getValue());
        }
    }

    public void requestInitialized(ServletRequestEvent event)
    {
        // noop
    }
    
    @SuppressWarnings("unchecked")
    public void requestDestroyed(ServletRequestEvent event)
    {
        if (_destroyer != null)
        {
            ServletRequest request = event.getServletRequest();
            Enumeration<String> attributes = request.getAttributeNames();
            if (!attributes.hasMoreElements())
            {
                // nothing to do
                return;
            }
            
            while (attributes.hasMoreElements())
            {
                String name = attributes.nextElement();
                Object value = request.getAttribute(name);
                _destroyer.destroy(name, value);
            }
        }
    }

}
