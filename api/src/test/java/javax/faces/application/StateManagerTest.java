/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.faces.application;

import org.apache.shale.test.mock.MockStateManager;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;


import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

public class StateManagerTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(StateManagerTest.class);
    }

    public StateManagerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
      * Test method for 'javax.faces.application.StateManager.isSavingStateInClient(FacesContext)'
      */
    public void testNullThrowsIsSavingStateInClient() {
        MockStateManager subject = new MockStateManager();
        try {
            subject.isSavingStateInClient(null);
            fail("should have thrown an exception");
        } catch (RuntimeException e) {
        }
    }

    /*
      * Test method for 'javax.faces.application.StateManager.isSavingStateInClient(FacesContext)'
      */
    public void testIsSavingStateInClientTrue() {
        IMocksControl contextControl = EasyMock.createControl();
        IMocksControl externalControl = EasyMock.createControl();
        FacesContext context = contextControl.createMock(FacesContext.class);
        ExternalContext external = externalControl.createMock(ExternalContext.class);
        context.getExternalContext();
        contextControl.andReturn(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.andReturn("client");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        assertEquals(true, subject.isSavingStateInClient(context));
    }

    /*
      * Test method for 'javax.faces.application.StateManager.isSavingStateInClient(FacesContext)'
      */
    public void testIsSavingStateInClientFalse() {
        IMocksControl contextControl = EasyMock.createControl();
        IMocksControl externalControl = EasyMock.createControl();
        FacesContext context = contextControl.createMock(FacesContext.class);
        ExternalContext external = externalControl.createMock(ExternalContext.class);
        context.getExternalContext();
        contextControl.andReturn(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.andReturn("server");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        assertEquals(false, subject.isSavingStateInClient(context));
        // calling a second time asserts that the code is caching the value correctly
        assertEquals(false, subject.isSavingStateInClient(context));
    }

    /*
      * Test method for 'javax.faces.application.StateManager.isSavingStateInClient(FacesContext)'
      */
    public void testIsSavingStateInClientBogus() {
        IMocksControl contextControl = EasyMock.createControl();
        IMocksControl externalControl = EasyMock.createControl();
        FacesContext context = contextControl.createMock(FacesContext.class);
        ExternalContext external = externalControl.createMock(ExternalContext.class);
        context.getExternalContext();
        contextControl.andReturn(external);
        context.getExternalContext();
        contextControl.andReturn(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.andReturn("blorf");
        external.log("Illegal state saving method 'blorf', default server state saving will be used");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        assertEquals(false, subject.isSavingStateInClient(context));
    }

    /*
      * Test method for 'javax.faces.application.StateManager.isSavingStateInClient(FacesContext)'
      */
    public void testIsSavingStateInClientNull() {
        IMocksControl contextControl = EasyMock.createControl();
        IMocksControl externalControl = EasyMock.createControl();
        FacesContext context = contextControl.createMock(FacesContext.class);
        ExternalContext external = externalControl.createMock(ExternalContext.class);
        context.getExternalContext();
        contextControl.andReturn(external);
        context.getExternalContext();
        contextControl.andReturn(external);
        contextControl.replay();
        external.getInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME);
        externalControl.andReturn(null);
        external.log("No state saving method defined, assuming default server state saving");
        externalControl.replay();

        MockStateManager subject = new MockStateManager();
        assertEquals(false, subject.isSavingStateInClient(context));
    }

}
