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

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.inject.Named;

/**
 *
 * @author Leonardo Uribe
 */
@Named("componentBindingVDLBean5")
@RequestScoped
public class ComponentBindingVDLBean5
{
    private UIPanel panel;
    
    public UIPanel getPanel()
    {
        if (panel == null)
        {
            panel = new HtmlPanelGroup();
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext.isPostback())
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
            
            ViewDeclarationLanguage vdl = facesContext.getApplication().
                getViewHandler().getViewDeclarationLanguage(
                    facesContext, facesContext.getViewRoot().getViewId());
            
            Map<String, Object> attributes = new HashMap<String, Object>();
            UIComponent cc = vdl.createComponent(facesContext, 
                "http://java.sun.com/jsf/composite/testComposite", 
                "dynComp_3", attributes);
            cc.setId("ccpanel");
            
            Map<String, Object> attributes2 = new HashMap<String, Object>();
            UIComponent text = vdl.createComponent(facesContext,
                "http://java.sun.com/jsf/composite/testComposite", 
                "dynComp_2", attributes2);
            text.setId("component");

            cc.getChildren().add(text);
            panel.getChildren().add(cc);

            if (!facesContext.isPostback())
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
}
