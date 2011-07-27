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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.HtmlFormRendererBase;
import org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils;


/**
 *   
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
@JSFRenderer(
    renderKitId="HTML_BASIC",
    family="javax.faces.Form",
    type="javax.faces.Form")
public class HtmlFormRenderer
        extends HtmlFormRendererBase
{    
    //private static final Log log = LogFactory.getLog(HtmlFormRenderer.class);
    
    @Override
    protected void afterFormElementsEnd(FacesContext facesContext,
            UIComponent component) throws IOException {
        super.afterFormElementsEnd(facesContext, component);
        
        ResponseWriter writer = facesContext.getResponseWriter();
        ExternalContext extContext = facesContext.getExternalContext();
        
        // If javascript viewstate is enabled write empty hidden input in forms 
        if (JavascriptUtils.isJavascriptAllowed(extContext) && MyfacesConfig.getCurrentInstance(extContext).isViewStateJavascript()) {
            writer.startElement(HTML.INPUT_ELEM, null);
            writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
            writer.writeAttribute(HTML.NAME_ATTR, HtmlResponseStateManager.VIEW_STATE_PARAM, null);
            writer.writeAttribute(HTML.ID_ATTR, HtmlResponseStateManager.VIEW_STATE_PARAM, null);
            writer.writeAttribute(HTML.VALUE_ATTR, "", null);
            writer.endElement(HTML.INPUT_ELEM);
        }
    }
}
