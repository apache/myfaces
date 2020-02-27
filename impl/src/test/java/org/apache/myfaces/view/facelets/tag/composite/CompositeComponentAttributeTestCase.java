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

import java.beans.BeanDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.util.Map;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;

import org.apache.myfaces.test.mock.MockResponseWriter;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;
import org.apache.myfaces.test.utils.HtmlRenderedAttr;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.bean.DummyBean;
import org.apache.myfaces.view.facelets.bean.HelloWorld;
import org.junit.Assert;
import org.junit.Test;

public class CompositeComponentAttributeTestCase extends FaceletTestCase
{

    @Override
    protected void setupComponents() throws Exception
    {
        super.setupComponents();
        application.addComponent(CompositeTestComponent.class.getName(), 
                CompositeTestComponent.class.getName());
    }

    /**
     * Test simple attribute resolution (not set, default, normal use case).
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleCompositeAttribute() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeVE.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlOutputText text1 = (HtmlOutputText) facet1.findComponent("text");
        Assert.assertNotNull(text1);
        HtmlCommandButton button1 = (HtmlCommandButton) facet1.findComponent("button");
        Assert.assertNotNull(button1);
        HtmlInputText input1 = (HtmlInputText) facet1.findComponent("input");
        Assert.assertNotNull(input1);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        text1.pushComponentToEL(facesContext, text1);
        //set on tag
        Assert.assertEquals(bean.getStyleClass(), text1.getStyleClass());
        //set as default
        Assert.assertEquals(bean.getStyle(), text1.getStyle());
        
        Assert.assertEquals(bean.getJavaProperty(), text1.getValue());
        
        Assert.assertEquals(bean.getValue(), input1.getValue());
        Assert.assertEquals(true, input1.isRequired());
        
        text1.popComponentFromEL(facesContext);
        button1.pushComponentToEL(facesContext,  button1);
        MethodExpression method = button1.getActionExpression();
        Assert.assertEquals(bean.doSomethingFunny("xysj"), method.invoke(facesContext.getELContext(), new Object[]{"xysj"}));
        button1.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
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
    
    /**
     * Test simple attribute resolution (not set, default, normal use case).
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleCompositeAttributeInsertChildren() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleAttributeVEInsertChildren.xhtml");

        UIComponent panelGroup1 = root.findComponent("testGroup1");
        Assert.assertNotNull(panelGroup1);
        CompositeTestComponent compositeComponent1 = (CompositeTestComponent) panelGroup1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        UIComponent compositeComponent2 = facet1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet2);
        HtmlOutputText text1 = (HtmlOutputText) facet2.findComponent("text");
        Assert.assertNotNull(text1);
        HtmlCommandButton button1 = (HtmlCommandButton) facet2.findComponent("button");
        Assert.assertNotNull(button1);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        text1.pushComponentToEL(facesContext, text1);
        //set on tag
        Assert.assertEquals(bean.getStyleClass(), text1.getStyleClass());
        //set as default
        Assert.assertEquals(bean.getStyle(), text1.getStyle());
        
        Assert.assertEquals(bean.getJavaProperty(), text1.getValue());
        
        text1.popComponentFromEL(facesContext);
        button1.pushComponentToEL(facesContext,  button1);
        MethodExpression method = button1.getActionExpression();
        Assert.assertEquals(bean.doSomethingFunny("xysj"), method.invoke(facesContext.getELContext(), new Object[]{"xysj"}));
        button1.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
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
    public void testSimpleMethodInvocation() throws Exception
    {
        DummyBean dummyBean = new DummyBean(); 
        
        facesContext.getExternalContext().getRequestMap().put("dummyBean",
                dummyBean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testSimpleMethodInvocation.xhtml");
        
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
        UIForm form = (UIForm) facet2.findComponent("mainForm");
        Assert.assertNotNull(form);
        UICommand button1 = (UICommand) form.findComponent("button1");
        Assert.assertNotNull(button1);
        UICommand button2 = (UICommand) form.findComponent("button2");
        Assert.assertNotNull(button2);
        UICommand button3 = (UICommand) form.findComponent("button3");
        Assert.assertNotNull(button3);
        UIInput text1 = (UIInput) form.findComponent("text1");
        Assert.assertNotNull(text1);
        UIInput text2 = (UIInput) form.findComponent("text2");
        Assert.assertNotNull(text2);

        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        facet2.pushComponentToEL(facesContext, facet2);
        form.pushComponentToEL(facesContext, form);
        
        button1.pushComponentToEL(facesContext, button1);
        button1.getActionExpression().invoke(facesContext.getELContext(), new Object[]{});
        button1.popComponentFromEL(facesContext);

        button2.pushComponentToEL(facesContext, button2);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        button2.popComponentFromEL(facesContext);

        button3.pushComponentToEL(facesContext, button3);
        button3.getActionExpression().invoke(facesContext.getELContext(), new Object[]{});
        button3.popComponentFromEL(facesContext);

        text1.pushComponentToEL(facesContext, text1);
        text1.getValidators()[0].validate(facesContext, text1, "");
        text1.popComponentFromEL(facesContext);

        text2.pushComponentToEL(facesContext, text2);
        text2.getValueChangeListeners()[0].processValueChange(new ValueChangeEvent(text2, "old", "new"));
        text2.popComponentFromEL(facesContext);

        form.popComponentFromEL(facesContext);
        facet2.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);

        compositeComponent1.encodeAll(facesContext);

        sw.flush();
        
        String resp = sw.toString();
    }
    
    @Test
    public void testCompositeActionMethodInvocation() throws Exception
    {
        HelloWorld helloWorld = new HelloWorld(); 
        
        facesContext.getExternalContext().getRequestMap().put("helloWorldBean",
                helloWorld);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeActionMethodInvocation.xhtml");
        
        UIComponent form = root.findComponent("testForm1");
        Assert.assertNotNull(form);
        UINamingContainer compositeComponent = (UINamingContainer) form.getChildren().get(0);
        Assert.assertNotNull(compositeComponent);
        UINamingContainer compositeComponent2 = (UINamingContainer) compositeComponent.findComponent("button1");
        Assert.assertNotNull(compositeComponent2);
        UICommand button = (UICommand) compositeComponent2.findComponent("button2");
        Assert.assertNotNull(button);
        
        Assert.assertNotNull(button.getActionExpression());
        
        Assert.assertEquals("success", button.getActionExpression().invoke(facesContext.getELContext(), null));
        
        Assert.assertEquals(1, button.getActionListeners().length);
        
        //StringWriter sw = new StringWriter();
        //MockResponseWriter mrw = new MockResponseWriter(sw);
        //facesContext.setResponseWriter(mrw);
        
        //root.encodeAll(facesContext);
        //sw.flush();
        //System.out.print(sw.toString());
    }    
    
    /**
     * Tests if unspecified attributes on <composite:interface>, <composite:attribute>
     * and <composite:facet> are handled correctly.
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testUnspecifiedAttributes() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testInterfaceDescriptorAttributes.xhtml");
        
        // get the composite component and its BeanInfo
        UIComponent composite = root.findComponent("panel").getChildren().get(0);
        CompositeComponentBeanInfo beanInfo = 
            (CompositeComponentBeanInfo) composite.getAttributes()
            .get(UIComponent.BEANINFO_KEY);
        Assert.assertNotNull(beanInfo);
        
        // get the <composite:interface> descriptor and check the unspecified attribute
        BeanDescriptor interfaceDescriptor = beanInfo.getBeanDescriptor();
        _checkUnspecifiedAttribute(interfaceDescriptor, 
                "unspecifiedInterfaceAttribute", "unspecifiedInterfaceValue");
        
        // check <composite:attribute>
        Assert.assertEquals("Expecting one <composite:attribute>",
                1, beanInfo.getPropertyDescriptors().length);
        PropertyDescriptor attributeDescriptor = beanInfo.getPropertyDescriptors()[0];
        _checkUnspecifiedAttribute(attributeDescriptor, 
                "unspecifiedAttributeAttribute", "unspecifiedAttributeValue");
        
        // check <composite:facet>
        Map<String, PropertyDescriptor> facetPropertyDescriptorMap = 
            (Map<String, PropertyDescriptor>) interfaceDescriptor.getValue(UIComponent.FACETS_KEY);
        Assert.assertNotNull(facetPropertyDescriptorMap);
        PropertyDescriptor facetDescriptor = facetPropertyDescriptorMap.get("facet");
        _checkUnspecifiedAttribute(facetDescriptor, 
                "unspecifiedFacetAttribute", "unspecifiedFacetValue");
    }
    
    /**
     * Assertions for testUnspecifiedAttributes()
     * @param descriptor
     * @param attributeName
     * @param attributeValue
     */
    private void _checkUnspecifiedAttribute(FeatureDescriptor descriptor,
            final String attributeName, final String attributeValue)
    {
        Object value = descriptor.getValue(attributeName);
        Assert.assertTrue("Unspecified attributes must be stored as a ValueExpression",
                value instanceof ValueExpression);
        Assert.assertEquals(attributeValue, 
                ((ValueExpression) value).getValue(facesContext.getELContext()));
    }
    
