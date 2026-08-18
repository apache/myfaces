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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
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

    /**
     * The event names always rendered by the standard HTML renderers via the various
     * <code>renderBehaviorizedEventHandlers</code> flavors: the generic mouse, key and click events.
     * Building block for the per-renderer sets below, which name the events the respective renderer takes care of
     * itself; every other event has to go through {@link #renderAdditionalBehaviorEventHandlers}.
     */
    private static final Set<String> COMMON_DOM_EVENTS = Set.of(
            ClientBehaviorEvents.CLICK, ClientBehaviorEvents.DBLCLICK,
            ClientBehaviorEvents.MOUSEDOWN, ClientBehaviorEvents.MOUSEUP, ClientBehaviorEvents.MOUSEOVER,
            ClientBehaviorEvents.MOUSEMOVE, ClientBehaviorEvents.MOUSEOUT,
            ClientBehaviorEvents.KEYPRESS, ClientBehaviorEvents.KEYDOWN, ClientBehaviorEvents.KEYUP);

    /**
     * Events handled by renderers of plain elements: div/span/table/form/img and alike.
     * See {@link org.apache.myfaces.renderkit.html.base.HtmlRenderer#renderEventHandlers}.
     */
    public static final Set<String> RENDERER_HANDLED_COMMON_EVENTS = COMMON_DOM_EVENTS;

    /**
     * Events handled by renderers of input fields: change, focus, blur, select on top of the common ones,
     * plus the virtual valueChange which is rendered on the change attribute.
     * See {@link org.apache.myfaces.renderkit.html.base.HtmlRenderer#renderFieldEventHandlers}.
     */
    public static final Set<String> RENDERER_HANDLED_FIELD_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.CHANGE, ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR,
            ClientBehaviorEvents.SELECT, ClientBehaviorEvents.VALUECHANGE);

    /**
     * Events handled by the listbox/menu renderers: like the field events, but without select
     * (renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect).
     */
    public static final Set<String> RENDERER_HANDLED_SELECTABLE_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.CHANGE, ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR,
            ClientBehaviorEvents.VALUECHANGE);

    /**
     * Events handled by the label/outcome target button renderers: only focus and blur on top of the common ones.
     */
    public static final Set<String> RENDERER_HANDLED_FOCUS_BLUR_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR);

    /**
     * Events handled by the command button renderer: the field events plus the virtual action, which is rendered
     * on the click attribute.
     */
    public static final Set<String> RENDERER_HANDLED_COMMAND_BUTTON_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.CHANGE, ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR,
            ClientBehaviorEvents.SELECT, ClientBehaviorEvents.ACTION);

    /**
     * Events handled by the link renderers: focus and blur plus the virtual action, which is rendered
     * on the click attribute.
     */
    public static final Set<String> RENDERER_HANDLED_LINK_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR, ClientBehaviorEvents.ACTION);

    /**
     * Events handled by the body renderer: load and unload on top of the common ones.
     */
    public static final Set<String> RENDERER_HANDLED_BODY_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.LOAD, ClientBehaviorEvents.UNLOAD);

    /**
     * Events handled by the jsf:element renderer: focus, blur, change, select, load and unload
     * on top of the common ones.
     */
    public static final Set<String> RENDERER_HANDLED_PASSTHROUGH_ELEMENT_EVENTS = merge(COMMON_DOM_EVENTS,
            ClientBehaviorEvents.FOCUS, ClientBehaviorEvents.BLUR, ClientBehaviorEvents.CHANGE,
            ClientBehaviorEvents.SELECT, ClientBehaviorEvents.LOAD, ClientBehaviorEvents.UNLOAD);

    private static Set<String> merge(Set<String> events, String... moreEvents)
    {
        Set<String> merged = new HashSet<>(events);
        merged.addAll(Arrays.asList(moreEvents));
        return Collections.unmodifiableSet(merged);
    }

    /**
     * Renders every behavior event attribute which is <em>not</em> handled by the calling renderer itself,
     * as required since Faces 5.0 (spec issue 1507).
     * <p>
     * A {@link ClientBehaviorHolder} exposes every HTML event name via {@link ClientBehaviorHolder#getEventNames()},
     * not only those which happen to have a matching component property. So both
     * <code>&lt;h:inputText oninput="..."/&gt;</code> and
     * <code>&lt;h:inputText&gt;&lt;f:ajax event="input"/&gt;&lt;/h:inputText&gt;</code> have to end up as an
     * <code>oninput</code> attribute, chained together when both are present. Each renderer only takes care of the
     * events for which the component has a matching property, hence this generic pass for all the others.
     * <p>
     * This must be invoked <em>after</em> the renderer specific event attributes have been rendered, with
     * <code>rendererHandledEvents</code> naming exactly the events the calling renderer renders itself
     * (one of the <code>RENDERER_HANDLED_*</code> constants of this class).
     *
     * @param facesContext The involved faces context.
     * @param writer The involved response writer.
     * @param component The component being rendered.
     * @param clientBehaviors The client behaviors of the component, may be <code>null</code> or empty.
     * @param rendererHandledEvents The events the calling renderer renders itself, to be skipped here.
     */
    public static void renderAdditionalBehaviorEventHandlers(FacesContext facesContext, ResponseWriter writer,
            UIComponent component, Map<String, List<ClientBehavior>> clientBehaviors,
            Set<String> rendererHandledEvents) throws IOException
    {
        if (!(component instanceof ClientBehaviorHolder holder))
        {
            return;
        }

        Set<String> additionalEventNames =
                collectAdditionalEventNames(component, clientBehaviors, rendererHandledEvents);
        if (additionalEventNames == null)
        {
            return;
        }

        Map<String, Object> attributes = component.getAttributes();
        Collection<String> eventNames = holder.getEventNames();

        for (String eventName : additionalEventNames)
        {
            // as required by the standard HTML RenderKit, the name after "on" must be a supported event name
            if (eventNames == null || !eventNames.contains(eventName))
            {
                continue;
            }

            String attributeName = CommonHtmlEvents.BEHAVIOR_EVENT_ATTRIBUTE_PREFIX + eventName;
            Object attributeValue = attributes.get(attributeName);

            renderBehaviorizedAttribute(facesContext, writer, attributeName, component, null, eventName, null,
                    clientBehaviors, attributeName,
                    attributeValue == null ? null : attributeValue.toString());
        }
    }

    /**
     * @return whether {@link #renderAdditionalBehaviorEventHandlers} would render anything for the given component.
     *         Intended for renderers which have to decide upfront whether an element has to be started at all.
     */
    public static boolean hasAdditionalBehaviorEventHandlers(UIComponent component,
            Map<String, List<ClientBehavior>> clientBehaviors, Set<String> rendererHandledEvents)
    {
        if (!(component instanceof ClientBehaviorHolder holder))
        {
            return false;
        }

        Set<String> additionalEventNames =
                collectAdditionalEventNames(component, clientBehaviors, rendererHandledEvents);
        if (additionalEventNames == null)
        {
            return false;
        }

        Collection<String> eventNames = holder.getEventNames();
        if (eventNames == null)
        {
            return false;
        }

        for (String eventName : additionalEventNames)
        {
            if (eventNames.contains(eventName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects the names of all behavior events of the given component which the calling renderer does not handle
     * itself, or <code>null</code> if there are none, which is the common case that has to stay cheap.
     */
    @SuppressWarnings("unchecked") // the marker entry is stored under an Object-valued component attribute
    private static Set<String> collectAdditionalEventNames(UIComponent component,
            Map<String, List<ClientBehavior>> clientBehaviors, Set<String> rendererHandledEvents)
    {
        Set<String> additionalEventNames = null;

        // 1) <f:ajax event="input"> and friends; any behavior whose event is not rendered by the renderer itself.
        // Usually empty or holds a single renderer handled event, so this loop is a no-op in practice.
        if (clientBehaviors != null && !clientBehaviors.isEmpty())
        {
            for (String eventName : clientBehaviors.keySet())
            {
                if (!rendererHandledEvents.contains(eventName))
                {
                    additionalEventNames = add(additionalEventNames, eventName);
                }
            }
        }

        // 2) + 3) in a SINGLE pass over the component attribute map, which is the only part of this method that
        // every rendered component has to pay for. That map is the plain state helper map (no property descriptor
        // lookups, no ValueExpression evaluation) and normally holds no more than the oam.* markers. It gives us
        //   - the literal on* attributes without matching property, e.g. oninput="...", which the facelets
        //     tag handler stores as plain attributes
        //   - the EVENT_ATTRIBUTES_MARKED entry naming the ValueExpression bound ones, e.g. oninput="#{...}",
        //     as those are not part of the attribute map at all
        for (Map.Entry<String, Object> attribute : component.getAttributes().entrySet())
        {
            String name = attribute.getKey();
            if (CommonHtmlEvents.isBehaviorEventAttribute(name))
            {
                String eventName = CommonHtmlEvents.getEventName(name);
                if (!rendererHandledEvents.contains(eventName))
                {
                    additionalEventNames = add(additionalEventNames, eventName);
                }
            }
            else if (CommonHtmlEvents.EVENT_ATTRIBUTES_MARKED.equals(name))
            {
                for (String markedName : (Set<String>) attribute.getValue())
                {
                    String eventName = CommonHtmlEvents.getEventName(markedName);
                    if (!rendererHandledEvents.contains(eventName))
                    {
                        additionalEventNames = add(additionalEventNames, eventName);
                    }
                }
            }
        }

        return additionalEventNames;
    }

    /**
     * Lazily creates the collector; a sorted set keeps the rendered attribute order deterministic.
     */
    private static Set<String> add(Set<String> eventNames, String eventName)
    {
        Set<String> result = eventNames == null ? new TreeSet<>() : eventNames;
        result.add(eventName);
        return result;
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
            Long commonAttributesMarked, Long commonEventsMarked,
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
            Long commonAttributesMarked, Long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == null)
        {
            commonAttributesMarked = 0L;
        }
        if (commonEventsMarked == null)
        {
            commonEventsMarked = 0L;
        }

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
            Long commonAttributesMarked, Long commonEventsMarked,
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
            Long commonAttributesMarked, Long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == null)
        {
            commonAttributesMarked = 0L;
        }
        if (commonEventsMarked == null)
        {
            commonEventsMarked = 0L;
        }

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
            Long commonAttributesMarked, Long commonEventsMarked,
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
            Long commonAttributesMarked, Long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == null)
        {
            commonAttributesMarked = 0L;
        }
        if (commonEventsMarked == null)
        {
            commonEventsMarked = 0L;
        }

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
            Long commonAttributesMarked, Long commonEventsMarked,
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
            Long commonAttributesMarked, Long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonAttributesMarked == null)
        {
            commonAttributesMarked = 0L;
        }
        if (commonEventsMarked == null)
        {
            commonEventsMarked = 0L;
        }

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
