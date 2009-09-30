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

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputFormat;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared_impl.renderkit.JSFAttr;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlTextRendererBase;

/**
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.Format")
public class HtmlFormatRenderer extends HtmlRenderer
{
    private static final Log log = LogFactory.getLog(HtmlFormatRenderer.class);

    private static final Object[] EMPTY_ARGS = new Object[0];

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
        boolean isEscape;
        if (component instanceof HtmlOutputFormat)
        {
            isEscape = ((HtmlOutputFormat) component).isEscape();
        }
        else
        {
            isEscape = RendererUtils.getBooleanAttribute(component, JSFAttr.ESCAPE_ATTR, true);
        }
        HtmlTextRendererBase.renderOutputText(facesContext, component, text, isEscape);
    }

    private String getOutputFormatText(FacesContext facesContext, UIComponent htmlOutputFormat)
    {
        String pattern = RendererUtils.getStringValue(facesContext, htmlOutputFormat);
        Object[] args;
        if (htmlOutputFormat.getChildCount() == 0)
        {
            args = EMPTY_ARGS;
        }
        else
        {
            List<Object> argsList = new ArrayList<Object>();
            for (UIComponent child : htmlOutputFormat.getChildren())
            {
                if (child instanceof UIParameter)
                {
                    // check for the disable attribute (since 2.0)
                    if (((UIParameter) child).isDisable())
                    {
                        // ignore this UIParameter and continue
                        continue;
                    }
                    argsList.add(((UIParameter)child).getValue());
                }
            }
            
            args = argsList.toArray(new Object[argsList.size()]);
        }

        MessageFormat format = new MessageFormat(pattern, facesContext.getViewRoot().getLocale());
        try
        {
            return format.format(args);
        }
        catch (Exception e)
        {
            log.error("Error formatting message of component " + htmlOutputFormat.getClientId(facesContext));
            return "";
        }
    }

}
