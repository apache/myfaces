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
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlInputFile;
import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.apache.myfaces.renderkit.html.util.HttpPartWrapper;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.JSFAttr;
import org.apache.myfaces.util.lang.Assert;

public class HtmlInputFileRendererBase extends HtmlRenderer
{
    private static final Logger log = Logger.getLogger(HtmlInputFileRendererBase.class.getName());
    
    private static final String AUTOCOMPLETE_VALUE_OFF = "off";

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        try
        {
           Part part = ((HttpServletRequest) facesContext.getExternalContext().getRequest()).
                   getPart(component.getClientId());
           if (part == null)
           {
               return;
           }

           ((UIInput) component).setSubmittedValue(new HttpPartWrapper(part));
        }
        catch (IOException | ServletException e)
        {
            throw new FacesException(e);
        }
        
        if (component instanceof ClientBehaviorHolder && !HtmlRendererUtils.isDisabled(component))
        {
            ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
        }
    }
    
    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
        throws IOException
    {   
        renderInput(facesContext, component);
        
        if(!facesContext.isProjectStage(ProjectStage.Production)
                && facesContext.isPostback()
                && (facesContext.getPartialViewContext().isPartialRequest() ||
                    facesContext.getPartialViewContext().isAjaxRequest()))
        {
            UIForm form = ComponentUtils.findClosest(UIForm.class, component);
            if (form != null && form instanceof HtmlForm)
            {
                String content = ((HtmlForm) form).getEnctype();
                if (content == null || !content.contains("multipart/form-data"))
                {
                     FacesMessage message = new FacesMessage("file upload requires a form with"+
                            " enctype equal to multipart/form-data");
                     facesContext.addMessage(component.getClientId(), message);
                }
            }
        }
    }
   
    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException
    {
        Assert.notNull(context, "context");
        Assert.notNull(component, "component");

        return submittedValue;
    }

    protected void renderValue(FacesContext facesContext, UIComponent component, ResponseWriter writer)
            throws IOException
    {
        //the input file element cannot render a value it is readonly
    }
    
    protected boolean isRenderOutputEventAttributes()
    {
        return true;
    }

    protected void renderInput(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        //allow subclasses to render custom attributes by separating rendering begin and end 
        renderInputBegin(facesContext, component);
        renderInputEnd(facesContext, component);
    }

    //Subclasses can set the value of an attribute before, or can render a custom attribute after calling this method
    protected void renderInputBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        String clientId = component.getClientId(facesContext);

        writer.startElement(HTML.INPUT_ELEM, component);
        writer.writeAttribute(HTML.ID_ATTR, clientId, null);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_FILE, null);

        renderValue(facesContext, component, writer);

        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            
            long commonPropertiesMarked = 0L;
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
            }
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderChangeEventProperty(writer, 
                        commonPropertiesMarked, component);
                CommonHtmlAttributesUtil.renderEventProperties(writer, 
                        commonPropertiesMarked, component);
                CommonHtmlAttributesUtil.renderFieldEventPropertiesWithoutOnchange(writer, 
                        commonPropertiesMarked, component);
            }
            else
            {
                HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, component, behaviors);
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(component);
                    CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                            commonPropertiesMarked, commonEventsMarked, component, behaviors);
                    CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                        facesContext, writer, commonPropertiesMarked, commonEventsMarked, component, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, component, behaviors);
                    HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                            facesContext, writer, component, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabledAndEvents(writer, 
                        commonPropertiesMarked, component);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                        HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
            }
        }
        else
        {
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabled(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                        HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
            }
        }

        HtmlInputFile inputFile = (HtmlInputFile) component;
        
        if (inputFile.isDisabled())
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        if (AUTOCOMPLETE_VALUE_OFF.equals(inputFile.getAutocomplete()))
        {
            writer.writeAttribute(HTML.AUTOCOMPLETE_ATTR, AUTOCOMPLETE_VALUE_OFF, HTML.AUTOCOMPLETE_ATTR);
        }

        if (inputFile.getAccept() != null)
        {
            writer.writeAttribute(HTML.ACCEPT_ATTR, inputFile.getAccept(), HTML.ACCEPT_ATTR);
        }
    }

    protected void renderInputEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter(); 

        writer.endElement(HTML.INPUT_ELEM);
    }

    public static void renderOutputText(FacesContext facesContext, UIComponent component, String text, boolean escape)
            throws IOException
    {
        if (text != null)
        {
            ResponseWriter writer = facesContext.getResponseWriter();
            boolean span = false;

            if (component.getId() != null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
            {
                span = true;

                writer.startElement(HTML.SPAN_ELEM, component);

                HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }
            else
            {
                span = HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(writer,
                                component, HTML.SPAN_ELEM, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }

            if (escape)
            {
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("renderOutputText writing '" + text + '\'');
                }
                writer.writeText(text, JSFAttr.VALUE_ATTR);
            }
            else
            {
                writer.write(text);
            }

            if (span)
            {
                writer.endElement(HTML.SPAN_ELEM);
            }
        }
    }
}
