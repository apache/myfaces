/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.context.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.util.EnumerationIterator;

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
    private HttpServletRequest _httpServletRequest;
    private String _requestServletPath;
    private String _requestPathInfo;
    
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
        _httpServletRequest = isHttpServletRequest(servletRequest) ? (HttpServletRequest)servletRequest : null;
        
        if (_httpServletRequest != null)
        {
            //HACK: MultipartWrapper scrambles the servletPath for some reason in Tomcat 4.1.29 embedded in JBoss 3.2.3!?
            // (this was reported by frederic.auge [frederic.auge@laposte.net])
            HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

            _requestServletPath = httpServletRequest.getServletPath();
            _requestPathInfo = httpServletRequest.getPathInfo();
        }
    }


    private boolean isHttpServletRequest(ServletRequest servletRequest)
    {
        return (servletRequest != null &&
                                 servletRequest instanceof HttpServletRequest);
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
        _httpServletRequest = null;
    }


    public Object getSession(boolean create)
    {
        checkHttpServletRequest();
        return _httpServletRequest.getSession(create);
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

    public String getResponseContentType()
    {
        return _servletResponse.getContentType();
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
            checkHttpServletRequest();
            _sessionMap = new SessionMap(_httpServletRequest);
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
            checkHttpServletRequest();
            _requestHeaderMap = new RequestHeaderMap(_httpServletRequest);
        }
        return _requestHeaderMap;
    }

    public Map getRequestHeaderValuesMap()
    {
        if (_requestHeaderValuesMap == null)
        {
            checkHttpServletRequest();
            _requestHeaderValuesMap = new RequestHeaderValuesMap(_httpServletRequest);
        }
        return _requestHeaderValuesMap;
    }

    public Map<String, Object> getRequestCookieMap()
    {
        if (_requestCookieMap == null)
        {
            checkHttpServletRequest();
            _requestCookieMap = new CookieMap(_httpServletRequest);
        }
        return _requestCookieMap;
    }

    public Locale getRequestLocale()
    {
        return _servletRequest.getLocale();
    }

    public String getRequestPathInfo()
    {
        checkHttpServletRequest();
        //return (_httpServletRequest).getPathInfo();
        //HACK: see constructor
        return _requestPathInfo;
    }

    public String getRequestContentType() {
        return _servletRequest.getContentType();
    }
    
    public String getRequestContextPath()
    {
        checkHttpServletRequest();
        return _httpServletRequest.getContextPath();
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

    @SuppressWarnings("unchecked")
    public Set<String> getResourcePaths(String s)
    {
        return _servletContext.getResourcePaths(s);
    }

    public InputStream getResourceAsStream(String s)
    {
        return _servletContext.getResourceAsStream(s);
    }

    public String encodeActionURL(String s)
    {
        checkHttpServletRequest();
        return ((HttpServletResponse)_servletResponse).encodeURL(s);
    }

    public String encodeResourceURL(String s)
    {
        checkHttpServletRequest();
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

            throw new FacesException(e);
            
        }
    }

    public String getRequestServletPath()
    {
        checkHttpServletRequest();
        //return (_httpServletRequest).getServletPath();
        //HACK: see constructor
        return _requestServletPath;
    }

    public String getAuthType()
    {
        checkHttpServletRequest();
        return _httpServletRequest.getAuthType();
    }

    public String getRemoteUser()
    {
        checkHttpServletRequest();
        return _httpServletRequest.getRemoteUser();
    }

    public boolean isUserInRole(String role)
    {
        checkHttpServletRequest();
        return _httpServletRequest.isUserInRole(role);
    }

    public Principal getUserPrincipal()
    {
        checkHttpServletRequest();
        return _httpServletRequest.getUserPrincipal();
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

    @SuppressWarnings("unchecked")
    public Iterator<Locale> getRequestLocales()
    {
        checkHttpServletRequest();
        return new EnumerationIterator(_httpServletRequest.getLocales());
    }

    public URL getResource(String s) throws MalformedURLException
    {
        return _servletContext.getResource(s);
    }
    
    /**
     * @since JSF 1.2
     * @param request
     */
    public void setRequest(java.lang.Object request)
    {
        this._servletRequest = (ServletRequest) request;
        this._httpServletRequest = isHttpServletRequest(_servletRequest) ? (HttpServletRequest) _servletRequest : null;
    }
    
    /**
     * @since JSF 1.2
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public void setRequestCharacterEncoding(java.lang.String encoding)
        throws java.io.UnsupportedEncodingException{
      
        this._servletRequest.setCharacterEncoding(encoding);
      
    }
    
    /**
     * @since JSF 1.2
     */
    @Override
    public String getRequestCharacterEncoding()
    {
        return _servletRequest.getCharacterEncoding();
    }
    
    /**
     * @since JSF 1.2
     */
    @Override
    public String getResponseCharacterEncoding()
    {
        return _servletResponse.getCharacterEncoding();
    }
    
    /**
     * @since JSF 1.2
     * @param response
     */
    public void setResponse(java.lang.Object response)
    {
        this._servletResponse = (ServletResponse) response;
    }
    
    /**
     * @since JSF 1.2
     * @param encoding
     */
    public void setResponseCharacterEncoding(java.lang.String encoding)
    {
        this._servletResponse.setCharacterEncoding(encoding);
    }

    private void checkHttpServletRequest()
    {
        if (_httpServletRequest == null)
        {
            throw new UnsupportedOperationException("Only HttpServletRequest supported");
        }
    }
}