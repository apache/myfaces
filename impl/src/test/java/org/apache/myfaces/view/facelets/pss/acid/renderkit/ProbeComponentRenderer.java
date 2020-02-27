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
package org.apache.myfaces.view.facelets.pss.acid.renderkit;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.render.Renderer;

@FacesRenderer(componentFamily = "com.myapp", rendererType = "testcomponent")
public class ProbeComponentRenderer extends Renderer
{

    //
    // Public methods
    //

    @Override
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {

        context.getResponseWriter()
                .write("<div style=\"border: 1px solid red; margin: 2px\"><div style=\"background-color: #ffc0c0; padding: 2px; margin-bottom: 5px; display:block\">TestComponent::encodeBegin <span style=\"color: #888888\">("
                        + component.getChildCount() + " children)</span></div>");

        super.encodeBegin(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException
    {

        super.encodeEnd(context, component);

        context.getResponseWriter()
                .write("<div style=\"background-color: #ffc0c0; padding: 2px; margin-top: 5px; display:block\">TestComponent::encodeEnd</div></div>");
    }
}
