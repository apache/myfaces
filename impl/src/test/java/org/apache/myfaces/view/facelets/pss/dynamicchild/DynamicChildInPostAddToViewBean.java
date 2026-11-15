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
package org.apache.myfaces.view.facelets.pss.dynamicchild;

import java.io.Serializable;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Adds an input to the view from a {@code postAddToView} listener, i.e. while the view is built.
 */
@Named("dynamicChildInPostAddToViewBean")
@ViewScoped
public class DynamicChildInPostAddToViewBean implements Serializable
{
    public static final String CONTAINER_ID = "form:container";
    public static final String CHILD_ID = "dynamicInput";
    public static final String CHILD_STYLE_CLASS = "dynamic-child";

    private static final long serialVersionUID = 1L;

    private String value;

    public void addViaViewRoot(ComponentSystemEvent event)
    {
        addChild(event.getComponent().findComponent(CONTAINER_ID));
    }

    public void addViaContainer(ComponentSystemEvent event)
    {
        addChild(event.getComponent());
    }

    private void addChild(UIComponent container)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();

        HtmlInputText input = (HtmlInputText) application.createComponent(HtmlInputText.COMPONENT_TYPE);
        input.setId(CHILD_ID);
        input.setStyleClass(CHILD_STYLE_CLASS);
        input.setValueExpression("value", application.getExpressionFactory().createValueExpression(
                context.getELContext(), "#{dynamicChildInPostAddToViewBean.value}", String.class));

        container.getChildren().add(input);
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
