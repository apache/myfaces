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

import java.util.HashMap;
import java.util.Map;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.view.ViewDeclarationLanguage;

@FacesComponent(value = "com.myapp.UIAddSimpleCCVDL6", createTag=true, 
        namespace="http://testcomponent", tagName="addSimpleCCVDL6")
public class UIAddSimpleCCVDL6 extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UIAddSimpleCCVDL6()
    {
        setRendererType(null);

        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();
        if (root != null)
        {
            root.subscribeToViewEvent(PreRenderViewEvent.class, this);
        }
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
            ViewDeclarationLanguage vdl = facesContext.getApplication().
                getViewHandler().getViewDeclarationLanguage(
                    facesContext, facesContext.getViewRoot().getViewId());

            Map<String, Object> attributes = new HashMap<String, Object>();
            UIComponent component = vdl.createComponent(facesContext, 
                "http://java.sun.com/jsf/composite/testComposite", 
                "dynComp_4", attributes);
            
            Map<String, Object> attributes2 = new HashMap<String, Object>();
            UIComponent text = vdl.createComponent(facesContext,
                "http://java.sun.com/jsf/composite/testComposite", 
                "dynComp_2", attributes2);

            component.getChildren().add(text);
            
            getChildren().add(component);
        }
    }
}
