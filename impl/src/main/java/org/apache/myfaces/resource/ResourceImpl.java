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

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.faces.application.ResourceHandler;

import org.apache.myfaces.application.ResourceHandlerSupport;

/**
 * Default implementation for resources
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ResourceImpl extends Resource
{

    private ResourceMeta _resourceMeta;
    private ResourceLoader _resourceLoader;
    private ResourceHandlerSupport _resourceHandlerSupport;
    
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
    
    public ResourceLoader getResourceLoader()
    {
        return _resourceLoader;
    }    
    
    @Override
    public InputStream getInputStream()
    {
        return getResourceLoader().getResourceInputStream(_resourceMeta);
    }

    @Override
    public String getRequestPath()
    {
        String path;
        if (_resourceHandlerSupport.isExtensionMapping())
        {
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/' + 
                getResourceName() + _resourceHandlerSupport.getMapping();
        }
        else
        {
            path = _resourceHandlerSupport.getMapping() + 
                ResourceHandler.RESOURCE_IDENTIFIER + '/' + getResourceName();
        }
 
        String metadata = null;
        if (getLibraryName() != null)
        {
            metadata = "?ln=" + getLibraryName();
        }
        
        path = path + metadata;
        
        return FacesContext.getCurrentInstance().getApplication().
            getViewHandler().getResourceURL(
                    FacesContext.getCurrentInstance(), path);
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        // TODO: Read the HTTP documentation to see how we can enhance this
        //part. For now, use always an empty map. 
        return Collections.emptyMap();
    }

    @Override
    public URL getURL()
    {
        return getResourceLoader().getResourceURL(_resourceMeta);
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        // TODO: When and How we can return safely false?
        return true;
    }

}
