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
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.StateManager;
import javax.faces.component.UICommand;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;
import javax.faces.render.ResponseStateManager;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.mc.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Test;
import org.testng.Assert;

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
        
        // Check the bean with @FlowScoped annotation can be instantiated
        Flow1Bean bean1 = facesContext.getApplication().evaluateExpressionGet(
            facesContext, "#{flow1Bean}", Flow1Bean.class);
        Assert.assertNotNull(bean1);
        Assert.assertEquals(bean1.getPostConstructCalled(), "true");        
        bean1.setName("John");
        
        processRender();
        
        UICommand button2 = (UICommand) facesContext.getViewRoot().findComponent("mainForm:call_flow2");
        submit(button2);
        
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

}
