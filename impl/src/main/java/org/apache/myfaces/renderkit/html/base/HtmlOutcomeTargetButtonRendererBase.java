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
package org.apache.myfaces.renderkit.html.base;

import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutcomeTarget;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlOutcomeTargetButton;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.CommonHtmlAttributes;

import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

public class HtmlOutcomeTargetButtonRendererBase extends HtmlRenderer
{
    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        super.encodeBegin(facesContext, uiComponent); //check for NP

        String clientId = uiComponent.getClientId(facesContext);

        ResponseWriter writer = facesContext.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }

        writer.startElement(HTML.INPUT_ELEM, uiComponent);

        writer.writeAttribute(HTML.ID_ATTR, clientId, ComponentAttrs.ID_ATTR);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, ComponentAttrs.ID_ATTR);

        String image = getImage(uiComponent);
        ExternalContext externalContext = facesContext.getExternalContext();

        if (image != null)
        {
            // type="image"
            writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_IMAGE, ComponentAttrs.TYPE_ATTR);
            String src = facesContext.getApplication().getViewHandler().getResourceURL(facesContext, image);
            writer.writeURIAttribute(HTML.SRC_ATTR, externalContext.encodeResourceURL(src), ComponentAttrs.IMAGE_ATTR);
        }
        else
        {
            // type="button"
            writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_BUTTON, ComponentAttrs.TYPE_ATTR);
            Object value = RendererUtils.getStringValue(facesContext, uiComponent);
            if (value != null)
            {
                writer.writeAttribute(HTML.VALUE_ATTR, value, ComponentAttrs.VALUE_ATTR);
            }
        }

        String outcomeTargetHref = HtmlRendererUtils.getOutcomeTargetHref(
                    facesContext, (UIOutcomeTarget) uiComponent);

        if (HtmlRendererUtils.isDisabled(uiComponent) || outcomeTargetHref == null)
        {
            // disable the button - if disabled is true or no fitting NavigationCase was found
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.DISABLED_ATTR, HTML.DISABLED_ATTR, true);
        }
        else
        {
            // render onClick attribute
            String href = facesContext.getExternalContext().encodeResourceURL(outcomeTargetHref);

            String commandOnClick = (String) uiComponent.getAttributes().get(HTML.ONCLICK_ATTR);
            String navigationJavaScript = "window.location.href = '" + href + '\'';

            if (behaviors != null && !behaviors.isEmpty())
            {
                HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, 
                        uiComponent, ClientBehaviorEvents.CLICK, null, behaviors, HTML.ONCLICK_ATTR, 
                        commandOnClick, navigationJavaScript);
            }
            else
            {
                StringBuilder onClick = new StringBuilder();
    
                if (commandOnClick != null)
                {
                    onClick.append(commandOnClick);
                    onClick.append(';');
                }
                onClick.append(navigationJavaScript);
    
                writer.writeAttribute(HTML.ONCLICK_ATTR, onClick.toString(), null);
            }
        }

        if (isCommonPropertiesOptimizationEnabled(facesContext))
        {
            long commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(uiComponent);
            
            if (behaviors != null && !behaviors.isEmpty() && uiComponent instanceof ClientBehaviorHolder)
            {
                HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnclick(
                        facesContext, writer, uiComponent, behaviors);
                HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                        facesContext, writer, uiComponent, behaviors);
            }
            else
            {
                CommonHtmlAttributesUtil.renderEventPropertiesWithoutOnclick(
                        writer, commonPropertiesMarked, uiComponent);
                CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer, commonPropertiesMarked, uiComponent);
            }
            
            CommonHtmlAttributesUtil.renderCommonFieldPassthroughPropertiesWithoutDisabledAndEvents(
                    writer, commonPropertiesMarked, uiComponent);
            if ((commonPropertiesMarked & CommonHtmlAttributes.ALT) != 0)
            {
                HtmlRendererUtils.renderHTMLStringAttribute(writer, uiComponent,
                        HTML.ALT_ATTR, HTML.ALT_ATTR);
            }
        }
        else
        {
            if (uiComponent instanceof ClientBehaviorHolder)
            {
                HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnclick(
                        facesContext, writer, uiComponent, behaviors);
                HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                        facesContext, writer, uiComponent, behaviors);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent,
                        HTML.EVENT_HANDLER_ATTRIBUTES_WITHOUT_ONCLICK);
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent,
                        HTML.COMMON_FIELD_EVENT_ATTRIBUTES_WITHOUT_ONSELECT_AND_ONCHANGE);
    
            }
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent,
                    HTML.COMMON_FIELD_PASSTROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
            HtmlRendererUtils.renderHTMLStringAttribute(writer, uiComponent,
                    HTML.ALT_ATTR, HTML.ALT_ATTR);
        }

        writer.flush();
    }

    private String getImage(UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlOutcomeTargetButton)
        {
            return ((HtmlOutcomeTargetButton) uiComponent).getImage();
        }
        return (String) uiComponent.getAttributes().get(ComponentAttrs.IMAGE_ATTR);
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.renderChildren(facesContext, component);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        super.encodeEnd(facesContext, component); //check for NP

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.endElement(HTML.INPUT_ELEM);
    }
}
