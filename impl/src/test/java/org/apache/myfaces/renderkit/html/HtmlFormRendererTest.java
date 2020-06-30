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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlForm;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.Assert;

public class HtmlFormRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlForm form;

    public void setUp() throws Exception
    {
        super.setUp();

        //application.setViewHandler(new MockTestViewHandler());
        form = new HtmlForm();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                form.getFamily(),
                form.getRendererType(),
                new HtmlFormRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        form = null;
        writer = null;
    }

    public void testHtmlPropertyPassTru() throws Exception 
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();

        try {
            HtmlCheckAttributesUtil.checkRenderedAttributes(
                    form, facesContext, writer, attrs);
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Assert.fail(sw.toString() + "\nHTML.FORM_PASSTHROUGH_ATTRIBUTES: " + printHTMLAttrs(HTML.FORM_PASSTHROUGH_ATTRIBUTES));
        }
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    public void testHtmlPropertyPassTruNotRendered() throws Exception 
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();

        try {
            HtmlCheckAttributesUtil.checkRenderedAttributes(
                    form, facesContext, writer, attrs);
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Assert.fail(sw.toString() + "\nHTML.FORM_PASSTHROUGH_ATTRIBUTES: " + printHTMLAttrs(HTML.FORM_PASSTHROUGH_ATTRIBUTES));
        }
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    private String printHTMLAttrs(String[] attrs) {
        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < attrs.length; i++) {
            buffer.append(attrs[i]);
            if(i+1 < attrs.length) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        form.addClientBehavior("focus", new AjaxBehavior());
        try 
        {
            form.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assert.assertTrue(output.matches(".+id=\".+\".+"));
            Assert.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
}
