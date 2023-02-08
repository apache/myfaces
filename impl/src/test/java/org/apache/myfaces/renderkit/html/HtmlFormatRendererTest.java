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
import jakarta.faces.component.html.HtmlOutputFormat;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HtmlFormatRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer;
    private HtmlOutputFormat outputFormat;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        outputFormat = new HtmlOutputFormat();
        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                outputFormat.getFamily(),
                outputFormat.getRendererType(),
                new HtmlFormatRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Test
    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = {
                //_UniversalProperties
                new HtmlRenderedAttr("dir"), 
                new HtmlRenderedAttr("lang"), 
                new HtmlRenderedAttr("title"),
                new HtmlRenderedAttr("role"),
                //_StyleProperties
                new HtmlRenderedAttr("style"), 
                new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            };
        
        outputFormat.setValue("outputdata");
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                outputFormat, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();
        
        outputFormat.setValue("outputdata");
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                outputFormat, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * If the disable attribute of a child UIParameter is true,
     * he should be ignored.
     * @throws Exception
     */
    @Test
    public void testDisabledUIParameterNotRendered() throws Exception
    {
        UIParameter param1 = new UIParameter();
        param1.setValue("value1");
        param1.setDisable(true);
        UIParameter param2 = new UIParameter();
        param2.setValue("value2");
        outputFormat.getChildren().add(param1);
        outputFormat.getChildren().add(param2);
        
        outputFormat.setValue("prefix{0}-{1}suffix");
        
        outputFormat.encodeAll(facesContext);
        String output = writer.getWriter().toString();
        Assertions.assertEquals("prefixvalue2-{1}suffix", output);
    }
}
