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
import jakarta.faces.component.html.HtmlGraphicImage;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
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
public class HtmlImageRendererTest extends AbstractJsfTestCase
{
    private MockResponseWriter writer ;
    private HtmlGraphicImage graphicImage;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        graphicImage = new HtmlGraphicImage();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                graphicImage.getFamily(),
                graphicImage.getRendererType(),
                new HtmlImageRenderer());
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        graphicImage = null;
        writer = null;
    }

    @Test
    public void testRenderDefault() throws Exception
    {
        String imgUrl = "http://www.apache.org/images/feather.gif";
        String src = facesContext.getApplication().getViewHandler().getResourceURL(facesContext, imgUrl);
        
        graphicImage.setId("img1");
        graphicImage.setValue(imgUrl);
        graphicImage.encodeBegin(facesContext);
        graphicImage.encodeChildren(facesContext);
        graphicImage.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        Assertions.assertEquals("<img id=\"img1\" src=\"" + src + "\"/>", output);
    }

    @Test
    public void testHtmlPropertyPassTru() throws Exception
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();
        
        graphicImage.setId("img1");
        graphicImage.setValue("http://www.apache.org/images/feather.gif");
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                graphicImage, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();
        
        graphicImage.setId("img1");
        graphicImage.setValue("http://www.apache.org/images/feather.gif");
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                graphicImage, facesContext, writer, attrs);
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
        graphicImage.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            graphicImage.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            Assertions.assertTrue(output.matches(".+id=\".+\".+"));
            Assertions.assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            Assertions.fail(e.getMessage());
        }
        
    }
}
