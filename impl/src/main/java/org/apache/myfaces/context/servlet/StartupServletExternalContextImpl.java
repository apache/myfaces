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
package org.apache.myfaces.context.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * An ExternalContext implementation for Servlet environments, which is used
 * by StartupFacesContextImpl at container startup and shutdown and which
 * provides ExternalContext functionality that does not require request and
 * response objects.
 * 
 * @author Jakob Korherr (latest modification by $Author: lu4242 $)
 * @version $Revision: 957581 $ $Date: 2010-06-24 10:22:24 -0500 (Jue, 24 Jun 2010) $
 */
public class StartupServletExternalContextImpl extends ServletExternalContextImplBase
{
    public static final String EXCEPTION_TEXT = "This method is not supported during ";
    
    private boolean _startup;
    
    public StartupServletExternalContextImpl(final ServletContext servletContext,
            boolean startup)
    {
        super(servletContext);
        _startup = startup;
    }
    
    // ~ Methods which are valid to be called during startup and shutdown------
    
    // Note that all methods, which are valid to be called during startup and
    // shutdown are implemented in ServletExternalContextImplBase, because they 
    // are exactly the same as in the real ExternalContext implementation.
    
    // ~ Methods which are not valid to be called during startup and shutdown, but we implement anyway ------
    
    // ~ Methods which are unsupported during startup and shutdown-------------

    @Override
    public String encodeActionURL(String url)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeNamespace(String name)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeResourceURL(String url)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getAuthType()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRemoteUser()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getRequest()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestContextPath()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getRequestCookieMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String> getRequestHeaderMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Locale getRequestLocale()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<Locale> getRequestLocales()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getRequestMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String> getRequestParameterMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<String> getRequestParameterNames()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestPathInfo()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestServletPath()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getResponse()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getSession(boolean create)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getSessionMap()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Principal getUserPrincipal()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isUserInRole(String role)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestContentType()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getResponseContentType()
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setRequest(Object request)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setRequestCharacterEncoding(String encoding)
            throws UnsupportedEncodingException
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponse(Object response)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseCharacterEncoding(String encoding)
    {
        throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    /**
     * Cannot dispatch because this is not a page request
     */
    @Override
    public void dispatch(String path) throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }
    
    /**
     * Cannot redirect because this is not a page request
     */
    @Override
    public void redirect(String url) throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }
    
    // ~ private Methods ------------------------------------------------------
    
    /**
     * Returns startup or shutdown as String according to the field _startup.
     * @return
     */
    private String _getTime()
    {
        return _startup ? "startup" : "shutdown";
    }

}
