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

package jakarta.faces.application;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.test.mock.MockStateManager;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateManagerTest
{
    /*
     * Test method for 'jakarta.faces.application.StateManager.isSavingStateInClient(FacesContext)'
     */
    @Test
    public void testNullThrowsIsSavingStateInClient()
    {
        MockStateManager subject = new MockStateManager();
        try
        {
            subject.isSavingStateInClient(null);
            Assertions.fail("should have thrown an exception");
        }
        catch (RuntimeException e)
        {
        }
    }

    /*
     * Test method for 'jakarta.faces.application.StateManager.isSavingStateInClient(FacesContext)'
     */
    @Test
    public void testIsSavingStateInClientTrue()
    {
        MockControl contextControl = MockClassControl.createControl(FacesContext.class);
        MockControl externalControl = MockClassControl.createControl(ExternalContext.class);
        FacesContext context = (FacesContext) contextControl.getMock();
        ExternalContext external = (ExternalContext) externalControl.getMock();
        context.getExternalContext();
        contextControl.setReturnValue(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.setReturnValue("client");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        Assertions.assertEquals(true, subject.isSavingStateInClient(context));
    }

    /*
     * Test method for 'jakarta.faces.application.StateManager.isSavingStateInClient(FacesContext)'
     */
    @Test
    public void testIsSavingStateInClientFalse()
    {
        MockControl contextControl = MockClassControl.createControl(FacesContext.class);
        MockControl externalControl = MockClassControl.createControl(ExternalContext.class);
        FacesContext context = (FacesContext) contextControl.getMock();
        ExternalContext external = (ExternalContext) externalControl.getMock();
        context.getExternalContext();
        contextControl.setReturnValue(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.setReturnValue("server");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        Assertions.assertEquals(false, subject.isSavingStateInClient(context));
        // calling a second time asserts that the code is caching the value correctly
        Assertions.assertEquals(false, subject.isSavingStateInClient(context));
    }

    /*
     * Test method for 'jakarta.faces.application.StateManager.isSavingStateInClient(FacesContext)'
     */
    @Test
    public void testIsSavingStateInClientBogus()
    {
        MockControl contextControl = MockClassControl.createControl(FacesContext.class);
        MockControl externalControl = MockClassControl.createControl(ExternalContext.class);
        FacesContext context = (FacesContext) contextControl.getMock();
        ExternalContext external = (ExternalContext) externalControl.getMock();
        context.getExternalContext();
        contextControl.setReturnValue(external);
        context.getExternalContext();
        contextControl.setReturnValue(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.setReturnValue("blorf");
        external.log("Illegal state saving method 'blorf', default server state saving will be used");
        externalControl.setVoidCallable();
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        Assertions.assertEquals(false, subject.isSavingStateInClient(context));
    }

    /*
     * Test method for 'jakarta.faces.application.StateManager.isSavingStateInClient(FacesContext)'
     */
    @Test
    public void testIsSavingStateInClientNull()
    {
        MockControl contextControl = MockClassControl.createControl(FacesContext.class);
        MockControl externalControl = MockClassControl.createControl(ExternalContext.class);
        FacesContext context = (FacesContext) contextControl.getMock();
        ExternalContext external = (ExternalContext) externalControl.getMock();
        context.getExternalContext();
        contextControl.setReturnValue(external);
        context.getExternalContext();
        contextControl.setReturnValue(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.setReturnValue(null);
        external.log("No state saving method defined, assuming default server state saving");
        externalControl.setVoidCallable();
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        Assertions.assertEquals(false, subject.isSavingStateInClient(context));
    }

}
