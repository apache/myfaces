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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.render.Renderer;
import jakarta.faces.view.Location;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.view.facelets.el.CompositeComponentELUtils;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

/**
 * Renderer used by h:outputScript component
 *
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "jakarta.faces.Output", type = "jakarta.faces.resource.Script")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class HtmlScriptRenderer extends Renderer implements ComponentSystemEventListener
{
    private static final Logger log = Logger.getLogger(HtmlScriptRenderer.class.getName());

    @Override
    public void processEvent(ComponentSystemEvent event)
    {
        if (event instanceof PostAddToViewEvent)
        {
            UIComponent component = event.getComponent();
            String target = (String) component.getAttributes().get(ComponentAttrs.TARGET_ATTR);
            if (target != null)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();

                Location location = (Location) component.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY);
                if (location != null)
                {
                    UIComponent ccParent
                            = CompositeComponentELUtils.getCompositeComponentBasedOnLocation(facesContext, location);
                    if (ccParent != null)
                    {
                        component.getAttributes().put(
                                CompositeComponentELUtils.CC_FIND_COMPONENT_EXPRESSION,
                                ComponentSupport.getFindComponentExpression(facesContext, ccParent));
                    }
                }

                facesContext.getViewRoot().addComponentResource(facesContext, component, target);
            }
        }

        if (event instanceof PreRenderViewEvent)
        {
            UIComponent component = event.getComponent();
            String target = (String) component.getAttributes().get(ComponentAttrs.TARGET_ATTR);
            if (target != null)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                UIComponent uiTarget = facesContext.getViewRoot().getFacet(target);
                if (uiTarget == null)
                {
                    throw new FacesException("Target for component not found");
                }
            }
        }
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(component, "component");

        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get(ComponentAttrs.NAME_ATTR);
        boolean hasChildren = component.getChildCount() > 0;

        if (resourceName != null && !resourceName.isEmpty())
        {
            if (hasChildren)
            {
                Level level = facesContext.isProjectStage(ProjectStage.Production)
                        ? Level.FINE
                        : Level.WARNING;
                if (log.isLoggable(level))
                {
                    log.log(level, "h:outputScript with resourceName " + resourceName + 
                            " and child components found. Child components will be ignored.");
                }
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
                HtmlRendererUtils.renderScriptType(facesContext, writer);
                RendererUtils.renderChildren(facesContext, component);
                writer.endElement(HTML.SCRIPT_ELEM);
            }
            else
            {
                Level level = facesContext.isProjectStage(ProjectStage.Production)
                        ? Level.FINE
                        : Level.WARNING;
                if (log.isLoggable(level))
                {
                    log.log(level, "h:outputScript with no name and no body content, so nothing rendered.");
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
        String resourceName = (String) componentAttributesMap.get(ComponentAttrs.NAME_ATTR);
        String libraryName = (String) componentAttributesMap.get(ComponentAttrs.LIBRARY_ATTR);

        if (resourceName == null || resourceName.isEmpty())
        {
            return;
        }

        String additionalQueryParams = null;
        int index = resourceName.indexOf('?');
        if (index >= 0)
        {
            additionalQueryParams = resourceName.substring(index + 1);
            resourceName = resourceName.substring(0, index);
        }

        Resource resource;
        if (libraryName == null)
        {
            if (ResourceUtils.isRenderedScript(facesContext, libraryName, resourceName))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName);
        }
        else
        {
            if (ResourceUtils.isRenderedScript(facesContext, libraryName, resourceName))
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
            log.warning("Resource referenced by resourceName " + resourceName +
                    (libraryName == null ? "" : " and libraryName " + libraryName) +
                    " not found in call to ResourceHandler.createResource." +
                    " It will be silenty ignored.");
            return;
        }
        else
        {
            if (ResourceUtils.isRenderedScript(facesContext, resource.getLibraryName(), resource.getResourceName()))
            {
                //Resource already founded
                return;
            }

            // Rendering resource
            ResourceUtils.markScriptAsRendered(facesContext, libraryName, resourceName);
            ResourceUtils.markScriptAsRendered(facesContext, resource.getLibraryName(), resource.getResourceName());
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement(HTML.SCRIPT_ELEM, component);
            HtmlRendererUtils.renderScriptType(facesContext, writer);
            String path = resource.getRequestPath();
            if (additionalQueryParams != null)
            {
                path = path + ((path.indexOf('?') >= 0) ? "&amp;" : "?") + additionalQueryParams;
            }
            writer.writeURIAttribute(HTML.SRC_ATTR, facesContext.getExternalContext().encodeResourceURL(path), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

}
