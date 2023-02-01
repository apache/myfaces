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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.faces.context.Flash;
import jakarta.servlet.ServletContext;

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
    public static final String EXCEPTION_TEXT = "This method is not supported during ";
    
    private boolean startup;
    private ServletContext servletContext;

    public StartupServletExternalContextImpl(ServletContext servletContext, boolean startup)
    {
        super(servletContext);
        this.servletContext = servletContext;
        this.startup = startup;
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
        throw unsupportedOperation();
    }

    @Override
    public String encodeNamespace(String name)
    {
        throw unsupportedOperation();
    }

    @Override
    public String encodeResourceURL(String url)
    {
        throw unsupportedOperation();
    }

    @Override
    public String getAuthType()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRemoteUser()
    {
        throw unsupportedOperation();
    }

    @Override
    public Object getRequest()
    {
        return null;
        //throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public String getRequestContextPath()
    {
        return servletContext.getContextPath();
    }

    @Override
    public Map<String, Object> getRequestCookieMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Map<String, String> getRequestHeaderMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Locale getRequestLocale()
    {
        throw unsupportedOperation();
    }

    @Override
    public Iterator<Locale> getRequestLocales()
    {
        throw unsupportedOperation();
    }

    @Override
    public Map<String, Object> getRequestMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Map<String, String> getRequestParameterMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Iterator<String> getRequestParameterNames()
    {
        throw unsupportedOperation();
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRequestPathInfo()
    {
        return "";
    }

    @Override
    public String getRequestServletPath()
    {
        return "";
    }

    @Override
    public Object getResponse()
    {
        return null;
        //throw new UnsupportedOperationException(EXCEPTION_TEXT + _getTime());
    }

    @Override
    public Object getSession(boolean create)
    {
        if (create)
        {
            throw unsupportedOperation();
        }
        return null;
    }

    @Override
    public String getSessionId(boolean create)
    {
        if (create)
        {
            throw unsupportedOperation();
        }
        return null;
    }

    @Override
    public Map<String, Object> getSessionMap()
    {
        throw unsupportedOperation();
    }

    @Override
    public Principal getUserPrincipal()
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        throw unsupportedOperation();
    }

    @Override
    public String encodeBookmarkableURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        throw unsupportedOperation();
    }

    @Override
    public String encodePartialActionURL(String url)
    {
        throw unsupportedOperation();
    }

    @Override
    public String encodeRedirectURL(String baseUrl,
            Map<String, List<String>> parameters)
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        throw unsupportedOperation();
    }

    @Override
    public int getRequestContentLength()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRequestContentType()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRequestScheme()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getRequestServerName()
    {
        throw unsupportedOperation();
    }

    @Override
    public int getRequestServerPort()
    {
        throw unsupportedOperation();
    }

    @Override
    public int getResponseBufferSize()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        throw unsupportedOperation();
    }

    @Override
    public String getResponseContentType()
    {
        throw unsupportedOperation();
    }

    @Override
    public void invalidateSession()
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean isResponseCommitted()
    {
        throw unsupportedOperation();
    }

    @Override
    public void setRequest(Object request)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding)
            throws UnsupportedEncodingException
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponse(Object response)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponseBufferSize(int size)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponseContentLength(int length)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponseContentType(String contentType)
    {
        throw unsupportedOperation();
    }

    @Override
    public void setResponseHeader(String name, String value)
    {
        throw unsupportedOperation();
    }
    
    @Override
    public void setResponseStatus(int statusCode)
    {
        throw unsupportedOperation();
    }

    /**
     * Cannot dispatch because this is not a page request
     */
    @Override
    public void dispatch(String path) throws IOException
    {
       throw unsupportedOperation();
    }

    /**
     * Cannot redirect because this is not a page request
     */
    @Override
    public void redirect(String url) throws IOException
    {
        throw unsupportedOperation();
    }
    

    @Override
    public void responseFlushBuffer() throws IOException
    {
        throw unsupportedOperation();
    }

    @Override
    public void responseReset()
    {
        throw unsupportedOperation();
    }

    @Override
    public void responseSendError(int statusCode, String message)
            throws IOException
    {
        throw unsupportedOperation();
    }
    
    @Override
    public void addResponseCookie(String name, String value,
            Map<String, Object> properties)
    {
        throw unsupportedOperation();
    }

    @Override
    public void addResponseHeader(String name, String value)
    {
        throw unsupportedOperation();
    }

    @Override
    public Flash getFlash()
    {
        throw unsupportedOperation();
    }

    @Override
    public OutputStream getResponseOutputStream() throws IOException
    {
        throw unsupportedOperation();
    }

    @Override
    public Writer getResponseOutputWriter() throws IOException
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean isSecure()
    {
        throw unsupportedOperation();
    }

    @Override
    public int getSessionMaxInactiveInterval()
    {
        throw unsupportedOperation();
    }

    @Override
    public void setSessionMaxInactiveInterval(int interval)
    {
        throw unsupportedOperation();
    }

    @Override
    public String encodeWebsocketURL(String url)
    {
        throw unsupportedOperation();
    }

    private UnsupportedOperationException unsupportedOperation()
    {
        return new UnsupportedOperationException("This method is not supported during "
                + (startup ? "startup" : "shutdown"));
    }
}
