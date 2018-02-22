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
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.myapp.UIToggleComponent")
public class UIToggleComponent extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UIToggleComponent()
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

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.isPostback())
        {
            HtmlOutputText text1 = new HtmlOutputText();
            text1.setId(facesContext.getViewRoot().createUniqueId());
            text1.setValue("Manually added child 1<br/>");
            text1.setEscape(false);
            getChildren().add(text1);
            
            HtmlOutputText text2 = new HtmlOutputText();
            text2.setId(facesContext.getViewRoot().createUniqueId());
            text2.setValue("Manually added child 2<br/>");
            text2.setEscape(false);
            getChildren().add(text2);

            //<h:outputText value="Manually added child 1&lt;br/&gt;" escape="false"/>
            //<h:outputText value="Manually added child 2&lt;br/&gt;" escape="false"/>
        }

        UIComponent component = getChildren().remove(0);
        getChildren().add(component);
    }
}
