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
package org.apache.myfaces.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.myfaces.resource.ResourceImpl;
import org.apache.myfaces.resource.ResourceLoader;
import org.apache.myfaces.resource.ResourceMeta;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.shared_impl.util.StringUtils;

/**
 * DOCUMENT ME!
 *
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * 
 * @version $Revision: 696515 $ $Date: 2008-09-17 19:37:53 -0500 (mer., 17 sept. 2008) $
 */
public class ResourceHandlerImpl extends ResourceHandler
{

    private static final String IS_RESOURCE_REQUEST = "org.apache.myfaces.IS_RESOURCE_REQUEST";

    private ResourceHandlerSupport _resourceHandlerSupport;

    //private static final Log log = LogFactory.getLog(ResourceHandlerImpl.class);
    private static final Logger log = Logger.getLogger(ResourceHandlerImpl.class.getName());

    private static final int _BUFFER_SIZE = 2048;

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
            contentType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(resourceName);
        }

        for (ResourceLoader loader : getResourceHandlerSupport()
                .getResourceLoaders())
        {
            ResourceMeta resourceMeta = deriveResourceMeta(loader,
                    resourceName, libraryName);

            if (resourceMeta != null)
            {
                resource = new ResourceImpl(resourceMeta, loader,
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
    protected ResourceMeta deriveResourceMeta(ResourceLoader resourceLoader,
            String resourceName, String libraryName)
    {
        String localePrefix = getLocalePrefixForLocateResource();
        String resourceVersion = null;
        String libraryVersion = null;
        ResourceMeta resourceId = null;
        
        //1. Try to locate resource in a localized path
        if (localePrefix != null)
        {
            if (null != libraryName)
            {
                String pathToLib = localePrefix + '/' + libraryName;
                libraryVersion = resourceLoader.getLibraryVersion(pathToLib);

                if (null != libraryVersion)
                {
                    String pathToResource = localePrefix + '/'
                            + libraryName + '/' + libraryVersion + '/'
                            + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }
                else
                {
                    String pathToResource = localePrefix + '/'
                            + libraryName + '/' + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(localePrefix, libraryName,
                            libraryVersion, resourceName, resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(localePrefix + '/'+ resourceName);
                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(localePrefix, null, null,
                            resourceName, resourceVersion);
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
                    String pathToResource = (libraryName + '/'
                            + resourceName);
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(null, libraryName,
                            libraryVersion, resourceName, resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(resourceName);
                
                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(null, null, null,
                            resourceName, resourceVersion);
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
            return "javax.faces.resource.Script";
        else if (resourceName.endsWith(".css"))
            return "javax.faces.resource.Stylesheet";
        return null;
    }

    /**
     *  Handle the resource request, writing in the output. 
     *  
     *  This method implements an algorithm semantically identical to 
     *  the one described on the javadoc of ResourceHandler.handleResourceRequest 
     */
    @Override
    public void handleResourceRequest(FacesContext facesContext) throws IOException
    {
        String resourceBasePath = getResourceHandlerSupport()
                .calculateResourceBasePath(facesContext);

        if (resourceBasePath == null)
        {
            // No base name could be calculated, so no further
            //advance could be done here. HttpServletResponse.SC_NOT_FOUND
            //cannot be returned since we cannot extract the 
            //resource base name
            return;
        }

        //We neet to get an instance of HttpServletResponse, but sometimes
        //the response object is wrapped by several instances of 
        //ServletResponseWrapper (like ResponseSwitch).
        //Since we are handling a resource, we can expect to get an 
        //HttpServletResponse.
        
        Object response = facesContext.getExternalContext().getResponse();
        
        //It is safe to cast it to ServletResponse
        ServletResponse servletResponse = (ServletResponse) response;
        
        HttpServletResponse httpServletResponse = null;
        if (response instanceof HttpServletResponse)
        {
            httpServletResponse = (HttpServletResponse) response;
        }
        else if (response instanceof ServletResponseWrapper)
        {
            //iterate until we find a instance that we can cast 
            while (!(response instanceof HttpServletResponse))
            {
                //assume ServletResponseWrapper as wrapper
                response = ((ServletResponseWrapper)response).getResponse();
            }
            //Case where it is an instance of ResponseSwitch
            //in this case just return the inner response
            httpServletResponse = (HttpServletResponse) response;
        }

        if (isResourceIdentifierExcluded(facesContext, resourceBasePath))
        {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String resourceName = null;
        if (resourceBasePath.startsWith(ResourceHandler.RESOURCE_IDENTIFIER))
        {
            resourceName = resourceBasePath
                    .substring(ResourceHandler.RESOURCE_IDENTIFIER.length() + 1);
        }
        else
        {
            //Does not have the conditions for be a resource call
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String libraryName = facesContext.getExternalContext()
                .getRequestParameterMap().get("ln");

        Resource resource = null;
        if (libraryName != null)
        {
            //log.info("libraryName=" + libraryName);
            resource = createResource(resourceName, libraryName);
        }
        else
        {
            resource = createResource(resourceName);
        }

        if (resource == null)
        {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!resource.userAgentNeedsUpdate(facesContext))
        {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        servletResponse.setContentType(resource.getContentType());

        Map<String, String> headers = resource.getResponseHeaders();

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            httpServletResponse.setHeader(entry.getKey(), entry.getValue());
        }

        //serve up the bytes (taken from trinidad ResourceServlet)
        try
        {
            InputStream in = resource.getInputStream();
            OutputStream out = servletResponse.getOutputStream();
            byte[] buffer = new byte[_BUFFER_SIZE];

            try
            {
                int count = pipeBytes(in, out, buffer);
                //set the content lenght
                servletResponse.setContentLength(count);
            }
            finally
            {
                try
                {
                    in.close();
                }
                finally
                {
                    out.close();
                }
            }
        }
        catch (IOException e)
        {
            //TODO: Log using a localized message (which one?)
            if (log.isLoggable(Level.SEVERE))
                log.severe("Error trying to load resource " + resourceName
                        + " with library " + libraryName + " :"
                        + e.getMessage());
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Reads the specified input stream into the provided byte array storage and
     * writes it to the output stream.
     */
    private static int pipeBytes(InputStream in, OutputStream out, byte[] buffer)
            throws IOException
    {
        int count = 0;
        int length;

        while ((length = (in.read(buffer))) >= 0)
        {
            out.write(buffer, 0, length);
            count += length;
        }
        return count;
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
            String resourceBasePath = getResourceHandlerSupport()
                    .calculateResourceBasePath(facesContext);

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

    protected String getLocalePrefixForLocateResource()
    {
        String localePrefix = null;
        FacesContext context = FacesContext.getCurrentInstance();

        String bundleName = context.getApplication().getMessageBundle();

        if (null != bundleName)
        {
            Locale locale = context.getApplication().getViewHandler()
                    .calculateLocale(context);

            ResourceBundle bundle = ResourceBundle
                    .getBundle(bundleName, locale, ClassUtils.getContextClassLoader());

            if (bundle != null)
            {
                localePrefix = bundle.getString(ResourceHandler.LOCALE_PREFIX);                
            }
        }
        return localePrefix;
    }
    
    private static ResourceBundle getBundle(FacesContext facesContext, Locale locale, String bundleName)
    {
        try
        {
            // First we try the JSF implementation class loader
            return ResourceBundle.getBundle(bundleName, locale, facesContext.getClass().getClassLoader());
        }
        catch (MissingResourceException ignore1)
        {
            try
            {
                // Next we try the JSF API class loader
                return ResourceBundle.getBundle(bundleName, locale, ResourceHandlerImpl.class.getClassLoader());
            }
            catch (MissingResourceException ignore2)
            {
                try
                {
                    // Last resort is the context class loader
                    return ResourceBundle.getBundle(bundleName, locale, ClassUtils.getContextClassLoader());
                }
                catch (MissingResourceException damned)
                {
                    return null;
                }
            }
        }
    }

    protected boolean isResourceIdentifierExcluded(FacesContext context,
            String resourceIdentifier)
    {
        String value = context.getExternalContext().getInitParameter(
                RESOURCE_EXCLUDES_PARAM_NAME);
        if (value == null)
        {
            value = RESOURCE_EXCLUDES_DEFAULT_VALUE;
        }
        //TODO: optimize this code
        String[] extensions = StringUtils.splitShortString(value, ' ');
        for (int i = 0; i < extensions.length; i++)
        {
            if (resourceIdentifier.endsWith(extensions[i]))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a library exists or not. This is done delegating
     * to each ResourceLoader used, because each one has a different
     * prefix and way to load resources.
     * 
     */
    @Override
    public boolean libraryExists(String libraryName)
    {
        String localePrefix = getLocalePrefixForLocateResource();

        String pathToLib = null;
        
        if (localePrefix != null)
        {
            //Check with locale
            pathToLib = localePrefix + '/' + libraryName;
            
            for (ResourceLoader loader : getResourceHandlerSupport()
                    .getResourceLoaders())
            {
                if (loader.libraryExists(pathToLib))
                {
                    return true;
                }
            }            
        }

        //Check without locale
        for (ResourceLoader loader : getResourceHandlerSupport()
                .getResourceLoaders())
        {
            if (loader.libraryExists(libraryName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @param resourceHandlerSupport
     *            the resourceHandlerSupport to set
     */
    public void setResourceHandlerSupport(
            ResourceHandlerSupport resourceHandlerSupport)
    {
        _resourceHandlerSupport = resourceHandlerSupport;
    }

    /**
     * @return the resourceHandlerSupport
     */
    protected ResourceHandlerSupport getResourceHandlerSupport()
    {
        if (_resourceHandlerSupport == null)
        {
            _resourceHandlerSupport = new DefaultResourceHandlerSupport();
        }
        return _resourceHandlerSupport;
    }
}
