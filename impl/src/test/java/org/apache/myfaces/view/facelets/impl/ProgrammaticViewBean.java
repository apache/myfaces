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
package org.apache.myfaces.view.facelets.impl;


import static jakarta.faces.application.StateManager.IS_BUILDING_INITIAL_STATE;

import java.io.IOException;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

/* Modelled off of corresponding TCK test */
@View("/test.xhtml")
@ApplicationScoped
public class ProgrammaticViewBean extends Facelet {

    @Override
    public void apply(FacesContext facesContext, UIComponent root) throws IOException {

        if (!facesContext.getAttributes().containsKey(IS_BUILDING_INITIAL_STATE)) {
            return;
        }

        ComponentBuilder components = new ComponentBuilder(facesContext);
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();

        List<UIComponent> rootChildren = root.getChildren();

        UIOutput output = new UIOutput();
        output.setValue("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        output.getAttributes().put("escape", false);
        rootChildren.add(output);

        HtmlBody body = components.create(HtmlBody.COMPONENT_TYPE);
        rootChildren.add(body);

        HtmlOutputText message = components.create(HtmlOutputText.COMPONENT_TYPE);
        message.setId("messageId");
        message.setValue("Success!");
        body.getChildren().add(message);

        HtmlOutputText cdiBeanMessage = new HtmlOutputText();
        cdiBeanMessage.setValueExpression("value", expressionFactory.createValueExpression(elContext, "CDI Bean Name: #{test1581Bean.name}", String.class));
        cdiBeanMessage.setId("cdiId");
        body.getChildren().add(cdiBeanMessage);

        output = new UIOutput();
        output.setValue("</html>");
        output.getAttributes().put("escape", false);
        rootChildren.add(output);

    }

    private static class ComponentBuilder {
        FacesContext facesContext;

        ComponentBuilder(FacesContext facesContext) {
            this.facesContext = facesContext;
        }

       @SuppressWarnings("unchecked")
       <T> T create(String componentType) {
           return (T) facesContext.getApplication().createComponent(facesContext, componentType, null);
       }
    }

}
