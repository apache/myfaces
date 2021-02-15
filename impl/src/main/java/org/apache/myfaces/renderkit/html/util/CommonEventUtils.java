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
import org.apache.myfaces.core.api.shared.CommonEventConstants;
import org.apache.myfaces.core.api.shared.CommonPropertyConstants;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;

public class CommonEventUtils
{
    public static long getCommonEventsMarked(UIComponent component)
    {
        Long commonEvents = (Long) component.getAttributes().get(CommonEventConstants.COMMON_EVENTS_MARKED);
        
        if (commonEvents == null)
        {
            commonEvents = 0L;
        }
        return commonEvents;
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
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedEventHandlers(facesContext, writer, 
                commonPropertiesMarked, commonEventsMarked, uiComponent,
                null, clientBehaviors);
    }
    
    public static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonPropertyConstants.ONCLICK_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.CLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CLICK,
                    clientBehaviors, HTML.ONCLICK_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONDBLCLICK_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.DBLCLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.DBLCLICK,
                    clientBehaviors, HTML.ONDBLCLICK_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEDOWN_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEDOWN_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEDOWN, clientBehaviors,
                    HTML.ONMOUSEDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEUP_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEUP,
                    clientBehaviors, HTML.ONMOUSEUP_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEOVER_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEOVER) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEOVER_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEOVER, clientBehaviors,
                    HTML.ONMOUSEOVER_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEMOVE_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEMOVE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEMOVE_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEMOVE, clientBehaviors,
                    HTML.ONMOUSEMOVE_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEOUT_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEOUT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEOUT,
                    clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYPRESS_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYPRESS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYPRESS,
                    clientBehaviors, HTML.ONKEYPRESS_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYDOWN_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYDOWN,
                    clientBehaviors, HTML.ONKEYDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYUP_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYUP,
                    clientBehaviors, HTML.ONKEYUP_ATTR);
        }
    }

    public static void renderBehaviorizedEventHandlersWithoutOnclick(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, 
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedEventHandlersWithoutOnclick(facesContext, writer, 
                commonPropertiesMarked, commonEventsMarked, uiComponent,
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
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonPropertyConstants.ONDBLCLICK_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.DBLCLICK) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.DBLCLICK,
                    clientBehaviors, HTML.ONDBLCLICK_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEDOWN_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEDOWN_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEDOWN, clientBehaviors,
                    HTML.ONMOUSEDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEUP_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEUP) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEUP,
                    clientBehaviors, HTML.ONMOUSEUP_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEOVER_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEOVER) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEOVER_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEOVER, clientBehaviors,
                    HTML.ONMOUSEOVER_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEMOVE_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEMOVE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer,
                    HTML.ONMOUSEMOVE_ATTR, uiComponent, sourceId,
                    ClientBehaviorEvents.MOUSEMOVE, clientBehaviors,
                    HTML.ONMOUSEMOVE_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONMOUSEOUT_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.MOUSEOUT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.MOUSEOUT,
                    clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYPRESS_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYPRESS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYPRESS,
                    clientBehaviors, HTML.ONKEYPRESS_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYDOWN_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYDOWN) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.KEYDOWN,
                    clientBehaviors, HTML.ONKEYDOWN_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONKEYUP_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.KEYUP) != 0)
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
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonPropertyConstants.ONFOCUS_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONBLUR_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONCHANGE_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.CHANGE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CHANGE, clientBehaviors,
                    HTML.ONCHANGE_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONSELECT_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnfocus(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }

        if ((commonPropertiesMarked & CommonPropertyConstants.ONBLUR_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONCHANGE_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.CHANGE) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.CHANGE, clientBehaviors,
                    HTML.ONCHANGE_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONSELECT_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, 
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedFieldEventHandlersWithoutOnchange(
                facesContext, writer, commonPropertiesMarked, commonEventsMarked, 
                uiComponent, null, clientBehaviors);
    }
    
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }
        
        if ((commonPropertiesMarked & CommonPropertyConstants.ONFOCUS_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONBLUR_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONSELECT_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.SELECT) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.SELECT, clientBehaviors,
                    HTML.ONSELECT_ATTR);
        }
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                facesContext, writer, 
                commonPropertiesMarked, commonEventsMarked, 
                uiComponent, null, 
                clientBehaviors);
    }
    
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
            FacesContext facesContext, ResponseWriter writer,
            long commonPropertiesMarked, long commonEventsMarked,
            UIComponent uiComponent, String sourceId,
            Map<String, List<ClientBehavior>> clientBehaviors)
            throws IOException
    {
        if (commonPropertiesMarked == 0 && commonEventsMarked == 0)
        {
            return;
        }

        if ((commonPropertiesMarked & CommonPropertyConstants.ONFOCUS_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.FOCUS) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.FOCUS, clientBehaviors,
                    HTML.ONFOCUS_ATTR);
        }
        if ((commonPropertiesMarked & CommonPropertyConstants.ONBLUR_PROP) != 0 ||
            (commonEventsMarked & CommonEventConstants.BLUR) != 0)
        {
            renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR,
                    uiComponent, sourceId, ClientBehaviorEvents.BLUR, clientBehaviors,
                    HTML.ONBLUR_ATTR);
        }
    }
}
