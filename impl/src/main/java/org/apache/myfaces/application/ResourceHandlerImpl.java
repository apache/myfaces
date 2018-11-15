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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.resource.ResourceHandlerCache;
import org.apache.myfaces.shared.resource.ResourceHandlerCache.ResourceValue;
import org.apache.myfaces.shared.resource.ResourceHandlerSupport;
import org.apache.myfaces.shared.resource.ResourceImpl;
import org.apache.myfaces.shared.resource.ResourceLoader;
import org.apache.myfaces.shared.resource.ResourceMeta;
import org.apache.myfaces.shared.resource.ResourceValidationUtils;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.shared.util.ExternalContextUtils;
import org.apache.myfaces.shared.util.StringUtils;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
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

/**
 * DOCUMENT ME!
 *
 * @author Simon Lessard (latest modification by $Author$)
 * 
 * @version $Revision$ $Date$
 */
public class ResourceHandlerImpl extends ResourceHandler
{

    private static final String IS_RESOURCE_REQUEST = "org.apache.myfaces.IS_RESOURCE_REQUEST";

    private ResourceHandlerSupport _resourceHandlerSupport;

    private ResourceHandlerCache _resourceHandlerCache;

    //private static final Log log = LogFactory.getLog(ResourceHandlerImpl.class);
    private static final Logger log = Logger.getLogger(ResourceHandlerImpl.class.getName());

    /**
     * Allow slash in the library name of a Resource. 
     */
    @JSFWebConfigParam(since="2.1.6, 2.0.12", defaultValue="false", 
            expectedValues="true, false", group="resources")
    public static final String INIT_PARAM_STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME = 
            "org.apache.myfaces.STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME";
    public static final boolean INIT_PARAM_STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME_DEFAULT = false;
    
    /**
     * Define the default buffer size that is used between Resource.getInputStream() and 
     * httpServletResponse.getOutputStream() when rendering resources using the default
     * ResourceHandler.
     */
    @JSFWebConfigParam(since="2.1.10, 2.0.16", defaultValue="2048", group="resources")
    public static final String INIT_PARAM_RESOURCE_BUFFER_SIZE = "org.apache.myfaces.RESOURCE_BUFFER_SIZE";
    public static final int INIT_PARAM_RESOURCE_BUFFER_SIZE_DEFAULT = 2048;
    
    private Boolean _allowSlashLibraryName;
    private int _resourceBufferSize = -1;
    
    private String[] _excludedResourceExtensions;

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
        
        if (!ResourceValidationUtils.isValidResourceName(resourceName))
        {
            return null;
        }
        if (libraryName != null && !ResourceValidationUtils.isValidLibraryName(
                libraryName, isAllowSlashesLibraryName()))
        {
            return null;
        }
        
        if (contentType == null)
        {
            //Resolve contentType using ExternalContext.getMimeType
            contentType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(resourceName);
        }

        final String localePrefix = getLocalePrefixForLocateResource();

