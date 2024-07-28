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

package org.apache.myfaces.view.facelets.tag.composite;

import java.io.StringWriter;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.event.PostRenderViewEvent;
import jakarta.faces.event.PreRenderViewEvent;

import org.apache.myfaces.config.NamedEventManager;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompositeComponentTestCase extends AbstractFaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
        application.addComponent(CompositeTestComponent.class.getName(), 
                CompositeTestComponent.class.getName());
    }
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter("jakarta.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
    }
    
    
    @Override
    protected void setUpExternalContext() throws Exception
    {
        super.setUpExternalContext();
        
        RuntimeConfig.getCurrentInstance(externalContext).setNamedEventManager(new NamedEventManager());
    }

    /**
     * Test if a child component inside composite component template is
     * rendered.
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleCompositeComponent() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleComposite.xhtml");

        UIComponent panelGroup = root.findComponent("testGroup");
        Assertions.assertNotNull(panelGroup);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("text");
        Assertions.assertNotNull(text);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent.encodeAll(facesContext);
        sw.flush();
        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("value")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
    }

    /**
     * Test simple attribute resolution (not set, default, normal use case).
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleCompositeAttribute() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttribute.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        HtmlOutputText text1 = (HtmlOutputText) facet1.findComponent("text");
        Assertions.assertNotNull(text1);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        text1.pushComponentToEL(facesContext, text1);
        //set on tag
        Assertions.assertEquals("class1", text1.getStyleClass());
        //set as default
        Assertions.assertEquals("background:red", text1.getStyle());
        //Check coercion of attribute using type value
        Assertions.assertEquals(5, compositeComponent1.getAttributes().get("index"));
        //Check default coercion
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{cc.attrs.cols}", Object.class);
        Assertions.assertEquals(1, (int) ve.getValue(facesContext.getELContext()));
        text1.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet2);        
        HtmlOutputText text2 = (HtmlOutputText) facet2.findComponent("text");
        Assertions.assertNotNull(text2);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        facet2.pushComponentToEL(facesContext, facet2);
        text2.pushComponentToEL(facesContext, text2);
        //set on tag
        Assertions.assertEquals("background:green", text2.getStyle());
        // not set, should return null, but since there is a ValueExpression indirection,
        // coercing rules apply here, so null is converted as ""
        Assertions.assertEquals("", text2.getStyleClass());
        text2.popComponentFromEL(facesContext);
        facet2.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("style")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
    }
    
    @Test
    public void testSimpleCompositeAttributeMethodExpression() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpression.xhtml");

        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        Assertions.assertNotNull(button);
        Assertions.assertEquals("#{helloWorldBean.send}", button.getActionExpression().getExpressionString());
        Assertions.assertEquals("#{helloWorldBean.send}", ((MethodExpression)compositeComponent.getAttributes().get("metodo")).getExpressionString());
        Assertions.assertNull(button.getAttributes().get("metodo"));
        
        UICommand link = (UICommand) compositeComponent.findComponent("link");
        Assertions.assertNotNull(link);
        Assertions.assertEquals(1, link.getActionListeners().length);
        UIInput input = (UIInput) compositeComponent.findComponent("input");
        Assertions.assertNotNull(input);
        Assertions.assertEquals(1, input.getValidators().length);
        Assertions.assertEquals(1, input.getValueChangeListeners().length);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //compositeComponent.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testSimpleActionSource() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleActionSource.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        Assertions.assertNotNull(button);
        Assertions.assertEquals(3, button.getActionListeners().length);
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testSimpleValueHolder() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleValueHolder.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.findComponent("text");
        Assertions.assertNotNull(text);
        Assertions.assertNotNull(text.getConverter());
        //Assertions.assertEquals(2, button.getActionListeners().length);
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testCompositeActionSource() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeActionSource.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button3");
        Assertions.assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button");
        Assertions.assertNotNull(button);
        //One added in testCompositeActionSource, the other one
        //inside compositeActionSource.xhtml
        Assertions.assertEquals(2, button.getActionListeners().length);
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testSimpleInsertChildren() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleInsertChildren.xhtml");
        
        /*
        UIComponent form = root.findComponent("testForm1");
        Assertions.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button3");
        Assertions.assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button");
        Assertions.assertNotNull(button);
        */
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        String resp = sw.toString();
        
        Assertions.assertTrue(resp.contains("Hello"));
        Assertions.assertTrue(resp.contains("Leonardo"));
        Assertions.assertTrue(resp.contains("Alfredo"));
        Assertions.assertTrue(resp.contains("Uribe"));
        Assertions.assertTrue(resp.contains("Sayonara"));
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testSimpleInsertChildrenAjax() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleInsertChildrenAjax.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) facet1.findComponent("link");
        Assertions.assertNotNull(link);
        Assertions.assertEquals(1, link.getClientBehaviors().size());
        Assertions.assertEquals(1, link.getClientBehaviors().get(link.getDefaultEventName()).size());
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        String resp = sw.toString();
        */
        //System.out.print(sw.toString());
    }

    @Test
    public void testSimpleInsertChildrenAjax2() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleInsertChildrenAjax2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) compositeComponent1.findComponent("link");
        Assertions.assertNotNull(link);
        Assertions.assertEquals(1, link.getClientBehaviors().size());
        Assertions.assertEquals(1, link.getClientBehaviors().get(link.getDefaultEventName()).size());
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        String resp = sw.toString();
        */
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testSimpleInsertChildrenNoAjax() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleInsertChildrenNoAjax.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) facet1.findComponent("link");
        Assertions.assertNotNull(link);
        Assertions.assertEquals(0, link.getClientBehaviors().size());
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        String resp = sw.toString();
        */
        //System.out.print(sw.toString());
    }
    
    @Test
    public void testCompositeInsertChildren() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        facet1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }
    
    @Test
    public void testCompositeInsertChildrenPreserveTemplateSlot() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        facet1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }
    
    @Test
    public void testCompositeInsertChildren3() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren3.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }
    
    @Test
    public void testCompositeInsertChildren4() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren4.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assertions.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }
    
        
    @Test
    public void testCompositeInsertChildren5() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren5.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assertions.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }

    @Test
    public void testCompositeInsertChildren6() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertChildren6.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assertions.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assertions.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("ALFA"));
        Assertions.assertTrue(resp.contains("BETA"));
        Assertions.assertTrue(resp.contains("GAMMA"));
        Assertions.assertTrue(resp.contains("OMEGA"));
    }

    @Test
    public void testCompositeInsertFacet() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertFacet.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) facet1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet2);
        Assertions.assertEquals(1,facet2.getChildCount());
        UIOutput targetComp = (UIOutput) facet2.getChildren().get(0);
        UIComponent insertedFacet = targetComp.getFacet("header");
        Assertions.assertNotNull(insertedFacet);
    }
    
    @Test
    public void testCompositeInsertFacetChildren() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeInsertFacetChildren.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) facet1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet2);
        Assertions.assertEquals(3,facet2.getChildCount());
        UIComponent insertedFacet = facet2.getChildren().get(1).getFacet("header");
        Assertions.assertNotNull(insertedFacet);
    }

    @Test
    public void testSimpleRenderFacet() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleRenderFacet.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("HELLO"));
        Assertions.assertTrue(resp.contains("WORLD"));
        
    }
    
    @Test
    public void testSimpleFEvent() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleFEvent.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        Assertions.assertTrue((Boolean) compositeComponent1.getAttributes().get("postAddToViewCallback"),
                "postAddToViewCallback should be called");
        
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("HELLO"));
        Assertions.assertTrue(resp.contains("WORLD"));
        */
        
    }
    
    @Test
    public void testSimpleFEvent2() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleFEvent2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        application.publishEvent(facesContext, PreRenderViewEvent.class, root);
        
        Assertions.assertTrue((Boolean) compositeComponent1.getAttributes().get("preRenderViewCallback"),
                "preRenderViewCallback should be called");
        
        application.publishEvent(facesContext, PostRenderViewEvent.class, root);
        
        Assertions.assertTrue((Boolean) compositeComponent1.getAttributes().get("postRenderViewCallback"),
                "postRenderViewCallback should be called");
        
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assertions.assertTrue(resp.contains("HELLO"));
        Assertions.assertTrue(resp.contains("WORLD"));
        */
        
    }
    
    @Test
    public void testsCompositeRefVE() throws Exception {
        
        servletContext.addInitParameter(
                MyfacesConfig.CACHE_EL_EXPRESSIONS, 
                "always");
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeRefVE.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assertions.assertNotNull(facet1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        Assertions.assertTrue(sw.toString().contains("success"),
                "Error when rendering" + sw.toString());
    }

    @Test
    public void testSimpleThisResourceReference() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleThisResourceReference.xhtml");

        UINamingContainer compositeComponent = (UINamingContainer) root.findComponent("cc1");
        Assertions.assertNotNull(compositeComponent);
        HtmlGraphicImage gi = (HtmlGraphicImage) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("gi");
        Assertions.assertNotNull(gi);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent.encodeAll(facesContext);
        sw.flush();

        String result = sw.toString();
        
        Resource resource = facesContext.getApplication().getResourceHandler().createResource("logo_mini.jpg", "testComposite");
        Assertions.assertNotNull(resource);
        
        Assertions.assertTrue(result.contains(resource.getRequestPath()));
    }
    
    @Test
    public void testComponentFromResourceId() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testComponentFromResourceId.xhtml");

        UIComponent panelGroup = root.findComponent("testGroup");
        Assertions.assertNotNull(panelGroup);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("text");
        Assertions.assertNotNull(text);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent.encodeAll(facesContext);
        sw.flush();
        
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("value")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
    }

}
