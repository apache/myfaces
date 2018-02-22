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

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.PostRenderViewEvent;
import javax.faces.event.PreRenderViewEvent;

import org.apache.myfaces.config.NamedEventManager;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.apache.myfaces.view.facelets.impl.FaceletCompositionContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class CompositeComponentTestCase extends FaceletTestCase
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
        servletContext.addInitParameter("javax.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
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
        Assert.assertNotNull(panelGroup);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("text");
        Assert.assertNotNull(text);
        
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlOutputText text1 = (HtmlOutputText) facet1.findComponent("text");
        Assert.assertNotNull(text1);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        text1.pushComponentToEL(facesContext, text1);
        //set on tag
        Assert.assertEquals("class1", text1.getStyleClass());
        //set as default
        Assert.assertEquals("background:red", text1.getStyle());
        //Check coercion of attribute using type value
        Assert.assertEquals(5, compositeComponent1.getAttributes().get("index"));
        //Check default coercion
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{cc.attrs.cols}", Object.class);
        Assert.assertEquals(1, ve.getValue(facesContext.getELContext()));
        text1.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assert.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assert.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet2);        
        HtmlOutputText text2 = (HtmlOutputText) facet2.findComponent("text");
        Assert.assertNotNull(text2);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        facet2.pushComponentToEL(facesContext, facet2);
        text2.pushComponentToEL(facesContext, text2);
        //set on tag
        Assert.assertEquals("background:green", text2.getStyle());
        // not set, should return null, but since there is a ValueExpression indirection,
        // coercing rules apply here, so null is converted as ""
        Assert.assertEquals("", text2.getStyleClass());
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
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        Assert.assertNotNull(button);
        Assert.assertEquals("#{helloWorldBean.send}", button.getActionExpression().getExpressionString());
        Assert.assertEquals("#{helloWorldBean.send}", ((MethodExpression)compositeComponent.getAttributes().get("metodo")).getExpressionString());
        Assert.assertNull(button.getAttributes().get("metodo"));
        
        UICommand link = (UICommand) compositeComponent.findComponent("link");
        Assert.assertNotNull(link);
        Assert.assertEquals(1, link.getActionListeners().length);
        UIInput input = (UIInput) compositeComponent.findComponent("input");
        Assert.assertNotNull(input);
        Assert.assertEquals(1, input.getValidators().length);
        Assert.assertEquals(1, input.getValueChangeListeners().length);
        
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
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("button");
        Assert.assertNotNull(button);
        Assert.assertEquals(3, button.getActionListeners().length);
        
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
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.findComponent("text");
        Assert.assertNotNull(text);
        Assert.assertNotNull(text.getConverter());
        //Assert.assertEquals(2, button.getActionListeners().length);
        
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
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button3");
        Assert.assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button");
        Assert.assertNotNull(button);
        //One added in testCompositeActionSource, the other one
        //inside compositeActionSource.xhtml
        Assert.assertEquals(2, button.getActionListeners().length);
        
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
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button3");
        Assert.assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button");
        Assert.assertNotNull(button);
        */
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        root.encodeAll(facesContext);
        sw.flush();

        String resp = sw.toString();
        
        Assert.assertTrue(resp.contains("Hello"));
        Assert.assertTrue(resp.contains("Leonardo"));
        Assert.assertTrue(resp.contains("Alfredo"));
        Assert.assertTrue(resp.contains("Uribe"));
        Assert.assertTrue(resp.contains("Sayonara"));
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) facet1.findComponent("link");
        Assert.assertNotNull(link);
        Assert.assertEquals(1, link.getClientBehaviors().size());
        Assert.assertEquals(1, link.getClientBehaviors().get(link.getDefaultEventName()).size());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) compositeComponent1.findComponent("link");
        Assert.assertNotNull(link);
        Assert.assertEquals(1, link.getClientBehaviors().size());
        Assert.assertEquals(1, link.getClientBehaviors().get(link.getDefaultEventName()).size());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlCommandLink link = (HtmlCommandLink) facet1.findComponent("link");
        Assert.assertNotNull(link);
        Assert.assertEquals(0, link.getClientBehaviors().size());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        facet1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        facet1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assert.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assert.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        //UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        //Assert.assertNotNull(compositeComponent1);
        //UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        //Assert.assertNotNull(facet1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        panelGroup1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("ALFA"));
        Assert.assertTrue(resp.contains("BETA"));
        Assert.assertTrue(resp.contains("GAMMA"));
        Assert.assertTrue(resp.contains("OMEGA"));
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) facet1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet2);
        Assert.assertEquals(1,facet2.getChildCount());
        UIOutput targetComp = (UIOutput) facet2.getChildren().get(0);
        UIComponent insertedFacet = targetComp.getFacet("header");
        Assert.assertNotNull(insertedFacet);
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) facet1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet2);
        Assert.assertEquals(3,facet2.getChildCount());
        UIComponent insertedFacet = facet2.getChildren().get(1).getFacet("header");
        Assert.assertNotNull(insertedFacet);
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("HELLO"));
        Assert.assertTrue(resp.contains("WORLD"));
        
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
        Assert.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        Assert.assertTrue("postAddToViewCallback should be called", (Boolean) compositeComponent1.getAttributes().get("postAddToViewCallback"));
        
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("HELLO"));
        Assert.assertTrue(resp.contains("WORLD"));
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
        Assert.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        application.publishEvent(facesContext, PreRenderViewEvent.class, root);
        
        Assert.assertTrue("preRenderViewCallback should be called", (Boolean) compositeComponent1.getAttributes().get("preRenderViewCallback"));
        
        application.publishEvent(facesContext, PostRenderViewEvent.class, root);
        
        Assert.assertTrue("postRenderViewCallback should be called", (Boolean) compositeComponent1.getAttributes().get("postRenderViewCallback"));
        
        /*
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();

        Assert.assertTrue(resp.contains("HELLO"));
        Assert.assertTrue(resp.contains("WORLD"));
        */
        
    }
    
    @Test
    public void testsCompositeRefVE() throws Exception {
        
        servletContext.addInitParameter(
                FaceletCompositionContextImpl.INIT_PARAM_CACHE_EL_EXPRESSIONS, 
                "always");
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeRefVE.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);

        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        Assert.assertTrue("Error when rendering" + sw.toString(), sw.toString().contains("success"));
    }

    @Test
    public void testSimpleThisResourceReference() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleThisResourceReference.xhtml");

        UINamingContainer compositeComponent = (UINamingContainer) root.findComponent("cc1");
        Assert.assertNotNull(compositeComponent);
        HtmlGraphicImage gi = (HtmlGraphicImage) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("gi");
        Assert.assertNotNull(gi);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent.encodeAll(facesContext);
        sw.flush();

        String result = sw.toString();
        
        Resource resource = facesContext.getApplication().getResourceHandler().createResource("logo_mini.jpg", "testComposite");
        Assert.assertNotNull(resource);
        
        Assert.assertTrue(result.contains(resource.getRequestPath()));
    }
    
    @Test
    public void testComponentFromResourceId() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testComponentFromResourceId.xhtml");

        UIComponent panelGroup = root.findComponent("testGroup");
        Assert.assertNotNull(panelGroup);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UIOutput text = (UIOutput) compositeComponent.getFacet(UIComponent.COMPOSITE_FACET_NAME).findComponent("text");
        Assert.assertNotNull(text);
        
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
