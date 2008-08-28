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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.el.ELResolver;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.webapp.FacesServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;
import org.apache.myfaces.context.FacesContextFactoryImpl;
import org.apache.myfaces.el.DefaultPropertyResolver;
import org.apache.myfaces.el.VariableResolverImpl;
import org.apache.myfaces.lifecycle.LifecycleFactoryImpl;
import org.apache.myfaces.renderkit.RenderKitFactoryImpl;
import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;
import org.apache.myfaces.shared_impl.config.MyfacesConfig;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.shared_impl.util.LocaleUtils;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.util.serial.DefaultSerialFactory;
import org.apache.myfaces.shared_impl.util.serial.SerialFactory;
import org.xml.sax.SAXException;

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
    private static final Log log = LogFactory.getLog(FacesConfigurator.class);

    private static final String STANDARD_FACES_CONFIG_RESOURCE = "META-INF/standard-faces-config.xml";
    private static final String FACES_CONFIG_RESOURCE = "META-INF/faces-config.xml";

    private static final String META_INF_SERVICES_RESOURCE_PREFIX = "META-INF/services/";

    private static final String DEFAULT_RENDER_KIT_CLASS = HtmlRenderKitImpl.class.getName();
    private static final String DEFAULT_APPLICATION_FACTORY = ApplicationFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONTEXT_FACTORY = FacesContextFactoryImpl.class.getName();
    private static final String DEFAULT_LIFECYCLE_FACTORY = LifecycleFactoryImpl.class.getName();
    private static final String DEFAULT_RENDER_KIT_FACTORY = RenderKitFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONFIG = "/WEB-INF/faces-config.xml";

    private static final Set<String> FACTORY_NAMES = new HashSet<String>();
    {
        FACTORY_NAMES.add(FactoryFinder.APPLICATION_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.FACES_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.LIFECYCLE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.RENDER_KIT_FACTORY);
    }

    private final ExternalContext _externalContext;
    private FacesConfigUnmarshaller _unmarshaller;
    private FacesConfigDispenser _dispenser;

    private RuntimeConfig _runtimeConfig;
    private static final String JAR_EXTENSION = ".jar";
    private static final String META_INF_MANIFEST_SUFFIX = "!/META-INF/MANIFEST.MF";
    private static final String JAR_PREFIX = "jar:";

    private static long lastUpdate;

    public static final String MYFACES_API_PACKAGE_NAME = "myfaces-api";
    public static final String MYFACES_IMPL_PACKAGE_NAME = "myfaces-impl";
    public static final String MYFACES_TOMAHAWK_PACKAGE_NAME = "tomahawk";
    public static final String MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME = "tomahawk-sandbox";
    public static final String MYFACES_TOMAHAWK_SANDBOX15_PACKAGE_NAME = "tomahawk-sandbox15";
    public static final String COMMONS_EL_PACKAGE_NAME = "commons-el";
    public static final String JSP_API_PACKAGE_NAME = "jsp-api";

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
    public void setUnmarshaller(FacesConfigUnmarshaller unmarshaller)
    {
        _unmarshaller = unmarshaller;
    }

    /**
     * @return the unmarshaller
     */
    protected FacesConfigUnmarshaller getUnmarshaller()
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
    public void setDispenser(FacesConfigDispenser dispenser)
    {
        _dispenser = dispenser;
    }

    /**
     * @return the dispenser
     */
    protected FacesConfigDispenser getDispenser()
    {
        if (_dispenser == null)
        {
            _dispenser = new DigesterFacesConfigDispenserImpl();
        }
        return _dispenser;
    }

    private long getResourceLastModified(String resource)
    {
        try {
            URL url =  _externalContext.getResource(resource);
            if (url != null) {
                return url.openConnection().getLastModified();
            }
        } catch (IOException e) {
            log.error("Could not read resource " + resource, e);
        }
        return 0;
    }
    
    private long getLastModifiedTime()
    {
        long lastModified = 0;
        long resModified;

        resModified = getResourceLastModified(DEFAULT_FACES_CONFIG);
        if (resModified > lastModified)
            lastModified = resModified;


        List configFilesList = getConfigFilesList();

        for (int i = 0; i < configFilesList.size(); i++) {
            String systemId = (String) configFilesList.get(i);

            resModified = getResourceLastModified(systemId);
                if (resModified > lastModified)
                    lastModified = resModified;

        }

        return lastModified;
    }

    public void update()
    {
        long refreshPeriod = (MyfacesConfig.getCurrentInstance(_externalContext).getConfigRefreshPeriod())*1000;

        if (refreshPeriod > 0){
            long ttl = lastUpdate + refreshPeriod;
            if ((System.currentTimeMillis() > ttl) && (getLastModifiedTime() > ttl)) {
                try {
                    purgeConfiguration();
                } catch (NoSuchMethodException e) {
                    log.error("Configuration objects do not support clean-up. Update aborted");
                    return;
                } catch (IllegalAccessException e) {
                    log.fatal("Error during configuration clean-up" + e.getMessage());
                } catch (InvocationTargetException e) {
                    log.fatal("Error during configuration clean-up" + e.getMessage());
                }
                configure();
            }
        }
    }

    private void purgeConfiguration() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Method purgeMethod;
        Class[] emptyParameterList = new Class[]{};

        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        purgeMethod = applicationFactory.getClass().getMethod("purgeApplication", emptyParameterList);
        purgeMethod.invoke(applicationFactory, emptyParameterList);

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        purgeMethod = renderKitFactory.getClass().getMethod("purgeRenderKit", emptyParameterList);
        purgeMethod.invoke(renderKitFactory, emptyParameterList);

        RuntimeConfig.getCurrentInstance(_externalContext).purge();

        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        purgeMethod = lifecycleFactory.getClass().getMethod("purgeLifecycle", emptyParameterList);
        purgeMethod.invoke(lifecycleFactory, emptyParameterList);

        // factories and serial factory need not be purged...
    }


    public void configure() throws FacesException
    {
        try
        {
            feedStandardConfig();
            feedMetaInfServicesFactories();
            feedClassloaderConfigurations();
            feedContextSpecifiedConfig();
            feedWebAppConfig();

            if (log.isInfoEnabled())
            {
                logMetaInf();
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        catch (SAXException e)
        {
            throw new FacesException(e);
        }

        configureFactories();
        configureApplication();
        configureRenderKits();
        configureRuntimeConfig();
        configureLifecycle();
        handleSerialFactory();

        //record the time of update
        lastUpdate = System.currentTimeMillis();
    }

    private void feedStandardConfig() throws IOException, SAXException
    {
        InputStream stream = ClassUtils.getResourceAsStream(STANDARD_FACES_CONFIG_RESOURCE);
        if (stream == null)
            throw new FacesException("Standard faces config " + STANDARD_FACES_CONFIG_RESOURCE + " not found");
        if (log.isInfoEnabled())
            log.info("Reading standard config " + STANDARD_FACES_CONFIG_RESOURCE);
        getDispenser().feed(getUnmarshaller().getFacesConfig(stream, STANDARD_FACES_CONFIG_RESOURCE));
        stream.close();
    }

    /**
     * This method performs part of the factory search outlined in section 10.2.6.1.
     */
    protected void logMetaInf()
    {
        try
        {
            List<VersionInfo> li = new ArrayList<VersionInfo>();
            li.add(new VersionInfo(MYFACES_API_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_IMPL_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_TOMAHAWK_SANDBOX15_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_TOMAHAWK_PACKAGE_NAME));

            Iterator it = ClassUtils.getResources("META-INF/MANIFEST.MF", this);
            while (it.hasNext())
            {
                URL url = (URL) it.next();

                for (int i = 0; i < li.size(); i++)
                {
                    VersionInfo versionInfo = li.get(i);
                    if (checkJar(versionInfo, url))
                        break;
                }
            }

            for (int i = 0; i < li.size(); i++)
            {
                VersionInfo versionInfo = li.get(i);

                if (versionInfo.getUsedVersion() != null)
                {
                    if (log.isInfoEnabled())
                    {
                        log.info("Starting up MyFaces-package : " + versionInfo.getPackageName() + " in version : "
                                + versionInfo.getUsedVersion() + " from path : " + versionInfo.getUsedVersionPath());
                    }
                }
                else
                {
                    if (log.isInfoEnabled())
                    {
                        log.info("MyFaces-package : " + versionInfo.getPackageName() + " not found.");
                    }
                }
            }
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }

    private static boolean checkJar(VersionInfo versionInfo, URL path)
    {
        int index;

        String version = versionInfo.getLastVersion();

        String pathString = path.toString();

        if (!pathString.startsWith(JAR_PREFIX))
            return false;

        if (!(pathString.length() > (META_INF_MANIFEST_SUFFIX.length() + JAR_PREFIX.length())))
        {
            if (log.isDebugEnabled())
                log.debug("PathString : " + pathString + " not long enough to be parsed.");
            return false;
        }

        pathString = pathString.substring(JAR_PREFIX.length(), pathString.length() - META_INF_MANIFEST_SUFFIX.length());

        File file = new File(pathString);

        String fileName = file.getName();

        if (fileName.endsWith(JAR_EXTENSION) && ((index = fileName.indexOf(versionInfo.getPackageName())) != -1))
        {
            int beginIndex = index + versionInfo.getPackageName().length() + 1;

            if (beginIndex > fileName.length() - 1)
            {
                log.debug("beginIndex out of bounds. fileName: " + fileName);
                return false;
            }

            int endIndex = fileName.length() - JAR_EXTENSION.length();

            if (endIndex < 0 || endIndex <= beginIndex)
            {
                log.debug("endIndex out of bounds. fileName: " + fileName);
                return false;
            }

            String newVersion = fileName.substring(beginIndex, endIndex);

            if (version == null)
            {
                versionInfo.addJarInfo(pathString, newVersion);
            }
            else if (version.equals(newVersion))
            {
                versionInfo.addJarInfo(pathString, version);
            }
            else
            {
                log.error("You are using the MyFaces-package : " + versionInfo.getPackageName()
                        + " in different versions; first (and probably used) version is : "
                        + versionInfo.getUsedVersion() + ", currently encountered version is : " + newVersion
                        + ". This will cause undesired behaviour. Please clean out your class-path."
                        + " The first encountered version is loaded from : " + versionInfo.getUsedVersionPath()
                        + ". The currently encountered version is loaded from : " + path);
            }

            return true;
        }

        return false;
    }

    /**
     * This method performs part of the factory search outlined in section 10.2.6.1.
     */
    protected void feedMetaInfServicesFactories()
    {
        try
        {
            for (Iterator<String> iterator = FACTORY_NAMES.iterator(); iterator.hasNext();)
            {
                String factoryName = iterator.next();
                Iterator it = ClassUtils.getResources(META_INF_SERVICES_RESOURCE_PREFIX + factoryName, this);
                while (it.hasNext())
                {
                    URL url = (URL) it.next();
                    InputStream stream = openStreamWithoutCache(url);
                    InputStreamReader isr = new InputStreamReader(stream);
                    BufferedReader br = new BufferedReader(isr);
                    String className;
                    try
                    {
                        className = br.readLine();
                    }
                    catch (IOException e)
                    {
                        throw new FacesException("Unable to read class name from file " + url.toExternalForm(), e);
                    }
                    br.close();
                    isr.close();
                    stream.close();

                    if (log.isInfoEnabled())
                        log.info("Found " + factoryName + " factory implementation: " + className);

                    if (factoryName.equals(FactoryFinder.APPLICATION_FACTORY))
                    {
                        getDispenser().feedApplicationFactory(className);
                    }
                    else if (factoryName.equals(FactoryFinder.FACES_CONTEXT_FACTORY))
                    {
                        getDispenser().feedFacesContextFactory(className);
                    }
                    else if (factoryName.equals(FactoryFinder.LIFECYCLE_FACTORY))
                    {
                        getDispenser().feedLifecycleFactory(className);
                    }
                    else if (factoryName.equals(FactoryFinder.RENDER_KIT_FACTORY))
                    {
                        getDispenser().feedRenderKitFactory(className);
                    }
                    else
                    {
                        throw new IllegalStateException("Unexpected factory name " + factoryName);
                    }
                }
            }
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }

    private InputStream openStreamWithoutCache(URL url) throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }

    /**
     * This method fixes MYFACES-208
     */
    private void feedClassloaderConfigurations()
    {
        try
        {
            Map<String,URL> facesConfigs = new TreeMap<String,URL>();
            Iterator it = ClassUtils.getResources(FACES_CONFIG_RESOURCE, this);
            while (it.hasNext())
            {
                URL url = (URL) it.next();
                String systemId = url.toExternalForm();
                facesConfigs.put(systemId,url);
            }

            Iterator<Map.Entry<String,URL>> facesConfigIt=facesConfigs.entrySet().iterator();

            while(facesConfigIt.hasNext()) {
                Map.Entry<String,URL> entry = facesConfigIt.next();
                InputStream stream = null;
                try {
                    openStreamWithoutCache(entry.getValue());
                    if (log.isInfoEnabled())
                        log.info("Reading config : " + entry.getKey());
                    getDispenser().feed(getUnmarshaller().getFacesConfig(stream, entry.getKey()));
                }
                finally {
                    if(stream!=null)
                        stream.close();
                }
            }
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }


    private void feedContextSpecifiedConfig() throws IOException, SAXException
    {
        List configFilesList = getConfigFilesList();
        for (int i = 0; i < configFilesList.size(); i++) {
            String systemId = (String) configFilesList.get(i);
            InputStream stream = _externalContext.getResourceAsStream(systemId);
            if (stream == null)
            {
                log.error("Faces config resource " + systemId + " not found");
                continue;
            }

            if (log.isInfoEnabled()) log.info("Reading config " + systemId);
            getDispenser().feed(getUnmarshaller().getFacesConfig(stream, systemId));
            stream.close();
        }
    }

    private List getConfigFilesList() {
        String configFiles = _externalContext.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
        List configFilesList = new ArrayList();
        if (configFiles != null)
        {
            StringTokenizer st = new StringTokenizer(configFiles, ",", false);
            while (st.hasMoreTokens())
            {
                String systemId = st.nextToken().trim();

                if (DEFAULT_FACES_CONFIG.equals(systemId))
                {
                    if(log.isWarnEnabled())
                        log.warn(DEFAULT_FACES_CONFIG + " has been specified in the " +
                                FacesServlet.CONFIG_FILES_ATTR + " context parameter of " +
                                "the deployment descriptor. This will automatically be removed, " +
                                "if we wouldn't do this, it would be loaded twice.  See JSF spec 1.1, 10.3.2");
                }
                else
                    configFilesList.add(systemId);
            }
        }
        return configFilesList;
    }

    private void feedWebAppConfig() throws IOException, SAXException
    {
        // web application config
        InputStream stream = _externalContext.getResourceAsStream(DEFAULT_FACES_CONFIG);
        if (stream != null)
        {
            if (log.isInfoEnabled())
                log.info("Reading config /WEB-INF/faces-config.xml");
            getDispenser().feed(getUnmarshaller().getFacesConfig(stream, DEFAULT_FACES_CONFIG));
            stream.close();
        }
    }

    private void configureFactories()
    {
        FacesConfigDispenser dispenser = getDispenser();
        setFactories(FactoryFinder.APPLICATION_FACTORY, dispenser.getApplicationFactoryIterator(),
                DEFAULT_APPLICATION_FACTORY);
        setFactories(FactoryFinder.FACES_CONTEXT_FACTORY, dispenser.getFacesContextFactoryIterator(),
                DEFAULT_FACES_CONTEXT_FACTORY);
        setFactories(FactoryFinder.LIFECYCLE_FACTORY, dispenser.getLifecycleFactoryIterator(),
                DEFAULT_LIFECYCLE_FACTORY);
        setFactories(FactoryFinder.RENDER_KIT_FACTORY, dispenser.getRenderKitFactoryIterator(),
                DEFAULT_RENDER_KIT_FACTORY);
    }

    private void setFactories(String factoryName, Iterator factories, String defaultFactory)
    {
        FactoryFinder.setFactory(factoryName, defaultFactory);
        while (factories.hasNext())
        {
            String factory = (String) factories.next();
            if (!factory.equals(defaultFactory))
                FactoryFinder.setFactory(factoryName, factory);
        }
    }

    private void configureApplication()
    {
        Application application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY))
                .getApplication();
        FacesConfigDispenser dispenser = getDispenser();
        application.setActionListener((ActionListener) getApplicationObject(ActionListener.class, dispenser
                .getActionListenerIterator(), null));

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

        application.setNavigationHandler((NavigationHandler) getApplicationObject(NavigationHandler.class, dispenser
                .getNavigationHandlerIterator(), application.getNavigationHandler()));
        
        application.setStateManager((StateManager) getApplicationObject(StateManager.class, dispenser
                .getStateManagerIterator(), application.getStateManager()));
        List<Locale> locales = new ArrayList<Locale>();
        for (Iterator it = dispenser.getSupportedLocalesIterator(); it.hasNext();)
        {
            locales.add(LocaleUtils.toLocale((String) it.next()));
        }
        application.setSupportedLocales(locales);

        application.setViewHandler((ViewHandler) getApplicationObject(ViewHandler.class, dispenser
                .getViewHandlerIterator(), application.getViewHandler()));

        for (Iterator it = dispenser.getComponentTypes(); it.hasNext();)
        {
            String componentType = (String) it.next();
            application.addComponent(componentType, dispenser.getComponentClass(componentType));
        }

        for (Iterator it = dispenser.getConverterIds(); it.hasNext();)
        {
            String converterId = (String) it.next();
            application.addConverter(converterId, dispenser.getConverterClassById(converterId));
        }

        for (Iterator it = dispenser.getConverterClasses(); it.hasNext();)
        {
            String converterClass = (String) it.next();
            try
            {
                application.addConverter(ClassUtils.simpleClassForName(converterClass), dispenser
                        .getConverterClassByClass(converterClass));
            }
            catch (Exception ex)
            {
                log.error("Converter could not be added. Reason:", ex);
            }
        }

        if (application instanceof ApplicationImpl)
        {
            for (Iterator it = dispenser.getConverterConfigurationByClassName(); it.hasNext();)
            {
                String converterClassName = (String) it.next();

                ((ApplicationImpl) application).addConverterConfiguration(converterClassName, dispenser
                        .getConverterConfiguration(converterClassName));
            }
        }

        for (Iterator it = dispenser.getValidatorIds(); it.hasNext();)
        {
            String validatorId = (String) it.next();
            application.addValidator(validatorId, dispenser.getValidatorClass(validatorId));
        }

        RuntimeConfig runtimeConfig = getRuntimeConfig();
        
        runtimeConfig.setPropertyResolverChainHead((PropertyResolver) getApplicationObject(PropertyResolver.class, dispenser
                .getPropertyResolverIterator(), new DefaultPropertyResolver()));
        
        runtimeConfig.setVariableResolverChainHead((VariableResolver) getApplicationObject(VariableResolver.class, dispenser
                .getVariableResolverIterator(), new VariableResolverImpl()));
    }

    /**
     * @return
     */
    protected RuntimeConfig getRuntimeConfig()
    {
        if(_runtimeConfig == null) 
        {
            _runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);
        }
        return _runtimeConfig;
    }
    
    public void setRuntimeConfig(RuntimeConfig runtimeConfig)
    {
        _runtimeConfig = runtimeConfig;
    }

    private Object getApplicationObject(Class interfaceClass, Iterator classNamesIterator, Object defaultObject)
    {
        Object current = defaultObject;

        while (classNamesIterator.hasNext())
        {
            String implClassName = (String) classNamesIterator.next();
            Class implClass = ClassUtils.simpleClassForName(implClassName);

            // check, if class is of expected interface type
            if (!interfaceClass.isAssignableFrom(implClass))
            {
                throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
            }

            if (current == null)
            {
                // nothing to decorate
                current = ClassUtils.newInstance(implClass);
            }
            else
            {
                // let's check if class supports the decorator pattern
                try
                {
                    Constructor delegationConstructor = implClass.getConstructor(new Class[] { interfaceClass });
                    // impl class supports decorator pattern,
                    try
                    {
                        // create new decorator wrapping current
                        current = delegationConstructor.newInstance(new Object[] { current });
                    }
                    catch (InstantiationException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // no decorator pattern support
                    current = ClassUtils.newInstance(implClass);
                }
            }
        }

        return current;
    }

    private void configureRuntimeConfig()
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);

        FacesConfigDispenser dispenser = getDispenser();
        for (Iterator iterator = dispenser.getManagedBeans(); iterator.hasNext();)
        {
            ManagedBean bean = (ManagedBean) iterator.next();

            if (log.isWarnEnabled() && runtimeConfig.getManagedBean(bean.getManagedBeanName()) != null)
                log.warn("More than one managed bean w/ the name of '" + bean.getManagedBeanName()
                        + "' - only keeping the last ");

            runtimeConfig.addManagedBean(bean.getManagedBeanName(), bean);

        }

        removePurgedBeansFromSessionAndApplication(runtimeConfig);

        for (Iterator iterator = dispenser.getNavigationRules(); iterator.hasNext();)
        {
            NavigationRule rule = (NavigationRule) iterator.next();
            runtimeConfig.addNavigationRule(rule);

        }

        for (Iterator<ResourceBundle> iter = dispenser.getResourceBundles(); iter.hasNext();)
        {
            runtimeConfig.addResourceBundle(iter.next());
        }
        
        for (Iterator<String> iter = dispenser.getElResolvers(); iter.hasNext();)
        {
            runtimeConfig.addFacesConfigElResolver((ELResolver) ClassUtils.newInstance(iter.next(), ELResolver.class));
        }

    }

    private void removePurgedBeansFromSessionAndApplication(RuntimeConfig runtimeConfig)
    {
        Map oldManagedBeans = runtimeConfig.getManagedBeansNotReaddedAfterPurge();
        if(oldManagedBeans!=null) {
            Iterator it=oldManagedBeans.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ManagedBean bean = (ManagedBean) entry.getValue();

                String scope = bean.getManagedBeanScope();

                if(scope!=null && scope.equalsIgnoreCase("session")) {
                    _externalContext.getSessionMap().remove(entry.getKey());
                }
                else if(scope!=null && scope.equalsIgnoreCase("application")) {
                    _externalContext.getApplicationMap().remove(entry.getKey());
                }
            }
        }
        runtimeConfig.resetManagedBeansNotReaddedAfterPurge();
    }

    private void configureRenderKits()
    {
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder
                .getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        FacesConfigDispenser dispenser = getDispenser();
        for (Iterator iterator = dispenser.getRenderKitIds(); iterator.hasNext();)
        {
            String renderKitId = (String) iterator.next();
            String renderKitClass = dispenser.getRenderKitClass(renderKitId);

            if (renderKitClass == null)
            {
                renderKitClass = DEFAULT_RENDER_KIT_CLASS;
            }

            RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);

            for (Iterator renderers = dispenser.getRenderers(renderKitId); renderers.hasNext();)
            {
                Renderer element = (Renderer) renderers.next();
                javax.faces.render.Renderer renderer;
                try
                {
                    renderer = (javax.faces.render.Renderer) ClassUtils.newInstance(element.getRendererClass());
                }
                catch (Throwable e)
                {
                    // ignore the failure so that the render kit is configured
                    log.error("failed to configure class " + element.getRendererClass(), e);
                    continue;
                }

               renderKit.addRenderer(element.getComponentFamily(), element.getRendererType(), renderer);
            }

            renderKitFactory.addRenderKit(renderKitId, renderKit);
        }
    }

    private void configureLifecycle()
    {
        // create the lifecycle used by the app
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());

        // add phase listeners
        for (Iterator iterator = getDispenser().getLifecyclePhaseListeners(); iterator.hasNext();)
        {
            String listenerClassName = (String) iterator.next();
            try
            {
                lifecycle.addPhaseListener((PhaseListener) ClassUtils.newInstance(listenerClassName));
            }
            catch (ClassCastException e)
            {
                log.error("Class " + listenerClassName + " does not implement PhaseListener");
            }
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

    public static class VersionInfo
    {
        private String packageName;
        private List<JarInfo> jarInfos;

        public VersionInfo(String packageName)
        {

            this.packageName = packageName;
        }

        public String getPackageName()
        {
            return packageName;
        }

        public void addJarInfo(String path, String version)
        {
            if (jarInfos == null)
            {
                jarInfos = new ArrayList<JarInfo>();
            }

            jarInfos.add(new JarInfo(path, version));
        }

        public String getLastVersion()
        {
            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;

            return jarInfos.get(jarInfos.size() - 1).getVersion();
        }

        /**
         * Probably, the first encountered version will be used.
         * 
         * @return probably used version
         */
        public String getUsedVersion()
        {

            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;

            return jarInfos.get(0).getVersion();
        }

        /**
         * Probably, the first encountered version will be used.
         * 
         * @return probably used classpath
         */
        public String getUsedVersionPath()
        {

            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;

            return jarInfos.get(0).getUrl();

        }
    }

    public static class JarInfo
    {
        private String url;
        private String version;

        public JarInfo(String url, String version)
        {
            this.url = url;
            this.version = version;
        }

        public String getVersion()
        {
            return version;
        }

        public String getUrl()
        {
            return url;
        }
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
                log.error("Make sure '" + serialProvider + "' implements the correct interface", e);
            }
            catch (Exception e)
            {
                log.error(e);
            }
            finally
            {
                if (serialFactory == null)
                {
                    serialFactory = new DefaultSerialFactory();
                    log.error("Using default serialization provider");
                }
            }

        }

        log.info("Serialization provider : " + serialFactory.getClass());
        _externalContext.getApplicationMap().put(StateUtils.SERIAL_FACTORY, serialFactory);

    }

}
