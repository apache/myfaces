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
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlInputSecret;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.myfaces.core.api.shared.AttributeUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

/**
 * see Spec.1.0 EA - Faces.7.6.4 Renderer Types for UIInput Components
 */
public class HtmlSecretRendererBase extends HtmlRenderer
{
    private static final String AUTOCOMPLETE_VALUE_OFF = "off";

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
      RendererUtils.checkParamValidity(facesContext, uiComponent, UIInput.class);

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
      //allow subclasses to render custom attributes by separating rendering begin and end
      renderInputBegin(facesContext, uiComponent);
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
            throws IOException
    {
        renderInputEnd(facesContext, uiComponent);
    }


    //Subclasses can set the value of an attribute before, or can render a custom attribute after calling this method
    protected void renderInputBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        
        writer.startElement(HTML.INPUT_ELEM, uiComponent);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_PASSWORD, null);

        if (uiComponent instanceof ClientBehaviorHolder
                && !((ClientBehaviorHolder) uiComponent).getClientBehaviors().isEmpty())
        {
            writer.writeAttribute(HTML.ID_ATTR, uiComponent.getClientId(facesContext), null);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        }
        writer.writeAttribute(HTML.NAME_ATTR, uiComponent.getClientId(facesContext), null);

        boolean isRedisplay;
        if (uiComponent instanceof HtmlInputSecret)
        {
            isRedisplay = ((HtmlInputSecret)uiComponent).isRedisplay();
        }
        else
        {
            isRedisplay = AttributeUtils.getBooleanAttribute(uiComponent, ComponentAttrs.REDISPLAY_ATTR, false);
        }
        if (isRedisplay)
        {
            String strValue = RendererUtils.getStringValue(facesContext, uiComponent);
            writer.writeAttribute(HTML.VALUE_ATTR, strValue, ComponentAttrs.VALUE_ATTR);
        }

        if (uiComponent instanceof ClientBehaviorHolder)
        {
            Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            
            long commonPropertiesMarked = 0L;
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(uiComponent);
            }
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderChangeEventProperty(writer, 
                        commonPropertiesMarked, uiComponent);
                CommonHtmlAttributesUtil.renderEventProperties(writer, 
                        commonPropertiesMarked, uiComponent);
                CommonHtmlAttributesUtil.renderFieldEventPropertiesWithoutOnchange(writer, 
                        commonPropertiesMarked, uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, uiComponent, behaviors);
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(uiComponent);
                    CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                            commonPropertiesMarked, commonEventsMarked, uiComponent, behaviors);
                    CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                        facesContext, writer, commonPropertiesMarked, commonEventsMarked, uiComponent, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
                    HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                            facesContext, writer, uiComponent, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabledAndEvents(writer, 
                        commonPropertiesMarked, uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, 
                        HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
            }
        }
        else
        {
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabled(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(uiComponent), uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, 
                        HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
            }
        }

        if (isDisabled(facesContext, uiComponent))
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, HTML.DISABLED_ATTR, null);
        }

        if (isAutocompleteOff(facesContext, uiComponent))
        {
            writer.writeAttribute(HTML.AUTOCOMPLETE_ATTR, AUTOCOMPLETE_VALUE_OFF, HTML.AUTOCOMPLETE_ATTR);
        }
    }

    protected void renderInputEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter(); 

        writer.endElement(HTML.INPUT_ELEM);
    }

    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlInputSecret)
        {
            return ((HtmlInputSecret)uiComponent).isDisabled();
        }

        return AttributeUtils.getBooleanAttribute(uiComponent, HTML.DISABLED_ATTR, false);
    }

    /**
     * If autocomplete is "on" or not set, do not render it
     */
    protected boolean isAutocompleteOff(FacesContext facesContext, UIComponent component)
    {
        if (component instanceof HtmlInputSecret)
        {
            String autocomplete = ((HtmlInputSecret)component).getAutocomplete();
            if (autocomplete != null)
            {
                return autocomplete.equals(AUTOCOMPLETE_VALUE_OFF);
            }
        }

        return false;
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        RendererUtils.checkParamValidity(facesContext, component, UIInput.class);

        HtmlRendererUtils.decodeUIInput(facesContext, component);
        if (component instanceof ClientBehaviorHolder && !HtmlRendererUtils.isDisabled(component))
        {
            ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
        }
    }

    @Override
    public Object getConvertedValue(FacesContext facesContext, UIComponent uiComponent, Object submittedValue)
        throws ConverterException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, UIOutput.class);
        return RendererUtils.getConvertedUIOutputValue(facesContext,
                                                       (UIOutput)uiComponent,
                                                       submittedValue);
    }

}
