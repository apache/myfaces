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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ExpressionFactory;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.context.Flash;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.webapp.FacesServlet;
import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletResponse;
import org.apache.myfaces.config.ConfigFilesXmlValidationUtils;
import org.apache.myfaces.config.DefaultFacesConfigurationProvider;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.annotation.NoInjectionAnnotationLifecycleProvider;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.FactoryImpl;
import org.apache.myfaces.lifecycle.LifecycleImpl;
import org.apache.myfaces.lifecycle.ViewNotFoundException;
import org.apache.myfaces.mc.test.core.annotation.BeforeJSFInit;
import org.apache.myfaces.mc.test.core.mock.MockMyFacesViewDeclarationLanguageFactory;
import org.apache.myfaces.mc.test.core.annotation.DeclareFacesConfig;
import org.apache.myfaces.mc.test.core.annotation.ManagedBeans;
import org.apache.myfaces.mc.test.core.annotation.TestConfig;
import org.apache.myfaces.mc.test.core.annotation.PageBean;
import org.apache.myfaces.mc.test.core.annotation.SetupWebConfigParams;
import org.apache.myfaces.mc.test.core.annotation.TestServletListeners;
import org.apache.myfaces.mc.test.core.mock.DefaultContext;
import org.apache.myfaces.mc.test.core.mock.MockInitialContextFactory;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.impl.CDIAnnotationDelegateInjectionProvider;
import org.apache.myfaces.spi.impl.DefaultFacesConfigurationProviderFactory;
import org.apache.myfaces.spi.impl.NoInjectionAnnotationInjectionProvider;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.apache.myfaces.test.mock.MockServletConfig;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.mock.MockWebContainer;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.webapp.AbstractFacesInitializer;
import org.apache.myfaces.webapp.FacesInitializer;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.xml.sax.SAXException;

/**
 *
 */
public class AbstractJsfTestContainer
{
    private static final Class<?> PHASE_EXECUTOR_CLASS;
    private static final Class<?> PHASE_MANAGER_CLASS;
    
    static 
    {
        Class<?> phaseExecutorClass = null;
        Class<?> phaseManagerClass = null;
        try
        {
            phaseExecutorClass = Class.forName("org.apache.myfaces.lifecycle.PhaseExecutor");
            phaseManagerClass = Class.forName("org.apache.myfaces.lifecycle.PhaseListenerManager");
        }
        catch (ClassNotFoundException e)
        {
            //No op
        }
        PHASE_EXECUTOR_CLASS = phaseExecutorClass;
        PHASE_MANAGER_CLASS = phaseManagerClass;
    }
    
    public static final String PHASE_MANAGER_INSTANCE = "org.apache.myfaces.test.PHASE_MANAGER_INSTANCE";
    
    public static final String LAST_PHASE_PROCESSED = "oam.LAST_PHASE_PROCESSED";
    
    public static final String LAST_RENDER_PHASE_STEP = "oam.LAST_RENDER_PHASE_STEP";
    
    public static final int BEFORE_RENDER_STEP = 1;
    public static final int BUILD_VIEW_CYCLE_STEP = 2;
    public static final int VIEWHANDLER_RENDER_STEP = 3;
    public static final int AFTER_RENDER_STEP = 4;
    
    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this test case.</p>
     *
     */    
    public AbstractJsfTestContainer(TestClass testClass)
    {
        this.testClass = testClass;
    }

    // ---------------------------------------------------- Overall Test Methods

    /**
     * <p>Set up instance variables required by this test case.</p>
     */
    //@Before
    public void setUp(Object testInstance)
    {
        this.testInstance = testInstance;
        
        setUpClassloader();
        
        jsfConfiguration = sharedConfiguration.get(getTestJavaClass().getName());
        if (jsfConfiguration == null)
        {
            jsfConfiguration = new SharedFacesConfiguration();
        }
        
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        boolean enableJNDI = (testConfig != null) ? testConfig.enableJNDI() : true;
        if (enableJNDI)
        {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
            jndiContext = new DefaultContext();
            MockInitialContextFactory.setCurrentContext(jndiContext);
        }

        // Set up Servlet API Objects
        setUpServletObjects();

        // Set up JSF API Objects
        FactoryFinder.releaseFactories();

        setUpServletListeners();
        
        webContainer.contextInitialized(new ServletContextEvent(servletContext));
        
        setUpFacesServlet();
        
        sharedConfiguration.put(getTestJavaClass().getName(), jsfConfiguration);
    }
    
    /**
     * Set up the thread context classloader. JSF uses the this classloader
     * in order to find related factory classes and other resources, but in
     * some selected cases, the default classloader cannot be properly set.
     * 
     */
    protected void setUpClassloader()
    {
        // Set up a new thread context class loader
        threadContextClassLoader = Thread.currentThread()
                .getContextClassLoader();
        Thread.currentThread()
                .setContextClassLoader(
                        new URLClassLoader(new URL[0], this.getClass()
                                .getClassLoader()));
        classLoaderSet = true;
    }
    
    /**
     * <p>Setup servlet objects that will be used for the test:</p>
     * 
     * <ul>
     * <li><code>servletConfig</code> (<code>MockServletConfig</code>)</li>
     * <li><code>servletContext</code> (<code>MockServletContext</code>)</li>
     * </ul>
     * 
     */
    protected void setUpServletObjects()
    {
        servletContext = new MockServletContext();
        servletConfig = new MockServletConfig(servletContext);
        servletContext.setDocumentRoot(getWebappContextURI());
        webContainer = new MockWebContainer();
        servletContext.setWebContainer(webContainer);
        setUpWebConfigParams();
    }
    
