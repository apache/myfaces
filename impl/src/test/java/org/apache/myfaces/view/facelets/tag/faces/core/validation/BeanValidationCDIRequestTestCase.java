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
package org.apache.myfaces.view.facelets.tag.faces.core.validation;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIInput;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.Validator;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This test is the same as FlowMyFacesRequestTestCase with the diference that
 * in this case CDI is enabled and the other alternative is used.
 */
public class BeanValidationCDIRequestTestCase extends AbstractMyFacesCDIRequestTestCase
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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                "org.apache.myfaces.view.facelets.tag.faces.core.validation");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
        servletContext.addInitParameter("jakarta.faces.CLIENT_WINDOW_MODE", "url");
        servletContext.addInitParameter("org.apache.myfaces.validator.BEAN_BEFORE_JSF_VALIDATION", "true");
    }
    
    @Test
    public void testBeanValidation_1() throws Exception
    {
        startViewRequest("/testBeanValidation_1.xhtml");
        
        processLifecycleExecute();
        processLifecycleRender();
        client.inputText("mainForm:username", "someusr");
        
        client.submit("mainForm:submit");
        
        processLifecycleExecute();
        
        UIInput username = (UIInput) facesContext.getViewRoot().findComponent("mainForm:username");
        Assertions.assertNotNull(username);
        Validator[] array = username.getValidators();
        Assertions.assertTrue(array[0] instanceof BeanValidator);
    }
    
    @Test
    public void testBeanValidation_2() throws Exception
    {
        startViewRequest("/testBeanValidation_2.xhtml");
        
        processLifecycleExecute();
        processLifecycleRender();
        client.inputText("mainForm:username", "someusr");
        
        client.submit("mainForm:submit");
        
        processLifecycleExecute();
        
        UIInput username = (UIInput) facesContext.getViewRoot().findComponent("mainForm:username");
        Assertions.assertNotNull(username);
        Validator[] array = username.getValidators();
        Assertions.assertTrue(array[0] instanceof BeanValidator);
    }
}
