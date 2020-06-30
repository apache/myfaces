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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

/**
 * <p> Mock implementation of <code>PortletSession</code>. </p>
 * 
 * $Id$
 * @since 1.0.0
 */
public class MockPortletSession implements PortletSession
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p> Configure a default instance. </p>
     */
    public MockPortletSession()
    {

        super();

    }

    /**
     * <p> Configure a session instance associated with the specified servlet
     * context. </p>
     *
     * @param portletContext The associated portlet context
     */
    public MockPortletSession(PortletContext portletContext)
    {

        super();
        this.portletContext = portletContext;

    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p> Set the <code>PortletContext</code> associated with this session.
     * </p>
     *
     * @param portletContext The associated portlet context
     */
    public void setPortletContext(PortletContext portletContext)
    {

        this.portletContext = portletContext;

    }

    // ------------------------------------------------------ Instance Variables

    private Map portletAttributes = new HashMap();
    private Map applicationAttributes = new HashMap();
    private String id = "123";
    private PortletContext portletContext = null;

    // ---------------------------------------------------------- Public Methods

    /**
     * <p> Set the session identifier of this session. </p>
     *
     * @param id The new session identifier
     */
    public void setId(String id)
    {

        this.id = id;

    }

    // -------------------------------------------------- PortletSession Methods

    @Override
    public Object getAttribute(String name)
    {
        return getAttribute(name, PORTLET_SCOPE);
    }

    @Override
    public Object getAttribute(String name, int scope)
    {
        if (scope == PORTLET_SCOPE)
        {
            return portletAttributes.get(name);
        }
        else if (scope == APPLICATION_SCOPE)
        {
            return applicationAttributes.get(name);
        }

        throw new IllegalArgumentException("Scope constant " + scope
                + " not recognized");

    }

    @Override
    public Enumeration getAttributeNames()
    {
        return getAttributeNames(PORTLET_SCOPE);
    }

    @Override
    public Enumeration getAttributeNames(int scope)
    {
        if (scope == PORTLET_SCOPE)
        {
            return new MockEnumeration(portletAttributes.keySet().iterator());
        }
        else if (scope == APPLICATION_SCOPE)
        {
            return new MockEnumeration(applicationAttributes.keySet()
                    .iterator());
        }

        throw new IllegalArgumentException("Scope constant " + scope
                + " not recognized");

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
    public PortletContext getPortletContext()
    {
        return portletContext;
    }

    @Override
    public void invalidate()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNew()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name)
    {
        removeAttribute(name, PORTLET_SCOPE);
    }

    @Override
    public void removeAttribute(String name, int scope)
    {
        Map attributes;
        if (scope == PORTLET_SCOPE)
        {
            attributes = portletAttributes;
        }
        else if (scope == APPLICATION_SCOPE)
        {
            attributes = applicationAttributes;
        }
        else
        {
            throw new IllegalArgumentException("Scope constant " + scope
                    + " not recognized");
        }
        if (attributes.containsKey(name))
        {
            attributes.remove(name);
        }

    }

    @Override
    public void setAttribute(String name, Object value)
    {
        setAttribute(name, value, PORTLET_SCOPE);
    }

    @Override
    public void setAttribute(String name, Object value, int scope)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (value == null)
        {
            removeAttribute(name, scope);
            return;
        }

        Map attributes;
        if (scope == PORTLET_SCOPE)
        {
            attributes = portletAttributes;
        }
        else if (scope == APPLICATION_SCOPE)
        {
            attributes = applicationAttributes;
        }
        else
        {
            throw new IllegalArgumentException("Scope constant " + scope
                    + " not recognized");
        }
        attributes.put(name, value);

    }

    @Override
    public void setMaxInactiveInterval(int arg0)
    {
        throw new UnsupportedOperationException();
    }

}
