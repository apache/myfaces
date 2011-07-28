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
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.*;
import javax.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared.renderkit.JSFAttr;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.view.facelets.PostBuildComponentTreeOnRestoreViewEvent;

/**
 * Renderer used by h:outputScript component
 *
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.resource.Script")
@ListenersFor({
@ListenerFor(systemEventClass = PostAddToViewEvent.class),
@ListenerFor(systemEventClass = PostBuildComponentTreeOnRestoreViewEvent.class)
})
public class HtmlScriptRenderer extends Renderer implements ComponentSystemEventListener {
    //private static final Log log = LogFactory.getLog(HtmlScriptRenderer.class);
    private static final Logger log = Logger.getLogger(HtmlScriptRenderer.class.getName());

    public void processEvent(ComponentSystemEvent event) {
        if (event instanceof PostAddToViewEvent) {
            UIComponent component = event.getComponent();
            String target = (String) component.getAttributes().get(JSFAttr.TARGET_ATTR);
            if (target != null) {
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
                        component, target);
            }
        }

        if (event instanceof PreRenderViewEvent)

        {
            //TODO target check here
            UIComponent component = event.getComponent();
            String target = (String) component.getAttributes().get(JSFAttr.TARGET_ATTR);
            if (target != null) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                UIComponent uiTarget = facesContext.getViewRoot().getFacet(target);
                if (uiTarget == null) {
                    throw new FacesException("Target for component not found");
                }
            }
        }
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
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component)
            throws IOException {
        if (facesContext == null)
            throw new NullPointerException("context");
        if (component == null)
            throw new NullPointerException("component");

        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
        boolean hasChildren = component.getChildCount() > 0;

        if (resourceName != null && (!"".equals(resourceName))) {
            if (hasChildren) {
                log.info("Component with resourceName " + resourceName +
                        " and child components found. Child components will be ignored.");
            }
        } else {
            if (hasChildren) {
                // Children are encoded as usual. Usually the layout is
                // <script type="text/javascript">
                // ...... some javascript .......
                // </script>
                ResponseWriter writer = facesContext.getResponseWriter();
                writer.startElement(HTML.SCRIPT_ELEM, component);
                writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
                RendererUtils.renderChildren(facesContext, component);
                writer.endElement(HTML.SCRIPT_ELEM);
            } else {
                if (!facesContext.getApplication().getProjectStage().equals(
                        ProjectStage.Production)) {
                    facesContext.addMessage(component.getClientId(),
                            new FacesMessage("Component with no name and no body content, so nothing rendered."));
                }
            }
        }
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException {
        super.encodeEnd(facesContext, component); //check for NP

        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
        String libraryName = (String) componentAttributesMap.get(JSFAttr.LIBRARY_ATTR);

        if (resourceName == null) {
            //log.warn("Trying to encode resource represented by component" +
            //        component.getClientId() + " without resourceName."+
            //        " It will be silenty ignored.");
            return;
        }
        if ("".equals(resourceName)) {
            return;
        }

        Resource resource;
        if (libraryName == null) {
            if (ResourceUtils.isRenderedScript(facesContext, libraryName, resourceName)) {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName);
        } else {
            if (ResourceUtils.isRenderedScript(facesContext, libraryName, resourceName)) {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName, libraryName);

        }

        if (resource == null) {
            //no resource found
            log.warning("Resource referenced by resourceName " + resourceName +
                    (libraryName == null ? "" : " and libraryName " + libraryName) +
                    " not found in call to ResourceHandler.createResource." +
                    " It will be silenty ignored.");
            return;
        } else {
            // Rendering resource
            ResourceUtils.markScriptAsRendered(facesContext, libraryName, resourceName);
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement(HTML.SCRIPT_ELEM, component);
// We can't render the content type, because usually it returns "application/x-javascript"
// and this is not compatible with IE. We should force render "text/javascript".
            writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
            writer.writeURIAttribute(HTML.SRC_ATTR, resource.getRequestPath(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

    /*
    private boolean _initialStateMarked;

    public void clearInitialState() {
        _initialStateMarked = false;
    }

    public boolean initialStateMarked() {
        return _initialStateMarked;
    }

    public void markInitialState() {
        _initialStateMarked = true;
    }

    public boolean isTransient() {
        return false;
    }

    public void restoreState(FacesContext context, Object state) {
    }

    public Object saveState(FacesContext context) {
        return null;
    }

    public void setTransient(boolean newTransientValue) {
    }
    */
}
