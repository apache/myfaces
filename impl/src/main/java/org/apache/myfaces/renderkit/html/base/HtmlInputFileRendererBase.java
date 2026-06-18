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
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlInputFile;
import org.apache.myfaces.core.api.shared.ComponentUtils;

import org.apache.myfaces.renderkit.html.util.HttpPartWrapper;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.core.api.shared.lang.Assert;

public class HtmlInputFileRendererBase<T extends HtmlInputFile> extends HtmlRenderer<T>
{
    private static final Logger log = Logger.getLogger(HtmlInputFileRendererBase.class.getName());

    @Override
    public void decode(FacesContext facesContext, T component)
    {
        try
        {
            String clientId = component.getClientId(facesContext);
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            Collection<Part> parts = request.getParts();
            Collection<Part> submittedValues = new ArrayList<>(parts.size());
            for (Part part : parts)
            {
                if (clientId.equals(part.getName()))
                {
                    HttpPartWrapper wrapper = new HttpPartWrapper(part);
                    submittedValues.add(wrapper);
                }
            }
            if (component.isMultiple())
            {
                component.setSubmittedValue(submittedValues);
            }
            else if (!submittedValues.isEmpty())
            {
                component.setSubmittedValue(submittedValues.iterator().next());
            }
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
    public void encodeEnd(FacesContext facesContext, T component)
        throws IOException
    {   
        renderInput(facesContext, component);
        
        if(!facesContext.isProjectStage(ProjectStage.Production)
                && facesContext.isPostback()
                && (facesContext.getPartialViewContext().isPartialRequest() ||
                    facesContext.getPartialViewContext().isAjaxRequest()))
        {
            UIForm form = ComponentUtils.findClosest(UIForm.class, component);
            if (form instanceof HtmlForm htmlForm)
            {
                String content = htmlForm.getEnctype();
                if (content == null || !content.contains("multipart/form-data"))
                {
                     FacesMessage message = new FacesMessage("file upload requires a form with"+
                            " enctype equal to multipart/form-data");
                     facesContext.addMessage(component.getClientId(facesContext), message);
                }
            }
        }
    }
   
    @Override
    public Object getConvertedValue(FacesContext context, T component, Object submittedValue)
            throws ConverterException
    {
        Assert.notNull(context, "context");
        Assert.notNull(component, "component");

        if (submittedValue instanceof Part part)
        {
            if (isEmpty(part))
            {
                return null;
            }
        }
        else if (submittedValue instanceof Collection)
        {
            Collection<Part> parts = (Collection<Part>) submittedValue;
            List<Part> nonEmpty = new ArrayList<>(parts.size());
            for (Part part : parts)
            {
                if (!isEmpty(part))
                {
                    nonEmpty.add(part);
                }
            }
            return Collections.unmodifiableList(nonEmpty);
        }
        return submittedValue;
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

        Long commonPropertiesMarked = getCommonPropertiesMarked(facesContext, component);
        Map<String, List<ClientBehavior>> behaviors = getClientBehaviors(component);

        if (behaviors != null)
        {
            renderFieldEventHandlers(facesContext, writer, component, behaviors, commonPropertiesMarked);
        }

        if (commonPropertiesMarked != null)
        {
            if (behaviors != null)
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabledAndEvents(
                        writer, commonPropertiesMarked, component);
            }
            else
            {
                CommonHtmlAttributesUtil.renderInputPassthroughPropertiesWithoutDisabled(
                        writer, commonPropertiesMarked, component);
            }
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, component,
                    behaviors != null
                        ? HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS
                        : HTML.INPUT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }

        HtmlInputFile inputFile = (HtmlInputFile) component;
        
        if (inputFile.isDisabled())
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, HTML.DISABLED_ATTR, null);
        }

        if (inputFile.isMultiple())
        {
            writer.writeAttribute(HTML.MULTIPLE_ATTR, HTML.MULTIPLE_ATTR, null);
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

    private static boolean isEmpty(Part part)
    {
        return part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty() || part.getSize() <= 0;
    }
}