    /**
     * <p>Setup web config params. By default it sets the following params</p>
     * 
     * <ul>
     * <li>"org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE", "true"</li>
     * <li>"jakarta.faces.PROJECT_STAGE", "UnitTest"</li>
     * <li>"jakarta.faces.PARTIAL_STATE_SAVING", "true"</li>
     * <li>"jakarta.faces.FACELETS_REFRESH_PERIOD", "-1"</li>
     * </ul>
     * 
     */
    protected void setUpWebConfigParams()
    {
        // Required parameters
        servletContext.addInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE", "true");
        servletContext.addInitParameter("jakarta.faces.PROJECT_STAGE", "UnitTest");
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME,"-1");
        servletContext.addInitParameter("org.apache.myfaces.config.annotation.LifecycleProvider",
            NoInjectionAnnotationLifecycleProvider.class.getName());
        
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null && testConfig.oamAnnotationScanPackages() != null &&
            testConfig.oamAnnotationScanPackages().length() > 0)
        {
            servletContext.addInitParameter("org.apache.myfaces.annotation.SCAN_PACKAGES",
                testConfig.oamAnnotationScanPackages());
        }

        List<FrameworkMethod> setupWebConfigParamMethods = testClass.getAnnotatedMethods(SetupWebConfigParams.class);
        if (setupWebConfigParamMethods != null && !setupWebConfigParamMethods.isEmpty())
        {
            for (FrameworkMethod fm : setupWebConfigParamMethods)
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
    }
    
    /**
     * <p>Return an URI that identifies the base path that will be used by servletContext
     * to load resources like facelet files an others. By default it points to the directory
     * path calculated from the package name of the child test class.</p>
     * 
     * @return
     */
    protected URI getWebappContextURI()
    {
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource(getWebappResourcePath());
            if (url == null)
            {
                throw new FileNotFoundException(cl.getResource("").getFile()
                        + getWebappResourcePath() + " was not found");
            }
            else
            {
                return new URI(url.toString());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error Initializing Context", e);
        }
    }
    
    /**
     * Return a path that is used to load resources like facelet files an others.
     * By default it points to the directory path calculated from the package 
     * name of the child test class.
     * 
     * @return
     */
    protected String getWebappResourcePath()
    {
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null && testConfig.webappResourcePath() != null &&
            !"testClassResourcePackage".equals(testConfig.webappResourcePath()))
        {
            return testConfig.webappResourcePath();
        }
        return getTestJavaClass().getName().substring(0,
                getTestJavaClass().getName().lastIndexOf('.')).replace('.', '/')
                + "/";
    }
    
    /**
     * Create the ExpressionFactory instance that will be used to initialize the test
     * environment. By default it uses MockExpressionFactory. 
     * 
     * @return
     */
    protected ExpressionFactory createExpressionFactory()
    {
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null && testConfig.expressionFactory() != null &&
            testConfig.expressionFactory().length() > 0)
        {
            return (ExpressionFactory) ClassUtils.newInstance(
                testConfig.expressionFactory(), ExpressionFactory.class);
        }
        return new MockExpressionFactory();
    }
    
    /**
     * setup servlets avaliable in the test environment
     * 
     * @throws Exception
     */
    protected void setUpServlets() throws Exception
    {
        setUpFacesServlet();
    }
    
    /**
     * setup listeners avaliable in the test environment
     * 
     */
    protected void setUpServletListeners()
    {
        TestServletListeners testServletListeners = getTestJavaClass().getAnnotation(TestServletListeners.class);
        if (testServletListeners != null && testServletListeners.value() != null)
        {
            for (String listener : testServletListeners.value())
            {
                try
                {
                    webContainer.subscribeListener(listener);
                }
                catch (Exception ex)
                {
                    throw new FacesException(ex);
                }
            }
        }

        // Subscribe a listener so we can trigger a method after all listeners but before initialize MyFaces
        webContainer.subscribeListener(new ServletContextListener()
        {
            @Override
            public void contextInitialized(ServletContextEvent sce)
            {
                List<FrameworkMethod> setupWebConfigParamMethods = testClass.getAnnotatedMethods(BeforeJSFInit.class);
                if (setupWebConfigParamMethods != null && !setupWebConfigParamMethods.isEmpty())
                {
                    for (FrameworkMethod fm : setupWebConfigParamMethods)
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
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce)
            {
            }
            
        });

        //owbListener = new WebBeansConfigurationListener();
        //webContainer.subscribeListener(owbListener);
        setUpMyFaces();
    }
    
    /**
     * 
     * @return
     */
    protected FacesConfigurationProvider createFacesConfigurationProvider()
    {
        return new MyFacesMockFacesConfigurationProvider(); 
    }
    
    protected AbstractFacesInitializer createFacesInitializer()
    {
        return new JUnitFacesInitializer(this);
    }
    
    protected void setUpMyFaces()
    {
        if (facesConfigurationProvider == null)
        {
            facesConfigurationProvider = createFacesConfigurationProvider();
        }
        servletContext.setAttribute(
                DefaultFacesConfigurationProviderFactory.FACES_CONFIGURATION_PROVIDER_INSTANCE_KEY, 
                facesConfigurationProvider);
        listener = new StartupServletContextListener();
        listener.setFacesInitializer(getFacesInitializer());
        webContainer.subscribeListener(listener);
        //listener.contextInitialized(new ServletContextEvent(servletContext));
    }

    protected void tearDownMyFaces()
    {
        //Don't tear down FacesConfigurationProvider, because that is shared by all tests.
        //This helps to reduce the time each test takes 
        //facesConfigurationProvider = null
        
        //listener.contextDestroyed(new ServletContextEvent(servletContext));
    }

    protected void setUpFacesServlet()
    {
        lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
    }
    
    protected void tearDownFacesServlet()
    {
        lifecycleFactory = null;
        facesContextFactory = null;
    }
    
    protected void tearDownServlets()
    {
        tearDownFacesServlet();
    }
    
    protected void tearDownServletListeners()
    {
        tearDownMyFaces();
        //owbListener = null;
    }
    //@After
    public void tearDown()
    {
        tearDownServlets();

        webContainer.contextDestroyed(new ServletContextEvent(servletContext));
        
        tearDownServletListeners();
        
        listener = null;
        
        servletConfig = null;
        servletContext = null;
        
        FactoryFinder.releaseFactories();
        
        if (jndiContext != null)
        {
            MockInitialContextFactory.clearCurrentContext();
        }
        
        tearDownClassloader();
    }
    
    protected void tearDownClassloader()
    {
        if (classLoaderSet)
        {
            Thread.currentThread().setContextClassLoader(threadContextClassLoader);
            threadContextClassLoader = null;
            classLoaderSet = false;
        }
    }    
    
    //@AfterClass
    public static void tearDownClass()
    {
        standardFacesConfig = null;
        sharedConfiguration.clear();
    }
    
    public static void tearDownClass(Class<?> targetTestClass)
    {
        sharedConfiguration.remove(targetTestClass);
    }    
    
    private String getLifecycleId()
    {
        // 1. check for Servlet's init-param
        // 2. check for global context parameter
        // 3. use default Lifecycle Id, if none of them was provided
        String serLifecycleId = servletConfig.getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        String appLifecycleId = servletConfig.getServletContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        appLifecycleId = serLifecycleId == null ? appLifecycleId : serLifecycleId;
        return appLifecycleId != null ? appLifecycleId : LifecycleFactory.DEFAULT_LIFECYCLE;
    }

    /**
     * Call lifecycle.execute(facesContext)
     * 
     * @param facesContext
     */
    public void processLifecycleExecute(FacesContext facesContext)
    {
        lifecycle.attachWindow(facesContext);
        lifecycle.execute(facesContext);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.INVOKE_APPLICATION);
    }

    /**
     * Execute restore view phase.
     * 
     * @param facesContext
     */
    public void restoreView(FacesContext facesContext)
    {
        lifecycle.attachWindow(facesContext);
        executePhase(facesContext, PhaseId.RESTORE_VIEW);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.RESTORE_VIEW);
    }
    
    /**
     * Execute apply request values phase. If the responseComplete or renderResponse
     * flags are set, it returns without do any action.
     * 
     * @param facesContext
     */
    public void applyRequestValues(FacesContext facesContext)
    {
        if (facesContext.getRenderResponse() || facesContext.getResponseComplete())
        {
            return;
        }
        processRemainingPhasesBefore(facesContext, PhaseId.APPLY_REQUEST_VALUES);
        executePhase(facesContext, PhaseId.APPLY_REQUEST_VALUES);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.APPLY_REQUEST_VALUES);
    }

    /**
     * Execute process validations phase. If the responseComplete or renderResponse
     * flags are set, it returns without do any action.
     * 
     * @param facesContext
     */
    public void processValidations(FacesContext facesContext)
    {
        if (facesContext.getRenderResponse() || facesContext.getResponseComplete())
        {
            return;
        }
        processRemainingPhasesBefore(facesContext, PhaseId.PROCESS_VALIDATIONS);
        executePhase(facesContext, PhaseId.PROCESS_VALIDATIONS);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.PROCESS_VALIDATIONS);
    }

    /**
     * Execute update model phase. If the responseComplete or renderResponse
     * flags are set, it returns without do any action.
     * 
     * @param facesContext
     */
    public void updateModelValues(FacesContext facesContext)
    {
        if (facesContext.getRenderResponse() || facesContext.getResponseComplete())
        {
            return;
        }
        processRemainingPhasesBefore(facesContext, PhaseId.UPDATE_MODEL_VALUES);
        executePhase(facesContext, PhaseId.UPDATE_MODEL_VALUES);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.UPDATE_MODEL_VALUES);

    }
    
    /**
     * Execute invoke application phase. If the responseComplete or renderResponse
     * flags are set, it returns without do any action.
     * 
     * @param facesContext
     */
    public void invokeApplication(FacesContext facesContext)
    {
        if (facesContext.getRenderResponse() || facesContext.getResponseComplete())
        {
            return;
        }
        processRemainingPhasesBefore(facesContext, PhaseId.INVOKE_APPLICATION);
        executePhase(facesContext, PhaseId.INVOKE_APPLICATION);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.INVOKE_APPLICATION);
    }

    public void processLifecycleRender(FacesContext facesContext)
    {
        renderResponse(facesContext);
    }

    /**
     * Call lifecycle.render(facesContext)
     * 
     * @param facesContext
     */
    public void renderResponse(FacesContext facesContext)
    {
        processRemainingExecutePhases(facesContext);
        lifecycle.render(facesContext);
        facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.RENDER_RESPONSE);
        facesContext.getAttributes().put(LAST_RENDER_PHASE_STEP, AFTER_RENDER_STEP);
    }
    
    protected void processRemainingPhasesBefore(FacesContext facesContext, PhaseId phaseId)
    {
        PhaseId lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
        if (lastPhaseId == null)
        {
            if (!phaseId.equals(PhaseId.RESTORE_VIEW))
            {
                restoreView(facesContext);
                lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            }
            else
            {
                // There are no phases before restore view
                return;
            }
        }
        if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId))
        {
            return;
        }
        boolean continueProcess = false;
        if (continueProcess || PhaseId.RESTORE_VIEW.equals(lastPhaseId))
        {
            applyRequestValues(facesContext);
            lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            continueProcess = true;
        }
        if (PhaseId.PROCESS_VALIDATIONS.equals(phaseId))
        {
            return;
        }
        if (continueProcess || PhaseId.APPLY_REQUEST_VALUES.equals(lastPhaseId))
        {
            processValidations(facesContext);
            lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            continueProcess = true;
        }
        if (PhaseId.UPDATE_MODEL_VALUES.equals(phaseId))
        {
            return;
        }
        if (continueProcess || PhaseId.PROCESS_VALIDATIONS.equals(lastPhaseId))
        {
            updateModelValues(facesContext);
            lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            continueProcess = true;
        }
        if (PhaseId.INVOKE_APPLICATION.equals(phaseId))
        {
            return;
        }
        if (continueProcess || PhaseId.UPDATE_MODEL_VALUES.equals(lastPhaseId))
        {
            invokeApplication(facesContext);
            lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            continueProcess = true;
        }        
        if (PhaseId.RENDER_RESPONSE.equals(phaseId))
        {
            return;
        }
        if (continueProcess || PhaseId.INVOKE_APPLICATION.equals(lastPhaseId))
        {
            renderResponse(facesContext);
            lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
            continueProcess = true;
        }
    }
    
    public void processRemainingExecutePhases(FacesContext facesContext)
    {
        PhaseId lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
        if (lastPhaseId == null)
        {
            processLifecycleExecute(facesContext);
            return;
        }
        else
        {
            boolean continueProcess = false;
            if (PhaseId.RESTORE_VIEW.equals(lastPhaseId))
            {
                applyRequestValues(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.APPLY_REQUEST_VALUES.equals(lastPhaseId))
            {
                processValidations(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.PROCESS_VALIDATIONS.equals(lastPhaseId))
            {
                updateModelValues(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.UPDATE_MODEL_VALUES.equals(lastPhaseId))
            {
                invokeApplication(facesContext);
                continueProcess = true;
            }
        }
    }

    public void processRemainingPhases(FacesContext facesContext)
    {
        PhaseId lastPhaseId = (PhaseId) facesContext.getAttributes().get(LAST_PHASE_PROCESSED);
        if (lastPhaseId == null)
        {
            processLifecycleExecute(facesContext);
            renderResponse(facesContext);
            return;
        }
        else
        {
            boolean continueProcess = false;
            if (PhaseId.RESTORE_VIEW.equals(lastPhaseId))
            {
                applyRequestValues(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.APPLY_REQUEST_VALUES.equals(lastPhaseId))
            {
                processValidations(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.PROCESS_VALIDATIONS.equals(lastPhaseId))
            {
                updateModelValues(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.UPDATE_MODEL_VALUES.equals(lastPhaseId))
            {
                invokeApplication(facesContext);
                continueProcess = true;
            }
            if (continueProcess || PhaseId.INVOKE_APPLICATION.equals(lastPhaseId))
            {
                Integer step = (Integer) facesContext.getAttributes().get(LAST_RENDER_PHASE_STEP);
                if (step == null)
                {
                    renderResponse(facesContext);
                }
                else
                {
                    if (BEFORE_RENDER_STEP == step.intValue())
                    {
                        executeBuildViewCycle(facesContext);
                        executeViewHandlerRender(facesContext);
                        executeAfterRender(facesContext);
                    }
                    else if (BUILD_VIEW_CYCLE_STEP == step.intValue())
                    {
                        executeViewHandlerRender(facesContext);
                        executeAfterRender(facesContext);
                    }
                    else if (VIEWHANDLER_RENDER_STEP == step.intValue())
                    {
                        executeAfterRender(facesContext);
                    }
                }
            }
        }
    }
    
    /**
     * Indicate if annotation scanning should be done over the classpath. 
     * By default it is set to false.
     * 
     * @return
     */
    protected boolean isScanAnnotations()
    {
        TestConfig testConfig = getTestJavaClass().getAnnotation(TestConfig.class);
        if (testConfig != null)
        {
            return testConfig.scanAnnotations();
        }
        return false;
    }
    
    public void executeBeforeRender(FacesContext facesContext)
    {
        if (lifecycle instanceof LifecycleImpl)
        {
            LifecycleImpl lifecycleImpl = (LifecycleImpl) lifecycle;
            
            Object phaseExecutor = null;
            Object phaseManager = null;
            try
            {
                Field renderExecutorField = lifecycleImpl.getClass().getDeclaredField("renderExecutor");
                if (!renderExecutorField.isAccessible())
                {
                    renderExecutorField.setAccessible(true);
                }
                phaseExecutor = renderExecutorField.get(lifecycleImpl);

                if (facesContext.getResponseComplete())
                {
                    return;
                }

                phaseManager = facesContext.getAttributes().get(PHASE_MANAGER_INSTANCE);
                if (phaseManager == null)
                {
                    Method getPhaseListenersMethod = lifecycleImpl.getClass().getDeclaredMethod("getPhaseListeners");
                    if (!getPhaseListenersMethod.isAccessible())
                    {
                        getPhaseListenersMethod.setAccessible(true);
                    }

                    Constructor<?> plmc = PHASE_MANAGER_CLASS.getDeclaredConstructor(
                        new Class[]{Lifecycle.class, FacesContext.class, PhaseListener[].class});
                    if (!plmc.isAccessible())
                    {
                        plmc.setAccessible(true);
                    }
                    phaseManager = plmc.newInstance(lifecycle, facesContext, getPhaseListenersMethod.invoke(
                        lifecycleImpl, null));
                    facesContext.getAttributes().put(PHASE_MANAGER_INSTANCE, phaseManager);
                }
            }
            catch (NoSuchFieldException ex)
            {
                throw new IllegalStateException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (SecurityException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (NoSuchMethodException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InstantiationException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            
            Flash flash = facesContext.getExternalContext().getFlash();
            
            try
            {
                facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
                
                flash.doPrePhaseActions(facesContext);
                
                // let the PhaseExecutor do some pre-phase actions
                
                //renderExecutor.doPrePhaseActions(facesContext);
                Method doPrePhaseActionsMethod = phaseExecutor.getClass().getMethod(
                    "doPrePhaseActions", FacesContext.class);
                if(!(doPrePhaseActionsMethod.isAccessible()))
                {
                    doPrePhaseActionsMethod.setAccessible(true);
                }
                doPrePhaseActionsMethod.invoke(phaseExecutor, facesContext);
                
                //phaseListenerMgr.informPhaseListenersBefore(PhaseId.RENDER_RESPONSE);
                Method informPhaseListenersBeforeMethod = 
                    phaseManager.getClass().getDeclaredMethod("informPhaseListenersBefore", PhaseId.class);
                if(!(informPhaseListenersBeforeMethod.isAccessible()))
                {
                    informPhaseListenersBeforeMethod.setAccessible(true);
                }
                informPhaseListenersBeforeMethod.invoke(phaseManager, PhaseId.RENDER_RESPONSE);
                
                // also possible that one of the listeners completed the response
                if (facesContext.getResponseComplete())
                {
                    return;
                }
                
                //renderExecutor.execute(facesContext);
            }
            
            catch (Throwable e)
            {
                // JSF 2.0: publish the executor's exception (if any).
                ExceptionQueuedEventContext context = new ExceptionQueuedEventContext (
                    facesContext, e, null, PhaseId.RENDER_RESPONSE);
                facesContext.getApplication().publishEvent (facesContext, ExceptionQueuedEvent.class, context);
            }
            
            finally
            {
                /*
                phaseListenerMgr.informPhaseListenersAfter(renderExecutor.getPhase());
                flash.doPostPhaseActions(facesContext);
                
                // publish a field in the application map to indicate
                // that the first request has been processed
                requestProcessed(facesContext);
                */
            }
            
            facesContext.getExceptionHandler().handle();
            

            facesContext.getAttributes().remove(PHASE_MANAGER_INSTANCE);
            
            facesContext.getAttributes().put(LAST_RENDER_PHASE_STEP, BEFORE_RENDER_STEP);
        }
        else
        {
            throw new UnsupportedOperationException("Cannot execute phase on custom lifecycle instances");
        }
    }
    
    public void executeBuildViewCycle(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot root;
        UIViewRoot previousRoot;
        String viewId;
        String newViewId;
        boolean isNotSameRoot;
        int loops = 0;
        int maxLoops = 15;
        
        if (facesContext.getViewRoot() == null)
        {
            throw new ViewNotFoundException("A view is required to execute "+facesContext.getCurrentPhaseId());
        }
        
        try
        {
            // do-while, because the view might change in PreRenderViewEvent-listeners
            do
            {
                root = facesContext.getViewRoot();
                previousRoot = root;
                viewId = root.getViewId();
                
                ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(
                        facesContext, viewId);
                if (vdl != null)
                {
                    vdl.buildView(facesContext, root);
                }
                
                // publish a PreRenderViewEvent: note that the event listeners
                // of this event can change the view, so we have to perform the algorithm 
                // until the viewId does not change when publishing this event.
                application.publishEvent(facesContext, PreRenderViewEvent.class, root);
                
                // was the response marked as complete by an event listener?
                if (facesContext.getResponseComplete())
                {
                    return;
                }

                root = facesContext.getViewRoot();
                
                newViewId = root.getViewId();
                
                isNotSameRoot = !( (newViewId == null ? newViewId == viewId : newViewId.equals(viewId) ) && 
                        previousRoot.equals(root) ); 
                
                loops++;
            }
            while ((newViewId == null && viewId != null) 
                    || (newViewId != null && (!newViewId.equals(viewId) || isNotSameRoot ) ) && loops < maxLoops);
            
            if (loops == maxLoops)
            {
                // PreRenderView reach maxLoops - probably a infinitive recursion:
                boolean production = facesContext.isProjectStage(ProjectStage.Production);
                /*
                Level level = production ? Level.FINE : Level.WARNING;
                if (log.isLoggable(level))
                {
                    log.log(level, "Cicle over buildView-PreRenderViewEvent on RENDER_RESPONSE phase "
                                   + "reaches maximal limit, please check listeners for infinite recursion.");
                }*/
            }
            
            facesContext.getAttributes().put(LAST_RENDER_PHASE_STEP, BUILD_VIEW_CYCLE_STEP);
        }
        catch (IOException e)
        {
            throw new FacesException(e.getMessage(), e);
        }
    }
    
    public void executeViewHandlerRender(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();

        try
        {
            viewHandler.renderView(facesContext, facesContext.getViewRoot());
            
            // log all unhandled FacesMessages, don't swallow them
            // perf: org.apache.myfaces.context.servlet.FacesContextImpl.getMessageList() creates
            // new Collections.unmodifiableList with every invocation->  call it only once
            // and messageList is RandomAccess -> use index based loop
            List<FacesMessage> messageList = facesContext.getMessageList();
            if (!messageList.isEmpty())
            {
                StringBuilder builder = new StringBuilder();
                //boolean shouldLog = false;
                for (int i = 0, size = messageList.size(); i < size; i++)
                {
                    FacesMessage message = messageList.get(i);
                    if (!message.isRendered())
                    {
                        builder.append("\n- ");
                        builder.append(message.getDetail());
                        
                        //shouldLog = true;
                    }
                }
                /*
                if (shouldLog)
                {
                    log.log(Level.WARNING, "There are some unhandled FacesMessages, " +
                            "this means not every FacesMessage had a chance to be rendered.\n" +
                            "These unhandled FacesMessages are: " + builder.toString());
                }*/
            }
            facesContext.getAttributes().put(LAST_RENDER_PHASE_STEP, VIEWHANDLER_RENDER_STEP);
        }
        catch (IOException e)
        {
            throw new FacesException(e.getMessage(), e);
        }
    }
    
    public void executeAfterRender(FacesContext facesContext)
    {
        if (lifecycle instanceof LifecycleImpl)
        {
            LifecycleImpl lifecycleImpl = (LifecycleImpl) lifecycle;
            
            Object phaseExecutor = null;
            Object phaseManager = null;
            Method informPhaseListenersAfterMethod = null;
            try
            {
                Field renderExecutorField = lifecycleImpl.getClass().getDeclaredField("renderExecutor");
                if (!renderExecutorField.isAccessible())
                {
                    renderExecutorField.setAccessible(true);
                }
                phaseExecutor = renderExecutorField.get(lifecycleImpl);
            
                phaseManager = facesContext.getAttributes().get(PHASE_MANAGER_INSTANCE);
                if (phaseManager == null)
                {
                    Method getPhaseListenersMethod = lifecycleImpl.getClass().getDeclaredMethod("getPhaseListeners");
                    if (!getPhaseListenersMethod.isAccessible())
                    {
                        getPhaseListenersMethod.setAccessible(true);
                    }

                    Constructor<?> plmc = PHASE_MANAGER_CLASS.getDeclaredConstructor(
                        new Class[]{Lifecycle.class, FacesContext.class, PhaseListener[].class});
                    if (!plmc.isAccessible())
                    {
                        plmc.setAccessible(true);
                    }
                    phaseManager = plmc.newInstance(lifecycle, facesContext, 
                        getPhaseListenersMethod.invoke(lifecycleImpl, null));
                    facesContext.getAttributes().put(PHASE_MANAGER_INSTANCE, phaseManager);
                }

                //phaseListenerMgr.informPhaseListenersAfter(renderExecutor.getPhase());
                informPhaseListenersAfterMethod = phaseManager.getClass().getDeclaredMethod(
                    "informPhaseListenersAfter", PhaseId.class);
                if(!(informPhaseListenersAfterMethod.isAccessible()))
                {
                    informPhaseListenersAfterMethod.setAccessible(true);
                }
                
                informPhaseListenersAfterMethod.invoke(phaseManager, PhaseId.RENDER_RESPONSE);
            }
            catch (NoSuchFieldException ex)
            {
                throw new IllegalStateException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (SecurityException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (NoSuchMethodException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InstantiationException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            
            Flash flash = facesContext.getExternalContext().getFlash();
            
            flash.doPostPhaseActions(facesContext);
            
            facesContext.getExceptionHandler().handle();

            facesContext.getAttributes().remove(PHASE_MANAGER_INSTANCE);
            
            facesContext.getAttributes().put(LAST_RENDER_PHASE_STEP, AFTER_RENDER_STEP);
            //End render response phase
            facesContext.getAttributes().put(LAST_PHASE_PROCESSED, PhaseId.RENDER_RESPONSE);
        }
        else
        {
            throw new UnsupportedOperationException("Cannot execute phase on custom lifecycle instances");
        }
    }
    
    /**
     * Execute an specified phase, doing some reflection over LifecycleImpl.
     * 
     * @param facesContext
     * @param phase
     */
    protected void executePhase(FacesContext facesContext, PhaseId phase)
    {
        if (lifecycle instanceof LifecycleImpl)
        {
            LifecycleImpl lifecycleImpl = (LifecycleImpl) lifecycle;
            
            int phaseId = phase.equals(PhaseId.RESTORE_VIEW) ? 0 :
                          phase.equals(PhaseId.APPLY_REQUEST_VALUES) ? 1 : 
                          phase.equals(PhaseId.PROCESS_VALIDATIONS) ? 2 :
                          phase.equals(PhaseId.UPDATE_MODEL_VALUES) ? 3 : 
                          phase.equals(PhaseId.INVOKE_APPLICATION) ? 4 : 5 ;
            
            Method executePhaseMethod = null;
            Object phaseManager = null;
            Object phaseExecutor = null;
            try
            {
                if (phaseId < 5)
                {
                    Field lifecycleExecutorsField;
                        lifecycleExecutorsField = lifecycleImpl.getClass().getDeclaredField("lifecycleExecutors");
                        if (!lifecycleExecutorsField.isAccessible())
                        {
                            lifecycleExecutorsField.setAccessible(true);
                        }
                        phaseExecutor = ((Object[])lifecycleExecutorsField.get(lifecycleImpl))[phaseId];
                }
                else
                {
                    Field renderExecutorField = lifecycleImpl.getClass().getDeclaredField("renderExecutor");
                    if (!renderExecutorField.isAccessible())
                    {
                        renderExecutorField.setAccessible(true);
                    }
                    phaseExecutor = renderExecutorField.get(lifecycleImpl);
                }

                phaseManager = facesContext.getAttributes().get(PHASE_MANAGER_INSTANCE);
                if (phaseManager == null)
                {
                    Method getPhaseListenersMethod = lifecycleImpl.getClass().getDeclaredMethod("getPhaseListeners");
                    if (!getPhaseListenersMethod.isAccessible())
                    {
                        getPhaseListenersMethod.setAccessible(true);
                    }

                    Constructor<?> plmc = PHASE_MANAGER_CLASS.getDeclaredConstructor(
                        new Class[]{Lifecycle.class, FacesContext.class, PhaseListener[].class});
                    if (!plmc.isAccessible())
                    {
                        plmc.setAccessible(true);
                    }
                    phaseManager = plmc.newInstance(lifecycle, facesContext, 
                        getPhaseListenersMethod.invoke(lifecycleImpl, null));
                    facesContext.getAttributes().put(PHASE_MANAGER_INSTANCE, phaseManager);
                }

                executePhaseMethod = lifecycleImpl.getClass().getDeclaredMethod("executePhase", new Class[]{
                        FacesContext.class, PHASE_EXECUTOR_CLASS, PHASE_MANAGER_CLASS});
                if (!executePhaseMethod.isAccessible())
                {
                    executePhaseMethod.setAccessible(true);
                }
                
                executePhaseMethod.invoke(lifecycleImpl, facesContext, phaseExecutor, phaseManager);
            }
            catch (NoSuchFieldException ex)
            {
                throw new IllegalStateException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (SecurityException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (NoSuchMethodException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }
            catch (InstantiationException ex)
            {
                throw new UnsupportedOperationException("Cannot get executors from LifecycleImpl", ex);
            }            
            
            if (phase.equals(PhaseId.RENDER_RESPONSE))
            {
                facesContext.getAttributes().remove(PHASE_MANAGER_INSTANCE);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot execute phase on custom lifecycle instances");
        }
    }
    
    public String getRenderedContent(FacesContext facesContext) throws IOException
    {
        MockPrintWriter writer1 = (MockPrintWriter) (((HttpServletResponse)
            facesContext.getExternalContext().getResponse()).getWriter());
        return String.valueOf(writer1.content());
    }

    public MockServletConfig getServletConfig()
    {
        return servletConfig;
    }

    public MockServletContext getServletContext()
    {
        return servletContext;
    }

    public InjectionProvider getInjectionProvider()
    {
        return injectionProvider;
    }

    // ------------------------------------------------------ Instance Variables


    // Thread context class loader saved and restored after each test
    private ClassLoader threadContextClassLoader = null;
    private boolean classLoaderSet = false;
    private Context jndiContext = null;

    // Servlet objects 
    protected MockServletConfig servletConfig = null;
    protected MockServletContext servletContext = null;
    protected MockWebContainer webContainer = null;

    // MyFaces specific objects created by the servlet environment
    protected StartupServletContextListener listener = null;
    protected FacesConfigurationProvider facesConfigurationProvider = null;
    private FacesInitializer facesInitializer = null;
    
    protected FacesContextFactory facesContextFactory = null;
    protected LifecycleFactory lifecycleFactory = null;
    protected Lifecycle lifecycle;

    private static FacesConfig standardFacesConfig;
    private static Map<String, SharedFacesConfiguration> sharedConfiguration =
        new ConcurrentHashMap<String, SharedFacesConfiguration>();
    private SharedFacesConfiguration jsfConfiguration;
    protected TestClass testClass;
    protected Object testInstance;

    //protected WebBeansConfigurationListener owbListener;
    protected InjectionProvider injectionProvider;

    /**
     * @return the facesInitializer
     */
    protected FacesInitializer getFacesInitializer()
    {
        if (facesInitializer == null)
        {
            facesInitializer = createFacesInitializer();
        }
        return facesInitializer;
    }

    /**
     * @param facesInitializer the facesInitializer to set
     */
    protected void setFacesInitializer(FacesInitializer facesInitializer)
    {
        this.facesInitializer = facesInitializer;
    }
    
    protected Class<?> getTestJavaClass()
    {
        return testClass.getJavaClass();
    }
    

    // ------------------------------------------------------ Subclasses

    /**
     * Mock FacesConfigurationProvider that replace the original ViewDeclarationLanguageFactory
     * with a customized one that contains only facelets vdl and cache some FacesConfig that 
     * does not change to reduce the time required to process each test.
     * 
     * @author Leonardo Uribe
     *
     */
    protected class MyFacesMockFacesConfigurationProvider extends DefaultFacesConfigurationProvider
    {
        
        public MyFacesMockFacesConfigurationProvider()
        {
        }
        
        @Override
        public FacesConfig getStandardFacesConfig(ExternalContext ectx)
        {
            if (standardFacesConfig == null)
            {
                FacesConfig sfc = super.getStandardFacesConfig(ectx);
                FactoryImpl factory = (FactoryImpl) sfc.getFactories().get(0);
                // Override the default vdl factory with a mock one that only load
                // facelet views
                factory.getViewDeclarationLanguageFactory().set(0, 
                    MockMyFacesViewDeclarationLanguageFactory.class.getName());
                standardFacesConfig = sfc;
            }
            return standardFacesConfig;
        }

        @Override
        public FacesConfig getAnnotationsFacesConfig(ExternalContext ectx,
                boolean metadataComplete)
        {
            FacesConfig facesConfig = jsfConfiguration.getAnnotationsFacesConfig();
            if (facesConfig == null)
            {
                if (isScanAnnotations())
                {
                    facesConfig = super.getAnnotationsFacesConfig(ectx, metadataComplete); 
                }

                ManagedBeans annoManagedBeans = getTestJavaClass().getAnnotation(ManagedBeans.class);
                if (annoManagedBeans != null)
                {
                    if (facesConfig == null)
                    {
                        facesConfig = new org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl();
                    }
                    for (PageBean annoPageBean : annoManagedBeans.value())
                    {
                        org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl bean = new 
                            org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl();
                        bean.setBeanClass(annoPageBean.clazz().getName());
                        bean.setName(annoPageBean.name() == null ? annoPageBean.clazz().getName() : 
                            annoPageBean.name());
                        bean.setScope(annoPageBean.scope() == null ? "request" : annoPageBean.scope());
                        bean.setEager(Boolean.toString(annoPageBean.eager()));

                        ((org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl)facesConfig).
                            addManagedBean(bean);
                    }
                }

                PageBean annoPageBean = getTestJavaClass().getAnnotation(PageBean.class);
                if (annoPageBean != null)
                {
                    if (facesConfig == null)
                    {
                        facesConfig = new org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl();
                    }
                    org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl bean = new 
                        org.apache.myfaces.config.impl.digester.elements.ManagedBeanImpl();
                    bean.setBeanClass(annoPageBean.clazz().getName());
                    bean.setName(annoPageBean.name() == null ? annoPageBean.clazz().getName() : annoPageBean.name());
                    bean.setScope(annoPageBean.scope() == null ? "request" : annoPageBean.scope());
                    bean.setEager(Boolean.toString(annoPageBean.eager()));

                    ((org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl)facesConfig).
                        addManagedBean(bean);
                }
                jsfConfiguration.setAnnotationFacesConfig(facesConfig);
            }
            return facesConfig;
        }

        @Override
        public List<FacesConfig> getClassloaderFacesConfig(ExternalContext ectx)
        {
            List<FacesConfig> list = jsfConfiguration.getClassloaderFacesConfig();
            if (list == null)
            {
                list = super.getClassloaderFacesConfig(ectx);
                jsfConfiguration.setClassloaderFacesConfig(list);
            }
            return list;
        }

        @Override
        public List<FacesConfig> getFaceletTaglibFacesConfig(ExternalContext externalContext)
        {
            List<FacesConfig> list = jsfConfiguration.getFaceletTaglibFacesConfig();
            if (list == null)
            {
                list = super.getFaceletTaglibFacesConfig(externalContext);
                jsfConfiguration.setFaceletTaglibFacesConfig(list);
            }
            return list;
        }

        @Override
        public List<FacesConfig> getFacesFlowFacesConfig(ExternalContext ectx)
        {
            List<FacesConfig> list = jsfConfiguration.getFacesFlowFacesConfig();
            if (list == null)
            {
                list = super.getFacesFlowFacesConfig(ectx);
                jsfConfiguration.setFacesFlowFacesConfig(list);
            }
            return list;
        }

        @Override
        public FacesConfig getMetaInfServicesFacesConfig(ExternalContext ectx)
        {
            FacesConfig facesConfig = jsfConfiguration.getMetaInfServicesFacesConfig();
            if (facesConfig == null)
            {
                facesConfig = super.getMetaInfServicesFacesConfig(ectx);
            }
            return facesConfig;
        }
        
        @Override
        public List<FacesConfig> getContextSpecifiedFacesConfig(ExternalContext ectx)
        {
            List<FacesConfig> appConfigResources = super.getContextSpecifiedFacesConfig(ectx);
            
            DeclareFacesConfig annoFacesConfig = getTestJavaClass().getAnnotation(DeclareFacesConfig.class);
            if (annoFacesConfig != null)
            {
                Logger log = Logger.getLogger(getTestJavaClass().getName());
                try
                {
                    for (String systemId : annoFacesConfig.value())
                    {
                        if (MyfacesConfig.getCurrentInstance(ectx).isValidateXML())
                        {
                            URL url = ectx.getResource(systemId);
                            if (url != null)
                            {
                                validateFacesConfig(ectx, url);
                            }
                        }   
                        InputStream stream = ectx.getResourceAsStream(systemId);
                        if (stream == null)
                        {
                            
                            log.severe("Faces config resource " + systemId + " not found");
                            continue;
                        }
            
                        if (log.isLoggable(Level.INFO))
                        {
                            log.info("Reading config " + systemId);
                        }
                        appConfigResources.add(getUnmarshaller(ectx).getFacesConfig(stream, systemId));
                        //getDispenser().feed(getUnmarshaller().getFacesConfig(stream, systemId));
                        stream.close();
    
                    }
                }
                catch (Throwable e)
                {
                    throw new FacesException(e);
                }
            }
            return appConfigResources;
        }
    }
    
    private void validateFacesConfig(ExternalContext ectx, URL url) throws IOException, SAXException
    {
        String version = ConfigFilesXmlValidationUtils.getFacesConfigVersion(url);
        if ("1.2".equals(version) || "2.0".equals(version) || "2.1".equals(version))
        {
            ConfigFilesXmlValidationUtils.validateFacesConfigFile(url, ectx, version);
        }
    }
    
    protected class JUnitFacesInitializer extends AbstractFacesInitializer
    {
        private final AbstractJsfTestContainer testCase;
        
        public JUnitFacesInitializer(AbstractJsfTestContainer testCase)
        {
            this.testCase = testCase;
        }
        
        @Override
        protected void initContainerIntegration(ServletContext servletContext,
                ExternalContext externalContext)
        {
            if (servletContext.getInitParameter("org.apache.myfaces.spi.InjectionProvider") == null)
            {
                if (ExternalSpecifications.isCDIAvailable(externalContext))
                {
                    ((MockServletContext)servletContext).addInitParameter("org.apache.myfaces.spi.InjectionProvider", 
                        CDIAnnotationDelegateInjectionProvider.class.getName());
                }
                else
                {
                    ((MockServletContext)servletContext).addInitParameter("org.apache.myfaces.spi.InjectionProvider", 
                        NoInjectionAnnotationInjectionProvider.class.getName());
                }
            }
            
            ExpressionFactory expressionFactory = createExpressionFactory();

            RuntimeConfig runtimeConfig = buildConfiguration(servletContext, externalContext, expressionFactory);
        }

        public AbstractJsfTestContainer getTestCase()
        {
            return testCase;
        }
        
        private static final String CDI_SERVLET_CONTEXT_BEAN_MANAGER_ATTRIBUTE = 
            "javax.enterprise.inject.spi.BeanManager";

        protected void initCDIIntegration(
                ServletContext servletContext, ExternalContext externalContext)
        {
            // Lookup bean manager and put it into an application scope attribute to 
            // access it later. Remember the trick here is do not call any CDI api 
            // directly, so if no CDI api is on the classpath no exception will be thrown.

            // Try with servlet context
            Object beanManager = servletContext.getAttribute(
                CDI_SERVLET_CONTEXT_BEAN_MANAGER_ATTRIBUTE);
            if (beanManager == null)
            {
                beanManager = lookupBeanManagerFromJndi();
            }
            if (beanManager != null)
            {
                externalContext.getApplicationMap().put(CDI_BEAN_MANAGER_INSTANCE,
                    beanManager);
            }
        }

        /**
         * Try to lookup the CDI BeanManager from JNDI.
         * We do all this via reflection to not blow up if CDI is not available.
         */
        private Object lookupBeanManagerFromJndi()
        {
            Object beanManager = null;
            // Use reflection to avoid restricted API in GAE
            Class icclazz = null;
            Method lookupMethod = null;
            try
            {
                icclazz = ClassUtils.simpleClassForName("javax.naming.InitialContext");
                if (icclazz != null)
                {
                    lookupMethod = icclazz.getMethod("doLookup", String.class);
                }
            }
            catch (Throwable t)
            {
                //
            }
            if (lookupMethod != null)
            {
                // Try with JNDI
                try
                {
                    // in an application server
                    //beanManager = InitialContext.doLookup("java:comp/BeanManager");
                    beanManager = lookupMethod.invoke(icclazz, "java:comp/BeanManager");
                }
                catch (Exception e)
                {
                    // silently ignore
                }
                catch (NoClassDefFoundError e)
                {
                    //On Google App Engine, javax.naming.Context is a restricted class.
                    //In that case, NoClassDefFoundError is thrown. stageName needs to be configured
                    //below by context parameter.
                }

                if (beanManager == null)
                {
                    try
                    {
                        // in a servlet container
                        //beanManager = InitialContext.doLookup("java:comp/env/BeanManager");
                        beanManager = lookupMethod.invoke(icclazz, "java:comp/env/BeanManager");
                    }
                    catch (Exception e)
                    {
                        // silently ignore
                    }
                    catch (NoClassDefFoundError e)
                    {
                        //On Google App Engine, javax.naming.Context is a restricted class.
                        //In that case, NoClassDefFoundError is thrown. stageName needs to be configured
                        //below by context parameter.
                    }
                }
            }

            return beanManager;
        }
    }
    
    protected static class SharedFacesConfiguration
    {
        private List<FacesConfig> classloaderFacesConfig;
        private FacesConfig annotationFacesConfig;
        private List<FacesConfig> faceletTaglibFacesConfig;
        private List<FacesConfig> facesFlowFacesConfig;
        private FacesConfig metaInfServicesFacesConfig;
        private List<FacesConfig> contextSpecifiedFacesConfig;

        /**
         * @return the annotationFacesConfig
         */
        public FacesConfig getAnnotationsFacesConfig()
        {
            return annotationFacesConfig;
        }

        /**
         * @param annotationFacesConfig the annotationFacesConfig to set
         */
        public void setAnnotationFacesConfig(FacesConfig annotationFacesConfig)
        {
            this.annotationFacesConfig = annotationFacesConfig;
        }

        /**
         * @return the annotationFacesConfig
         */
        public FacesConfig getAnnotationFacesConfig()
        {
            return annotationFacesConfig;
        }

        /**
         * @return the faceletTaglibFacesConfig
         */
        public List<FacesConfig> getFaceletTaglibFacesConfig()
        {
            return faceletTaglibFacesConfig;
        }

        /**
         * @param faceletTaglibFacesConfig the faceletTaglibFacesConfig to set
         */
        public void setFaceletTaglibFacesConfig(List<FacesConfig> faceletTaglibFacesConfig)
        {
            this.faceletTaglibFacesConfig = faceletTaglibFacesConfig;
        }

        /**
         * @return the facesFlowFacesConfig
         */
        public List<FacesConfig> getFacesFlowFacesConfig()
        {
            return facesFlowFacesConfig;
        }

        /**
         * @param facesFlowFacesConfig the facesFlowFacesConfig to set
         */
        public void setFacesFlowFacesConfig(List<FacesConfig> facesFlowFacesConfig)
        {
            this.facesFlowFacesConfig = facesFlowFacesConfig;
        }

        /**
         * @return the metaInfServicesFacesConfig
         */
        public FacesConfig getMetaInfServicesFacesConfig()
        {
            return metaInfServicesFacesConfig;
        }

        /**
         * @param metaInfServicesFacesConfig the metaInfServicesFacesConfig to set
         */
        public void setMetaInfServicesFacesConfig(FacesConfig metaInfServicesFacesConfig)
        {
            this.metaInfServicesFacesConfig = metaInfServicesFacesConfig;
        }

        /**
         * @return the contextSpecifiedFacesConfig
         */
        public List<FacesConfig> getContextSpecifiedFacesConfig()
        {
            return contextSpecifiedFacesConfig;
        }

        /**
         * @param contextSpecifiedFacesConfig the contextSpecifiedFacesConfig to set
         */
        public void setContextSpecifiedFacesConfig(List<FacesConfig> contextSpecifiedFacesConfig)
        {
            this.contextSpecifiedFacesConfig = contextSpecifiedFacesConfig;
        }

        /**
         * @return the classloaderFacesConfigList
         */
        public List<FacesConfig> getClassloaderFacesConfig()
        {
            return classloaderFacesConfig;
        }

        /**
         * @param classloaderFacesConfigList the classloaderFacesConfigList to set
         */
        public void setClassloaderFacesConfig(List<FacesConfig> classloaderFacesConfigList)
        {
            this.classloaderFacesConfig = classloaderFacesConfigList;
        }
    }
}
