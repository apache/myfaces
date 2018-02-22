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

import java.io.IOException;
import java.io.StringWriter;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;

import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlOutputLabel;

import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.application.ApplicationImplJsfTest;

import org.apache.myfaces.test.base.junit4.AbstractJsfConfigurableMockTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class HtmlLabelRendererTest extends AbstractJsfConfigurableMockTestCase
{
    private MockResponseWriter writer;
    private HtmlOutputLabel label;
    
    public HtmlLabelRendererTest()
    {
    }
    
    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();
        
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                "org.apache.myfaces.view.facelets.mock.MockViewDeclarationLanguageFactory");
        FactoryFinder.setFactory(FactoryFinder.FACELET_CACHE_FACTORY,
                "org.apache.myfaces.view.facelets.impl.FaceletCacheFactoryImpl");
        FactoryFinder.setFactory(FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY,
                "org.apache.myfaces.component.search.SearchExpressionContextFactoryImpl");
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                "org.apache.myfaces.application.ApplicationFactoryImpl");
    }
    
    public void setUp() throws Exception {
        
        super.setUp();
        label = new HtmlOutputLabel();
        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);
        
        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                label.getFamily(),
                label.getRendererType(),
                new HtmlLabelRenderer());
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
        
        
        //Application _testApplication = new ApplicationImplJsfTest.TestApplicationWrapper(new ApplicationImpl());
        //facesContext.setApplication(_testApplication);
        //facesContext.getApplication().setSearchExpressionHandler(new SearchExpressionHandlerImpl());
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        writer = null;
        label = null;
    }

    @Test
    public void testHtmlPropertyPassTru() throws Exception
    {
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
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
        };
        
        label.setValue("outputdata");
        //label.setFor("compId"); 
        
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                label, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    /**
     * Gets the page contents.
     * @return the page contents
     */
    protected String getPageContents()
    {
        return ((StringWriter) writer.getWriter()).toString();
    }
    
    @Test
    public void testEscapeUntouched() throws IOException
    {
        label.setId("labelId");
        label.setValue("<span class=\"required\">field label</span>");

        // render label
        label.encodeAll(facesContext);

        String page = getPageContents();
        assertEquals("<label id=\"labelId\">&lt;span class=&quot;required&quot;&gt;field label&lt;/span&gt;</label>", page);
    }

    @Test
    public void testEscapeSetToFalse() throws IOException
    {
        label.setId("labelId");
        label.setValue("<span class=\"required\">field label</span>");
        label.setEscape(false);

        // render label
        label.encodeAll(facesContext);

        String page = getPageContents();
        assertEquals("<label id=\"labelId\"><span class=\"required\">field label</span></label>", page);
    }
    
    /**
     * Components that render client behaviors should always render "id" and "name" attribute
     */
    @Test
    public void testClientBehaviorHolderRendersIdAndName() 
    {
        label.addClientBehavior("keypress", new AjaxBehavior());
        try 
        {
            label.encodeAll(facesContext);
            String output = ((StringWriter) writer.getWriter()).getBuffer().toString();
            assertTrue(output.matches(".+id=\".+\".+"));
            assertTrue(output.matches(".+name=\".+\".+"));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
    }
    
}
