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
package org.apache.myfaces.application;

import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorBase;
import javax.faces.component.behavior.FacesBehavior;

import org.apache.myfaces.component.ComponentResourceContainer;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.base.junit.AbstractJsfConfigurableMockTestCase;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.junit.Assert;
import org.junit.Test;

public class ClientBehaviorTestCase extends AbstractJsfConfigurableMockTestCase
{

    public ClientBehaviorTestCase()
    {
    }

    @Override
    protected void setFactories() throws Exception
    {
        super.setFactories();
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                ApplicationFactoryImpl.class.getName());
        FactoryFinder.setFactory(
                FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                "org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl");
    }

    @Override
    protected void setUpExternalContext() throws Exception
    {
        super.setUpExternalContext();
        //Set RuntimeConfig object properly to make work ValueExpressions 
        RuntimeConfig.getCurrentInstance(externalContext).setExpressionFactory(
                new MockExpressionFactory());
    }

    @Override
    protected void setUpApplication() throws Exception
    {
        super.setUpApplication();
        //We need this two components added
        application.addComponent(UIOutput.COMPONENT_TYPE, UIOutput.class
                .getName());
        application.addComponent(UIPanel.COMPONENT_TYPE, UIPanel.class
                .getName());
        application.addComponent(ComponentResourceContainer.COMPONENT_TYPE, 
                ComponentResourceContainer.class.getName());
    }

    @Override
    public void tearDown() throws Exception
    {
        RuntimeConfig.getCurrentInstance(externalContext).purge();
        super.tearDown();
    }

    @FacesBehavior("org.apache.myfaces.component.MockClientBehavior")
    @ResourceDependencies({
      @ResourceDependency(name="test.js", library="test", target="head")
    })
    public static class MockClientBehavior extends ClientBehaviorBase
    {
    }

    public static class UITestComponentWithBehavior extends UIComponentBase
    {
        public static final String COMPONENT_TYPE = "javax.faces.UITestComponentWithBehavior";
        public static final String COMPONENT_FAMILY = "javax.faces.UITestComponentWithBehavior";
        public static final String DEFAULT_RENDERER_TYPE = "javax.faces.UITestComponentWithBehavior";

        static private final java.util.Collection<String> CLIENT_EVENTS_LIST =
            java.util.Collections.unmodifiableCollection(
                java.util.Arrays.asList(
                  "click"
            ));

        public UITestComponentWithBehavior()
        {
            setRendererType(DEFAULT_RENDERER_TYPE);
        }

        @Override
        public String getFamily()
        {
            return COMPONENT_FAMILY;
        }

        public java.util.Collection<String> getEventNames()
        {
            return CLIENT_EVENTS_LIST;
        }
    }

    @Test
    public void testAddBehaviorWithResourceDependencies() throws Exception
    {

        application.addComponent(UITestComponentWithBehavior.COMPONENT_TYPE,
                UITestComponentWithBehavior.class.getName());
        application.addComponent(UIOutput.COMPONENT_TYPE,
                UIOutput.class.getName());

        UITestComponentWithBehavior comp = (UITestComponentWithBehavior)
            application.createComponent(UITestComponentWithBehavior.COMPONENT_TYPE);


        application.addBehavior("myBehaviorId", MockClientBehavior.class.getName());
        ClientBehavior behavior = (ClientBehavior) application.createBehavior("myBehaviorId");
        comp.addClientBehavior("click", behavior);

        // verify that method still works
        Assert.assertTrue(comp.getClientBehaviors().get("click").contains(behavior));

        // get behavior resource
        List<UIComponent> resources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        Assert.assertEquals(1, resources.size());
        Map<String,Object> attrMap = resources.get(0).getAttributes();
        Assert.assertEquals("test.js", attrMap.get("name"));
    }
}
