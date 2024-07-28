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

import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlForm;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HtmlButtonRendererTest extends AbstractFacesTestCase {

    private MockResponseWriter writer;
    private HtmlCommandButton commandButton;
    private HtmlForm form;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        commandButton = new HtmlCommandButton();
        form = new HtmlForm();
        commandButton.setParent(form);
        
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                commandButton.getFamily(),
                commandButton.getRendererType(),
                new HtmlButtonRenderer());
        facesContext.getRenderKit().addRenderer(
                form.getFamily(),
                form.getRendererType(),
                new HtmlFormRenderer());
        facesContext.getRenderKit().addRenderer(
                "jakarta.faces.Input",
                "jakarta.faces.Hidden",
                new HtmlHiddenRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }
    
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        writer = null;
    }

    @Test
    public void testJSNotAllowedHtmlPropertyPassTru() throws Exception {
        HtmlRenderedAttr[] attrs = {
            //_AccesskeyProperty
            new HtmlRenderedAttr("accesskey"),
            //_UniversalProperties
            new HtmlRenderedAttr("dir"), 
            new HtmlRenderedAttr("lang"), 
            new HtmlRenderedAttr("title"),
            new HtmlRenderedAttr("role"),

            /* If js is set to false, no need to bother over event attributes
            //_FocusBlurProperties
            new HtmlRenderedAttr("onfocus"), 
            new HtmlRenderedAttr("onblur"),
            //_ChangeSelectProperties
            new HtmlRenderedAttr("onchange"), 
            new HtmlRenderedAttr("onselect"),
            //_EventProperties
            new HtmlRenderedAttr("onclick", "onclick", "onclick=\""), 
            new HtmlRenderedAttr("ondblclick"), 
            new HtmlRenderedAttr("onkeydown"), 
            new HtmlRenderedAttr("onkeypress"),
            new HtmlRenderedAttr("onkeyup"), 
            new HtmlRenderedAttr("onmousedown"), 
            new HtmlRenderedAttr("onmousemove"), 
            new HtmlRenderedAttr("onmouseout"),
            new HtmlRenderedAttr("onmouseover"), 
            new HtmlRenderedAttr("onmouseup"),
            */
            
            //_StyleProperties
            new HtmlRenderedAttr("style"), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            //_TabindexProperty
            new HtmlRenderedAttr("tabindex")
        };
        
        MockServletContext servletContext = new MockServletContext();
        MockExternalContext mockExtCtx = new MockExternalContext(servletContext, 
                new MockHttpServletRequest(), new MockHttpServletResponse());
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(mockExtCtx);
        facesContext.setExternalContext(mockExtCtx);
    
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                commandButton, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testAllowedHtmlPropertyPassTru() throws Exception {
           HtmlRenderedAttr[] attrs = {
               //_AccesskeyProperty
               new HtmlRenderedAttr("accesskey"),
               //_UniversalProperties
               new HtmlRenderedAttr("dir"), 
               new HtmlRenderedAttr("lang"), 
               new HtmlRenderedAttr("title"),
               new HtmlRenderedAttr("role"),
               //_FocusBlurProperties
               new HtmlRenderedAttr("onfocus"), 
               new HtmlRenderedAttr("onblur"),
               //_ChangeSelectProperties
               new HtmlRenderedAttr("onchange"), 
               new HtmlRenderedAttr("onselect"),
               //_EventProperties
               //onclick is not allowed in this test case
               new HtmlRenderedAttr("onclick", "onclick", "onclick=\""),
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
               new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
               //_TabindexProperty
               new HtmlRenderedAttr("tabindex")
           };
        
        MockServletContext servletContext = new MockServletContext();
        MockExternalContext mockExtCtx = new MockExternalContext(servletContext, 
                new MockHttpServletRequest(), new MockHttpServletResponse());
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(mockExtCtx);
        facesContext.setExternalContext(mockExtCtx);
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                commandButton, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }

    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        commandButton.addClientBehavior("focus", new AjaxBehavior());
        try 
        {
            commandButton.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.matches(".+id=\".+\".+"));
            Assertions.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
    
    /**
     * If a h:commandButton has any UIParameter children, he should
     * render them with a renderer of family jakarta.faces.Input and
     * renderer type jakarta.faces.Hidden.
     * If the disable attribute of a child UIParameter is true,
     * he should be ignored.
     * @throws Exception
     */
    @Test
    public void testCommandButtonRendersNotDisabledUIParameters() throws Exception
    {
        UIParameter param1 = new UIParameter();
        param1.setName("param1");
        param1.setValue("value1");
        param1.setDisable(true);
        UIParameter param2 = new UIParameter();
        param2.setName("param2");
        param2.setValue("value2");
        commandButton.getChildren().add(param1);
        commandButton.getChildren().add(param2);
        
        commandButton.setValue("commandButton");
        
        commandButton.encodeAll(facesContext);
        String output = writer.getWriter().toString();
        Assertions.assertFalse(output.contains("param1"));
        Assertions.assertFalse(output.contains("value1"));
        Assertions.assertTrue(output.contains("param2"));
        Assertions.assertTrue(output.contains("value2"));
    }
    
}
