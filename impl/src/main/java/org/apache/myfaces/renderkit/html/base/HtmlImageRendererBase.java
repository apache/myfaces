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
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonPropertyUtils;
import org.apache.myfaces.renderkit.html.util.CommonEventUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.apache.myfaces.renderkit.html.util.JSFAttr;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.renderkit.html.util.HTML;

public class HtmlImageRendererBase extends HtmlRenderer
{
    private static final Logger log = Logger.getLogger(HtmlImageRendererBase.class.getName());
    
    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);
        
        ClientBehaviorRendererUtils.decodeClientBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, UIGraphic.class);

        ResponseWriter writer = facesContext.getResponseWriter();
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
            }
        }
        
        writer.startElement(HTML.IMG_ELEM, uiComponent);

        if (uiComponent instanceof ClientBehaviorHolder && !behaviors.isEmpty())
        {
            HtmlRendererUtils.writeIdAndName(writer, uiComponent, facesContext);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        }

        final String url = RendererUtils.getIconSrc(facesContext, uiComponent, JSFAttr.URL_ATTR);
        if (url != null)
        {
            writer.writeURIAttribute(HTML.SRC_ATTR, url,JSFAttr.VALUE_ATTR);
        }
        else
        {
            Level level = facesContext.isProjectStage(ProjectStage.Production)
                    ? Level.FINE
                    : Level.WARNING;
            if (log.isLoggable(level))
            {
                log.log(level, "Component UIGraphic " + uiComponent.getClientId(facesContext) 
                        + " has no attribute url, value, name or attribute resolves to null. Path to component " 
                        + ComponentUtils.getPathToComponent(uiComponent));
            }
        }

        /* 
         * Warn the user if the ALT attribute is missing.
         */                
        if (uiComponent.getAttributes().get(HTML.ALT_ATTR) == null) 
        {
            Level level = facesContext.isProjectStage(ProjectStage.Production)
                    ? Level.FINE
                    : Level.WARNING;
            if (log.isLoggable(level))
            {
                log.log(level, "Component UIGraphic " + uiComponent.getClientId(facesContext) 
                        + " has no attribute alt or attribute resolves to null. Path to component " 
                        + ComponentUtils.getPathToComponent(uiComponent));
            }
        }

        if (uiComponent instanceof ClientBehaviorHolder)
        {
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonPropertyUtils.renderEventProperties(writer, 
                        CommonPropertyUtils.getCommonPropertiesMarked(uiComponent), uiComponent);
            }
            else
            {
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    CommonEventUtils.renderBehaviorizedEventHandlers(facesContext, writer, 
                           CommonPropertyUtils.getCommonPropertiesMarked(uiComponent),
                           CommonEventUtils.getCommonEventsMarked(uiComponent), uiComponent, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.IMG_ATTRIBUTES);
                CommonPropertyUtils.renderCommonPassthroughPropertiesWithoutEvents(writer, 
                        CommonPropertyUtils.getCommonPropertiesMarked(uiComponent), uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, 
                        HTML.IMG_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
            }
        }
        else
        {
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.IMG_ATTRIBUTES);
                CommonPropertyUtils.renderCommonPassthroughProperties(writer, 
                        CommonPropertyUtils.getCommonPropertiesMarked(uiComponent), uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, 
                        HTML.IMG_PASSTHROUGH_ATTRIBUTES);
            }
        }

        writer.endElement(HTML.IMG_ELEM);

    }

}
