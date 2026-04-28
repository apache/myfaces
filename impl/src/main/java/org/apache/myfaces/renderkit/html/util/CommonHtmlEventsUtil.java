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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialResponseWriter;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.CommonHtmlEvents;
import org.apache.myfaces.core.api.shared.CommonHtmlAttributes;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.util.lang.StringUtils;

public class CommonHtmlEventsUtil
{
    private static final String CSP_DEFERRED_BEHAVIOR_SCRIPTS_KEY =
            CommonHtmlEventsUtil.class.getName() + ".DEFERRED_CSP_BEHAVIOR_SCRIPTS";

    public static long getMarkedEvents(UIComponent component)
    {
        return CommonHtmlEvents.getMarkedEvents(component);
    }

    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String sourceId, String eventName,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName) throws IOException
    {
        return renderBehaviorizedAttribute(facesContext, writer,
                componentProperty, component, sourceId, eventName, null,
                clientBehaviors, htmlAttrName, (String) component
                        .getAttributes().get(componentProperty));
    }

    /**
     * Render an attribute taking into account the passed event,
     * the component property and the passed attribute value for the component
     * property. The event will be rendered on the selected htmlAttrName.
     *
     * @param facesContext
     * @param writer
     * @param componentProperty
     * @param component
     * @param eventName
     * @param clientBehaviors
     * @param htmlAttrName
     * @param attributeValue
     * @return
     * @throws IOException
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String eventName,
            Collection<ClientBehaviorContext.Parameter> eventParameters,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue) throws IOException
    {
        return renderBehaviorizedAttribute(facesContext, writer,
                componentProperty, component,
                null, eventName,
                eventParameters, clientBehaviors, htmlAttrName, attributeValue);
    }

    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String sourceId, String eventName,
            Collection<ClientBehaviorContext.Parameter> eventParameters,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue) throws IOException
    {

        List<ClientBehavior> cbl = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;
        if (cbl == null || cbl.isEmpty())
        {
            return HtmlRendererUtils.renderHTMLAttribute(writer, componentProperty,
                    htmlAttrName, attributeValue);
        }

        String targetElementId = sourceId != null ? sourceId : component.getClientId(facesContext);

        if (cbl.size() > 1 || (cbl.size() == 1 && attributeValue != null))
        {
            String chain = ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                    component, sourceId, eventName,
                    eventParameters, clientBehaviors, attributeValue,
                    RendererUtils.EMPTY_STRING);
            if (StringUtils.isNotBlank(chain)
                    && deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, chain))
            {
                return true;
            }
            return HtmlRendererUtils.renderHTMLAttribute(writer, componentProperty, htmlAttrName, chain);
        }
        else
        {
            //Only 1 behavior and attrValue == null, so just render it directly
            ClientBehaviorContext ctx = ClientBehaviorContext.createClientBehaviorContext(
                                    facesContext, component, eventName,sourceId, eventParameters);
            String script = cbl.get(0).getScript(ctx);
            if (deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, script))
            {
                return true;
            }
            return HtmlRendererUtils.renderHTMLAttribute(
                    writer,
                    componentProperty,
                    htmlAttrName,
                    script);
        }
    }

    /**
     * When JSF CSP support is enabled ({@link jakarta.faces.application.ResourceHandler#ENABLE_CSP_NONCE_PARAM_NAME})
     * or {@link jakarta.faces.application.ResourceHandler#getCurrentNonce} is non-null, inline {@code on*} event
     * handler attributes must not carry behavior scripts. This method enqueues the same script to be emitted
     * later (nonce-bearing {@code <script>} on a full response, or {@code <eval>} on Ajax) that assigns the
     * handler on the element via the DOM ({@code element.onclick = function(event) { ... }}).
     *
     * @return {@code true} if the script was deferred and must not be rendered as an attribute
     */
    public static boolean deferClientBehaviorScriptIfCspNonceActive(
            FacesContext facesContext,
            String targetElementId,
            String htmlAttrName,
            String scriptBody)
    {
        if (!isCspDeferClientBehaviorInlineHandlers(facesContext))
        {
            return false;
        }
        if (StringUtils.isBlank(scriptBody))
        {
            return false;
        }
        String escapedId = escapeJsStringForSingleQuotes(targetElementId);
        StringBuilder sb = new StringBuilder(64 + scriptBody.length() + escapedId.length());
        sb.append("(function(el){if(!el)return;el.");
        sb.append(htmlAttrName);
        sb.append("=function(event){");
        sb.append(scriptBody);
        sb.append("};})(document.getElementById('");
        sb.append(escapedId);
        sb.append("'));");

        List<String> queue = (List<String>) facesContext.getAttributes().computeIfAbsent(
                CSP_DEFERRED_BEHAVIOR_SCRIPTS_KEY, k -> new ArrayList<>());
        queue.add(sb.toString());
        return true;
    }

    /**
     * @return true when inline {@code on*} attributes must not carry client behavior scripts: JSF CSP is enabled
     *         in configuration, or the resource handler already exposes a view nonce.
     */
    public static boolean isCspDeferClientBehaviorInlineHandlers(FacesContext facesContext)
    {
        MyfacesConfig cfg = MyfacesConfig.getCurrentInstance(facesContext);
        if (cfg != null && cfg.isCspEnabled())
        {
            return true;
        }
        return facesContext.getApplication().getResourceHandler().getCurrentNonce(facesContext) != null;
    }

    private static String escapeJsStringForSingleQuotes(String s)
    {
        if (s == null)
        {
            return "";
        }
        StringBuilder out = null;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c == '\\' || c == '\'')
            {
                if (out == null)
                {
                    out = new StringBuilder(s.length() + 8);
                    out.append(s, 0, i);
                }
                out.append('\\');
            }
            if (out != null)
            {
                out.append(c);
            }
        }
        return out == null ? s : out.toString();
    }

    /**
     * Writes all scripts enqueued by {@link #deferClientBehaviorScriptIfCspNonceActive} in one
     * nonce-bearing {@code <script>} block. Safe to call multiple times; only the first flush emits output.
     */
    public static void flushDeferredCspBehaviorScripts(FacesContext facesContext, ResponseWriter writer)
            throws IOException
    {
        List<String> queue = (List<String>) facesContext.getAttributes().remove(CSP_DEFERRED_BEHAVIOR_SCRIPTS_KEY);
        if (queue == null || queue.isEmpty())
        {
            return;
        }
        if (writer instanceof PartialResponseWriter prw)
        {
            prw.startEval();
            for (int i = 0, n = queue.size(); i < n; i++)
            {
                prw.write(queue.get(i));
            }
            prw.endEval();
        }
        else
        {
            writer.startElement(HTML.SCRIPT_ELEM, null);
            HtmlRendererUtils.renderScriptType(facesContext, writer);
            HtmlRendererUtils.renderNonce(facesContext, writer);
            for (int i = 0, n = queue.size(); i < n; i++)
            {
                writer.write(queue.get(i));
            }
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

    // CHECKSTYLE:OFF (ParameterNumber — mirrors jakarta.faces renderBehaviorizedAttribute overloads)
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String sourceId, String eventName,
            Collection<ClientBehaviorContext.Parameter> eventParameters,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException
    {

        List<ClientBehavior> cbl = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;
        String targetElementId = sourceId != null ? sourceId : component.getClientId(facesContext);
        if (((cbl != null) ? cbl.size() : 0) + (attributeValue != null ? 1 : 0)
                + (serverSideScript != null ? 1 : 0) <= 1)
        {
            if (cbl == null || cbl.isEmpty())
            {
                if (attributeValue != null)
                {
                    return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                            attributeValue);
                }
                else
                {
                    return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                            serverSideScript);
                }
            }
            else
            {
                String script = cbl.get(0).getScript(
                        ClientBehaviorContext
                                .createClientBehaviorContext(
                                        facesContext, component,
                                        eventName, sourceId,
                                        eventParameters));
                if (deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, script))
                {
                    return true;
                }
                return HtmlRendererUtils.renderHTMLStringAttribute(
                        writer, componentProperty, htmlAttrName,
                        script);
            }
        }
        else
        {
            String chain = ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                    component, sourceId, eventName,
                    eventParameters, clientBehaviors, attributeValue,
                    serverSideScript);
            if (StringUtils.isNotBlank(chain)
                    && deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, chain))
            {
                return true;
            }
            return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                    chain);
        }
    }

    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String sourceId, String eventName,
            Collection<ClientBehaviorContext.Parameter> eventParameters,
            String eventName2,
            Collection<ClientBehaviorContext.Parameter> eventParameters2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException
    {
        List<ClientBehavior> cb1 = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;
        List<ClientBehavior> cb2 = (clientBehaviors != null) ? clientBehaviors.get(eventName2) : null;
        String targetElementId = sourceId != null ? sourceId : component.getClientId(facesContext);
        if (((cb1 != null) ? cb1.size() : 0) + ((cb2 != null) ? cb2.size() : 0)
                + (attributeValue != null ? 1 : 0) <= 1)
        {
            if (attributeValue != null)
            {
                return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                        attributeValue);
            }
            else if (serverSideScript != null)
            {
                return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                        serverSideScript);
            }
            else if (((cb1 != null) ? cb1.size() : 0) > 0)
            {
                String script = cb1.get(0).getScript(ClientBehaviorContext
                        .createClientBehaviorContext(
                                facesContext, component,
                                eventName, sourceId,
                                eventParameters));
                if (deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, script))
                {
                    return true;
                }
                return HtmlRendererUtils.renderHTMLStringAttribute(
                        writer, componentProperty, htmlAttrName,
                        script);
            }
            else
            {
                String script = cb2.get(0).getScript(ClientBehaviorContext
                        .createClientBehaviorContext(
                                facesContext, component,
                                eventName2, sourceId,
                                eventParameters2));
                if (deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, script))
                {
                    return true;
                }
                return HtmlRendererUtils.renderHTMLStringAttribute(
                        writer, componentProperty, htmlAttrName,
                        script);
            }
        }
        else
        {
            String chain = ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                    component, sourceId, eventName,
                    eventParameters, eventName2, eventParameters2,
                    clientBehaviors, attributeValue, serverSideScript);
            if (StringUtils.isNotBlank(chain)
                    && deferClientBehaviorScriptIfCspNonceActive(facesContext, targetElementId, htmlAttrName, chain))
            {
                return true;
            }
            return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                    chain);
        }
    }
    // CHECKSTYLE:ON

    public static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedEventHandlers(facesContext, writer, 
                commonAttributesMarked, commonEventsMarked, uiComponent,
                null, clientBehaviors);
    }
    
    public static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonAttributesMarked & CommonHtmlAttributes.ONCLICK) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.CLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CLICK,
                    clientBehaviors, HTML.ONCLICK_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONDBLCLICK) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.DBLCLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.DBLCLICK,
                    clientBehaviors, HTML.ONDBLCLICK_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEDOWN) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEDOWN_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEDOWN, clientBehaviors,
                    HTML.ONMOUSEDOWN_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEUP) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEUP,
                    clientBehaviors, HTML.ONMOUSEUP_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEOVER) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEOVER) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEOVER_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEOVER, clientBehaviors,
                    HTML.ONMOUSEOVER_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEMOVE) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEMOVE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEMOVE_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEMOVE, clientBehaviors,
                    HTML.ONMOUSEMOVE_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEOUT) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEOUT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEOUT,
                    clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYPRESS) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYPRESS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYPRESS,
                    clientBehaviors, HTML.ONKEYPRESS_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYDOWN) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYDOWN,
                    clientBehaviors, HTML.ONKEYDOWN_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYUP) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYUP,
                    clientBehaviors, HTML.ONKEYUP_ATTR);
        }
    }

    public static void renderBehaviorizedEventHandlersWithoutOnclick(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, 
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedEventHandlersWithoutOnclick(facesContext, writer, 
                commonAttributesMarked, commonEventsMarked, uiComponent,
                null, clientBehaviors);
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     */
    public static void renderBehaviorizedEventHandlersWithoutOnclick(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonAttributesMarked & CommonHtmlAttributes.ONDBLCLICK) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.DBLCLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.DBLCLICK,
                    clientBehaviors, HTML.ONDBLCLICK_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEDOWN) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEDOWN_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEDOWN, clientBehaviors,
                    HTML.ONMOUSEDOWN_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEUP) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEUP,
                    clientBehaviors, HTML.ONMOUSEUP_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEOVER) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEOVER) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEOVER_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEOVER, clientBehaviors,
                    HTML.ONMOUSEOVER_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEMOVE) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEMOVE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEMOVE_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEMOVE, clientBehaviors,
                    HTML.ONMOUSEMOVE_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONMOUSEOUT) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.MOUSEOUT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEOUT,
                    clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYPRESS) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYPRESS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYPRESS,
                    clientBehaviors, HTML.ONKEYPRESS_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYDOWN) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYDOWN,
                    clientBehaviors, HTML.ONKEYDOWN_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONKEYUP) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.KEYUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYUP,
                    clientBehaviors, HTML.ONKEYUP_ATTR);
        }
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     */
    public static void renderBehaviorizedFieldEventHandlers(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonAttributesMarked & CommonHtmlAttributes.ONFOCUS) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONBLUR) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONCHANGE) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.CHANGE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CHANGE, clientBehaviors,
                    HTML.ONCHANGE_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONSELECT) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnfocus(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }

        if ((commonAttributesMarked & CommonHtmlAttributes.ONBLUR) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONCHANGE) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.CHANGE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CHANGE, clientBehaviors,
                    HTML.ONCHANGE_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONSELECT) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, 
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedFieldEventHandlersWithoutOnchange(
                facesContext, writer, commonAttributesMarked, commonEventsMarked, 
                uiComponent, null, clientBehaviors);
    }
    
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonAttributesMarked & CommonHtmlAttributes.ONFOCUS) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONBLUR) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONSELECT) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                facesContext, writer, 
                commonAttributesMarked, commonEventsMarked, 
                uiComponent, null, 
                clientBehaviors);
    }
    
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
            FacesContext facesContext, ResponseWriter writer,
            long commonAttributesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }

        if ((commonAttributesMarked & CommonHtmlAttributes.ONFOCUS) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonAttributesMarked & CommonHtmlAttributes.ONBLUR) != 0 ||
            (commonEventsMarked & CommonHtmlEvents.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
    }
}
