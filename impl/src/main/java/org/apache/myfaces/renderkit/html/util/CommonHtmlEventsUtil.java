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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.CommonHtmlEvents;
import org.apache.myfaces.core.api.shared.CommonHtmlAttributes;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;

public class CommonHtmlEventsUtil
{
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
            return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty,
                    htmlAttrName, attributeValue);
        }

        if (cbl.size() > 1 || (cbl.size() == 1 && attributeValue != null))
        {
            return HtmlRendererUtils.renderHTMLStringAttribute(writer, componentProperty, htmlAttrName,
                    ClientBehaviorRendererUtils.buildBehaviorChain(facesContext,
                            component, sourceId, eventName,
                            eventParameters, clientBehaviors, attributeValue,
                            RendererUtils.EMPTY_STRING));
        }
        else
        {
            //Only 1 behavior and attrValue == null, so just render it directly
            ClientBehaviorContext ctx = ClientBehaviorContext.createClientBehaviorContext(
                                    facesContext, component, eventName,sourceId, eventParameters);
            return HtmlRendererUtils.renderHTMLStringAttribute(
                    writer,
                    componentProperty,
                    htmlAttrName,
                    cbl.get(0).getScript(ctx));
        }
    }

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
