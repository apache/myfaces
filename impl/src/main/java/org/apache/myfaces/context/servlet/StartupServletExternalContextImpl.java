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

import static org.apache.myfaces.context.servlet.StartupFacesContextImpl.EXCEPTION_TEXT;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.Flash;
import javax.servlet.ServletContext;

/**
 * An ExternalContext implementation for Servlet environments, which is used
 * by StartupFacesContextImpl at container startup and shutdown and which
 * provides ExternalContext functionality that does not require request and
 * response objects.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupServletExternalContextImpl extends ServletExternalContextImplBase
{
    
    private boolean _startup;
    
    public StartupServletExternalContextImpl(boolean startup, ServletContext servletContext)
    {
        super(servletContext);
        
        _startup = startup;
    }
    
    // ~ Methods which are valid to be called during startup and shutdown------
    
    // Note that all methods, which are valid to be called during startup and
    // shutdown are implemented in ServletExternalContextImplBase, because they 
    // are exactly the same as in the real ExternalContext implementation.
    
    // ~ Methods which are unsupported during startup and shutdown-------------

    @Override
    public void dispatch(String path) throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeActionURL(String url)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeNamespace(String name)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeResourceURL(String url)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getAuthType()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRemoteUser()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getRequest()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestContextPath()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getRequestCookieMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String> getRequestHeaderMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Locale getRequestLocale()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<Locale> getRequestLocales()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getRequestMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String> getRequestParameterMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Iterator<String> getRequestParameterNames()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestPathInfo()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestServletPath()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getResponse()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getSession(boolean create)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Map<String, Object> getSessionMap()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Principal getUserPrincipal()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isUserInRole(String role)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void redirect(String url) throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void addResponseCookie(String name, String value,
            Map<String, Object> properties)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void addResponseHeader(String name, String value)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeBookmarkableURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodePartialActionURL(String url)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String encodeRedirectURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Flash getFlash()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public int getRequestContentLength()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestContentType()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestScheme()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestServerName()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public int getRequestServerPort()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public int getResponseBufferSize()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getResponseContentType()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public OutputStream getResponseOutputStream() throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Writer getResponseOutputWriter() throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void invalidateSession()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public boolean isResponseCommitted()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void responseFlushBuffer() throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void responseReset()
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void responseSendError(int statusCode, String message)
            throws IOException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setRequest(Object request)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setRequestCharacterEncoding(String encoding)
            throws UnsupportedEncodingException
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponse(Object response)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseBufferSize(int size)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseCharacterEncoding(String encoding)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseContentLength(int length)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseContentType(String contentType)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseHeader(String name, String value)
    {
        throw new IllegalStateException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public void setResponseStatus(int statusCode)
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
