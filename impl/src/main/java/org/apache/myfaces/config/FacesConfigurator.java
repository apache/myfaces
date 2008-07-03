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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.context.FacesContextFactoryImpl;
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

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.*;
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
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


/**
 * Configures everything for a given context.
 * The FacesConfigurator is independent of the concrete implementations that lie
 * behind FacesConfigUnmarshaller and FacesConfigDispenser.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesConfigurator
{
    private static final Log log = LogFactory.getLog(FacesConfigurator.class);

    private static final String STANDARD_FACES_CONFIG_RESOURCE
        = "org.apache.myfaces.resource".replace('.', '/') + "/standard-faces-config.xml";
    private static final String FACES_CONFIG_RESOURCE = "META-INF/faces-config.xml";

    private static final String META_INF_SERVICES_RESOURCE_PREFIX = "META-INF/services/";

    private static final String DEFAULT_RENDER_KIT_CLASS = HtmlRenderKitImpl.class.getName();
    private static final String DEFAULT_APPLICATION_FACTORY = ApplicationFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONTEXT_FACTORY = FacesContextFactoryImpl.class.getName();
    private static final String DEFAULT_LIFECYCLE_FACTORY = LifecycleFactoryImpl.class.getName();
    private static final String DEFAULT_RENDER_KIT_FACTORY = RenderKitFactoryImpl.class.getName();
    private static final String DEFAULT_FACES_CONFIG = "/WEB-INF/faces-config.xml";

    private static final Set FACTORY_NAMES  = new HashSet();
    {
        FACTORY_NAMES.add(FactoryFinder.APPLICATION_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.FACES_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.LIFECYCLE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.RENDER_KIT_FACTORY);
    }


    private ExternalContext _externalContext;
    private FacesConfigUnmarshaller _unmarshaller;
    private FacesConfigDispenser _dispenser;
    private static final String JAR_EXTENSION = ".jar";
    private static final String META_INF_MANIFEST_SUFFIX = "!/META-INF/MANIFEST.MF";
    private static final String JAR_PREFIX = "jar:";

    private static long lastUpdate;

    public static final String MYFACES_API_PACKAGE_NAME = "myfaces-api";
    public static final String MYFACES_IMPL_PACKAGE_NAME = "myfaces-impl";
    public static final String MYFACES_TOMAHAWK_PACKAGE_NAME = "tomahawk";
    public static final String MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME = "tomahawk-sandbox";
    public static final String COMMONS_EL_PACKAGE_NAME = "commons-el";
    public static final String JSP_API_PACKAGE_NAME = "jsp-api";

    public FacesConfigurator(ExternalContext externalContext)
    {
        _externalContext = externalContext;

    }

    private long getResourceLastModified(String resource){
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

    private long getLastModifiedTime(){
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

    public void update(){
        long refreshPeriod = (MyfacesConfig.getCurrentInstance(_externalContext).getConfigRefreshPeriod())*1000;

        if (refreshPeriod > 0){
            long ttl = lastUpdate + refreshPeriod;
            if ((System.currentTimeMillis() > ttl) && (getLastModifiedTime() > ttl)) {
                log.info("Faces config-files are being reloaded. If you don't want this reload to happen (e.g. in production), set the web-xml-parameter: "+MyfacesConfig.INIT_PARAM_CONFIG_REFRESH_PERIOD +" to -1.");
                purgeConfiguration();
                configure();
            }
        }
    }

    private void purgeConfiguration() {

        try {
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
        } catch (NoSuchMethodException e) {
            log.error("Configuration objects do not support clean-up. Update aborted",e);
            return;
        } catch (IllegalAccessException e) {
            log.fatal("Error during configuration clean-up" + e.getMessage(),e);
        } catch (InvocationTargetException e) {
            log.fatal("Error during configuration clean-up" + e.getMessage(),e);
        }
    }

    public void configure()
        throws FacesException
    {
        //These two classes can be easily replaced by alternative implementations.
        //As long as there is no need to switch implementations we need no
        //factory pattern to create them.
        _unmarshaller = new DigesterFacesConfigUnmarshallerImpl(_externalContext);
        _dispenser = new DigesterFacesConfigDispenserImpl();

        try
        {
            feedStandardConfig();
            feedMetaInfServicesFactories();
            //feedJarFileConfigurations();
            feedClassloaderConfigurations();
            feedContextSpecifiedConfig();
            feedWebAppConfig();

            if(log.isInfoEnabled())
            {
                logMetaInf();
            }
        } catch (IOException e)
        {
            throw new FacesException(e);
        } catch (SAXException e)
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
        if (stream == null) throw new FacesException("Standard faces config " + STANDARD_FACES_CONFIG_RESOURCE + " not found");
        if (log.isInfoEnabled()) log.info("Reading standard config " + STANDARD_FACES_CONFIG_RESOURCE);
        _dispenser.feed(_unmarshaller.getFacesConfig(stream, STANDARD_FACES_CONFIG_RESOURCE));
        stream.close();
    }

    /**
     * This method performs part of the factory search outlined in section 10.2.6.1.
     */
    protected void logMetaInf()
    {
        try
        {
            List li = new ArrayList();
            li.add(new VersionInfo(MYFACES_API_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_IMPL_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_TOMAHAWK_SANDBOX_PACKAGE_NAME));
            li.add(new VersionInfo(MYFACES_TOMAHAWK_PACKAGE_NAME));

            Iterator it = ClassUtils.getResources("META-INF/MANIFEST.MF",
                                                  this);
            while (it.hasNext())
            {
                URL url = (URL)it.next();

                for (int i = 0; i < li.size(); i++)
                {
                    VersionInfo versionInfo = (VersionInfo) li.get(i);
                    if(checkJar(versionInfo, url))
                        break;
                }
            }

            for (int i = 0; i < li.size(); i++)
            {
                VersionInfo versionInfo = (VersionInfo) li.get(i);

                if(versionInfo.getUsedVersion()!=null)
                {
                    if(log.isInfoEnabled())
                    {
                        log.info("Starting up MyFaces-package : "+versionInfo.getPackageName()+" in version : "
                                +versionInfo.getUsedVersion()+" from path : "+versionInfo.getUsedVersionPath());
                    }
                }
                else
                {
                    if(log.isInfoEnabled())
                    {
                        log.info("MyFaces-package : "+versionInfo.getPackageName()+" not found.");
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

        if(!pathString.startsWith(JAR_PREFIX))
            return false;

        if(!(pathString.length()>(META_INF_MANIFEST_SUFFIX.length()+JAR_PREFIX.length())))
        {
            if(log.isDebugEnabled())
                log.debug("PathString : "+pathString+" not long enough to be parsed.");
            return false;
        }

        pathString = pathString.substring(JAR_PREFIX.length(),pathString.length()-META_INF_MANIFEST_SUFFIX.length());

        File file = new File(pathString);

        String fileName = file.getName();

        if(fileName.endsWith(JAR_EXTENSION) && ((index=fileName.indexOf(versionInfo.getPackageName()))!=-1))
        {
            int beginIndex = index+versionInfo.getPackageName().length()+1;

            if(beginIndex > fileName.length()-1)
            {
                log.debug("beginIndex out of bounds. fileName: "+fileName);
                return false;
            }

            int endIndex = fileName.length()-JAR_EXTENSION.length();

            if(endIndex<0 || endIndex<=beginIndex)
            {
                log.debug("endIndex out of bounds. fileName: "+fileName);
                return false;
            }

            String newVersion = fileName.substring(beginIndex, endIndex);

            if(version == null)
            {
                versionInfo.addJarInfo(pathString,newVersion);
           }
            else if(version.equals(newVersion))
            {
                versionInfo.addJarInfo(pathString, version);
            }
            else
            {
                log.error("You are using the MyFaces-package : "+versionInfo.getPackageName() +
                        " in different versions; first (and probably used) version is : "+versionInfo.getUsedVersion() +", currently encountered version is : "+newVersion+
                        ". This will cause undesired behaviour. Please clean out your class-path." +
                        " The first encountered version is loaded from : "+versionInfo.getUsedVersionPath()+". The currently encountered version is loaded from : "+
                        path);
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
            for (Iterator iterator = FACTORY_NAMES.iterator(); iterator.hasNext();)
            {
                String factoryName = (String)iterator.next();
                Iterator it = ClassUtils.getResources(META_INF_SERVICES_RESOURCE_PREFIX + factoryName,
                                                      this);
                while (it.hasNext())
                {
                    URL url = (URL)it.next();
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
                        throw new FacesException("Unable to read class name from file "
                                                 + url.toExternalForm(), e);
                    }
                    br.close();
                    isr.close();
                    stream.close();

                    if (log.isInfoEnabled()) log.info("Found " + factoryName + " factory implementation: " + className);

                    if (factoryName.equals(FactoryFinder.APPLICATION_FACTORY))
                    {
                        _dispenser.feedApplicationFactory(className);
                    } else if (factoryName.equals(FactoryFinder.FACES_CONTEXT_FACTORY))
                    {
                        _dispenser.feedFacesContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.LIFECYCLE_FACTORY))
                    {
                        _dispenser.feedLifecycleFactory(className);
                    } else if (factoryName.equals(FactoryFinder.RENDER_KIT_FACTORY))
                    {
                        _dispenser.feedRenderKitFactory(className);
                    } else
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

    private InputStream openStreamWithoutCache(URL url)
            throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }

    /*private Map expandFactoryNames(Set factoryNames)
   {
       Map names = new HashMap();
       Iterator itr = factoryNames.iterator();
       while (itr.hasNext())
       {
           String name = (String) itr.next();
           names.put(META_INF_SERVICES_LOCATION + name, name);
       }
       return names;
   } */


    /**
     * This method fixes MYFACES-208
     */
    private void feedClassloaderConfigurations()
    {
        try
        {
            Iterator it = ClassUtils.getResources(FACES_CONFIG_RESOURCE, this);
            while (it.hasNext())
            {
                URL url = (URL)it.next();
                InputStream stream = openStreamWithoutCache(url);
                String systemId = url.toExternalForm();
                if (log.isInfoEnabled()) log.info("Reading config " + systemId);
                _dispenser.feed(_unmarshaller.getFacesConfig(stream, systemId));
                stream.close();
            }
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }


    /*
     * To make this easier AND to fix MYFACES-208 at the same time, this method is replaced by
     * {@link FacesConfigurator#feedClassloaderConfigurations}
     *
     * @deprecated {@link FacesConfigurator#feedClassloaderConfigurations} replaces this one

    private void feedJarFileConfigurations()
    {
        Set jars = _externalContext.getResourcePaths("/WEB-INF/lib/");
        if (jars != null)
        {
            for (Iterator it = jars.iterator(); it.hasNext();)
            {
                String path = (String) it.next();
                if (path.toLowerCase().endsWith(".jar"))
                {
                    feedJarConfig(path);
                }
            }
        }
    }

    private void feedJarConfig(String jarPath)
        throws FacesException
    {
        try
        {
            // not all containers expand archives, so we have to do it the generic way:
            // 1. get the stream from external context
            InputStream in = _externalContext.getResourceAsStream(jarPath);
            if (in == null)
            {
                if (jarPath.startsWith("/"))
                {
                    in = _externalContext.getResourceAsStream(jarPath.substring(1));
                } else
                {
                    in = _externalContext.getResourceAsStream("/" + jarPath);
                }
            }
            if (in == null)
            {
                log.error("Resource " + jarPath + " not found");
                return;
            }

            // 2. search the jar stream for META-INF/faces-config.xml
            JarInputStream jar = new JarInputStream(in);
            JarEntry entry = jar.getNextJarEntry();
            boolean found = false;

            while (entry != null)
            {
                if (entry.getName().equals(FACES_CONFIG_RESOURCE))
                {
                    if (log.isDebugEnabled()) log.debug("faces-config.xml found in " + jarPath);
                    found = true;
                    break;
                }
                entry = jar.getNextJarEntry();
            }
            jar.close();

            File tmp;

            // 3. if faces-config.xml was found, extract the jar and copy it to a temp file; hand over the temp file
            // to the parser and delete it afterwards
            if (found)
            {
                tmp = File.createTempFile("myfaces", ".jar");
                tmp.deleteOnExit(); // just to make sure the file will be deleted

                in = _externalContext.getResourceAsStream(jarPath);
                FileOutputStream out = new FileOutputStream(tmp);
                byte[] buffer = new byte[4096];
                int r;

                while ((r = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, r);
                }
                out.close();

                JarFile jarFile = new JarFile(tmp);
                try
                {
                    JarEntry configFile = jarFile.getJarEntry(FACES_CONFIG_RESOURCE);
                    if (configFile != null)
                    {
                        if (log.isInfoEnabled()) log.info("faces-config.xml found in jar " + jarPath);
                        InputStream stream = jarFile.getInputStream(configFile);
                        String systemId = "jar:" + tmp.toURL() + "!/" + configFile.getName();
                        if (log.isDebugEnabled()) log.debug("Reading config " + systemId);
                        _dispenser.feed(_unmarshaller.getFacesConfig(stream, systemId));
                        stream.close();
                    }
                } finally
                {
                    jarFile.close();
                    tmp.delete();
                }
            } else
            {
                if (log.isDebugEnabled()) log.debug("Jar " + jarPath + " contains no faces-config.xml");
            }
        } catch (Exception e)
        {
            throw new FacesException(e);
        }
    }
     */

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
            _dispenser.feed(_unmarshaller.getFacesConfig(stream, systemId));
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
        //web application config
        InputStream stream = _externalContext.getResourceAsStream(DEFAULT_FACES_CONFIG);
        if (stream != null)
        {
            if (log.isInfoEnabled()) log.info("Reading config /WEB-INF/faces-config.xml");
            _dispenser.feed(_unmarshaller.getFacesConfig(stream, DEFAULT_FACES_CONFIG));
            stream.close();
        }
    }


    private void configureFactories()
    {
        setFactories(FactoryFinder.APPLICATION_FACTORY, _dispenser.getApplicationFactoryIterator(), DEFAULT_APPLICATION_FACTORY);
        setFactories(FactoryFinder.FACES_CONTEXT_FACTORY, _dispenser.getFacesContextFactoryIterator(), DEFAULT_FACES_CONTEXT_FACTORY);
        setFactories(FactoryFinder.LIFECYCLE_FACTORY, _dispenser.getLifecycleFactoryIterator(), DEFAULT_LIFECYCLE_FACTORY);
        setFactories(FactoryFinder.RENDER_KIT_FACTORY, _dispenser.getRenderKitFactoryIterator(), DEFAULT_RENDER_KIT_FACTORY);
    }


    private void setFactories(String factoryName, Iterator factories, String defaultFactory)
    {
        FactoryFinder.setFactory(factoryName, defaultFactory);
        while (factories.hasNext())
        {
            FactoryFinder.setFactory(factoryName, (String) factories.next());
        }
    }


    private void configureApplication()
    {
        Application application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();
        application.setActionListener((ActionListener) getApplicationObject(ActionListener.class, _dispenser.getActionListenerIterator(), null));

        if (_dispenser.getDefaultLocale() != null)
        {
            application.setDefaultLocale(
                LocaleUtils.toLocale(_dispenser.getDefaultLocale()));
        }

        if (_dispenser.getDefaultRenderKitId() != null)
        {
            application.setDefaultRenderKitId(_dispenser.getDefaultRenderKitId());
        }

        if (_dispenser.getMessageBundle() != null)
        {
            application.setMessageBundle(_dispenser.getMessageBundle());
        }

        application.setNavigationHandler((NavigationHandler) getApplicationObject(NavigationHandler.class,
                                                                                  _dispenser.getNavigationHandlerIterator(), application.getNavigationHandler()));
        application.setPropertyResolver((PropertyResolver) getApplicationObject(PropertyResolver.class,
                                                                                _dispenser.getPropertyResolverIterator(), application.getPropertyResolver()));
        application.setStateManager((StateManager) getApplicationObject(StateManager.class,
                                                                        _dispenser.getStateManagerIterator(), application.getStateManager()));
        List locales = new ArrayList();
        for (Iterator it = _dispenser.getSupportedLocalesIterator(); it.hasNext();)
        {
            locales.add(LocaleUtils.toLocale((String) it.next()));
        }
        application.setSupportedLocales(locales);

        application.setVariableResolver((VariableResolver) getApplicationObject(VariableResolver.class,
                                                                                _dispenser.getVariableResolverIterator(), application.getVariableResolver()));
        application.setViewHandler((ViewHandler) getApplicationObject(ViewHandler.class,
                                                                      _dispenser.getViewHandlerIterator(), application.getViewHandler()));

        for (Iterator it = _dispenser.getComponentTypes(); it.hasNext();)
        {
            String componentType = (String) it.next();
            application.addComponent(componentType,
                                     _dispenser.getComponentClass(componentType));
        }

        for (Iterator it = _dispenser.getConverterIds(); it.hasNext();)
        {
            String converterId = (String) it.next();
            application.addConverter(converterId,
                                     _dispenser.getConverterClassById(converterId));
        }

        for (Iterator it = _dispenser.getConverterClasses(); it.hasNext();)
        {
            String converterClass = (String) it.next();
            try
            {
                application.addConverter(ClassUtils.simpleClassForName(converterClass),
                                         _dispenser.getConverterClassByClass(converterClass));
            }
            catch(Exception ex)
            {
                log.error("Converter could not be added. Reason:",ex);
            }
        }

        if(application instanceof ApplicationImpl)
        {
            for (Iterator it = _dispenser.getConverterConfigurationByClassName(); it.hasNext();)
            {
                String converterClassName = (String) it.next();

                ((ApplicationImpl) application).addConverterConfiguration(converterClassName,
                                                                          _dispenser.getConverterConfiguration(converterClassName));
            }
        }

        for (Iterator it = _dispenser.getValidatorIds(); it.hasNext();)
        {
            String validatorId = (String) it.next();
            application.addValidator(validatorId,
                                     _dispenser.getValidatorClass(validatorId));
        }
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
            } else
            {
                // let's check if class supports the decorator pattern
                try
                {
                    Constructor delegationConstructor = implClass.getConstructor(new Class[]{interfaceClass});
                    // impl class supports decorator pattern,
                    try
                    {
                        // create new decorator wrapping current
                        current = delegationConstructor.newInstance(new Object[]{current});
                    } catch (InstantiationException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    } catch (IllegalAccessException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    } catch (InvocationTargetException e)
                    {
                        log.error(e.getMessage(), e);
                        throw new FacesException(e);
                    }
                } catch (NoSuchMethodException e)
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

        for (Iterator iterator = _dispenser.getManagedBeans(); iterator.hasNext();)
        {
            ManagedBean bean = (ManagedBean) iterator.next();
            ManagedBean oldBean = runtimeConfig.getManagedBean(bean.getManagedBeanName());

            if(log.isWarnEnabled() && oldBean != null)
                log.warn("More than one managed bean w/ the name of '" 
                        + bean.getManagedBeanName() + "' registered. First managed bean was registered in :" +
                            oldBean.getConfigLocation()+", new managed bean was registered in : "+
                            bean.getConfigLocation()+". The first definition of the managed-bean will be ignored by the standard MyFaces variable resolver!");

            runtimeConfig.addManagedBean(bean.getManagedBeanName(), bean);

        }

        removePurgedBeansFromSessionAndApplication(runtimeConfig);

        for (Iterator iterator = _dispenser.getNavigationRules(); iterator.hasNext();)
        {
            NavigationRule rule = (NavigationRule) iterator.next();
            runtimeConfig.addNavigationRule(rule);

        }
    }

    private void removePurgedBeansFromSessionAndApplication(RuntimeConfig runtimeConfig) {
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
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        for (Iterator iterator = _dispenser.getRenderKitIds(); iterator.hasNext();)
        {
            String renderKitId = (String) iterator.next();
            String renderKitClass = _dispenser.getRenderKitClass(renderKitId);

            if (renderKitClass == null)
            {
                renderKitClass = DEFAULT_RENDER_KIT_CLASS;
            }

            RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);

            for (Iterator renderers = _dispenser.getRenderers(renderKitId); renderers.hasNext();)
            {
                Renderer element = (Renderer) renderers.next();
                javax.faces.render.Renderer renderer;
                try {
                  renderer = (javax.faces.render.Renderer) ClassUtils.newInstance(element.getRendererClass());
                } catch(Throwable e) {
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
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(getLifecycleId());

        // add phase listeners
        for (Iterator iterator = _dispenser.getLifecyclePhaseListeners(); iterator.hasNext();)
        {
            String listenerClassName = (String) iterator.next();
            try
            {
                lifecycle.addPhaseListener((PhaseListener) ClassUtils.newInstance(listenerClassName));
            } catch (ClassCastException e)
            {
                log.error("Class " + listenerClassName + " does not implement PhaseListener",e);
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
        private List jarInfos;

        public VersionInfo(String packageName)
        {

            this.packageName = packageName;
        }

        public String getPackageName()
        {
            return packageName;
        }

        public void setPackageName(String packageName)
        {
            this.packageName = packageName;
        }

        public void addJarInfo(String path, String version)
        {
            if(jarInfos == null)
            {
                jarInfos = new ArrayList();
            }

            jarInfos.add(new JarInfo(path, version));
        }

        public String getLastVersion()
        {
            if(jarInfos == null)
                return null;
            if(jarInfos.size()==0)
                return null;

            return ((JarInfo) jarInfos.get(jarInfos.size()-1)).getVersion();
        }

        /**Probably, the first encountered version will be used.
         *
         * @return probably used version
         */
        public String getUsedVersion()
        {

            if(jarInfos == null)
                return null;
            if(jarInfos.size()==0)
                return null;

            return ((JarInfo) jarInfos.get(0)).getVersion();
        }

        /**Probably, the first encountered version will be used.
         *
         * @return probably used classpath
         */
        public String getUsedVersionPath()
        {

            if(jarInfos == null)
                return null;
            if(jarInfos.size()==0)
                return null;

            return ((JarInfo) jarInfos.get(0)).getUrl();

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

        public void setVersion(String version)
        {
            this.version = version;
        }

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }


    private void handleSerialFactory(){

        String serialProvider = _externalContext.getInitParameter(StateUtils.SERIAL_FACTORY);
        SerialFactory serialFactory = null;

        if(serialProvider == null)
        {
            serialFactory = new DefaultSerialFactory();
        }
        else
        {
            try
            {
                serialFactory = (SerialFactory) ClassUtils.newInstance(serialProvider);

            }catch(ClassCastException e){
                log.error("Make sure '" + serialProvider +
                        "' implements the correct interface", e);
            }
            catch(Exception e){
                log.error(e);
            }
            finally
            {
                if(serialFactory == null)
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
