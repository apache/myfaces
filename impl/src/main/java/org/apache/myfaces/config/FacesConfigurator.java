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
package org.apache.myfaces.config;

import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.BackwardsCompatibleNavigationHandlerWrapper;
import org.apache.myfaces.component.visit.VisitContextFactoryImpl;
import org.apache.myfaces.config.annotation.AnnotationConfigurator;
import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;
import org.apache.myfaces.config.element.Behavior;
import org.apache.myfaces.config.element.ClientBehaviorRenderer;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.FacesConfigData;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.element.ResourceBundle;
import org.apache.myfaces.config.element.SystemEventListener;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.context.ExceptionHandlerFactoryImpl;
import org.apache.myfaces.context.ExternalContextFactoryImpl;
import org.apache.myfaces.context.FacesContextFactoryImpl;
import org.apache.myfaces.context.PartialViewContextFactoryImpl;
import org.apache.myfaces.el.DefaultPropertyResolver;
import org.apache.myfaces.el.VariableResolverImpl;
import org.apache.myfaces.el.unified.ResolverBuilderBase;
import org.apache.myfaces.lifecycle.LifecycleFactoryImpl;
import org.apache.myfaces.renderkit.RenderKitFactoryImpl;
import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;
import org.apache.myfaces.shared_impl.config.MyfacesConfig;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.shared_impl.util.LocaleUtils;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.util.serial.DefaultSerialFactory;
import org.apache.myfaces.shared_impl.util.serial.SerialFactory;
import org.apache.myfaces.spi.FacesConfigurationMerger;
import org.apache.myfaces.spi.FacesConfigurationMergerFactory;
import org.apache.myfaces.util.ContainerUtils;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl;
import org.apache.myfaces.view.facelets.tag.jsf.TagHandlerDelegateFactoryImpl;
import org.apache.myfaces.view.facelets.tag.ui.DebugPhaseListener;
import org.apache.myfaces.webapp.ManagedBeanDestroyerListener;

