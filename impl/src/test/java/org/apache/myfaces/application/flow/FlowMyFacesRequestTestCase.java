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
package org.apache.myfaces.application.flow;

import javax.el.ExpressionFactory;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.StateManager;
import javax.faces.component.UICommand;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;
import javax.faces.render.ResponseStateManager;
import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Test;
import org.testng.Assert;

/**
 *
 * @author Leonardo Uribe
 */
public class FlowMyFacesRequestTestCase extends AbstractMyFacesRequestTestCase
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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.flow");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("javax.faces.CONFIG_FILES", "/WEB-INF/flow1-flow.xml");
        servletContext.addInitParameter("javax.faces.CLIENT_WINDOW_MODE", "url");
    }
    
    @Test
    public void testFlow1_1() throws Exception
    {
        setupRequest("/flow1_1.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        processRender();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        
        contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNotNull(contentCase);
    }
    
    @Test
    public void testFlow1_2() throws Exception
    {
        setupRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        processRender();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        processRender();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        processRender();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        submit(button3);
        processLifecycleExecute();
        processRender();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "end");
        Assert.assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        submit(button4);
        
        processLifecycleExecute();
        
        // The interesting thing here is that it requires to get out from two consecutive flows, and it needs
        // to chain all commands. The difficulty here resides in the context should be resolved properly, and
        // there are a couple of recursive calls that needs to be solved.
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow);
        Assert.assertEquals("/flow1_end.xhtml", facesContext.getViewRoot().getViewId());
    }
    
    @Test
    public void testFlow1_3() throws Exception
    {
        setupRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        processRender();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        processRender();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        processRender();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        submit(button3);
        processLifecycleExecute();
        processRender();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        submit(button4);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        Assert.assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        processRender();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow);
        Assert.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
        
    }

    @Test
    public void testFlow1_4() throws Exception
    {
        setupRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        processRender();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        processRender();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        processRender();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        submit(button3);
        processLifecycleExecute();
        processRender();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        String toViewId = endCase.getToViewId(facesContext);
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        tearDownRequest();
        setupRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        //Assert.assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        processRender();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);

        toViewId = endCase.getToViewId(facesContext);
        fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        tearDownRequest();
        setupRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow);
        Assert.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
    }     
    
    @Test
    public void testFlow1_5() throws Exception
    {
        setupRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow3");
        
        Assert.assertNotNull(navCase); 
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        processRender();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        setupRequest(navCase.getToViewId(facesContext));
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, navCase.getToFlowDocumentId());
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, navCase.getFromOutcome());
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
       
        //Enter flow 1
        //UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        //submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        processRender();
        
        //UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        //submit(button2);
        navCase = handler.getNavigationCase(facesContext, null, "call_flow2");
        
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        setupRequest(navCase.getToViewId(facesContext));
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, navCase.getToFlowDocumentId());
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, navCase.getFromOutcome());
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        processRender();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        submit(button3);
        processLifecycleExecute();
        processRender();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        String toViewId = endCase.getToViewId(facesContext);
        String fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        tearDownRequest();
        setupRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId (since it was GET, it should be the start viewId)
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        processRender();
        
        endCase = handler.getNavigationCase(facesContext, null, "switchBack");
        Assert.assertNotNull(endCase);

        toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        Assert.assertEquals(toViewId, "/flow3/content.xhtml");
        fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        tearDownRequest();
        setupRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        
        //Assert.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
    }    
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
}
