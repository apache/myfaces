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

package org.apache.myfaces.test.mock.resource;

import org.apache.myfaces.test.mock.MockServletContext;

import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Mock implementation of <code>ResourceHandler</code>.</p>
 * <p>This Mock could be used on situations where all resources
 * are on a specific path.</p>
 * 
 * @author Jakob Korherr (latest modification by $Author: lu4242 $)
 * @version $Revision: 882702 $ $Date: 2009-11-20 15:16:07 -0500 (Vie, 20 Nov 2009) $
 * @since 1.0.0
 */
public class MockSimpleResourceHandler extends ResourceHandler
{

    private static final String IS_RESOURCE_REQUEST = "org.apache.myfaces.IS_RESOURCE_REQUEST";

    /**
     * It checks version like this: /1/, /1_0/, /1_0_0/, /100_100/
     * <p/>
     * Used on getLibraryVersion to filter resource directories
     */
    protected static final Pattern VERSION_CHECKER = Pattern
            .compile("/\\p{Digit}+(_\\p{Digit}*)*/");

    /**
     * It checks version like this: /1.js, /1_0.js, /1_0_0.js, /100_100.js
     * <p/>
     * Used on getResourceVersion to filter resources
     */
    protected static final Pattern RESOURCE_VERSION_CHECKER = Pattern
            .compile("/\\p{Digit}+(_\\p{Digit}*)*\\..*");

    private File _documentRoot;

