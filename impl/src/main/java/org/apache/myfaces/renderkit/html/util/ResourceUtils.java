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
package org.apache.myfaces.renderkit.html.util;

import java.io.IOException;
import jakarta.faces.FacesWrapper;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.resource.ContractResource;

public class ResourceUtils
{
    public final static String MYFACES_LIBRARY_NAME = "org.apache.myfaces";

    public final static String JSF_MYFACES_JSFJS_MINIMAL = "minimal";
    public final static String JSF_MYFACES_JSFJS_NORMAL = "normal";
    
    public final static String JSF_UNCOMPRESSED_JS_RESOURCE_NAME = "jsf-uncompressed.js";
    public final static String JSF_UNCOMPRESSED_FULL_JS_RESOURCE_NAME = "jsf-uncompressed-full.js";
    public final static String JSF_MINIMAL_JS_RESOURCE_NAME = "jsf-minimal.js";
    public final static String JSF_MYFACES_JSFJS_I18N = "jsf-i18n.js";

    private final static String RENDERED_JSF_JS = "org.apache.myfaces.RENDERED_JSF_JS";

    public static final String JAVAX_FACES_OUTPUT_COMPONENT_TYPE = "jakarta.faces.Output";
    public static final String JAVAX_FACES_TEXT_RENDERER_TYPE = "jakarta.faces.Text";
    public static final String DEFAULT_SCRIPT_RENDERER_TYPE = "jakarta.faces.resource.Script";
    public static final String DEFAULT_STYLESHEET_RENDERER_TYPE = "jakarta.faces.resource.Stylesheet";

    public static void markScriptAsRendered(FacesContext facesContext, String libraryName, String resourceName)
    {
        facesContext.getApplication().getResourceHandler().markResourceRendered(
                facesContext, resourceName, libraryName);
    }
    
    public static void markStylesheetAsRendered(FacesContext facesContext, String libraryName, String resourceName)
    {
        facesContext.getApplication().getResourceHandler().markResourceRendered(
                facesContext, resourceName, libraryName);
    }
    
    public static boolean isRenderedScript(FacesContext facesContext, String libraryName, String resourceName)
    {
        return facesContext.getApplication().getResourceHandler().isResourceRendered(
                facesContext, resourceName, libraryName);
    }
    
    public static boolean isRenderedStylesheet(FacesContext facesContext, String libraryName, String resourceName)
    {
        return facesContext.getApplication().getResourceHandler().isResourceRendered(
                facesContext, resourceName, libraryName);
    }
    
    public static void writeScriptInline(FacesContext facesContext, ResponseWriter writer, String libraryName, 
            String resourceName) throws IOException
    {
        if (!ResourceUtils.isRenderedScript(facesContext, libraryName, resourceName))
        {
            //Fast shortcut, don't create component instance and do what HtmlScriptRenderer do.
            Resource resource = facesContext.getApplication().getResourceHandler().createResource(
                    resourceName, libraryName);
            markScriptAsRendered(facesContext, libraryName, resourceName);
            writer.startElement(HTML.SCRIPT_ELEM, null);
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT , null);
            writer.writeURIAttribute(HTML.SRC_ATTR, resource.getRequestPath(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }
    
    public static void renderDefaultJsfJsInlineIfNecessary(FacesContext facesContext, ResponseWriter writer) 
        throws IOException
    {
        if (facesContext.getAttributes().containsKey(RENDERED_JSF_JS))
        {
            return;
        }
        
        // Check first if we have lucky, we are using myfaces and the script has
        // been previously rendered
        if (isRenderedScript(facesContext, ResourceHandler.JSF_SCRIPT_LIBRARY_NAME,
                ResourceHandler.JSF_SCRIPT_RESOURCE_NAME))
        {
            facesContext.getAttributes().put(RENDERED_JSF_JS, Boolean.TRUE);
            return;
        }

        // Check if this is an ajax request. If so, we don't need to include it, because that was
        // already done and in the worst case, jsf script was already loaded on the page.
        PartialViewContext partialViewContext = facesContext.getPartialViewContext();
        if (partialViewContext != null && 
                (partialViewContext.isPartialRequest() || partialViewContext.isAjaxRequest()))
        {
            return;
        }

        //Fast shortcut, don't create component instance and do what HtmlScriptRenderer do.
        Resource resource = facesContext.getApplication().getResourceHandler().createResource(
                ResourceHandler.JSF_SCRIPT_RESOURCE_NAME, ResourceHandler.JSF_SCRIPT_LIBRARY_NAME);
        markScriptAsRendered(facesContext, ResourceHandler.JSF_SCRIPT_LIBRARY_NAME,
                ResourceHandler.JSF_SCRIPT_RESOURCE_NAME);
        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
        writer.writeURIAttribute(HTML.SRC_ATTR, resource.getRequestPath(), null);
        writer.endElement(HTML.SCRIPT_ELEM);

        //mark as rendered
        facesContext.getAttributes().put(RENDERED_JSF_JS, Boolean.TRUE);
    }

    public static String getContractName(Resource resource)
    {
        while (resource != null)
        {
            if (resource instanceof ContractResource)
            {
                return ((ContractResource)resource).getContractName();
            }
            else if (resource instanceof FacesWrapper)
            {
                resource = (Resource) ((FacesWrapper)resource).getWrapped();
            }
            else
            {
                resource = null;
            }
        }
        return null;
    }
}
