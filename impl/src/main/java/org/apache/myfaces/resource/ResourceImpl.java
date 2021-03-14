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
package org.apache.myfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.application.FacesServletMapping;
import org.apache.myfaces.application.FacesServletMappingUtils;
import org.apache.myfaces.config.MyfacesConfig;

/**
 * Default implementation for resources
 */
public class ResourceImpl extends Resource implements ContractResource
{
    protected final static String JAKARTA_FACES_LIBRARY_NAME = "jakarta.faces";
    protected final static String JSF_JS_RESOURCE_NAME = "jsf.js";


    private ResourceMeta _resourceMeta;
    private ResourceLoader _resourceLoader;
    private ResourceHandlerSupport _resourceHandlerSupport;
    
    private URL _url;
    private String _requestPath;
    
    public ResourceImpl(ResourceMeta resourceMeta, 
            ResourceLoader resourceLoader, ResourceHandlerSupport support, String contentType)
    {
        _resourceMeta = resourceMeta;
        _resourceLoader = resourceLoader;
        _resourceHandlerSupport = support;
        setLibraryName(resourceMeta.getLibraryName());
        setResourceName(resourceMeta.getResourceName());
        setContentType(contentType);
    }
    
    public ResourceImpl(ResourceMeta resourceMeta, 
            ResourceLoader resourceLoader, ResourceHandlerSupport support, String contentType,
            URL url, String requestPath)
    {
        
        _resourceMeta = resourceMeta;
        _resourceLoader = resourceLoader;
        _resourceHandlerSupport = support;
        _url = url;
        _requestPath = requestPath;
        setLibraryName(resourceMeta.getLibraryName());
        setResourceName(resourceMeta.getResourceName());
        setContentType(contentType);
    }
    