    /**
     * The "displayName", "shortDescription", "expert", "hidden", and "preferred"
     * attributes are only exposed, if ProjectStage equals Development. This test
     * case tests exactly this case.
     * @throws Exception
     */
    @Test
    public void testDevelopmentValuesDevelopmentStage() throws Exception
    {
        _testDevelopmentValues(ProjectStage.Development);
    }
    
    /**
     * The "displayName", "shortDescription", "expert", "hidden", and "preferred"
     * attributes are only exposed, if ProjectStage equals Development. This test
     * case tests the case when ProjectStage equals Production, thus the values
     * must not be exposed.
     * @throws Exception
     */
    @Test
    public void testDevelopmentValuesProductionStage() throws Exception
    {
        _testDevelopmentValues(ProjectStage.Production);
    }
    
    /**
     * Generic test code for testDevelopmentValuesDevelopmentStage()
     * and testDevelopmentValuesProductionStage().
     * @param stage
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void _testDevelopmentValues(ProjectStage stage) throws Exception
    {
        final boolean development = stage.equals(ProjectStage.Development);
        
        // set ProjectStage accordingly
        setProjectStage(stage);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testInterfaceDescriptorAttributes.xhtml");
        
        // get the composite component and its BeanInfo
        UIComponent composite = root.findComponent("panel").getChildren().get(0);
        CompositeComponentBeanInfo beanInfo = 
            (CompositeComponentBeanInfo) composite.getAttributes()
            .get(UIComponent.BEANINFO_KEY);
        Assert.assertNotNull(beanInfo);
        
        // check <composite:interface>
        BeanDescriptor interfaceDescriptor = beanInfo.getBeanDescriptor();
        _checkDevelopmentValues(interfaceDescriptor, "interfaceDisplayName",
                "interfaceShortDescription", development);
        
        // check <composite:attribute>
        Assert.assertEquals("Expecting one <composite:attribute>",
                1, beanInfo.getPropertyDescriptors().length);
        PropertyDescriptor attributeDescriptor = beanInfo.getPropertyDescriptors()[0];
        _checkDevelopmentValues(attributeDescriptor, "attributeDisplayName",
                "attributeShortDescription", development);
        
        // check <composite:facet>
        Map<String, PropertyDescriptor> facetPropertyDescriptorMap = 
            (Map<String, PropertyDescriptor>) interfaceDescriptor.getValue(UIComponent.FACETS_KEY);
        Assert.assertNotNull(facetPropertyDescriptorMap);
        PropertyDescriptor facetDescriptor = facetPropertyDescriptorMap.get("facet");
        _checkDevelopmentValues(facetDescriptor, "facetDisplayName",
                "facetShortDescription", development);
    }
    
    /**
     * Assertions for _testDevelopmentValues()
     * @param descriptor
     * @param displayName
     * @param shortDescription
     * @param development
     */
    private void _checkDevelopmentValues(FeatureDescriptor descriptor,
            String displayName, String shortDescription, 
            final boolean development)
    {
        boolean booleanPropertiesValue;
        
        // set values for assertions depending on the ProjectStage
        if (development)
        {
            // if we have ProjectStage == Development, all values
            // will be set to true in the composite component facelet file
            booleanPropertiesValue = true;
            
            // displayName and shortDescription must equal the given values
        }
        else
        {
            // standard value of all boolean properties is false
            booleanPropertiesValue = false;
            
            // getDisplayName()'s default value is the return from getName()
            displayName = descriptor.getName();
            
            // getShortDescription()'s default value is the return from getDisplayName()
            shortDescription = displayName;
        }
        
        // Assertions
        Assert.assertEquals(displayName, descriptor.getDisplayName());
        Assert.assertEquals(shortDescription, descriptor.getShortDescription());
        Assert.assertEquals(booleanPropertiesValue, descriptor.isExpert());
        Assert.assertEquals(booleanPropertiesValue, descriptor.isHidden());
        Assert.assertEquals(booleanPropertiesValue, descriptor.isPreferred());
    }
    
