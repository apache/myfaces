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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;

/**
 * Renderer used by h:outputScript component
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.resource.Script")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class HtmlScriptRenderer extends Renderer implements
        ComponentSystemEventListener
{
    private static final Log log = LogFactory.getLog(HtmlScriptRenderer.class);
    
    private final static String RENDERED_RESOURCES_SET = HtmlScriptRenderer.class+".RENDERED_RESOURCES_SET"; 

    @Override
    public void processEvent(ComponentSystemEvent event)
    {
        UIComponent component = event.getComponent();
        String target = (String) component.getAttributes().get("target");
        if (target != null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getViewRoot().addComponentResource(facesContext,
                    component, target);
        }
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    /**
     * Return a set of already rendered resources by this renderer on the current
     * request. 
     * 
     * @param facesContext
     * @return
     */
    protected Set<String> getRenderedResources(FacesContext facesContext)
    {
        Set<String> map = (Set<String>) facesContext.getAttributes().get(RENDERED_RESOURCES_SET);
        if (map == null)
        {
            map = new HashSet<String>();
            facesContext.getAttributes().put(RENDERED_RESOURCES_SET,map);
        }
        return map;
    }
    
    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        if (facesContext == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");

        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get("name");
        boolean hasChildren = component.getChildCount() > 0;
        
        if (resourceName != null && (!"".equals(resourceName)) )
        {
            if (hasChildren)
            {
                log.info("Component with resourceName "+ resourceName + 
                        " and child components found. Child components will be ignored.");
            }
        }
        else
        {
            if (hasChildren)
            {
                // Children are encoded as usual. Usually the layout is
                // <script type="text/javascript">
                // ...... some javascript .......
                // </script>
                ResponseWriter writer = facesContext.getResponseWriter();
                writer.startElement(HTML.SCRIPT_ELEM, component);
                writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
                RendererUtils.renderChildren(facesContext, component);
                writer.endElement(HTML.SCRIPT_ELEM);
            }
            else
            {
                if (!facesContext.getApplication().getProjectStage().equals(
                        ProjectStage.Production))
                {
                    facesContext.addMessage(component.getClientId(), 
                            new FacesMessage("Component with no name and no body content, so nothing rendered."));
                }
            }            
        }
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        super.encodeEnd(facesContext, component); //check for NP
        
        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get("name");
        String libraryName = (String) componentAttributesMap.get("library");

        if (resourceName == null)
        {
            //log.warn("Trying to encode resource represented by component" + 
            //        component.getClientId() + " without resourceName."+
            //        " It will be silenty ignored.");
            return;
        }
        if ("".equals(resourceName))
        {
            return;
        }
        
        Set<String> renderedResources = getRenderedResources(facesContext);
                
        String resourceKey;
        Resource resource;
        if (libraryName == null)
        {
            resourceKey = resourceName;
            if (renderedResources.contains(resourceKey))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName);
        }
        else
        {
            resourceKey = libraryName+'/'+resourceName;
            if (renderedResources.contains(resourceKey))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName, libraryName);

        }
        
        if (resource == null)
        {
            //no resource found
            log.warn("Resource referenced by resourceName "+ resourceName +
                    (libraryName == null ? "" : " and libraryName " + libraryName) +
                    " not found in call to ResourceHandler.createResource."+
                    " It will be silenty ignored.");
            return;
        }
        else
        {
            // Rendering resource
            renderedResources.add(resourceKey);
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement(HTML.SCRIPT_ELEM, component);
            // We can't render the content type, because usually it returns "application/x-javascript"
            // and this is not compatible with IE. We should force render "text/javascript".
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT , null);
            writer.writeURIAttribute(HTML.SRC_ATTR, resource.getRequestPath(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }
}
