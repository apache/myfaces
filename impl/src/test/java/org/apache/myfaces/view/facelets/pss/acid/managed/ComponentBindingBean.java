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
package org.apache.myfaces.view.facelets.pss.acid.managed;

import javax.enterprise.context.RequestScoped;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author Leonardo Uribe
 */
@Named("componentBindingBean")
@RequestScoped
public class ComponentBindingBean
{
    private UIPanel panel;
    
    private UIViewParameter viewParam;
    
    public UIPanel getPanel()
    {
        if (panel == null)
        {
            panel = new HtmlPanelGroup();
            if (FacesContext.getCurrentInstance().isPostback())
            {
                // Just try to mess the binding. In theory this does
                // not have effect, because the binding with children
                // or facets should be restored fully.
                UIOutput out2 = new UIOutput();
                out2.setValue("hello2");
                panel.getChildren().add(out2);
            }
            UIOutput out = new UIOutput();
            out.setValue("hello1");
            panel.getChildren().add(out);
            if (!FacesContext.getCurrentInstance().isPostback())
            {
                // Store something into the state
                panel.getAttributes().put("attr1", "value1");
                panel.getChildren().get(0).getAttributes().put("attr2", "value2");
            }
            else
            {
                //Try to mess the state, in theory it should not have effect
                panel.getAttributes().remove("attr1");
                panel.getChildren().get(0).getAttributes().remove("attr2");
            }
        }
        return panel;
    }
    
    public void setPanel(UIPanel panel)
    {
        this.panel = panel;
    }

    /**
     * @return the viewParam
     */
    public UIViewParameter getViewParam()
    {
        return viewParam;
    }

    /**
     * @param viewParam the viewParam to set
     */
    public void setViewParam(UIViewParameter viewParam)
    {
        this.viewParam = viewParam;
    }
}
