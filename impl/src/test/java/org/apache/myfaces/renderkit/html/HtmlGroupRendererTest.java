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
package org.apache.myfaces.renderkit.html;

import java.io.StringWriter;
import javax.faces.component.behavior.AjaxBehavior;

import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import junit.framework.TestSuite;
import org.apache.myfaces.config.MyfacesConfig;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.Assert;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlGroupRendererTest extends AbstractJsfTestCase
{
    private static String PANEL_CHILD_TEXT = "PANEL";

    private MockResponseWriter writer ;
    private HtmlPanelGroup panelGroup;

    public void setUp() throws Exception
    {
        super.setUp();
        
        servletContext.addInitParameter(MyfacesConfig.RENDER_CLIENTBEHAVIOR_SCRIPTS_AS_STRING, "true");

        panelGroup = new HtmlPanelGroup();

        HtmlOutputText panelChildOutputText = new HtmlOutputText();
        panelChildOutputText.setValue(PANEL_CHILD_TEXT);
        panelGroup.getChildren().add(panelChildOutputText);

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                panelGroup.getFamily(),
                panelGroup.getRendererType(),
                new HtmlGroupRenderer());
        facesContext.getRenderKit().addRenderer(
                panelChildOutputText.getFamily(),
                panelChildOutputText.getRendererType(),
                new HtmlTextRenderer());
        facesContext.getRenderKit().addClientBehaviorRenderer(
                AjaxBehavior.BEHAVIOR_ID, new HtmlAjaxBehaviorRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown()throws Exception
    {
        super.tearDown();
        writer = null;
    }
    
    public void testHtmlPropertyPassTru() throws Exception
    { 
        HtmlRenderedAttr[] attrs = {
                //_EventProperties
                new HtmlRenderedAttr("onclick"),
                new HtmlRenderedAttr("ondblclick"), 
                new HtmlRenderedAttr("onkeydown"), 
                new HtmlRenderedAttr("onkeypress"),
                new HtmlRenderedAttr("onkeyup"), 
                new HtmlRenderedAttr("onmousedown"), 
                new HtmlRenderedAttr("onmousemove"), 
                new HtmlRenderedAttr("onmouseout"),
                new HtmlRenderedAttr("onmouseover"), 
                new HtmlRenderedAttr("onmouseup"),
                //_StyleProperties
                new HtmlRenderedAttr("style"), 
                new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\"")
                }; 
            //HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                panelGroup, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();        

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                panelGroup, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testClientBehaviorHolderRendersIdAndNameOutputLink() 
    {
        panelGroup.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            panelGroup.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assert.assertTrue(output.matches(".+id=\".+\".+"));
            Assert.assertTrue(output.contains("myfaces.ab"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
