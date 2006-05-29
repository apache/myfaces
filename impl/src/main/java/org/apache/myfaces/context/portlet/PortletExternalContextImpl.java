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
package org.apache.myfaces.context.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.util.EnumerationIterator;

/**
 * An ExternalContext implementation for JSF applications that run inside a
 * a Portlet.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class PortletExternalContextImpl extends ExternalContext implements ReleaseableExternalContext {

    private static final Log log = LogFactory.getLog(PortletExternalContextImpl.class);

    private static final String INIT_PARAMETER_MAP_ATTRIBUTE = InitParameterMap.class.getName();
    private static final Map EMPTY_UNMODIFIABLE_MAP = Collections.unmodifiableMap(new HashMap(0));

    PortletContext _portletContext;
    PortletRequest _portletRequest;
    PortletResponse _portletResponse;

    private Map _applicationMap;
    private Map _sessionMap;
    private Map _requestMap;
    private Map _requestParameterMap;
    private Map _requestParameterValuesMap;
    private Map _requestHeaderMap;
    private Map _requestHeaderValuesMap;
    private Map _initParameterMap;
    private boolean _isActionRequest;

    /** Creates a new instance of PortletFacesContextImpl */
    public PortletExternalContextImpl(PortletContext portletContext,
                                      PortletRequest portletRequest,
                                      PortletResponse portletResponse)
    {
        _portletContext = portletContext;
        _portletRequest = portletRequest;
        _portletResponse = portletResponse;
        _isActionRequest = (portletRequest != null &&
                                 portletRequest instanceof ActionRequest);

        if (_isActionRequest)
        {
            ActionRequest actionRequest = (ActionRequest)portletRequest;

            // try to set character encoding as described in section 2.5.2.2 of JSF 1.1 spec
            try
            {
                String contentType = portletRequest.getProperty("Content-Type");

                String characterEncoding = lookupCharacterEncoding(contentType);

                if (characterEncoding == null) {
                    PortletSession session = portletRequest.getPortletSession(false);

                    if (session != null) {
                        characterEncoding = (String) session.getAttribute(ViewHandler.CHARACTER_ENCODING_KEY,
                                                                          PortletSession.PORTLET_SCOPE);
                    }

                    if (characterEncoding != null) {
                        actionRequest.setCharacterEncoding(characterEncoding);
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

    public void dispatch(String path) throws IOException
    {
        if (_isActionRequest)
        { // dispatch only allowed for RenderRequest
            String msg = "Can not call dispatch() during a portlet ActionRequest";
            throw new IllegalStateException(msg);
        }

        PortletRequestDispatcher requestDispatcher
            = _portletContext.getRequestDispatcher(path); //TODO: figure out why I need named dispatcher
        try
        {
            requestDispatcher.include((RenderRequest)_portletRequest,
                                      (RenderResponse)_portletResponse);
        }
        catch (PortletException e)
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

    public String encodeActionURL(String url) {
        checkNull(url, "url");
        return _portletResponse.encodeURL(url);
    }

    public String encodeNamespace(String name) {
        if (_isActionRequest)
        { // encodeNamespace only allowed for RenderRequest
            String msg = "Can not call encodeNamespace() during a portlet ActionRequest";
            throw new IllegalStateException(msg);
        }

        //we render out the name and then the namespace as
        // e.g. for JSF-ids, it is important to keep the _id prefix
        //to know that id creation has happened automatically
        return name+((RenderResponse)_portletResponse).getNamespace();
    }

    public String encodeResourceURL(String url) {
        checkNull(url, "url");
        return _portletResponse.encodeURL(url);
    }

    public Map getApplicationMap() {
        if (_applicationMap == null)
        {
            _applicationMap = new ApplicationMap(_portletContext);
        }
        return _applicationMap;
    }

    public String getAuthType() {
        return _portletRequest.getAuthType();
    }

    public Object getContext() {
        return _portletContext;
    }

    public String getInitParameter(String name) {
        return _portletContext.getInitParameter(name);
    }

    public Map getInitParameterMap() {
        if (_initParameterMap == null)
        {
            // We cache it as an attribute in PortletContext itself (is this circular reference a problem?)
            if ((_initParameterMap = (Map) _portletContext.getAttribute(INIT_PARAMETER_MAP_ATTRIBUTE)) == null)
            {
                _initParameterMap = new InitParameterMap(_portletContext);
                _portletContext.setAttribute(INIT_PARAMETER_MAP_ATTRIBUTE, _initParameterMap);
            }
        }
        return _initParameterMap;
    }

    public String getRemoteUser() {
        return _portletRequest.getRemoteUser();
    }

    public Object getRequest() {
        return _portletRequest;
    }

    public String getRequestContentType() {
        return null;
    }
    
    public String getRequestContextPath() {
        return _portletRequest.getContextPath();
    }

    public Map getRequestCookieMap() {
        return EMPTY_UNMODIFIABLE_MAP;
    }

    public Map getRequestHeaderMap() {
        if (_requestHeaderMap == null)
        {
            _requestHeaderMap = new RequestHeaderMap(_portletRequest);
        }
        return _requestHeaderMap;
    }

    public Map getRequestHeaderValuesMap() {
        if (_requestHeaderValuesMap == null)
        {
            _requestHeaderValuesMap = new RequestHeaderValuesMap(_portletRequest);
        }
        return _requestHeaderValuesMap;
    }

    public Locale getRequestLocale() {
        return _portletRequest.getLocale();
    }

    public Iterator getRequestLocales() {
        return new EnumerationIterator(_portletRequest.getLocales());
    }

    public Map getRequestMap() {
        if (_requestMap == null)
        {
            _requestMap = new RequestMap(_portletRequest);
        }
        return _requestMap;
    }

    public Map getRequestParameterMap() {
        if (_requestParameterMap == null)
        {
            _requestParameterMap = new RequestParameterMap(_portletRequest);
        }
        return _requestParameterMap;
    }

    public Iterator getRequestParameterNames() {
        // TODO: find out why it is not done this way in ServletExternalContextImpl
        return new EnumerationIterator(_portletRequest.getParameterNames());
    }

    public Map getRequestParameterValuesMap() {
        if (_requestParameterValuesMap == null)
        {
            _requestParameterValuesMap = new RequestParameterValuesMap(_portletRequest);
        }
        return _requestParameterValuesMap;
    }

    public String getRequestPathInfo() {
        return null; // must return null
    }

    public String getRequestServletPath() {
        return null; // must return null
    }

    public URL getResource(String path) throws MalformedURLException {
        checkNull(path, "path");

        return _portletContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        checkNull(path, "path");

        return _portletContext.getResourceAsStream(path);
    }

    public Set getResourcePaths(String path) {
        checkNull(path, "path");
        return _portletContext.getResourcePaths(path);
    }

    public Object getResponse() {
        return _portletResponse;
    }

    public String getResponseContentType() {
        return null;
    }
    
    public Object getSession(boolean create) {
        return _portletRequest.getPortletSession(create);
    }

    public Map getSessionMap() {
        if (_sessionMap == null)
        {
            _sessionMap = new SessionMap(_portletRequest);
        }
        return _sessionMap;
    }

    public Principal getUserPrincipal() {
        return _portletRequest.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        checkNull(role, "role");

        return _portletRequest.isUserInRole(role);
    }

    public void log(String message) {
        checkNull(message, "message");

        _portletContext.log(message);
    }

    public void log(String message, Throwable exception) {
        checkNull(message, "message");
        checkNull(exception, "exception");

        _portletContext.log(message, exception);
    }

    public void redirect(String url) throws IOException {
        if (_portletResponse instanceof ActionResponse)
        {
            ((ActionResponse)_portletResponse).sendRedirect(url);
        }
        else
        {
            throw new IllegalArgumentException("Only ActionResponse supported");
        }
    }

    public void release() {
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
    }

    private void checkNull(Object o, String param)
    {
        if (o == null)
        {
            throw new NullPointerException(param + " can not be null.");
        }
    }

}