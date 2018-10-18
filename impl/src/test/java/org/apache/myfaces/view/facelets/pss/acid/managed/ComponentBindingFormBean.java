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

import java.util.Random;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.html.HtmlPanelGroup;
import javax.inject.Named;

/**
 *
 */
@Named("componentBindingFormBean")
@RequestScoped
public class ComponentBindingFormBean
{
    private boolean rebuildDone = false;
    
    private UIPanel panel;
    
    public UIPanel getPanel()
    {
        if (!rebuildDone)
        {
            rebuildForm();
        }
        return panel;
    }
    
    public void rebuildForm()
    {
        if (panel == null)
        {
            panel = new HtmlPanelGroup();
            //panel.setTransient(true);
            panel.setId("formRoot");            
        }
        
         // Clear the existing tree. 
        panel.getChildren().clear();
            
        // Start building the new component tree with formRoot as parent.
        Random random = new Random() ;
        int n = random.nextInt(9)+1;

        for(int i = 0; i < n; i++)
        {
            UIOutput input = new UIOutput();
            input.setId("input_"+i);
            panel.getChildren().add(input);
        }
        
        this.rebuildDone = true;
    }
    
    public void forceRebuild()
    {
        rebuildForm();
    }
    
    public void setPanel(UIPanel panel)
    {
        this.panel = panel;
    }
}