    public ResourceLoader getResourceLoader()
    {
        return _resourceLoader;
    }    
    
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (couldResourceContainValueExpressions())
        {
            return new ValueExpressionFilterInputStream(
                    getResourceLoader().getResourceInputStream(_resourceMeta), getLibraryName(), getResourceName()); 
        }
        else
        {
            return getResourceLoader().getResourceInputStream(_resourceMeta);            
        }
    }
    
    private boolean couldResourceContainValueExpressions()
    {
        if (_resourceMeta.couldResourceContainValueExpressions())
        {
            return true;
        }
        else
        {
            //By default only css resource contain value expressions
            String contentType = getContentType();
    
            return ("text/css".equals(contentType));
        }
    }

    @Override
    public String getRequestPath()
    {
        if (_requestPath == null)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesServletMapping mapping = FacesServletMappingUtils.getCurrentRequestFacesServletMapping(context);
            if (mapping.isExactMapping())
            {
                // resources can't be exact, lets fallback to a generic one
                mapping = FacesServletMappingUtils.getGenericPrefixOrSuffixMapping(context);
            }
            
            String path = "";
            if (mapping.isExtensionMapping())
            {
                path = _resourceHandlerSupport.getResourceIdentifier() + '/' + 
                    getResourceName() + mapping.getExtension();
            }
            else
            {
                path = _resourceHandlerSupport.getResourceIdentifier() + '/' + getResourceName();
                path = (mapping.getPrefix() == null) ? path : mapping.getPrefix() + path;
            }

            String metadata = null;
            boolean useAmp = false;
            if (getLibraryName() != null)
            {
                metadata = "?ln=" + getLibraryName();
                path = path + metadata;
                useAmp = true;

                if (!context.isProjectStage(ProjectStage.Production)
                        && JSF_JS_RESOURCE_NAME.equals(getResourceName()) 
                        && JAKARTA_FACES_LIBRARY_NAME.equals(getLibraryName()))
                {
                    // append &stage=?? for all ProjectStages except Production
                    path = path + "&stage=" + context.getApplication().getProjectStage().toString();
                }
            }
            if (_resourceMeta.getLocalePrefix() != null)
            {
                path = path + (useAmp ? '&' : '?') + "loc=" + _resourceMeta.getLocalePrefix();
                useAmp = true;
            }
            if (_resourceMeta.getContractName() != null)
            {
                path = path + (useAmp ? '&' : '?') + "con=" + _resourceMeta.getContractName();
                useAmp = true;
            }
            _requestPath = context.getApplication().getViewHandler().getResourceURL(context, path);
        }
        return _requestPath;
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        if (facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext))
        {
            Map<String, String> headers = new HashMap<>(2, 1f);
            
            long lastModified = getLastModified(facesContext);
            
            // Here we have two cases: If the file could contain EL Expressions
            // the last modified time is the greatest value between application startup and
            // the value from file.
            if (this.couldResourceContainValueExpressions()
                    && lastModified < _resourceHandlerSupport.getStartupTime())
            {
                lastModified = _resourceHandlerSupport.getStartupTime();
            }            
            else if (_resourceMeta instanceof AliasResourceMetaImpl
                    && lastModified < _resourceHandlerSupport.getStartupTime())
            {
                // If the resource meta is aliased, the last modified time is the greatest 
                // value between application startup and the value from file.
                lastModified = _resourceHandlerSupport.getStartupTime();
            }

            if (lastModified >= 0)
            {
                headers.put("Last-Modified", ResourceLoaderUtils.formatDateHeader(lastModified));

                if (facesContext.isProjectStage(ProjectStage.Development))
                {
                    headers.put("Cache-Control", "no-cache");
                }
                else
                {
                    headers.put("Cache-Control", "max-age=" + (_resourceHandlerSupport.getMaxTimeExpires()/1000));
                }
            }
            
            return headers;
        }
        else
        {
            //No need to return headers 
            return Collections.emptyMap();
        }
    }

    @Override
    public URL getURL()
    {
        // For the default algorithm, it is safe to assume the resource
        // URL will not change over resource lifetime. See MYFACES-3458
        if (_url == null)
        {
            _url = getResourceLoader().getResourceURL(_resourceMeta);
        }
        return _url;
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        // RFC2616 says related to If-Modified-Since header the following:
        //
        // "... The If-Modified-Since request-header field is used with a method to 
        // make it conditional: if the requested variant has not been modified since 
        // the time specified in this field, an entity will not be returned from 
        // the server; instead, a 304 (not modified) response will be returned 
        // without any message-body..."
        // 
        // This method is called from ResourceHandlerImpl.handleResourceRequest and if
        // returns false send a 304 Not Modified response.
        
        String ifModifiedSinceString = context.getExternalContext().getRequestHeaderMap().get("If-Modified-Since");
        
        if (ifModifiedSinceString == null)
        {
            return true;
        }
        
        Long ifModifiedSince = ResourceLoaderUtils.parseDateHeader(ifModifiedSinceString);
        
        if (ifModifiedSince == null)
        {
            return true;
        }
        
        long lastModified = getLastModified(context);
        if (lastModified >= 0)
        {
            if (this.couldResourceContainValueExpressions()
                    && lastModified < _resourceHandlerSupport.getStartupTime())
            {
                lastModified = _resourceHandlerSupport.getStartupTime();
            }
            
            // If the lastModified date is lower or equal than ifModifiedSince,
            // the agent does not need to update.
            // Note the lastModified time is set at milisecond precision, but when 
            // the date is parsed and sent on ifModifiedSince, the exceding miliseconds
            // are trimmed. So, we have to compare trimming this from the calculated
            // lastModified time.
            if ( (lastModified-(lastModified % 1000)) <= ifModifiedSince)
            {
                return false;
            }
        }
        
        return true;
    }
    
    protected ResourceHandlerSupport getResourceHandlerSupport()
    {
        return _resourceHandlerSupport;
    }
    
    protected ResourceMeta getResourceMeta()
    {
        return _resourceMeta;
    }

    @Override
    public boolean isContractResource()
    {
        return _resourceMeta.getContractName() != null;
    }
    
    @Override
    public String getContractName()
    {
        return _resourceMeta.getContractName();
    }
    
    
    protected long getLastModified(FacesContext facesContext)
    {
        if (MyfacesConfig.getCurrentInstance(facesContext).isResourceCacheLastModified())
        {
            Long lastModified = _resourceMeta.getLastModified();
            if (lastModified == null)
            {
                try
                {
                    lastModified = ResourceLoaderUtils.getResourceLastModified(this.getURL());
                }
                catch (IOException e)
                {
                    lastModified = -1L;
                }

                _resourceMeta.setLastModified(lastModified);
            }

            return lastModified;
        }

        try
        {
            return ResourceLoaderUtils.getResourceLastModified(this.getURL());
        }
        catch (IOException e)
        {
            return -1;
        }
    }
}
