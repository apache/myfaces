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

import java.util.Iterator;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.myapp.UIInputComponent")
public class UIInputComponent extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UIInputComponent()
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

            UIComponent originalComponent = getChildren().remove(0);

            HtmlPanelGroup panelGroup = new HtmlPanelGroup();
            panelGroup
                    .setStyle("border: 1px dashed blue; padding: 5px; margin: 5px");
            getChildren().add(panelGroup);

            // Move original HtmlInputText inside new HtmlPanelGroup

            panelGroup.getChildren().add(originalComponent);
        }
        else
        {
            // If the algorithm do a refresh, the inputText is added again, but since it is inside
            // the HtmlPanelGroup, it is not affected by the c:if add/delete algorithm. We need to
            // remove that duplicate.
            for (Iterator<UIComponent> it = getChildren().iterator(); it
                    .hasNext();)
            {
                if (it.next() instanceof HtmlInputText)
                {
                    it.remove();
                }
            }
        }
    }
}
