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

package org.apache.myfaces.view.facelets.compiler;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGrid;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlGridRenderer;
import org.apache.myfaces.renderkit.html.HtmlMenuRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.util.lang.FastWriter;
import org.junit.Assert;
import org.junit.Test;

public class WhitespaceTestCase extends FaceletTestCase {

    private UIComponent target;
    
    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE,
                HtmlForm.class.getName());
        application.addComponent(HtmlPanelGrid.COMPONENT_TYPE,
                HtmlPanelGrid.class.getName());
        application.addComponent(HtmlSelectOneMenu.COMPONENT_TYPE,
                HtmlSelectOneMenu.class.getName());
        application.addComponent(UISelectItem.COMPONENT_TYPE,
                UISelectItem.class.getName()); 
        application.addComponent(HtmlOutputText.COMPONENT_TYPE,
                HtmlOutputText.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "jakarta.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY,
                "jakarta.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(UISelectOne.COMPONENT_FAMILY,
                "jakarta.faces.Menu", new HtmlMenuRenderer());        
        renderKit.addRenderer(UIPanel.COMPONENT_FAMILY,
                "jakarta.faces.Grid", new HtmlGridRenderer());
        
    }    

    @Test
    public void testSelectOneMenu() throws Exception {
        request.setAttribute("test", this);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "selectOne.xml");
        
        Assert.assertNotNull("target binding", target);
        Assert.assertEquals("children", 2, this.target.getChildCount());

        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }
    
    @Test
    public void testPanelGrid() throws Exception {
        request.setAttribute("test", this);

        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "panelGrid.xml");
        
        Assert.assertNotNull("target binding", target);
        Assert.assertEquals("children", 3, this.target.getChildCount());

        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        //System.out.println(fw);
    }

    public void setUp() throws Exception {
        super.setUp();
        this.target = null;
    }

    public UIComponent getTarget() {
        return target;
    }

    public void setTarget(UIComponent target) {
        this.target = target;
    }

}
