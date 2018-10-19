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

import javax.el.ExpressionFactory;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.Assert;
import org.junit.Test;

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
        servletContext.addInitParameter("javax.faces.FACELETS_LIBRARIES", "/test-facelet.taglib.xml");
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assert.assertNotNull(input1);
        Assert.assertNotNull(input1.getValidators());
        Assert.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assert.assertTrue(bean.isValidator1Called());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent1inner);
        UIInput input1 = (UIInput) compositeComponent1inner.findComponent("testComponent");
        Assert.assertNotNull(input1);
        Assert.assertNotNull(input1.getValidators());
        Assert.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assert.assertTrue(bean.isValidator1Called());

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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponent");
        Assert.assertNotNull(input1);
        Assert.assertNotNull(input1.getValidators());
        Assert.assertEquals(1, input1.getValidators().length);
        
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assert.assertTrue(bean.isValidator1Called());
        
        UIInput input1n = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        
        Assert.assertNotNull(input1n);
        Assert.assertNotNull(input1n.getValidators());
        Assert.assertEquals(1, input1n.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        bean.setValidator1Called(false);
        input1n.getValidators()[0].validate(facesContext, input1n, "x");
        Assert.assertTrue(bean.isValidator1Called());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1target = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionTarget");
        Assert.assertNotNull(compositeComponent1target);
        UIInput input1target = (UIInput) compositeComponent1target.findComponent("testComponent");
        Assert.assertNotNull(input1target);
        Assert.assertNotNull(input1target.getValidators());
        Assert.assertEquals(1, input1target.getValidators().length);
        
        bean.setValidator1Called(false);
        input1target.getValidators()[0].validate(facesContext, input1target, "x");
        Assert.assertTrue(bean.isValidator1Called());

        UIInput input1notarget = (UIInput) compositeComponent1target.findComponent("testComponentNoTarget");
        Assert.assertNotNull(input1notarget);
        Assert.assertNotNull(input1notarget.getValidators());
        Assert.assertEquals(1, input1notarget.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1target.pushComponentToEL(facesContext, compositeComponent1target);
        bean.setValidator1Called(false);
        input1notarget.getValidators()[0].validate(facesContext, input1notarget, "x");
        Assert.assertTrue(bean.isValidator1Called());
        compositeComponent1target.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UINamingContainer compositeComponent1notarget = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent1notarget);
        UIInput inputnotarget1target = (UIInput) compositeComponent1notarget.findComponent("testComponent");
        Assert.assertNotNull(inputnotarget1target);
        Assert.assertNotNull(inputnotarget1target.getValidators());
        Assert.assertEquals(1, inputnotarget1target.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValidator1Called(false);
        inputnotarget1target.getValidators()[0].validate(facesContext, inputnotarget1target, "x");
        Assert.assertTrue(bean.isValidator1Called());
        compositeComponent1notarget.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        UIInput inputnotarget1notarget = (UIInput) compositeComponent1notarget.findComponent("testComponentNoTarget");
        Assert.assertNotNull(inputnotarget1notarget);
        Assert.assertNotNull(inputnotarget1notarget.getValidators());
        Assert.assertEquals(1, inputnotarget1notarget.getValidators().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1notarget.pushComponentToEL(facesContext, compositeComponent1notarget);
        bean.setValidator1Called(false);
        inputnotarget1notarget.getValidators()[0].validate(facesContext, inputnotarget1notarget, "x");
        Assert.assertTrue(bean.isValidator1Called());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIInput input1 = (UIInput) compositeComponent1.findComponent("testComponentNoTarget");
        Assert.assertNotNull(input1);
        Assert.assertNotNull(input1.getValidators());
        Assert.assertEquals(1, input1.getValidators().length);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        input1.pushComponentToEL(facesContext,  input1);
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assert.assertTrue(bean.isValidator1Called());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);

        UINamingContainer compositeComponent1inner = (UINamingContainer) compositeComponent1.findComponent("simpleAttributeActionMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent1inner);
        UIInput testComponentNoTarget1 = (UIInput) compositeComponent1inner.findComponent("testComponentNoTarget");
        Assert.assertNotNull(testComponentNoTarget1);
        Assert.assertNotNull(testComponentNoTarget1.getValidators());
        Assert.assertEquals(1, testComponentNoTarget1.getValidators().length);

        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1inner);
        bean.setValidator1Called(false);
        testComponentNoTarget1.getValidators()[0].validate(facesContext, testComponentNoTarget1, "x");
        Assert.assertTrue(bean.isValidator1Called());
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
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        
        UINamingContainer compositeComponent1n1 = (UINamingContainer) compositeComponent1.findComponent("compositeAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent1n1);
        UINamingContainer compositeComponent1n2 = (UINamingContainer) compositeComponent1n1.findComponent("simpleAttributeMethodExpressionNoTarget");
        Assert.assertNotNull(compositeComponent1n2);
        UIInput input1 = (UIInput) compositeComponent1n2.findComponent("testComponentNoTarget");
        Assert.assertNotNull(input1);
        Assert.assertNotNull(input1.getValidators());
        Assert.assertEquals(1, input1.getValidators().length);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1n2);
        bean.setValidator1Called(false);
        input1.getValidators()[0].validate(facesContext, input1, "x");
        Assert.assertTrue(bean.isValidator1Called());
        compositeComponent1n2.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
    }
}
