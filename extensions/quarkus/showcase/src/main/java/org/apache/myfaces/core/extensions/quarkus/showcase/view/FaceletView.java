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
package org.apache.myfaces.core.extensions.quarkus.showcase.view;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlDoctype;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

@View("/facelet.xhtml")
@ApplicationScoped
public class FaceletView extends Facelet
{

    @Override
    public void apply(FacesContext facesContext, UIComponent parent)
    {
        if (!facesContext.getAttributes().containsKey(StateManager.IS_BUILDING_INITIAL_STATE))
        {
            return;
        }

        var components = new ComponentBuilder(facesContext);
        var rootChildren = parent.getChildren();

        var htmlDoctype = new HtmlDoctype();
        htmlDoctype.setRootElement("html");
        rootChildren.add(htmlDoctype);

        UIOutput output = new UIOutput();
        output.setValue("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        output.getAttributes().put("escape", false);
        rootChildren.add(output);

        HtmlBody body = components.create(HtmlBody.COMPONENT_TYPE);
        rootChildren.add(body);

        HtmlForm form = components.create(HtmlForm.COMPONENT_TYPE);
        form.setId("form");
        body.getChildren().add(form);

        HtmlOutputText message = components.create(HtmlOutputText.COMPONENT_TYPE);
        message.setId("message");

        HtmlCommandButton actionButton = components.create(HtmlCommandButton.COMPONENT_TYPE);
        actionButton.setId("button");
        actionButton.addActionListener(
                e -> message.setValue("Hello, World! Welcome to Faces 4.0 on Jakarta EE 10"));
        actionButton.setValue("Greet");

        form.getChildren().add(actionButton);

        body.getChildren().add(message);

        output = new UIOutput();
        output.setValue("</html>");
        output.getAttributes().put("escape", false);
        rootChildren.add(output);
    }

    private static class ComponentBuilder
    {
        FacesContext facesContext;

        ComponentBuilder(FacesContext facesContext)
        {
            this.facesContext = facesContext;
        }

        @SuppressWarnings("unchecked")
        <T> T create(String componentType)
        {
            try
            {
                return (T) facesContext.getApplication().createComponent(componentType);
            }
            catch (ClassCastException e)
            {
                throw new IllegalArgumentException("Component type " + componentType + " is not valid.", e);
            }
        }
    }
}