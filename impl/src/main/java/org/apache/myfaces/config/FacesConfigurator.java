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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELResolver;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
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

import org.apache.myfaces.application.ApplicationFactoryImpl;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.application.BackwardsCompatibleNavigationHandlerWrapper;
import org.apache.myfaces.component.visit.VisitContextFactoryImpl;
import org.apache.myfaces.config.annotation.AnnotationConfigurator;
import org.apache.myfaces.config.element.Behavior;
import org.apache.myfaces.config.element.ClientBehaviorRenderer;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigDispenserImpl;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.ConfigOthersSlot;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigNameSlot;
import org.apache.myfaces.config.impl.digester.elements.OrderSlot;
import org.apache.myfaces.config.impl.digester.elements.Ordering;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;
import org.apache.myfaces.config.impl.digester.elements.SystemEventListener;
import org.apache.myfaces.config.util.CyclicDependencyException;
import org.apache.myfaces.config.util.DirectedAcyclicGraphVerifier;
import org.apache.myfaces.config.util.Vertex;
import org.apache.myfaces.context.ExceptionHandlerFactoryImpl;
import org.apache.myfaces.context.ExternalContextFactoryImpl;
import org.apache.myfaces.context.FacesContextFactoryImpl;
import org.apache.myfaces.context.PartialViewContextFactoryImpl;
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
import org.apache.myfaces.util.ContainerUtils;
import org.apache.myfaces.util.ExternalSpecifications;
import org.apache.myfaces.view.ViewDeclarationLanguageFactoryImpl;
import org.apache.myfaces.view.facelets.tag.jsf.TagHandlerDelegateFactoryImpl;
import org.apache.myfaces.view.facelets.util.Classpath;
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
    //private static final Log log = LogFactory.getLog(FacesConfigurator.class);
    private static final Logger log = Logger.getLogger(FacesConfigurator.class.getName());

    private static final String STANDARD_FACES_CONFIG_RESOURCE = "META-INF/standard-faces-config.xml";
    private static final String FACES_CONFIG_RESOURCE = "META-INF/faces-config.xml";
    private static final String META_INF_PREFIX = "META-INF/";
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";

    private static final String META_INF_SERVICES_RESOURCE_PREFIX = "META-INF/services/";

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

    private static final Set<String> FACTORY_NAMES = new HashSet<String>();
    {
        FACTORY_NAMES.add(FactoryFinder.APPLICATION_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.FACES_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.LIFECYCLE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.RENDER_KIT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.VISIT_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
    }

    private final ExternalContext _externalContext;
    private FacesConfigUnmarshaller<? extends FacesConfig> _unmarshaller;
    private FacesConfigDispenser<FacesConfig> _dispenser;
    private AnnotationConfigurator _annotationConfigurator;

    private RuntimeConfig _runtimeConfig;

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
    public void setDispenser(FacesConfigDispenser<FacesConfig> dispenser)
    {
        _dispenser = dispenser;
    }

    /**
     * @return the dispenser
     */
    protected FacesConfigDispenser<FacesConfig> getDispenser()
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
            _annotationConfigurator = new AnnotationConfigurator(_externalContext);
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
        boolean metadataComplete = false;
        try
        {
            //1. Feed standard-faces-config.xml first.
            feedStandardConfig();
            //2. Feed META-INF/services factories
            feedMetaInfServicesFactories();
            
            //3. Retrieve all appConfigResources
            List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
            addClassloaderConfigurations(appConfigResources);
            addContextSpecifiedConfig(appConfigResources);
            
            //4. Retrieve webAppFacesConfig
            FacesConfig webAppFacesConfig = getWebAppConfig();

            //read metadata-complete attribute on WEB-INF/faces-config.xml
            if(webAppFacesConfig != null)
            {
                metadataComplete = Boolean.valueOf(webAppFacesConfig.getMetadataComplete());    
            }
            else
            {
                metadataComplete = false;   //assume false if no faces-config.xml was found
                                            //metadata-complete can only be specified in faces-config.xml per the JSF 2.0 schema 
            }
            
            
            //5. Ordering of Artifacts (see section 11.4.7 of the spec)
            orderAndFeedArtifacts(appConfigResources,webAppFacesConfig); 

            if (log.isLoggable(Level.INFO))
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
        
                //Now we can configure annotations
        getAnnotationConfigurator().configure(
                ((ApplicationFactory) FactoryFinder.getFactory(
                        FactoryFinder.APPLICATION_FACTORY)).getApplication(),
                getDispenser(), metadataComplete);
        
        configureRuntimeConfig();
        configureLifecycle();
        handleSerialFactory();
        

        // record the time of update
        lastUpdate = System.currentTimeMillis();
    }

    private void feedStandardConfig() throws IOException, SAXException
    {
        InputStream stream = ClassUtils.getResourceAsStream(STANDARD_FACES_CONFIG_RESOURCE);
        if (stream == null)
            throw new FacesException("Standard faces config " + STANDARD_FACES_CONFIG_RESOURCE + " not found");
        if (log.isLoggable(Level.INFO))
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

            if (log.isLoggable(Level.INFO))
            {
                if (log.isLoggable(Level.WARNING))
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

                            log.warning(builder.toString());
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
                Iterator<URL> it = ClassUtils.getResources(META_INF_SERVICES_RESOURCE_PREFIX + factoryName, this);
                while (it.hasNext())
                {
                    URL url = it.next();
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
                    finally
                    {
                        if (br != null)
                        {
                            br.close();
                        }
                        if (isr != null)
                        {
                            isr.close();
                        }
                        if (stream != null)
                        {
                            stream.close();
                        }
                    }


                    if (log.isLoggable(Level.INFO))
                    {
                        log.info("Found " + factoryName + " factory implementation: " + className);
                    }

                    if (factoryName.equals(FactoryFinder.APPLICATION_FACTORY))
                    {
                        getDispenser().feedApplicationFactory(className);
                    } else if (factoryName.equals(FactoryFinder.EXTERNAL_CONTEXT_FACTORY))
                    {
                        getDispenser().feedExternalContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.FACES_CONTEXT_FACTORY))
                    {
                        getDispenser().feedFacesContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.LIFECYCLE_FACTORY))
                    {
                        getDispenser().feedLifecycleFactory(className);
                    } else if (factoryName.equals(FactoryFinder.RENDER_KIT_FACTORY))
                    {
                        getDispenser().feedRenderKitFactory(className);
                    } else if (factoryName.equals(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY))
                    {
                        getDispenser().feedPartialViewContextFactory(className);
                    } else if(factoryName.equals(FactoryFinder.VISIT_CONTEXT_FACTORY)) 
                    {
                        getDispenser().feedVisitContextFactory(className);
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

    private InputStream openStreamWithoutCache(URL url) throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }

    /**
     * This method fixes MYFACES-208
     */
    private void addClassloaderConfigurations(List<FacesConfig> appConfigResources)
    {
        try
        {
            Map<String, URL> facesConfigs = new TreeMap<String, URL>();
            Iterator<URL> it = ClassUtils.getResources(FACES_CONFIG_RESOURCE, this);
            while (it.hasNext())
            {
                URL url = it.next();
                String systemId = url.toExternalForm();
                facesConfigs.put(systemId, url);
            }
            
            //Scan files inside META-INF ending with .faces-config.xml
            //TODO: specify classpath for make easier configuration security java 2
            URL[] urls = Classpath.search(META_INF_PREFIX, FACES_CONFIG_SUFFIX);
            for (int i = 0; i < urls.length; i++)
            {
                String systemId = urls[i].toExternalForm();
                facesConfigs.put(systemId, urls[i]);
            }

            for (Map.Entry<String, URL> entry : facesConfigs.entrySet())
            {
                InputStream stream = null;
                try
                {
                    stream = openStreamWithoutCache(entry.getValue());
                    if (log.isLoggable(Level.INFO))
                    {
                        log.info("Reading config : " + entry.getKey());
                    }
                    appConfigResources.add(getUnmarshaller().getFacesConfig(stream, entry.getKey()));
                    //getDispenser().feed(getUnmarshaller().getFacesConfig(stream, entry.getKey()));
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

    private void addContextSpecifiedConfig(List<FacesConfig> appConfigResources) throws IOException, SAXException
    {
        for (String systemId : getConfigFilesList())
        {
            InputStream stream = _externalContext.getResourceAsStream(systemId);
            if (stream == null)
            {
                log.severe("Faces config resource " + systemId + " not found");
                continue;
            }

            if (log.isLoggable(Level.INFO))
            {
                log.info("Reading config " + systemId);
            }
            appConfigResources.add(getUnmarshaller().getFacesConfig(stream, systemId));
            //getDispenser().feed(getUnmarshaller().getFacesConfig(stream, systemId));
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

    private FacesConfig getWebAppConfig() throws IOException, SAXException
    {
        FacesConfig webAppConfig = null;
        // web application config
        InputStream stream = _externalContext.getResourceAsStream(DEFAULT_FACES_CONFIG);
        if (stream != null)
        {
            if (log.isLoggable(Level.INFO))
                log.info("Reading config /WEB-INF/faces-config.xml");
            webAppConfig = getUnmarshaller().getFacesConfig(stream, DEFAULT_FACES_CONFIG);
            //getDispenser().feed(getUnmarshaller().getFacesConfig(stream, DEFAULT_FACES_CONFIG));
            stream.close();
        }
        return webAppConfig;
    }
    
    protected void orderAndFeedArtifacts(List<FacesConfig> appConfigResources, FacesConfig webAppConfig)
        throws FacesException
    {
        if (webAppConfig != null && webAppConfig.getAbsoluteOrdering() != null)
        {
            if (webAppConfig.getOrdering() != null)
            {
                if (log.isLoggable(Level.WARNING))
                {
                    log.warning("<ordering> element found in application faces config. " +
                            "This description will be ignored and the actions described " +
                            "in <absolute-ordering> element will be taken into account instead.");
                }                
            }
            //Absolute ordering
            
            //1. Scan all appConfigResources and create a list
            //containing all resources not mentioned directly, preserving the
            //order founded
            List<FacesConfig> othersResources = new ArrayList<FacesConfig>();
            List<OrderSlot> slots = webAppConfig.getAbsoluteOrdering().getOrderList();
            for (FacesConfig resource : appConfigResources)
            {
                // First condition: if faces-config.xml does not have name it is 1) pre-JSF-2.0 or 2) has no <name> element,
                // -> in both cases cannot be ordered
                // Second condition : faces-config.xml has a name but <ordering> element does not have slot with that name
                //  -> resource can be ordered, but will fit into <others /> element
                if ((resource.getName() == null) || (resource.getName() != null && !containsResourceInSlot(slots, resource.getName())))
                {
                    othersResources.add(resource);
                }
            }
            
            //2. Scan slot by slot and merge information according
            for (OrderSlot slot : webAppConfig.getAbsoluteOrdering().getOrderList())
            {
                if (slot instanceof ConfigOthersSlot)
                {
                    //Add all mentioned in othersResources
                    for (FacesConfig resource : othersResources)
                    {
                        getDispenser().feed(resource);
                    }
                }
                else
                {
                    //Add it to the sorted list
                    FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                    getDispenser().feed(getFacesConfig(appConfigResources, nameSlot.getName()));
                }
            }
        }
        else if (!appConfigResources.isEmpty())
        {
            //Relative ordering
            for (FacesConfig resource : appConfigResources)
            {
                if (resource.getOrdering() != null)
                {
                    if (log.isLoggable(Level.WARNING))
                    {
                        log.warning("<absolute-ordering> element found in application " +
                                "configuration resource "+resource.getName()+". " +
                                "This description will be ignored and the actions described " +
                                "in <ordering> elements will be taken into account instead.");
                    }
                }
            }
            
            List<FacesConfig> postOrderedList = getPostOrderedList(appConfigResources);
            
            List<FacesConfig> sortedList = sortRelativeOrderingList(postOrderedList);
            
            if (sortedList == null)
            {
                //The previous algorithm can't sort correctly, try this one
                sortedList = applySortingAlgorithm(appConfigResources);
            }
            
            for (FacesConfig resource : sortedList)
            {
                //Feed
                getDispenser().feed(resource);
            }            
        }
        
        if(webAppConfig != null)    //add null check for apps which don't have a faces-config.xml (e.g. tomahawk examples for 1.1/1.2)
        {
            getDispenser().feed(webAppConfig);
            
            // check if there is an empty <default-validators> entry.
            // note that this applys only for the faces-config with the 
            // highest precendence (not for standard-faces-config files).
            for (org.apache.myfaces.config.impl.digester.elements.Application app : webAppConfig.getApplications())
            {
                if (app.isDefaultValidatorsPresent() && app.getDefaultValidatorIds().isEmpty())
                {
                    getDispenser().setEmptyDefaultValidators(true);
                    break;
                }
            }
        }
    }

    /**
     * Sort using topological ordering algorithm.
     * 
     * @param appConfigResources
     * @return
     * @throws FacesException
     */
    protected List<FacesConfig> applySortingAlgorithm(List<FacesConfig> appConfigResources) throws FacesException
    {
        
        //0. Convert the references into a graph
        List<Vertex<FacesConfig>> vertexList = new ArrayList<Vertex<FacesConfig>>();
        for (FacesConfig config : appConfigResources)
        {
            Vertex<FacesConfig> v = null;
            if (config.getName() != null)
            {
                v = new Vertex<FacesConfig>(config.getName(), config);
            }
            else
            {
                v = new Vertex<FacesConfig>(config);
            }
            vertexList.add(v);
        }
        
        //1. Resolve dependencies (before-after rules) and mark referenced vertex
        boolean[] referencedVertex = new boolean[vertexList.size()];
        
        for (int i = 0; i < vertexList.size(); i++)
        {
            Vertex<FacesConfig> v = vertexList.get(i);
            FacesConfig f = (FacesConfig) v.getNode();
            
            if (f.getOrdering() != null)
            {
                for (OrderSlot slot : f.getOrdering().getBeforeList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        int j = DirectedAcyclicGraphVerifier.findVertex(vertexList, name);
                        Vertex<FacesConfig> v1 = vertexList.get(j);
                        if (v1 != null)
                        {
                            referencedVertex[i] = true;
                            referencedVertex[j] = true;
                            v1.addDependency(v);
                        }
                    }
                }
                for (OrderSlot slot : f.getOrdering().getAfterList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        int j = DirectedAcyclicGraphVerifier.findVertex(vertexList, name);
                        Vertex<FacesConfig> v1 = vertexList.get(j);
                        if (v1 != null)
                        {
                            referencedVertex[i] = true;
                            referencedVertex[j] = true;
                            v.addDependency(v1);
                        }
                    }
                }
            }
        }
        
        //2. Classify into categories
        List<Vertex<FacesConfig>> beforeAfterOthersList = new ArrayList<Vertex<FacesConfig>>();
        List<Vertex<FacesConfig>> othersList = new ArrayList<Vertex<FacesConfig>>();
        List<Vertex<FacesConfig>> referencedList = new ArrayList<Vertex<FacesConfig>>();
        
        for (int i = 0; i < vertexList.size(); i++)
        {
            if (!referencedVertex[i])
            {
                Vertex<FacesConfig> v = vertexList.get(i);
                FacesConfig f = (FacesConfig) v.getNode();
                boolean added = false;
                if (f.getOrdering() != null)
                {
                    if (!f.getOrdering().getBeforeList().isEmpty())
                    {
                        added = true;
                        beforeAfterOthersList.add(v);
                    }
                    else if (!f.getOrdering().getAfterList().isEmpty())
                    {
                        added = true;
                        beforeAfterOthersList.add(v);
                    }
                }
                if (!added)
                {
                    othersList.add(v);
                }
            }
            else
            {
                referencedList.add(vertexList.get(i));
            }
        }
        
        //3. Sort all referenced nodes
        try
        {
            DirectedAcyclicGraphVerifier.topologicalSort(referencedList);
        }
        catch (CyclicDependencyException e)
        {
            e.printStackTrace();
        }
        
        //4. Add referenced nodes
        List<FacesConfig> sortedList = new ArrayList<FacesConfig>();
        for (Vertex<FacesConfig> v : referencedList)
        {
            sortedList.add((FacesConfig)v.getNode());
        }
        
        //5. add nodes without instructions at the end
        for (Vertex<FacesConfig> v : othersList)
        {
            sortedList.add((FacesConfig)v.getNode());
        }
        
        //6. add before/after nodes
        for (Vertex<FacesConfig> v : beforeAfterOthersList)
        {
            FacesConfig f = (FacesConfig) v.getNode();
            boolean added = false;
            if (f.getOrdering() != null)
            {
                if (!f.getOrdering().getBeforeList().isEmpty())
                {
                    added = true;
                    sortedList.add(0,f);
                }
            }
            if (!added)
            {
                sortedList.add(f);
            }            
        }
        
        //Check
        for (int i = 0; i < sortedList.size(); i++)
        {
            FacesConfig resource = sortedList.get(i);
            
            if (resource.getOrdering() != null)
            {
                for (OrderSlot slot : resource.getOrdering().getBeforeList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;
                            for (int j = i-1; j >= 0; j--)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                log.severe("Circular references detected when sorting " +
                                          "application config resources. Use absolute ordering instead.");
                                throw new FacesException("Circular references detected when sorting " +
                                        "application config resources. Use absolute ordering instead.");
                            }
                        }
                    }
                }
                for (OrderSlot slot : resource.getOrdering().getAfterList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;
                            for (int j = i+1; j < sortedList.size(); j++)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                log.severe("Circular references detected when sorting " +
                                    "application config resources. Use absolute ordering instead.");
                                throw new FacesException("Circular references detected when sorting " +
                                    "application config resources. Use absolute ordering instead.");
                            }
                        }
                    }
                }
            }
        }
        
        return sortedList;
    }
    
    /**
     * Sort a list of pre ordered elements. It scans one by one the elements
     * and apply the conditions mentioned by Ordering object if it is available.
     * 
     * The preOrderedList ensures that application config resources referenced by
     * other resources are processed first, making more easier the sort procedure. 
     * 
     * @param preOrderedList
     * @return
     */
    protected List<FacesConfig> sortRelativeOrderingList(List<FacesConfig> preOrderedList)
    {
        List<FacesConfig> sortedList = new ArrayList<FacesConfig>();
        
        for (int i=0; i < preOrderedList.size(); i++)
        {
            FacesConfig resource = preOrderedList.get(i);
            if (resource.getOrdering() != null)
            {
                if (resource.getOrdering().getBeforeList().isEmpty() &&
                    resource.getOrdering().getAfterList().isEmpty())
                {
                    //No order rules, just put it as is
                    sortedList.add(resource);
                }
                else if (resource.getOrdering().getBeforeList().isEmpty())
                {
                    //Only after rules
                    applyAfterRule(sortedList, resource);
                }
                else if (resource.getOrdering().getAfterList().isEmpty())
                {
                    //Only before rules
                    
                    //Resolve if there is a later reference to this node before
                    //apply it
                    boolean referenceNode = false;

                    for (int j = i+1; j < preOrderedList.size(); j++)
                    {
                        FacesConfig pointingResource = preOrderedList.get(j);
                        for (OrderSlot slot : pointingResource.getOrdering().getBeforeList())
                        {
                            if (slot instanceof FacesConfigNameSlot &&
                                    resource.getName().equals(((FacesConfigNameSlot)slot).getName()) )
                            {
                                referenceNode = true;
                            }
                            if (slot instanceof ConfigOthersSlot)
                            {
                                //No matter if there is a reference, because this rule
                                //is not strict and before other ordering is unpredictable.
                                //
                                referenceNode = false;
                                break;
                            }
                        }
                        if (referenceNode)
                        {
                            break;
                        }
                        for (OrderSlot slot : pointingResource.getOrdering().getAfterList())
                        {
                            if (slot instanceof FacesConfigNameSlot &&
                                resource.getName().equals(((FacesConfigNameSlot)slot).getName()) )
                            {
                                referenceNode = true;
                                break;
                            }
                        }
                    }
                    
                    applyBeforeRule(sortedList, resource, referenceNode);
                }
                else
                {
                    //Both before and after rules
                    //In this case we should compare before and after rules
                    //and the one with names takes precedence over the other one.
                    //It both have names references, before rules takes
                    //precedence over after
                    //after some action is applied a check of the condition is made.
                    int beforeWeight = 0;
                    int afterWeight = 0;
                    for (OrderSlot slot : resource.getOrdering()
                            .getBeforeList())
                    {
                        if (slot instanceof FacesConfigNameSlot)
                        {
                            beforeWeight++;
                        }
                    }
                    for (OrderSlot slot : resource.getOrdering()
                            .getAfterList())
                    {
                        if (slot instanceof FacesConfigNameSlot)
                        {
                            afterWeight++;
                        }
                    }
                    
                    if (beforeWeight >= afterWeight)
                    {
                        applyBeforeRule(sortedList, resource,false);
                    }
                    else
                    {
                        applyAfterRule(sortedList, resource);
                    }
                    
                    
                }
            }
            else
            {
                //No order rules, just put it as is
                sortedList.add(resource);
            }
        }
        
        //Check
        for (int i = 0; i < sortedList.size(); i++)
        {
            FacesConfig resource = sortedList.get(i);
            
            if (resource.getOrdering() != null)
            {
                for (OrderSlot slot : resource.getOrdering().getBeforeList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;                                
                            for (int j = i-1; j >= 0; j--)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                //Cyclic reference
                                return null;
                            }
                        }
                    }
                }
                for (OrderSlot slot : resource.getOrdering().getAfterList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;                                
                            for (int j = i+1; j < sortedList.size(); j++)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                //Cyclic reference
                                return null;
                            }
                        }
                    }
                }
            }
        }
        
        return sortedList;
    }
    
    private void applyBeforeRule(List<FacesConfig> sortedList, FacesConfig resource, boolean referenced) throws FacesException
    {
        //Only before rules
        boolean configOthers = false;
        List<String> names = new ArrayList<String>();
        
        for (OrderSlot slot : resource.getOrdering().getBeforeList())
        {
            if (slot instanceof ConfigOthersSlot)
            {
                configOthers = true;
                break;
            }
            else
            {
                FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                names.add(nameSlot.getName());
            }
        }
        
        if (configOthers)
        {
            //<before>....<others/></before> case
            //other reference where already considered when
            //pre ordered list was calculated, so just add to the end.
            
            //There is one very special case, and it is when there
            //is another resource with a reference on it. In this case,
            //it is better do not apply this rule and add it to the end
            //to give the chance to the other one to be applied.
            if (resource.getOrdering().getBeforeList().size() > 1)
            {
                //If there is a reference apply it
                sortedList.add(0,resource);
            }
            else if (!referenced)
            {
                //If it is not referenced apply it
                sortedList.add(0,resource);
            }
            else
            {
                //if it is referenced bypass the rule and add it to the end
                sortedList.add(resource);
            }
        }
        else
        {
            //Scan the nearest reference and add it after
            boolean founded = false;
            for (int i = 0; i < sortedList.size() ; i++)
            {
                if (names.contains(sortedList.get(i).getName()))
                {
                    sortedList.add(i,resource);
                    founded = true;
                    break;
                }
            }
            if (!founded)
            {
                //just add it to the end
                sortedList.add(resource);
            }
        }        
    }
    
    private void applyAfterRule(List<FacesConfig> sortedList, FacesConfig resource) throws FacesException
    {
        boolean configOthers = false;
        List<String> names = new ArrayList<String>();
        
        for (OrderSlot slot : resource.getOrdering().getAfterList())
        {
            if (slot instanceof ConfigOthersSlot)
            {
                configOthers = true;
                break;
            }
            else
            {
                FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                names.add(nameSlot.getName());
            }
        }
        
        if (configOthers)
        {
            //<after>....<others/></after> case
            //other reference where already considered when
            //pre ordered list was calculated, so just add to the end.
            sortedList.add(resource);
        }
        else
        {
            //Scan the nearest reference and add it after
            boolean founded = false;
            for (int i = sortedList.size()-1 ; i >=0 ; i--)
            {
                if (names.contains(sortedList.get(i).getName()))
                {
                    if (i+1 < sortedList.size())
                    {
                        sortedList.add(i+1,resource);
                    }
                    else
                    {
                        sortedList.add(resource);
                    }
                    founded = true;
                    break;
                }
            }
            if (!founded)
            {
                //just add it to the end
                sortedList.add(resource);
            }
        }        
    }
    
    
    /**
     * Pre Sort the appConfigResources, detecting cyclic references, so when sort process
     * start, it is just necessary to traverse the preOrderedList once. To do that, we just
     * scan "before" and "after" lists for references, and then those references are traversed
     * again, so the first elements of the pre ordered list does not have references and
     * the next elements has references to the already added ones.
     * 
     * The elements on the preOrderedList looks like this:
     * 
     * [ no ordering elements , referenced elements ... more referenced elements, 
     *  before others / after others non referenced elements]
     * 
     * @param appConfigResources
     * @return
     */
    protected List<FacesConfig> getPostOrderedList(final List<FacesConfig> appConfigResources) throws FacesException
    {
        
        //0. Clean up: remove all not found resource references from the ordering 
        //descriptions.
        List<String> availableReferences = new ArrayList<String>();
        for (FacesConfig resource : appConfigResources)
        {
            String name = resource.getName();
            if (name != null && !"".equals(name))
            {
                availableReferences.add(name);
            }
        }
        
        for (FacesConfig resource : appConfigResources)
        {
            Ordering ordering = resource.getOrdering();
            if (ordering != null)
            {
                for (Iterator<OrderSlot> it =  resource.getOrdering().getBeforeList().iterator();it.hasNext();)
                {
                    OrderSlot slot = it.next();
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (!availableReferences.contains(name))
                        {
                            it.remove();
                        }
                    }
                }
                for (Iterator<OrderSlot> it =  resource.getOrdering().getAfterList().iterator();it.hasNext();)
                {
                    OrderSlot slot = it.next();
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (!availableReferences.contains(name))
                        {
                            it.remove();
                        }
                    }
                }
            }
        }

        List<FacesConfig> appFilteredConfigResources = null; 

        //1. Pre filtering: Sort nodes according to its weight. The weight is the number of named
        //nodes containing in both before and after lists. The sort is done from the more complex
        //to the most simple
        if (appConfigResources instanceof ArrayList)
        {
            appFilteredConfigResources = (List<FacesConfig>)
                ((ArrayList<FacesConfig>)appConfigResources).clone();
        }
        else
        {
            appFilteredConfigResources = new ArrayList<FacesConfig>();
            appFilteredConfigResources.addAll(appConfigResources);
        }
        Collections.sort(appFilteredConfigResources,
                new Comparator<FacesConfig>()
                {
                    public int compare(FacesConfig o1, FacesConfig o2)
                    {
                        int o1Weight = 0;
                        int o2Weight = 0;
                        if (o1.getOrdering() != null)
                        {
                            for (OrderSlot slot : o1.getOrdering()
                                    .getBeforeList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o1Weight++;
                                }
                            }
                            for (OrderSlot slot : o1.getOrdering()
                                    .getAfterList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o1Weight++;
                                }
                            }
                        }
                        if (o2.getOrdering() != null)
                        {
                            for (OrderSlot slot : o2.getOrdering()
                                    .getBeforeList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o2Weight++;
                                }
                            }
                            for (OrderSlot slot : o2.getOrdering()
                                    .getAfterList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o2Weight++;
                                }
                            }
                        }
                        return o2Weight - o1Weight;
                    }
                });
        
        List<FacesConfig> postOrderedList = new LinkedList<FacesConfig>();
        List<FacesConfig> othersList = new ArrayList<FacesConfig>();
        
        List<String> nameBeforeStack = new ArrayList<String>();
        List<String> nameAfterStack = new ArrayList<String>();
        
        boolean[] visitedSlots = new boolean[appFilteredConfigResources.size()];
        
        //2. Scan and resolve conflicts
        for (int i = 0; i < appFilteredConfigResources.size(); i++)
        {
            if (!visitedSlots[i])
            {
                resolveConflicts(appFilteredConfigResources, i, visitedSlots, 
                        nameBeforeStack, nameAfterStack, postOrderedList, othersList, false);
            }
        }
        
        //Add othersList to postOrderedList so <before><others/></before> and <after><others/></after>
        //ordering conditions are handled at last if there are not referenced by anyone
        postOrderedList.addAll(othersList);
        
        return postOrderedList;
    }
        
    private void resolveConflicts(final List<FacesConfig> appConfigResources, int index, boolean[] visitedSlots,
            List<String> nameBeforeStack, List<String> nameAfterStack, List<FacesConfig> postOrderedList,
            List<FacesConfig> othersList, boolean indexReferenced) throws FacesException
    {
        FacesConfig facesConfig = appConfigResources.get(index);
        
        if (nameBeforeStack.contains(facesConfig.getName()))
        {
            //Already referenced, just return. Later if there exists a
            //circular reference, it will be detected and solved.
            return;
        }
        
        if (nameAfterStack.contains(facesConfig.getName()))
        {
            //Already referenced, just return. Later if there exists a
            //circular reference, it will be detected and solved.
            return;
        }
        
        if (facesConfig.getOrdering() != null)
        {
            boolean pointingResource = false;
            
            //Deal with before restrictions first
            for (OrderSlot slot : facesConfig.getOrdering().getBeforeList())
            {
                if (slot instanceof FacesConfigNameSlot)
                {
                    FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                    //The resource pointed is not added yet?
                    boolean alreadyAdded = false;
                    for (FacesConfig res : postOrderedList)
                    {
                        if (nameSlot.getName().equals(res.getName()))
                        {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded)
                    {
                        int indexSlot = -1;
                        //Find it
                        for (int i = 0; i < appConfigResources.size(); i++)
                        {
                            FacesConfig resource = appConfigResources.get(i);
                            if (resource.getName() != null && nameSlot.getName().equals(resource.getName()))
                            {
                                indexSlot = i;
                                break;
                            }
                        }
                        
                        //Resource founded on appConfigResources
                        if (indexSlot != -1)
                        {
                            pointingResource = true;
                            //Add to nameStac
                            nameBeforeStack.add(facesConfig.getName());
                            
                            resolveConflicts(appConfigResources, indexSlot, visitedSlots, 
                                    nameBeforeStack, nameAfterStack, postOrderedList,
                                    othersList,true);
                            
                            nameBeforeStack.remove(facesConfig.getName());
                        }
                    }
                    else
                    {
                        pointingResource = true;
                    }
                }
            }
            
            for (OrderSlot slot : facesConfig.getOrdering().getAfterList())
            {
                if (slot instanceof FacesConfigNameSlot)
                {
                    FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                    //The resource pointed is not added yet?
                    boolean alreadyAdded = false;
                    for (FacesConfig res : postOrderedList)
                    {
                        if (nameSlot.getName().equals(res.getName()))
                        {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded)
                    {
                        int indexSlot = -1;
                        //Find it
                        for (int i = 0; i < appConfigResources.size(); i++)
                        {
                            FacesConfig resource = appConfigResources.get(i);
                            if (resource.getName() != null && nameSlot.getName().equals(resource.getName()))
                            {
                                indexSlot = i;
                                break;
                            }
                        }
                        
                        //Resource founded on appConfigResources
                        if (indexSlot != -1)
                        {
                            pointingResource = true;
                            //Add to nameStac
                            nameAfterStack.add(facesConfig.getName());
                            
                            resolveConflicts(appConfigResources, indexSlot, visitedSlots, 
                                    nameBeforeStack, nameAfterStack, postOrderedList,
                                    othersList,true);
                            
                            nameAfterStack.remove(facesConfig.getName());
                        }
                    }
                    else
                    {
                        pointingResource = true;
                    }
                }
            }
            
            if (facesConfig.getOrdering().getBeforeList().isEmpty() &&
                facesConfig.getOrdering().getAfterList().isEmpty())
            {
                //Fits in the category "others", put at beginning
                postOrderedList.add(0,appConfigResources.get(index));
            }
            else if (pointingResource || indexReferenced)
            {
                //If the node points to other or is referenced from other,
                //add to the postOrderedList at the end
                postOrderedList.add(appConfigResources.get(index));                    
            }
            else
            {
                //Add to othersList
                othersList.add(appConfigResources.get(index));
            }
        }
        else
        {
            //Add at start of the list, since does not have any ordering
            //instructions and on the next step makes than "before others" and "after others"
            //works correctly
            postOrderedList.add(0,appConfigResources.get(index));
        }
        //Set the node as visited
        visitedSlots[index] = true;
    }    
    
    private FacesConfig getFacesConfig(List<FacesConfig> appConfigResources, String name)
    {
        for (FacesConfig cfg: appConfigResources)
        {
            if (cfg.getName() != null && name.equals(cfg.getName()))
            {
                return cfg;
            }
        }
        return null;
    }
    
    private boolean containsResourceInSlot(List<OrderSlot> slots, String name)
    {
        for (OrderSlot slot: slots)
        {
            if (slot instanceof FacesConfigNameSlot)
            {
                FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                if (name.equals(nameSlot.getName()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void configureFactories()
    {
        FacesConfigDispenser<FacesConfig> dispenser = getDispenser();
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
        Application application = ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();

        FacesConfigDispenser<FacesConfig> dispenser = getDispenser();
        application.setActionListener(getApplicationObject(ActionListener.class,
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

        application.setNavigationHandler(getApplicationObject(NavigationHandler.class,
                                                              ConfigurableNavigationHandler.class,
                                                              BackwardsCompatibleNavigationHandlerWrapper.class,
                                                              dispenser.getNavigationHandlerIterator(),
                                                              application.getNavigationHandler()));

        application.setStateManager(getApplicationObject(StateManager.class,
                                                         dispenser.getStateManagerIterator(),
                                                         application.getStateManager()));

        application.setResourceHandler(getApplicationObject(ResourceHandler.class,
                                                            dispenser.getResourceHandlerIterator(),
                                                            application.getResourceHandler()));

        List<Locale> locales = new ArrayList<Locale>();
        for (String locale : dispenser.getSupportedLocalesIterator())
        {
            locales.add(LocaleUtils.toLocale(locale));
        }

        application.setSupportedLocales(locales);

        application.setViewHandler(getApplicationObject(ViewHandler.class,
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

        if (application instanceof ApplicationImpl)
        {
            for (String converterClassName : dispenser.getConverterConfigurationByClassName())
            {
                ApplicationImpl app = (ApplicationImpl)application;
                app.addConverterConfiguration(converterClassName, dispenser.getConverterConfiguration(converterClassName));
            }
        }

        for (String validatorId : dispenser.getValidatorIds())
        {
            application.addValidator(validatorId, dispenser.getValidatorClass(validatorId));
        }

        // only add default validators if there is no empty <default-validators> in faces-config.xml
        if (!dispenser.isEmptyDefaultValidators())
        {
            boolean beanValidatorAdded = false;
            for (String validatorId : dispenser.getDefaultValidatorIds())
            {
                if (validatorId.equals(BeanValidator.VALIDATOR_ID))
                {
                    if (!ExternalSpecifications.isBeanValidationAvailable())
                    {
                        // do not add it as a default validator
                        continue;
                    }
                    else
                    {
                        beanValidatorAdded = true;
                    }
                }
                application.addDefaultValidatorId(validatorId);
            }
        
            // add the bean validator if it is available, not already added and not disabled
            if (!beanValidatorAdded && ExternalSpecifications.isBeanValidationAvailable())
            {
                String disabled = _externalContext.getInitParameter(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME);
                boolean defaultBeanValidatorDisabled = (disabled != null && disabled.toLowerCase().equals("true"));
                if (!defaultBeanValidatorDisabled)
                {
                    application.addDefaultValidatorId(BeanValidator.VALIDATOR_ID);
                }
            }
        }

        for (Behavior behavior : dispenser.getBehaviors()) {
            application.addBehavior(behavior.getBehaviorId(), behavior.getBehaviorClass());
        }
        
        RuntimeConfig runtimeConfig = getRuntimeConfig();

        runtimeConfig.setPropertyResolverChainHead(getApplicationObject(PropertyResolver.class,
                                                                        dispenser.getPropertyResolverIterator(),
                                                                        new DefaultPropertyResolver()));

        runtimeConfig.setVariableResolverChainHead(getApplicationObject(VariableResolver.class,
                                                                        dispenser.getVariableResolverIterator(),
                                                                        new VariableResolverImpl()));
        
        // configure ManagedBeanDestroyer to listen to PreDestroyCustomScopeEvent and PreDestroyViewMapEvent
        ManagedBeanDestroyer mbDestroyer = new ManagedBeanDestroyer();
        application.subscribeToEvent(PreDestroyCustomScopeEvent.class, mbDestroyer);
        application.subscribeToEvent(PreDestroyViewMapEvent.class, mbDestroyer);
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
    };


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
    
    private <T> T getApplicationObject(Class<T> interfaceClass, Collection<String> classNamesIterator, T defaultObject)
    {
        return getApplicationObject(interfaceClass, null, null, classNamesIterator, defaultObject);
    }

    /**
     * Creates ApplicationObjects like NavigationHandler or StateManager and creates 
     * the right wrapping chain of the ApplicationObjects known as the decorator pattern. 
     * @param <T>
     * @param interfaceClass The class from which the implementation has to inherit from.
     * @param extendedInterfaceClass A subclass of interfaceClass which specifies a more
     *                               detailed implementation.
     * @param extendedInterfaceWrapperClass A wrapper class for the case that you have an ApplicationObject
     *                                      which only implements the interfaceClass but not the 
     *                                      extendedInterfaceClass.
     * @param classNamesIterator All the class names of the actual ApplicationObject implementations
     *                           from the faces-config.xml.
     * @param defaultObject The default implementation for the given ApplicationObject.
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getApplicationObject(Class<T> interfaceClass, Class<? extends T> extendedInterfaceClass,
            Class<? extends T> extendedInterfaceWrapperClass,
            Collection<String> classNamesIterator, T defaultObject)
    {
        T current = defaultObject;
        

        for (String implClassName : classNamesIterator)
        {
            Class<? extends T> implClass = ClassUtils.simpleClassForName(implClassName);

            // check, if class is of expected interface type
            if (!interfaceClass.isAssignableFrom(implClass))
            {
                throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
            }

            if (current == null)
            {
                // nothing to decorate
                current = (T) ClassUtils.newInstance(implClass);
            }
            else
            {
                // let's check if class supports the decorator pattern
                T newCurrent = null;
                try
                {
                    Constructor<? extends T> delegationConstructor = null;
                    
                    // first, if there is a extendedInterfaceClass,
                    // try to find a constructor that uses that
                    if (extendedInterfaceClass != null 
                            && extendedInterfaceClass.isAssignableFrom(current.getClass()))
                    {
                        try
                        {
                            delegationConstructor = 
                                    implClass.getConstructor(new Class[] {extendedInterfaceClass});
                        }
                        catch (NoSuchMethodException mnfe)
                        {
                            // just eat it
                        }
                    }
                    if (delegationConstructor == null)
                    {
                        // try to find the constructor with the "normal" interfaceClass
                        delegationConstructor = 
                                implClass.getConstructor(new Class[] {interfaceClass});
                    }
                    // impl class supports decorator pattern at this point
                    try
                    {
                        // create new decorator wrapping current
                        newCurrent = delegationConstructor.newInstance(new Object[] { current });
                    }
                    catch (InstantiationException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // no decorator pattern support
                    newCurrent = (T) ClassUtils.newInstance(implClass);
                }
                
                // now we have a new current object (newCurrent)
                // --> find out if it is assignable from extendedInterfaceClass
                // and if not, wrap it in a backwards compatible wrapper (if available)
                if (extendedInterfaceWrapperClass != null
                        && !extendedInterfaceClass.isAssignableFrom(newCurrent.getClass()))
                {
                    try
                    {
                        Constructor<? extends T> wrapperConstructor
                                = extendedInterfaceWrapperClass.getConstructor(
                                        new Class[] {interfaceClass, extendedInterfaceClass});
                        newCurrent = wrapperConstructor.newInstance(new Object[] {newCurrent, current});
                    }
                    catch (NoSuchMethodException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InstantiationException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        throw new FacesException(e);
                    }
                }
                
                current = newCurrent;
            }
        }

        return current;
    }

    private void configureRuntimeConfig()
    {
        RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(_externalContext);

        FacesConfigDispenser<FacesConfig> dispenser = getDispenser();
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

        for (ResourceBundle bundle : dispenser.getResourceBundles())
        {
            runtimeConfig.addResourceBundle(bundle);
        }

        for (String className : dispenser.getElResolvers())
        {
            runtimeConfig.addFacesConfigElResolver((ELResolver)ClassUtils.newInstance(className, ELResolver.class));
        }
        
        runtimeConfig.setFacesVersion (dispenser.getFacesVersion());
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

        FacesConfigDispenser<FacesConfig> dispenser = getDispenser();
        for (String renderKitId : dispenser.getRenderKitIds())
        {
            Collection<String> renderKitClass = dispenser.getRenderKitClasses(renderKitId);

            if (renderKitClass.isEmpty())
            {
                renderKitClass = new ArrayList<String>(1);
                renderKitClass.add(DEFAULT_RENDER_KIT_CLASS);
            }

            //RenderKit renderKit = (RenderKit) ClassUtils.newInstance(renderKitClass);
            RenderKit renderKit = (RenderKit) getApplicationObject(RenderKit.class, renderKitClass, null);

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
     * public static class VersionInfo { private String artifactId; private List<JarInfo> jarInfos;
     * 
     * public VersionInfo(String artifactId) { this.artifactId = artifactId; }
     * 
     * public String getArtifactId() { return packageName; }
     * 
     * public void addJarInfo(Matcher matcher) { if (jarInfos == null) { jarInfos = new ArrayList<JarInfo>(); }
     * 
     * String path = matcher.group(1);
     * 
     * Version version = new Version(matcher.group(3), matcher.group(5), matcher.group(7), matcher.group(9),
     * matcher.group(10));
     * 
     * jarInfos.add(new JarInfo(path, version)); }
     * 
     * public String getLastVersion() { if (jarInfos == null) return null; if (jarInfos.size() == 0) return null;
     * 
     * return ""; //return jarInfos.get(jarInfos.size() - 1).getVersion(); }
     * 
     * / Probably, the first encountered version will be used.
     * 
     * @return probably used version
     * 
     * public String getUsedVersion() {
     * 
     * if (jarInfos == null) return null; if (jarInfos.size() == 0) return null; return ""; //return
     * jarInfos.get(0).getVersion(); }
     * 
     * / Probably, the first encountered version will be used.
     * 
     * @return probably used classpath
     * 
     * public String getUsedVersionPath() {
     * 
     * if (jarInfos == null) return null; if (jarInfos.size() == 0) return null;
     * 
     * return jarInfos.get(0).getUrl();
     * 
     * } }
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
                JarInfo other = (JarInfo) o;
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
        private Integer[] parts;

        private boolean snapshot;

        public Version(String major, String minor, String maintenance, String extra, String snapshot)
        {
            parts = new Integer[4];
            parts[0] = Integer.valueOf(major);

            if (minor != null)
            {
                parts[1] = Integer.valueOf(minor);

                if (maintenance != null)
                {
                    parts[2] = Integer.valueOf(maintenance);

                    if (extra != null)
                    {
                        parts[3] = Integer.valueOf(extra);
                    }
                }
            }

            this.snapshot = snapshot != null;
        }

        public int compareTo(Version v)
        {
            for (int i = 0; i < parts.length; i++)
            {
                Integer left = parts[i];
                Integer right = v.parts[i];
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
                Version other = (Version) o;
                if (snapshot != other.snapshot)
                {
                    return false;
                }

                for (int i = 0; i < parts.length; i++)
                {
                    Integer thisPart = parts[i];
                    Integer otherPart = other.parts[i];
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
            for (Integer part : parts)
            {
                if (part != null)
                {
                    hash ^= part.hashCode();
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
                Integer val = parts[i];
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
}