    /**
     * @param documentRoot parent folder of resource directories. Must not be <code>null</code>
     */
    public MockSimpleResourceHandler(File documentRoot)
    {
        if (documentRoot == null)
        {
            throw new NullPointerException("documentRoot must not be null");
        }

        _documentRoot = documentRoot;

        ((MockServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext())
                .setDocumentRoot(_documentRoot);
    }

    @Override
    public Resource createResource(String resourceName)
    {
        return createResource(resourceName, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        return createResource(resourceName, libraryName, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName,
            String contentType)
    {
        String prefix = getLocalePrefixForLocateResource();
        String libraryVersion = getLibraryVersion(prefix + '/' + libraryName);

        String pathToResource;
        if (null != libraryVersion)
        {
            pathToResource = prefix + '/' + libraryName + '/' + libraryVersion
                    + '/' + resourceName;
        }
        else
        {
            pathToResource = prefix + '/' + libraryName + '/' + resourceName;
        }

        return new MockSimpleResource(prefix, libraryName, libraryVersion,
                resourceName, getResourceVersion(pathToResource), _documentRoot);
    }

    @Override
    public String getRendererTypeForResourceName(String resourceName)
    {
        if (resourceName.endsWith(".js"))
        {
            return "javax.faces.resource.Script";
        }
        else if (resourceName.endsWith(".css"))
        {
            return "javax.faces.resource.Stylesheet";
        }
        return null;
    }

    @Override
    public void handleResourceRequest(FacesContext context) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResourceRequest(FacesContext facesContext)
    {
        // Since this method could be called many times we save it
        //on request map so the first time is calculated it remains
        //alive until the end of the request
        Boolean value = (Boolean) facesContext.getExternalContext()
                .getRequestMap().get(IS_RESOURCE_REQUEST);

        if (value != null && value.booleanValue())
        {
            //return the saved value
            return value.booleanValue();
        }
        else
        {
            // assuming that we don't have servlet mapping
            String resourceBasePath = facesContext.getExternalContext()
                    .getRequestPathInfo();

            if (resourceBasePath != null
                    && resourceBasePath
                            .startsWith(ResourceHandler.RESOURCE_IDENTIFIER))
            {
                facesContext.getExternalContext().getRequestMap().put(
                        IS_RESOURCE_REQUEST, Boolean.TRUE);
                return true;
            }
            else
            {
                facesContext.getExternalContext().getRequestMap().put(
                        IS_RESOURCE_REQUEST, Boolean.FALSE);
                return false;
            }
        }
    }

    @Override
    public boolean libraryExists(String libraryName)
    {
        String localePrefix = getLocalePrefixForLocateResource();

        String pathToLib;

        if (localePrefix != null)
        {
            //Check with locale
            pathToLib = localePrefix + '/' + libraryName;
        }
        else
        {
            pathToLib = libraryName;
        }

        try
        {
            URL url = FacesContext.getCurrentInstance().getExternalContext()
                    .getResource('/' + pathToLib);
            return (url != null);
        }
        catch (MalformedURLException e)
        {
            return false;
        }
    }

    protected String getLocalePrefixForLocateResource()
    {
        String localePrefix = null;
        FacesContext context = FacesContext.getCurrentInstance();

        String bundleName = context.getApplication().getMessageBundle();

        if (null != bundleName)
        {
            Locale locale = context.getApplication().getViewHandler().calculateLocale(context);

            ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                    locale, getContextClassLoader());

            if (bundle != null)
            {
                try
                {
                    localePrefix = bundle.getString(ResourceHandler.LOCALE_PREFIX);
                }
                catch (MissingResourceException e)
                {
                    // Ignore it and return null
                }
            }
        }
        return localePrefix;
    }

    /**
     * Gets the ClassLoader associated with the current thread.  Includes a check for priviledges
     * against java2 security to ensure no security related exceptions are encountered.
     *
     * @return ClassLoader
     * @since 3.0.6
     */
    private static ClassLoader getContextClassLoader()
    {
        if (System.getSecurityManager() != null)
        {
            try
            {
                ClassLoader cl = AccessController.doPrivileged(
                        (PrivilegedExceptionAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
                return cl;
            }
            catch (PrivilegedActionException pae)
            {
                throw new FacesException(pae);
            }
        }
        else
        {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    private String getLibraryVersion(String path)
    {
        ExternalContext context = FacesContext.getCurrentInstance()
                .getExternalContext();

        String libraryVersion = null;
        Set<String> libraryPaths = context.getResourcePaths('/' + path);
        if (null != libraryPaths && !libraryPaths.isEmpty())
        {
            // Look in the libraryPaths for versioned libraries.
            // If one or more versioned libraries are found, take
            // the one with the "highest" version number as the value
            // of libraryVersion. If no versioned libraries
            // are found, let libraryVersion remain null.

            for (String libraryPath : libraryPaths)
            {
                String version = libraryPath.substring(path.length());

                if (VERSION_CHECKER.matcher(version).matches())
                {
                    version = version.substring(1, version.length() - 1);
                    if (libraryVersion == null)
                    {
                        libraryVersion = version;
                    }
                    else if (compareVersion(libraryVersion, version) < 0)
                    {
                        libraryVersion = version;
                    }
                }
            }
        }
        return libraryVersion;
    }

    private int compareVersion(String s1, String s2)
    {
        int n1 = 0;
        int n2 = 0;
        String o1 = s1;
        String o2 = s2;

        boolean p1 = true;
        boolean p2 = true;

        while (n1 == n2 && (p1 || p2))
        {
            int i1 = o1.indexOf('_');
            int i2 = o2.indexOf('_');
            if (i1 < 0)
            {
                if (o1.length() > 0)
                {
                    p1 = false;
                    n1 = Integer.parseInt(o1);
                    o1 = "";
                }
                else
                {
                    p1 = false;
                    n1 = 0;
                }
            }
            else
            {
                n1 = Integer.valueOf(o1.substring(0, i1));
                o1 = o1.substring(i1 + 1);
            }
            if (i2 < 0)
            {
                if (o2.length() > 0)
                {
                    p2 = false;
                    n2 = Integer.valueOf(o2);
                    o2 = "";
                }
                else
                {
                    p2 = false;
                    n2 = 0;
                }
            }
            else
            {
                n2 = Integer.valueOf(o2.substring(0, i2));
                o2 = o2.substring(i2 + 1);
            }
        }

        if (n1 == n2)
        {
            return s1.length() - s2.length();
        }
        return n1 - n2;
    }

    private String getResourceVersion(String path)
    {
        ExternalContext context = FacesContext.getCurrentInstance()
                .getExternalContext();
        String resourceVersion = null;
        Set<String> resourcePaths = context.getResourcePaths('/' + path);

        if (null != resourcePaths && !resourcePaths.isEmpty())
        {
            // resourceVersion = // execute the comment
            // Look in the resourcePaths for versioned resources.
            // If one or more versioned resources are found, take
            // the one with the "highest" version number as the value
            // of resourceVersion. If no versioned libraries
            // are found, let resourceVersion remain null.
            for (String resourcePath : resourcePaths)
            {
                String version = resourcePath.substring(path.length());

                if (RESOURCE_VERSION_CHECKER.matcher(version).matches())
                {
                    version = version.substring(1, version.lastIndexOf('.'));
                    if (resourceVersion == null)
                    {
                        resourceVersion = version;
                    }
                    else if (compareVersion(resourceVersion, version) < 0)
                    {
                        resourceVersion = version;
                    }
                }
            }
        }
        return resourceVersion;
    }
}
