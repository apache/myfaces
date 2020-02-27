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

import org.apache.myfaces.config.annotation.AnnotationConfigurator;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.spi.FacesConfigResourceProvider;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.xml.sax.SAXException;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.webapp.FacesServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Leonardo Uribe
 * @since 2.0.3
 */
public class DefaultFacesConfigurationProvider extends FacesConfigurationProvider
{

    private static final String STANDARD_FACES_CONFIG_RESOURCE = "META-INF/standard-faces-config.xml";
    
    //private static final String META_INF_SERVICES_RESOURCE_PREFIX = "META-INF/services/";

    private static final String DEFAULT_FACES_CONFIG = "/WEB-INF/faces-config.xml";

    private static final Set<String> FACTORY_NAMES = new HashSet<String>();
    static
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

    private static final Logger log = Logger.getLogger(DefaultFacesConfigurationProvider.class.getName());

    private FacesConfigUnmarshaller<? extends FacesConfig> _unmarshaller;
    
    private AnnotationConfigurator _annotationConfigurator;

    protected void setUnmarshaller(ExternalContext ectx, FacesConfigUnmarshaller<? extends FacesConfig> unmarshaller)
    {
        _unmarshaller = unmarshaller;
    }

    @SuppressWarnings("unchecked")
    protected FacesConfigUnmarshaller<? extends FacesConfig> getUnmarshaller(ExternalContext ectx)
    {
        if (_unmarshaller == null)
        {
            _unmarshaller = new DigesterFacesConfigUnmarshallerImpl(ectx);
        }
        return _unmarshaller;
    }
    
    protected void setAnnotationConfigurator(AnnotationConfigurator configurator)
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

