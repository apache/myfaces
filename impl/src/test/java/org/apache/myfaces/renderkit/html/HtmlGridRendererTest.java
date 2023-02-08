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

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGrid;

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
public class HtmlGridRendererTest extends AbstractJsfTestCase
{
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\r\n");

    private MockResponseWriter writer ;
    private HtmlPanelGrid panelGrid;
    private HtmlOutputText colText;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();

        panelGrid = new HtmlPanelGrid();
        colText = new HtmlOutputText();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                panelGrid.getFamily(),
                panelGrid.getRendererType(),
                new HtmlGridRenderer());
        facesContext.getRenderKit().addRenderer(
                colText.getFamily(),
                colText.getRendererType(),
                new HtmlTextRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_FACES_JS", Boolean.TRUE);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        panelGrid = null;
        writer = null;
    }

    @Test
    public void testRenderTable() throws Exception
    {
        UIColumn col1 = new UIColumn();
        HtmlOutputText col1Text = new HtmlOutputText();
        col1Text.setValue("col1Text");

        UIColumn col2 = new UIColumn();
        HtmlOutputText col2Text = new HtmlOutputText();
        col2Text.setValue("col2Text");

        col1.getChildren().add(col1Text);
        col2.getChildren().add(col2Text);
        panelGrid.getChildren().add(col1);
        panelGrid.getChildren().add(col2);

        panelGrid.encodeBegin(facesContext);
        panelGrid.encodeChildren(facesContext);
        panelGrid.encodeEnd(facesContext);
        facesContext.renderResponse();

        String output = writer.getWriter().toString();
        Assertions.assertEquals("<table><tbody>"+
                "<tr><td>col1Text</td></tr>"+
                "<tr><td>col2Text</td></tr>"+
                "</tbody>"+"</table>", output);
    }

    @Test
    public void testHtmlPropertyPassTru() throws Exception 
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateBasicReadOnlyAttrs();

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                panelGrid, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assertions.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testHtmlPropertyPassTruNotRendered() throws Exception 
    { 
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();

        HtmlCheckAttributesUtil.checkRenderedAttributes(
                panelGrid, facesContext, writer, attrs);
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
        panelGrid.addClientBehavior("click", new AjaxBehavior());
        try 
        {
            panelGrid.encodeAll(facesContext);
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
