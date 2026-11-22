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
package org.apache.myfaces.event;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.core.AbstractMyFacesCDIRequestTestCase;
import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.jupiter.api.Test;

class PostAddToViewEventTest extends AbstractMyFacesCDIRequestTestCase
{
    
    
    @Override
    protected void setUpWebConfigParams() throws Exception
    {
        super.setUpWebConfigParams();
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.CLIENT.name());
        servletContext.addInitParameter("org.apache.myfaces.REFRESH_TRANSIENT_BUILD_ON_PSS", "true");
    }

    /**
     * Test that UIViewRoot receive a PostAddToViewEvent after the view is
     * populated.
     * 
     * @throws Exception
     */
    @Test
    void postAddToViewOnViewRoot() throws Exception
    {
        startViewRequest("/postAddToViewEvent_1.xhtml");

        PostAddToViewEventBean bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            ComponentSystemEvent e = (ComponentSystemEvent) EasyMock.getCurrentArguments()[0];
            assertInstanceOf(UIViewRoot.class, e.getComponent());
            assertEquals(PhaseId.RENDER_RESPONSE, facesContext.getCurrentPhaseId());
            return null;
        }).once();
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecuteAndRender();
        EasyMock.verify(bean);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(button);
        
        bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        // With PSS PostAddToViewEvent is called again on restore view phase
        // but when building initial state.
        EasyMock.expectLastCall().andAnswer(() -> {
            ComponentSystemEvent e = (ComponentSystemEvent) EasyMock.getCurrentArguments()[0];
            assertInstanceOf(UIViewRoot.class, e.getComponent());
            assertEquals(PhaseId.RESTORE_VIEW, facesContext.getCurrentPhaseId());
            assertTrue(facesContext.getAttributes().containsKey("jakarta.faces.IS_BUILDING_INITIAL_STATE"));
            assertFalse(FaceletViewDeclarationLanguage.isRefreshingTransientBuild(facesContext));
            return null;
        }).once();
        
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecute();
        EasyMock.verify(bean);
    }

    public static interface PostAddToViewEventBean
    {
        public void invokePostAddToViewEvent(ComponentSystemEvent e);
    }

    /**
     * Test a component receive PostAddToViewEvent
     * 
     * @throws Exception
     */
    @Test
    void postAddToViewOnComponent() throws Exception
    {
        startViewRequest("/postAddToViewEvent_2.xhtml");

        PostAddToViewEventBean bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            public Object answer()
            {
                ComponentSystemEvent e = (ComponentSystemEvent) EasyMock
                        .getCurrentArguments()[0];
                assertInstanceOf(UIForm.class, e.getComponent());
                assertEquals(PhaseId.RENDER_RESPONSE, facesContext.getCurrentPhaseId());
                return null;
            }
        }).once();
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecuteAndRender();
        EasyMock.verify(bean);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(button);
        
        bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        // With PSS PostAddToViewEvent is called again on restore view phase
        // but when building initial state.
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            public Object answer()
            {
                ComponentSystemEvent e = (ComponentSystemEvent) EasyMock
                        .getCurrentArguments()[0];
                assertInstanceOf(UIForm.class, e.getComponent());
                assertEquals(PhaseId.RESTORE_VIEW, facesContext.getCurrentPhaseId());
                assertTrue(facesContext.getAttributes().containsKey("jakarta.faces.IS_BUILDING_INITIAL_STATE"));
                assertFalse(FaceletViewDeclarationLanguage.isRefreshingTransientBuild(facesContext));
                return null;
            }
        }).once();
        
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecute();
        EasyMock.verify(bean);
    }

    /**
     * Test that a component added dynamically should receive PostAddToViewEvent
     * 
     * @throws Exception
     */
    @Test
    void postAddToViewOnComponentCif() throws Exception
    {
        startViewRequest("/postAddToViewEvent_3.xhtml");

        PostAddToViewEventBean bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            fail();
            return null;
        }).anyTimes();
        EasyMock.replay(bean);

        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        request.setAttribute("condition", Boolean.FALSE);
        processLifecycleExecuteAndRender();
        EasyMock.verify(bean);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(button);
        
        bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            fail();
            return null;
        }).anyTimes();
        
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecute();
        EasyMock.verify(bean);
        
        bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            ComponentSystemEvent e = (ComponentSystemEvent) EasyMock.getCurrentArguments()[0];
            assertInstanceOf(UIPanel.class, e.getComponent());
            assertEquals(PhaseId.RENDER_RESPONSE, facesContext.getCurrentPhaseId());
            return null;
        }).once();
        EasyMock.replay(bean);
        
        request.setAttribute("postAddToViewEventBean", bean);
        request.setAttribute("condition", Boolean.TRUE);
        renderResponse();
        EasyMock.verify(bean);
    }


    @Test
    void postAddToViewOnComponentHead() throws Exception
    {
        startViewRequest("/postAddToViewEvent_4.xhtml");

        PostAddToViewEventBean bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            ComponentSystemEvent e = (ComponentSystemEvent) EasyMock.getCurrentArguments()[0];
            assertInstanceOf(UIOutput.class, e.getComponent());
            assertEquals("jakarta.faces.Head", e.getComponent().getRendererType());
            return null;
        }).once();
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecuteAndRender();
        EasyMock.verify(bean);
        
        UICommand button = (UICommand) facesContext.getViewRoot().findComponent("mainForm:submit");
        client.submit(button);
        
        bean = EasyMock.createMock(PostAddToViewEventBean.class);
        bean.invokePostAddToViewEvent(EasyMock.isA(ComponentSystemEvent.class));
        // With PSS PostAddToViewEvent is called again on restore view phase
        // but when building initial state.
        EasyMock.expectLastCall().andAnswer(() -> {
            ComponentSystemEvent e = (ComponentSystemEvent) EasyMock.getCurrentArguments()[0];
            assertInstanceOf(UIOutput.class, e.getComponent());
            assertEquals("jakarta.faces.Head", e.getComponent().getRendererType());
            return null;
        }).once();
        
        EasyMock.replay(bean);
        //Put on request map
        request.setAttribute("postAddToViewEventBean", bean);
        processLifecycleExecute();
        EasyMock.verify(bean);
    }
}
