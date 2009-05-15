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

import org.apache.myfaces.util.EnumerationIterator;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.*;
import java.lang.reflect.Method;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.myfaces.context.ReleaseableExternalContext;

/**
 * JSF 1.0 PRD2, 6.1.1
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 *
 * Revision 1.11 Sylvain Vieujot
 * Forward the message when an exception is thrown in dispatch
 */
public class ServletExternalContextImpl
    extends ExternalContext implements ReleaseableExternalContext
{

    private static final Log log = LogFactory.getLog(ServletExternalContextImpl.class);

    private static final String INIT_PARAMETER_MAP_ATTRIBUTE = InitParameterMap.class.getName();

    private ServletContext _servletContext;
    private ServletRequest _servletRequest;
    private ServletResponse _servletResponse;
    private Map _applicationMap;
    private Map _sessionMap;
    private Map _requestMap;
    private Map _requestParameterMap;
    private Map _requestParameterValuesMap;
    private Map _requestHeaderMap;
    private Map _requestHeaderValuesMap;
    private Map _requestCookieMap;
    private Map _initParameterMap;
    private boolean _isHttpServletRequest;
    private String _requestServletPath;
    private String _requestPathInfo;
    private static Method setCharacterEncodingMethod = null;
    
    static {
        try {
            setCharacterEncodingMethod = ServletRequest.class.getMethod("setCharacterEncoding", new Class[]{String.class});
        } catch (Exception e) {
                    log.warn("Detecting request character encoding is disable.");
                    log.warn("Failed to obtain ServletRequest#setCharacterEncoding() method: " + e);
        }
    } 

    public ServletExternalContextImpl(ServletContext servletContext,
                                      ServletRequest servletRequest,
                                      ServletResponse servletResponse)
    {
        _servletContext = servletContext;
        _servletRequest = servletRequest;
        _servletResponse = servletResponse;
        _applicationMap = null;
        _sessionMap = null;
        _requestMap = null;
        _requestParameterMap = null;
        _requestParameterValuesMap = null;
        _requestHeaderMap = null;
        _requestHeaderValuesMap = null;
        _requestCookieMap = null;
        _initParameterMap = null;
        _isHttpServletRequest = (servletRequest != null &&
                                 servletRequest instanceof HttpServletRequest);
        if (_isHttpServletRequest)
        {
            //HACK: MultipartWrapper scrambles the servletPath for some reason in Tomcat 4.1.29 embedded in JBoss 3.2.3!?
            // (this was reported by frederic.auge [frederic.auge@laposte.net])
            HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

            _requestServletPath = httpServletRequest.getServletPath();
            _requestPathInfo = httpServletRequest.getPathInfo();

            // try to set character encoding as described in section 2.5.2.2 of JSF 1.1 spec
            // we have to use reflection as method setCharacterEncoding is not supported Servlet API <= 2.3
            try
            {
                if (setCharacterEncodingMethod != null) {
                    String contentType = httpServletRequest.getHeader("Content-Type");

                    String characterEncoding = lookupCharacterEncoding(contentType);

                    if (characterEncoding == null) {
                        HttpSession session = httpServletRequest.getSession(false);

                        if (session != null) {
                            characterEncoding = (String) session.getAttribute(ViewHandler.CHARACTER_ENCODING_KEY);
                        }
                    }

                    if (characterEncoding != null)
                    {
                        setCharacterEncodingMethod.invoke(servletRequest, new Object[]{characterEncoding});
                    }
                }
            } catch (Exception e)
            {
                if (log.isWarnEnabled())
                    log.warn("Failed to set character encoding " + e);
            }
        }
    }


    private String lookupCharacterEncoding(String contentType)
    {
        String characterEncoding = null;

        if (contentType != null)
        {
            int charsetFind = contentType.indexOf("charset=");
            if (charsetFind != -1)
            {
                if (charsetFind == 0)
                {
                    //charset at beginning of Content-Type, curious
                    characterEncoding = contentType.substring(8);
                }
                else
                {
                    char charBefore = contentType.charAt(charsetFind - 1);
                    if (charBefore == ';' || Character.isWhitespace(charBefore))
                    {
                        //Correct charset after mime type
                        characterEncoding = contentType.substring(charsetFind + 8);
                    }
                }
                if (log.isDebugEnabled()) log.debug("Incoming request has Content-Type header with character encoding " + characterEncoding);
            }
            else
            {
                if (log.isDebugEnabled()) log.debug("Incoming request has Content-Type header without character encoding: " + contentType);
            }
        }
        return characterEncoding;
    }


    public void release()
    {
        _servletContext = null;
        _servletRequest = null;
        _servletResponse = null;
        _applicationMap = null;
        _sessionMap = null;
        _requestMap = null;
        _requestParameterMap = null;
        _requestParameterValuesMap = null;
        _requestHeaderMap = null;
        _requestHeaderValuesMap = null;
        _requestCookieMap = null;
        _initParameterMap = null;
    }


    public Object getSession(boolean create)
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).getSession(create);
    }

    public Object getContext()
    {
        return _servletContext;
    }

    public Object getRequest()
    {
        return _servletRequest;
    }

    public Object getResponse()
    {
        return _servletResponse;
    }

    public void setResponse(Object response) {
        if (response instanceof ServletResponse) {
            this._servletResponse = (ServletResponse) response;
        }
    }

    public Map getApplicationMap()
    {
        if (_applicationMap == null)
        {
            _applicationMap = new ApplicationMap(_servletContext);
        }
        return _applicationMap;
    }

    public Map getSessionMap()
    {
        if (_sessionMap == null)
        {
            if (!_isHttpServletRequest)
            {
                throw new IllegalArgumentException("Only HttpServletRequest supported");
            }
            _sessionMap = new SessionMap((HttpServletRequest) _servletRequest);
        }
        return _sessionMap;
    }

    public Map getRequestMap()
    {
        if (_requestMap == null)
        {
            _requestMap = new RequestMap(_servletRequest);
        }
        return _requestMap;
    }

    public Map getRequestParameterMap()
    {
        if (_requestParameterMap == null)
        {
            _requestParameterMap = new RequestParameterMap(_servletRequest);
        }
        return _requestParameterMap;
    }

    public Map getRequestParameterValuesMap()
    {
        if (_requestParameterValuesMap == null)
        {
            _requestParameterValuesMap = new RequestParameterValuesMap(_servletRequest);
        }
        return _requestParameterValuesMap;
    }

    public Iterator getRequestParameterNames()
    {
        final Enumeration enumer = _servletRequest.getParameterNames();
        Iterator it = new Iterator()
        {
            public boolean hasNext() {
                return enumer.hasMoreElements();
            }

            public Object next() {
                return enumer.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
            }
        };
        return it;
    }

    public Map getRequestHeaderMap()
    {
        if (_requestHeaderMap == null)
        {
            if (!_isHttpServletRequest)
            {
                throw new IllegalArgumentException("Only HttpServletRequest supported");
            }
            _requestHeaderMap = new RequestHeaderMap((HttpServletRequest)_servletRequest);
        }
        return _requestHeaderMap;
    }

    public Map getRequestHeaderValuesMap()
    {
        if (_requestHeaderValuesMap == null)
        {
            if (!_isHttpServletRequest)
            {
                throw new IllegalArgumentException("Only HttpServletRequest supported");
            }
            _requestHeaderValuesMap = new RequestHeaderValuesMap((HttpServletRequest)_servletRequest);
        }
        return _requestHeaderValuesMap;
    }

    public Map getRequestCookieMap()
    {
        if (_requestCookieMap == null)
        {
            if (!_isHttpServletRequest)
            {
                throw new IllegalArgumentException("Only HttpServletRequest supported");
            }
            _requestCookieMap = new CookieMap((HttpServletRequest)_servletRequest);
        }
        return _requestCookieMap;
    }

    public Locale getRequestLocale()
    {
        return _servletRequest.getLocale();
    }

    public String getRequestPathInfo()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        //return ((HttpServletRequest)_servletRequest).getPathInfo();
        //HACK: see constructor
        return _requestPathInfo;
    }

    public String getRequestContextPath()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).getContextPath();
    }

    public String getInitParameter(String s)
    {
        return _servletContext.getInitParameter(s);
    }

    public Map getInitParameterMap()
    {
        if (_initParameterMap == null)
        {
            // We cache it as an attribute in ServletContext itself (is this circular reference a problem?)
            if ((_initParameterMap = (Map) _servletContext.getAttribute(INIT_PARAMETER_MAP_ATTRIBUTE)) == null)
            {
                _initParameterMap = new InitParameterMap(_servletContext);
                _servletContext.setAttribute(INIT_PARAMETER_MAP_ATTRIBUTE, _initParameterMap);
            }
        }
        return _initParameterMap;
    }

    public Set getResourcePaths(String s)
    {
        return _servletContext.getResourcePaths(s);
    }

    public InputStream getResourceAsStream(String s)
    {
        return _servletContext.getResourceAsStream(s);
    }

    public String encodeActionURL(String s)
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletResponse)_servletResponse).encodeURL(s);
    }

    public String encodeResourceURL(String s)
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletResponse)_servletResponse).encodeURL(s);
    }

    public String encodeNamespace(String s)
    {
        return s;
    }

    public void dispatch(String requestURI) throws IOException, FacesException
    {
        RequestDispatcher requestDispatcher
            = _servletRequest.getRequestDispatcher(requestURI);
        
        // If there is no dispatcher, send NOT_FOUND
        if (requestDispatcher == null)
        {
           ((HttpServletResponse)_servletResponse).sendError(
                  HttpServletResponse.SC_NOT_FOUND);

           return;
       } 
        
        try
        {
            requestDispatcher.forward(_servletRequest, _servletResponse);
        }
        catch (ServletException e)
        {
            if (e.getMessage() != null)
            {
                throw new FacesException(e.getMessage(), e);
            }
            else
            {
                throw new FacesException(e);
            }
        }
    }

    public String getRequestServletPath()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        //return ((HttpServletRequest)_servletRequest).getServletPath();
        //HACK: see constructor
        return _requestServletPath;
    }

    public String getAuthType()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).getAuthType();
    }

    public String getRemoteUser()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).getRemoteUser();
    }

    public boolean isUserInRole(String role)
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).isUserInRole(role);
    }

    public Principal getUserPrincipal()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return ((HttpServletRequest)_servletRequest).getUserPrincipal();
    }

    public void log(String message) {
        _servletContext.log(message);
    }

    public void log(String message, Throwable t) {
        _servletContext.log(message, t);
    }

    public void redirect(String url) throws IOException
    {
        if (_servletResponse instanceof HttpServletResponse)
        {
            ((HttpServletResponse)_servletResponse).sendRedirect(url);
            FacesContext.getCurrentInstance().responseComplete();            
        }
        else
        {
            throw new IllegalArgumentException("Only HttpServletResponse supported");
        }
    }

    public Iterator getRequestLocales()
    {
        if (!_isHttpServletRequest)
        {
            throw new IllegalArgumentException("Only HttpServletRequest supported");
        }
        return new EnumerationIterator(((HttpServletRequest)_servletRequest).getLocales());
    }

    public URL getResource(String s) throws MalformedURLException
    {
        return _servletContext.getResource(s);
    }
}
