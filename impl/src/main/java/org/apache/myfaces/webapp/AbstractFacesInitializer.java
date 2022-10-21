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
package org.apache.myfaces.webapp;

import org.apache.myfaces.config.FacesConfigValidator;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.context.ExceptionHandlerImpl;
import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.util.WebConfigParamUtils;
import org.apache.myfaces.cdi.util.BeanEntry;
import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderException;
import org.apache.myfaces.spi.InjectionProviderFactory;
import org.apache.myfaces.spi.ViewScopeProvider;
import org.apache.myfaces.spi.ViewScopeProviderFactory;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.view.facelets.tag.MetaRulesetImpl;

import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.ViewVisitOption;
import javax.faces.push.PushContext;
import javax.servlet.ServletRegistration;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension;
import org.apache.myfaces.push.EndpointImpl;
import org.apache.myfaces.push.WebsocketConfigurator;
import org.apache.myfaces.push.WebsocketFacesInit;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.spi.FacesFlowProvider;
import org.apache.myfaces.spi.FacesFlowProviderFactory;
import org.apache.myfaces.spi.ServiceProviderFinder;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.apache.myfaces.util.lang.StringUtils;

/**
 * Performs common initialization tasks.
 */
public abstract class AbstractFacesInitializer implements FacesInitializer
{
    private static final Logger log = Logger.getLogger(AbstractFacesInitializer.class.getName());

    public static final String CDI_BEAN_MANAGER_INSTANCE = "oam.cdi.BEAN_MANAGER_INSTANCE";

    private static final String CDI_SERVLET_CONTEXT_BEAN_MANAGER_ATTRIBUTE = 
        "javax.enterprise.inject.spi.BeanManager";

    public static final String INJECTED_BEAN_STORAGE_KEY = "org.apache.myfaces.spi.BEAN_ENTRY_STORAGE";

    /**
     * Performs all necessary initialization tasks like configuring this JSF
     * application.
     * 
     * @param servletContext The current {@link ServletContext}
     */
    @Override
    public void initFaces(ServletContext servletContext)
    {
        try
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Initializing MyFaces");
            }

