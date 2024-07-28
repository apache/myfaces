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
package org.apache.myfaces.test.core;

import jakarta.el.ExpressionFactory;
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
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.myfaces.config.ConfigFilesXmlValidationUtils;
import org.apache.myfaces.config.DefaultFacesConfigurationProvider;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.impl.element.FactoryImpl;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.lifecycle.LifecycleImpl;
import org.apache.myfaces.lifecycle.PhaseExecutor;
import org.apache.myfaces.lifecycle.PhaseListenerManager;
import org.apache.myfaces.lifecycle.ViewNotFoundException;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.impl.DefaultFacesConfigurationProviderFactory;
import org.apache.myfaces.spi.impl.NoInjectionAnnotationInjectionProvider;
import org.apache.myfaces.test.core.annotation.DeclareFacesConfig;
import org.apache.myfaces.test.core.mock.DefaultContext;
import org.apache.myfaces.test.core.mock.MockInitialContextFactory;
import org.apache.myfaces.test.core.mock.MockMyFacesViewDeclarationLanguageFactory;
import org.apache.myfaces.test.el.MockExpressionFactory;
import org.apache.myfaces.test.mock.MockPrintWriter;
import org.apache.myfaces.test.mock.MockServletConfig;
import org.apache.myfaces.test.mock.MockServletContext;
import org.apache.myfaces.test.mock.MockWebContainer;
import org.apache.myfaces.webapp.FacesInitializer;
import org.apache.myfaces.webapp.FacesInitializerImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.xml.sax.SAXException;

import javax.naming.Context;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Abstract JUnit test case base class, which sets up MyFaces Core environment
 * using mock object for the outer servlet environment.</p>
 * <p>Since jsp engine is not bundled with MyFaces, this configuration is able to 
 * handle facelet pages only.</p>
 * 
 * @author Leonardo Uribe
 *
 */
public abstract class AbstractMyFacesTestCase
{
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
    public AbstractMyFacesTestCase()
    {
    }

    // ---------------------------------------------------- Overall Test Methods

    /**
     * <p>Set up instance variables required by this test case.</p>
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        // Set up a new thread context class loader
        setUpClassloader();

        jsfConfiguration = sharedConfiguration.get(getTestJavaClass().getName());
        if (jsfConfiguration == null)
        {
            jsfConfiguration = new SharedFacesConfiguration();
        }

        //JNDI
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
        jndiContext = new DefaultContext();
        MockInitialContextFactory.setCurrentContext(jndiContext);

        // Set up Servlet API Objects
        setUpServletObjects();

        // Set up Faces API Objects
        FactoryFinder.releaseFactories();

        setUpServletListeners();
        
        webContainer.contextInitialized(new ServletContextEvent(servletContext));
        
        setUpFacesServlet();
        
        sharedConfiguration.put(getTestJavaClass().getName(), jsfConfiguration);
    }
    
    /**
     * Set up the thread context classloader. Faces uses the this classloader
     * in order to find related factory classes and other resources, but in
     * some selected cases, the default classloader cannot be properly set.
     * 
     * @throws Exception 
     */
    protected void setUpClassloader() throws Exception
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
     * @throws Exception
     */
    protected void setUpServletObjects() throws Exception
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
     * @throws Exception
     */
    protected void setUpWebConfigParams() throws Exception
    {
        servletContext.addInitParameter("org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE", "true");
        servletContext.addInitParameter("jakarta.faces.PROJECT_STAGE", "UnitTest");
        servletContext.addInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "true");
        servletContext.addInitParameter(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME,"-1");
        servletContext.addInitParameter("org.apache.myfaces.spi.InjectionProvider", 
            NoInjectionAnnotationInjectionProvider.class.getName());
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
        return this.getClass().getName().substring(0,
                this.getClass().getName().lastIndexOf('.')).replace('.', '/')
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
     * @throws Exception
     */
    protected void setUpServletListeners() throws Exception
    {
        setUpMyFaces();
    }

    protected FacesConfigurationProvider createFacesConfigurationProvider()
    {
        return new MyFacesMockFacesConfigurationProvider(this); 
    }
    
    protected FacesInitializerImpl createFacesInitializer()
    {
        return new JUnitNoCDIFacesInitializer(this);
    }
    
    protected void setUpMyFaces() throws Exception
    {
        if (facesConfigurationProvider == null)
        {
            facesConfigurationProvider = createFacesConfigurationProvider();
        }
        servletContext.setAttribute(
                DefaultFacesConfigurationProviderFactory.FACES_CONFIGURATION_PROVIDER_INSTANCE_KEY, 
                facesConfigurationProvider);
        listener = new TestStartupServletContextListener(createFacesInitializer());
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

    protected void setUpFacesServlet() throws Exception
    {
        lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        facesContextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());
    }
    