        // check cache
        if(getResourceLoaderCache().containsResource(resourceName, libraryName, contentType, localePrefix))
        {
            ResourceValue resourceValue = getResourceLoaderCache().getResource(
                    resourceName, libraryName, contentType, localePrefix);
            
            resource = new ResourceImpl(resourceValue.getResourceMeta(), resourceValue.getResourceLoader(),
                    getResourceHandlerSupport(), contentType);
        }
        else
        {
            for (ResourceLoader loader : getResourceHandlerSupport().getResourceLoaders())
            {
                ResourceMeta resourceMeta = deriveResourceMeta(loader, resourceName, libraryName, localePrefix);
    
                if (resourceMeta != null)
                {
                    resource = new ResourceImpl(resourceMeta, loader, getResourceHandlerSupport(), contentType);

                    // cache it
                    getResourceLoaderCache().putResource(resourceName, libraryName, contentType,
                            localePrefix, resourceMeta, loader);
                    break;
                }
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
            String resourceName, String libraryName, String localePrefix)
    {
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
        {
            return "javax.faces.resource.Script";
        }
        else if (resourceName.endsWith(".css"))
        {
            return "javax.faces.resource.Stylesheet";
        }
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
        //try
        //{
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
    
            // We neet to get an instance of HttpServletResponse, but sometimes
            // the response object is wrapped by several instances of 
            // ServletResponseWrapper (like ResponseSwitch).
            // Since we are handling a resource, we can expect to get an 
            // HttpServletResponse.
            ExternalContext extContext = facesContext.getExternalContext();
            Object response = extContext.getResponse();
            HttpServletResponse httpServletResponse = ExternalContextUtils.getHttpServletResponse(response);
            if (httpServletResponse == null)
            {
                throw new IllegalStateException("Could not obtain an instance of HttpServletResponse.");
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
                
                if (resourceBasePath != null && !ResourceValidationUtils.isValidResourceName(resourceName))
                {
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            else
            {
                //Does not have the conditions for be a resource call
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
    
            String libraryName = facesContext.getExternalContext()
                    .getRequestParameterMap().get("ln");
    
            if (libraryName != null && !ResourceValidationUtils.isValidLibraryName(
                    libraryName, isAllowSlashesLibraryName()))
            {
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Resource resource = null;
            if (libraryName != null)
            {
                //log.info("libraryName=" + libraryName);
                resource = facesContext.getApplication().getResourceHandler().createResource(resourceName, libraryName);
            }
            else
            {
                resource = facesContext.getApplication().getResourceHandler().createResource(resourceName);
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
    
            httpServletResponse.setContentType(_getContentType(resource, facesContext.getExternalContext()));
    
            Map<String, String> headers = resource.getResponseHeaders();
    
            for (Map.Entry<String, String> entry : headers.entrySet())
            {
                httpServletResponse.setHeader(entry.getKey(), entry.getValue());
            }
    
            // Sets the preferred buffer size for the body of the response
            extContext.setResponseBufferSize(this.getResourceBufferSize());
            
            //serve up the bytes (taken from trinidad ResourceServlet)
            try
            {
                InputStream in = resource.getInputStream();
                OutputStream out = httpServletResponse.getOutputStream();
                //byte[] buffer = new byte[_BUFFER_SIZE];
                byte[] buffer = new byte[this.getResourceBufferSize()];
    
                try
                {
                    int count = pipeBytes(in, out, buffer);
                    //set the content lenght
                    if (!httpServletResponse.isCommitted())
                    {
                        httpServletResponse.setContentLength(count);
                    }
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
                if (isConnectionAbort(e))
                {
                    log.log(Level.INFO,"Connection was aborted while loading resource " + resourceName
                            + " with library " + libraryName);
                }
                else
                {
                    if (log.isLoggable(Level.WARNING))
                    {
                        log.log(Level.WARNING,"Error trying to load and send resource " + resourceName
                                + " with library " + libraryName + " :"
                                + e.getMessage(), e);
                    }
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        //}
        //catch (Throwable ex)
        //{
            // handle the Throwable accordingly. Maybe generate an error page.
            // FIXME we are creating a html error page for a non html request here
            // shouln't we do something better? -=Jakob Korherr=-
            //ErrorPageWriter.handleThrowable(facesContext, ex);
        //}
    }

    private static boolean isConnectionAbort(IOException e)
    {
        return e.getClass().getCanonicalName().equals("org.apache.catalina.connector.ClientAbortException");
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
        // on request map so the first time is calculated it remains
        // alive until the end of the request
        Boolean value = (Boolean) facesContext.getAttributes().get(IS_RESOURCE_REQUEST);

        if (value == null)
        {
            String resourceBasePath = getResourceHandlerSupport()
                    .calculateResourceBasePath(facesContext);

            value = resourceBasePath != null
                    && resourceBasePath.startsWith(ResourceHandler.RESOURCE_IDENTIFIER);
            facesContext.getAttributes().put(IS_RESOURCE_REQUEST, value);
        }
        return value;
    }

    protected String getLocalePrefixForLocateResource()
    {
        String localePrefix = null;
        FacesContext context = FacesContext.getCurrentInstance();
        boolean isResourceRequest = context.getApplication().getResourceHandler().isResourceRequest(context);

        if (isResourceRequest)
        {
            localePrefix = context.getExternalContext().getRequestParameterMap().get("loc");
            
            if (localePrefix != null)
            {
                if (!ResourceValidationUtils.isValidLocalePrefix(localePrefix))
                {
                    return null;
                }
                return localePrefix;
            }
        }
        
        String bundleName = context.getApplication().getMessageBundle();

        if (null != bundleName)
        {
            Locale locale = null;
            
            if (isResourceRequest || context.getViewRoot() == null)
            {
                locale = context.getApplication().getViewHandler()
                                .calculateLocale(context);
            }
            else
            {
                locale = context.getViewRoot().getLocale();
            }

            try
            {
                ResourceBundle bundle = ResourceBundle
                        .getBundle(bundleName, locale, ClassUtils.getContextClassLoader());

                if (bundle != null)
                {
                    localePrefix = bundle.getString(ResourceHandler.LOCALE_PREFIX);
                }
            }
            catch (MissingResourceException e)
            {
                // Ignore it and return null
            }
        }
        return localePrefix;
    }

    protected boolean isResourceIdentifierExcluded(FacesContext context, String resourceIdentifier)
    {
        if (_excludedResourceExtensions == null)
        {
            String value = WebConfigParamUtils.getStringInitParameter(context.getExternalContext(),
                            RESOURCE_EXCLUDES_PARAM_NAME,
                            RESOURCE_EXCLUDES_DEFAULT_VALUE);
            
            _excludedResourceExtensions = StringUtils.splitShortString(value, ' ');
        }
        
        for (int i = 0; i < _excludedResourceExtensions.length; i++)
        {
            if (resourceIdentifier.endsWith(_excludedResourceExtensions[i]))
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
        
        if (libraryName != null && !ResourceValidationUtils.isValidLibraryName(
                libraryName, isAllowSlashesLibraryName()))
        {
            return false;
        }
        
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

    private ResourceHandlerCache getResourceLoaderCache()
    {
        if (_resourceHandlerCache == null)
        {
            _resourceHandlerCache = new ResourceHandlerCache();
        }
        return _resourceHandlerCache;
    }

    private String _getContentType(Resource resource, ExternalContext externalContext)
    {
        String contentType = resource.getContentType();

        // the resource does not provide a content-type --> determine it via mime-type
        if (contentType == null || contentType.length() == 0)
        {
            String resourceName = getWrappedResourceName(resource);

            if (resourceName != null)
            {
                contentType = externalContext.getMimeType(resourceName);
            }
        }

        return contentType;
    }

    /**
     * Recursively unwarp the resource until we find the real resourceName
     * This is needed because the JSF2 specced ResourceWrapper doesn't override
     * the getResourceName() method :(
     * @param resource
     * @return the first non-null resourceName or <code>null</code> if none set
     */
    private String getWrappedResourceName(Resource resource)
    {
        String resourceName = resource.getResourceName();
        if (resourceName != null)
        {
            return resourceName;
        }

        if (resource instanceof ResourceWrapper)
        {
            return getWrappedResourceName(((ResourceWrapper) resource).getWrapped());
        }

        return null;
    }
    
    protected boolean isAllowSlashesLibraryName()
    {
        if (_allowSlashLibraryName == null)
        {
            _allowSlashLibraryName = WebConfigParamUtils.getBooleanInitParameter(
                    FacesContext.getCurrentInstance().getExternalContext(), 
                    INIT_PARAM_STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME,
                    INIT_PARAM_STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME_DEFAULT);
        }
        return _allowSlashLibraryName;
    }

    protected int getResourceBufferSize()
    {
        if (_resourceBufferSize == -1)
        {
            _resourceBufferSize = WebConfigParamUtils.getIntegerInitParameter(
                FacesContext.getCurrentInstance().getExternalContext(),
                INIT_PARAM_RESOURCE_BUFFER_SIZE,
                INIT_PARAM_RESOURCE_BUFFER_SIZE_DEFAULT);
        }
        return _resourceBufferSize;
    }

}
