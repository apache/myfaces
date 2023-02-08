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

import jakarta.faces.component.html.HtmlDoctype;




import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Leonardo Uribe
 */
public class HtmlDoctypeRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlDoctype doctype;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        doctype = new HtmlDoctype();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                doctype.getFamily(),
                doctype.getRendererType(),
                new HtmlDoctypeRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        writer = null;
    }
    
    @Test
    public void testHtmlPropertyPassTru() throws Exception
    { 
        HtmlRenderedAttr[] attrs = {
                new HtmlRenderedAttr("rootElement","rootElement", "rootElement"),
                new HtmlRenderedAttr("public","-//W3C//DTD XHTML 1.0 Transitional//EN", "\"-//W3C//DTD XHTML 1.0 Transitional//EN\""),
                new HtmlRenderedAttr("system","http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"")
        };

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                doctype, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
        
        Assertions.assertTrue(
                writer.getWriter().toString().contains("<!DOCTYPE rootElement PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"));
    }
    
    public void testHtml5Doctype() throws Exception
    {
        doctype.setRootElement("html");
        
        doctype.encodeAll(facesContext);
        facesContext.renderResponse();
        
        Assertions.assertTrue(writer.getWriter().toString().contains("<!DOCTYPE html>"));
    }

}
