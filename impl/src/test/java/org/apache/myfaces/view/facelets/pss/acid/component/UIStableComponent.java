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

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.myapp.UIStableComponent")
public class UIStableComponent extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UIStableComponent()
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

        if (FacesContext.getCurrentInstance().isPostback())
        {
            return;
        }

        if (FacesContext.getCurrentInstance().getMaximumSeverity() != null)
        {
            return;
        }

        HtmlInputText inputText1 = new HtmlInputText();
        inputText1.setValue("1");
        getChildren().add(inputText1);

        HtmlInputText inputText2 = new HtmlInputText();
        inputText2.setValue("2");
        getChildren().add(inputText2);

        HtmlInputText inputText3 = new HtmlInputText();
        inputText3.setId("text3");
        inputText3.setRequired(true);
        getChildren().add(inputText3);
    }
}
