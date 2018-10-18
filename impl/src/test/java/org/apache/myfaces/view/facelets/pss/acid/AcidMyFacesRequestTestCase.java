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
package org.apache.myfaces.view.facelets.pss.acid;

import java.util.Set;
import java.util.TreeSet;
import javax.el.ExpressionFactory;
import javax.faces.application.StateManager;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewParameter;
import javax.faces.component.html.HtmlDataTable;
import org.apache.myfaces.mc.test.core.AbstractMyFacesCDIRequestTestCase;

import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.apache.myfaces.view.facelets.pss.acid.component.UISimpleComponent1;
import org.apache.myfaces.view.facelets.pss.acid.component.UISimpleComponent2;
import org.apache.myfaces.view.facelets.pss.acid.managed.CheckActionEventBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.ComponentBindingBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.ComponentBindingFormBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.CustomSessionBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.ForEachBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.ResourceDependencyBean;
import org.apache.myfaces.view.facelets.pss.acid.managed.AcidTestBean;
import org.junit.Assert;
import org.junit.Test;

public class AcidMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase
{

    @Override
    protected boolean isScanAnnotations()
    {
        return true;
    }

    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.pss.acid");
        servletContext.addInitParameter("javax.faces.FACELETS_LIBRARIES", "/WEB-INF/testcomponent.taglib.xml");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
    }
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }    
    
    @Test
    public void testIndex() throws Exception
    {
        startViewRequest("/index.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        
        /*
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer1.content()).contains(
                "<div style=\"border: 1px solid red; margin: 2px\">" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-bottom: 5px; display:block\">" +
                "TestComponent::encodeBegin <span style=\"color: #888888\">(1 children)</span>" +
                "</div>" +
                "Dynamically added child" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-top: 5px; display:block\">TestComponent::encodeEnd</div></div>"));
                */
        //System.out.println(writer1.content());
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        /*
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer2.content()).contains(
                "<div style=\"border: 1px solid red; margin: 2px\">" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-bottom: 5px; display:block\">" +
                "TestComponent::encodeBegin <span style=\"color: #888888\">(1 children)</span>" +
                "</div>" +
                "Dynamically added child" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-top: 5px; display:block\">TestComponent::encodeEnd</div></div>"));
                */
        //System.out.println(writer2.content());
        endRequest();
    }
    
    @Test
    public void testInput() throws Exception
    {
        startViewRequest("/input.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals(1, comp.getChildren().get(0).getChildCount());
        Assert.assertEquals("mainForm:input", comp.getChildren().get(0).getChildren().get(0).getClientId(facesContext));
        /*
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer1.content()).contains(
               "<div style=\"border: 1px solid red; margin: 2px\">" +
               "<div style=\"background-color: #ffc0c0; padding: 2px; margin-bottom: 5px; display:block\">" +
               "TestComponent::encodeBegin <span style=\"color: #888888\">(1 children)</span>" +
               "</div>" +
               "<span style=\"border: 1px dashed blue; padding: 5px; margin: 5px\">" +
               "<input id=\"mainForm:input\" name=\"mainForm:input\" type=\"text\" value=\"Foo\" style=\"background-color: red\" />" +
               "</span>" +
               "<div style=\"background-color: #ffc0c0; padding: 2px; margin-top: 5px; display:block\">TestComponent::encodeEnd</div></div>"));
               */
        //System.out.println(writer1.content());
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals(1, comp.getChildren().get(0).getChildCount());
        Assert.assertEquals("mainForm:input", comp.getChildren().get(0).getChildren().get(0).getClientId(facesContext));

        /*
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        System.out.println(writer2.content());
        Assert.assertTrue(new String(writer2.content()).contains(
                "<div style=\"border: 1px solid red; margin: 2px\">" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-bottom: 5px; display:block\">" +
                "TestComponent::encodeBegin <span style=\"color: #888888\">(1 children)</span>" +
                "</div>" +
                "<span style=\"border: 1px dashed blue; padding: 5px; margin: 5px\">" +
                "<input id=\"mainForm:input\" name=\"mainForm:input\" type=\"text\" value=\"Foo\" style=\"background-color: red\" />" +
                "</span>" +
                "<div style=\"background-color: #ffc0c0; padding: 2px; margin-top: 5px; display:block\">TestComponent::encodeEnd</div></div>"));
                */
    }
    
    @Test
    public void testRecursive() throws Exception
    {
        startViewRequest("/recursive.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals(1, comp.getChildren().get(1).getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(1).getChildren().get(0).getAttributes().get("value"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals(1, comp.getChildren().get(1).getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(1).getChildren().get(0).getAttributes().get("value"));
        
        endRequest();
    }
    
    @Test
    public void testStable() throws Exception
    {
        startViewRequest("/stable.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        UIInput input = (UIInput) facesContext.getViewRoot().findComponent("mainForm:text3");
        client.inputText(input, "3");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());
        
    }
    
    @Test
    public void testTable() throws Exception
    {
        startViewRequest("/table.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        HtmlDataTable dataTable = (HtmlDataTable) comp.getChildren().get(0);
        Assert.assertEquals(1, dataTable.getChildCount());

        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        dataTable = (HtmlDataTable) comp.getChildren().get(0);
        Assert.assertEquals(1, dataTable.getChildCount());
    }
    
    @Test
    public void testToggle() throws Exception
    {
        startViewRequest("/toggle.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(1).getAttributes().get("value"));

        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
    }
    
    @Test
    public void testInclude() throws Exception
    {
        startViewRequest("/include.xhtml");
        processLifecycleExecute();
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());
        //Set a token to save on the state as delta
        UIComponent component = facesContext.getViewRoot().findComponent("mainForm:component1");
        component.getAttributes().put("test", "test1");
        renderResponse();
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page1");
        client.submit(button);
        processLifecycleExecute();
        //Check it is restored
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertEquals("test1", component.getAttributes().get("test"));
        renderResponse();
        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertEquals("test1", component.getAttributes().get("test"));
        
        //Go to page2
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);
        
        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        //Check it is restored
        Assert.assertEquals("test1", component.getAttributes().get("test"));
        
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());

        //Check the page was changed and the state discarded, because it is a different component.
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertNull(component.getAttributes().get("test"));
        //Set a token to save on the state as delta
        component.getAttributes().put("test", "test2");
        renderResponse();
        
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertEquals("test2", component.getAttributes().get("test"));

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);
        
        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        //Check it is restored
        Assert.assertNotNull("mainForm:component2 was not restored correctly",component);
        Assert.assertEquals("test2", component.getAttributes().get("test"));
        
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());

        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertEquals("test2", component.getAttributes().get("test"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);

        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        //Check it is restored
        Assert.assertEquals("test2", component.getAttributes().get("test"));
    }

    /**
     * Same as testInclude, but only check the component is restored correctly.
     * Since there is no delta, no state is saved unless it is necessary.
     * 
     * @throws Exception
     */
    @Test
    public void testInclude2() throws Exception
    {
        startViewRequest("/include.xhtml");
        processLifecycleExecute();
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());
        //Set a token to save on the state as delta
        UIComponent component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        renderResponse();
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page1");
        client.submit(button);
        
        processLifecycleExecute();
        //Check it is restored
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        renderResponse();
        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        
        //Go to page2
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);
        
        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        //Check it is restored
        Assert.assertNotNull(component);
        
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());

        //Check the page was changed and the state discarded, because it is a different component.
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertNotNull(component);
        renderResponse();
        
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertNotNull(component);

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);
        
        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        //Check it is restored
        Assert.assertNotNull(component);
        
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());

        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertNotNull(component);
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        client.submit(button);

        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        //Check it is restored
        Assert.assertNotNull(component);
    }
    
    /**
     * Check if a dynamic subtree can be created from a binding property, and if it
     * will be preserved across request. 
     * 
     * The idea is just inject a subtree using some code like this:
     * <code>&lt;h:panelGroup id="panel" binding="#{componentBindingBean.panel}"&gt;</code>
     * 
     * The solution is if a binding returns a component that has children or facets
     * attached, it is not elegible for PSS algorithm because the additional components
     * are created outside facelets control, and there is no warrant that the same structure
     * will be generated across requests, violating PSS base principle (it is possible to
     * restore to the initial state calling vdl.buildView).
     * 
     * This test is here because all state saving modes should support this method.
     * 
     * @throws Exception 
     */
    @Test
    public void testComponentBinding() throws Exception
    {
        startViewRequest("/componentBinding1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(1, comp.getChildCount());
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        // Even if in the postback two components were added, pss algorithm must replace the
        // component with the one saved.
        Assert.assertEquals(1, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp);
        // Even if in the postback two components were added, pss algorithm must replace the
        // component with the one saved.
        Assert.assertEquals(1, comp.getChildCount());
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        endRequest();
    }
    
    @Test
    public void testComponentBinding2() throws Exception
    {
        startViewRequest("/componentBinding2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        int fieldCount = comp.getChildCount();
        Set<String> clientIds = new TreeSet<String>();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        // Check the components are restored.
        Assert.assertEquals(fieldCount, comp.getChildCount());
        Set<String> clientIds2 = new TreeSet<String>();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());        

        ComponentBindingFormBean formBean = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{componentBindingFormBean}", ComponentBindingFormBean.class);
        formBean.forceRebuild();
        
        processLifecycleRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);

        processLifecycleExecute();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(fieldCount, comp.getChildCount());
        clientIds2.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());        
        
        formBean = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{componentBindingFormBean}", ComponentBindingFormBean.class);
        formBean.forceRebuild();
        
        processLifecycleRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);

        processLifecycleExecute();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(fieldCount, comp.getChildCount());
        clientIds2.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());        
        
        formBean = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{componentBindingFormBean}", ComponentBindingFormBean.class);
        formBean.forceRebuild();
        
        processLifecycleRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        
        
        endRequest();
    }    
    
    @Test
    public void testViewParamBinding() throws Exception
    {
        startViewRequest("/viewParamBinding1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(1, comp.getChildCount());
        
        UIViewParameter viewParam = (UIViewParameter) facesContext.getExternalContext().getRequestMap().get("foo");
        Assert.assertNotNull(viewParam);
        Assert.assertEquals("foo", viewParam.getName());
        UIViewParameter viewParam2 = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{componentBindingBean}", ComponentBindingBean.class).getViewParam();
        Assert.assertNotNull(viewParam2);
        Assert.assertEquals("foo2", viewParam2.getName());
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        // Even if in the postback two components were added, pss algorithm must replace the
        // component with the one saved.
        Assert.assertEquals(1, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp);
        // Even if in the postback two components were added, pss algorithm must replace the
        // component with the one saved.
        Assert.assertEquals(1, comp.getChildCount());
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        endRequest();
    }    
    
    @Test
    public void testDynamicForm() throws Exception
    {
        startViewRequest("/dynamicForm.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        int fieldCount = comp.getChildCount();
        Set<String> clientIds = new TreeSet<String>();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        // Check the components are restored.
        Assert.assertEquals(fieldCount, comp.getChildCount());
        Set<String> clientIds2 = new TreeSet<String>();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());
        
        processLifecycleRender();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        // Check the components are restored.
        Assert.assertEquals(fieldCount, comp.getChildCount());
        clientIds2.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());
        
        // Check the components are restored.
        processLifecycleRender();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        // Check the components are restored.
        Assert.assertEquals(fieldCount, comp.getChildCount());
        clientIds2.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());
        
        processLifecycleRender();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        // Check the components are restored.
        Assert.assertEquals(fieldCount, comp.getChildCount());
        clientIds2.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds2.add(c.getClientId(facesContext));
        }
        Assert.assertArrayEquals(clientIds.toArray(), clientIds2.toArray());
        
        processLifecycleRender();
        comp = facesContext.getViewRoot().findComponent("mainForm:dynPanel");
        Assert.assertNotNull(comp);
        fieldCount = comp.getChildCount();
        clientIds.clear();
        for (UIComponent c : comp.getChildren())
        {
            clientIds.add(c.getClientId(facesContext));
        }        
        
        endRequest();
    }
    
    @Test
    public void testResourceDependency() throws Exception
    {
        startViewRequest("/resourceDependency1.xhtml");
        processLifecycleExecute();

        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertEquals(1, headPanel.getChildCount());
        
        String nextUniqueId = facesContext.getViewRoot().createUniqueId(facesContext, null);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        
        processLifecycleExecute();
        
        ResourceDependencyBean bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{resourceDependencyBean}", ResourceDependencyBean.class);
        bean.setIncludeContent(true);
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertEquals(1, headPanel.getChildCount());
        Assert.assertNotSame(nextUniqueId, headPanel.getChildren().get(0).getId());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
    }

    @Test
    public void testResourceDependency2() throws Exception
    {
        startViewRequest("/resourceDependency2.xhtml");
        processLifecycleExecute();

        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIPanel headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNull(headPanel);
        
        String nextUniqueId = facesContext.getViewRoot().createUniqueId(facesContext, null);
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        
        processLifecycleExecute();
        
        ResourceDependencyBean bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{resourceDependencyBean}", ResourceDependencyBean.class);
        bean.setIncludeContent(true);
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertTrue(1 >= headPanel.getChildCount());
        Assert.assertNotSame(nextUniqueId, headPanel.getChildren().get(0).getId());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button2);
        
        processLifecycleExecute();
        
        bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{resourceDependencyBean}", ResourceDependencyBean.class);
        bean.setIncludeContent(false);
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertTrue(1 >= headPanel.getChildCount());
        //Assert.assertNotSame(nextUniqueId, headPanel.getChildren().get(0).getId());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);    
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button3);
        
        processLifecycleExecute();
        
        bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{resourceDependencyBean}", ResourceDependencyBean.class);
        bean.setIncludeContent(true);
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertTrue(1 >= headPanel.getChildCount());
        //Assert.assertNotSame(nextUniqueId, headPanel.getChildren().get(0).getId());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button4);
        
        processLifecycleExecute();
        
        bean = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{resourceDependencyBean}", ResourceDependencyBean.class);
        bean.setIncludeContent(false);
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        headPanel = (UIPanel) facesContext.getViewRoot().getFacet("head");
        Assert.assertNotNull(headPanel);
        Assert.assertTrue(1 >= headPanel.getChildCount());
        //Assert.assertNotSame(nextUniqueId, headPanel.getChildren().get(0).getId());
        
        executeViewHandlerRender(facesContext);
        executeAfterRender(facesContext);
    }

    @Test
    public void testAddSimpleComponentVDL() throws Exception
    {
        startViewRequest("/addSimpleComponentVDL.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));

        endRequest();
    }
    
    @Test
    public void testAddSimpleIncludeVDL_1() throws Exception
    {
        startViewRequest("/addSimpleIncludeVDL_1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertEquals(2, wrapper.getChildCount());
        Assert.assertEquals("Dynamically added child", wrapper.getChildren().get(1).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer1.content()).contains("Dynamically added markup"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertEquals(2, wrapper.getChildCount());
        Assert.assertEquals("Dynamically added child", wrapper.getChildren().get(1).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer2.content()).contains("Dynamically added markup"));

        endRequest();
    }
    
    @Test
    public void testAddSimpleIncludeVDL_2() throws Exception
    {
        startViewRequest("/addSimpleIncludeVDL_2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertEquals(3, wrapper.getChildCount());
        Assert.assertEquals("Dynamically added child", wrapper.getChildren().get(1).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content = new String(writer1.content());
        Assert.assertTrue(content.contains("Dynamically added markup"));
        Assert.assertTrue(content.contains("Value in param1: value1"));
        Assert.assertTrue(content.contains("Value in param2: value2"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        
        AcidTestBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext, "#{acidTestBean}", AcidTestBean.class);
        bean.setParam2("otherValue2");
        
        processLifecycleRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertEquals(3, wrapper.getChildCount());
        Assert.assertEquals("Dynamically added child", wrapper.getChildren().get(1).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        Assert.assertTrue(content2.contains("Value in param1: value1"));
        Assert.assertTrue(content2.contains("Value in param2: otherValue2"));

        endRequest();
    }
    
    @Test
    public void testAddSimpleCCVDL() throws Exception
    {
        startViewRequest("/addSimpleCCVDL.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added header", 
            ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer1.content()).contains("Dynamically added markup"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added header", 
            ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        Assert.assertTrue(new String(writer2.content()).contains("Dynamically added markup"));

        endRequest();
    }

    @Test
    public void testAddSimpleCCVDL2() throws Exception
    {
        startViewRequest("/addSimpleCCVDL2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added header"));
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added header"));
        Assert.assertTrue(content2.contains("Dynamically added markup"));

        endRequest();
    }

    @Test
    public void testAddSimpleCCVDL3() throws Exception
    {
        startViewRequest("/addSimpleCCVDL3.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2 = content2.indexOf("Dynamically added header", indexDynHeader1);
        int indexDynHeader3 = content2.indexOf("End Dynamic Header", indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader1);
        Assert.assertNotSame(-1, indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader3);

        endRequest();
    }

    @Test
    public void testAddSimpleCCVDL4() throws Exception
    {
        startViewRequest("/addSimpleCCVDL4.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2 = content2.indexOf("Dynamically added header", indexDynHeader1);
        int indexDynHeader3 = content2.indexOf("End Dynamic Header", indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader1);
        Assert.assertNotSame(-1, indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader3);

        endRequest();
    }
    
    @Test
    public void testAddSimpleCCVDL5() throws Exception
    {
        startViewRequest("/addSimpleCCVDL5.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);


        endRequest();
    }
    
    @Test
    public void testAddSimpleCCVDL6() throws Exception
    {
        startViewRequest("/addSimpleCCVDL6.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);

        endRequest();
    }
    
    @Test
    public void testAddSimpleCCVDL7() throws Exception
    {
        startViewRequest("/addSimpleCCVDL7.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        UIComponent wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        Assert.assertFalse(content1.contains("This is section 1"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();

        CustomSessionBean sessionBean = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{customSessionBean}", CustomSessionBean.class);
        // Here we change the value to show the section 1 part. If the refresh algorithm works, 
        // this part should be rendered
        sessionBean.setShowSection1(true);

        renderResponse();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        wrapper = comp.getChildren().get(0);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));        
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2 = content2.indexOf("Dynamically added header", indexDynHeader1);
        int indexDynHeader3 = content2.indexOf("End Dynamic Header", indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader1);
        Assert.assertNotSame(-1, indexDynHeader2);
        Assert.assertNotSame(-1, indexDynHeader3);
        
        Assert.assertTrue(content2.contains("This is section 1"));

        endRequest();
    }


    @Test
    public void testComponentBindingVDL_1() throws Exception
    {
        startViewRequest("/componentBindingVDL_1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added header", 
            ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();

        UIComponent comp2 = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp2);
        Assert.assertEquals(2, comp2.getChildCount());
        
        Assert.assertEquals("value1", comp2.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp2.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp2.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added header", 
            ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        
        renderResponse();
        
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();

        UIComponent comp3 = facesContext.getViewRoot().findComponent("panel");
        Assert.assertNotNull(comp3);
        Assert.assertEquals(2, comp3.getChildCount());
        
        Assert.assertEquals("value1", comp3.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp3.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp3.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added header", 
            ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        
        endRequest();
    }
    
    @Test
    public void testComponentBindingVDL_2() throws Exception
    {
        startViewRequest("/componentBindingVDL_2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added header"));
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        
        UIComponent ccpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel");
        Assert.assertNotNull(ccpanel);
        UIComponent ccinnerpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel:component");
        Assert.assertNotNull(ccinnerpanel);
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        
        CheckActionEventBean checkBean = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{checkActionEventBean}", CheckActionEventBean.class);
        int oldcount1 = checkBean.getActionListenerCount();
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount1+1, checkBean.getActionListenerCount());
        renderResponse();

        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added header"));
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        int oldcount2 = checkBean.getActionListenerCount();        
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount2+1, checkBean.getActionListenerCount());
        
        renderResponse();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added header"));
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        
        endRequest();
    }
    
    @Test
    public void testComponentBindingVDL_3() throws Exception
    {
        startViewRequest("/componentBindingVDL_3.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        int indexDynHeader3_1 = content3.indexOf("Start Dynamic Header");
        int indexDynHeader3_2 = content3.indexOf("Dynamically added header", indexDynHeader3_1);
        int indexDynHeader3_3 = content3.indexOf("End Dynamic Header", indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_1);
        Assert.assertNotSame(-1, indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_3);

        endRequest();
    }
    
    @Test
    public void testComponentBindingVDL_4() throws Exception
    {
        startViewRequest("/componentBindingVDL_4.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        int indexDynHeader3_1 = content3.indexOf("Start Dynamic Header");
        int indexDynHeader3_2 = content3.indexOf("Dynamically added header", indexDynHeader3_1);
        int indexDynHeader3_3 = content3.indexOf("End Dynamic Header", indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_1);
        Assert.assertNotSame(-1, indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_3);

        endRequest();
    }
    
    @Test
    public void testComponentBindingVDL_5() throws Exception
    {
        startViewRequest("/componentBindingVDL_5.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UIComponent ccpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel");
        Assert.assertNotNull(ccpanel);
        UIComponent ccinnerpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel:component");
        Assert.assertNotNull(ccinnerpanel);
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        
        CheckActionEventBean checkBean = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{checkActionEventBean}", CheckActionEventBean.class);
        int oldcount1 = checkBean.getActionListenerCount();
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount1+1, checkBean.getActionListenerCount());
        renderResponse();

        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        int oldcount2 = checkBean.getActionListenerCount();        
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount2+1, checkBean.getActionListenerCount());
        
        renderResponse();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        int indexDynHeader3_1 = content3.indexOf("Start Dynamic Header");
        int indexDynHeader3_2 = content3.indexOf("Dynamically added header", indexDynHeader3_1);
        int indexDynHeader3_3 = content3.indexOf("End Dynamic Header", indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_1);
        Assert.assertNotSame(-1, indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_3);
        
        endRequest();
    }

    @Test
    public void testComponentBindingVDL_6() throws Exception
    {
        startViewRequest("/componentBindingVDL_6.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        UIComponent ccpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel");
        Assert.assertNotNull(ccpanel);
        UIComponent ccinnerpanel = facesContext.getViewRoot().findComponent("mainForm:ccpanel:component");
        Assert.assertNotNull(ccinnerpanel);
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        
        CheckActionEventBean checkBean = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{checkActionEventBean}", CheckActionEventBean.class);
        int oldcount1 = checkBean.getActionListenerCount();
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount1+1, checkBean.getActionListenerCount());
        renderResponse();

        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:ccpanel:component:increment");
        Assert.assertNotNull(button);
        int oldcount2 = checkBean.getActionListenerCount();        
        
        client.submit(button);
        processLifecycleExecute();
        
        Assert.assertEquals("event not called", oldcount2+1, checkBean.getActionListenerCount());
        
        renderResponse();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        int indexDynHeader3_1 = content3.indexOf("Start Dynamic Header");
        int indexDynHeader3_2 = content3.indexOf("Dynamically added header", indexDynHeader3_1);
        int indexDynHeader3_3 = content3.indexOf("End Dynamic Header", indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_1);
        Assert.assertNotSame(-1, indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_3);
        
        endRequest();
    }
    
    @Test
    public void testComponentBindingVDL_7() throws Exception
    {
        startViewRequest("/componentBindingVDL_7.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        UIComponent wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        UIComponent ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("Dynamically added markup"));
        int indexDynHeader1_1 = content1.indexOf("Start Dynamic Header");
        int indexDynHeader1_2 = content1.indexOf("Dynamically added header", indexDynHeader1_1);
        int indexDynHeader1_3 = content1.indexOf("End Dynamic Header", indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_1);
        Assert.assertNotSame(-1, indexDynHeader1_2);
        Assert.assertNotSame(-1, indexDynHeader1_3);
        
        Assert.assertFalse(content1.contains("This is section 1"));

        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        
        CustomSessionBean sessionBean = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{customSessionBean}", CustomSessionBean.class);
        // Here we change the value to show the section 1 part. If the refresh algorithm works, 
        // this part should be rendered
        sessionBean.setShowSection1(true);
        
        renderResponse();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());
        
        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("Dynamically added markup"));
        int indexDynHeader2_1 = content2.indexOf("Start Dynamic Header");
        int indexDynHeader2_2 = content2.indexOf("Dynamically added header", indexDynHeader2_1);
        int indexDynHeader2_3 = content2.indexOf("End Dynamic Header", indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_1);
        Assert.assertNotSame(-1, indexDynHeader2_2);
        Assert.assertNotSame(-1, indexDynHeader2_3);
        
        Assert.assertTrue(content2.contains("This is section 1"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecute();
        
        sessionBean.setShowSection1(false);
        
        renderResponse();
        Assert.assertNotNull(comp);
        Assert.assertEquals(2, comp.getChildCount());

        comp = facesContext.getViewRoot().findComponent("mainForm:panel");
        
        Assert.assertEquals("value1", comp.getAttributes().get("attr1"));
        Assert.assertEquals("value2", comp.getChildren().get(0).getAttributes().get("attr2"));
        
        wrapper = comp.getChildren().get(1);
        Assert.assertNotNull(wrapper);
        Assert.assertTrue(UIComponent.isCompositeComponent(wrapper));
        ccContent = wrapper.getFacet(UIComponent.COMPOSITE_FACET_NAME);
        Assert.assertNotNull(ccContent);
        Assert.assertEquals(3, ccContent.getChildCount());
        //Assert.assertEquals("Dynamically added header", 
        //    ccContent.getChildren().get(0).getFacet("header").getAttributes().get("value"));
        Assert.assertEquals("Dynamically added child", ccContent.getChildren().get(2).getAttributes().get("value"));
        MockPrintWriter writer3 = (MockPrintWriter) response.getWriter();
        String content3 = new String(writer3.content());
        Assert.assertTrue(content3.contains("Dynamically added markup"));
        int indexDynHeader3_1 = content3.indexOf("Start Dynamic Header");
        int indexDynHeader3_2 = content3.indexOf("Dynamically added header", indexDynHeader3_1);
        int indexDynHeader3_3 = content3.indexOf("End Dynamic Header", indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_1);
        Assert.assertNotSame(-1, indexDynHeader3_2);
        Assert.assertNotSame(-1, indexDynHeader3_3);
        
        Assert.assertFalse(content3.contains("This is section 1"));

        endRequest();
    }

    @Test
    public void testCForEach1() throws Exception
    {
        startViewRequest("/forEach1.xhtml");
        processLifecycleExecute();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);
        
        UIOutput itemA_1 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_a");
        Assert.assertNotNull(itemA_1);
        Assert.assertEquals("a", itemA_1.getValue());
        itemA_1.getAttributes().put("prop", "a");
        UIOutput itemB_1 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_b");
        Assert.assertNotNull(itemB_1);
        Assert.assertEquals("b", itemB_1.getValue());
        itemB_1.getAttributes().put("prop", "b");
        UIOutput itemC_1 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_c");
        Assert.assertNotNull(itemC_1);
        Assert.assertEquals("c", itemC_1.getValue());
        itemC_1.getAttributes().put("prop", "c");
        
        executeViewHandlerRender(facesContext);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        
        processLifecycleExecute();

        UIOutput itemA_2 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_a");
        Assert.assertNotNull(itemA_2);
        Assert.assertEquals("a", itemA_2.getValue());
        Assert.assertEquals("a", itemA_2.getAttributes().get("prop"));
        UIOutput itemB_2 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_b");
        Assert.assertNotNull(itemB_2);
        Assert.assertEquals("b", itemB_2.getValue());
        Assert.assertEquals("b", itemB_2.getAttributes().get("prop"));
        UIOutput itemC_2 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_c");
        Assert.assertNotNull(itemC_2);
        Assert.assertEquals("c", itemC_2.getValue());
        Assert.assertEquals("c", itemC_2.getAttributes().get("prop"));

        ForEachBean bean = facesContext.getApplication().evaluateExpressionGet(facesContext, "#{forEachBean}", 
            ForEachBean.class);
        bean.addFirst();
        bean.addMiddle();
        bean.removeLast();
        
        executeBeforeRender(facesContext);
        executeBuildViewCycle(facesContext);

        UIOutput itemA_3 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_a");
        Assert.assertNotNull(itemA_3);
        Assert.assertEquals("a", itemA_3.getValue());
        Assert.assertEquals("a", itemA_3.getAttributes().get("prop"));
        UIOutput itemB_3 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_b");
        Assert.assertNotNull(itemB_3);
        Assert.assertEquals("b", itemB_3.getValue());
        Assert.assertEquals("b", itemB_3.getAttributes().get("prop"));
        UIOutput itemC_3 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_c");
        Assert.assertNull(itemC_3);
        UIOutput itemZ_3 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_z");
        Assert.assertNotNull(itemZ_3);
        Assert.assertEquals("z", itemZ_3.getValue());
        Assert.assertNull(itemZ_3.getAttributes().get("prop"));
        UIOutput itemX_3 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_x");
        Assert.assertNotNull(itemX_3);
        Assert.assertEquals("x", itemX_3.getValue());
        Assert.assertNull(itemX_3.getAttributes().get("prop"));
        
        executeViewHandlerRender(facesContext);

        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button2);
        
        processLifecycleExecute();

        UIOutput itemA_4 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_a");
        Assert.assertNotNull(itemA_4);
        Assert.assertEquals("a", itemA_4.getValue());
        UIOutput itemB_4 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_b");
        Assert.assertNotNull(itemB_4);
        Assert.assertEquals("b", itemB_4.getValue());
        UIOutput itemC_4 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_c");
        Assert.assertNull(itemC_4);
        UIOutput itemZ_4 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_z");
        Assert.assertNotNull(itemZ_4);
        Assert.assertEquals("z", itemZ_4.getValue());
        UIOutput itemX_4 = (UIOutput) facesContext.getViewRoot().findComponent("mainForm:item_x");
        Assert.assertNotNull(itemX_4);
        Assert.assertEquals("x", itemX_4.getValue());

        endRequest();
    }

    /**
     * Check for StackoverflowException when this subscription:
     * 
     * @ListenerFor(systemEventClass = PostRestoreStateEvent.class)
     * 
     * is used.
     * 
     * @throws Exception 
     */
    @Test
    public void testSimpleComponent1() throws Exception
    {
        startViewRequest("/simpleComponent1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof UISimpleComponent1);
        //Assert.assertEquals(1, comp.getChildCount());
        //Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof UISimpleComponent1);

        endRequest();
    }
    
    @Test
    public void testSimpleComponent2() throws Exception
    {
        startViewRequest("/simpleComponent2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof UISimpleComponent2);
        //Assert.assertEquals(1, comp.getChildCount());
        //Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        client.submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof UISimpleComponent2);

        endRequest();
    }
    
    @Test
    public void testUIRepeatCC1() throws Exception
    {
        startViewRequest("/nested_ui_repeat_1.xhtml");
        processLifecycleExecuteAndRender();
        
        MockPrintWriter writer1 = (MockPrintWriter) response.getWriter();
        String content1 = new String(writer1.content());
        Assert.assertTrue(content1.contains("A-"));
        Assert.assertTrue(content1.contains("B-"));
        Assert.assertTrue(content1.contains("C-"));
        
        this.client.ajax("mainForm:j_id_7:1:j_id_8:refresh", "action", 
                "mainForm:j_id_7:1:j_id_8:refresh", 
                "mainForm:j_id_7:1:j_id_8:compToUpdate", true);
        
        processLifecycleExecuteAndRender();
        
        MockPrintWriter writer2 = (MockPrintWriter) response.getWriter();
        String content2 = new String(writer2.content());
        Assert.assertTrue(content2.contains("mainForm:j_id_7:1:j_id_8:compToUpdate"));
        Assert.assertTrue(content2.contains("B-"));

        endRequest();
    }
}
