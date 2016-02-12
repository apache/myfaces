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

package org.apache.myfaces.view.facelets.tag.jsf.html;

import java.io.StringWriter;
import javax.el.MethodExpression;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;

import org.apache.myfaces.renderkit.html.HtmlButtonRenderer;
import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlGridRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

public class HtmlTestCase extends FaceletTestCase {
    
    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE,
                UIViewRoot.class.getName());
        application.addComponent(HtmlCommandButton.COMPONENT_TYPE,
                HtmlCommandButton.class.getName());
        application.addComponent(HtmlForm.COMPONENT_TYPE,
                HtmlForm.class.getName());
        application.addComponent(HtmlPanelGrid.COMPONENT_TYPE,
                HtmlPanelGrid.class.getName());
        application.addComponent(HtmlOutputText.COMPONENT_TYPE,
                HtmlOutputText.class.getName());
        application.addComponent(UIParameter.COMPONENT_TYPE,
                UIParameter.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY,
                "javax.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "javax.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(HtmlCommandButton.COMPONENT_FAMILY,
                "javax.faces.Button", new HtmlButtonRenderer());
        renderKit.addRenderer(HtmlPanelGrid.COMPONENT_FAMILY,
                "javax.faces.Grid", new HtmlGridRenderer());
    }    
    
    @Test
    public void testCommandComponent() throws Exception {
        request.getSession().setAttribute("test", new MockBean());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "componentOwner.xml");
        
        UIComponent c = root.findComponent("cmd");
        Assert.assertNotNull("cmd", c);
        
        Object v = c.getAttributes().get("id");
        Assert.assertEquals("id", "cmd", v);
        
        ActionSource2 as2 = (ActionSource2) c;
        MethodExpression me = as2.getActionExpression();
        Assert.assertNotNull("method", me);
        
        String result = (String) me.invoke(facesContext.getELContext(), null);
        //System.out.println(result);
    }
    
    @Test
    public void testCommandButton() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "commandButton.xml");
        
        UIComponent c = root.findComponent("form:button");
        Assert.assertNotNull("button", c);
        
        Object v = c.getAttributes().get("id");
        Assert.assertEquals("id", "button", v);
    }

    @Test
    public void testPanelGrid() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "panelGrid.xml");
    }
    
    @Test
    public void testEmptyHtml() throws Exception {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testEmptyHtmlAttribute.xhtml");
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        Assert.assertTrue(sw.toString().contains("alt=\"\""));
    }
            

}
