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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.application.FacesServletMapping;
import org.apache.myfaces.application.FacesServletMappingUtils;
import org.apache.myfaces.config.MyfacesConfig;

/**
 * A ResourceHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports javax.servlet, and uses a standard web.xml file.
 */
public class BaseResourceHandlerSupport extends ResourceHandlerSupport
{    
    private static final ResourceLoader[] EMPTY_RESOURCE_LOADERS = new ResourceLoader[]{}; 
    private static final ContractResourceLoader[] EMPTY_CONTRACT_RESOURCE_LOADERS = 
        new ContractResourceLoader[]{}; 
    
    private Long _startupTime;
    private Long _maxTimeExpires;
        
    public BaseResourceHandlerSupport()
    {
        _startupTime = System.currentTimeMillis();
    }
    
    @Override
    public ResourceLoader[] getResourceLoaders()
    {
        return EMPTY_RESOURCE_LOADERS;
    }
    
    @Override
    public ContractResourceLoader[] getContractResourceLoaders()
    {
        return EMPTY_CONTRACT_RESOURCE_LOADERS;
    }
    
    @Override
    public ResourceLoader[] getViewResourceLoaders()
    {
        return EMPTY_RESOURCE_LOADERS;
    }

    @Override
    public String calculateResourceBasePath(FacesContext facesContext)
    {        
        FacesServletMapping mapping = FacesServletMappingUtils.getCurrentRequestFacesServletMapping(facesContext);
        ExternalContext externalContext = facesContext.getExternalContext();      
        
        if (mapping != null)
        {
            String resourceBasePath = null;
            if (mapping.isExactMapping())
            {
                // this method is actually only used to determine if the current request is a resource request
                // as the resource can never be a exact mapping, lets ignore it
            }
            else if (mapping.isExtensionMapping())
            {
                // Mapping using a suffix. In this case we have to strip 
                // the suffix. If we have a url like:
                // http://localhost:8080/testjsf20/javax.faces.resource/imagen.jpg.jsf?ln=dojo
                // 
                // The servlet path is /javax.faces.resource/imagen.jpg.jsf
                //
                // For obtain the resource name we have to remove the .jsf suffix and 
                // the prefix ResourceHandler.RESOURCE_IDENTIFIER
                resourceBasePath = externalContext.getRequestServletPath();
                int stripPoint = resourceBasePath.lastIndexOf('.');
                if (stripPoint > 0)
                {
                    resourceBasePath = resourceBasePath.substring(0, stripPoint);
                }
            }
            else
            {
                // Mapping using prefix. In this case we have to strip 
                // the prefix used for mapping. If we have a url like:
                // http://localhost:8080/testjsf20/faces/javax.faces.resource/imagen.jpg?ln=dojo
                //
                // The servlet path is /faces
                // and the path info is /javax.faces.resource/imagen.jpg
                //
                // For obtain the resource name we have to remove the /faces prefix and 
                // then the prefix ResourceHandler.RESOURCE_IDENTIFIER
                resourceBasePath = externalContext.getRequestPathInfo();
            }
            return resourceBasePath;            
        }
        else
        {
            //If no mapping is detected, just return the
            //information follows the servlet path but before
            //the query string
            return externalContext.getRequestPathInfo();
        }
    }

    @Override
    public long getStartupTime()
    {
        return _startupTime;
    }
    
    @Override
    public long getMaxTimeExpires()
    {
        if (_maxTimeExpires == null)
        {
            _maxTimeExpires = MyfacesConfig.getCurrentInstance().getResourceMaxTimeExpires();
        }
        return _maxTimeExpires;
    }
}
