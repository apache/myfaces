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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.html.HtmlOutputFormat;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.core.api.shared.AttributeUtils;
import org.apache.myfaces.core.api.shared.CommonHtmlAttributes;
import org.apache.myfaces.core.api.shared.ComponentUtils;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.base.HtmlRenderer;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

/**
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "jakarta.faces.Output", type = "jakarta.faces.Format")
public class HtmlFormatRenderer extends HtmlRenderer
{
    private static final Logger log = Logger.getLogger(HtmlFormatRenderer.class.getName());

    private static final Object[] EMPTY_ARGS = new Object[0];
    
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

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
    }

    @Override
    public void encodeChildren(FacesContext facescontext, UIComponent uicomponent) throws IOException
    {
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, UIOutput.class);

        String text = getOutputFormatText(facesContext, component);
        boolean escape;
        if (component instanceof HtmlOutputFormat)
        {
            escape = ((HtmlOutputFormat) component).isEscape();
        }
        else
        {
            escape = AttributeUtils.getBooleanAttribute(component, ComponentAttrs.ESCAPE_ATTR, true);
        }
        if (text != null)
        {
            ResponseWriter writer = facesContext.getResponseWriter();
            boolean span = false;

            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                long commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
                
                if ( (commonPropertiesMarked & ~(CommonHtmlAttributes.ESCAPE)) > 0)
                {
                    span = true;
                    writer.startElement(HTML.SPAN_ELEM, component);
                    HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                }
                else if (CommonHtmlAttributesUtil.isIdRenderingNecessary(component))
                {
                    span = true;
                    writer.startElement(HTML.SPAN_ELEM, component);
                    writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext), null);
                }
                
                CommonHtmlAttributesUtil.renderUniversalProperties(writer, commonPropertiesMarked, component);
                CommonHtmlAttributesUtil.renderStyleProperties(writer, commonPropertiesMarked, component);
            }
            else
            {
                if (shouldRenderId(facesContext, component))
                {
                    span = true;
    
                    writer.startElement(HTML.SPAN_ELEM, component);
    
                    HtmlRendererUtils.writeId(writer, component, facesContext);
    
                    HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
    
                }
                else
                {
                    span = HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(writer,component,
                            HTML.SPAN_ELEM,HTML.COMMON_PASSTROUGH_ATTRIBUTES);
                }
            }

            if (escape)
            {
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("renderOutputText writing '" + text + '\'');
                }
                writer.writeText(text, ComponentAttrs.VALUE_ATTR);
            }
            else
            {
                writer.write(text);
            }

            if(span)
            {
                writer.endElement(HTML.SPAN_ELEM);
            }
        }
    }

    private String getOutputFormatText(FacesContext facesContext, UIComponent htmlOutputFormat)
    {
        String pattern = RendererUtils.getStringValue(facesContext, htmlOutputFormat);
        Object[] args = EMPTY_ARGS;
        if (htmlOutputFormat.getChildCount() > 0)
        {
            List<UIParameter> validParams = HtmlRendererUtils.getValidUIParameterChildren(
                    facesContext, htmlOutputFormat.getChildren(), false, false, false);
            if (validParams != null && !validParams.isEmpty())
            {
                List<Object> argsList = new ArrayList<>(validParams.size());
                for (int i = 0, size = validParams.size(); i < size; i++)
                {
                    UIParameter param = validParams.get(i);
                    argsList.add(param.getValue());
                }

                args = argsList.toArray(new Object[argsList.size()]);
            }
        }

        MessageFormat format = new MessageFormat(pattern, facesContext.getViewRoot().getLocale());
        try
        {
            return format.format(args);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Error formatting message of component "
                                  + htmlOutputFormat.getClientId(facesContext) + " "
                                  + ComponentUtils.getPathToComponent(htmlOutputFormat));
            return "";
        }
    }

}
