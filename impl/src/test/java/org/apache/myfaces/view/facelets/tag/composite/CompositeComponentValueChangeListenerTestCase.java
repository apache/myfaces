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
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.ValueChangeEvent;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompositeComponentValueChangeListenerTestCase extends FaceletTestCase
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
    public void testSimpleValueChangeListenerTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValueChangeListenerTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValueChangeListeners());
        Assertions.assertEquals(1, input1.getValueChangeListeners().length);
        
        bean.setValueChangeListener1Called(false);
        input1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIInput input2 = (UIInput) compositeComponent2.findComponent("testComponent");
        Assertions.assertNotNull(input2);
        Assertions.assertNotNull(input2.getValueChangeListeners());
        Assertions.assertEquals(1, input2.getValueChangeListeners().length);
        
        bean.setValueChangeListener2Called(false);
        input2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
    }

    @Test
    public void testCompositeValueChangeListenerTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValueChangeListenerTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent1inner);
        UIInput input1 = (UIInput) compositeComponent1inner.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValueChangeListeners());
        Assertions.assertEquals(1, input1.getValueChangeListeners().length);
        
        bean.setValueChangeListener1Called(false);
        input1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        
        UINamingContainer compositeComponent2inner = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent2inner);
        UIInput input2 = (UIInput) compositeComponent2inner.findComponent("testComponent");
        Assertions.assertNotNull(input2);
        Assertions.assertNotNull(input2.getValueChangeListeners());
        Assertions.assertEquals(1, input2.getValueChangeListeners().length);
        
        bean.setValueChangeListener2Called(false);
        input2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());

    }
    
    @Test
    public void testSimpleValueChangeListenerTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValueChangeListenerTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValueChangeListeners());
        Assertions.assertEquals(1, input1.getValueChangeListeners().length);
        
        bean.setValueChangeListener1Called(false);
        input1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        
        UIInput input1n = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        
        Assertions.assertNotNull(input1n);
        Assertions.assertNotNull(input1n.getValueChangeListeners());
        Assertions.assertEquals(1, input1n.getValueChangeListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        bean.setValueChangeListener1Called(false);
        input1n.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1n,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIInput input2 = (UIInput) compositeComponent2.findComponent("testComponent");
        Assertions.assertNotNull(input2);
        Assertions.assertNotNull(input2.getValueChangeListeners());
        Assertions.assertEquals(1, input2.getValueChangeListeners().length);
        
        bean.setValueChangeListener2Called(false);
        input2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        
        UIInput input2n = (UIInput) compositeComponent2.findComponent("testComponentNoTarget");
        
        Assertions.assertNotNull(input2n);
        Assertions.assertNotNull(input2n.getValueChangeListeners());
        Assertions.assertEquals(1, input2n.getValueChangeListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        bean.setValueChangeListener2Called(false);
        input2n.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2n,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        compositeComponent2.popComponentFromEL(facesContext);
    }

    @Test
    public void testCompositeValueChangeListenerTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValueChangeListenerTarget2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1target = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent1target);
        UIInput input1target = (UIInput) compositeComponent1target.findComponent("testComponent");
        Assertions.assertNotNull(input1target);
        Assertions.assertNotNull(input1target.getValueChangeListeners());
        Assertions.assertEquals(1, input1target.getValueChangeListeners().length);
        
        bean.setValueChangeListener1Called(false);
        input1target.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1target,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());

        UIInput input1notarget = (UIInput) compositeComponent1target.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input1notarget);
        Assertions.assertNotNull(input1notarget.getValueChangeListeners());
        Assertions.assertEquals(1, input1notarget.getValueChangeListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1target.pushComponentToEL(facesContext, compositeComponent1target);
        bean.setValueChangeListener1Called(false);
        input1notarget.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1notarget,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1target.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent1notarget = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1notarget);
        UIInput inputnotarget1target = (UIInput) compositeComponent1notarget.findComponent("testComponent");
        Assertions.assertNotNull(inputnotarget1target);
        Assertions.assertNotNull(inputnotarget1target.getValueChangeListeners());
        Assertions.assertEquals(1, inputnotarget1target.getValueChangeListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValueChangeListener1Called(false);
        inputnotarget1target.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(inputnotarget1target,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIInput inputnotarget1notarget = (UIInput) compositeComponent1notarget.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(inputnotarget1notarget);
        Assertions.assertNotNull(inputnotarget1notarget.getValueChangeListeners());
        Assertions.assertEquals(1, inputnotarget1notarget.getValueChangeListeners().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValueChangeListener1Called(false);
        inputnotarget1notarget.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(inputnotarget1notarget,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        
        UINamingContainer compositeComponent2target = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent2target);
        UIInput input2target = (UIInput) compositeComponent2target.findComponent("testComponent");
        Assertions.assertNotNull(input2target);
        Assertions.assertNotNull(input2target.getValueChangeListeners());
        Assertions.assertEquals(1, input2target.getValueChangeListeners().length);
        
        bean.setValueChangeListener2Called(false);
        input2target.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2target,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());

        UIInput input2notarget = (UIInput) compositeComponent2target.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input2notarget);
        Assertions.assertNotNull(input2notarget.getValueChangeListeners());
        Assertions.assertEquals(1, input2notarget.getValueChangeListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2target.pushComponentToEL(facesContext, compositeComponent2target);
        bean.setValueChangeListener2Called(false);
        input2notarget.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2notarget,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        compositeComponent2target.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent2notarget = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent2notarget);
        UIInput inputnotarget2target = (UIInput) compositeComponent2notarget.findComponent("testComponent");
        Assertions.assertNotNull(inputnotarget2target);
        Assertions.assertNotNull(inputnotarget2target.getValueChangeListeners());
        Assertions.assertEquals(1, inputnotarget2target.getValueChangeListeners().length);
        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2notarget.pushComponentToEL(facesContext, compositeComponent2notarget);
        bean.setValueChangeListener2Called(false);
        inputnotarget2target.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(inputnotarget2target,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        compositeComponent2notarget.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        
        UIInput inputnotarget2notarget = (UIInput) compositeComponent2notarget.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(inputnotarget2notarget);
        Assertions.assertNotNull(inputnotarget2notarget.getValueChangeListeners());
        Assertions.assertEquals(1, inputnotarget2notarget.getValueChangeListeners().length);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2notarget.pushComponentToEL(facesContext, compositeComponent2notarget);
        bean.setValueChangeListener2Called(false);
        inputnotarget2notarget.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(inputnotarget2notarget,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        compositeComponent2notarget.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
    }
    
    @Test
    public void testSimpleValueChangeListenerNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValueChangeListenerNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValueChangeListeners());
        Assertions.assertEquals(1, input1.getValueChangeListeners().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        input1.pushComponentToEL(facesContext,  input1);
        bean.setValueChangeListener1Called(false);
        input1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        input1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);
        UIInput input2 = (UIInput) compositeComponent2.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input2);
        Assertions.assertNotNull(input2.getValueChangeListeners());
        Assertions.assertEquals(1, input2.getValueChangeListeners().length);

        
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        input2.pushComponentToEL(facesContext,  input2);
        bean.setValueChangeListener2Called(false);
        input2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input2,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        input2.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);

    }
    
    @Test
    public void testCompositeValueChangeListenerNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValueChangeListenerNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);

        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1inner);
        UIInput testComponentNoTarget1 = (UIInput) compositeComponent1inner.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(testComponentNoTarget1);
        Assertions.assertNotNull(testComponentNoTarget1.getValueChangeListeners());
        Assertions.assertEquals(1, testComponentNoTarget1.getValueChangeListeners().length);

        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1inner);
        bean.setValueChangeListener1Called(false);
        testComponentNoTarget1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(testComponentNoTarget1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1inner.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

        UIComponent panelGroup2 = root.findComponent("testGroup2");
        Assertions.assertNotNull(panelGroup2);
        UINamingContainer compositeComponent2 = (UINamingContainer) panelGroup2.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent2);

        UINamingContainer compositeComponent2inner = (UINamingContainer) compositeComponent2.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent2inner);
        UIInput testComponentNoTarget2 = (UIInput) compositeComponent2inner.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(testComponentNoTarget2);
        Assertions.assertNotNull(testComponentNoTarget2.getValueChangeListeners());
        Assertions.assertEquals(1, testComponentNoTarget2.getValueChangeListeners().length);

        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2inner);
        bean.setValueChangeListener2Called(false);
        testComponentNoTarget2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(testComponentNoTarget2,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener2Called());
        compositeComponent2inner.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
    
    }

    @Test
    public void testCompositeValueChangeListenerNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValueChangeListenerNoTarget2.xhtml");
        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1n1 = (UINamingContainer) compositeComponent1.findComponent("compositeAttributeMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1n1);
        UINamingContainer compositeComponent1n2 = (UINamingContainer) compositeComponent1n1.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1n2);
        UIInput input1 = (UIInput) compositeComponent1n2.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValueChangeListeners());
        Assertions.assertEquals(1, input1.getValueChangeListeners().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1n2);
        bean.setValueChangeListener1Called(false);
        input1.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(input1,"x","y"));
        Assertions.assertTrue(bean.isValueChangeListener1Called());
        compositeComponent1n2.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
    }
}
