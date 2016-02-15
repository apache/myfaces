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

package org.apache.myfaces.view.facelets.tag.ui;

import java.io.StringWriter;
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

/**
 *
 * @author lu4242
 */
public class RemoveTestCase extends FaceletTestCase
{
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
    public void testRemoveMetadata() throws Exception {
        
        request.setAttribute("beanValue", "hello");
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testRemoveMetadata.xhtml");

        Assert.assertNull(root.getFacet(UIViewRoot.METADATA_FACET_NAME));
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();
        
        //System.out.println(sw.toString());
        Assert.assertFalse(sw.toString().contains("ui:remove"));
        Assert.assertFalse(sw.toString().contains("f:metadata"));
        Assert.assertFalse(sw.toString().contains("Should not be here"));
    }    
    

}
