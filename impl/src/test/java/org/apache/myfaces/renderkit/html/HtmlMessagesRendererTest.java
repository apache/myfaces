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
import jakarta.faces.FactoryFinder;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.html.HtmlMessages;

import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.test.base.junit.AbstractJsfConfigurableMockTestCase;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlMessagesRendererTest extends AbstractJsfConfigurableMockTestCase
{
    private static final String ERROR_CLASS = "errorClass";
    private static final String WARN_CLASS = "warnClass";
    private static final String INFO_CLASS = "infoClass";

    private HtmlMessages messages;
    private MockResponseWriter writer;

    public HtmlMessagesRendererTest()
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

    public void setUp() throws Exception
    {
        super.setUp();
        messages = new HtmlMessages();

        writer = new MockResponseWriter(new StringWriter(), null, null);
        facesContext.setResponseWriter(writer);

        facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                messages.getFamily(),
                messages.getRendererType(),
                new HtmlMessagesRenderer());
        
        facesContext.getAttributes().put("org.apache.myfaces.RENDERED_JSF_JS", Boolean.TRUE);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        messages = null;
        writer = null;
    }

    @Test
    public void testHtmlPropertyPassTru() throws Exception
    {
        HtmlRenderedAttr[] attrs = {
            //_EventProperties
            new HtmlRenderedAttr("onclick",2), 
            new HtmlRenderedAttr("ondblclick",2), 
            new HtmlRenderedAttr("onkeydown",2), 
            new HtmlRenderedAttr("onkeypress",2),
            new HtmlRenderedAttr("onkeyup",2), 
            new HtmlRenderedAttr("onmousedown",2), 
            new HtmlRenderedAttr("onmousemove",2), 
            new HtmlRenderedAttr("onmouseout",2),
            new HtmlRenderedAttr("onmouseover",2), 
            new HtmlRenderedAttr("onmouseup",2),
            //_StyleProperties
            new HtmlRenderedAttr("styleClass", "styleClass", "class=\"styleClass\""),
            new HtmlRenderedAttr("style"),
            new HtmlRenderedAttr("role"),
            new HtmlRenderedAttr("warnClass", "warnClass", "class=\"warnClass\"",2),
            new HtmlRenderedAttr("warnStyle", "warnStyle", "style=\"warnStyle\"",2)
        };
        
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));
        facesContext.addMessage("test2", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary2", "detailWarnSummary2"));        

        messages.setErrorClass(ERROR_CLASS);
        messages.setWarnClass(WARN_CLASS);
        messages.setInfoClass(INFO_CLASS);
        messages.setWarnStyle("warnStyle");
        
        messages.setLayout("table");
        //messages.setStyle("left: 48px; top: 432px; position: absolute");
        
        MockResponseWriter writer = (MockResponseWriter)facesContext.getResponseWriter();
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                messages, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
    
    @Test
    public void testRenderSpanOnlyWhenNecessary1() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));
        messages.encodeEnd(facesContext);
        facesContext.renderResponse();
        String output = writer.getWriter().toString();
        Assert.assertTrue(output.contains("warnSumary"));
        Assert.assertTrue(!output.contains("span"));
    }
    
    @Test
    public void testRenderSpanOnlyWhenNecessary2() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));
        messages.setLayout("table");
        messages.encodeEnd(facesContext);
        facesContext.renderResponse();
        String output = writer.getWriter().toString();
        Assert.assertTrue(output.contains("warnSumary"));
        Assert.assertTrue(!output.contains("span"));
    }
    
    @Test
    public void testRenderSpanOnlyWhenNecessary3() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));
        messages.setId("msgPanel");
        messages.encodeEnd(facesContext);
        facesContext.renderResponse();
        String output = writer.getWriter().toString();
        Assert.assertTrue(output.contains("warnSumary"));
        Assert.assertTrue(!output.contains("span"));
    }
    
    /**
     * It should output the class on li
     * @throws Exception
     */
    @Test
    public void testRenderSpanOnlyWhenNecessary4() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_FATAL, "fatalSumary", "detailFatalSummary"));
        messages.setId("msgPanel");
        messages.setFatalClass("fatalClass");
        messages.encodeEnd(facesContext);
        facesContext.renderResponse();
        String output = writer.getWriter().toString();
        Assert.assertTrue(output.contains("fatalSumary"));
        Assert.assertTrue(output.contains("li class=\"fatalClass\""));
        Assert.assertTrue(!output.contains("span"));
    }
    
    /**
     * It should output the class on td
     * @throws Exception
     */
    @Test
    public void testRenderSpanOnlyWhenNecessary5() throws Exception
    {
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_FATAL, "fatalSumary", "detailFatalSummary"));
        messages.setId("msgPanel");
        messages.setLayout("table");
        messages.setFatalClass("fatalClass");
        messages.encodeEnd(facesContext);
        facesContext.renderResponse();
        String output = writer.getWriter().toString();
        Assert.assertTrue(output.contains("fatalSumary"));
        Assert.assertTrue(output.contains("td class=\"fatalClass\""));
        Assert.assertTrue(!output.contains("span"));
    }
    
    @Test
    public void testHtmlPropertyPassTruNotRendered() throws Exception
    {
        HtmlRenderedAttr[] attrs = HtmlCheckAttributesUtil.generateAttrsNotRenderedForReadOnly();
        
        facesContext.addMessage("test1", new FacesMessage(FacesMessage.SEVERITY_WARN, "warnSumary", "detailWarnSummary"));

        messages.setErrorClass(ERROR_CLASS);
        messages.setWarnClass(WARN_CLASS);
        messages.setInfoClass(INFO_CLASS);
        messages.setWarnStyle("warnStyle");
        
        messages.setLayout("table");
        messages.setStyle("left: 48px; top: 432px; position: absolute");
        
        MockResponseWriter writer = (MockResponseWriter)facesContext.getResponseWriter();
        HtmlCheckAttributesUtil.checkRenderedAttributes(
                messages, facesContext, writer, attrs);
        if(HtmlCheckAttributesUtil.hasFailedAttrRender(attrs)) {
            Assert.fail(HtmlCheckAttributesUtil.constructErrorMessage(attrs, writer.getWriter().toString()));
        }
    }
}
