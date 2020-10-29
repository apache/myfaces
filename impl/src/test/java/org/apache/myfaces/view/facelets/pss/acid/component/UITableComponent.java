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
package org.apache.myfaces.view.facelets.pss.acid.component;

import jakarta.faces.application.Application;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

@FacesComponent(value = "com.myapp.UITableComponent")
public class UITableComponent extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UITableComponent()
    {

        setRendererType("testcomponent");

        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        root.subscribeToViewEvent(PreRenderViewEvent.class, this);
    }

    //
    // Public methods
    //

    @Override
    public String getFamily()
    {

        return "com.myapp";
    }

    public boolean isListenerForSource(Object source)
    {

        return (source instanceof UIViewRoot);
    }

    public void processEvent(SystemEvent event) throws AbortProcessingException
    {

        FacesContext context = FacesContext.getCurrentInstance();

        if (!context.isPostback())
        {

            Application application = context.getApplication();

            HtmlDataTable dataTable = new HtmlDataTable();
            dataTable.setVar("_internal");
            dataTable.setValueExpression(
                    "value",
                    application.getExpressionFactory().createValueExpression(
                            context.getELContext(), "#{testManagedBean.list}",
                            Object.class));
            getChildren().add(dataTable);

            UIColumn column = new UIColumn();
            column.setId(context.getViewRoot().createUniqueId());
            dataTable.getChildren().add(column);

            HtmlOutputText outputText = new HtmlOutputText();
            outputText.setId(context.getViewRoot().createUniqueId());
            outputText.setValueExpression(
                    "value",
                    application.getExpressionFactory().createValueExpression(
                            context.getELContext(), "#{_internal}",
                            Object.class));
            column.getChildren().add(outputText);
        }
    }
}
