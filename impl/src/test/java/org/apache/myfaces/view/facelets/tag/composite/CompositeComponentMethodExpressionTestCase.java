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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

class CompositeComponentMethodExpressionTestCase extends AbstractFaceletTestCase
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
    void simpleMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getCustomMethod());
        assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
    }

    @Test
    void compositeMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent2.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getCustomMethod());
        assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
    }

    @Test
    void simpleMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getCustomMethod());
        
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeMethodExpressionNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionNoTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("compositeAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent2);
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }


    @Test
    void simpleMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getCustomMethod());
        assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        
        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2);
        SimpleComponent testComponent = (SimpleComponent) compositeComponent2.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getCustomMethod());
        assertEquals("somethingFunny"+"x", testComponent.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));

        SimpleComponent testComponentNoTarget = (SimpleComponent) compositeComponent2.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals("somethingFunny"+"x", testComponentNoTarget.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        SimpleComponent testComponent3 = (SimpleComponent) compositeComponent3.findComponent("testComponent");
        assertNotNull(testComponent3);
        assertNotNull(testComponent3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals("somethingFunny"+"x", testComponent3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        SimpleComponent testComponentNoTarget3 = (SimpleComponent) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getCustomMethod());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals("somethingFunny"+"x", testComponentNoTarget3.getCustomMethod().invoke(facesContext.getELContext(), new Object[]{"x"}));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void simpleActionMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
    }

    @Test
    void compositeActionMethodExpressionTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
    }

    @Test
    void simpleActionMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionNoTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand button = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        assertNotNull(button);
        
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        button.pushComponentToEL(facesContext,  button);
        MethodExpression method = button.getActionExpression();
        assertEquals(bean.doSomeAction(), method.invoke(facesContext.getELContext(), null));
        button.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);

    }

    @Test
    void compositeActionMethodExpressionNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void simpleActionMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
        
        UICommand testComponentNoTarget = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void simpleActionMethodExpressionTarget3() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionMethodExpressionTarget3.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        UICommand testComponent = (UICommand) compositeComponent.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));
        
        UICommand testComponentNoTarget = (UICommand) compositeComponent.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeActionMethodExpressionTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));

        UICommand testComponentNoTarget = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        UICommand testComponent3 = (UICommand) compositeComponent3.findComponent("testComponent");
        assertNotNull(testComponent3);
        assertNotNull(testComponent3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponent3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeActionMethodExpressionNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionNoTarget2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("compositeAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent2);
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }

    @Test
    void compositeActionMethodExpressionTarget3() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionMethodExpressionTarget3.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent);
        
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2);
        UICommand testComponent = (UICommand) compositeComponent2.findComponent("testComponent");
        assertNotNull(testComponent);
        assertNotNull(testComponent.getActionExpression());
        assertEquals(bean.doSomeAction(), testComponent.getActionExpression().invoke(facesContext.getELContext(), null));

        UICommand testComponentNoTarget = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget);
        assertNotNull(testComponentNoTarget.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        assertEquals(bean.doSomeAction(), testComponentNoTarget.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent2.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent3 = (UINamingContainer) compositeComponent.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent3);
        UICommand testComponent3 = (UICommand) compositeComponent3.findComponent("testComponent");
        assertNotNull(testComponent3);
        assertNotNull(testComponent3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponent3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
        
        UICommand testComponentNoTarget3 = (UICommand) compositeComponent3.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget3);
        assertNotNull(testComponentNoTarget3.getActionExpression());
        compositeComponent.pushComponentToEL(facesContext, compositeComponent);
        compositeComponent3.pushComponentToEL(facesContext, compositeComponent3);
        assertEquals(bean.doSomeAction(), testComponentNoTarget3.getActionExpression().invoke(facesContext.getELContext(), null));
        compositeComponent3.popComponentFromEL(facesContext);
        compositeComponent.popComponentFromEL(facesContext);
    }


}
