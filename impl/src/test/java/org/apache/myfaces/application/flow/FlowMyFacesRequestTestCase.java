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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.render.ResponseStateManager;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Leonardo Uribe
 */
class FlowMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.application.flow");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/flow1-flow.xml");
        servletContext.addInitParameter("jakarta.faces.CLIENT_WINDOW_MODE", "url");
    }

    @Test
    void flow11() throws Exception
    {
        startViewRequest("/flow1_1.xhtml");
        processLifecycleExecute();

        NavigationHandler handler = facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        assertNull(contentCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        
        contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        assertNotNull(contentCase);
    }

    @Test
    void flow12() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();

        NavigationHandler handler = facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        assertNull(contentCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "end");
        assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button4);
        
        processLifecycleExecute();
        
        // The interesting thing here is that it requires to get out from two consecutive flows, and it needs
        // to chain all commands. The difficulty here resides in the context should be resolved properly, and
        // there are a couple of recursive calls that needs to be solved.
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow);
        assertEquals("/flow1_end.xhtml", facesContext.getViewRoot().getViewId());
    }

    @Test
    void flow13() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        assertNull(contentCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        client.submit(button4);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        assertNotNull(endCase);
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow);
        assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
        
    }

    @Test
    void flow14() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        assertNotNull(endCase);
        String toViewId = endCase.getToViewId(facesContext);
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        //Assertions.assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        assertNotNull(endCase);

        toViewId = endCase.getToViewId(facesContext);
        fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow);
        assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
    }

    @Test
    void flow15() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow3");
        
        assertNotNull(navCase); 
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        startViewRequest(navCase.getToViewId(facesContext));
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, navCase.getToFlowDocumentId());
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, navCase.getFromOutcome());
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
       
        //Enter flow 1
        //UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        //submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        //UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        //submit(button2);
        navCase = handler.getNavigationCase(facesContext, null, "call_flow2");
        
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        startViewRequest(navCase.getToViewId(facesContext));
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, navCase.getToFlowDocumentId());
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, navCase.getFromOutcome());
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        assertNotNull(endCase);
        String toViewId = endCase.getToViewId(facesContext);
        String fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId (since it was GET, it should be the start viewId)
        assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "switchBack");
        assertNotNull(endCase);

        toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        assertEquals("/flow3/content.xhtml", toViewId);
        fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        
        //Assertions.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
    }    
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 1
     * - Start flow 2
     * - End flow 1
     * - End flow 2
     * 
     * Since flow2 was called using the flow name, end flow 1 doesn't end flow 2
     * 
     * @throws Exception 
     */
    @Test
    void flow16() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow2", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 1
     * - Start flow 2 (using call node)
     * - End flow 1
     * 
     * Since flow2 was called using a call node, end flow 1 also end flow 2
     * 
     * @throws Exception 
     */
    @Test
    void flow17() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        assertNotNull(navCase);
        
        // Check begin view node
        assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow3);
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 3 (start flow 1)
     * - Start flow 2
     * - End flow 1
     * - End flow 2
     * 
     * Since flow2 was called using the flow name, end flow 1 doesn't end flow 2
     * At the end flow 3 should still be active
     * 
     * @throws Exception 
     */
    @Test
    void flow18() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow3", currentFlow3.getId());
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 2 
     * - Start flow 3 (start flow 1)
     * - Return flow 1 and 3
     * - End flow 2
     * 
     * @throws Exception 
     */    
    @Test
    void flow19() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow1", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow2", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 2 
     * - Start flow 3 (start flow 1)
     * - Return flow 1 and 3 (GET)
     * - End flow 2
     * 
     * @throws Exception 
     */   
    @Test
    void flow191() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow2", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow1", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        //submit(button5);
        
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_3");
        assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        assertEquals("/flow_base.xhtml", toViewId);
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow2", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }


    /**
     * This tests do the following:
     * 
     * - Start flow 3 (start flow 1)
     * - Start flow 2 
     * - End flow 2
     * - Return flow 1 and 3
     * 
     * @throws Exception 
     */   
    @Test
    void flow110() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow1", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 3 (start flow 1)
     * - Start flow 2 
     * - Return flow 1 and 3
     * - End flow 2
     * 
     * @throws Exception 
     */ 
    @Test
    void flow1101() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button5);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow2", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 3 (start flow 1)
     * - Start flow 2 
     * - Return flow 1 and 3 (GET)
     * - End flow 2
     * 
     * @throws Exception 
     */ 
    @Test
    void flow1102() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow1", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        //submit(button5);
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_3");
        assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        assertEquals("/flow_base.xhtml", toViewId);
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow2", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow4);
        
        renderResponse();
    }

    /**
     * This tests do the following:
     * 
     * - Start flow 4
     * - Start flow 2
     * - Start flow 1 (use call node from flow 4)
     * - Return flow 1 and 4
     * - Return flow 2
     * 
     * @throws Exception 
     */ 
    @Test
    void flow111() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow4");
        client.submit(button);
        
        processLifecycleExecute();
        
        assertEquals("/flow4/flow4.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow4", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1_4");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow1", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button6);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_4");
        client.submit(button7);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow4);
        assertEquals("flow2", currentFlow4.getId());
        
        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button8);
        
        processLifecycleExecute();

        Flow currentFlow5 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow5);
        
        renderResponse();

    }

    /**
     * This tests do the following:
     * 
     * - Start flow 4
     * - Start flow 2
     * - Start flow 1 (use call node from flow 4)
     * - Return flow 1 and 4 (GET)
     * - Return flow 2
     * 
     * @throws Exception 
     */
    @Test
    void flow1111() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow4");
        client.submit(button);
        
        processLifecycleExecute();
        
        assertEquals("/flow4/flow4.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flow4", currentFlow.getId());
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow2);
        assertEquals("flow2", currentFlow2.getId());
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1_4");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow3);
        assertEquals("flow1", currentFlow3.getId());
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button6);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_4");
        //submit(button7);
        
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_4");
        assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        assertEquals("/flow_base.xhtml", toViewId);
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow4);
        assertEquals("flow2", currentFlow4.getId());
        
        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button8);
        
        processLifecycleExecute();

        Flow currentFlow5 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNull(currentFlow5);
        
        renderResponse();

    }

    /**
     * This tests do the following:
     * 
     * - Start Flow A
     * - Call Flow B
     * - Call Flow A
     * - Call Flow B
     * - Return from Flow B
     * - Return from Flow A
     * - Return from Flow B
     */
    @Test
    void flowA1() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowA");
        client.submit(button);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_1");
        assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_1");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_A");
        client.submit(button3);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowB"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button4);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertEquals("valueA_2", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button6);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        assertEquals("valueB_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowB"));

        renderResponse();
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button7);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertEquals("valueA_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
    }

    @Test
    void flowA2() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        NavigationHandler handler =facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow A
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowA");
        client.submit(button);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_1");
        assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        
        renderResponse();
        
        //Go to base
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        // We are still on flowA, just returned to base
        assertEquals("flowA", currentFlow.getId());
        
        renderResponse();
        
        //Enter flow B
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowB");
        client.submit(button3);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_1");
        assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        
        renderResponse();
        
        // Call flow A through flow call
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_A");
        client.submit(button4);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowB"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());

        // Call flow B through flow call
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button6);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertEquals("valueA_2", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button7);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowB", currentFlow.getId());
        assertEquals("valueB_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowB"));

        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button8);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        assertNotNull(currentFlow);
        assertEquals("flowA", currentFlow.getId());
        assertEquals("valueA_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
    }
    
}
