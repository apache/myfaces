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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>ResourceHandler</code>.</p>
 * <p>This ResourceHandler implementation try to follow the default algorithm
 * defined by the spec, so it try to load resources using the current 
 * ExternalContext and the specified ClassLoader, in the same locations
 * it is expected ("resources" and "META-INF/resources").</p>
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class MockResourceHandler extends ResourceHandler
{

    private boolean _resourceRequest;

    private MockResourceHandlerSupport resourceHandlerSupport;

    private ClassLoader _classLoader;

    public MockResourceHandler()
    {
        _classLoader = getContextClassLoader();
        resourceHandlerSupport = new MockResourceHandlerSupport(true, ".jsf",
                _classLoader);
    }

    public MockResourceHandler(ClassLoader classLoader)
    {
        if (classLoader == null)
        {
            _classLoader = getContextClassLoader();
        }
        else
        {
            _classLoader = classLoader;
        }

        resourceHandlerSupport = new MockResourceHandlerSupport(true, ".jsf",
                _classLoader);
    }

    public MockResourceHandler(boolean extensionMapping, String mapping,
            ClassLoader classLoader)
    {
        if (classLoader == null)
        {
            _classLoader = getContextClassLoader();
        }
        else
        {
            _classLoader = classLoader;
        }

        resourceHandlerSupport = new MockResourceHandlerSupport(
                extensionMapping, mapping, _classLoader);
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
        Resource resource = null;

        if (contentType == null)
        {
            //Resolve contentType using ExternalContext.getMimeType
            contentType = FacesContext.getCurrentInstance()
                    .getExternalContext().getMimeType(resourceName);
        }

        for (MockResourceLoader loader : getResourceHandlerSupport()
                .getResourceLoaders())
        {
            MockResourceMeta resourceMeta = deriveResourceMeta(loader,
                    resourceName, libraryName);

            if (resourceMeta != null)
            {
                resource = new MockResource(resourceMeta, loader,
                        getResourceHandlerSupport(), contentType);
                break;
            }
        }
        return resource;
    }

    /**
     * This method try to create a ResourceMeta for a specific resource
     * loader. If no library, or resource is found, just return null,
     * so the algorithm in createResource can continue checking with the 
     * next registered ResourceLoader. 
     */
    protected MockResourceMeta deriveResourceMeta(
            MockResourceLoader resourceLoader, String resourceName,
            String libraryName)
    {
        String localePrefix = getLocalePrefixForLocateResource();
        String resourceVersion = null;
        String libraryVersion = null;
        MockResourceMeta resourceId = null;

        //1. Try to locate resource in a localized path
        if (localePrefix != null)
        {
            if (null != libraryName)
            {
                String pathToLib = localePrefix + '/' + libraryName;
                libraryVersion = resourceLoader.getLibraryVersion(pathToLib);

                if (null != libraryVersion)
                {
                    String pathToResource = localePrefix + '/' + libraryName
                            + '/' + libraryVersion + '/' + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }
                else
                {
                    String pathToResource = localePrefix + '/' + libraryName
                            + '/' + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && MockResourceLoader.VERSION_INVALID
                        .equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(
                            localePrefix, libraryName, libraryVersion,
                            resourceName, resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(localePrefix + '/' + resourceName);
                if (!(resourceVersion != null && MockResourceLoader.VERSION_INVALID
                        .equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(
                            localePrefix, null, null, resourceName,
                            resourceVersion);
                }
            }

            if (resourceId != null)
            {
                URL url = resourceLoader.getResourceURL(resourceId);
                if (url == null)
                {
                    resourceId = null;
                }
            }
        }

        //2. Try to localize resource in a non localized path
        if (resourceId == null)
        {
            if (null != libraryName)
            {
                libraryVersion = resourceLoader.getLibraryVersion(libraryName);

                if (null != libraryVersion)
                {
                    String pathToResource = (libraryName + '/' + libraryVersion
                            + '/' + resourceName);
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }
                else
                {
                    String pathToResource = (libraryName + '/' + resourceName);
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && MockResourceLoader.VERSION_INVALID
                        .equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(null,
                            libraryName, libraryVersion, resourceName,
                            resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(resourceName);

                if (!(resourceVersion != null && MockResourceLoader.VERSION_INVALID
                        .equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(null, null,
                            null, resourceName, resourceVersion);
                }
            }

            if (resourceId != null)
            {
                URL url = resourceLoader.getResourceURL(resourceId);
                if (url == null)
                {
                    resourceId = null;
                }
            }
        }

        return resourceId;
    }

    @Override
    public String getRendererTypeForResourceName(String resourceName)
    {
        if (resourceName.endsWith(".js"))
        {
            return "jakarta.faces.resource.Script";
        }
        else if (resourceName.endsWith(".css"))
        {
            return "jakarta.faces.resource.Stylesheet";
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
        return _resourceRequest;
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
            Locale locale = context.getApplication().getViewHandler()
                    .calculateLocale(context);

            ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                    locale, getContextClassLoader());

            if (bundle != null)
            {
                try
                {
                    localePrefix = bundle
                            .getString(ResourceHandler.LOCALE_PREFIX);
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
     */
    static ClassLoader getContextClassLoader()
    {
        if (System.getSecurityManager() != null)
        {
            try
            {
                ClassLoader cl = AccessController
                        .doPrivileged(new PrivilegedExceptionAction<ClassLoader>()
                        {
                            public ClassLoader run()
                                    throws PrivilegedActionException
                            {
                                return Thread.currentThread()
                                        .getContextClassLoader();
                            }
                        });
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

    public MockResourceHandlerSupport getResourceHandlerSupport()
    {
        return resourceHandlerSupport;
    }

    public void setResourceHandlerSupport(
            MockResourceHandlerSupport resourceHandlerSupport)
    {
        this.resourceHandlerSupport = resourceHandlerSupport;
    }

    public void setResourceRequest(boolean resourceRequest)
    {
        this._resourceRequest = resourceRequest;
    }

    public boolean isResourceRequest()
    {
        return _resourceRequest;
    }
}