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
package org.apache.myfaces.view.facelets.component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.base.HtmlRenderer;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.view.facelets.LocationAwareFacesException;

/**
 *
 * @author Leonardo Uribe
 */
@JSFRenderer(
    renderKitId = "HTML_BASIC",
    family = "jakarta.faces.Panel",
    type = "jakarta.faces.passthrough.Element")
public class JsfElementRenderer extends HtmlRenderer
{

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }
    
    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        // Check for npe
        super.decode(context, component);
        
        ClientBehaviorRendererUtils.decodeClientBehaviors(context, component);
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        String elementName = (String) 
            component.getPassThroughAttributes().get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);

        if (elementName == null)
        {
            throw new LocationAwareFacesException("jsf:element with clientId"
                + component.getClientId(facesContext) + " requires 'elementName' passthrough attribute", component);
        }
        JsfElement jsfElement = (JsfElement) component;
        Map<String, List<ClientBehavior>> behaviors = jsfElement.getClientBehaviors();
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
        }
        
        writer.startElement(elementName, component);

        if (!behaviors.isEmpty())
        {
            HtmlRendererUtils.writeIdAndName(writer, component, facesContext);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
        }
        
        // Write in the optimized way, because this is a renderer for internal use only
        long commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
        if (behaviors.isEmpty())
        {
            CommonHtmlAttributesUtil.renderEventProperties(writer, commonPropertiesMarked, component);
            CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
            CommonHtmlAttributesUtil.renderChangeSelectEventProperties(writer, commonPropertiesMarked, component);
        }
        else
        {
            long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(component);
            CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                   commonPropertiesMarked, commonEventsMarked, component, behaviors);
            CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlers(facesContext, writer, 
                   commonPropertiesMarked, commonEventsMarked, component, 
                   component.getClientId(facesContext), behaviors);
        }
        CommonHtmlAttributesUtil.renderStyleProperties(writer, commonPropertiesMarked, component);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONLOAD_ATTR, component,
                ClientBehaviorEvents.LOAD, behaviors, HTML.ONLOAD_ATTR);
        HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONUNLOAD_ATTR, component,
                ClientBehaviorEvents.UNLOAD, behaviors, HTML.ONUNLOAD_ATTR);
        
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        RendererUtils.renderChildren(facesContext, component);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        String elementName = (String) component.getPassThroughAttributes().get(
            Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
        writer.endElement(elementName);
    }
    
    @Override
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }
    
    @Override
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }
}
