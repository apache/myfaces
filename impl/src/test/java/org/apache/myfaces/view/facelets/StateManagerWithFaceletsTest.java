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

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.renderkit.html.HtmlResponseStateManager;
import org.apache.myfaces.util.StateUtils;
import org.apache.myfaces.util.serial.DefaultSerialFactory;
import org.apache.myfaces.test.mock.MockFacesContext20;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.junit.Assert;
import org.junit.Test;

public class StateManagerWithFaceletsTest extends FaceletMultipleRequestsTestCase
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
        servletContext.addInitParameter(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateManager.STATE_SAVING_METHOD_SERVER);
    }

    @Test
    public void testWriteAndRestoreState() throws Exception
    {
        String viewStateParam = null;
        try
        {
            setupRequest();
            
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/simpleTree.xhtml");
            vdl.buildView(facesContext, root, "/simpleTree.xhtml");
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
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
    
            UIViewRoot restoredViewRoot = application.getStateManager().restoreView(facesContext, "/simpleTree.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);
            
            Assert.assertNotNull(restoredViewRoot);
        }
        finally
        {
            tearDownRequest();
        }
    }
    
    @Test
    public void testWriteAndRestoreStateWithMyFacesRSM() throws Exception
    {
        String viewStateParam = null;
        
        ((MockRenderKit)renderKit).setResponseStateManager(new HtmlResponseStateManager());
        StateUtils.initSecret(servletContext);
        servletContext.setAttribute(StateUtils.SERIAL_FACTORY, new DefaultSerialFactory());

        try
        {
            setupRequest();
            
            UIViewRoot root = facesContext.getViewRoot();
            root.setViewId("/simpleTree.xhtml");
            vdl.buildView(facesContext, root, "/simpleTree.xhtml");
            
            application.getStateManager().writeState(facesContext, application.getStateManager().saveView(facesContext));
            
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
    
            UIViewRoot restoredViewRoot = application.getStateManager().restoreView(facesContext, "/simpleTree.xhtml", RenderKitFactory.HTML_BASIC_RENDER_KIT);
            
            Assert.assertNotNull(restoredViewRoot);
        }
        finally
        {
            tearDownRequest();
        }
    }

}
