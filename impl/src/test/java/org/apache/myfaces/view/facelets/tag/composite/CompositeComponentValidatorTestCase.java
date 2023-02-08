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

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompositeComponentValidatorTestCase extends FaceletTestCase
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
    public void testSimpleValidatorTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValidatorTarget.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValidators());
        Assertions.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assertions.assertTrue(bean.isValidator1Called());
    }

    @Test
    public void testCompositeValidatorTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValidatorTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent1inner);
        UIInput input1 = (UIInput) compositeComponent1inner.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValidators());
        Assertions.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assertions.assertTrue(bean.isValidator1Called());

    }
    
    @Test
    public void testSimpleValidatorTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValidatorTarget2.xhtml");

        
        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValidators());
        Assertions.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        
        UIInput input1n = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        
        Assertions.assertNotNull(input1n);
        Assertions.assertNotNull(input1n.getValidators());
        Assertions.assertEquals(1, input1n.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        bean.setValidator1Called(false);
        input1n.getValidators()[0].validate(facesContext, input1n, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1.popComponentFromEL(facesContext);
        
    }

    @Test
    public void testCompositeValidatorTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValidatorTarget2.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1target = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assertions.assertNotNull(compositeComponent1target);
        UIInput input1target = (UIInput) compositeComponent1target.findComponent("testComponent");
        Assertions.assertNotNull(input1target);
        Assertions.assertNotNull(input1target.getValidators());
        Assertions.assertEquals(1, input1target.getValidators().length);
        
        bean.setValidator1Called(false);
        input1target.getValidators()[0].validate(facesContext, input1target, "x");
        Assertions.assertTrue(bean.isValidator1Called());

        UIInput input1notarget = (UIInput) compositeComponent1target.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input1notarget);
        Assertions.assertNotNull(input1notarget.getValidators());
        Assertions.assertEquals(1, input1notarget.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1target.pushComponentToEL(facesContext, compositeComponent1target);
        bean.setValidator1Called(false);
        input1notarget.getValidators()[0].validate(facesContext, input1notarget, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1target.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent1notarget = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1notarget);
        UIInput inputnotarget1target = (UIInput) compositeComponent1notarget.findComponent("testComponent");
        Assertions.assertNotNull(inputnotarget1target);
        Assertions.assertNotNull(inputnotarget1target.getValidators());
        Assertions.assertEquals(1, inputnotarget1target.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValidator1Called(false);
        inputnotarget1target.getValidators()[0].validate(facesContext, inputnotarget1target, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIInput inputnotarget1notarget = (UIInput) compositeComponent1notarget.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(inputnotarget1notarget);
        Assertions.assertNotNull(inputnotarget1notarget.getValidators());
        Assertions.assertEquals(1, inputnotarget1notarget.getValidators().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValidator1Called(false);
        inputnotarget1notarget.getValidators()[0].validate(facesContext, inputnotarget1notarget, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
    }
    
    @Test
    public void testSimpleValidatorNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeValidatorNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(input1);
        Assertions.assertNotNull(input1.getValidators());
        Assertions.assertEquals(1, input1.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        input1.pushComponentToEL(facesContext,  input1);
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        input1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

    }
    
    @Test
    public void testCompositeValidatorNoTarget() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValidatorNoTarget.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assertions.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assertions.assertNotNull(compositeComponent1);

        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        Assertions.assertNotNull(compositeComponent1inner);
        UIInput testComponentNoTarget1 = (UIInput) compositeComponent1inner.findComponent("testComponentNoTarget");
        Assertions.assertNotNull(testComponentNoTarget1);
        Assertions.assertNotNull(testComponentNoTarget1.getValidators());
        Assertions.assertEquals(1, testComponentNoTarget1.getValidators().length);

        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1inner);
        bean.setValidator1Called(false);
        testComponentNoTarget1.getValidators()[0].validate(facesContext, testComponentNoTarget1, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1inner.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);

    }

    @Test
    public void testCompositeValidatorNoTarget2() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeAttributeValidatorNoTarget2.xhtml");
        
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
        Assertions.assertNotNull(input1.getValidators());
        Assertions.assertEquals(1, input1.getValidators().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1n2);
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assertions.assertTrue(bean.isValidator1Called());
        compositeComponent1n2.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
    }
}
