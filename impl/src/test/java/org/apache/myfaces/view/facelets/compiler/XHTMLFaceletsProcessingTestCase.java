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

import java.io.StringWriter;

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

import org.junit.Assert;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.impl.element.FaceletsProcessingImpl;

import org.apache.myfaces.renderkit.html.HtmlFormRenderer;
import org.apache.myfaces.renderkit.html.HtmlGridRenderer;
import org.apache.myfaces.renderkit.html.HtmlMenuRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Test;

public class XHTMLFaceletsProcessingTestCase extends FaceletTestCase {

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
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY,
                "javax.faces.Form", new HtmlFormRenderer());
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY,
                "javax.faces.Text", new HtmlTextRenderer());
        renderKit.addRenderer(UISelectOne.COMPONENT_FAMILY,
                "javax.faces.Menu", new HtmlMenuRenderer());        
        renderKit.addRenderer(UIPanel.COMPONENT_FAMILY,
                "javax.faces.Grid", new HtmlGridRenderer());
        
    } 
    
    

    @Override
    protected void setUpExternalContext() throws Exception
    {
        super.setUpExternalContext();

        FaceletsProcessingImpl item = new FaceletsProcessingImpl();
        item.setFileExtension(".xhtml");
        item.setProcessAs(FaceletsProcessingImpl.PROCESS_AS_XHTML);
        RuntimeConfig.getCurrentInstance(externalContext).addFaceletProcessingConfiguration(
            FaceletsProcessingImpl.PROCESS_AS_XHTML, item);
    }

    @Test
    public void testXHTMLProcessing1() throws Exception
    {
        facesContext.getExternalContext().getRequestMap().put("rquote", "\"");
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testXHTMLProcessing1.xhtml");

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        root.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();
        
        Assert.assertTrue("Response contains DOCTYPE declaration", resp.contains("<!DOCTYPE"));
        Assert.assertFalse("Response not contains DOCTYPE html declaration", resp.contains("<!DOCTYPE html>"));
        Assert.assertTrue("Response contains xml declaration", resp.contains("<?xml"));
        Assert.assertTrue("Response contains xml processing instructions", resp.contains("<?name"));
        Assert.assertTrue("Response contains cdata section", resp.contains("<![CDATA["));
        Assert.assertTrue("Response contains cdata section", resp.contains("cdata not consumed"));
        Assert.assertTrue("Response does not escape characters", resp.contains("In this mode, if you put a double quote, it will be replaced by &quot; : &quot"));
        Assert.assertTrue("Response contains comments", resp.contains("<!--"));
        Assert.assertTrue("Response should escape EL and markup", resp.contains("Check EL Escaping &quot; : &quot;"));
        
    }
}
