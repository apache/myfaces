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

import java.util.Random;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

@FacesComponent(value = "com.myapp.UIDynamicFormComponent")
public class UIDynamicFormComponent extends UIComponentBase implements
        SystemEventListener
{

    //
    // Constructor
    //

    public UIDynamicFormComponent()
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
         // Clear the existing tree. 
        this.getChildren().clear();
        
        Integer index = (Integer) this.getAttributes().get("index");
        if (index == null)
        {
            index = 1;
        }
        else
        {
            index = index + 1;
        }
        this.getAttributes().put("index", index);
            
        // Start building the new component tree with formRoot as parent.
        Random random = new Random() ;
        int n = random.nextInt(9)+1;

        for(int i = 0; i < n; i++)
        {
            UIOutput input = new UIOutput();
            input.setId("input_"+index+"_"+i);
            this.getChildren().add(input);
        }
    }
}
