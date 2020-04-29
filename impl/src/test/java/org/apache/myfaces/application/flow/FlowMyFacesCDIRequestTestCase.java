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

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;
import jakarta.faces.flow.Flow;
import org.apache.myfaces.mc.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test is the same as FlowMyFacesRequestTestCase with the diference that
 * in this case CDI is enabled and the other alternative is used.
 */
public class FlowMyFacesCDIRequestTestCase extends AbstractMyFacesCDIRequestTestCase
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
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/flow1-flow.xml");
        servletContext.addInitParameter("jakarta.faces.CLIENT_WINDOW_MODE", "url");
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
        
        // Check Flow1Bean can be created
        Flow1Bean bean1 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow1Bean}", Flow1Bean.class);
        Assert.assertNotNull(bean1);
        Assert.assertEquals(bean1.getPostConstructCalled(), "true");
        
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
        
        // Check the bean with @FlowScoped annotation can be instantiated
        Flow1Bean bean1 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow1Bean}", Flow1Bean.class);
        Assert.assertNotNull(bean1);
        Assert.assertEquals(bean1.getPostConstructCalled(), "true");        
        bean1.setName("John");
        
        renderResponse();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        client.submit(button2);
        
        processLifecycleExecute();
        
        currentFlow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        Assert.assertNotNull(currentFlow);
        Assert.assertEquals("flow2", currentFlow.getId());
        Assert.assertFalse(facesContext.getApplication().getFlowHandler().getCurrentFlowScope().containsKey("flow1"));
        facesContext.getApplication().getFlowHandler().getCurrentFlowScope().put("flow2","value2");
        
        Flow2Bean bean2 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow2Bean}", Flow2Bean.class);
        Assert.assertNotNull(bean2);
        Assert.assertEquals(bean2.getPostConstructCalled(), "true");
        
        Flow1Bean bean1_1 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow1Bean}", Flow1Bean.class);
        Assert.assertEquals(bean1_1.getName(), "John");
        
        Flow21Bean bean2_1 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow21Bean}", Flow21Bean.class);
        Assert.assertNotNull(bean2_1);
        Assert.assertEquals(bean2_1.getPostConstructCalled(), "true");
        Assert.assertNotNull(bean2_1.getFlow1Bean());

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
        
        try
        {
            Flow1Bean bean1_2 = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{flow1Bean}", Flow1Bean.class);
            bean1_2.getName();
            Assert.fail("Invocation show throw NullPointerException or ContextNotActiveException");
        }
        catch (ContextNotActiveException e)
        {
        }
        catch (NullPointerException e)
        {
        }
    }

    /**
     * Check outbound parameter is initialized before call initializer method
     * 
     * @throws Exception 
     */
    @Test
    public void testFlow1_12() throws Exception
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
        
        NavigationCase goFlowBase = handler.getNavigationCase(facesContext, null, "call_flow5_4");
        Assert.assertNotNull(goFlowBase);
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow5");
        client.submit(button2);
        
        processLifecycleExecute();
        
        Assert.assertEquals("/flow5/flow5.xhtml", facesContext.getViewRoot().getViewId());

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
