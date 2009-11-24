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
import javax.faces.component.html.HtmlInputTextarea;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;

/**
 * @author Bruno Aranda (latest modification by $Author: baranda $)
 * @version $Revision: 451814 $ $Date: 2006-10-01 22:28:42 +0100 (dom, 01 oct 2006) $
 */
public class HtmlTextareaRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlInputTextarea inputTextarea;

    public HtmlTextareaRendererTest(String name)
    {
        super(name);
    }
    
    public static Test suite() {
        return new TestSuite(HtmlTextareaRendererTest.class);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        inputTextarea = new HtmlInputTextarea();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                inputTextarea.getFamily(),
                inputTextarea.getRendererType(),
                new HtmlTextareaRenderer());

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        inputTextarea = null;
        writer = null;
    }

    public void testRenderDefault() throws Exception
    {
        inputTextarea.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<textarea name=\"j_id0\"></textarea>", output);
    }

    public void testRenderColsRows() throws Exception
    {
        inputTextarea.setCols(5);
        inputTextarea.setRows(10);
        inputTextarea.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        assertEquals("<textarea name=\"j_id0\" cols=\"5\" rows=\"10\"></textarea>", output);
    }
    
    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                inputTextarea, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        inputTextarea.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            inputTextarea.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches("(?s).+id=\".+\".+"));
            assertTrue(output.matches("(?s).+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
}
