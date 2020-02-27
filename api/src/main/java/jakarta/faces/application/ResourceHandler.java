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
package jakarta.faces.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

/**
 * @since 2.0
 */
public abstract class ResourceHandler
{
    public static final String LOCALE_PREFIX = "jakarta.faces.resource.localePrefix";
    public static final String RESOURCE_EXCLUDES_DEFAULT_VALUE = ".class .jsp .jspx .properties .xhtml .groovy";
    
    /**
     * Space separated file extensions that will not be served by the default ResourceHandler implementation.
     */
    @JSFWebConfigParam(defaultValue=".class .jsp .jspx .properties .xhtml .groovy",since="2.0", group="resources")
    public static final String RESOURCE_EXCLUDES_PARAM_NAME = "jakarta.faces.RESOURCE_EXCLUDES";
    public static final String RESOURCE_IDENTIFIER = "/jakarta.faces.resource";
    
    /**
     * @since 2.2
     */
    public static final String RESOURCE_CONTRACT_XML = "jakarta.faces.contract.xml";
    
    /**
     * @since 2.2
     */
    public static final String WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME = "jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY";

    /**
     * @since 2.2
     */
    public static final String WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME = "jakarta.faces.WEBAPP_RESOURCES_DIRECTORY";

    /**
     * @since 2.3
     */
    public static final String JSF_SCRIPT_RESOURCE_NAME = "jsf.js";

    /**
     * @since 2.3
     */
    public static final String JSF_SCRIPT_LIBRARY_NAME = "jakarta.faces";
    
    private final static String MYFACES_JS_RESOURCE_NAME = "oamSubmit.js";
    private final static String MYFACES_JS_RESOURCE_NAME_UNCOMPRESSED = "oamSubmit-uncompressed.js";
    private final static String RENDERED_RESOURCES_SET = "org.apache.myfaces.RENDERED_RESOURCES_SET";
    private final static String MYFACES_LIBRARY_NAME = "org.apache.myfaces";
    private final static String RENDERED_MYFACES_JS = "org.apache.myfaces.RENDERED_MYFACES_JS";

    public abstract Resource createResource(String resourceName);
    
    public abstract Resource createResource(String resourceName, String libraryName);
    
    public abstract Resource createResource(String resourceName, String libraryName, String contentType);
    
    public abstract String getRendererTypeForResourceName(String resourceName);
    
    public abstract void handleResourceRequest(FacesContext context) throws IOException;
    
    public abstract boolean isResourceRequest(FacesContext context);
    
    public abstract  boolean libraryExists(String libraryName);
    
    /**
     * @since 2.2
     * @param resourceId
     * @return 
     */
    public Resource createResourceFromId(String resourceId)
    {
        return null;
    }
    
    /**
     * 
     * @since 2.2
     * @param context
     * @param resourceName
     * @return 
     */
    public ViewResource createViewResource(FacesContext context,
                                       String resourceName)
    {
        return context.getApplication().getResourceHandler().createResource(resourceName);
    }
    
    public boolean isResourceURL(java.lang.String url)
    {
        if (url == null)
        {
            throw new NullPointerException();
        }
        return url.contains(RESOURCE_IDENTIFIER);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param path
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViewResources(
            FacesContext facesContext, String path, ResourceVisitOption... options)
    {
        return getViewResources(facesContext, path, Integer.MAX_VALUE, options);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param path
     * @param maxDepth
     * @param options
     * @return 
     */
    public Stream<java.lang.String> getViewResources(FacesContext facesContext, 
            String path, int maxDepth, ResourceVisitOption... options)
    {
        return null;
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param resourceName
     * @param libraryName
     * @return 
     */
    public boolean isResourceRendered(FacesContext facesContext, String resourceName, String libraryName)
    {
        return getRenderedResources(facesContext).containsKey(
                libraryName != null ? libraryName+'/'+resourceName : resourceName);
    }
    
    /**
     * @since 2.3
     * @param facesContext
     * @param resourceName
     * @param libraryName 
     */
    public void markResourceRendered(FacesContext facesContext, String resourceName, String libraryName)
    {
        getRenderedResources(facesContext).put(
                libraryName != null ? libraryName+'/'+resourceName : resourceName, Boolean.TRUE);
        if (ResourceHandler.JSF_SCRIPT_LIBRARY_NAME.equals(libraryName) &&
            ResourceHandler.JSF_SCRIPT_RESOURCE_NAME.equals(resourceName))
        {
            // If we are calling this method, it is expected myfaces core is being used as runtime and note
            // oamSubmit script is included inside jsf.js, so mark this one too.
            getRenderedResources(facesContext).put(
                    MYFACES_LIBRARY_NAME+'/'+MYFACES_JS_RESOURCE_NAME, Boolean.TRUE);
        }
    }
    
    /**
     * Return a set of already rendered resources by this renderer on the current
     * request. 
     * 
     * @param facesContext
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> getRenderedResources(FacesContext facesContext)
    {
        Map<String, Boolean> map = (Map<String, Boolean>) facesContext.getViewRoot().getTransientStateHelper()
                .getTransient(RENDERED_RESOURCES_SET);
        if (map == null)
        {
            map = new HashMap<String, Boolean>();
            facesContext.getViewRoot().getTransientStateHelper().putTransient(RENDERED_RESOURCES_SET,map);
        }
        return map;
    }

}
