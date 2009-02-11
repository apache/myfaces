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

import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.shared_impl.config.MyfacesConfig;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockExternalContext;
import org.apache.shale.test.mock.MockHttpServletRequest;
import org.apache.shale.test.mock.MockHttpServletResponse;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockResponseWriter;
import org.apache.shale.test.mock.MockServletContext;

public class HtmlButtonRendererTest extends AbstractJsfTestCase {

    private MockResponseWriter writer;
    private HtmlCommandButton commandButton;
    private HtmlForm form;
    
    public HtmlButtonRendererTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(HtmlButtonRendererTest.class);
    }
    
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
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        writer = null;
    }

    public void testJSNotAllowedHtmlPropertyPassTru() throws Exception {
        HtmlRenderedAttr[] attrs = {
            //_AccesskeyProperty
            new HtmlRenderedAttr("accesskey"),
            //_UniversalProperties
            new HtmlRenderedAttr("dir"), 
            new HtmlRenderedAttr("lang"), 
            new HtmlRenderedAttr("title"),
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
            //_StyleProperties
            new HtmlRenderedAttr("style"), 
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            //_TabindexProperty
            new HtmlRenderedAttr("tabindex")
        };
    
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                commandButton, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testAllowedHtmlPropertyPassTru() throws Exception {
           HtmlRenderedAttr[] attrs = {
               //_AccesskeyProperty
               new HtmlRenderedAttr("accesskey"),
               //_UniversalProperties
               new HtmlRenderedAttr("dir"), 
               new HtmlRenderedAttr("lang"), 
               new HtmlRenderedAttr("title"),
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
        servletContext.addInitParameter("org.apache.myfaces.ALLOW_JAVASCRIPT", "true");
        MockExternalContext mockExtCtx = new MockExternalContext(servletContext, 
                new MockHttpServletRequest(), new MockHttpServletResponse());
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(mockExtCtx);
        facesContext.setExternalContext(mockExtCtx);
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                commandButton, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }

    }
}