    protected void tearDownFacesServlet() throws Exception
    {
        lifecycleFactory = null;
        facesContextFactory = null;
    }
    
    protected void tearDownServlets() throws Exception
    {
        tearDownFacesServlet();
    }
    
    protected void tearDownServletListeners() throws Exception
    {
        tearDownMyFaces();
    }

    @AfterEach
    public void tearDown() throws Exception
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
    
    protected void tearDownClassloader() throws Exception
    {
        if (classLoaderSet)
        {
            Thread.currentThread().setContextClassLoader(threadContextClassLoader);
            threadContextClassLoader = null;
            classLoaderSet = false;
        }
    }    
    
    @AfterAll
    public static void tearDownClass()
    {
        standardFacesConfig = null;
        sharedConfiguration.clear();
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
                    if (BEFORE_RENDER_STEP == step)
                    {
                        executeBuildViewCycle(facesContext);
                        executeViewHandlerRender(facesContext);
                        executeAfterRender(facesContext);
                    }
                    else if (BUILD_VIEW_CYCLE_STEP == step)
                    {
                        executeViewHandlerRender(facesContext);
                        executeAfterRender(facesContext);
                    }
                    else if (VIEWHANDLER_RENDER_STEP == step)
                    {
                        executeAfterRender(facesContext);
                    }
                }
            }
        }
    }

    public void executeBeforeRender(FacesContext facesContext)
    {
        if (facesContext.getResponseComplete())
        {
            return;
        }

        if (lifecycle instanceof LifecycleImpl lifecycleImpl)
        {
            PhaseListenerManager phaseManager = (PhaseListenerManager) facesContext.getAttributes().get(PHASE_MANAGER_INSTANCE);
            if (phaseManager == null)
            {
                phaseManager = new PhaseListenerManager(lifecycle, facesContext, lifecycleImpl.getPhaseListeners());
                facesContext.getAttributes().put(PHASE_MANAGER_INSTANCE, phaseManager);
            }

            Flash flash = facesContext.getExternalContext().getFlash();
            try
            {
                facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
                
                flash.doPrePhaseActions(facesContext);

                PhaseExecutor phaseExecutor = lifecycleImpl.getPhaseExecutor(PhaseId.RENDER_RESPONSE);

                // let the PhaseExecutor do some pre-phase actions
                phaseExecutor.doPrePhaseActions(facesContext);

                phaseManager.informPhaseListenersBefore(PhaseId.RENDER_RESPONSE);
                
                // also possible that one of the listeners completed the response
                if (facesContext.getResponseComplete())
                {
                    return;
                }
                
                //renderExecutor.execute(facesContext);
            }
            
            catch (Throwable e)
            {
                // Faces 2.0: publish the executor's exception (if any).
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
        if (lifecycle instanceof LifecycleImpl lifecycleImpl)
        {
            PhaseListenerManager phaseManager;
            try
            {
                phaseManager = (PhaseListenerManager) facesContext.getAttributes().get(PHASE_MANAGER_INSTANCE);
                if (phaseManager == null)
                {
                    phaseManager = new PhaseListenerManager(lifecycle, facesContext, lifecycleImpl.getPhaseListeners());
                    facesContext.getAttributes().put(PHASE_MANAGER_INSTANCE, phaseManager);
                }

                phaseManager.informPhaseListenersAfter(PhaseId.RENDER_RESPONSE);
            }
            catch (SecurityException | IllegalArgumentException ex)
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
        if (lifecycle instanceof LifecycleImpl lifecycleImpl)
        {
            lifecycleImpl.executePhase(facesContext, phase);
        }
        else
        {
            throw new UnsupportedOperationException("Cannot execute phase on custom lifecycle instances");
        }
    }

    protected String getRenderedContent(FacesContext facesContext) throws IOException
    {
        MockPrintWriter writer1 = (MockPrintWriter) 
            (((HttpServletResponse) facesContext.getExternalContext().getResponse()).getWriter());
        return String.valueOf(writer1.content());
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
    protected TestStartupServletContextListener listener = null;
    protected FacesConfigurationProvider facesConfigurationProvider = null;
    
    protected FacesContextFactory facesContextFactory = null;
    protected LifecycleFactory lifecycleFactory = null;
    protected Lifecycle lifecycle;

    private static FacesConfig standardFacesConfig;
    private static Map<String, SharedFacesConfiguration> sharedConfiguration = new ConcurrentHashMap<>();
    private SharedFacesConfiguration jsfConfiguration;

    protected Class<?> getTestJavaClass()
    {
        return this.getClass();
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
        private AbstractMyFacesTestCase testCase;
        
        public MyFacesMockFacesConfigurationProvider(AbstractMyFacesTestCase testCase)
        {
            this.testCase = testCase;
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
        public FacesConfig getAnnotationsFacesConfig(ExternalContext ectx, boolean metadataComplete)
        {
            FacesConfig facesConfig = jsfConfiguration.annotationFacesConfig;
            if (facesConfig == null)
            {
                facesConfig = super.getAnnotationsFacesConfig(ectx, metadataComplete);
                jsfConfiguration.annotationFacesConfig = facesConfig;
            }
            return facesConfig;
        }

        @Override
        public List<FacesConfig> getClassloaderFacesConfig(ExternalContext ectx)
        {
            List<FacesConfig> list = jsfConfiguration.classloaderFacesConfig;
            if (list == null)
            {
                list = super.getClassloaderFacesConfig(ectx);
                jsfConfiguration.classloaderFacesConfig = list;
            }
            return list;
        }

        @Override
        public List<FacesConfig> getFaceletTaglibFacesConfig(ExternalContext externalContext)
        {
            List<FacesConfig> list = jsfConfiguration.faceletTaglibFacesConfig;
            if (list == null)
            {
                list = super.getFaceletTaglibFacesConfig(externalContext);
                jsfConfiguration.faceletTaglibFacesConfig = list;
            }
            return list;
        }

        @Override
        public List<FacesConfig> getFacesFlowFacesConfig(ExternalContext ectx)
        {
            List<FacesConfig> list = jsfConfiguration.facesFlowFacesConfig;
            if (list == null)
            {
                list = super.getFacesFlowFacesConfig(ectx);
                jsfConfiguration.facesFlowFacesConfig = list;
            }
            return list;
        }

        @Override
        public FacesConfig getMetaInfServicesFacesConfig(ExternalContext ectx)
        {
            FacesConfig facesConfig = jsfConfiguration.metaInfServicesFacesConfig;
            if (facesConfig == null)
            {
                facesConfig = super.getMetaInfServicesFacesConfig(ectx);
                jsfConfiguration.metaInfServicesFacesConfig = facesConfig;
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
    
    protected class JUnitFacesInitializer extends FacesInitializerImpl
    {
        private final AbstractMyFacesTestCase testCase;
        
        public JUnitFacesInitializer(AbstractMyFacesTestCase testCase)
        {
            this.testCase = testCase;
        }
        
        @Override
        protected void initContainerIntegration(ServletContext servletContext,
                ExternalContext externalContext)
        {
            ExpressionFactory expressionFactory = createExpressionFactory();

            RuntimeConfig runtimeConfig = buildConfiguration(servletContext, externalContext, expressionFactory);
        }

        public AbstractMyFacesTestCase getTestCase()
        {
            return testCase;
        }

    }
    
    protected class JUnitNoCDIFacesInitializer extends JUnitFacesInitializer
    {

        public JUnitNoCDIFacesInitializer(AbstractMyFacesTestCase testCase)
        {
            super(testCase);
        }

        @Override
        protected void initCDIIntegration(ServletContext servletContext, ExternalContext externalContext)
        {
            //super.initCDIIntegration(servletContext, externalContext);
        }
        
    }

    protected static class SharedFacesConfiguration
    {
        protected List<FacesConfig> classloaderFacesConfig;
        protected FacesConfig annotationFacesConfig;
        protected List<FacesConfig> faceletTaglibFacesConfig;
        protected List<FacesConfig> facesFlowFacesConfig;
        protected FacesConfig metaInfServicesFacesConfig;
        protected List<FacesConfig> contextSpecifiedFacesConfig;
    }

    protected static class TestStartupServletContextListener implements ServletContextListener
    {
        private FacesInitializer facesInitializer;

        public TestStartupServletContextListener(FacesInitializer facesInitializer)
        {
            this.facesInitializer = facesInitializer;
        }

        @Override
        public void contextInitialized(ServletContextEvent event)
        {
            if (facesInitializer != null)
            {
                facesInitializer.initFaces(event.getServletContext());
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent event)
        {
            if (facesInitializer != null)
            {
                facesInitializer.destroyFaces(event.getServletContext());
            }
        }
    }

}
