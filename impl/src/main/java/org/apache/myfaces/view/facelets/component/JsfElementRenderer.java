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
package org.apache.myfaces.view.facelets.component;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.CommonPropertyUtils;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared.renderkit.html.HtmlRendererUtils;

/**
 *
 * @author Leonardo Uribe
 */
@JSFRenderer(
    renderKitId = "HTML_BASIC",
    family = "javax.faces.Panel",
    type = "javax.faces.passthrough.Element")
public class JsfElementRenderer extends HtmlRenderer
{

    public boolean getRendersChildren()
    {
        return true;
    }

    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return false;
    }

    public void encodeBegin(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        String elementName = (String) 
            component.getPassThroughAttributes().get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);

        if (elementName == null)
        {
            throw new FacesException("jsf:element with clientId"
                + component.getClientId(facesContext) + " requires 'elementName' passthrough attribute");
        }
        writer.startElement(elementName, component);
        HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
        
        if (isCommonPropertiesOptimizationEnabled(facesContext))
        {
            long commonPropertiesMarked = CommonPropertyUtils.getCommonPropertiesMarked(component);
            if (commonPropertiesMarked > 0)
            {
                CommonPropertyUtils.renderEventProperties(writer, commonPropertiesMarked, component);
                CommonPropertyUtils.renderFocusBlurEventProperties(writer, commonPropertiesMarked, component);
                CommonPropertyUtils.renderChangeSelectEventProperties(writer, commonPropertiesMarked, component);
            }
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.EVENT_HANDLER_ATTRIBUTES);
            HtmlRendererUtils.renderHTMLAttributes(writer, component, HTML.COMMON_FIELD_EVENT_ATTRIBUTES);
        }
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        RendererUtils.renderChildren(facesContext, component);
    }

    public void encodeEnd(FacesContext facesContext, UIComponent component)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        String elementName = (String) component.getPassThroughAttributes().get(
            Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
        writer.endElement(elementName);
    }
}
