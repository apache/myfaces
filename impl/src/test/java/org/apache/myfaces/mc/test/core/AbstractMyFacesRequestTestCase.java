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
package org.apache.myfaces.mc.test.core;

import java.io.IOException;

import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.apache.myfaces.test.mock.MockHttpSession;

/**
 * <p>Abstract JUnit test case base class, with method to setup/teardown a request.
 * It helps to create tests that involve multiple requests like client submits, or
 * tests that involve more control over the lifecycle.</p>
 * 
 * 
 * @author Leonardo Uribe
 *
 */
public abstract class AbstractMyFacesRequestTestCase extends AbstractMyFacesTestCase
{
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
    }

    @Override
    public void tearDown() throws Exception
    {
        tearDownRequest();
        session = null;
        if (client != null)
        {
            client.setTestCase(null);
        }
        client = null;
        super.tearDown();
    }

    protected void setupRequest() throws Exception
    {
        setupRequest(null);
    }

    protected void setupRequest(String pathInfo) throws Exception
    {
        if (pathInfo == null)
        {
            setupRequest(null, null);
        }
        else
        {
            int queryIndex = pathInfo.indexOf("?");
            if (queryIndex >= 0) 
            {
                setupRequest(pathInfo.substring(0,queryIndex), pathInfo.substring(queryIndex+1));
            }
            else
            {
                setupRequest(pathInfo, null);
            }
        }
    }
    
    protected void setupRequest(String pathInfo, String query) throws Exception
    {
        session = (session == null) ? new MockHttpSession() : session;
        session.setServletContext(servletContext);
        request = new MockHttpServletRequest(session);
        request.setServletContext(servletContext);
        response = new MockHttpServletResponse();
        //TODO check if this is correct
        request.setPathElements(getContextPath(), getServletPath(), pathInfo, query);

        facesContext = facesContextFactory.getFacesContext(servletContext, request, response, lifecycle);
        externalContext = facesContext.getExternalContext();
        application = facesContext.getApplication();
        if (client != null)
        {
            client.apply(request);
        }
        //Reset client
        client = createClient();
    }
    
    protected MockMyFacesClient createClient()
    {
        return new MockMyFacesClient(facesContext, this);
    }
    
    protected void tearDownRequest()
    {
        if (facesContext != null)
        {
            facesContext.release();
        }
        facesContext = null;
        externalContext = null;
        application = null;
        
        response = null;
        request = null;
        //session = null;
    }
    
    protected String getContextPath()
    {
        return "/test";
    }
    
    protected String getServletPath()
    {
        return "/faces";
    }

    protected void processLifecycleExecute() throws Exception
    {
        processLifecycleExecute(facesContext);
    }
    
    protected void processLifecycleExecuteAndRender() throws Exception
    {
        processLifecycleExecute();
        processRender();
    }

    protected void processRestoreViewPhase() throws Exception
    {
        processRestoreViewPhase(facesContext);
    }
    
    protected void processApplyRequestValuesPhase() throws Exception
    {
        processApplyRequestValuesPhase(facesContext);
    }

    protected void processValidationsPhase() throws Exception
    {
        processValidationsPhase(facesContext);
    }

    protected void processUpdateModelPhase() throws Exception
    {
        processUpdateModelPhase(facesContext);

    }
    
    protected void processInvokeApplicationPhase() throws Exception
    {
        processInvokeApplicationPhase(facesContext);
    }
    
    protected void processRender() throws Exception
    {
        processRender(facesContext);
    }
    
    protected void processRemainingExecutePhases() throws Exception
    {
        processRemainingExecutePhases(facesContext);
    }

    protected void processRemainingPhases() throws Exception
    {
        processRemainingPhases(facesContext);
    }
    
    protected String getRenderedContent() throws IOException
    {
        return getRenderedContent(facesContext);
    }
    
    protected void inputText(UIComponent input, String text)
    {
        client.inputText((UIInput)input, text);
    }
    
    /**
     * Simulate a submit, processing the remaining phases and setting up the new request.
     * It delegates to client.submit, where the necessary data is gathered to be applied
     * later on client.apply method.
     * 
     * @param component
     * @throws Exception
     */
    protected void submit(UIComponent component) throws Exception
    {
        client.submit(component);
        /*
        processRemainingPhases();
        client.submit((UICommand)component);
        String viewId = facesContext.getViewRoot().getViewId();
        tearDownRequest();
        setupRequest(viewId);
        */
    }
    
    protected MockMyFacesClient client = null;
    
    // Servlet objects 
    protected MockHttpServletRequest request = null;
    protected MockHttpServletResponse response = null;
    protected MockHttpSession session = null;
    
    protected Application application = null;
    protected ExternalContext externalContext = null;
    protected FacesContext facesContext = null;

}
