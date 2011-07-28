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
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared.renderkit.JSFAttr;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.view.facelets.PostBuildComponentTreeOnRestoreViewEvent;

/**
 * Renderer used by h:outputStylesheet component
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.resource.Stylesheet")
@ListenersFor({
@ListenerFor(systemEventClass = PostAddToViewEvent.class),
@ListenerFor(systemEventClass = PostBuildComponentTreeOnRestoreViewEvent.class)
})
public class HtmlStylesheetRenderer extends Renderer implements
    ComponentSystemEventListener
{
    //private static final Log log = LogFactory.getLog(HtmlStylesheetRenderer.class);
    private static final Logger log = Logger.getLogger(HtmlStylesheetRenderer.class.getName());
    
    public void processEvent(ComponentSystemEvent event)
    {
        UIComponent component = event.getComponent();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        //if (component.getId() != null)
        //{
        //    UniqueIdVendor uiv = findParentUniqueIdVendor(component);
        //
        //    if ( (!(uiv instanceof UIViewRoot)) && component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        //    {
        //        // The id was set using the closest UniqueIdVendor, but since this one
        //        // will be relocated, we need to assign an id from the current root.
        //        // otherwise a duplicate id exception could happen.
        //        component.setId(facesContext.getViewRoot().createUniqueId(facesContext, null));
        //    }
        //}
        
        facesContext.getViewRoot().addComponentResource(facesContext,
                    component, "head");
    }
    
    //private static UniqueIdVendor findParentUniqueIdVendor(UIComponent component)
    //{
    //    UIComponent parent = component.getParent();
    //
    //    while (parent != null)
    //    {
    //        if (parent instanceof UniqueIdVendor)
    //        {
    //            return (UniqueIdVendor) parent;
    //        }
    //        parent = parent.getParent();
    //    }
    //    return null;
    //}

    @Override
    public boolean getRendersChildren()
    {
        return true;
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
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
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
                ResponseWriter writer = facesContext.getResponseWriter();
                writer.startElement(HTML.STYLE_ELEM, component);
                writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, null);
                RendererUtils.renderChildren(facesContext, component);
                writer.endElement(HTML.STYLE_ELEM);
            }
            else
            {
                if (!facesContext.isProjectStage(ProjectStage.Production))
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
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
        String libraryName = (String) componentAttributesMap.get(JSFAttr.LIBRARY_ATTR);

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
        
        Resource resource;
        if (libraryName == null)
        {
            if (ResourceUtils.isRenderedStylesheet(facesContext, libraryName, resourceName))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName);
        }
        else
        {
            if (ResourceUtils.isRenderedStylesheet(facesContext, libraryName, resourceName))
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
            log.warning("Resource referenced by resourceName "+ resourceName +
                    (libraryName == null ? "" : " and libraryName " + libraryName) +
                    " not found in call to ResourceHandler.createResource."+
                    " It will be silenty ignored.");
            return;
        }
        else
        {
            // Rendering resource
            ResourceUtils.markStylesheetAsRendered(facesContext, libraryName, resourceName);
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement(HTML.LINK_ELEM, component);
            writer.writeAttribute(HTML.REL_ATTR, HTML.STYLESHEET_VALUE,null );
            writer.writeAttribute("media", "screen",null );            
            writer.writeAttribute(HTML.TYPE_ATTR, 
                    (resource.getContentType() == null ? HTML.STYLE_TYPE_TEXT_CSS
                            : resource.getContentType()) , null);
            writer.writeURIAttribute(HTML.HREF_ATTR, resource.getRequestPath(), null);
            writer.endElement(HTML.LINK_ELEM);
        }
    }

    /*
    private boolean _initialStateMarked;
    
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    public void markInitialState()
    {
        _initialStateMarked = true;
    }

    public boolean isTransient()
    {
        return false;
    }

    public void restoreState(FacesContext context, Object state)
    {
    }

    public Object saveState(FacesContext context)
    {
        return null;
    }

    public void setTransient(boolean newTransientValue)
    {
    }*/
}
