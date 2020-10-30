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

import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

public class CompositeComponentMethodExpressionTestCase extends FaceletTestCase
{
    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
        application.addComponent(CompositeTestComponent.class.getName(), 
                CompositeTestComponent.class.getName());
        application.addComponent(SimpleComponent.class.getName(), SimpleComponent.class.getName());
    }

    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        servletContext.addInitParameter("jakarta.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
    }
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    public void testSimpleMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getCustomMethod());
        Assert.assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
    }

    @Test
    public void testCompositeMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent2);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent2.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getCustomMethod());
        Assert.assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
    }
    
    @Test
    public void testSimpleMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getCustomMethod());
        
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testCompositeMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }
    
    @Test
    public void testCompositeMethodExpressionNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionNoTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("compositeAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent2);
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }


    @Test
    public void testSimpleMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getCustomMethod());
        Assert.assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        
        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testCompositeMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent2);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent2.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getCustomMethod());
        Assert.assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));

        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent2.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        SimpleComponent testComponent3 = (SimpleComponent) compositeComponent3.findComponent("testComponent");
        Assert.assertNotNull(testComponent3);
        Assert.assertNotNull(testComponent3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals("somethingFunny"+"x", testComponent3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testSimpleActionMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
    }

    @Test
    public void testCompositeActionMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
    }

    @Test
    public void testSimpleActionMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        Assert.assertNotNull(button);
        
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        button.pushComponentToEL(facesContext,  button);
        MethodExpression method = button.getActionExpression();
        Assert.assertEquals(bean.doSomeAction(), method.invoke(facesContext.getELContext(), null));
        button.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);

    }
    
    @Test
    public void testCompositeActionMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testSimpleActionMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
        
        UICommand testComponentNoTarget = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
    }
    
    @Test
    public void testSimpleActionMethodExpressionTarget3() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget3.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
        
        UICommand testComponentNoTarget = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testCompositeActionMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));

        UICommand testComponentNoTarget = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        UICommand testComponent3 = (UICommand) compositeComponent3.findComponent("testComponent");
        Assert.assertNotNull(testComponent3);
        Assert.assertNotNull(testComponent3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponent3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    public void testCompositeActionMethodExpressionNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionNoTarget2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("compositeAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent2);
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }
    
    @Test
    public void testCompositeActionMethodExpressionTarget3() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget3.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        Assert.assertNotNull(testComponent);
        Assert.assertNotNull(testComponent.getActionExpression());
        Assert.assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));

        UICommand testComponentNoTarget = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget);
        Assert.assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent2.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent3);
        UICommand testComponent3 = (UICommand) compositeComponent3.findComponent("testComponent");
        Assert.assertNotNull(testComponent3);
        Assert.assertNotNull(testComponent3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponent3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget3);
        Assert.assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        Assert.assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }


}
