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

import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlInputTextarea;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlTextareaRendererTest extends AbstractFacesTestCase
{
    private MockResponseWriter writer ;
    private HtmlInputTextarea inputTextarea;

    @Override
    @BeforeEach
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
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        inputTextarea = null;
        writer = null;
    }

    @Test
    public void testRenderDefault() throws Exception
    {
        inputTextarea.encodeBegin(facesContext);
        inputTextarea.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        Assertions.assertEquals("<textarea name=\"j_id__v_0\"></textarea>", output);
    }

    @Test
    public void testRenderColsRows() throws Exception
    {
        inputTextarea.setCols(5);
        inputTextarea.setRows(10);
        inputTextarea.encodeBegin(facesContext);
        inputTextarea.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        Assertions.assertEquals("<textarea name=\"j_id__v_0\" cols=\"5\" rows=\"10\"></textarea>", output);
    }
    
    @Test
    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicAttrs();
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                inputTextarea, facesContext, writer, attrs);
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
        inputTextarea.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            inputTextarea.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.matches("(?s).+id=\".+\".+"));
            Assertions.assertTrue(output.matches("(?s).+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
}
