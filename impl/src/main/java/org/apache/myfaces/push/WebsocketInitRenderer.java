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

package org.apache.myfaces.push;

import java.io.IOException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;

@JSFRenderer(renderKitId = "HTML_BASIC",
    family = "jakarta.faces.Output",
    type = "org.apache.myfaces.WebsocketInit")
public class WebsocketInitRenderer extends Renderer
{

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        super.encodeEnd(facesContext, component); //check for NP
        WebsocketInit init = (WebsocketInit) component;
  
        ResponseWriter writer = facesContext.getResponseWriter();
        // If two websocket share the same channel and scope, share init.
        for (int i = 0; i < init.getUIWebsocketMarkupList().size(); i++)
        {
            String markup = (String) init.getUIWebsocketMarkupList().get(i);
            writer.write(markup);
        }
    }
}
