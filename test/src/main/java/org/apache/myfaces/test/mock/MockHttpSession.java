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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * <p>Mock implementation of <code>HttpSession</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */
public class MockHttpSession implements HttpSession
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Configure a default instance.</p>
     */
    public MockHttpSession()
    {
        super();
    }

    /**
     * <p>Configure a session instance associated with the specified
     * servlet context.</p>
     *
     * @param servletContext The associated servlet context
     */
    public MockHttpSession(ServletContext servletContext)
    {

        super();
        setServletContext(servletContext);

    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p>Add a new listener instance that should be notified about
     * attribute changes.</p>
     *
     * @param listener The new listener to be added
     */
    public void addAttributeListener(HttpSessionAttributeListener listener)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            attributeListeners.add(listener);
        }
        else
        {
            container.subscribeListener(listener);
        }
    }

    /**
     * <p>Set the ServletContext associated with this session.</p>
     *
     * @param servletContext The associated servlet context
     */
    public void setServletContext(ServletContext servletContext)
    {

        this.servletContext = servletContext;

    }

    protected MockWebContainer getWebContainer()
    {
        if (this.servletContext instanceof MockServletContext)
        {
            return ((MockServletContext)this.servletContext).getWebContainer();
        }
        return null;
    }

    // ------------------------------------------------------ Instance Variables

    private List attributeListeners = new ArrayList();
    private HashMap attributes = new HashMap();
    private String id = "123";
    private ServletContext servletContext = null;
    private boolean invalid = false;
    private MockWebContainer webContainer;

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>Set the session identifier of this session.</p>
     *
     * @param id The new session identifier
     */
    public void setId(String id)
    {
        this.id = id;
    }

    // ----------------------------------------------------- HttpSession Methods

    @Override
    public Object getAttribute(String name)
    {

        assertValidity();

        return attributes.get(name);

    }

    @Override
    public Enumeration getAttributeNames()
    {

        assertValidity();

        return new MockEnumeration(attributes.keySet().iterator());

    }

    @Override
    public long getCreationTime()
    {

        throw new UnsupportedOperationException();

    }

    @Override
    public String getId()
    {

        return this.id;

    }

    @Override
    public long getLastAccessedTime()
    {

        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxInactiveInterval()
    {

        throw new UnsupportedOperationException();

    }

    @Override
    public ServletContext getServletContext()
    {
        return this.servletContext;
    }

    public Object getValue(String name)
    {
        throw new UnsupportedOperationException();
    }

    public String[] getValueNames()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate()
    {

        assertValidity();

        attributes.clear();
        invalid = true;
        
        MockWebContainer container = getWebContainer();
        if (container != null)
        {
            HttpSessionEvent se = new HttpSessionEvent(this);
            container.sessionDestroyed(se);
        }
    }

    @Override
    public boolean isNew()
    {
        throw new UnsupportedOperationException();
    }

    public void putValue(String name, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name)
    {
        assertValidity();

        if (attributes.containsKey(name))
        {
            Object value = attributes.remove(name);
            fireAttributeRemoved(name, value);
        }
    }

    public void removeValue(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, Object value)
    {

        assertValidity();

        if (name == null)
        {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (value == null)
        {
            removeAttribute(name);
            return;
        }
        if (attributes.containsKey(name))
        {
            Object oldValue = attributes.get(name);
            attributes.put(name, value);
            fireAttributeReplaced(name, oldValue, value);
        }
        else
        {
            attributes.put(name, value);
            fireAttributeAdded(name, value);
        }

    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        throw new UnsupportedOperationException();
    }

    // --------------------------------------------------------- Support Methods

    /**
     * <p>Fire an attribute added event to interested listeners.</p>
     *
     * @param key Attribute whose value was added
     * @param value The new value
     */
    private void fireAttributeAdded(String key, Object value)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            if (attributeListeners.size() < 1)
            {
                return;
            }
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueBound(event);
            }
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                HttpSessionAttributeListener listener = (HttpSessionAttributeListener) listeners
                        .next();
                listener.attributeAdded(event);
            }
        }
        else
        {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueBound(event);
            }            
            container.attributeAdded(event);
        }
    }

    /**
     * <p>Fire an attribute removed event to interested listeners.</p>
     *
     * @param key Attribute whose value was removed
     * @param value The removed value
     */
    private void fireAttributeRemoved(String key, Object value)
    {
        MockWebContainer container = getWebContainer();
        if (container == null)
        {
            if (attributeListeners.size() < 1)
            {
                return;
            }
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueUnbound(event);
            }            
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                HttpSessionAttributeListener listener = (HttpSessionAttributeListener) listeners
                        .next();
                listener.attributeRemoved(event);
            }
        }
        else
        {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueUnbound(event);
            }
            container.attributeRemoved(event);
        }
    }

    /**
     * <p>Fire an attribute replaced event to interested listeners.</p>
     *
     * @param key Attribute whose value was replaced
     * @param value The original value
     */
    private void fireAttributeReplaced(String key, Object oldValue, Object value)
    {
        if (oldValue instanceof HttpSessionBindingListener)
        {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    oldValue);
            ((HttpSessionBindingListener)value).valueUnbound(event);
        }
        MockWebContainer container = getWebContainer();
        if (container == null)
        {        
            if (attributeListeners.size() < 1)
            {
                return;
            }
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueBound(event);
            }            
            Iterator listeners = attributeListeners.iterator();
            while (listeners.hasNext())
            {
                HttpSessionAttributeListener listener = (HttpSessionAttributeListener) listeners
                        .next();
                listener.attributeReplaced(event);
            }
        }
        else
        {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, key,
                    value);
            if (value instanceof HttpSessionBindingListener)
            {
                ((HttpSessionBindingListener)value).valueBound(event);
            }
            container.attributeReplaced(event);
        }
    }

    /**
     * <p>Throws an {@link IllegalStateException} if this session is invalid.</p>
     */
    private void assertValidity()
    {
        if (invalid)
        {
            throw new IllegalStateException("Session is invalid.");
        }
    }

}
