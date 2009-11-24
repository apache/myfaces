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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;

import junit.framework.TestCase;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.test.mock.MockFacesContextFactory;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.apache.myfaces.test.mock.MockHttpSession;
import org.apache.myfaces.test.mock.MockPropertyResolver;
import org.apache.myfaces.test.mock.MockRenderKit;
import org.apache.myfaces.test.mock.MockRenderKitFactory;
import org.apache.myfaces.test.mock.MockServletConfig;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.mock.MockVariableResolver;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycle;
import org.apache.myfaces.test.mock.lifecycle.MockLifecycleFactory;

public class ApplicationImplAnnotationTest extends TestCase
{
    //TODO: need mock objects for VDL/VDLFactory
    //remove from excludes list in pom.xml after complete
    
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

    public ApplicationImplAnnotationTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
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

    /**
     * Test component for ApplicationImplTest
     * 
     */
    public static class UITestComponentA extends UIComponentBase
    {
        public static final String COMPONENT_TYPE = "javax.faces.TestComponentA";
        public static final String COMPONENT_FAMILY = "javax.faces.TestComponentA";
        public static final String DEFAULT_RENDERER_TYPE = "javax.faces.TestComponentA";

        public UITestComponentA()
        {
            setRendererType(DEFAULT_RENDERER_TYPE);
        }

        @Override
        public String getFamily()
        {
            return COMPONENT_FAMILY;
        }
    }

    @ListenerFor(systemEventClass=PostAddToViewEvent.class,
            sourceClass=UIComponentBase.class)
    @ResourceDependency(library = "testLib", name = "testResource.js")
    public static class TestRendererA extends Renderer
    {

    }

    public void testCreateComponentRenderer() throws Exception
    {
        facesContext.getViewRoot().setRenderKitId(
                MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                UITestComponentA.COMPONENT_FAMILY,
                UITestComponentA.DEFAULT_RENDERER_TYPE, new TestRendererA());

        application.addComponent(UITestComponentA.COMPONENT_TYPE,
                UITestComponentA.class.getName());
        
        UITestComponentA comp = (UITestComponentA) application.createComponent(facesContext, 
                UITestComponentA.COMPONENT_TYPE,
                UITestComponentA.DEFAULT_RENDERER_TYPE);
        
        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(1,componentResources.size());
        Map<String,Object> attrMap = componentResources.get(0).getAttributes();
        assertEquals("testResource.js",attrMap.get("name"));
        assertEquals("testLib",attrMap.get("library"));
    }

    @ResourceDependency(name = "testResource.js")
    public static class UITestComponentB extends UIComponentBase
    {
        public static final String COMPONENT_TYPE = "javax.faces.TestComponentB";
        public static final String COMPONENT_FAMILY = "javax.faces.TestComponentB";
        public static final String DEFAULT_RENDERER_TYPE = "javax.faces.TestComponentB";

        public UITestComponentB()
        {
            setRendererType(DEFAULT_RENDERER_TYPE);
        }

        @Override
        public String getFamily()
        {
            return COMPONENT_FAMILY;
        }
    }

    public void testCreateComponentAnnotation() throws Exception
    {
        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(UITestComponentB.COMPONENT_TYPE);
        
        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(1,componentResources.size());
        Map<String,Object> attrMap = componentResources.get(0).getAttributes();
        assertEquals("testResource.js",attrMap.get("name"));
    }
    
    public void testCreateComponentRendererAnnotation() throws Exception
    {
        facesContext.getViewRoot().setRenderKitId(
                MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                UITestComponentB.COMPONENT_FAMILY,
                UITestComponentB.DEFAULT_RENDERER_TYPE, new TestRendererA());

        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(facesContext, 
                UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.DEFAULT_RENDERER_TYPE);
        
        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(2,componentResources.size());
        for (UIComponent component : componentResources)
        {
            if ("testResource.js".equals(component.getAttributes().get("name")))
            {
                //Good!
            }
            else
            {
                fail("Not expected resource found"+component.getAttributes().get("name"));
            }
        }
    }
    
    public void testCreateComponentValueExpression1() throws Exception
    {
        
        ValueExpression expr = application.getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{testComponent}", Object.class);
        
        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(
                expr, facesContext, UITestComponentB.COMPONENT_TYPE);
        
        List<UIComponent> componentResources = 
            facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(1,componentResources.size());
        Map<String,Object> attrMap = componentResources.get(0).getAttributes();
        assertEquals("testResource.js",attrMap.get("name"));
    }
    
    public void testCreateComponentValueExpression2() throws Exception
    {
        
        ValueExpression expr = application.getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{testComponent}", Object.class);
        
        expr.setValue(facesContext.getELContext(), new UITestComponentB());
        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(
                expr, facesContext, UITestComponentB.COMPONENT_TYPE);
        
        List<UIComponent> componentResources = 
            facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(1,componentResources.size());
        Map<String,Object> attrMap = componentResources.get(0).getAttributes();
        assertEquals("testResource.js",attrMap.get("name"));
    }

    public void testCreateComponentValueExpression3() throws Exception
    {
        
        ValueExpression expr = application.getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{testComponent}", Object.class);
        
        facesContext.getViewRoot().setRenderKitId(
                MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                UITestComponentB.COMPONENT_FAMILY,
                UITestComponentB.DEFAULT_RENDERER_TYPE, new TestRendererA());

        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(expr, facesContext, 
                UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.DEFAULT_RENDERER_TYPE);
        
        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(2,componentResources.size());
        for (UIComponent component : componentResources)
        {
            if ("testResource.js".equals(component.getAttributes().get("name")))
            {
                //Good!
            }
            else
            {
                fail("Not expected resource found"+component.getAttributes().get("name"));
            }
        }
    }
    
    public void testCreateComponentValueExpression4() throws Exception
    {
        
        ValueExpression expr = application.getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{testComponent}", Object.class);
        
        expr.setValue(facesContext.getELContext(), new UITestComponentB());
        
        facesContext.getViewRoot().setRenderKitId(
                MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.getRenderKit().addRenderer(
                UITestComponentB.COMPONENT_FAMILY,
                UITestComponentB.DEFAULT_RENDERER_TYPE, new TestRendererA());

        application.addComponent(UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.class.getName());
        
        UITestComponentB comp = (UITestComponentB) application.createComponent(expr, facesContext, 
                UITestComponentB.COMPONENT_TYPE,
                UITestComponentB.DEFAULT_RENDERER_TYPE);
        
        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext, "head");
        assertEquals(2,componentResources.size());
        for (UIComponent component : componentResources)
        {
            if ("testResource.js".equals(component.getAttributes().get("name")))
            {
                //Good!
            }
            else
            {
                fail("Not expected resource found"+component.getAttributes().get("name"));
            }
        }
    }
    
    public void testDatetimeconverterDefaultTimezoneIsSystemTimezoneInitParameter()
    {
        servletContext.addInitParameter(
                "javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
        application.addConverter(java.util.Date.class, "javax.faces.convert.DateTimeConverter");
        Converter converter = application.createConverter(java.util.Date.class);
        assertEquals(((DateTimeConverter) converter).getTimeZone(), TimeZone.getDefault());
    }

}
