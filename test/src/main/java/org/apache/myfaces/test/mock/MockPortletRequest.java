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

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

/**
 * <p> Mock implementation of <code>PortletRequest</code>. </p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockPortletRequest implements PortletRequest
{

    // ------------------------------------------------------------ Constructors

    public MockPortletRequest()
    {

        super();

    }

    public MockPortletRequest(PortletSession session)
    {

        super();
        this.session = session;

    }

    // ----------------------------------------------------- Mock Object Methods

    /**
     * <p> Add a request parameter for this request. </p>
     *
     * @param name Parameter name
     * @param value Parameter value
     */
    public void addParameter(String name, String value)
    {

        String[] values = (String[]) parameters.get(name);
        if (values == null)
        {
            String[] results = new String[] { value };
            parameters.put(name, results);
            return;
        }
        String[] results = new String[values.length + 1];
        System.arraycopy(values, 0, results, 0, values.length);
        results[values.length] = value;
        parameters.put(name, results);

    }

    /**
     * <p> Set the <code>PortletSession</code> associated with this request.
     * </p>
     *
     * @param session The new session
     */
    public void setPortletSession(PortletSession session)
    {

        this.session = session;
    }

    /**
     * <p> Set the <code>Locale</code> associated with this request. </p>
     *
     * @param locale The new locale
     */
    public void setLocale(Locale locale)
    {

        this.locale = locale;

    }

    /**
     * <p> Set the <code>Principal</code> associated with this request. </p>
     *
     * @param principal The new Principal
     */
    public void setUserPrincipal(Principal principal)
    {

        this.principal = principal;

    }

    // ------------------------------------------------------ Instance Variables

    private Map attributes = new HashMap();
    private String contextPath = null;
    private Locale locale = null;
    private Map parameters = new HashMap();
    private Principal principal = null;
    private PortletSession session = null;

    // -------------------------------------------------- PortletRequest Methods

    /** {@inheritDoc} */
    public String getAuthType()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getContextPath()
    {

        return contextPath;

    }

    /** {@inheritDoc} */
    public Object getAttribute(String name)
    {

        return attributes.get(name);

    }

    /** {@inheritDoc} */
    public Enumeration getAttributeNames()
    {

        return new MockEnumeration(attributes.keySet().iterator());

    }

    /** {@inheritDoc} */
    public Locale getLocale()
    {

        return locale;
    }

    /** {@inheritDoc} */
    public Enumeration getLocales()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getParameter(String name)
    {

        String[] values = (String[]) parameters.get(name);
        if (values != null)
        {
            return values[0];
        }
        else
        {
            return null;
        }

    }

    /** {@inheritDoc} */
    public Map getParameterMap()
    {

        return parameters;

    }

    /** {@inheritDoc} */
    public Enumeration getParameterNames()
    {

        return new MockEnumeration(parameters.keySet().iterator());

    }

    /** {@inheritDoc} */
    public String[] getParameterValues(String name)
    {

        return (String[]) parameters.get(name);

    }

    /** {@inheritDoc} */
    public PortalContext getPortalContext()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public PortletMode getPortletMode()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public PortletSession getPortletSession()
    {

        return getPortletSession(true);

    }

    /** {@inheritDoc} */
    public PortletSession getPortletSession(boolean create)
    {

        if (create && (session == null))
        {
            throw new UnsupportedOperationException();
        }
        return session;

    }

    /** {@inheritDoc} */
    public PortletPreferences getPreferences()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public Enumeration getProperties(String arg0)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getProperty(String arg0)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public Enumeration getPropertyNames()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getRemoteUser()
    {

        if (principal != null)
        {
            return principal.getName();
        }
        else
        {
            return null;
        }

    }

    /** {@inheritDoc} */
    public String getRequestedSessionId()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getResponseContentType()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public Enumeration getResponseContentTypes()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public String getScheme()
    {

        return ("http");

    }

    /** {@inheritDoc} */
    public String getServerName()
    {

        return ("localhost");

    }

    /** {@inheritDoc} */
    public int getServerPort()
    {

        return (8080);

    }

    /** {@inheritDoc} */
    public Principal getUserPrincipal()
    {

        return principal;

    }

    /** {@inheritDoc} */
    public WindowState getWindowState()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public boolean isPortletModeAllowed(PortletMode arg0)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public boolean isRequestedSessionIdValid()
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public boolean isSecure()
    {

        return false;

    }

    /** {@inheritDoc} */
    public boolean isUserInRole(String arg0)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public boolean isWindowStateAllowed(WindowState arg0)
    {

        throw new UnsupportedOperationException();

    }

    /** {@inheritDoc} */
    public void removeAttribute(String name)
    {

        if (attributes.containsKey(name))
        {
            attributes.remove(name);
        }

    }

    /** {@inheritDoc} */
    public void setAttribute(String name, Object value)
    {

        if (name == null)
        {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }
        if (value == null)
        {
            removeAttribute(name);
            return;
        }
        attributes.put(name, value);

    }

}
