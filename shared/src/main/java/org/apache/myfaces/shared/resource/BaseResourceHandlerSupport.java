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
package org.apache.myfaces.shared.resource;

import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.application.FacesServletMapping;
import org.apache.myfaces.shared.application.FacesServletMappingUtils;
import org.apache.myfaces.shared.util.WebConfigParamUtils;

/**
 * A ResourceHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports javax.servlet, and uses a standard web.xml file.
 */
public class BaseResourceHandlerSupport extends ResourceHandlerSupport
{

    /**
     * Set the max time in miliseconds set on the "Expires" header for a resource rendered by 
     * the default ResourceHandler.
     * (default to one week in miliseconds or 604800000) 
     */
    @JSFWebConfigParam(since="2.0", defaultValue="604800000", group="resources", tags="performance")
    public static final String RESOURCE_MAX_TIME_EXPIRES = "org.apache.myfaces.RESOURCE_MAX_TIME_EXPIRES";

    /**
     * Identifies the FacesServlet mapping in the current request map.
     */
    private static final String CACHED_SERVLET_MAPPING =
        BaseResourceHandlerSupport.class.getName() + ".CACHED_SERVLET_MAPPING";
    
    private static final ResourceLoader[] EMPTY_RESOURCE_LOADERS = new ResourceLoader[]{}; 
    private static final ContractResourceLoader[] EMPTY_CONTRACT_RESOURCE_LOADERS = 
        new ContractResourceLoader[]{}; 
    
    private Long _startupTime;
    
    private Long _maxTimeExpires;
        
    public BaseResourceHandlerSupport()
    {
        _startupTime = System.currentTimeMillis();
    }
    
    public ResourceLoader[] getResourceLoaders()
    {
        return EMPTY_RESOURCE_LOADERS;
    }
    
    public ContractResourceLoader[] getContractResourceLoaders()
    {
        return EMPTY_CONTRACT_RESOURCE_LOADERS;
    }
    
    public ResourceLoader[] getViewResourceLoaders()
    {
        return EMPTY_RESOURCE_LOADERS;
    }

