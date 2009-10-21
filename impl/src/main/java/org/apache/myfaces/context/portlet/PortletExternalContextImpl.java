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
package org.apache.myfaces.context.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.util.EnumerationIterator;

/**
 * An ExternalContext implementation for JSF applications that run inside a Portlet.
 * 
 * @deprecated Replaced by jsr 301 portlet bridge. 
 * @author Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class PortletExternalContextImpl extends ExternalContext implements ReleaseableExternalContext
{

    private static final String INIT_PARAMETER_MAP_ATTRIBUTE = InitParameterMap.class.getName();

    PortletContext _portletContext;
    PortletRequest _portletRequest;
    PortletResponse _portletResponse;

    private Map<String, Object> _applicationMap;
    private Map<String, Object> _sessionMap;
    private Map<String, Object> _requestMap;
    private Map<String, String> _requestParameterMap;
    private Map<String, String[]> _requestParameterValuesMap;
    private Map<String, String> _requestHeaderMap;
    private Map<String, String[]> _requestHeaderValuesMap;
    private Map<String, String> _initParameterMap;

    private ActionRequest _actionRequest;

    /** Creates a new instance of PortletFacesContextImpl */
    public PortletExternalContextImpl(PortletContext portletContext, PortletRequest portletRequest,
            PortletResponse portletResponse)
    {
        _portletContext = portletContext;
        _portletRequest = portletRequest;
        _portletResponse = portletResponse;
        _actionRequest =  isActionRequest(portletRequest) ? (ActionRequest)portletRequest : null;
    }

    public void dispatch(String path) throws IOException
    {
        if (_actionRequest != null)
        { // dispatch only allowed for RenderRequest
            String msg = "Can not call dispatch() during a portlet ActionRequest";
            throw new UnsupportedOperationException(msg);
        }

        PortletRequestDispatcher requestDispatcher
            = _portletContext.getRequestDispatcher(path); //TODO: figure out why I need named dispatcher
        try
        {
            requestDispatcher.include((RenderRequest) _portletRequest, (RenderResponse) _portletResponse);
        }
        catch (PortletException e)
        {
            if (e.getMessage() != null)
            {
                throw new FacesException(e.getMessage(), e);
            }
            throw new FacesException(e);            
        }
    }

    public String encodeActionURL(String url)
    {
        checkNull(url, "url");
        return _portletResponse.encodeURL(url);
    }

    public String encodeNamespace(String name)
    {
        if (_actionRequest != null)
        {
            // encodeNamespace only allowed for RenderRequest
            throw new UnsupportedOperationException("Can not call encodeNamespace() during a portlet ActionRequest");
        }

        // we render out the name and then the namespace as
        // e.g. for JSF-ids, it is important to keep the _id prefix
        // to know that id creation has happened automatically
        return name + ((RenderResponse) _portletResponse).getNamespace();
    }

    @Override
    public String encodeResourceURL(String url)
    {
        checkNull(url, "url");
        return _portletResponse.encodeURL(url);
    }

    @Override
    public Map<String, Object> getApplicationMap()
    {
        if (_applicationMap == null)
        {
            _applicationMap = new ApplicationMap(_portletContext);
        }
        return _applicationMap;
    }

    @Override
    public String getAuthType()
    {
        return _portletRequest.getAuthType();
    }

    @Override
    public Object getContext()
    {
        return _portletContext;
    }

    @Override
    public String getInitParameter(String name)
    {
        return _portletContext.getInitParameter(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getInitParameterMap()
    {
        if (_initParameterMap == null)
        {
            // We cache it as an attribute in PortletContext itself (is this circular reference a problem?)
            if ((_initParameterMap = (Map<String, String>) _portletContext.getAttribute(INIT_PARAMETER_MAP_ATTRIBUTE)) == null)
            {
                _initParameterMap = new InitParameterMap(_portletContext);
                _portletContext.setAttribute(INIT_PARAMETER_MAP_ATTRIBUTE, _initParameterMap);
            }
        }
        return _initParameterMap;
    }

    @Override
    public String getRemoteUser()
    {
        return _portletRequest.getRemoteUser();
    }

    @Override
    public Object getRequest()
    {
        return _portletRequest;
    }

    @Override
    public String getRequestContentType()
    {
        return null;
    }

    @Override
    public String getRequestContextPath()
    {
        return _portletRequest.getContextPath();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getRequestCookieMap()
    {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map<String, String> getRequestHeaderMap()
    {
        if (_requestHeaderMap == null)
        {
            _requestHeaderMap = new RequestHeaderMap(_portletRequest);
        }
        return _requestHeaderMap;
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap()
    {
        if (_requestHeaderValuesMap == null)
        {
            _requestHeaderValuesMap = new RequestHeaderValuesMap(_portletRequest);
        }
        return _requestHeaderValuesMap;
    }

    @Override
    public Locale getRequestLocale()
    {
        return _portletRequest.getLocale();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Locale> getRequestLocales()
    {
        return new EnumerationIterator(_portletRequest.getLocales());
    }

    @Override
    public Map<String, Object> getRequestMap()
    {
        if (_requestMap == null)
        {
            _requestMap = new RequestMap(_portletRequest);
        }
        return _requestMap;
    }

    @Override
    public Map<String, String> getRequestParameterMap()
    {
        if (_requestParameterMap == null)
        {
            _requestParameterMap = new RequestParameterMap(_portletRequest);
        }
        return _requestParameterMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<String> getRequestParameterNames()
    {
        return new EnumerationIterator(_portletRequest.getParameterNames());
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap()
    {
        if (_requestParameterValuesMap == null)
        {
            _requestParameterValuesMap = new RequestParameterValuesMap(_portletRequest);
        }
        return _requestParameterValuesMap;
    }

    @Override
    public String getRequestPathInfo()
    {
        return null; // must return null
    }

    @Override
    public String getRequestServletPath()
    {
        return null; // must return null
    }

    @Override
    public URL getResource(String path) throws MalformedURLException
    {
        checkNull(path, "path");

        return _portletContext.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        checkNull(path, "path");

        return _portletContext.getResourceAsStream(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getResourcePaths(String path)
    {
        checkNull(path, "path");
        return _portletContext.getResourcePaths(path);
    }

    @Override
    public Object getResponse()
    {
        return _portletResponse;
    }

    @Override
    public String getResponseContentType()
    {
        return null;
    }

    @Override
    public Object getSession(boolean create)
    {
        return _portletRequest.getPortletSession(create);
    }

    @Override
    public Map<String, Object> getSessionMap()
    {
        if (_sessionMap == null)
        {
            _sessionMap = new SessionMap(_portletRequest);
        }
        return _sessionMap;
    }

    @Override
    public Principal getUserPrincipal()
    {
        return _portletRequest.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        checkNull(role, "role");

        return _portletRequest.isUserInRole(role);
    }

    @Override
    public void log(String message)
    {
        checkNull(message, "message");

        _portletContext.log(message);
    }

    @Override
    public void log(String message, Throwable exception)
    {
        checkNull(message, "message");
        checkNull(exception, "exception");

        _portletContext.log(message, exception);
    }

    @Override
    public void redirect(String url) throws IOException
    {
        if (_actionRequest instanceof ActionResponse)
        {
            ((ActionResponse) _portletResponse).sendRedirect(url);
        }
        else
        {
            throw new IllegalArgumentException("Only ActionResponse supported");
        }
    }

    public void release()
    {
        _portletContext = null;
        _portletRequest = null;
        _portletResponse = null;
        _applicationMap = null;
        _sessionMap = null;
        _requestMap = null;
        _requestParameterMap = null;
        _requestParameterValuesMap = null;
        _requestHeaderMap = null;
        _requestHeaderValuesMap = null;
        _initParameterMap = null;
        _actionRequest = null;
    }

    /**
     * @since JSF 1.2
     * @param request
     */
    @Override
    public void setRequest(java.lang.Object request)
    {
        this._portletRequest = (PortletRequest) request;
        this._actionRequest = isActionRequest(_portletRequest) ? (ActionRequest) request : null;
    }

    /**
     * @since JSF 1.2
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public void setRequestCharacterEncoding(java.lang.String encoding)
        throws java.io.UnsupportedEncodingException{
      
        if(_actionRequest != null)
            _actionRequest.setCharacterEncoding(encoding);
        else
            throw new UnsupportedOperationException("Can not set request character encoding to value '" + encoding
                    + "'. Request is not an action request");
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        if(_actionRequest != null)
            return _actionRequest.getCharacterEncoding();
        throw new UnsupportedOperationException("Can not get request character encoding. Request is not an action request");
    }
    
    @Override
    public String getResponseCharacterEncoding()
    {
        return null;
    }
    
    /**
     * @since JSF 1.2
     * @param response
     */
    @Override
    public void setResponse(java.lang.Object response)
    {
        this._portletResponse = (PortletResponse) response;
    }

    /**
     * @since JSF 1.2
     * @param encoding
     */
    @Override
    public void setResponseCharacterEncoding(java.lang.String encoding)
    {
        // nope!
    }

    private void checkNull(Object o, String param)
    {
        if (o == null)
        {
            throw new NullPointerException(param + " can not be null.");
        }
    }
    
    private boolean isActionRequest(PortletRequest portletRequest)
    {
        return portletRequest instanceof ActionRequest;
    }

}