    @Test
    public void testSimpleActionTargetAttributeName() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testActionTargetAttributeName.xhtml");

        UIComponent panelGroup1 = root.findComponent("mainForm:testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.findComponent("cc1");
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        HtmlCommandButton button1 = (HtmlCommandButton) facet1.findComponent("submitButton");
        Assert.assertNotNull(button1);
        HtmlCommandButton button2 = (HtmlCommandButton) facet1.findComponent("cancelAction");
        Assert.assertNotNull(button2);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        button1.pushComponentToEL(facesContext,  button1);

        MethodExpression method = button1.getActionExpression();
        Assert.assertEquals("testActionMethodTypeSubmit", method.invoke(facesContext.getELContext(), null));
        
        Assert.assertEquals(1, button1.getActionListeners().length);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        Assert.assertTrue(bean.isSubmitActionListenerCalled());
        
        button1.popComponentFromEL(facesContext);
        button2.pushComponentToEL(facesContext,  button2);
        
        method = button2.getActionExpression();
        Assert.assertEquals(bean.cancelAction(), method.invoke(facesContext.getELContext(), null));
        
        Assert.assertEquals(1, button2.getActionListeners().length);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        Assert.assertTrue(bean.isCancelActionListenerCalled());
        
        button2.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        /*
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("style")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        */
    }

