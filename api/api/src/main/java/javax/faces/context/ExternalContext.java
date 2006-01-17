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
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
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

    public abstract Map getApplicationMap();

    public abstract String getAuthType();

    public abstract Object getContext();

    public abstract String getInitParameter(String name);

    public abstract Map getInitParameterMap();

    public abstract String getRemoteUser();

    public abstract Object getRequest();

    public abstract String getRequestContextPath();

    public abstract Map getRequestCookieMap();

    public abstract Map getRequestHeaderMap();

    public abstract Map getRequestHeaderValuesMap();

    public abstract Locale getRequestLocale();

    public abstract Iterator getRequestLocales();

    public abstract Map getRequestMap();

    public abstract Map getRequestParameterMap();

    public abstract Iterator getRequestParameterNames();

    public abstract Map getRequestParameterValuesMap();

    public abstract String getRequestPathInfo();

    public abstract String getRequestServletPath();

    public abstract java.net.URL getResource(String path)
            throws java.net.MalformedURLException;

    public abstract java.io.InputStream getResourceAsStream(String path);

    public abstract Set getResourcePaths(String path);

    public abstract Object getResponse();

    public abstract Object getSession(boolean create);

    public abstract Map getSessionMap();

    public abstract java.security.Principal getUserPrincipal();

    public abstract boolean isUserInRole(String role);

    public abstract void log(String message);

    public abstract void log(String message,
                             Throwable exception);

    public abstract void redirect(String url)
            throws java.io.IOException;
}
