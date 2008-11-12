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

import java.io.OutputStream;
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
     * @since JSF 2.0
     */
    public void addResponseCookie(String name, String value, Map<String, Object> properties)
    {
        // TODO: JSF 2.0 #24
        // VALIDATE: Should this be asbtract? Check with the EG
    }

    public abstract void dispatch(String path) throws java.io.IOException;

    public abstract String encodeActionURL(String url);

    public abstract String encodeNamespace(String name);

    public abstract String encodeResourceURL(String url);

    public abstract Map<String, Object> getApplicationMap();

    public abstract String getAuthType();

    /**
     * Returns the content length or -1 if the unknown.
     * 
     * @since JSF 2.0
     * @return the length or -1
     */
    public int getContentLength()
    {
        // TODO: JSF 2.0 MYFACES-1950
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return -1;
    }

    public abstract Object getContext();

    /**
     * Returns the name of the underlying context
     * 
     * @since JSF 2.0
     * @return the name or null
     */
    public String getContextName()
    {
        // TODO: JSF 2.0 MYFACES-1950
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return null;
    }

    public abstract String getInitParameter(String name);

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
        throw new UnsupportedOperationException();
    }

    /**
     * @since JSF 2.0
     */
    public String getRequestServerName()
    {
        // TODO: JSF 2.0 #27
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return null;
    }

    /**
     * @since JSF 2.0
     */
    public int getRequestServerPort()
    {
        // TODO: JSF 2.0 #28
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return 0;
    }

    public abstract String getRequestServletPath();

    public abstract java.net.URL getResource(String path) throws java.net.MalformedURLException;

    public abstract java.io.InputStream getResourceAsStream(String path);

    public abstract Set<String> getResourcePaths(String path);

    public abstract Object getResponse();

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
    public OutputStream getResponseOutputStream()
    {
        // TODO: JSF 2.0 #29
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return null;
    }

    public abstract Object getSession(boolean create);

    public abstract Map<String, Object> getSessionMap();

    public abstract java.security.Principal getUserPrincipal();

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
     * @since JSF 2.0
     */
    public void setResponseContentType(String contentType)
    {
        // TODO: JSF 2.0 #31
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
    }

    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException("JSF 1.2 : figure out how to tell if this is a Portlet request");
    }

    /**
     * @since JSF 2.0
     */
    public void invalidateSession()
    {
        // TODO: JSF 2.0 #30
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
    }

    /**
     * @since JSF 2.0
     */
    public boolean isNewSession()
    {
        // TODO: JSF 2.0 MYFACES-1950
        // VALIDATE: Should this be asbtract or throws UnsupportedOperationException? Check with the EG
        return false;
    }

    public abstract boolean isUserInRole(String role);

    public abstract void log(String message);

    public abstract void log(String message, Throwable exception);

    public abstract void redirect(String url) throws java.io.IOException;
}
