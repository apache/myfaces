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
package javax.faces.context;

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

    public abstract void dispatch(String path)
            throws java.io.IOException;

    public abstract String encodeActionURL(String url);

    public abstract String encodeNamespace(String name);

    public abstract String encodeResourceURL(String url);

    public abstract Map<String, Object> getApplicationMap();

    public abstract String getAuthType();

    public abstract Object getContext();

    public abstract String getInitParameter(String name);

    public abstract Map getInitParameterMap();

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

    public abstract String getRequestServletPath();

    public abstract java.net.URL getResource(String path)
            throws java.net.MalformedURLException;

    public abstract java.io.InputStream getResourceAsStream(String path);

    public abstract Set<String> getResourcePaths(String path);

    public abstract Object getResponse();
    
    public abstract String getResponseContentType();

    public abstract Object getSession(boolean create);

    public abstract Map<String, Object> getSessionMap();

    public abstract java.security.Principal getUserPrincipal();

    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * @since JSF 1.2
     * @param request
     */
    public void setRequest(java.lang.Object request)
    {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * @since JSF 1.2
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public void setRequestCharacterEncoding(java.lang.String encoding)
    		throws java.io.UnsupportedEncodingException{
    	
    	throw new UnsupportedOperationException();
    }
    
    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * @since JSF 1.2
     * @param response
     */
    public void setResponse(java.lang.Object response)
    {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * throws <code>UnsupportedOperationException</code> by default.
     * @since JSF 1.2
     * @param encoding
     */
    public void setResponseCharacterEncoding(java.lang.String encoding)
    {
    	throw new UnsupportedOperationException();
    }
    
    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException(
                "JSF 1.2 : figure out how to tell if this is a Portlet request");
    }
    
    public abstract boolean isUserInRole(String role);

    public abstract void log(String message);

    public abstract void log(String message,
                             Throwable exception);

    public abstract void redirect(String url)
            throws java.io.IOException;
}