    @Test
    public void testCompositeActionTargetAttributeName() throws Exception
    {
        MockAttributeBean bean = new MockAttributeBean();
        
        facesContext.getExternalContext().getRequestMap().put("bean",
                bean);
        
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "testCompositeActionTargetAttributeName.xhtml");

        UIComponent panelGroup1 = root.findComponent("mainForm:testGroup1");
        Assert.assertNotNull(panelGroup1);
        UINamingContainer compositeComponent1 = (UINamingContainer) panelGroup1.findComponent("cc1");
        Assert.assertNotNull(compositeComponent1);
        UIComponent facet1 = compositeComponent1.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet1);
        UINamingContainer compositeComponent2 = (UINamingContainer) facet1.getChildren().get(0);
        Assert.assertNotNull(compositeComponent2);
        UIComponent facet2 = compositeComponent2.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(facet2);
        
        
        HtmlCommandButton button1 = (HtmlCommandButton) facet2.findComponent("submitButton");
        Assert.assertNotNull(button1);
        HtmlCommandButton button2 = (HtmlCommandButton) facet2.findComponent("cancelAction");
        Assert.assertNotNull(button2);
        
        compositeComponent1.pushComponentToEL(facesContext, compositeComponent1);
        facet1.pushComponentToEL(facesContext, facet1);
        compositeComponent2.pushComponentToEL(facesContext, compositeComponent2);
        facet2.pushComponentToEL(facesContext, facet1);
        button1.pushComponentToEL(facesContext,  button1);

        MethodExpression method = button1.getActionExpression();
        Assert.assertEquals("testActionMethodTypeSubmit", method.invoke(facesContext.getELContext(), null));
        
        Assert.assertEquals(1, button1.getActionListeners().length);
        button1.getActionListeners()[0].processAction(new ActionEvent(button1));
        Assert.assertTrue(bean.isSubmitActionListenerCalled());
        
        button1.popComponentFromEL(facesContext);
        button2.pushComponentToEL(facesContext,  button2);
        
        method = button2.getActionExpression();
        Assert.assertEquals(bean.cancelAction(), method.invoke(facesContext.getELContext(), null));
        
        Assert.assertEquals(1, button2.getActionListeners().length);
        button2.getActionListeners()[0].processAction(new ActionEvent(button2));
        Assert.assertTrue(bean.isCancelActionListenerCalled());
        
        button2.popComponentFromEL(facesContext);
        facet2.popComponentFromEL(facesContext);
        compositeComponent2.popComponentFromEL(facesContext);
        facet1.popComponentFromEL(facesContext);
        compositeComponent1.popComponentFromEL(facesContext);
        
        StringWriter sw = new StringWriter();
        MockResponseWriter mrw = new MockResponseWriter(sw);
        facesContext.setResponseWriter(mrw);
        
        compositeComponent1.encodeAll(facesContext);
        sw.flush();
        
        /*
        HtmlRenderedAttr[] attrs = new HtmlRenderedAttr[]{
                new HtmlRenderedAttr("style")
        };
            
        HtmlCheckAttributesUtil.checkRenderedAttributes(attrs, sw.toString());
        */
    }
}
