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
package org.apache.myfaces.application;

import junit.framework.TestCase;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.mock.*;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycle;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.*;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorBase;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKitFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

public class ClientBehaviorTestCase extends TestCase
{

    // Mock object instances for our tests
    protected ApplicationImpl application = null;
    protected MockServletConfig       config = null;
    protected MockExternalContext     externalContext = null;
    protected MockFacesContext        facesContext = null;
    protected MockFacesContextFactory facesContextFactory = null;
    protected MockLifecycle           lifecycle = null;
    protected MockLifecycleFactory    lifecycleFactory = null;
    protected MockRenderKit           renderKit = null;
    protected MockHttpServletRequest  request = null;
    protected MockHttpServletResponse response = null;
    protected MockServletContext      servletContext = null;
    protected MockHttpSession         session = null;

    // Thread context class loader saved and restored after each test
    private ClassLoader threadContextClassLoader = null;    


   public ClientBehaviorTestCase(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Set up a new thread context class loader
        threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0],
                this.getClass().getClassLoader()));

        // Set up Servlet API Objects
        servletContext = new MockServletContext();
        config = new MockServletConfig(servletContext);
        session = new MockHttpSession();
        session.setServletContext(servletContext);
        request = new MockHttpServletRequest(session);
        request.setServletContext(servletContext);
        response = new MockHttpServletResponse();
        externalContext =
            new MockExternalContext(servletContext, request, response);

        // Set up JSF API Objects
        FactoryFinder.releaseFactories();
        RuntimeConfig.getCurrentInstance(externalContext).setPropertyResolver(new MockPropertyResolver());
        RuntimeConfig.getCurrentInstance(externalContext).setVariableResolver(new MockVariableResolver());
        RuntimeConfig.getCurrentInstance(externalContext).setExpressionFactory(new MockExpressionFactory());
        //To make work ValueExpressions


        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                ApplicationFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
        "org.apache.myfaces.test.mock.MockFacesContextFactory");
        FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY,
        "org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory");
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
        "org.apache.myfaces.test.mock.MockRenderKitFactory");
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                "org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl");

        lifecycleFactory = (MockLifecycleFactory)
        FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        lifecycle = (MockLifecycle)
        lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        facesContextFactory = (MockFacesContextFactory)
        FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        facesContext = (MockFacesContext)
        facesContextFactory.getFacesContext(servletContext,
                request,
                response,
                lifecycle);
        externalContext = (MockExternalContext) facesContext.getExternalContext();
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/viewId");
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
        ApplicationFactory applicationFactory = (ApplicationFactory)
          FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (ApplicationImpl) applicationFactory.getApplication();
        facesContext.setApplication(application);
        RenderKitFactory renderKitFactory = (RenderKitFactory)
        FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = new MockRenderKit();
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);

        //We need this two components added
        application.addComponent(UIOutput.COMPONENT_TYPE, UIOutput.class.getName());
        application.addComponent(UIPanel.COMPONENT_TYPE, UIPanel.class.getName());
    }

   @Override
   public void tearDown() throws Exception
   {
        RuntimeConfig.getCurrentInstance(externalContext).purge();
        application = null;
        config = null;
        externalContext = null;
        facesContext.release();
        facesContext = null;
        lifecycle = null;
        lifecycleFactory = null;
        renderKit = null;
        request = null;
        response = null;
        servletContext = null;
        session = null;
        FactoryFinder.releaseFactories();

        Thread.currentThread().setContextClassLoader(threadContextClassLoader);
        threadContextClassLoader = null;

   }

    @FacesBehavior("org.apache.myfaces.component.MockClientBehavior")
    @ResourceDependencies({
      @ResourceDependency(name="test.js", library="test", target="head")
    })
    public static class MockClientBehavior extends ClientBehaviorBase
    {
    }

    public static class UITestComponentWithBehavior extends UIComponentBase
    {
        public static final String COMPONENT_TYPE = "javax.faces.UITestComponentWithBehavior";
        public static final String COMPONENT_FAMILY = "javax.faces.UITestComponentWithBehavior";
        public static final String DEFAULT_RENDERER_TYPE = "javax.faces.UITestComponentWithBehavior";

        static private final java.util.Collection<String> CLIENT_EVENTS_LIST =
            java.util.Collections.unmodifiableCollection(
                java.util.Arrays.asList(
                  "click"
            ));

        public UITestComponentWithBehavior()
        {
            setRendererType(DEFAULT_RENDERER_TYPE);
        }

        @Override
        public String getFamily()
        {
            return COMPONENT_FAMILY;
        }

        public java.util.Collection<String> getEventNames()
        {
            return CLIENT_EVENTS_LIST;
        }
    }

    public void testAddBehaviorWithResourceDependencies() throws Exception
    {

        application.addComponent(UITestComponentWithBehavior.COMPONENT_TYPE,
                UITestComponentWithBehavior.class.getName());
        application.addComponent(UIOutput.COMPONENT_TYPE,
                UIOutput.class.getName());

        UITestComponentWithBehavior comp = (UITestComponentWithBehavior)
            application.createComponent(UITestComponentWithBehavior.COMPONENT_TYPE);


        application.addBehavior("myBehaviorId", MockClientBehavior.class.getName());
        ClientBehavior behavior = (ClientBehavior) application.createBehavior("myBehaviorId");
        comp.addClientBehavior("click", behavior);

        // verify that method still works
        assertTrue(comp.getClientBehaviors().get("click").contains(behavior));

        // get behavior resource
        List<UIComponent> resources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(1, resources.size());
        Map<String,Object> attrMap = resources.get(0).getAttributes();
        assertEquals("test.js", attrMap.get("name"));
    }
}
