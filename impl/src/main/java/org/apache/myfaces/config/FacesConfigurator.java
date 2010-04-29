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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String MYFACES_TOMAHAWK12_PACKAGE_NAME = "tomahawk12";
    public static final String MYFACES_ORCHESTRA_PACKAGE_NAME = "myfaces-orchestra-core";
    public static final String MYFACES_ORCHESTRA12_PACKAGE_NAME = "myfaces-orchestra-core12";
    public static final String MYFACES_TRINIDAD_API_PACKAGE_NAME = "trinidad-api";
    public static final String MYFACES_TRINIDAD_IMPL_PACKAGE_NAME = "trinidad-impl";
    public static final String MYFACES_TOBAGO_PACKAGE_NAME = "tobago";
    public static final String MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME = "tomahawk-sandbox";
    public static final String MYFACES_TOMAHAWK_SANDBOX12_PACKAGE_NAME = "tomahawk-sandbox12";
    public static final String MYFACES_TOMAHAWK_SANDBOX15_PACKAGE_NAME = "tomahawk-sandbox15";
    public static final String COMMONS_EL_PACKAGE_NAME = "commons-el";
    public static final String JSP_API_PACKAGE_NAME = "jsp-api";
    
    private static final String[] ARTIFACTS_IDS = 
        { 
            MYFACES_API_PACKAGE_NAME, MYFACES_IMPL_PACKAGE_NAME,
            MYFACES_TOMAHAWK_PACKAGE_NAME, MYFACES_TOMAHAWK12_PACKAGE_NAME,
            MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME, MYFACES_TOMAHAWK_SANDBOX12_PACKAGE_NAME,
            MYFACES_TOMAHAWK_SANDBOX15_PACKAGE_NAME,
            MYFACES_ORCHESTRA_PACKAGE_NAME, MYFACES_ORCHESTRA12_PACKAGE_NAME,
            MYFACES_TRINIDAD_API_PACKAGE_NAME, MYFACES_TRINIDAD_IMPL_PACKAGE_NAME,
            MYFACES_TOBAGO_PACKAGE_NAME, 
            COMMONS_EL_PACKAGE_NAME, JSP_API_PACKAGE_NAME
        };
    
    /**
     * Regular expression used to extract the jar information from the 
     * files present in the classpath.
     * <p>The groups found with the regular expression are:</p>
     * <ul>
     *   <li>Group 6: file path (required)</li>
     *   <li>Group 7: artifact id (required)</li>
     *   <li>Group 8: major version (required)</li>
     *   <li>Group 10: minor version (optional)</li>
     *   <li>Group 12: maintenance version (optional)</li>
     *   <li>Group 14: extra version (optional)</li>
     *   <li>Group 15: SNAPSHOT marker (optional)</li>
     * </ul>
     */
    public static final String REGEX_LIBRARY = "((jar)?(besjar)?(wsjar)?(zip)?)?:(file:.*/(.+)-" +
            "(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+))?)?)?(-SNAPSHOT)?" +
            "\\.jar)!/META-INF/MANIFEST.MF";
    private static final Pattern REGEX_LIBRARY_PATTERN = Pattern.compile(REGEX_LIBRARY);
    private static final int REGEX_LIBRARY_FILE_PATH = 6;
    private static final int REGEX_LIBRARY_ARTIFACT_ID = 7;
    private static final int REGEX_LIBRARY_MAJOR_VERSION = 8;
    private static final int REGEX_LIBRARY_MINOR_VERSION = 10;
    private static final int REGEX_LIBRARY_MAINTENANCE_VERSION = 12;
    private static final int REGEX_LIBRARY_EXTRA_VERSION = 14;
    private static final int REGEX_LIBRARY_SNAPSHOT_MARKER = 15;

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
            log.error("Could not read resource " + resource, e);
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


        List<String> configFilesList = getConfigFilesList();

        for (String systemId : configFilesList)
        {
            resModified = getResourceLastModified(systemId);
            if (resModified > lastModified)
            {
                lastModified = resModified;
            }

        }

        return lastModified;
    }

    public void update() {
        long refreshPeriod = (MyfacesConfig.getCurrentInstance(_externalContext).getConfigRefreshPeriod()) * 1000;

        if (refreshPeriod > 0) {
            long ttl = lastUpdate + refreshPeriod;
            if ((System.currentTimeMillis() > ttl) && (getLastModifiedTime() > ttl)) {
                try {
                    purgeConfiguration();
                }
                catch (NoSuchMethodException e) {
                    log.error("Configuration objects do not support clean-up. Update aborted");

                    // We still want to update the timestamp to avoid running purge on every subsequent
                    // request after this one.
                    //
                    lastUpdate = System.currentTimeMillis();

                    return;
                }
                catch (IllegalAccessException e) {
                    log.fatal("Error during configuration clean-up" + e.getMessage());
                }
                catch (InvocationTargetException e) {
                    log.fatal("Error during configuration clean-up" + e.getMessage());
                }
                configure();
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
    @SuppressWarnings("unchecked")
    protected void logMetaInf()
    {
        try
        {
            Map<String, List<JarInfo>> libs = new HashMap<String, List<JarInfo>>(30);

            Iterator<URL> it = ClassUtils.getResources("META-INF/MANIFEST.MF", this);
            while (it.hasNext())
            {
                URL url = it.next();
                Matcher matcher = REGEX_LIBRARY_PATTERN.matcher(url.toString());
                if (matcher.matches())
                {
                    // We have a valid JAR
                    String artifactId = matcher.group(REGEX_LIBRARY_ARTIFACT_ID);
                    List<JarInfo> versions = libs.get(artifactId);
                    if (versions == null)
                    {
                        versions = new ArrayList<JarInfo>(2);
                        libs.put(artifactId, versions);
                    }

                    String path = matcher.group(REGEX_LIBRARY_FILE_PATH);

                    Version version = new Version(matcher.group(REGEX_LIBRARY_MAJOR_VERSION), 
                            matcher.group(REGEX_LIBRARY_MINOR_VERSION), 
                            matcher.group(REGEX_LIBRARY_MAINTENANCE_VERSION),
                            matcher.group(REGEX_LIBRARY_EXTRA_VERSION), 
                            matcher.group(REGEX_LIBRARY_SNAPSHOT_MARKER));

                    JarInfo newInfo = new JarInfo(path, version);
                    if (!versions.contains(newInfo))
                    {
                        versions.add(newInfo);
                    }
                }
            }

            if (log.isInfoEnabled())
            {
                if (log.isWarnEnabled())
                {
                    for (String artifactId : ARTIFACTS_IDS)
                    {
                        List<JarInfo> versions = libs.get(artifactId);
                        if (versions != null && versions.size() > 1)
                        {
                            StringBuilder builder = new StringBuilder(1024);
                            builder.append("You are using the library: ");
                            builder.append(artifactId);
                            builder.append(" in different versions; first (and probably used) version is: ");
                            builder.append(versions.get(0).getVersion());
                            builder.append(" loaded from: ");
                            builder.append(versions.get(0).getUrl());
                            builder.append(", but also found the following versions: ");

                            boolean needComma = false;
                            for (int i = 1; i < versions.size(); i++)
                            {
                                JarInfo info = versions.get(i);
                                if (needComma)
                                {
                                    builder.append(", ");
                                }

                                builder.append(info.getVersion());
                                builder.append(" loaded from: ");
                                builder.append(info.getUrl());

                                needComma = true;
                            }

                            log.warn(builder.toString());
                        }
                    }
                }

                for (String artifactId : ARTIFACTS_IDS)
                {
                    startLib(artifactId, libs);
                }
            }
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }

    /**
     * This method performs part of the factory search outlined in section 10.2.6.1.
     */
    protected void feedMetaInfServicesFactories()
    {
        try
        {
            for (String factoryName : FACTORY_NAMES)
            {
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
                    } finally {
                        if (br != null)
                        {
                            br.close();
                        }
                        if (isr != null) {
                            isr.close();
                        }
                        if (stream != null) {
                            stream.close();
                        }
                    }


                    if (log.isInfoEnabled())
                    {
                        log.info("Found " + factoryName + " factory implementation: " + className);
                    }

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

            for (Map.Entry<String, URL> entry : facesConfigs.entrySet())
            {
                InputStream stream = null;
                try
                {
                    stream = openStreamWithoutCache(entry.getValue());
                    if (log.isInfoEnabled())
                    {
                        log.info("Reading config : " + entry.getKey());
                    }
                    getDispenser().feed(getUnmarshaller().getFacesConfig(stream, entry.getKey()));
                }
                finally
                {
                    if (stream != null)
                    {
                        stream.close();
                    }
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
        List<String> configFilesList = getConfigFilesList();
        for (String systemId : configFilesList)
        {
            InputStream stream = _externalContext.getResourceAsStream(systemId);
            if (stream == null)
            {
                log.error("Faces config resource " + systemId + " not found");
                continue;
            }

            if (log.isInfoEnabled())
            {
                log.info("Reading config " + systemId);
            }
            getDispenser().feed(getUnmarshaller().getFacesConfig(stream, systemId));
            stream.close();
        }
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
    
    private void startLib(String artifactId, Map<String, List<JarInfo>> libs)
    {
        List<JarInfo> versions = libs.get(artifactId);
        if (versions == null)
        {
            log.info("MyFaces-package : " + artifactId + " not found.");
        }
        else
        {
            JarInfo info = versions.get(0);
            log.info("Starting up MyFaces-package : " + artifactId + " in version : "
                     + info.getVersion() + " from path : " + info.getUrl());
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
/*
    public static class VersionInfo
    {
        private String artifactId;
        private List<JarInfo> jarInfos;

        public VersionInfo(String artifactId)
        {
            this.artifactId = artifactId;
        }

        public String getArtifactId()
        {
            return packageName;
        }

        public void addJarInfo(Matcher matcher)
        {
            if (jarInfos == null)
            {
                jarInfos = new ArrayList<JarInfo>();
            }
            
            String path = matcher.group(1);
            
            Version version = new Version(matcher.group(3), matcher.group(5), 
                                          matcher.group(7), matcher.group(9),
                                          matcher.group(10));

            jarInfos.add(new JarInfo(path, version));
        }

        public String getLastVersion()
        {
            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;

            return "";
            //return jarInfos.get(jarInfos.size() - 1).getVersion();
        }

        /**
         * Probably, the first encountered version will be used.
         * 
         * @return probably used version
         *
        public String getUsedVersion()
        {

            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;
            return "";
            //return jarInfos.get(0).getVersion();
        }

        /**
         * Probably, the first encountered version will be used.
         * 
         * @return probably used classpath
         *
        public String getUsedVersionPath()
        {

            if (jarInfos == null)
                return null;
            if (jarInfos.size() == 0)
                return null;

            return jarInfos.get(0).getUrl();

        }
    }
*/
    private static class JarInfo implements Comparable<JarInfo>
    {
        private String url;
        private Version version;

        public JarInfo(String url, Version version)
        {
            this.url = url;
            this.version = version;
        }

        public Version getVersion()
        {
            return version;
        }

        public String getUrl()
        {
            return url;
        }

        public int compareTo(JarInfo info)
        {
            return version.compareTo(info.version);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            else if (o instanceof JarInfo)
            {
                JarInfo other = (JarInfo)o;
                return version.equals(other.version);
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public int hashCode()
        {
            return version.hashCode();
        }
    }
    
    static class Version implements Comparable<Version>
    {
        // we have to use Long here, because the version number
        // could be something like 20060714150240 and this value
        // exceeds an Integer (see MYFACES-2686)
        private Long[] parts;
        
        private boolean snapshot;
        
        public Version(String major, String minor, String maintenance,
                       String extra, String snapshot)
        {
            parts = new Long[4];
            parts[0] = Long.valueOf(major);
            
            if (minor != null)
            {
                parts[1] = Long.valueOf(minor);
                
                if (maintenance != null)
                {
                    parts[2] = Long.valueOf(maintenance);
                    
                    if (extra != null)
                    {
                        parts[3] = Long.valueOf(extra);
                    }
                }
            }
            
            this.snapshot = snapshot != null;
        }

        public int compareTo(Version v)
        {
            for (int i = 0; i < parts.length; i++)
            {
                Long left = parts[i];
                Long right = v.parts[i];
                if (left == null)
                {
                    if (right == null)
                    {
                        break;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    if (right == null)
                    {
                        return 1;
                    }
                    else if (left < right)
                    {
                        return -1;
                    }
                    else if (left > right)
                    {
                        return 1;
                    }
                }
            }
            
            if (snapshot)
            {
                return v.snapshot ? 0 : -1;
            }
            else
            {
                return v.snapshot ? 1 : 0;
            }
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            else if (o instanceof Version)
            {
                Version other = (Version)o;
                if (snapshot != other.snapshot)
                {
                    return false;
                }
                
                for (int i = 0; i < parts.length; i++)
                {
                    Long thisPart = parts[i];
                    Long otherPart = other.parts[i];
                    if (thisPart == null ? otherPart != null : !thisPart.equals(otherPart))
                    {
                        return false;
                    }
                }

                return true;
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public int hashCode()
        {
            int hash = 0;
            for (int i = 0; i < parts.length; i++)
            {
                if (parts[i] != null)
                {
                    hash ^= parts[i].hashCode();
                }
            }
            
            hash ^= Boolean.valueOf(snapshot).hashCode();
            
            return hash;
        }
        
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(parts[0]);
            for (int i = 1; i < parts.length; i++)
            {
                Long val = parts[i];
                if (val != null)
                {
                    builder.append('.').append(val);
                }
            }
            
            if (snapshot)
            {
                builder.append("-SNAPSHOT");
            }
            
            return builder.toString();
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