    @Override
    public FacesConfig getStandardFacesConfig(ExternalContext ectx)
    {
        try
        {
            if (MyfacesConfig.getCurrentInstance(ectx).isValidateXML())
            {
                URL url = ClassUtils.getResource(STANDARD_FACES_CONFIG_RESOURCE);
                if (url != null)
                {
                    validateFacesConfig(ectx, url);
                }
            }
            InputStream stream = ClassUtils.getResourceAsStream(STANDARD_FACES_CONFIG_RESOURCE);
            if (stream == null)
                throw new FacesException("Standard faces config " + STANDARD_FACES_CONFIG_RESOURCE + " not found");
            if (log.isLoggable(Level.INFO))
                log.info("Reading standard config " + STANDARD_FACES_CONFIG_RESOURCE);
            
            FacesConfig facesConfig = getUnmarshaller(ectx).getFacesConfig(stream, STANDARD_FACES_CONFIG_RESOURCE);
            stream.close();
            return facesConfig;
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        catch (SAXException e)
        {
            throw new FacesException(e);
        }
    }

    @Override
    public FacesConfig getAnnotationsFacesConfig(ExternalContext ectx, boolean metadataComplete)
    {
        return getAnnotationConfigurator().createFacesConfig(ectx, metadataComplete);
    }

    /**
     * This method performs part of the factory search outlined in section 10.2.6.1.
     */
    @Override
    public FacesConfig getMetaInfServicesFacesConfig(ExternalContext ectx)
    {
        try
        {
            org.apache.myfaces.config.impl.digester.elements.FacesConfig facesConfig
                    = new org.apache.myfaces.config.impl.digester.elements.FacesConfig();
            org.apache.myfaces.config.impl.digester.elements.Factory factory
                    = new org.apache.myfaces.config.impl.digester.elements.Factory();
            
            facesConfig.addFactory(factory);
            
            for (String factoryName : FACTORY_NAMES)
            {
                List<String> classList = ServiceProviderFinderFactory.getServiceProviderFinder(ectx)
                        .getServiceProviderList(factoryName);
                
                for (String className : classList)
                {
                    if (log.isLoggable(Level.INFO))
                    {
                        log.info("Found " + factoryName + " factory implementation: " + className);
                    }

                    if (factoryName.equals(FactoryFinder.APPLICATION_FACTORY))
                    {
                        factory.addApplicationFactory(className);
                    } 
                    else if(factoryName.equals(FactoryFinder.EXCEPTION_HANDLER_FACTORY)) 
                    {
                        factory.addExceptionHandlerFactory(className);
                    } 
                    else if (factoryName.equals(FactoryFinder.EXTERNAL_CONTEXT_FACTORY))
                    {
                        factory.addExternalContextFactory(className);
                    } 
                    else if (factoryName.equals(FactoryFinder.FACES_CONTEXT_FACTORY))
                    {
                        factory.addFacesContextFactory(className);
                    } 
                    else if (factoryName.equals(FactoryFinder.LIFECYCLE_FACTORY))
                    {
                        factory.addLifecycleFactory(className);
                    } 
                    else if (factoryName.equals(FactoryFinder.RENDER_KIT_FACTORY))
                    {
                        factory.addRenderkitFactory(className);
                    } 
                    else if(factoryName.equals(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY)) 
                    {
                        factory.addTagHandlerDelegateFactory(className);
                    } 
                    else if (factoryName.equals(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY))
                    {
                        factory.addPartialViewContextFactory(className);
                    } 
                    else if(factoryName.equals(FactoryFinder.VISIT_CONTEXT_FACTORY)) 
                    {
                        factory.addVisitContextFactory(className);
                    } 
                    else if(factoryName.equals(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY)) 
                    {
                        factory.addViewDeclarationLanguageFactory(className);
                    }
                    
                    else
                    {
                        throw new IllegalStateException("Unexpected factory name " + factoryName);
                    }
                }
            }
            return facesConfig;
        }
        catch (Throwable e)
        {
            throw new FacesException(e);
        }
    }

    /**
     * This method fixes MYFACES-208
     */
    @Override
    public List<FacesConfig> getClassloaderFacesConfig(ExternalContext ectx)
    {
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        try
        {
            FacesConfigResourceProvider provider = FacesConfigResourceProviderFactory.
                getFacesConfigResourceProviderFactory(ectx).createFacesConfigResourceProvider(ectx);
            
            Collection<URL> facesConfigs = provider.getMetaInfConfigurationResources(ectx);
            
            for (URL url : facesConfigs)
            {
                if (MyfacesConfig.getCurrentInstance(ectx).isValidateXML())
                {
                    validateFacesConfig(ectx, url);
                }
                InputStream stream = null;
                try
                {
                    stream = openStreamWithoutCache(url);
                    if (log.isLoggable(Level.INFO))
                    {
                        log.info("Reading config : " + url.toExternalForm());
                    }
                    appConfigResources.add(getUnmarshaller(ectx).getFacesConfig(stream, url.toExternalForm()));
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
        return appConfigResources;
    }

    @Override
    public List<FacesConfig> getContextSpecifiedFacesConfig(ExternalContext ectx)
    {
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        try
        {
            for (String systemId : getConfigFilesList(ectx))
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
        return appConfigResources;
    }
    
    @Override
    public FacesConfig getWebAppFacesConfig(ExternalContext ectx)
    {
        try
        {
            FacesConfig webAppConfig = null;
            // web application config
            if (MyfacesConfig.getCurrentInstance(ectx).isValidateXML())
            {
                URL url = ectx.getResource(DEFAULT_FACES_CONFIG);
                if (url != null)
                {
                    validateFacesConfig(ectx, url);
                }
            }
            InputStream stream = ectx.getResourceAsStream(DEFAULT_FACES_CONFIG);
            if (stream != null)
            {
                if (log.isLoggable(Level.INFO))
                    log.info("Reading config /WEB-INF/faces-config.xml");
                webAppConfig = getUnmarshaller(ectx).getFacesConfig(stream, DEFAULT_FACES_CONFIG);
                //getDispenser().feed(getUnmarshaller().getFacesConfig(stream, DEFAULT_FACES_CONFIG));
                stream.close();
            }
            return webAppConfig;
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        catch (SAXException e)
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

    private List<String> getConfigFilesList(ExternalContext ectx) {
        String configFiles = ectx.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
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
    
    private void validateFacesConfig(ExternalContext ectx, URL url) throws IOException, SAXException
    {
        String version = ConfigFilesXmlValidationUtils.getFacesConfigVersion(url);
        if ("1.2".equals(version) || "2.0".equals(version))
        {
            ConfigFilesXmlValidationUtils.validateFacesConfigFile(url, ectx, version);
        }
    }

}
