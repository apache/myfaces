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

package org.apache.myfaces.lifecycle;

import javax.faces.FactoryFinder;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

public class LifecycleImplCactus extends ServletTestCase
{
    private FacesServlet servlet;

    private FacesContext facesContext;

    private Lifecycle lifecycle;

    protected void setUp() throws Exception
    {
        super.setUp();
        servlet = new FacesServlet();
        servlet.init(this.config);
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                                                                            .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        String lifecycleId = this.config.getServletContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        lifecycleId = (lifecycleId != null ? lifecycleId : LifecycleFactory.DEFAULT_LIFECYCLE);
        lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
        for (int i = 0; i < lifecycle.getPhaseListeners().length; i++)
        {
            if (lifecycle.getPhaseListeners()[i] instanceof InstrumentingPhaseListener)
            {
                lifecycle.removePhaseListener(lifecycle.getPhaseListeners()[i]);
            }
        }
        FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder
                                                                                     .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        facesContext = facesContextFactory.getFacesContext(this.config.getServletContext(), request, response,
            lifecycle);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /*
     * Test method for 'javax.faces.lifecycle.Lifecycle.addPhaseListener(PhaseListener)'
     */
    public void testAddPhaseListenerPhaseListener()
    {
    }

    public void beginExecuteRestoreViewRenderBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteRestoreViewRenderBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.RESTORE_VIEW);
        listener.setBefore(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(2, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(1));
        assertEquals(2, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(1));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteRestoreViewRenderAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteRestoreViewRenderAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.RESTORE_VIEW);
        listener.setAfter(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(2, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(1));
        assertEquals(2, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(1));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteRestoreViewCompleteBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteRestoreViewCompleteBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.RESTORE_VIEW);
        listener.setBefore(true);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(1, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(0, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteRestoreViewCompleteAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteRestoreViewCompleteAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.RESTORE_VIEW);
        listener.setAfter(true);
        listener.setBefore(false);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(1, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(1, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteApplyRequestValuesRenderBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteApplyRequestValuesRenderBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        listener.setBefore(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(3, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(2));
        assertEquals(3, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(2));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteApplyRequestValuesRenderAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteApplyRequestValuesRenderAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        listener.setAfter(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(3, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(2));
        assertEquals(3, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(2));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteApplyRequestValuesCompleteBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteApplyRequestValuesCompleteBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        listener.setBefore(true);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(2, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(1, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteApplyRequestValuesCompleteAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteApplyRequestValuesCompleteAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        listener.setAfter(true);
        listener.setBefore(false);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(2, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(2, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteProcessValidationsRenderBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteProcessValidationsRenderBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.PROCESS_VALIDATIONS);
        listener.setBefore(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(4, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(3));
        assertEquals(4, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(3));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteProcessValidationsRenderAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteProcessValidationsRenderAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.PROCESS_VALIDATIONS);
        listener.setAfter(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(4, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(3));
        assertEquals(4, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(3));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteProcessValidationsCompleteBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteProcessValidationsCompleteBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.PROCESS_VALIDATIONS);
        listener.setBefore(true);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(3, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(2, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteProcessValidationsCompleteAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteProcessValidationsCompleteAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we would go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.PROCESS_VALIDATIONS);
        listener.setAfter(true);
        listener.setBefore(false);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        assertEquals(3, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(3, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteUpdateModelValuesRenderBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteUpdateModelValuesRenderBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        listener.setBefore(true);
        listener.setAfter(false);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(5, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getBeforePhases().get(3));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(4));
        assertEquals(5, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getAfterPhases().get(3));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(4));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteUpdateModelValuesRenderAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteUpdateModelValuesRenderAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        listener.setBefore(false);
        listener.setAfter(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(5, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getBeforePhases().get(3));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getBeforePhases().get(4));
        assertEquals(5, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getAfterPhases().get(3));
        assertEquals(PhaseId.RENDER_RESPONSE, listener.getAfterPhases().get(4));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteUpdateModelValuesCompleteBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteUpdateModelValuesCompleteBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        listener.setBefore(true);
        listener.setAfter(false);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(4, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getBeforePhases().get(3));
        assertEquals(3, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteUpdateModelValuesCompleteAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteUpdateModelValuesCompleteAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        listener.setBefore(false);
        listener.setAfter(true);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(4, listener.getBeforePhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getBeforePhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getBeforePhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getBeforePhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getBeforePhases().get(3));
        assertEquals(4, listener.getAfterPhases().size());
        assertEquals(PhaseId.RESTORE_VIEW, listener.getAfterPhases().get(0));
        assertEquals(PhaseId.APPLY_REQUEST_VALUES, listener.getAfterPhases().get(1));
        assertEquals(PhaseId.PROCESS_VALIDATIONS, listener.getAfterPhases().get(2));
        assertEquals(PhaseId.UPDATE_MODEL_VALUES, listener.getAfterPhases().get(3));
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteInvokeApplicationRenderBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteInvokeApplicationRenderBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.INVOKE_APPLICATION);
        listener.setBefore(true);
        listener.setAfter(false);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(6, listener.getBeforePhases().size());
        assertEquals(6, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteInvokeApplicationRenderAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteInvokeApplicationRenderAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.INVOKE_APPLICATION);
        listener.setBefore(false);
        listener.setAfter(true);
        listener.setRender(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(6, listener.getBeforePhases().size());
        assertEquals(6, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteInvokeApplicationCompleteBefore(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteInvokeApplicationCompleteBefore() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.INVOKE_APPLICATION);
        listener.setBefore(true);
        listener.setAfter(false);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(5, listener.getBeforePhases().size());
        assertEquals(4, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecuteInvokeApplicationCompleteAfter(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecuteInvokeApplicationCompleteAfter() throws Exception
    {
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        listener.setEventPhaseId(PhaseId.INVOKE_APPLICATION);
        listener.setBefore(false);
        listener.setAfter(true);
        listener.setComplete(true);
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(5, listener.getBeforePhases().size());
        assertEquals(5, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    public void beginExecute(WebRequest request)
    {
        request.addParameter("foo", "bar");
        request.setURL("localhost:8080", "/test-app", "/faces", "/index.jsp", null);
    }

    public void testExecute() throws Exception
    {
        // set the view root
        setViewRoot("/index.jsp");
        // simulate that this is the 2nd request for this page
        // so that we will go through all 6 phases
        StateManager stateManager = facesContext.getApplication().getStateManager();
        stateManager.saveSerializedView(facesContext);
        InstrumentingPhaseListener listener = new InstrumentingPhaseListener();
        lifecycle.addPhaseListener(listener);
        servlet.service(this.request, this.response);
        // assert the phases were hit
        assertEquals(6, listener.getBeforePhases().size());
        assertEquals(6, listener.getAfterPhases().size());
        lifecycle.removePhaseListener(listener);
    }

    /*
     * Test method for 'javax.faces.lifecycle.Lifecycle.getPhaseListeners()'
     */
    public void testGetPhaseListeners()
    {

    }

    /*
     * Test method for 'javax.faces.lifecycle.Lifecycle.removePhaseListener(PhaseListener)'
     */
    public void testRemovePhaseListenerPhaseListener()
    {

    }

    /*
     * Test method for 'javax.faces.lifecycle.Lifecycle.render(FacesContext)'
     */
    public void testRenderFacesContext()
    {

    }

    private void setViewRoot(String viewId)
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(facesContext, viewId);
        viewRoot.setViewId(viewId);
        facesContext.setViewRoot(viewRoot);
    }

}
