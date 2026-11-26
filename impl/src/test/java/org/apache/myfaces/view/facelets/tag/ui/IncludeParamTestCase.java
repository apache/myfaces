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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.renderkit.html.HtmlCompositeComponentRenderer;
import org.apache.myfaces.renderkit.html.HtmlCompositeFacetRenderer;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;
import org.apache.myfaces.util.lang.FastWriter;
import org.junit.jupiter.api.Test;

class IncludeParamTestCase extends AbstractFaceletTestCase
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
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "jakarta.faces.Text",
                new HtmlTextRenderer());
        renderKit.addRenderer(UINamingContainer.COMPONENT_TYPE,
                "jakarta.faces.Composite", new HtmlCompositeComponentRenderer());
        renderKit.addRenderer(UIOutput.COMPONENT_TYPE, 
                "jakarta.faces.CompositeFacet", new HtmlCompositeFacetRenderer());
    }

    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        // For this test we need the a real one, because the Mock does not
        // handle VariableMapper stuff properly and ui:param logic will not work
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    void caching() throws Exception
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
        
        assertTrue(result.contains("<p>Component value: page test2</p>"));
        assertTrue(result.contains("<p>Inline EL value: page test2</p>"));
        
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
        
        assertTrue(result.contains("<p>Component value: page test3</p>"));
        assertTrue(result.contains("<p>Inline EL value: page test3</p>"));

    }

    @Test
    void simpleCompositionParam() throws Exception
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
        assertTrue(result.contains("value1"));
    }

    /**
     * ui:param inside ui:decorate applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope1() throws Exception
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
        assertTrue(result.contains("rightValue"));
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param defined outside should not pass through ui:include, because it occurs
     * on another template context.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope2() throws Exception
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
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param inside ui:include applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope3() throws Exception
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
        assertTrue(result.contains("rightValue"));
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param inside ui:composition applies only to the content of the tag and the
     * referenced template.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope4() throws Exception
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
        assertTrue(result.contains("rightValue"));
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param defined outside should only pass through ui:include if the param is declared.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope5() throws Exception
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
        assertTrue(result.contains("rightValue"));
        assertTrue(result.contains("right2Value"));
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param defined outside should not pass through composite components, to do
     * that the composite component provide a clean attribute interface.
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope6() throws Exception
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
        assertTrue(result.contains("rightValue"));
        assertFalse(result.contains("doNotPrintValue"));
    }

    /**
     * ui:param should pass through nested ui:decorate or ui:composition constructions, because it is the
     * same template context. Additionally, ui:param declarations should follow the same ordering rules
     * for ui:decorate or ui:composition
     * 
     * @throws Exception
     */
    @Test
    void uIParamTemplateScope7() throws Exception
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
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        assertTrue(result.contains("value4"));
        assertTrue(result.contains("value5"));
        assertTrue(result.contains("valu1e5"));
        assertTrue(result.contains("valu1e6"));
        assertFalse(result.contains("value6"));
    }
}
