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

import javax.faces.application.StateManager;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;

import junit.framework.Assert;

import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Test;

public class AcidMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
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
    
    @Test
    public void testIndex() throws Exception
    {
        setupRequest("/index.xhtml");
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
        submit(button);
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
        tearDownRequest();
    }
    
    @Test
    public void testInput() throws Exception
    {
        setupRequest("/input.xhtml");
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
        submit(button);
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
        setupRequest("/recursive.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals(1, comp.getChildren().get(1).getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(1).getChildren().get(0).getAttributes().get("value"));
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals(1, comp.getChildren().get(1).getChildCount());
        Assert.assertEquals("Dynamically added child", comp.getChildren().get(1).getChildren().get(0).getAttributes().get("value"));
        
        tearDownRequest();
    }
    
    @Test
    public void testStable() throws Exception
    {
        setupRequest("/stable.xhtml");
        processLifecycleExecuteAndRender();
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(3, comp.getChildCount());
        Assert.assertEquals("1", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("2", comp.getChildren().get(1).getAttributes().get("value"));
        Assert.assertEquals("text3", comp.getChildren().get(2).getId());

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        UIInput input = (UIInput) facesContext.getViewRoot().findComponent("mainForm:text3");
        inputText(input, "3");
        submit(button);
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
        setupRequest("/table.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        HtmlDataTable dataTable = (HtmlDataTable) comp.getChildren().get(0);
        Assert.assertEquals(1, dataTable.getChildCount());

        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(1, comp.getChildCount());
        dataTable = (HtmlDataTable) comp.getChildren().get(0);
        Assert.assertEquals(1, dataTable.getChildCount());
    }
    
    @Test
    public void testToggle() throws Exception
    {
        setupRequest("/toggle.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(1).getAttributes().get("value"));

        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();

        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:postback");
        submit(button);
        processLifecycleExecuteAndRender();
        
        comp = facesContext.getViewRoot().findComponent("mainForm:component");
        Assert.assertEquals(2, comp.getChildCount());
        Assert.assertEquals("Manually added child 1<br/>", comp.getChildren().get(0).getAttributes().get("value"));
        Assert.assertEquals("Manually added child 2<br/>", comp.getChildren().get(1).getAttributes().get("value"));
        
    }
    
    @Test
    public void testInclude() throws Exception
    {
        setupRequest("/include.xhtml");
        processLifecycleExecute();
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());
        //Set a token to save on the state as delta
        UIComponent component = facesContext.getViewRoot().findComponent("mainForm:component1");
        component.getAttributes().put("test", "test1");
        processRender();
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page1");
        submit(button);
        processLifecycleExecute();
        //Check it is restored
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertEquals("test1", component.getAttributes().get("test"));
        processRender();
        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertEquals("test1", component.getAttributes().get("test"));
        
        //Go to page2
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        submit(button);
        
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
        processRender();
        
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertEquals("test2", component.getAttributes().get("test"));

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        submit(button);
        
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
        submit(button);

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
        setupRequest("/include.xhtml");
        processLifecycleExecute();
        //Build the view
        facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(
                facesContext, facesContext.getViewRoot().getViewId()).buildView(facesContext, facesContext.getViewRoot());
        //Set a token to save on the state as delta
        UIComponent component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        processRender();
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page1");
        submit(button);
        
        processLifecycleExecute();
        //Check it is restored
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        processRender();
        //Check buildView does not destroy the state
        component = facesContext.getViewRoot().findComponent("mainForm:component1");
        Assert.assertNotNull(component);
        
        //Go to page2
        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        submit(button);
        
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
        processRender();
        
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        Assert.assertNotNull(component);

        button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:page2");
        submit(button);
        
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
        submit(button);

        processLifecycleExecute();
        component = facesContext.getViewRoot().findComponent("mainForm:component2");
        //Check it is restored
        Assert.assertNotNull(component);
    }
}
