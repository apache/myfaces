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

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared_impl.renderkit.JSFAttr;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRenderer;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;

/**
 * 
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Anton Koinov
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.Label")
public class HtmlLabelRenderer extends HtmlRenderer
{
    private static final Log log = LogFactory.getLog(HtmlLabelRenderer.class);

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        super.encodeBegin(facesContext, uiComponent); // check for NP

        ResponseWriter writer = facesContext.getResponseWriter();

        encodeBefore(facesContext, writer, uiComponent);

        writer.startElement(HTML.LABEL_ELEM, uiComponent);
        HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.LABEL_PASSTHROUGH_ATTRIBUTES);

        String forAttr = getFor(uiComponent);

        if (forAttr != null)
        {
            writer.writeAttribute(HTML.FOR_ATTR, getClientId(facesContext, uiComponent, forAttr), JSFAttr.FOR_ATTR);
        }
        else
        {
            if (log.isWarnEnabled())
            {
                log.warn("Attribute 'for' of label component with id " + uiComponent.getClientId(facesContext)
                        + " is not defined");
            }
        }

        // MyFaces extension: Render a label text given by value
        // TODO: Move to extended component
        if (uiComponent instanceof ValueHolder)
        {
            String text = RendererUtils.getStringValue(facesContext, uiComponent);
            if (text != null)
            {
                writer.writeText(text, "value");
            }
        }

        writer.flush(); // close start tag

        encodeAfterStart(facesContext, writer, uiComponent);
    }

    /**
     * @throws IOException  
     */
    protected void encodeAfterStart(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    /**
     * @throws IOException  
     */
    protected void encodeBefore(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    protected String getFor(UIComponent component)
    {
        if (component instanceof HtmlOutputLabel)
        {
            return ((HtmlOutputLabel)component).getFor();
        }

        return (String)component.getAttributes().get(JSFAttr.FOR_ATTR);

    }

    protected String getClientId(FacesContext facesContext, UIComponent uiComponent, String forAttr)
    {
        return RendererUtils.getClientId(facesContext, uiComponent, forAttr);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        super.encodeEnd(facesContext, uiComponent); // check for NP

        ResponseWriter writer = facesContext.getResponseWriter();

        encodeBeforeEnd(facesContext, writer, uiComponent);

        writer.endElement(HTML.LABEL_ELEM);

        encodeAfter(facesContext, writer, uiComponent);
    }

    /**
     * @throws IOException  
     */
    protected void encodeBeforeEnd(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    /**
     * @throws IOException  
     */
    protected void encodeAfter(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }
}
