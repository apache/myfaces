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
package org.apache.myfaces.test.mock;

import java.util.Locale;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.render.RenderKitFactory;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.myfaces.test.config.ResourceBundleVarNames;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycle;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory;

/**
 *
 */
public class MockedJsfTestContainer implements HttpSessionListener
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this test case.</p>
     */
    public MockedJsfTestContainer()
    {
    }

    // ---------------------------------------------------- Overall Test Methods

    /**
     * <p>Set up instance variables required by this test case.</p>
     */
    public void setUp()
    {
        // Set up Servlet API Objects
        setUpServletContext();

        // Set up JSF API Objects
        FactoryFinder.releaseFactories();

        setFactories();

        setUpJSFObjects();
    }
    
    public void setUpAll()
    {
        setUp();
        startRequest();
    }
    
    public void tearDownAll()
    {
        endRequest();
        tearDownRequest();
    }

    /**
     * <p>Setup JSF object used for the test. By default it calls to the following
     * methods in this order:</p>
     * 
     * <ul>
     * <li><code>setUpLifecycle();</code></li>
     * <li><code>setUpApplication();</code></li>
     * <li><code>setUpRenderKit();</code></li>
     * </ul>
     * 
     * @throws Exception
     */
    protected void setUpJSFObjects()
    {
        setUpLifecycle();
        setUpApplication();
        setUpRenderKit();
    }

    /**
     * <p>Setup servlet objects that will be used for the test:</p>
     * 
     * <ul>
     * <li><code>config</code> (<code>MockServletConfig</code>)</li>
     * <li><code>servletContext</code> (<code>MockServletContext</code>)</li>
     * </ul>
     * 
     * @throws Exception
     */
    protected void setUpServletContext()
    {
        servletContext = new MockServletContext();
        config = new MockServletConfig(servletContext);
        webContainer = new MockWebContainer();
        servletContext.setWebContainer(webContainer);
        // Subscribe the container to receive session creation and destroy events.
        webContainer.subscribeListener(this);
    }
    
    /**
     * <p>Setup servlet objects that will be used for the test:</p>
     * 
     * <ul>
     * <li><code>request</code> (<code>MockHttpServletRequest</code></li>
     * <li><code>response</code> (<code>MockHttpServletResponse</code>)</li>
     * </ul>
     * 
     * @throws Exception
     */
    protected void setUpRequest()
    {
        request = lastSession == null ? 
            new MockHttpServletRequest() : new MockHttpServletRequest(lastSession);
        requestInitializedCalled = false;
        request.setServletContext(servletContext);
        response = new MockHttpServletResponse();
    }
    
    protected void doRequestInitialized()
    {
        if (!requestInitializedCalled)
        {
            webContainer.requestInitialized(new ServletRequestEvent(servletContext, request));
            requestInitializedCalled = true;
        }
    }
    
    public void startRequest()
    {
        setUpRequest();        
        doRequestInitialized();
        
        setUpFacesContext();
        setUpDefaultView();
    }
    
    public void startSession()
    {
        if (request != null)
        {
            //Create it indirectly through call to getSession(...)
            request.getSession(true);
        }
    }
    
    public void endSession()
    {
        MockHttpSession session = (MockHttpSession) request.getSession(false);
        if (session != null)
        {
            session.invalidate();
        }
    }
    
    public void sessionCreated(HttpSessionEvent se)
    {
        lastSession = (MockHttpSession) se.getSession();
        //No op
    }

    public void sessionDestroyed(HttpSessionEvent se)
    {
        lastSession = null;
    }
    
    /**
     * <p>Set JSF factories using FactoryFinder method setFactory.</p>
     * 
     * @throws Exception
     */
    protected void setFactories()
    {
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                "org.apache.myfaces.test.mock.MockApplicationFactory");
        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
                "org.apache.myfaces.test.mock.MockFacesContextFactory");
        FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY,
                "org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory");
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
                "org.apache.myfaces.test.mock.MockRenderKitFactory");
        FactoryFinder.setFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY,
                "org.apache.myfaces.test.mock.MockExceptionHandlerFactory");
        FactoryFinder.setFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY,
                "org.apache.myfaces.test.mock.MockPartialViewContextFactory");
        FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY,
                "org.apache.myfaces.test.mock.visit.MockVisitContextFactory");
        FactoryFinder.setFactory(FactoryFinder.CLIENT_WINDOW_FACTORY,
                "org.apache.myfaces.test.mock.MockClientWindowFactory");
    }

    /**
     * Setup the <code>lifecycle</code> and <code>lifecycleFactory</code>
     * variables.
     * 
     * @throws Exception
     */
    protected void setUpLifecycle()
    {
        lifecycleFactory = (MockLifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        lifecycle = (MockLifecycle) lifecycleFactory
                .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
    }

    /**
     * Setup the <code>facesContextFactory</code> and <code>facesContext</code>
     * variable. Before end, by default it override <code>externalContext</code>
     * variable from the value retrieved from facesContext.getExternalContext(),
     * because sometimes it is possible facesContext overrides externalContext
     * internally.
     * 
     * @throws Exception
     */
    protected void setUpFacesContext()
    {
        facesContextFactory = (MockFacesContextFactory) FactoryFinder
                .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        facesContext = (MockFacesContext) facesContextFactory.getFacesContext(
                servletContext, request, response, lifecycle);
        if (facesContext.getExternalContext() != null)
        {
            externalContext = (MockExternalContext) facesContext
                    .getExternalContext();
        }
        else
        {
            externalContext = new MockExternalContext(servletContext, request, response);
            facesContext.setExternalContext(externalContext);
        }
        facesContext.setApplication(application);
    }

    /**
     * By default, create an instance of UIViewRoot, set its viewId as "/viewId"
     * and assign it to the current facesContext.
     * 
     * @throws Exception
     */
    protected void setUpDefaultView()
    {
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/viewId");
        root.setLocale(getLocale());
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
    }
    
    protected Locale getLocale()
    {
        return Locale.getDefault();
    }    

    /**
     * Setup the <code>application</code> variable and before
     * the end by default it is assigned to the <code>facesContext</code>
     * variable, calling <code>facesContext.setApplication(application)</code>
     * 
     * @throws Exception
     */
    protected void setUpApplication()
    {
        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder
                .getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (MockApplication) applicationFactory.getApplication();
    }

    /**
     * Setup the <code>renderKit</code> variable. This is a good place to use
     * <code>ConfigParser</code> to register converters, validators, components
     * or renderkits.
     * 
     * @throws Exception
     */
    protected void setUpRenderKit()
    {
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder
                .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = new MockRenderKit();
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                renderKit);
    }

    public MockApplication10 getApplication()
    {
        return application;
    }
    
    public MockExternalContext getExternalContext()
    {
        return externalContext;
    }

    public MockFacesContext getFacesContext()
    {
        return facesContext;
    }

    public MockHttpServletRequest getRequest()
    {
        return request;
    }

    public MockHttpServletResponse getResponse()
    {
        return response;
    }

    public MockServletContext getServletContext()
    {
        return servletContext;
    }
    
    /**
     * @return the webContainer
     */
    public MockWebContainer getWebContainer()
    {
        return webContainer;
    }

    /**
     * This method call doRequestDestroyed() and then tearDownRequest(). 
     */
    public final void endRequest()
    {
        
        doRequestDestroyed();
        tearDownRequest();
    }

    protected void doRequestDestroyed()
    {
        if (request != null)
        {
            webContainer.requestDestroyed(new ServletRequestEvent(servletContext, request));
        }
    }

    protected void tearDownRequest()
    {
        if (facesContext != null)
        {
            facesContext.release();
        }
        externalContext = null;
        facesContext = null;
        request = null;
        response = null;
    }
    
    /**
     * <p>Tear down instance variables required by this test case.</p>
     */
    public void tearDown()
    {
        if (facesContext != null)
        {
            facesContext.release();
        }
        application = null;
        config = null;
        externalContext = null;
        facesContext = null;
        lifecycle = null;
        lifecycleFactory = null;
        renderKit = null;
        request = null;
        response = null;
        servletContext = null;
        lastSession = null;
        webContainer = null;
        FactoryFinder.releaseFactories();
        ResourceBundleVarNames.resetNames();
    }

    // ------------------------------------------------------ Instance Variables

    // Mock object instances for our tests
    protected MockApplication application = null;
    protected MockServletConfig config = null;
    protected MockExternalContext externalContext = null;
    protected MockFacesContext facesContext = null;
    protected MockFacesContextFactory facesContextFactory = null;
    protected MockLifecycle lifecycle = null;
    protected MockLifecycleFactory lifecycleFactory = null;
    protected MockRenderKit renderKit = null;
    protected MockHttpServletRequest request = null;
    protected boolean requestInitializedCalled = false;
    protected MockHttpServletResponse response = null;
    protected MockHttpSession lastSession = null;
    protected MockServletContext servletContext = null;
    private MockWebContainer webContainer = null;

}
