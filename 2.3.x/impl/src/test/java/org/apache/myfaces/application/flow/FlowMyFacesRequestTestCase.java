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
import org.junit.Assert;
import org.junit.Test;

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
        startViewRequest("/flow1_1.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        
        contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNotNull(contentCase);
    }
    
    @Test
    public void testFlow1_2() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "end");
        Assert.assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button4);
        
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
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        NavigationCase contentCase = handler.getNavigationCase(facesContext, null, "flow1_content");
        
        Assert.assertNull(contentCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        client.submit(button4);
        
        processLifecycleExecute();
        
        // Check it should go back to flow1 
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        Assert.assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:back_flow");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow);
        Assert.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
        
    }

    @Test
    public void testFlow1_4() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow1","value1");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
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
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId
        //Assert.assertEquals("/flow1/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);

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
        Assert.assertNull(currentFlow);
        Assert.assertEquals("/flow1_2.xhtml", facesContext.getViewRoot().getViewId());
    }     
    
    @Test
    public void testFlow1_5() throws Exception
    {
        startViewRequest("/flow1_2.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow3");
        
        Assert.assertNotNull(navCase); 
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
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
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        
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
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:content");
        client.submit(button3);
        processLifecycleExecute();
        renderResponse();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());

        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back");
        Assert.assertNotNull(endCase);
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
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow1", currentFlow.getId());
        Assert.assertTrue(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        // Check lastDisplayedViewId (since it was GET, it should be the start viewId)
        Assert.assertEquals("/flow2/begin.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        endCase = handler.getNavigationCase(facesContext, null, "switchBack");
        Assert.assertNotNull(endCase);

        toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        Assert.assertEquals(toViewId, "/flow3/content.xhtml");
        fromOutcome = endCase.getFromOutcome();
        clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
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
    public void testFlow1_6() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow2");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_7() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        NavigationCase navCase = handler.getNavigationCase(facesContext, null, "flow1");
        
        Assert.assertNotNull(navCase);
        
        // Check begin view node
        Assert.assertEquals("/flow1/begin.xhtml", navCase.getToViewId(facesContext));
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow3);
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
    public void testFlow1_8() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow3");
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
    public void testFlow1_9() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow2");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow1");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow2");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_9_1() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow2");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow1");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        //submit(button5);
        
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_3");
        Assert.assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        Assert.assertEquals(toViewId, "/flow_base.xhtml");
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow2");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_10() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow1");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_10_1() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow2");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_10_2() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow3");
        client.submit(button);
        
        processLifecycleExecute();
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow1");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_3");
        //submit(button5);
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_3");
        Assert.assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        Assert.assertEquals(toViewId, "/flow_base.xhtml");
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow2");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button6);
        
        processLifecycleExecute();

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow4);
        
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
    public void testFlow1_11() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow4");
        client.submit(button);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow4/flow4.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow4");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1_4");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow1");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button6);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_4");
        client.submit(button7);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow4);
        Assert.assertEquals(currentFlow4.getId(), "flow2");
        
        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button8);
        
        processLifecycleExecute();

        Flow currentFlow5 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow5);
        
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
    public void testFlow1_11_1() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow4");
        client.submit(button);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow4/flow4.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flow4");
        
        renderResponse();
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "flow_base");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        renderResponse();
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow2");
        client.submit(button3);
        
        processLifecycleExecute();
        
        Flow currentFlow2 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow2);
        Assert.assertEquals(currentFlow2.getId(), "flow2");
        
        renderResponse();
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button4);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlow1_4");
        client.submit(button5);
        
        processLifecycleExecute();
        
        Flow currentFlow3 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow3);
        Assert.assertEquals(currentFlow3.getId(), "flow1");
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button6);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());
        
        renderResponse();
        
        //UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow1_4");
        //submit(button7);
        
        NavigationCase endCase = handler.getNavigationCase(facesContext, null, "back_flow_1_4");
        Assert.assertNotNull(endCase);

        String toViewId = endCase.getToViewId(facesContext);
        // Check if the dynamic outcome return hack has been correctly resolved. 
        Assert.assertEquals(toViewId, "/flow_base.xhtml");
        String fromOutcome = endCase.getFromOutcome();
        String clientWindowId = facesContext.getExternalContext().getClientWindow().getId();
        
        endRequest();
        startViewRequest(toViewId);
        request.addParameter(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, FlowHandler.NULL_FLOW);
        request.addParameter(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, fromOutcome);
        request.addParameter(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, clientWindowId);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow_base.xhtml", facesContext.getViewRoot().getViewId());

        Flow currentFlow4 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow4);
        Assert.assertEquals(currentFlow4.getId(), "flow2");
        
        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:returnFlow2");
        client.submit(button8);
        
        processLifecycleExecute();

        Flow currentFlow5 = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNull(currentFlow5);
        
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
    public void testFlowA_1() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow 1
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowA");
        client.submit(button);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_1");
        Assert.assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flowA");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowB", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_1");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_A");
        client.submit(button3);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowB"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button4);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowB", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertEquals("valueA_2", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button6);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowB", currentFlow.getId());
        Assert.assertEquals("valueB_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowB"));

        renderResponse();
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button7);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertEquals("valueA_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
    }
    
    @Test
    public void testFlowA_2() throws Exception
    {
        startViewRequest("/flow_base.xhtml");
        processLifecycleExecute();
        
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        
        renderResponse();
       
        //Enter flow A
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowA");
        client.submit(button);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_1");
        Assert.assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());
        
        Flow currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flowA");
        
        renderResponse();
        
        //Go to base
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:go_flow_base");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        // We are still on flowA, just returned to base
        Assert.assertEquals("flowA", currentFlow.getId());
        
        renderResponse();
        
        //Enter flow B
        UICommand button3 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:startFlowB");
        client.submit(button3);
        
        processLifecycleExecute();
        
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_1");
        Assert.assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals(currentFlow.getId(), "flowB");
        
        renderResponse();
        
        // Call flow A through flow call
        UICommand button4 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_A");
        client.submit(button4);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowB"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowA","valueA_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flowA/flowA.xhtml", facesContext.getViewRoot().getViewId());

        // Call flow B through flow call
        UICommand button5 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow_B");
        client.submit(button5);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowB", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flowA"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flowB","valueB_2");
        
        renderResponse();
        
        //Check current view is the begin of flow2
        Assert.assertEquals("/flowB/flowB.xhtml", facesContext.getViewRoot().getViewId());
        
        UICommand button6 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button6);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertEquals("valueA_2", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
        UICommand button7 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button7);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowB", currentFlow.getId());
        Assert.assertEquals("valueB_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowB"));

        renderResponse();
        
        UICommand button8 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:end_flow");
        client.submit(button8);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flowA", currentFlow.getId());
        Assert.assertEquals("valueA_1", facesContext.getApplication().getFlowHandler().getCurrentFlowScope().get("flowA"));
        
        renderResponse();
        
    }
    
}
