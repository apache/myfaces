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
package org.apache.myfaces.view.facelets.pool;

import java.util.HashMap;
import java.util.Map;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 *
 * @author lu4242
 */
@ManagedBean(name="simpleRequestBean")
@RequestScoped
public class SimpleRequestBean
{
    // Simple binding
    private HtmlPanelGroup panel1;
    
    // Simple binding with children
    private HtmlPanelGroup panel2;
    
    // Simple binding with children and createComponent
    private HtmlPanelGroup panel3;
    
    // Simple binding with children and createComponent composite
    private HtmlPanelGroup panel4;
    
    public void validateField(FacesContext context, UIComponent component, Object value)
    {
        //Dummy method to check if the view pool works or not
    }
    
    public HtmlPanelGroup getPanel1()
    {
        if (panel1 == null)
        {
            panel1 = new HtmlPanelGroup();
        }
        return panel1;
    }

    /**
     * @param panel1 the panel1 to set
     */
    public void setPanel1(HtmlPanelGroup panel1)
    {
        this.panel1 = panel1;
    }
    
    public HtmlPanelGroup getPanel2()
    {
        if (panel2 == null)
        {
            panel2 = new HtmlPanelGroup();
            UIOutput text = new HtmlOutputText();
            text.setValue("added component through binding");
            panel2.getChildren().add(text);
        }
        return panel2;
    }

    /**
     * @param panel1 the panel1 to set
     */
    public void setPanel2(HtmlPanelGroup panel2)
    {
        this.panel2 = panel2;
    }

    /**
     * @return the panel3
     */
    public HtmlPanelGroup getPanel3()
    {
        if (panel3 == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            ViewDeclarationLanguage vdl = facesContext.getApplication().
                getViewHandler().getViewDeclarationLanguage(
                    facesContext, facesContext.getViewRoot().getViewId());
            
            panel3 = new HtmlPanelGroup();
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("src", "/staticPageBinding3_1.xhtml");
            UIComponent component = vdl.createComponent(facesContext, 
                "http://java.sun.com/jsf/facelets", 
                "include", attributes);
            panel3.getChildren().add(component);

        }
        return panel3;
    }

    /**
     * @param panel3 the panel3 to set
     */
    public void setPanel3(HtmlPanelGroup panel3)
    {
        this.panel3 = panel3;
    }

    /**
     * @return the panel4
     */
    public HtmlPanelGroup getPanel4()
    {
        if (panel4 == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            ViewDeclarationLanguage vdl = facesContext.getApplication().
                getViewHandler().getViewDeclarationLanguage(
                    facesContext, facesContext.getViewRoot().getViewId());
            
            panel4 = new HtmlPanelGroup();

            Map<String, Object> attributes = new HashMap<String, Object>();
            UIComponent cc = vdl.createComponent(facesContext, 
                "http://java.sun.com/jsf/composite/testComposite", 
                "dynComp_1", attributes);
            UIOutput text = (UIOutput) facesContext.getApplication().
                createComponent(UIOutput.COMPONENT_TYPE);
            text.setValue("Dynamically added header");
            cc.getFacets().put("header", text);
            panel4.getChildren().add(cc);            
        }
        return panel4;
    }

    /**
     * @param panel4 the panel4 to set
     */
    public void setPanel4(HtmlPanelGroup panel4)
    {
        this.panel4 = panel4;
    }
}
