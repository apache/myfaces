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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.ActionEvent;

import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.junit.jupiter.api.Test;

class CompositeComponentActionListenerTestCase extends AbstractFaceletTestCase
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
    void simpleActionListenerTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionListenerTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        UICommand button1 = (UICommand) compositeComponent1.findComponent("testComponent");
        assertNotNull(button1);
        assertNotNull(button1.getActionListeners());
        assertEquals(1, button1.getActionListeners().length);
        
        bean.setActionListener1Called(false);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        assertTrue(bean.isActionListener1Called());

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);
        UICommand button2 = (UICommand) compositeComponent2.findComponent("testComponent");
        assertNotNull(button2);
        assertNotNull(button2.getActionListeners());
        assertEquals(1, button2.getActionListeners().length);
        
        bean.setActionListener2Called(false);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        assertTrue(bean.isActionListener2Called());
    }

    @Test
    void compositeActionListenerTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionListenerTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent1inner);
        UICommand button1 = (UICommand) compositeComponent1inner.findComponent("testComponent");
        assertNotNull(button1);
        assertNotNull(button1.getActionListeners());
        assertEquals(1, button1.getActionListeners().length);
        
        bean.setActionListener1Called(false);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        assertTrue(bean.isActionListener1Called());

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);
        
        UINamingContainer compositeComponent2inner = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2inner);
        UICommand button2 = (UICommand) compositeComponent2inner.findComponent("testComponent");
        assertNotNull(button2);
        assertNotNull(button2.getActionListeners());
        assertEquals(1, button2.getActionListeners().length);
        
        bean.setActionListener2Called(false);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        assertTrue(bean.isActionListener2Called());

    }

    @Test
    void simpleActionListenerTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionListenerTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        UICommand button1 = (UICommand) compositeComponent1.findComponent("testComponent");
        assertNotNull(button1);
        assertNotNull(button1.getActionListeners());
        assertEquals(1, button1.getActionListeners().length);
        
        bean.setActionListener1Called(false);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        assertTrue(bean.isActionListener1Called());
        
        UICommand button1n = (UICommand) compositeComponent1.findComponent("testComponentNoTarget");
        
        assertNotNull(button1n);
        assertNotNull(button1n.getActionListeners());
        assertEquals(1, button1n.getActionListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        bean.setActionListener1Called(false);
        button1n.getActionListeners()[0].processAction(new ActionEvent(button1n));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);
        UICommand button2 = (UICommand) compositeComponent2.findComponent("testComponent");
        assertNotNull(button2);
        assertNotNull(button2.getActionListeners());
        assertEquals(1, button2.getActionListeners().length);
        
        bean.setActionListener2Called(false);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        assertTrue(bean.isActionListener2Called());
        
        UICommand button2n = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        
        assertNotNull(button2n);
        assertNotNull(button2n.getActionListeners());
        assertEquals(1, button2n.getActionListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        bean.setActionListener2Called(false);
        button2n.getActionListeners()[0].processAction(new ActionEvent(button2n));
        assertTrue(bean.isActionListener2Called());
        compositeComponent2.popComponentFromEL(facesContext);
    }

    @Test
    void compositeActionListenerTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionListenerTarget2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1target = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent1target);
        UICommand button1target = (UICommand) compositeComponent1target.findComponent("testComponent");
        assertNotNull(button1target);
        assertNotNull(button1target.getActionListeners());
        assertEquals(1, button1target.getActionListeners().length);
        
        bean.setActionListener1Called(false);
        button1target.getActionListeners()[0].processAction(new ActionEvent(button1target));
        assertTrue(bean.isActionListener1Called());

        UICommand button1notarget = (UICommand) compositeComponent1target.findComponent("testComponentNoTarget");
        assertNotNull(button1notarget);
        assertNotNull(button1notarget.getActionListeners());
        assertEquals(1, button1notarget.getActionListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1target.pushComponentToEL(facesContext, compositeComponent1target);
        bean.setActionListener1Called(false);
        button1notarget.getActionListeners()[0].processAction(new ActionEvent(button1notarget));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1target.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent1notarget = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent1notarget);
        UICommand buttonnotarget1target = (UICommand) compositeComponent1notarget.findComponent("testComponent");
        assertNotNull(buttonnotarget1target);
        assertNotNull(buttonnotarget1target.getActionListeners());
        assertEquals(1, buttonnotarget1target.getActionListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setActionListener1Called(false);
        buttonnotarget1target.getActionListeners()[0].processAction(new ActionEvent(buttonnotarget1target));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UICommand buttonnotarget1notarget = (UICommand) compositeComponent1notarget.findComponent("testComponentNoTarget");
        assertNotNull(buttonnotarget1notarget);
        assertNotNull(buttonnotarget1notarget.getActionListeners());
        assertEquals(1, buttonnotarget1notarget.getActionListeners().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setActionListener1Called(false);
        buttonnotarget1notarget.getActionListeners()[0].processAction(new ActionEvent(buttonnotarget1notarget));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);
        
        UINamingContainer compositeComponent2target = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionTarget");
        assertNotNull(compositeComponent2target);
        UICommand button2target = (UICommand) compositeComponent2target.findComponent("testComponent");
        assertNotNull(button2target);
        assertNotNull(button2target.getActionListeners());
        assertEquals(1, button2target.getActionListeners().length);
        
        bean.setActionListener2Called(false);
        button2target.getActionListeners()[0].processAction(new ActionEvent(button2target));
        assertTrue(bean.isActionListener2Called());

        UICommand button2notarget = (UICommand) compositeComponent2target.findComponent("testComponentNoTarget");
        assertNotNull(button2notarget);
        assertNotNull(button2notarget.getActionListeners());
        assertEquals(1, button2notarget.getActionListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2target.pushComponentToEL(facesContext, compositeComponent2target);
        bean.setActionListener2Called(false);
        button2notarget.getActionListeners()[0].processAction(new ActionEvent(button2notarget));
        assertTrue(bean.isActionListener2Called());
        compositeComponent2target.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent2notarget = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent2notarget);
        UICommand buttonnotarget2target = (UICommand) compositeComponent2notarget.findComponent("testComponent");
        assertNotNull(buttonnotarget2target);
        assertNotNull(buttonnotarget2target.getActionListeners());
        assertEquals(1, buttonnotarget2target.getActionListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2notarget.pushComponentToEL(facesContext, compositeComponent2notarget);
        bean.setActionListener2Called(false);
        buttonnotarget2target.getActionListeners()[0].processAction(new ActionEvent(buttonnotarget2target));
        assertTrue(bean.isActionListener2Called());
        compositeComponent2notarget.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        
        UICommand buttonnotarget2notarget = (UICommand) compositeComponent2notarget.findComponent("testComponentNoTarget");
        assertNotNull(buttonnotarget2notarget);
        assertNotNull(buttonnotarget2notarget.getActionListeners());
        assertEquals(1, buttonnotarget2notarget.getActionListeners().length);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2notarget.pushComponentToEL(facesContext, compositeComponent2notarget);
        bean.setActionListener2Called(false);
        buttonnotarget2notarget.getActionListeners()[0].processAction(new ActionEvent(buttonnotarget2notarget));
        assertTrue(bean.isActionListener2Called());
        compositeComponent2notarget.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
    }

    @Test
    void simpleActionListenerNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeActionListenerNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        UICommand button1 = (UICommand) compositeComponent1.findComponent("testComponentNoTarget");
        assertNotNull(button1);
        assertNotNull(button1.getActionListeners());
        assertEquals(1, button1.getActionListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        button1.pushComponentToEL(facesContext,  button1);
        bean.setActionListener1Called(false);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        assertTrue(bean.isActionListener1Called());
        button1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);
        UICommand button2 = (UICommand) compositeComponent2.findComponent("testComponentNoTarget");
        assertNotNull(button2);
        assertNotNull(button2.getActionListeners());
        assertEquals(1, button2.getActionListeners().length);

        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        button2.pushComponentToEL(facesContext,  button2);
        bean.setActionListener2Called(false);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        assertTrue(bean.isActionListener2Called());
        button2.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);

    }

    @Test
    void compositeActionListenerNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionListenerNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);

        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        assertNotNull(compositeComponent1inner);
        UICommand testComponentNoTarget1 = (UICommand) compositeComponent1inner.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget1);
        assertNotNull(testComponentNoTarget1.getActionListeners());
        assertEquals(1, testComponentNoTarget1.getActionListeners().length);

        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1inner);
        bean.setActionListener1Called(false);
        testComponentNoTarget1.getActionListeners()[0].processAction(new ActionEvent(testComponentNoTarget1));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1inner.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        assertNotNull(compositeComponent2);

        UINamingContainer compositeComponent2inner = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        assertNotNull(compositeComponent2inner);
        UICommand testComponentNoTarget2 = (UICommand) compositeComponent2inner.findComponent("testComponentNoTarget");
        assertNotNull(testComponentNoTarget2);
        assertNotNull(testComponentNoTarget2.getActionListeners());
        assertEquals(1, testComponentNoTarget2.getActionListeners().length);

        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2inner);
        bean.setActionListener2Called(false);
        testComponentNoTarget2.getActionListeners()[0].processAction(new ActionEvent(testComponentNoTarget2));
        assertTrue(bean.isActionListener2Called());
        compositeComponent2inner.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
    
    }

    @Test
    void compositeActionListenerNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeActionListenerNoTarget2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1n1 = (UINamingContainer) compositeComponent1.findComponent("compositeAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent1n1);
        UINamingContainer compositeComponent1n2 = (UINamingContainer) compositeComponent1n1.findComponent("simpleAttributeMethodExpressionNoTarget");
        assertNotNull(compositeComponent1n2);
        UICommand button1 = (UICommand) compositeComponent1n2.findComponent("testComponentNoTarget");
        assertNotNull(button1);
        assertNotNull(button1.getActionListeners());
        assertEquals(1, button1.getActionListeners().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1n2);
        bean.setActionListener1Called(false);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        assertTrue(bean.isActionListener1Called());
        compositeComponent1n2.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
    }
}