            // Some parts of the following configuration tasks have been implemented 
            // by using an ExternalContext. However, that's no problem as long as no 
            // one tries to call methods depending on either the ServletRequest or 
            // the ServletResponse.
            // JSF 2.0: FacesInitializer now has some new methods to
            // use proper startup FacesContext and ExternalContext instances.
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            // Setup ServiceProviderFinder
            ServiceProviderFinder spf = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext);
            Map<String, List<String>> spfConfig = spf.calculateKnownServiceProviderMapInfo(
                externalContext, ServiceProviderFinder.KNOWN_SERVICES);
            if (spfConfig != null)
            {
                spf.initKnownServiceProviderMapInfo(externalContext, spfConfig);
            }

            if (!WebConfigParamUtils.getBooleanInitParameter(externalContext,
                    MyfacesConfig.INITIALIZE_ALWAYS_STANDALONE, false))
            {
                // check to see if the FacesServlet was found by MyFacesContainerInitializer
                Boolean mappingAdded = (Boolean) servletContext.getAttribute(
                    MyFacesContainerInitializer.FACES_SERVLET_FOUND);

                if (mappingAdded == null || !mappingAdded)
                {
                    // check if the FacesServlet has been added dynamically
                    // in a Servlet 3.0 environment by MyFacesContainerInitializer
                    mappingAdded = (Boolean) servletContext.getAttribute(
                        MyFacesContainerInitializer.FACES_SERVLET_ADDED_ATTRIBUTE);

                    if (mappingAdded == null || !mappingAdded)
                    {
                        if (log.isLoggable(Level.WARNING))
                        {
                            log.warning("No mappings of FacesServlet found. Abort initializing MyFaces.");
                        }
                        return;
                    }
                }
            }

            initCDIIntegration(servletContext, externalContext);

            initContainerIntegration(servletContext, externalContext);

            // log environment integrations
            ExternalSpecifications.isBeanValidationAvailable();
            ExternalSpecifications.isCDIAvailable(externalContext);
            ExternalSpecifications.isEL3Available();
            ExternalSpecifications.isServlet4Available();
            
            ViewScopeProviderFactory viewScopeProviderFactory =
                    ViewScopeProviderFactory.getViewScopeHandlerFactory(externalContext);
            ViewScopeProvider viewScopeProvider = viewScopeProviderFactory.getViewScopeHandler(externalContext);

            FacesFlowProviderFactory facesFlowProviderFactory =
                    FacesFlowProviderFactory.getFacesFlowProviderFactory(externalContext);
            FacesFlowProvider facesFlowProvider = facesFlowProviderFactory.getFacesFlowProvider(externalContext);
            
            MyFacesHttpSessionListener listener = (MyFacesHttpSessionListener) externalContext.getApplicationMap()
                    .get(MyFacesHttpSessionListener.APPLICATION_MAP_KEY);
            if (listener != null)
            {
                listener.setViewScopeProvider(viewScopeProvider);
                listener.setFacesFlowProvider(facesFlowProvider);
            }
            
            String useEncryption = servletContext.getInitParameter(StateUtils.USE_ENCRYPTION);
            if ("false".equals(useEncryption))
            {
                log.warning(StateUtils.USE_ENCRYPTION + " is set to false. " 
                        + "This is unsecure and should only be used for local or intranet applications!");
            }
            else
            {
                StateUtils.initSecret(servletContext);
            }

            _dispatchApplicationEvent(servletContext, PostConstructApplicationEvent.class);
            
            initWebsocketIntegration(servletContext, externalContext);

            if (log.isLoggable(Level.FINEST))
            {
                log.finest("ServletContext initialized");
            }

            WebConfigParamsLogger.logWebContextParams(facesContext);

            //Start ViewPoolProcessor if necessary
            ViewPoolProcessor.initialize(facesContext);

            MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext.getExternalContext());
            if (config.isAutomaticExtensionlessMapping())
            {
                initAutomaticExtensionlessMapping(facesContext, servletContext);
            }

            // publish resourceBundleControl to applicationMap, to make it available to the API
            ResourceBundle.Control resourceBundleControl = config.getResourceBundleControl();
            if (resourceBundleControl != null)
            {
                facesContext.getExternalContext().getApplicationMap().put(
                        MyfacesConfig.RESOURCE_BUNDLE_CONTROL, resourceBundleControl);
            }

            // print out a very prominent log message if the project stage is != Production
            if (!facesContext.isProjectStage(ProjectStage.Production)
                    && !facesContext.isProjectStage(ProjectStage.UnitTest))
            {
                ProjectStage projectStage = facesContext.getApplication().getProjectStage();
                StringBuilder message = new StringBuilder("\n\n");
                message.append("********************************************************************\n");
                message.append("*** WARNING: Apache MyFaces Core is running in ");
                message.append(projectStage.name().toUpperCase());        
                message.append(" mode.");
                int length = projectStage.name().length();
                for (int i = 0; i < 11 - length; i++)
                {
                    message.append(' ');
                }
                message.append(" ***\n");
                message.append("***                                            ");
                for (int i = 0; i < length; i++)
                {
                    message.append('^');
                }
                for (int i = 0; i < 18 - length; i++)
                {
                    message.append(' ');
                }
                message.append("***\n");
                message.append("*** Do NOT deploy to your live server(s) without changing this.  ***\n");
                message.append("*** See Application#getProjectStage() for more information.      ***\n");
                message.append("********************************************************************\n");
                message.append("\n");
                log.log(Level.WARNING, message.toString());
            }

            cleanupAfterStartup(facesContext);
            
            servletContext.setAttribute(MyFacesContainerInitializer.INITIALIZED, Boolean.TRUE);
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, "An error occured while initializing MyFaces: "
                      + ex.getMessage(), ex);
        }
    }

    protected void cleanupAfterStartup(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        
        if (ExternalSpecifications.isCDIAvailable(externalContext))
        {
            BeanManager beanManager = CDIUtils.getBeanManager(externalContext);
            CdiAnnotationProviderExtension extension = CDIUtils.getOptional(beanManager,
                    CdiAnnotationProviderExtension.class);
            if (extension != null)
            {
                extension.release();
            }
        }
    }
    
    /**
     * Eventually we can use our plugin infrastructure for this as well
     * it would be a cleaner interception point than the base class
     * but for now this position is valid as well
     * <p/>
     * Note we add it for now here because the application factory object
     * leaves no possibility to have a destroy interceptor
     * and applications are per web application singletons
     * Note if this does not work out
     * move the event handler into the application factory
     *
     * @param servletContext The current {@link ServletContext}
     * @param eventClass     the class to be passed down into the dispatching
     *                       code
     */
    private void _dispatchApplicationEvent(ServletContext servletContext, Class<? extends SystemEvent> eventClass)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        application.publishEvent(facesContext, eventClass, Application.class, application);
    }
    
    /**
     * Cleans up all remaining resources (well, theoretically).
     * 
     * @param servletContext The current {@link ServletContext}
     */
    @Override
    public void destroyFaces(ServletContext servletContext)
    {
        if (!Boolean.TRUE.equals(servletContext.getAttribute(MyFacesContainerInitializer.INITIALIZED)))
        {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        _dispatchApplicationEvent(servletContext, PreDestroyApplicationEvent.class);

        _callPreDestroyOnInjectedJSFArtifacts(facesContext);
        
        // clear the cache of MetaRulesetImpl in order to prevent a memory leak
        MetaRulesetImpl.clearMetadataTargetCache();
        
        if (facesContext.getExternalContext().getApplicationMap().containsKey("org.apache.myfaces.push"))
        {
            WebsocketFacesInit.destroy(facesContext.getExternalContext());
        }
        
        // clear UIViewParameter default renderer map
        try
        {
            Class<?> c = ClassUtils.classForName("javax.faces.component.UIViewParameter");
            Method m = c.getDeclaredMethod("releaseRenderer");
            m.setAccessible(true);
            m.invoke(null);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            log.log(Level.SEVERE, e.getMessage(), e);
        }


        // TODO is it possible to make a real cleanup?
    }

    /**
     * Configures this JSF application. It's required that every
     * FacesInitializer (i.e. every subclass) calls this method during
     * initialization.
     *
     * @param servletContext    the current ServletContext
     * @param externalContext   the current ExternalContext
     * @param expressionFactory the ExpressionFactory to use
     * @return the current runtime configuration
     */
    protected RuntimeConfig buildConfiguration(ServletContext servletContext,
                                               ExternalContext externalContext, ExpressionFactory expressionFactory)
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        runtimeConfig.setExpressionFactory(expressionFactory);

        // And configure everything
        new FacesConfigurator(externalContext).configure();

        validateFacesConfig(servletContext, externalContext);

        return runtimeConfig;
    }

    protected void validateFacesConfig(ServletContext servletContext, ExternalContext externalContext)
    {
        String validate = servletContext.getInitParameter(MyfacesConfig.VALIDATE);
        if ("true".equals(validate) && log.isLoggable(Level.WARNING)) // the default value is false
        {
            List<String> warnings = FacesConfigValidator.validate(externalContext);

            for (String warning : warnings)
            {
                log.warning(warning);
            }
        }
    }

    /**
     * Try to load user-definied ExpressionFactory. Returns <code>null</code>,
     * if no custom ExpressionFactory was specified.
     *
     * @param externalContext the current ExternalContext
     * @return User-specified ExpressionFactory, or
     *         <code>null</code>, if no no custom implementation was specified
     */
    protected static ExpressionFactory getUserDefinedExpressionFactory(ExternalContext externalContext)
    {
        String expressionFactoryClassName
                = MyfacesConfig.getCurrentInstance(externalContext).getExpressionFactory();
        if (StringUtils.isNotBlank(expressionFactoryClassName))
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Attempting to load the ExpressionFactory implementation "
                        + "you've specified: '" + expressionFactoryClassName + "'.");
            }

            return loadExpressionFactory(expressionFactoryClassName);
        }

        return null;
    }

    /**
     * Loads and instantiates the given ExpressionFactory implementation.
     *
     * @param expressionFactoryClassName the class name of the ExpressionFactory implementation
     * @return the newly created ExpressionFactory implementation, or
     *         <code>null</code>, if an error occurred
     */
    protected static ExpressionFactory loadExpressionFactory(String expressionFactoryClassName)
    {
        return loadExpressionFactory(expressionFactoryClassName, true);
    }
    
    protected static ExpressionFactory loadExpressionFactory(String expressionFactoryClassName, boolean logMissing)
    {
        try
        {
            ClassLoader cl = ClassUtils.getContextClassLoader();
            if (cl == null)
            {
                cl = AbstractFacesInitializer.class.getClassLoader();
            }

            Class<?> expressionFactoryClass = cl.loadClass(expressionFactoryClassName);
            return (ExpressionFactory) expressionFactoryClass.newInstance();
        }
        catch (Exception ex)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "An error occured while instantiating a new ExpressionFactory. "
                        + "Attempted to load class '" + expressionFactoryClassName + "'.", ex);
            }
        }

        return null;
    }

    @Override
    public FacesContext initStartupFacesContext(ServletContext servletContext)
    {
        // We cannot use FacesContextFactory, because it is necessary to initialize 
        // before Application and RenderKit factories, so we should use different object. 
        return _createFacesContext(servletContext, true);
    }

    @Override
    public void destroyStartupFacesContext(FacesContext facesContext)
    {
        _releaseFacesContext(facesContext);
    }

    @Override
    public FacesContext initShutdownFacesContext(ServletContext servletContext)
    {
        return _createFacesContext(servletContext, false);
    }

    @Override
    public void destroyShutdownFacesContext(FacesContext facesContext)
    {
        _releaseFacesContext(facesContext);
    }

    private FacesContext _createFacesContext(ServletContext servletContext, boolean startup)
    {
        ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, startup);
        ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
        FacesContext facesContext = new StartupFacesContextImpl(externalContext,
                externalContext, exceptionHandler, startup);

        // If getViewRoot() is called during application startup or shutdown,
        // it should return a new UIViewRoot with its locale set to Locale.getDefault().
        UIViewRoot startupViewRoot = new UIViewRoot();
        startupViewRoot.setLocale(Locale.getDefault());
        facesContext.setViewRoot(startupViewRoot);
        
        return facesContext;
    }

    private void _releaseFacesContext(FacesContext facesContext)
    {        
        // make sure that the facesContext gets released.
        // This is important in an OSGi environment 
        if (facesContext != null)
        {
            facesContext.release();
        }
    }
    
    /**
     * Performs initialization tasks depending on the current environment.
     *
     * @param servletContext  the current ServletContext
     * @param externalContext the current ExternalContext
     */
    protected abstract void initContainerIntegration(
            ServletContext servletContext, ExternalContext externalContext);

    /**
     * The intention of this method is provide a point where CDI integration is done.
     * {@link javax.faces.flow.FlowScoped} and {@link javax.faces.view.ViewScoped} requires CDI in order to work,
     * so this method should set a BeanManager instance on application map under
     * the key "oam.cdi.BEAN_MANAGER_INSTANCE".
     * The default implementation look on ServletContext first and then use JNDI.
     * 
     * @param servletContext
     * @param externalContext 
     */
    protected void initCDIIntegration(
            ServletContext servletContext, ExternalContext externalContext)
    {
        // Lookup bean manager and put it into an application scope attribute to 
        // access it later. Remember the trick here is do not call any CDI api 
        // directly, so if no CDI api is on the classpath no exception will be thrown.
        
        // Try with servlet context
        Object beanManager = servletContext.getAttribute(CDI_SERVLET_CONTEXT_BEAN_MANAGER_ATTRIBUTE);
        if (beanManager == null)
        {
            beanManager = lookupBeanManagerFromCDI();
        }
        if (beanManager == null)
        {
            beanManager = lookupBeanManagerFromJndi();
        }
        if (beanManager != null)
        {
            externalContext.getApplicationMap().put(CDI_BEAN_MANAGER_INSTANCE, beanManager);
        }
    }

    /**
     * This method tries to use the CDI-1.1 CDI.current() method to lookup the CDI BeanManager.
     * We do all this via reflection to not blow up if CDI-1.1 is not on the classpath.
     * @return the BeanManager or {@code null} if either not in a CDI-1.1 environment
     *         or the BeanManager doesn't exist yet.
     */
    private Object lookupBeanManagerFromCDI()
    {
        try
        {
            Class cdiClass = ClassUtils.simpleClassForName("javax.enterprise.inject.spi.CDI", false);
            if (cdiClass != null)
            {
                Method currentMethod = cdiClass.getMethod("current");
                Object cdi = currentMethod.invoke(null);

                Method getBeanManagerMethod = cdiClass.getMethod("getBeanManager");
                Object beanManager = getBeanManagerMethod.invoke(cdi);
                return beanManager;
            }
        }
        catch (Exception e)
        {
            // ignore
        }
        return null;
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
            // noop
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

    public void _callPreDestroyOnInjectedJSFArtifacts(FacesContext facesContext)
    {
        InjectionProvider injectionProvider = InjectionProviderFactory.getInjectionProviderFactory(
            facesContext.getExternalContext()).getInjectionProvider(facesContext.getExternalContext());
        List<BeanEntry> injectedBeanStorage =
                (List<BeanEntry>)facesContext.getExternalContext().getApplicationMap().get(INJECTED_BEAN_STORAGE_KEY);

        if (injectedBeanStorage != null)
        {
            for (BeanEntry entry : injectedBeanStorage)
            {
                try
                {
                    injectionProvider.preDestroy(entry.getInstance(), entry.getCreationMetaData());
                }
                catch (InjectionProviderException ex)
                {
                    log.log(Level.INFO, "Exception on PreDestroy", ex);
                }
            }
            injectedBeanStorage.clear();
        }
    }
    
    protected void initWebsocketIntegration(ServletContext servletContext, ExternalContext externalContext)
    {
        Boolean b = WebConfigParamUtils.getBooleanInitParameter(externalContext, 
                PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME);
        
        if (Boolean.TRUE.equals(b))
        {
            // get the instance
            // see https://docs.oracle.com/javaee/7/api/javax/websocket/server/ServerContainer.html)
            final ServerContainer serverContainer = (ServerContainer) 
                    servletContext.getAttribute(ServerContainer.class.getName());
            if (serverContainer == null)
            {
                log.log(Level.INFO, "f:websocket support enabled but cannot found websocket ServerContainer instance "
                        + "on current context.");
                return;
            }

            try 
            {
                serverContainer.addEndpoint(ServerEndpointConfig.Builder
                        .create(EndpointImpl.class, EndpointImpl.JAVAX_FACES_PUSH_PATH)
                        .configurator(new WebsocketConfigurator(externalContext)).build());

                //Init LRU cache
                WebsocketFacesInit.init(externalContext);

                externalContext.getApplicationMap().put("org.apache.myfaces.push", Boolean.TRUE);
            }
            catch (DeploymentException e)
            {
                log.log(Level.INFO, "Exception on initialize Websocket Endpoint: ", e);
            }
        }
    }
    
    /**
     * 
     * @since 2.3
     * @param facesContext 
     * @param servletContext
     */
    protected void initAutomaticExtensionlessMapping(FacesContext facesContext, ServletContext servletContext)
    {
        ServletRegistration facesServletRegistration =
                (ServletRegistration) servletContext.getAttribute(
                    MyFacesContainerInitializer.FACES_SERVLET_SERVLETREGISTRATION);

        if (facesServletRegistration != null)
        {
            facesContext.getApplication().getViewHandler().getViews(facesContext, "/", 
                    ViewVisitOption.RETURN_AS_MINIMAL_IMPLICIT_OUTCOME).forEach(s -> {
                        facesServletRegistration.addMapping(s);
                    });
        }
    }


}
