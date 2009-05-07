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
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.util.ArrayUtils;

/**
 * Renderer used by h:body component
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.Body")
public class HtmlBodyRenderer extends Renderer
{
    //TODO: Move constants to shared HTML class
    private final static String BODY_ELEM = "body";
    private final static String BODY_TARGET = BODY_ELEM;
    
    private final static String ONLOAD_ATTR = "onload";
    private final static String ONUNLOAD_ATTR = "onload";
    private final static String ALINK_ATTR = "alink";
    private final static String VLINK_ATTR = "vlink";
    private final static String LINK_ATTR = "link";
    private final static String TEXT_ATTR = "text";
    private final static String BACKGROUND_ATTR = "background";

    private final static String[] BODY_ATTRIBUTES =
    {
        ONLOAD_ATTR,
        ONUNLOAD_ATTR,
        ALINK_ATTR,
        VLINK_ATTR,
        LINK_ATTR,
        TEXT_ATTR,
        BACKGROUND_ATTR,
        HTML.BGCOLOR_ATTR
    };

    private final static String[] BODY_PASSTHROUGH_ATTRIBUTES =
        (String[]) ArrayUtils.concat(
                HTML.COMMON_PASSTROUGH_ATTRIBUTES,
                BODY_ATTRIBUTES);
    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        super.encodeBegin(facesContext, component); //check for NP

        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(BODY_ELEM, component);
        HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
        HtmlRendererUtils.renderHTMLAttributes(writer, component,
                BODY_PASSTHROUGH_ATTRIBUTES);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        super.encodeEnd(facesContext, component); //check for NP

        ResponseWriter writer = facesContext.getResponseWriter();
        UIViewRoot root = facesContext.getViewRoot();
        for (UIComponent child : root.getComponentResources(facesContext,
                BODY_TARGET))
        {
            child.encodeAll(facesContext);
        }
        writer.endElement(BODY_ELEM);
    }
}
