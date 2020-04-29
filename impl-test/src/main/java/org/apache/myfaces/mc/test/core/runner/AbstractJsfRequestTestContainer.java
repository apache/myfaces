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
package org.apache.myfaces.mc.test.core.runner;

import java.io.IOException;
import java.util.List;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletRequestEvent;
import org.apache.myfaces.mc.test.core.mock.MockMyFacesClient;
import org.apache.myfaces.mc.test.core.mock.ServletMockContainer;
import org.apache.myfaces.mc.test.core.annotation.AfterRequest;
import org.apache.myfaces.mc.test.core.annotation.BeforeRequest;
import org.apache.myfaces.mc.test.core.annotation.TestConfig;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.apache.myfaces.test.mock.MockHttpSession;
import org.apache.myfaces.test.mock.MockHttpSessionProxy;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 *
 */
public class AbstractJsfRequestTestContainer extends AbstractJsfTestContainer
    implements ServletMockContainer
{

    public AbstractJsfRequestTestContainer(TestClass testClass)
    {
        super(testClass);
    }
    
    @Override
    public void setUp(Object testInstance)
    {
        super.setUp(testInstance);
        
    }

    @Override
    public void tearDown()
    {
        endRequest();
        session = null;
        lastSession = null;
        if (client != null)
        {
            client.setTestCase(null);
        }
        client = null;
        super.tearDown();
    }

    protected void setupRequest()
    {
        setupRequest(null);
    }

    protected void setupRequest(String pathInfo)
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

    protected void setupRequest(String pathInfo, String query)
    {
        if (request != null)
        {
            //tear down previous request
            endRequest();
        }
        request = lastSession == null ? 
            new MockHttpServletRequest() : new MockHttpServletRequest(lastSession);
        request.setServletContext(servletContext);
        requestInitializedCalled = false;
        if (session == null)
        {
            session = new MockHttpSessionProxy(servletContext, request);
        }
        else
        {
            session.setRequest(request);
        }
        session.setServletContext(servletContext);
        response = new MockHttpServletResponse();
        //TODO check if this is correct
        request.setPathElements(getContextPath(), getServletPath(), pathInfo, query);

        facesContext = facesContextFactory.getFacesContext(
            servletContext, request, response, lifecycle);
        externalContext = facesContext.getExternalContext();
        application = facesContext.getApplication();
        if (client != null)
        {
            client.apply(request);
            client.reset(facesContext);
        }
        else
        {
            client = createClient();
        }
    }
    
    protected MockMyFacesClient createClient()
    {
        return new MockMyFacesClient(facesContext, this);
    }

    /**
     * This method call startViewRequest(viewId) and doRequestInitialized()
     */
    public final void startViewRequest(String viewId)
    {
        setupRequest(viewId);
        doRequestInitialized();
    }
    
    /**
     * This method call startViewRequest(null) and doRequestInitialized()
     */
    public final void startRequest()
    {
        startViewRequest(null);
        doRequestInitialized();
    }
    
    public void doRequestInitialized()
    {
        if (!requestInitializedCalled)
        {
            List<FrameworkMethod> beforeRequestMethods = testClass.getAnnotatedMethods(BeforeRequest.class);
            if (beforeRequestMethods != null && !beforeRequestMethods.isEmpty())
            {
                for (FrameworkMethod fm : beforeRequestMethods)
                {
                    try
                    {
                        fm.invokeExplosively(testInstance);
                    }
                    catch (Throwable ex)
                    {
                        throw new FacesException(ex);
                    }
                }
            }
            
            webContainer.requestInitialized(new ServletRequestEvent(servletContext, request));
            requestInitializedCalled = true;
        }
    }
    
    /**
     * This method call doRequestDestroyed() and then tearDownRequest(). 
     */
    public final void endRequest()
    {
        doRequestDestroyed();
        tearDownRequest();
    }
    
    public void doRequestDestroyed()
    {
        if (request != null)
        {
            List<FrameworkMethod> afterRequestMethods = testClass.getAnnotatedMethods(AfterRequest.class);
            if (afterRequestMethods != null && !afterRequestMethods.isEmpty())
            {
                for (FrameworkMethod fm : afterRequestMethods)
                {
                    try
                    {
                        fm.invokeExplosively(testInstance);
                    }
                    catch (Throwable ex)
                    {
                        throw new FacesException(ex);
                    }
                }
            }
            lastSession = (MockHttpSession) request.getSession(false);
            webContainer.requestDestroyed(new ServletRequestEvent(servletContext, request));
        }
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
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null)
        {
            return testConfig.contextPath();
        }
        return "/test";
    }
    
    protected String getServletPath()
    {
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null)
        {
            return testConfig.servletPath();
        }
        return "/faces";
    }

    public void processLifecycleExecute()
    {
        processLifecycleExecute(facesContext);
    }

    public void processLifecycleRender()
    {
        processLifecycleRender(facesContext);
    }
    
    public void processLifecycleExecuteAndRender()
    {
        processLifecycleExecute();
        renderResponse();
    }

    public void restoreView()
    {
        restoreView(facesContext);
    }
    
    public void applyRequestValues()
    {
        applyRequestValues(facesContext);
    }

    public void processValidations()
    {
        processValidations(facesContext);
    }

    public void updateModelValues()
    {
        updateModelValues(facesContext);

    }

    public void invokeApplication()
    {
        invokeApplication(facesContext);
    }
    
    public void renderResponse()
    {
        renderResponse(facesContext);
    }
    
    public void processRemainingExecutePhases()
    {
        processRemainingExecutePhases(facesContext);
    }

    public void processRemainingPhases()
    {
        processRemainingPhases(facesContext);
    }
    
    public void executeBeforeRender()
    {
        executeBeforeRender(facesContext);
    }
    
    public void executeViewHandlerRender()
    {
        executeViewHandlerRender(facesContext);
    }
        
    public void executeBuildViewCycle()
    {
        executeBuildViewCycle(facesContext);
    }
    
    public void executeAfterRender()
    {
        executeAfterRender(facesContext);
    }
    
    public String getRenderedContent() throws IOException
    {
        return getRenderedContent(facesContext);
    }
    
    protected MockMyFacesClient client = null;
    
    // Servlet objects 
    protected MockHttpServletRequest request = null;
    protected boolean requestInitializedCalled = false;
    protected MockHttpServletResponse response = null;
    protected MockHttpSessionProxy session = null;
    protected MockHttpSession lastSession = null;
    
    protected Application application = null;
    protected ExternalContext externalContext = null;
    protected FacesContext facesContext = null;

    @Override
    public MockHttpServletRequest getRequest()
    {
        return request;
    }

    @Override
    public MockHttpServletResponse getResponse()
    {
        return response;
    }

    @Override
    public FacesContext getFacesContext()
    {
        return facesContext;
    }

    public MockMyFacesClient getClient()
    {
        return client;
    }

    public Application getApplication()
    {
        return application;
    }

    public ExternalContext getExternalContext()
    {
        return externalContext;
    }

}