    public String calculateResourceBasePath(FacesContext facesContext)
    {        
        FacesServletMapping mapping = getFacesServletMapping(facesContext);
        ExternalContext externalContext = facesContext.getExternalContext();      
        
        if (mapping != null)
        {
            String resourceBasePath = null;
            if (mapping.isExtensionMapping())
            {
                // Mapping using a suffix. In this case we have to strip 
                // the suffix. If we have a url like:
                // http://localhost:8080/testjsf20/jakarta.faces.resource/imagen.jpg.jsf?ln=dojo
                // 
                // The servlet path is /jakarta.faces.resource/imagen.jpg.jsf
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
                // http://localhost:8080/testjsf20/faces/jakarta.faces.resource/imagen.jpg?ln=dojo
                //
                // The servlet path is /faces
                // and the path info is /jakarta.faces.resource/imagen.jpg
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

    public boolean isExtensionMapping()
    {
        FacesServletMapping mapping = getFacesServletMapping(
                FacesContext.getCurrentInstance());
        if (mapping != null)
        {
            if (mapping.isExtensionMapping())
            {
                return true;
            }
        }
        return false;
    }
    
    public String getMapping()
    {
        FacesServletMapping mapping = getFacesServletMapping(
                FacesContext.getCurrentInstance());
        if (mapping != null)
        {
            if (mapping.isExtensionMapping())
            {
                return mapping.getExtension();
            }
            else
            {
                return mapping.getPrefix();
            }
        }
        return "";
    }

    /**
     * Read the web.xml file that is in the classpath and parse its internals to
     * figure out how the FacesServlet is mapped for the current webapp.
     */
    protected FacesServletMapping getFacesServletMapping(FacesContext context)
    {
        Map<Object, Object> attributes = context.getAttributes();

        // Has the mapping already been determined during this request?
        FacesServletMapping mapping = (FacesServletMapping) attributes.get(CACHED_SERVLET_MAPPING);
        if (mapping == null)
        {
            ExternalContext externalContext = context.getExternalContext();
            mapping = FacesServletMappingUtils.calculateFacesServletMapping(
                    context, externalContext.getRequestServletPath(),
                    externalContext.getRequestPathInfo(),
                    false);

            attributes.put(CACHED_SERVLET_MAPPING, mapping);
        }
        return mapping;
    }
    
    /*
    protected static FacesServletMapping calculateFacesServletMapping(
        FacesContext facesContext, String servletPath, String pathInfo)
    {
        if (ExternalContextUtils.isPortlet(facesContext.getExternalContext()))
        {
            return calculateFacesServletMapping(servletPath, pathInfo);
        }
        else
        {
            Object context = facesContext.getExternalContext().getContext();
            if (context instanceof ServletContext)
            {
                if (pathInfo != null)
                {
                    // If there is a "extra path", it's definitely no extension mapping.
                    // Now we just have to determine the path which has been specified
                    // in the url-pattern, but that's easy as it's the same as the
                    // current servletPath. It doesn't even matter if "/*" has been used
                    // as in this case the servletPath is just an empty string according
                    // to the Servlet Specification (SRV 4.4).
                    return createMappingFromServletRegistration(facesContext, 
                            (ServletContext)context, servletPath, pathInfo);
                }
                else
                {
                    // In the case of extension mapping, no "extra path" is available.
                    // Still it's possible that prefix-based mapping has been used.
                    // Actually, if there was an exact match no "extra path"
                    // is available (e.g. if the url-pattern is "/faces/*"
                    // and the request-uri is "/context/faces").
                    int slashPos = servletPath.lastIndexOf('/');
                    int extensionPos = servletPath.lastIndexOf('.');
                    if (extensionPos > -1 && extensionPos > slashPos)
                    {
                        String extension = servletPath.substring(extensionPos);
                        return FacesServletMapping.createExtensionMapping(extension);
                    }
                    else
                    {
                        // There is no extension in the given servletPath and therefore
                        // we assume that it's an exact match using prefix-based mapping.
                        return createMappingFromServletRegistration(facesContext, 
                                (ServletContext)context, servletPath, pathInfo);
                    }
                }
            }
            else
            {
                return calculateFacesServletMapping(servletPath, pathInfo);
            }
        }
        //return null;
    }
    
    private static FacesServletMapping createMappingFromServletRegistration(FacesContext facesContext, 
            ServletContext servletContext, String servletPath, String pathInfo)
    {
        try
        {
            Map<String, ? extends ServletRegistration> map = servletContext.getServletRegistrations();
            if (map != null)
            {
                for (Map.Entry<String, ? extends ServletRegistration> entry : map.entrySet())
                {
                    try
                    {
                        Class servletClass = org.apache.myfaces.shared.util.ClassUtils.simpleClassForName(
                                (String)entry.getValue().getClassName());
                        if (FacesServlet.class.isAssignableFrom(servletClass) ||
                            DelegatedFacesServlet.class.isAssignableFrom(servletClass) ||
                            servletClass.getName().equals(
                                    WebXml.getWebXml(facesContext.getExternalContext()).getDelegateFacesServlet()))
                        {
                            Collection<String> mappings = entry.getValue().getMappings();
                            for (String mapping : mappings)
                            {
                                if (mapping.startsWith("*."))
                                {
                                    // extension mapping, use it.
                                    return FacesServletMapping.createExtensionMapping(mapping.substring(1));
                                }
                                else if (mapping.startsWith("/") && mapping.endsWith("/*"))
                                {
                                    // prefix mapping, use it.
                                    return FacesServletMapping.createPrefixMapping(
                                            mapping.substring(0, mapping.length()-2));
                                }
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        //No op
                    }
                }
                return FacesServletMapping.createPrefixMapping(servletPath);
            }
            else
            {
                return FacesServletMapping.createPrefixMapping(servletPath);
            }
        }
        catch(Exception ex)
        {
            return FacesServletMapping.createPrefixMapping(servletPath);
        }
    }
    */

    /**
     * Determines the mapping of the FacesServlet in the web.xml configuration
     * file. However, there is no need to actually parse this configuration file
     * as runtime information is sufficient.
     *
     * @param servletPath The servletPath of the current request
     * @param pathInfo    The pathInfo of the current request
     * @return the mapping of the FacesServlet in the web.xml configuration file
     */
    @Deprecated
    protected static FacesServletMapping calculateFacesServletMapping(
        String servletPath, String pathInfo)
    {
        if (pathInfo != null)
        {
            // If there is a "extra path", it's definitely no extension mapping.
            // Now we just have to determine the path which has been specified
            // in the url-pattern, but that's easy as it's the same as the
            // current servletPath. It doesn't even matter if "/*" has been used
            // as in this case the servletPath is just an empty string according
            // to the Servlet Specification (SRV 4.4).
            return FacesServletMapping.createPrefixMapping(servletPath);
        }
        else
        {
            // In the case of extension mapping, no "extra path" is available.
            // Still it's possible that prefix-based mapping has been used.
            // Actually, if there was an exact match no "extra path"
            // is available (e.g. if the url-pattern is "/faces/*"
            // and the request-uri is "/context/faces").
            int slashPos = servletPath.lastIndexOf('/');
            int extensionPos = servletPath.lastIndexOf('.');
            if (extensionPos > -1 && extensionPos > slashPos)
            {
                String extension = servletPath.substring(extensionPos);
                return FacesServletMapping.createExtensionMapping(extension);
            }
            else
            {
                // There is no extension in the given servletPath and therefore
                // we assume that it's an exact match using prefix-based mapping.
                return FacesServletMapping.createPrefixMapping(servletPath);
            }
        }
    }
    
    public long getStartupTime()
    {
        return _startupTime;
    }
    
    public long getMaxTimeExpires()
    {
        if (_maxTimeExpires == null)
        {
            _maxTimeExpires = WebConfigParamUtils.getLongInitParameter(
                    FacesContext.getCurrentInstance().getExternalContext(), 
                    RESOURCE_MAX_TIME_EXPIRES, 604800000L);
        }
        return _maxTimeExpires;
    }
}
