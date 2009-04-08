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
package javax.faces.context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class ExternalContext
{
    public static final String BASIC_AUTH = "BASIC";
    public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";
    public static final String DIGEST_AUTH = "DIGEST";
    public static final String FORM_AUTH = "FORM";

    /**
     * 
     * @param name
     * @param value
     * @param properties
     * 
     * @since 2.0
     */
    public void addResponseCookie(String name, String value, Map<String, Object> properties)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param name
     * @param value
     * 
     * @since 2.0
     */
    public void addResponseHeader(String name, String value)
    {
        throw new UnsupportedOperationException();
    }

    public abstract void dispatch(String path) throws IOException;

    public abstract String encodeActionURL(String url);

    public abstract String encodeNamespace(String name);

    public abstract String encodeResourceURL(String url);

    public abstract Map<String, Object> getApplicationMap();

    public abstract String getAuthType();

    public abstract Object getContext();

    /**
     * Returns the name of the underlying context
     * 
     * @return the name or null
     * 
     * @since 2.0
     */
    public String getContextName()
    {
        // TODO: IMPLEMENT IMPL JSF 2.0 MYFACES-1950
        throw new UnsupportedOperationException();
    }

    public abstract String getInitParameter(String name);

    // FIXME: Notify EG about generic usage
    public abstract Map getInitParameterMap();

    /**
     * @since JSF 2.0
     */
    public String getMimeType(String file)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public String getRealPath(String path)
    {
        throw new UnsupportedOperationException();
    }

    public abstract String getRemoteUser();

    public abstract Object getRequest();

    public String getRequestCharacterEncoding()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @return
     * 
     * @since 2.0
     */
    public int getRequestContentLength()
    {
        throw new UnsupportedOperationException();
    }

    public String getRequestContentType()
    {
        throw new UnsupportedOperationException();
    }

    public abstract String getRequestContextPath();

    public abstract Map<String, Object> getRequestCookieMap();

    public abstract Map<String, String> getRequestHeaderMap();

    public abstract Map<String, String[]> getRequestHeaderValuesMap();

    public abstract Locale getRequestLocale();

    public abstract Iterator<Locale> getRequestLocales();

    public abstract Map<String, Object> getRequestMap();

    public abstract Map<String, String> getRequestParameterMap();

    public abstract Iterator<String> getRequestParameterNames();

    public abstract Map<String, String[]> getRequestParameterValuesMap();

    public abstract String getRequestPathInfo();

    /**
     * @since JSF 2.0
     */
    public String getRequestScheme()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public String getRequestServerName()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public int getRequestServerPort()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    public abstract String getRequestServletPath();

    public abstract java.net.URL getResource(String path) throws java.net.MalformedURLException;

    public abstract java.io.InputStream getResourceAsStream(String path);

    public abstract Set<String> getResourcePaths(String path);

    public abstract Object getResponse();

    /**
     * 
     * @return
     * 
     * @since 2.0
     */
    public int getResponseBufferSize()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException("JSF 1.2 : figure out how to tell if this is a Portlet request");
    }

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * 
     * @since JSF 1.2
     */
    public String getResponseContentType()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public OutputStream getResponseOutputStream() throws IOException
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public Writer getResponseOutputWriter() throws IOException
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    public abstract Object getSession(boolean create);

    public abstract Map<String, Object> getSessionMap();

    public abstract java.security.Principal getUserPrincipal();

    /**
     * 
     * 
     * @since 2.0
     */
    public void invalidateSession()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @return
     * 
     * @since 2.0
     */
    public boolean isResponseCommitted()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    public abstract boolean isUserInRole(String role);

    public abstract void log(String message);

    public abstract void log(String message, Throwable exception);

    public abstract void redirect(String url) throws java.io.IOException;

    /**
     * 
     * @throws IOException
     * 
     * @since 2.0
     */
    public void responseFlushBuffer() throws IOException
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @since 2.0
     */
    public void responseReset()
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param statusCode
     * @param message
     * @throws IOException
     * 
     * @since 2.0
     */
    public void responseSendError(int statusCode, String message) throws IOException
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * 
     * @since JSF 1.2
     * @param request
     */
    public void setRequest(Object request)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * 
     * @since JSF 1.2
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public void setRequestCharacterEncoding(String encoding) throws java.io.UnsupportedEncodingException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * 
     * @since JSF 1.2
     * @param response
     */
    public void setResponse(Object response)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param size
     * 
     * @since 2.0
     */
    public void setResponseBufferSize(int size)
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * 
     * @since JSF 1.2
     * @param encoding
     */
    public void setResponseCharacterEncoding(String encoding)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param length
     * 
     * @since 2.0
     */
    public void setResponseContentLength(int length)
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param contentType
     * 
     * @since 2.0
     */
    public void setResponseContentType(String contentType)
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @param name
     * @param value
     * 
     * @since 2.0
     */
    public void setResponseHeader(String name, String value)
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }
    
    /**
     * 
     * @param statusCode
     * 
     * @since 2.0
     */
    public void setResponseStatus(int statusCode)
    {
        // TODO: IMPLEMENT IMPL
        throw new UnsupportedOperationException();
    }
}
