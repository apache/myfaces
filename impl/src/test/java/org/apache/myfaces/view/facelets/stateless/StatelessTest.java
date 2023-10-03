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

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.render.ResponseStateManager;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * See https://issues.apache.org/jira/browse/MYFACES-4267
 */
public class StatelessTest extends AbstractMyFacesCDIRequestTestCase

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
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
    }
    
    /**
     * Verify that a view with a template that has transient set can be restored
     * 
     * @throws Exception
     */
    @Test
    public void restoreStatelessTemplateView() throws Exception
    {
        startViewRequest("/stateless.xhtml");
        processLifecycleExecuteAndRender();

        Assertions.assertTrue(facesContext.getViewRoot().isTransient());

        // set the view state param so this context is treated as a postback
        client.getParameters().put(ResponseStateManager.VIEW_STATE_PARAM, "stateless");
        UIComponent formButton = facesContext.getViewRoot().findComponent("smt");
        client.submit(formButton);

        try {
            // this will cause an exception without the fix in MYFACES-4267
            restoreView();
        } catch (Exception e) {
            Assertions.fail("caught an exception trying to restore a stateless view: " + e.getMessage());
            endRequest();
            return;
        }

        Assertions.assertNotNull(facesContext.getViewRoot());

        // render the response and make sure the view contains the expected text
        renderResponse();
        String text = getRenderedContent(facesContext);

        Assertions.assertTrue(text.contains("success"));

        endRequest();
    }
}
