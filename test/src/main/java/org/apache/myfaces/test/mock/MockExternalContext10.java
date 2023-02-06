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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>Mock implementation of <code>ExternalContext</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */
public abstract class MockExternalContext10 extends ExternalContext
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a wrapper instance.</p>
     *
     * @param context <code>ServletContext</code> for this application
     * @param request <code>HttpServetRequest</code> for this request
     * @param response <code>HttpServletResponse</code> for this request
     */
    public MockExternalContext10(ServletContext context,
            HttpServletRequest request, HttpServletResponse response)
    {

        this.context = context;
        this.request = request;
        this.response = response;
        this.applicationMap = null;
        this.initParameterMap = null;
        this.requestMap = null;
        this.requestCookieMap = null;
        this.requestHeaderMap = null;
        this.requestParameterMap = null;
        this.requestParameterValuesMap = null;
        this.requestHeaderValuesMap = null;
        this.sessionMap = null;
    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    protected ServletContext context;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    private Map applicationMap;
    private Map initParameterMap;
    private Map requestMap;
    private Map requestCookieMap;
    private Map requestHeaderMap;
    private Map requestParameterMap;
    private Map requestParameterValuesMap;
    private Map requestHeaderValuesMap;
    private Map sessionMap;

    // ------------------------------------------------- setters for the mock object

    /**
     * <p>Add a new cookie for this request.</p>
     *
     * @param cookie The new cookie
     */
    public void addRequestCookieMap(Cookie cookie)
    {
        Map map = getRequestCookieMap();
        if (request instanceof MockHttpServletRequest
                && map instanceof _CookieMap)
        {
            ((MockHttpServletRequest) request).addCookie(cookie);
        }
        else
        {
            map.put(cookie.getName(), cookie);
        }
    }

    /**
     * <p>Set the request cookie map for this request.</p>
     *
     * @param map The new request cookie map
     */
    public void setRequestCookieMap(Map map)
    {
        requestCookieMap = map;
    }

    /**
     * <p>Add the specified request parameter for this request.</p>
     *
     * @param key Parameter name
     * @param value Parameter value
     */
    public void addRequestParameterMap(String key, String value)
    {
        Map map = getRequestParameterMap();
        if (request instanceof MockHttpServletRequest
                && map instanceof _RequestParameterMap)
        {
            ((MockHttpServletRequest) request).addParameter(key, value);
        }
        else
        {
            map.put(key, value);
        }
    }

    /**
     * <p>Set the request parameter map for this request.</p>
     *
     * @param map The new request parameter map
     */
    public void setRequestParameterMap(Map map)
    {
        requestParameterMap = map;
    }

    /**
     * <p>Add the specified request header for this request.</p>
     *
     * @param key Parameter name
     * @param value Parameter value
     */
    public void addRequestHeader(String key, String value)
    {
        Map map = getRequestHeaderMap();
        if (request instanceof MockHttpServletRequest
                && map instanceof _RequestHeaderMap)
        {
            ((MockHttpServletRequest) request).addHeader(key, value);
        }
        else
        {
            map.put(key, value);
        }
    }

    /**
     * <p>Set the request header map for this request.</p>
     *
     * @param map The new request header map
     */
    public void setRequestHeaderMap(Map map)
    {
        requestHeaderMap = map;
    }

    // ------------------------------------------------- ExternalContext Methods

    @Override
    public void dispatch(String requestURI) throws IOException, FacesException
    {
        RequestDispatcher requestDispatcher = request
                .getRequestDispatcher(requestURI);
        // If there is no dispatcher, send NOT_FOUND
        if (requestDispatcher == null)
        {
            ((HttpServletResponse) response)
                    .sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try
        {
            requestDispatcher.forward(request, response);
        }
        catch (ServletException e)
        {
            if (e.getMessage() != null)
            {
                throw new FacesException(e.getMessage(), e);
            }
            throw new FacesException(e);
        }
    }

    @Override
    public String encodeActionURL(String sb)
    {
        return sb;
    }

    @Override
    public String encodeNamespace(String aValue)
    {
        return aValue;
    }

    @Override
    public String encodeResourceURL(String sb)
    {
        return sb;
    }

    @Override
    public Map getApplicationMap()
    {
        if (applicationMap == null)
        {
            applicationMap = new _ApplicationMap(context);
        }
        return applicationMap;
    }

    @Override
    public String getAuthType()
    {
        return request.getAuthType();
    }

    @Override
    public Object getContext()
    {
        return context;
    }

    @Override
    public String getInitParameter(String name)
    {
        return context.getInitParameter(name);
    }

    @Override
    public Map getInitParameterMap()
    {
        if (initParameterMap == null)
        {
            initParameterMap = new _InitParameterMap(context);
        }
        return initParameterMap;
    }

    @Override
    public String getRemoteUser()
    {
        return request.getRemoteUser();
    }

    @Override
    public Object getRequest()
    {
        return request;
    }

    @Override
    public String getRequestContextPath()
    {
        return request.getContextPath();
    }

    @Override
    public Map getRequestCookieMap()
    {
        if (requestCookieMap == null)
        {
            requestCookieMap = new _CookieMap(request);
        }
        return requestCookieMap;
    }

    @Override
    public Map getRequestHeaderMap()
    {
        if (requestHeaderMap == null)
        {
            requestHeaderMap = new _RequestHeaderMap(request);
        }
        return requestHeaderMap;
    }

    @Override
    public Map getRequestHeaderValuesMap()
    {
        if (requestHeaderValuesMap == null)
        {
            requestHeaderValuesMap = new _RequestHeaderValuesMap(request);
        }
        return requestHeaderValuesMap;
    }

    @Override
    public Locale getRequestLocale()
    {
        return request.getLocale();
    }

    @Override
    public Iterator getRequestLocales()
    {
        return new LocalesIterator(request.getLocales());
    }

    @Override
    public Map getRequestMap()
    {
        if (requestMap == null)
        {
            requestMap = new _RequestMap(request);
        }
        return requestMap;
    }

    @Override
    public Map getRequestParameterMap()
    {
        if (requestParameterMap == null)
        {
            requestParameterMap = new _RequestParameterMap(request);
        }
        return requestParameterMap;
    }

    @Override
    public Iterator getRequestParameterNames()
    {
        final Enumeration enumer = request.getParameterNames();
        Iterator it = new Iterator()
        {
            @Override
            public boolean hasNext()
            {
                return enumer.hasMoreElements();
            }

            @Override
            public Object next()
            {
                return enumer.nextElement();
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException(this.getClass().getName()
                        + " UnsupportedOperationException");
            }
        };
        return it;
    }

    @Override
    public Map getRequestParameterValuesMap()
    {
        if (requestParameterValuesMap == null)
        {
            requestParameterValuesMap = new _RequestParameterValuesMap(request);
        }
        return requestParameterValuesMap;
    }

    @Override
    public String getRequestPathInfo()
    {
        return request.getPathInfo();
    }

    @Override
    public String getRequestServletPath()
    {
        return request.getServletPath();
    }

    @Override
    public URL getResource(String path) throws MalformedURLException
    {
        return context.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        return context.getResourceAsStream(path);
    }

    @Override
    public Set getResourcePaths(String path)
    {
        return context.getResourcePaths(path);
    }

    @Override
    public Object getResponse()
    {
        return response;
    }

    @Override
    public Object getSession(boolean create)
    {
        return request.getSession(create);
    }

    @Override
    public Map getSessionMap()
    {
        if (sessionMap == null)
        {
            sessionMap = new _SessionMap(request);
        }
        return sessionMap;
    }

    @Override
    public java.security.Principal getUserPrincipal()
    {
        return request.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return request.isUserInRole(role);
    }

    @Override
    public void log(String message)
    {
        context.log(message);
    }

    @Override
    public void log(String message, Throwable throwable)
    {
        context.log(message, throwable);
    }

    @Override
    public void redirect(String requestURI) throws IOException
    {
        response.sendRedirect(requestURI);
        FacesContext.getCurrentInstance().responseComplete();
    }
 
    /**
     * <p>Iterator implementation that wraps an enumeration
     * of Locales for the current request.</p>
     */
    private class LocalesIterator implements Iterator
    {

        /**
         * <p>Construct an iterator wrapping the specified
         * enumeration.</p>
         *
         * @param locales Locales enumeration to wrap
         */
        public LocalesIterator(Enumeration locales)
        {
            this.locales = locales;
        }

        /**
         * <p>The enumeration to be wrapped.</p>
         */
        private Enumeration locales;

        @Override
        public boolean hasNext()
        {
            return locales.hasMoreElements();
        }

        @Override
        public Object next()
        {
            return locales.nextElement();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

}
