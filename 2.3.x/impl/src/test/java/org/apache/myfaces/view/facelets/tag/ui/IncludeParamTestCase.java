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

package org.apache.myfaces.view.facelets.tag.ui;

import javax.el.ExpressionFactory;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.renderkit.html.HtmlCompositeComponentRenderer;
import org.apache.myfaces.renderkit.html.HtmlCompositeFacetRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.impl.FaceletCompositionContextImpl;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.util.FastWriter;
import org.junit.Assert;
import org.junit.Test;

public class IncludeParamTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        application.addComponent(UIViewRoot.COMPONENT_TYPE, UIViewRoot.class
                .getName());
        application.addComponent(HtmlOutputText.COMPONENT_TYPE,
                HtmlOutputText.class.getName());
        application.addComponent(UINamingContainer.COMPONENT_TYPE, 
                UINamingContainer.class.getName());
        application.addComponent(UIPanel.COMPONENT_TYPE, 
                UIPanel.class.getName());
    }

    @Override
    protected void setupConvertersAndValidators() throws Exception
    {
    }

    @Override
    protected void setupRenderers() throws Exception
    {
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "javax.faces.Text",
                new HtmlTextRenderer());
        renderKit.addRenderer(UINamingContainer.COMPONENT_TYPE,
                "javax.faces.Composite", new HtmlCompositeComponentRenderer());
        renderKit.addRenderer(UIOutput.COMPONENT_TYPE, 
                "javax.faces.CompositeFacet", new HtmlCompositeFacetRenderer());
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        // For this test we need the a real one, because the Mock does not
        // handle VariableMapper stuff properly and ui:param logic will not work
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testCaching() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();

        request.setAttribute("test", "test2.xml");
        
        //https://facelets.dev.java.net/issues/show_bug.cgi?id=117
        
        // test1.xml
        // <ui:composition xmlns="http://www.w3.org/1999/xhtml"
        //      xmlns:ui="http://java.sun.com/jsf/facelets">
        //   <ui:include src="#{test}"/>
        //</ui:composition>
        
        // test2.xml
        //<ui:composition xmlns="http://www.w3.org/1999/xhtml"
        //    xmlns:ui="http://java.sun.com/jsf/facelets"
        //    xmlns:h="http://java.sun.com/jsf/html"
        //    template="test0.xml">
        //  <ui:param name="testParam" value="page test2" />
        //</ui:composition>
        
        // test0.xml
        //<ui:composition xmlns="http://www.w3.org/1999/xhtml"
        //    xmlns:ui="http://java.sun.com/jsf/facelets"
        //    xmlns:h="http://java.sun.com/jsf/html">
        //  <p>Component value: <h:outputText value="#{testParam}" /></p>
        //  <p>Inline EL value: #{testParam}</p> 
        //</ui:composition>
        
        System.out.println("ApplicationImpl:" + facesContext.getApplication().getClass().getName());
        System.out.println("ExpressionFactory:" + facesContext.getApplication().getExpressionFactory().getClass().getName());
        
        vdl.buildView(facesContext, root, "test1.xml");

        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        
        String result = fw.toString();
        
        Assert.assertTrue("Output:" + result, result.contains("<p>Component value: page test2</p>"));
        Assert.assertTrue("Output:" + result,result.contains("<p>Inline EL value: page test2</p>"));
        
        //System.out.println(fw);

        ComponentSupport.removeTransient(root);

        request.setAttribute("test", "test3.xml");

        facesContext.setViewRoot(facesContext.getApplication().getViewHandler()
                .createView(facesContext, "/test"));
        root = facesContext.getViewRoot();

        vdl.buildView(facesContext, root, "test1.xml");

        fw = new FastWriter();
        rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        //System.out.println(fw);
        
        result = fw.toString();
        
        Assert.assertTrue("Output:" + result, result.contains("<p>Component value: page test3</p>"));
        Assert.assertTrue("Output:" + result, result.contains("<p>Inline EL value: page test3</p>"));

    }
    
    @Test
    public void testSimpleCompositionParam() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "simpleCompositionParam.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output:" + result, result.contains("value1"));
    }

    /**
     * ui:param inside ui:decorate applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope1.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param defined outside should not pass through ui:include, because it occurs
     * on another template context.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope2() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope2.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param inside ui:include applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope3() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope3.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param inside ui:composition applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope4() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope4.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param defined outside should only pass through ui:include if the param is declared.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope5() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope5.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertTrue("Output should contain 'right2Value'", result.contains("right2Value"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param defined outside should not pass through composite components, to do
     * that the composite component provide a clean attribute interface.
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope6() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope6.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'rightValue'", result.contains("rightValue"));
        Assert.assertFalse("Output should not contain 'doNotPrintValue'", result.contains("doNotPrintValue"));
    }
    
    /**
     * ui:param should pass through nested ui:decorate or ui:composition constructions, because it is the
     * same template context. Additionally, ui:param declarations should follow the same ordering rules
     * for ui:decorate or ui:composition
     * 
     * @throws Exception
     */
    @Test
    public void testUIParamTemplateScope7() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "uiparamtemplatescope7.xhtml");
        
        FastWriter fw = new FastWriter();
        ResponseWriter rw = facesContext.getResponseWriter();
        rw = rw.cloneWithWriter(fw);
        facesContext.setResponseWriter(rw);
        root.encodeAll(facesContext);
        rw.flush();
        
        String result = fw.toString();
        Assert.assertTrue("Output should contain 'value1'", result.contains("value1"));
        Assert.assertTrue("Output should contain 'value2'", result.contains("value2"));
        Assert.assertTrue("Output should contain 'value3'", result.contains("value3"));
        Assert.assertTrue("Output should contain 'value4'", result.contains("value4"));
        Assert.assertTrue("Output should contain 'value5'", result.contains("value5"));
        Assert.assertTrue("Output should contain 'valu1e5'", result.contains("valu1e5"));
        Assert.assertTrue("Output should contain 'valu1e6'", result.contains("valu1e6"));
        Assert.assertFalse("Output should not contain 'value6'", result.contains("value6"));
    }
}
