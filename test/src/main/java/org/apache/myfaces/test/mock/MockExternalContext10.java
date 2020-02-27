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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public String encodeActionURL(String sb)
    {
        return sb;
    }

    /** {@inheritDoc} */
    public String encodeNamespace(String aValue)
    {
        return aValue;
    }

    /** {@inheritDoc} */
    public String encodeResourceURL(String sb)
    {
        return sb;
    }

    /** {@inheritDoc} */
    public Map getApplicationMap()
    {
        if (applicationMap == null)
        {
            applicationMap = new _ApplicationMap(context);
        }
        return applicationMap;
    }

    /** {@inheritDoc} */
    public String getAuthType()
    {
        return request.getAuthType();
    }

    /** {@inheritDoc} */
    public Object getContext()
    {
        return context;
    }

    /** {@inheritDoc} */
    public String getInitParameter(String name)
    {
        return context.getInitParameter(name);
    }

    /** {@inheritDoc} */
    public Map getInitParameterMap()
    {
        if (initParameterMap == null)
        {
            initParameterMap = new _InitParameterMap(context);
        }
        return initParameterMap;
    }

    /** {@inheritDoc} */
    public String getRemoteUser()
    {
        return request.getRemoteUser();
    }

    /** {@inheritDoc} */
    public Object getRequest()
    {
        return request;
    }

    /** {@inheritDoc} */
    public String getRequestContextPath()
    {
        return request.getContextPath();
    }

    /** {@inheritDoc} */
    public Map getRequestCookieMap()
    {
        if (requestCookieMap == null)
        {
            requestCookieMap = new _CookieMap(request);
        }
        return requestCookieMap;
    }

    /** {@inheritDoc} */
    public Map getRequestHeaderMap()
    {
        if (requestHeaderMap == null)
        {
            requestHeaderMap = new _RequestHeaderMap(request);
        }
        return requestHeaderMap;
    }

    /** {@inheritDoc} */
    public Map getRequestHeaderValuesMap()
    {
        if (requestHeaderValuesMap == null)
        {
            requestHeaderValuesMap = new _RequestHeaderValuesMap(request);
        }
        return requestHeaderValuesMap;
    }

    /** {@inheritDoc} */
    public Locale getRequestLocale()
    {
        return request.getLocale();
    }

    /** {@inheritDoc} */
    public Iterator getRequestLocales()
    {
        return new LocalesIterator(request.getLocales());
    }

    /** {@inheritDoc} */
    public Map getRequestMap()
    {
        if (requestMap == null)
        {
            requestMap = new _RequestMap(request);
        }
        return requestMap;
    }

    /** {@inheritDoc} */
    public Map getRequestParameterMap()
    {
        if (requestParameterMap == null)
        {
            requestParameterMap = new _RequestParameterMap(request);
        }
        return requestParameterMap;
    }

    /** {@inheritDoc} */
    public Iterator getRequestParameterNames()
    {
        final Enumeration enumer = request.getParameterNames();
        Iterator it = new Iterator()
        {
            public boolean hasNext()
            {
                return enumer.hasMoreElements();
            }

            public Object next()
            {
                return enumer.nextElement();
            }

            public void remove()
            {
                throw new UnsupportedOperationException(this.getClass()
                        .getName()
                        + " UnsupportedOperationException");
            }
        };
        return it;
    }

    /** {@inheritDoc} */
    public Map getRequestParameterValuesMap()
    {
        if (requestParameterValuesMap == null)
        {
            requestParameterValuesMap = new _RequestParameterValuesMap(request);
        }
        return requestParameterValuesMap;
    }

    /** {@inheritDoc} */
    public String getRequestPathInfo()
    {
        return request.getPathInfo();
    }

    /** {@inheritDoc} */
    public String getRequestServletPath()
    {
        return request.getServletPath();
    }

    /** {@inheritDoc} */
    public URL getResource(String path) throws MalformedURLException
    {
        return context.getResource(path);
    }

    /** {@inheritDoc} */
    public InputStream getResourceAsStream(String path)
    {
        return context.getResourceAsStream(path);
    }

    /** {@inheritDoc} */
    public Set getResourcePaths(String path)
    {
        return context.getResourcePaths(path);
    }

    /** {@inheritDoc} */
    public Object getResponse()
    {
        return response;
    }

    /** {@inheritDoc} */
    public Object getSession(boolean create)
    {
        return request.getSession(create);
    }

    /** {@inheritDoc} */
    public Map getSessionMap()
    {
        if (sessionMap == null)
        {
            sessionMap = new _SessionMap(request);
        }
        return sessionMap;
    }

    /** {@inheritDoc} */
    public java.security.Principal getUserPrincipal()
    {
        return request.getUserPrincipal();
    }

    /** {@inheritDoc} */
    public boolean isUserInRole(String role)
    {
        return request.isUserInRole(role);
    }

    /** {@inheritDoc} */
    public void log(String message)
    {
        context.log(message);
    }

    /** {@inheritDoc} */
    public void log(String message, Throwable throwable)
    {
        context.log(message, throwable);
    }

    /** {@inheritDoc} */
    public void redirect(String requestURI) throws IOException
    {
        response.sendRedirect(requestURI);
        FacesContext.getCurrentInstance().responseComplete();
    }
    
    public String encodeWebsocketURL(String baseUrl)
    {
        Integer port = 8080;
        port = (port == 0) ? null : port;
        if (port != null && 
            !port.equals(request.getServerPort()))
        {
            String scheme = "http";
            String serverName = request.getServerName();
            String url;
            try
            {
                url = new URL(scheme, serverName, port, baseUrl).toExternalForm();
                url = url.replaceFirst("http", "ws");
                return url;
            }
            catch (MalformedURLException ex)
            {
                //If cannot build the url, return the base one unchanged
                return baseUrl;
            }
        }
        else
        {
            return baseUrl;
        }
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

        /** {@inheritDoc} */
        public boolean hasNext()
        {
            return locales.hasMoreElements();
        }

        /** {@inheritDoc} */
        public Object next()
        {
            return locales.nextElement();
        }

        /** {@inheritDoc} */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

}
