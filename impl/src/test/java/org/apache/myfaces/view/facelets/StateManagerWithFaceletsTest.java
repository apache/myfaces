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
package org.apache.myfaces.view.facelets;

import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.renderkit.html.HtmlResponseStateManager;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.spi.impl.DefaultSerialFactory;
import org.apache.myfaces.test.mock.MockFacesContext20;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateManagerWithFaceletsTest extends AbstractFaceletMultipleRequestsTestCase
{

    @Override
    protected void setUpApplication() throws Exception
    {
        super.setUpApplication();
        
        application.setStateManager(new StateManagerImpl());
    }

    @Override
    protected void setUpServletContextAndSession() throws Exception
    {
        super.setUpServletContextAndSession();
        
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "true");
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.StateSavingMethod.SERVER.name());
    }

    @Test
    public void testWriteAndRestoreState() throws Exception
    {
        String viewId = "/simpleTree.xhtml";
        
        String viewStateParam = null;
        try
        {
            setupRequest();
            
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId(viewId);
            vdl.buildView(facesContext, root, viewId);
            
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, viewId);
            StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
            
            application.getStateManager().writeState(facesContext, sms.saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
            
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, viewId);
            StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
            
            UIViewRoot restoredViewRoot = sms.restoreView(facesContext, viewId, RenderKitFactory.HTML_BASIC_RENDER_KIT);
            
            Assertions.assertNotNull(restoredViewRoot);
        }
        finally
        {
            tearDownRequest();
        }
    }
    
    @Test
    public void testWriteAndRestoreStateWithMyFacesRSM() throws Exception
    {
        String viewId = "/simpleTree.xhtml";
        String viewStateParam = null;
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());

        try
        {
            setupRequest();
            
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId(viewId);
            vdl.buildView(facesContext, root, viewId);
            
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, viewId);
            StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
            
            application.getStateManager().writeState(facesContext, sms.saveView(facesContext));
            
            viewStateParam = application.getStateManager().getViewState(facesContext);
            
        }
        finally
        {
            tearDownRequest();
        }
        
        try
        {
            setupRequest();
            
            request.addParameter(ResponseStateManager.VIEW_STATE_PARAM, viewStateParam);
            ((MockFacesContext20)facesContext).setPostback(true);
    
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, viewId);
            StateManagementStrategy sms = vdl.getStateManagementStrategy(facesContext, viewId);
            
            UIViewRoot restoredViewRoot = sms.restoreView(facesContext, viewId, RenderKitFactory.HTML_BASIC_RENDER_KIT);
            
            Assertions.assertNotNull(restoredViewRoot);
        }
        finally
        {
            tearDownRequest();
        }
    }

}
