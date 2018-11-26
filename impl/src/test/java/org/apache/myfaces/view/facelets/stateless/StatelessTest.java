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
package org.apache.myfaces.view.facelets.stateless;

import javax.faces.application.StateManager;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import org.apache.myfaces.mc.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class StatelessTest extends AbstractMyFacesRequestTestCase
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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.stateless");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("javax.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("org.apache.myfaces.STRICT_JSF_2_REFRESH_TARGET_AJAX", "true");
    }
    
    @Test
    public void postWithoutPrependFormId() throws Exception
    {
        startViewRequest("/stateless.xhtml");
        processLifecycleExecuteAndRender();
        
        Assert.assertTrue(facesContext.getViewRoot().isTransient());
        
        UIComponent form = facesContext.getViewRoot().findComponent("form1");
        UIComponent formButton = facesContext.getViewRoot().findComponent("form1:smt");
        
        client.submit(formButton);
        
        processLifecycleExecuteAndRender();
        String text = getRenderedContent(facesContext);

        endRequest();
    }
    
    @Test
    public void postAjaxWithoutPrependFormId() throws Exception
    {
        startViewRequest("/stateless.xhtml");
        processLifecycleExecuteAndRender();
        
        Assert.assertTrue(facesContext.getViewRoot().isTransient());
        
        UIComponent form = facesContext.getViewRoot().findComponent("form1");
        UIComponent formButton = facesContext.getViewRoot().findComponent("form1:smtAjax");
        
        client.ajax(formButton, "action", formButton.getClientId(facesContext), form.getClientId(facesContext), true);
        
        processLifecycleExecuteAndRender();
        String text = getRenderedContent(facesContext);

        endRequest();
    }
    
    @Test
    public void postWithPrependFormId() throws Exception
    {
        startViewRequest("/stateless.xhtml");
        processLifecycleExecuteAndRender();
        
        Assert.assertTrue(facesContext.getViewRoot().isTransient());
        
        UIComponent form = facesContext.getViewRoot().findComponent("form2");
        UIComponent formButton = facesContext.getViewRoot().findComponent("form2:smt");
        
        client.submit(formButton);
        
        processLifecycleExecuteAndRender();
        String text = getRenderedContent(facesContext);

        endRequest();
    }

    @Test
    public void postAjaxWithPrependFormId() throws Exception
    {
        startViewRequest("/stateless.xhtml");
        processLifecycleExecuteAndRender();
        
        Assert.assertTrue(facesContext.getViewRoot().isTransient());
        
        UIComponent form = facesContext.getViewRoot().findComponent("form2");
        UIComponent formButton = facesContext.getViewRoot().findComponent("form2:smtAjax");
        
        client.ajax(formButton, "action", formButton.getClientId(facesContext), form.getClientId(facesContext), true);
        
        processLifecycleExecuteAndRender();
        String text = getRenderedContent(facesContext);

        endRequest();
    }
    
    
}
