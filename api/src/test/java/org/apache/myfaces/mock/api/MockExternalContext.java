/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.myfaces.mock.api;

import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Locale;
import java.util.Iterator;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;

public class MockExternalContext extends ExternalContext
{
    public void dispatch(String path) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeActionURL(String url)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeNamespace(String name)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String encodeResourceURL(String url)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getApplicationMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAuthType()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getContext()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getInitParameter(String name)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getInitParameterMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteUser()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getRequest()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestContextPath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestCookieMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestHeaderMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestHeaderValuesMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getRequestLocale()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator getRequestLocales()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestParameterMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator getRequestParameterNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getRequestParameterValuesMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestPathInfo()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestServletPath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getResourceAsStream(String path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set getResourcePaths(String path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getResponse()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getSession(boolean create)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getSessionMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal getUserPrincipal()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserInRole(String role)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void log(String message)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void log(String message, Throwable exception)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void redirect(String url) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
