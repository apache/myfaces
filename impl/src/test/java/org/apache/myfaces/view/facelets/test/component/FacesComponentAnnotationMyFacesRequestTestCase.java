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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.junit.jupiter.api.Test;

class FacesComponentAnnotationMyFacesRequestTestCase extends AbstractMyFacesCDIRequestTestCase
{
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES","org.apache.myfaces.view.facelets.test.component");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter(MyfacesConfig.REFRESH_TRANSIENT_BUILD_ON_PSS, "auto");
    }
    
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Test
    void uIPanel1() throws Exception
    {
        startViewRequest("/testMyUIPanel1.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel1");
        assertNotNull(comp);
        assertInstanceOf(MyUIPanel1.class, comp);
        
        endRequest();
    }

    @Test
    void uIPanel2() throws Exception
    {
        startViewRequest("/testMyUIPanel2.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel1");
        assertNotNull(comp);
        assertInstanceOf(MyUIPanel2.class, comp);
        
        endRequest();
    }

    @Test
    void uIPanel3() throws Exception
    {
        startViewRequest("/testMyUIPanel3.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent comp = facesContext.getViewRoot().findComponent("panel3");
        assertNotNull(comp);
        assertInstanceOf(MyUIPanel3.class, comp);

        // Check component type
        MyUIPanel3 comp2 = (MyUIPanel3) 
            facesContext.getApplication().createComponent("myUIPanel3");
        assertNotNull(comp2);
        
        endRequest();
    }

    // Test for MYFACES-4003
    @Test
    void inputTextWithClass() throws Exception
    {
        startViewRequest("/testClassAttribute.xhtml");
        processLifecycleExecuteAndRender();
        
        UIComponent jsfInputText = facesContext.getViewRoot().findComponent("form:JSF_inputText");
        assertNotNull(jsfInputText);
        assertEquals("TEST", jsfInputText.getAttributes().get("styleClass"));
        
        // The "class" attribute should have been mapped to a "styleClass" attribute
        UIComponent testInputText = facesContext.getViewRoot().findComponent("form:test_inputText");
        assertNotNull(testInputText);
        assertEquals("TEST", testInputText.getAttributes().get("styleClass"));
        
        endRequest();
    }    
}