import javax.el.ELResolver;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ProjectStage;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.PhaseListener;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyCustomScopeEvent;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.validator.BeanValidator;
import javax.faces.webapp.FacesServlet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures everything for a given context. The FacesConfigurator is independent of the concrete implementations that
 * lie behind FacesConfigUnmarshaller and FacesConfigDispenser.
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class FacesConfigurator
{
    //private static final Log log = LogFactory.getLog(FacesConfigurator.class);
    private static final Logger log = Logger.getLogger(FacesConfigurator.class.getName());

    private static final String DEFAULT_RENDER_KIT_CLASS = HtmlRenderKitImpl.class.getName();
    private static final String DEFAULT_APPLICATION_FACTORY = ApplicationFactoryImpl.class.getName();
    private static final String DEFAULT_EXTERNAL_CONTEXT_FACTORY = ExternalContextFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONTEXT_FACTORY = FacesContextFactoryImpl.class.getName();
    private static final String DEFAULT_LIFECYCLE_FACTORY = LifecycleFactoryImpl.class.getName();
    private static final String DEFAULT_RENDER_KIT_FACTORY = RenderKitFactoryImpl.class.getName();
    private static final String DEFAULT_PARTIAL_VIEW_CONTEXT_FACTORY = PartialViewContextFactoryImpl.class.getName();
    private static final String DEFAULT_VISIT_CONTEXT_FACTORY = VisitContextFactoryImpl.class.getName();
    private static final String DEFAULT_VIEW_DECLARATION_LANGUAGE_FACTORY = ViewDeclarationLanguageFactoryImpl.class.getName();
    private static final String DEFAULT_EXCEPTION_HANDLER_FACTORY = ExceptionHandlerFactoryImpl.class.getName();
    private static final String DEFAULT_TAG_HANDLER_DELEGATE_FACTORY = TagHandlerDelegateFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONFIG = "/WEB-INF/faces-config.xml";

    private final ExternalContext _externalContext;
    private FacesConfigUnmarshaller<? extends FacesConfig> _unmarshaller;
    private FacesConfigData _dispenser;
    private AnnotationConfigurator _annotationConfigurator;

    private RuntimeConfig _runtimeConfig;

    private static long lastUpdate;

    public FacesConfigurator(ExternalContext externalContext)
    {
        if (externalContext == null)
        {
            throw new IllegalArgumentException("external context must not be null");
        }
        _externalContext = externalContext;

    }

    /**
     * @param unmarshaller
     *            the unmarshaller to set
     */
    public void setUnmarshaller(FacesConfigUnmarshaller<? extends FacesConfig> unmarshaller)
    {
        _unmarshaller = unmarshaller;
    }

    /**
     * @return the unmarshaller
     */
    protected FacesConfigUnmarshaller<? extends FacesConfig> getUnmarshaller()
    {
        if (_unmarshaller == null)
        {
            _unmarshaller = new DigesterFacesConfigUnmarshallerImpl(_externalContext);
        }

        return _unmarshaller;
    }

    /**
     * @param dispenser
     *            the dispenser to set
     */
    public void setDispenser(FacesConfigData dispenser)
    {
        _dispenser = dispenser;
    }

    /**
     * @return the dispenser
     */
    protected FacesConfigData getDispenser()
    {
        if (_dispenser == null)
        {
            _dispenser = new DigesterFacesConfigDispenserImpl();
        }

        return _dispenser;
    }
    
    public void setAnnotationConfigurator(AnnotationConfigurator configurator)
    {
        _annotationConfigurator = configurator;
    }
    
    protected AnnotationConfigurator getAnnotationConfigurator()
    {
        if (_annotationConfigurator == null)
        {
            _annotationConfigurator = new AnnotationConfigurator();
        }
        return _annotationConfigurator;
    }

    private long getResourceLastModified(String resource)
    {
        try 
        {
            URL url =  _externalContext.getResource(resource);
            if (url != null)
            {
                return getResourceLastModified(url);
            }
        }
        catch (IOException e)
        {
            log.log(Level.SEVERE, "Could not read resource " + resource, e);
        }
        return 0;
    }

    //Taken from trinidad URLUtils
    private long getResourceLastModified(URL url) throws IOException
    {
        if ("file".equals(url.getProtocol()))
        {
            String externalForm = url.toExternalForm();
            // Remove the "file:"
            File file = new File(externalForm.substring(5));

            return file.lastModified();
        }
        else
        {
            return getResourceLastModified(url.openConnection());
        }
    }

    //Taken from trinidad URLUtils
    private long getResourceLastModified(URLConnection connection) throws IOException
    {
        long modified;
        if (connection instanceof JarURLConnection)
        {
            // The following hack is required to work-around a JDK bug.
            // getLastModified() on a JAR entry URL delegates to the actual JAR file
            // rather than the JAR entry.
            // This opens internally, and does not close, an input stream to the JAR
            // file.
            // In turn, you cannot close it by yourself, because it's internal.
            // The work-around is to get the modification date of the JAR file
            // manually,
            // and then close that connection again.

            URL jarFileUrl = ((JarURLConnection) connection).getJarFileURL();
            URLConnection jarFileConnection = jarFileUrl.openConnection();

            try
            {
                modified = jarFileConnection.getLastModified();
            }
            finally
            {
                try
                {
                    jarFileConnection.getInputStream().close();
                }
                catch (Exception exception)
                {
                    // Ignored
                }
            }
        }
        else
        {
            modified = connection.getLastModified();
        }

        return modified;
    }    
    
    private long getLastModifiedTime()
    {
        long lastModified = 0;
        long resModified;

        resModified = getResourceLastModified(DEFAULT_FACES_CONFIG);
        if (resModified > lastModified)
            lastModified = resModified;

        for (String systemId : getConfigFilesList())
        {
            resModified = getResourceLastModified(systemId);
            if (resModified > lastModified)
            {
                lastModified = resModified;
            }
        }

        return lastModified;
    }

    public void update()
    {
        //Google App Engine does not allow to get last modified time of a file; 
        //and when an application is running on GAE there is no way to update faces config xml file.
        //thus, no need to check if the config file is modified.
        if (ContainerUtils.isRunningOnGoogleAppEngine(_externalContext))
            return;
        long refreshPeriod = (MyfacesConfig.getCurrentInstance(_externalContext).getConfigRefreshPeriod()) * 1000;

        if (refreshPeriod > 0)
        {
            long ttl = lastUpdate + refreshPeriod;
            if ((System.currentTimeMillis() > ttl) && (getLastModifiedTime() > ttl))
            {
                try
                {
                    purgeConfiguration();
                }
                catch (NoSuchMethodException e)
                {
                    log.severe("Configuration objects do not support clean-up. Update aborted");

                    // We still want to update the timestamp to avoid running purge on every subsequent
                    // request after this one.
                    //
                    lastUpdate = System.currentTimeMillis();

                    return;
                }
                catch (IllegalAccessException e)
                {
                    log.severe("Error during configuration clean-up" + e.getMessage());
                }
                catch (InvocationTargetException e)
                {
                    log.severe("Error during configuration clean-up" + e.getMessage());
                }
                configure();
                                
                // JSF 2.0 Publish PostConstructApplicationEvent after all configuration resources
                // has been parsed and processed
                FacesContext facesContext = FacesContext.getCurrentInstance();
                Application application = facesContext.getApplication();
                
                application.publishEvent(facesContext, PostConstructApplicationEvent.class, Application.class, application);
            }
        }
    }

    private void purgeConfiguration() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        final Class<?>[] NO_PARAMETER_TYPES = new Class[]{};
        final Object[] NO_PARAMETERS = new Object[]{};

        Method appFactoryPurgeMethod;
        Method renderKitPurgeMethod;
        Method lifecyclePurgeMethod;

        // Check that we have access to all of the necessary purge methods before purging anything
        //
        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        appFactoryPurgeMethod = applicationFactory.getClass().getMethod("purgeApplication", NO_PARAMETER_TYPES);

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitPurgeMethod = renderKitFactory.getClass().getMethod("purgeRenderKit", NO_PARAMETER_TYPES);
        
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        lifecyclePurgeMethod = lifecycleFactory.getClass().getMethod("purgeLifecycle", NO_PARAMETER_TYPES);

        // If there was no exception so far, now we can purge
        //
        appFactoryPurgeMethod.invoke(applicationFactory, NO_PARAMETERS);
        renderKitPurgeMethod.invoke(renderKitFactory, NO_PARAMETERS);
        RuntimeConfig.getCurrentInstance(_externalContext).purge();
        lifecyclePurgeMethod.invoke(lifecycleFactory, NO_PARAMETERS);

        // factories and serial factory need not be purged...
    }

    public void configure() throws FacesException
    {
        // get FacesConfigurationMerger SPI implementation
        FacesConfigurationMerger facesConfigurationMerger = FacesConfigurationMergerFactory
                .getFacesConfigurationMergerFactory(_externalContext).getFacesConfigurationMerger(_externalContext);

        // get all faces-config data, merge it and set it as Dispenser
        setDispenser(facesConfigurationMerger.getFacesConfigData(_externalContext));

        configureFactories();
        configureApplication();
        configureRenderKits();
        
                //Now we can configure annotations
        //getAnnotationConfigurator().configure(
        //        ((ApplicationFactory) FactoryFinder.getFactory(
        //                FactoryFinder.APPLICATION_FACTORY)).getApplication(),
        //        getDispenser(), metadataComplete);
        
        configureRuntimeConfig();
        configureLifecycle();
        handleSerialFactory();
        configureManagedBeanDestroyer();

        // record the time of update
        lastUpdate = System.currentTimeMillis();
    }

    private List<String> getConfigFilesList() {
        String configFiles = _externalContext.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
        List<String> configFilesList = new ArrayList<String>();
        if (configFiles != null)
        {
            StringTokenizer st = new StringTokenizer(configFiles, ",", false);
            while (st.hasMoreTokens())
            {
                String systemId = st.nextToken().trim();

                if (DEFAULT_FACES_CONFIG.equals(systemId))
                {
                    if (log.isLoggable(Level.WARNING))
                    {
                        log.warning(DEFAULT_FACES_CONFIG + " has been specified in the " + FacesServlet.CONFIG_FILES_ATTR
                                + " context parameter of "
                                + "the deployment descriptor. This will automatically be removed, "
                                + "if we wouldn't do this, it would be loaded twice.  See JSF spec 1.1, 10.3.2");
                    }
                }
                else
                {
                    configFilesList.add(systemId);
                }
            }
        }
        return configFilesList;
    }
    
    private void configureFactories()
    {
        FacesConfigData dispenser = getDispenser();
        setFactories(FactoryFinder.APPLICATION_FACTORY, dispenser.getApplicationFactoryIterator(),
                     DEFAULT_APPLICATION_FACTORY);
        setFactories(FactoryFinder.EXCEPTION_HANDLER_FACTORY, dispenser.getExceptionHandlerFactoryIterator(),
                     DEFAULT_EXCEPTION_HANDLER_FACTORY);        
        setFactories(FactoryFinder.EXTERNAL_CONTEXT_FACTORY, dispenser.getExternalContextFactoryIterator(),
                     DEFAULT_EXTERNAL_CONTEXT_FACTORY);
        setFactories(FactoryFinder.FACES_CONTEXT_FACTORY, dispenser.getFacesContextFactoryIterator(),
                     DEFAULT_FACES_CONTEXT_FACTORY);
        setFactories(FactoryFinder.LIFECYCLE_FACTORY, dispenser.getLifecycleFactoryIterator(),
                     DEFAULT_LIFECYCLE_FACTORY);
        setFactories(FactoryFinder.RENDER_KIT_FACTORY, dispenser.getRenderKitFactoryIterator(),
                     DEFAULT_RENDER_KIT_FACTORY);
        setFactories(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY, dispenser.getTagHandlerDelegateFactoryIterator(),
                DEFAULT_TAG_HANDLER_DELEGATE_FACTORY);        
        setFactories(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY, dispenser.getPartialViewContextFactoryIterator(),
                     DEFAULT_PARTIAL_VIEW_CONTEXT_FACTORY);
        setFactories(FactoryFinder.VISIT_CONTEXT_FACTORY, dispenser.getVisitContextFactoryIterator(),
                     DEFAULT_VISIT_CONTEXT_FACTORY);
        setFactories(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY, dispenser.getViewDeclarationLanguageFactoryIterator(),
                     DEFAULT_VIEW_DECLARATION_LANGUAGE_FACTORY);
    }

    private void setFactories(String factoryName, Collection<String> factories, String defaultFactory)
    {
        FactoryFinder.setFactory(factoryName, defaultFactory);
        for (String factory : factories)
        {
            if (!factory.equals(defaultFactory))
            {
                FactoryFinder.setFactory(factoryName, factory);
            }
        }
    }
    
    private void configureApplication()
    {
        Application application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();

        FacesConfigData dispenser = getDispenser();
        application.setActionListener(ClassUtils.buildApplicationObject(ActionListener.class,
                                                           dispenser.getActionListenerIterator(), null));

        if (dispenser.getDefaultLocale() != null)
        {
            application.setDefaultLocale(LocaleUtils.toLocale(dispenser.getDefaultLocale()));
        }

        if (dispenser.getDefaultRenderKitId() != null)
        {
            application.setDefaultRenderKitId(dispenser.getDefaultRenderKitId());
        }

        if (dispenser.getMessageBundle() != null)
        {
            application.setMessageBundle(dispenser.getMessageBundle());
        }

        application.setNavigationHandler(ClassUtils.buildApplicationObject(NavigationHandler.class,
                                                              ConfigurableNavigationHandler.class,
                                                              BackwardsCompatibleNavigationHandlerWrapper.class,
                                                              dispenser.getNavigationHandlerIterator(),
                                                              application.getNavigationHandler()));

        application.setStateManager(ClassUtils.buildApplicationObject(StateManager.class,
                                                         dispenser.getStateManagerIterator(),
                                                         application.getStateManager()));

        application.setResourceHandler(ClassUtils.buildApplicationObject(ResourceHandler.class,
                                                            dispenser.getResourceHandlerIterator(),
                                                            application.getResourceHandler()));

        List<Locale> locales = new ArrayList<Locale>();
        for (String locale : dispenser.getSupportedLocalesIterator())
        {
            locales.add(LocaleUtils.toLocale(locale));
        }

        application.setSupportedLocales(locales);

        application.setViewHandler(ClassUtils.buildApplicationObject(ViewHandler.class,
                                                        dispenser.getViewHandlerIterator(),
                                                        application.getViewHandler()));
        for (SystemEventListener systemEventListener : dispenser.getSystemEventListeners())
        {


            try {
                //note here used to be an instantiation to deal with the explicit source type in the registration,
                // that cannot work because all system events need to have the source being passed in the constructor
                //instead we now  rely on the standard system event types and map them to their appropriate constructor types
                Class eventClass = ClassUtils.classForName((systemEventListener.getSystemEventClass() != null) ? systemEventListener.getSystemEventClass():SystemEvent.class.getName());
                application.subscribeToEvent(
                    (Class<? extends SystemEvent>)eventClass ,
                        (Class<?>)ClassUtils.classForName((systemEventListener.getSourceClass() != null) ? systemEventListener.getSourceClass(): getDefaultSourcClassForSystemEvent(eventClass) ), //Application.class???
                        (javax.faces.event.SystemEventListener)ClassUtils.newInstance(systemEventListener.getSystemEventListenerClass()));
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "System event listener could not be initialized, reason:",e);
            }
        }

        
        for (String componentType : dispenser.getComponentTypes())
        {
            application.addComponent(componentType, dispenser.getComponentClass(componentType));
        }

        for (String converterId : dispenser.getConverterIds())
        {
            application.addConverter(converterId, dispenser.getConverterClassById(converterId));
        }

        for (String converterClass : dispenser.getConverterClasses())
        {
            try
            {
                application.addConverter(ClassUtils.simpleClassForName(converterClass),
                                         dispenser.getConverterClassByClass(converterClass));
            }
            catch (Exception ex)
            {
                log.log(Level.SEVERE, "Converter could not be added. Reason:", ex);
            }
        }

        for (String validatorId : dispenser.getValidatorIds())
        {
            application.addValidator(validatorId, dispenser.getValidatorClass(validatorId));
        }

        // programmatically add the BeanValidator if the following requirements are met:
        //     - bean validation has not been disabled
        //     - bean validation is available in the classpath
        String beanValidatorDisabled = _externalContext.getInitParameter(
                BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME);
        final boolean defaultBeanValidatorDisabled = (beanValidatorDisabled != null 
                && beanValidatorDisabled.toLowerCase().equals("true"));
        boolean beanValidatorInstalledProgrammatically = false;
        if (!defaultBeanValidatorDisabled
                && ExternalSpecifications.isBeanValidationAvailable())
        {
            // add the BeanValidator as default validator
            application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
            beanValidatorInstalledProgrammatically = true;
        }

        // add the default-validators from the config files
        for (String validatorId : dispenser.getDefaultValidatorIds())
        {
            application.addDefaultValidatorId(validatorId);
        }
        
        // do some checks if the BeanValidator was not installed as a
        // default-validator programmatically, but via a config file.
        if (!beanValidatorInstalledProgrammatically 
                && application.getDefaultValidatorInfo()
                        .containsKey(BeanValidator.VALIDATOR_ID))
        {
            if (!ExternalSpecifications.isBeanValidationAvailable())
            {
                // the BeanValidator was installed via a config file,
                // but bean validation is not available
                log.log(Level.WARNING, "The BeanValidator was installed as a " +
                        "default-validator from a faces-config file, but bean " +
                        "validation is not available on the classpath, " +
                        "thus it will not work!");
            }
            else if (defaultBeanValidatorDisabled)
            {
                // the user disabled the default bean validator in web.xml,
                // but a config file added it, which is ok with the spec
                // (section 11.1.3: "though manual installation is still possible")
                // --> inform the user about this scenario
                log.log(Level.INFO, "The BeanValidator was disabled as a " +
                        "default-validator via the config parameter " + 
                        BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME +
                        " in web.xml, but a faces-config file added it, " +
                        "thus it actually was installed as a default-validator.");
            }
        }

        for (Behavior behavior : dispenser.getBehaviors()) {
            application.addBehavior(behavior.getBehaviorId(), behavior.getBehaviorClass());
        }
        
        RuntimeConfig runtimeConfig = getRuntimeConfig();

        runtimeConfig.setPropertyResolverChainHead(ClassUtils.buildApplicationObject(PropertyResolver.class,
                                                                        dispenser.getPropertyResolverIterator(),
                                                                        new DefaultPropertyResolver()));

        runtimeConfig.setVariableResolverChainHead(ClassUtils.buildApplicationObject(VariableResolver.class,
                                                                        dispenser.getVariableResolverIterator(),
                                                                        new VariableResolverImpl()));
    }

    /**
     * A mapper for the handful of system listener defaults
     * since every default mapper has the source type embedded
     * in the constructor we can rely on introspection for the
     * default mapping
     *
     * @param systemEventClass the system listener class which has to be checked
     * @return
     */
    String getDefaultSourcClassForSystemEvent(Class systemEventClass)
    {
        Constructor[] constructors = systemEventClass.getConstructors();
        for(Constructor constr: constructors) {
            Class [] parms = constr.getParameterTypes();
            if(parms == null || parms.length != 1)
            {
                //for standard types we have only one parameter representing the type
                continue;
            }
            return parms[0].getName();
        }
        log.warning("The SystemEvent source type for "+systemEventClass.getName() + " could not be detected, either register it manually or use a constructor argument for auto detection, defaulting now to java.lang.Object");
        return "java.lang.Object";
    }


    protected RuntimeConfig getRuntimeConfig()
    {
        if (_runtimeConfig == null)
        {
            _runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);
        }
        return _runtimeConfig;
    }

    public void setRuntimeConfig(RuntimeConfig runtimeConfig)
    {
        _runtimeConfig = runtimeConfig;
    }

    private void configureRuntimeConfig()
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);

        FacesConfigData dispenser = getDispenser();
        for (ManagedBean bean : dispenser.getManagedBeans())
        {
            if (log.isLoggable(Level.WARNING) && runtimeConfig.getManagedBean(bean.getManagedBeanName()) != null)
            {
                log.warning("More than one managed bean w/ the name of '" + bean.getManagedBeanName()
                        + "' - only keeping the last ");
            }

            runtimeConfig.addManagedBean(bean.getManagedBeanName(), bean);

        }

        removePurgedBeansFromSessionAndApplication(runtimeConfig);

        for (NavigationRule rule : dispenser.getNavigationRules())
        {
            runtimeConfig.addNavigationRule(rule);
        }

        for (String converterClassName : dispenser.getConverterConfigurationByClassName())
        {
            runtimeConfig.addConverterConfiguration(converterClassName,
                                                                      _dispenser.getConverterConfiguration(converterClassName));
        }

        for (ResourceBundle bundle : dispenser.getResourceBundles())
        {
            runtimeConfig.addResourceBundle(bundle);
        }

        for (String className : dispenser.getElResolvers())
        {
            runtimeConfig.addFacesConfigElResolver((ELResolver)ClassUtils.newInstance(className, ELResolver.class));
        }
        
        runtimeConfig.setFacesVersion (dispenser.getFacesVersion());
        
        runtimeConfig.setNamedEventManager(new NamedEventManager());
        
        for (NamedEvent event : dispenser.getNamedEvents())
        {
            try
            {
                Class<? extends ComponentSystemEvent> clazz = ClassUtils.classForName(event.getEventClass());
                runtimeConfig.getNamedEventManager().addNamedEvent(event.getShortName(), clazz);                
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "Named event could not be initialized, reason:",e);
            }
        }

        String comparatorClass = _externalContext.getInitParameter(ResolverBuilderBase.EL_RESOLVER_COMPARATOR);
        
        if (comparatorClass != null && !"".equals(comparatorClass))
        {
            // get the comparator class
            Class<Comparator<ELResolver>> clazz;
            try {
                clazz = ClassUtils.classForName(comparatorClass);
                // create the instance
                Comparator<ELResolver> comparator = clazz.newInstance();
                
                runtimeConfig.setELResolverComparator(comparator);
            } catch (Exception e)
            {
                if (log.isLoggable(Level.SEVERE))
                {
                    log.log(Level.SEVERE, "Cannot instantiate EL Resolver Comparator "+ comparatorClass+
                            " . Check org.apache.myfaces.EL_RESOLVER_COMPARATOR web config param. Initialization continues with no comparator used.", e);
                }
            } 
        }
        else
        {
            runtimeConfig.setELResolverComparator(null);
        }
    }

    private void removePurgedBeansFromSessionAndApplication(RuntimeConfig runtimeConfig)
    {
        Map<String, ManagedBean> oldManagedBeans = runtimeConfig.getManagedBeansNotReaddedAfterPurge();
        if (oldManagedBeans != null)
        {
            for (Map.Entry<String, ManagedBean> entry : oldManagedBeans.entrySet())
            {
                ManagedBean bean = entry.getValue();

                String scope = bean.getManagedBeanScope();

                if (scope != null && scope.equalsIgnoreCase("session"))
                {
                    _externalContext.getSessionMap().remove(entry.getKey());
                }
                else if (scope != null && scope.equalsIgnoreCase("application"))
                {
                    _externalContext.getApplicationMap().remove(entry.getKey());
                }
            }
        }
        
        runtimeConfig.resetManagedBeansNotReaddedAfterPurge();
    }

    private void configureRenderKits()
    {
        RenderKitFactory renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        FacesConfigData dispenser = getDispenser();
        for (String renderKitId : dispenser.getRenderKitIds())
        {
            Collection<String> renderKitClass = dispenser.getRenderKitClasses(renderKitId);

            if (renderKitClass.isEmpty())
            {
                renderKitClass = new ArrayList<String>(1);
                renderKitClass.add(DEFAULT_RENDER_KIT_CLASS);
            }

            //RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);
            RenderKit renderKit = (RenderKit) ClassUtils.buildApplicationObject(RenderKit.class, renderKitClass, null);

            for (Renderer element : dispenser.getRenderers(renderKitId))
            {
                javax.faces.render.Renderer renderer;
                Collection<ClientBehaviorRenderer> clientBehaviorRenderers = dispenser.getClientBehaviorRenderers (renderKitId);
                
                try
                {
                    renderer = (javax.faces.render.Renderer) ClassUtils.newInstance(element.getRendererClass());
                }
                catch (Throwable e)
                {
                    // ignore the failure so that the render kit is configured
                    log.log(Level.SEVERE, "failed to configure class " + element.getRendererClass(), e);
                    continue;
                }

                renderKit.addRenderer(element.getComponentFamily(), element.getRendererType(), renderer);
                
                // Add in client behavior renderers.
                
                for (ClientBehaviorRenderer clientBehaviorRenderer : clientBehaviorRenderers) {
                    try {
                        javax.faces.render.ClientBehaviorRenderer behaviorRenderer = (javax.faces.render.ClientBehaviorRenderer)
                            ClassUtils.newInstance (clientBehaviorRenderer.getRendererClass());
                        
                        renderKit.addClientBehaviorRenderer(clientBehaviorRenderer.getRendererType(), behaviorRenderer);
                    }
                    
                    catch (Throwable e) {
                        // Ignore.
                        
                        if (log.isLoggable(Level.SEVERE)) {
                            log.log(Level.SEVERE, "failed to configure client behavior renderer class " +
                                 clientBehaviorRenderer.getRendererClass(), e);
                        }
                    }
                }
            }

            renderKitFactory.addRenderKit(renderKitId, renderKit);
        }
    }

    private void configureLifecycle()
    {
        // create the lifecycle used by the app
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());

        // add phase listeners
        for (String listenerClassName : getDispenser().getLifecyclePhaseListeners())
        {
            try
            {
                lifecycle.addPhaseListener((PhaseListener) ClassUtils.newInstance(listenerClassName, PhaseListener.class));
            }
            catch (ClassCastException e)
            {
                log.severe("Class " + listenerClassName + " does not implement PhaseListener");
            }
        }
        
        // if ProjectStage is Development, install the DebugPhaseListener
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext.isProjectStage(ProjectStage.Development))
        {
            lifecycle.addPhaseListener(new DebugPhaseListener());
        }
    }

    private String getLifecycleId()
    {
        String id = _externalContext.getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);

        if (id != null)
        {
            return id;
        }

        return LifecycleFactory.DEFAULT_LIFECYCLE;
    }

    private void handleSerialFactory()
    {

        String serialProvider = _externalContext.getInitParameter(StateUtils.SERIAL_FACTORY);
        SerialFactory serialFactory = null;

        if (serialProvider == null)
        {
            serialFactory = new DefaultSerialFactory();
        }
        else
        {
            try
            {
                serialFactory = (SerialFactory) ClassUtils.newInstance(serialProvider);

            }
            catch (ClassCastException e)
            {
                log.log(Level.SEVERE, "Make sure '" + serialProvider + "' implements the correct interface", e);
            }
            catch (Exception e)
            {
                log.log(Level.SEVERE,"", e);
            }
            finally
            {
                if (serialFactory == null)
                {
                    serialFactory = new DefaultSerialFactory();
                    log.severe("Using default serialization provider");
                }
            }

        }

        log.info("Serialization provider : " + serialFactory.getClass());
        _externalContext.getApplicationMap().put(StateUtils.SERIAL_FACTORY, serialFactory);
    }

    private void configureManagedBeanDestroyer()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        Application application = facesContext.getApplication();

        // get RuntimeConfig and LifecycleProvider
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        LifecycleProvider lifecycleProvider = LifecycleProviderFactory
                .getLifecycleProviderFactory(externalContext).getLifecycleProvider(externalContext);

        // create ManagedBeanDestroyer
        ManagedBeanDestroyer mbDestroyer
                = new ManagedBeanDestroyer(lifecycleProvider, runtimeConfig);

        // subscribe ManagedBeanDestroyer as listener for needed events 
        application.subscribeToEvent(PreDestroyCustomScopeEvent.class, mbDestroyer);
        application.subscribeToEvent(PreDestroyViewMapEvent.class, mbDestroyer);

        // get ManagedBeanDestroyerListener instance 
        ManagedBeanDestroyerListener listener = (ManagedBeanDestroyerListener)
                applicationMap.get(ManagedBeanDestroyerListener.APPLICATION_MAP_KEY);
        if (listener != null)
        {
            // set the instance on the listener
            listener.setManagedBeanDestroyer(mbDestroyer);
        }
        else
        {
            log.log(Level.SEVERE, "No ManagedBeanDestroyerListener instance found, thus "
                    + "@PreDestroy methods won't get called in every case. "
                    + "This instance needs to be published before configuration is started.");
        }
    }

}
