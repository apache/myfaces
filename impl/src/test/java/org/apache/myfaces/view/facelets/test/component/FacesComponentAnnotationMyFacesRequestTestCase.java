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
package org.apache.myfaces.view.facelets.test.component;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import org.junit.jupiter.api.Assertions;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.junit.jupiter.api.Test;

public class FacesComponentAnnotationMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase

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
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.test.component");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_CLIENT);
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
    }
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }    
    
    @Test
    public void testUIPanel1() throws Exception
    {
        startViewRequest("/testMyUIPanel1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel1");
        Assertions.assertNotNull(comp);
        Assertions.assertTrue(comp instanceof MyUIPanel1);
        
        endRequest();
    }    

    @Test
    public void testUIPanel2() throws Exception
    {
        startViewRequest("/testMyUIPanel2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel1");
        Assertions.assertNotNull(comp);
        Assertions.assertTrue(comp instanceof MyUIPanel2);
        
        endRequest();
    }
    
    @Test
    public void testUIPanel3() throws Exception
    {
        startViewRequest("/testMyUIPanel3.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel3");
        Assertions.assertNotNull(comp);
        Assertions.assertTrue(comp instanceof MyUIPanel3);

        // Check component type
        MyUIPanel3 comp2 = (MyUIPanel3) 
            facesContext.getApplication().createComponent("myUIPanel3");
        Assertions.assertNotNull(comp2);
        
        endRequest();
    }
    
    // Test for MYFACES-4003
    @Test
    public void testInputTextWithClass() throws Exception
    {
        startViewRequest("/testClassAttribute.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent jsfInputText = facesContext.getViewRoot().findComponent("form:JSF_inputText");
        Assertions.assertNotNull(jsfInputText);
        Assertions.assertEquals("TEST", jsfInputText.getAttributes().get("styleClass"));
        
        // The "class" attribute should have been mapped to a "styleClass" attribute
        UIComponent testInputText = facesContext.getViewRoot().findComponent("form:test_inputText");
        Assertions.assertNotNull(testInputText);
        Assertions.assertEquals("TEST", testInputText.getAttributes().get("styleClass"));
        
        endRequest();
    }    
}
