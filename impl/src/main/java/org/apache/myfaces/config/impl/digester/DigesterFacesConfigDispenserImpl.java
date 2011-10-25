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
package org.apache.myfaces.config.impl.digester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.config.FacesConfigDispenser;
import org.apache.myfaces.config.element.Behavior;
import org.apache.myfaces.config.element.ClientBehaviorRenderer;
import org.apache.myfaces.config.element.FaceletsProcessing;
import org.apache.myfaces.config.element.FacesConfigExtension;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.element.Application;
import org.apache.myfaces.config.element.Converter;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.Factory;
import org.apache.myfaces.config.element.LocaleConfig;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.config.element.RenderKit;
import org.apache.myfaces.config.element.ResourceBundle;
import org.apache.myfaces.config.element.SystemEventListener;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class DigesterFacesConfigDispenserImpl extends FacesConfigDispenser
{
    /**
     * 
     */
    private static final long serialVersionUID = 3550379003287939559L;
    // Factories
    private List<String> applicationFactories = new ArrayList<String>();
    private List<String> exceptionHandlerFactories = new ArrayList<String>();
    private List<String> externalContextFactories = new ArrayList<String>();
    private List<String> facesContextFactories = new ArrayList<String>();
    private List<String> lifecycleFactories = new ArrayList<String>();
    private List<String> ViewDeclarationLanguageFactories = new ArrayList<String>();
    private List<String> partialViewContextFactories = new ArrayList<String>();
    private List<String> renderKitFactories = new ArrayList<String>();
    private List<String> tagHandlerDelegateFactories = new ArrayList<String>();
    private List<String> visitContextFactories = new ArrayList<String>();
    private List<String> faceletCacheFactories = new ArrayList<String>();
    
    private String defaultRenderKitId;
    private String messageBundle;
    private String partialTraversal;
    private String facesVersion;
    
    private LocaleConfig localeConfig;

    private Map<String, String> components = new HashMap<String, String>();
    private Map<String, String> converterByClass = new HashMap<String, String>();
    private Map<String, String> converterById = new HashMap<String, String>();
    private Map<String, String> validators = new HashMap<String, String>();
    private List<Behavior> behaviors = new ArrayList<Behavior>();
    
    private Map<String, Converter> converterConfigurationByClassName = new HashMap<String, Converter>();
    
    private Map<String, org.apache.myfaces.config.impl.digester.elements.RenderKit> renderKits
            = new LinkedHashMap<String, org.apache.myfaces.config.impl.digester.elements.RenderKit>();
    
    private List<String> actionListeners = new ArrayList<String>();
    private List<String> elResolvers = new ArrayList<String>();
    private List<String> lifecyclePhaseListeners = new ArrayList<String>();
    private List<String> navigationHandlers = new ArrayList<String>();
    private List<String> propertyResolver = new ArrayList<String>();
    private List<String> resourceHandlers = new ArrayList<String>();
    private List<String> stateManagers = new ArrayList<String>();
    private List<String> variableResolver = new ArrayList<String>();
    private List<String> viewHandlers = new ArrayList<String>();
    private List<String> defaultValidatorIds = new ArrayList<String>();
    private List<String> defaultAnnotatedValidatorIds = new ArrayList<String>();
    
    private List<ManagedBean> managedBeans = new ArrayList<ManagedBean>();
    
    private List<NavigationRule> navigationRules = new ArrayList<NavigationRule>();
    private List<ResourceBundle> resourceBundles = new ArrayList<ResourceBundle>();

    private List<SystemEventListener> systemEventListeners = new ArrayList<SystemEventListener>();
    
    private List<NamedEvent> namedEvents = new ArrayList<NamedEvent>();
    
    private Map<String, FaceletsProcessing> faceletsProcessingByFileExtension
            = new HashMap<String, FaceletsProcessing>();
    
    /**
     * Add another unmarshalled faces config object.
     * 
     * @param config
     *            unmarshalled faces config object
     */
    public void feed(FacesConfig config)
    {
        for (Factory factory : config.getFactories())
        {
            applicationFactories.addAll(factory.getApplicationFactory());
            exceptionHandlerFactories.addAll(factory.getExceptionHandlerFactory());
            externalContextFactories.addAll(factory.getExternalContextFactory());
            facesContextFactories.addAll(factory.getFacesContextFactory());
            lifecycleFactories.addAll(factory.getLifecycleFactory());
            ViewDeclarationLanguageFactories.addAll(factory.getViewDeclarationLanguageFactory());
            partialViewContextFactories.addAll(factory.getPartialViewContextFactory());
            renderKitFactories.addAll(factory.getRenderkitFactory());
            tagHandlerDelegateFactories.addAll(factory.getTagHandlerDelegateFactory());
            visitContextFactories.addAll(factory.getVisitContextFactory());
            faceletCacheFactories.addAll(factory.getFaceletCacheFactory());
        }

        components.putAll(config.getComponents());
        validators.putAll(config.getValidators());
        behaviors.addAll (config.getBehaviors());
        
        for (Application application : config.getApplications())
        {
            if (!application.getDefaultRenderkitId().isEmpty())
            {
                defaultRenderKitId =
                        application.getDefaultRenderkitId().get(application.getDefaultRenderkitId().size() - 1);
            }

            if (!application.getMessageBundle().isEmpty())
            {
                messageBundle = application.getMessageBundle().get(application.getMessageBundle().size() - 1);
            }

            if (!application.getLocaleConfig().isEmpty())
            {
                localeConfig = application.getLocaleConfig().get(application.getLocaleConfig().size() - 1);
            }
            
            if (!application.getPartialTraversal().isEmpty())
            {
                partialTraversal = application.getPartialTraversal().get (application.getPartialTraversal().size() - 1);
            }
            
            actionListeners.addAll(application.getActionListener());
            navigationHandlers.addAll(application.getNavigationHandler());
            resourceHandlers.addAll(application.getResourceHandler());
            viewHandlers.addAll(application.getViewHandler());
            stateManagers.addAll(application.getStateManager());
            propertyResolver.addAll(application.getPropertyResolver());
            variableResolver.addAll(application.getVariableResolver());
            resourceBundles.addAll(application.getResourceBundle());
            elResolvers.addAll(application.getElResolver());

            // Jsf 2.0 spec section 3.5.3 says this: 
            // ".... Any configuration resource that declares a list of default 
            // validators overrides any list provided in a previously processed
            // configuration resource. If an empty <default-validators/> element 
            // is found in a configuration resource, the list
            // of default validators must be cleared....."
            if (application.isDefaultValidatorsPresent())
            {
                // we have a <default-validators> element, so any existing
                // default validators should be removed
                defaultValidatorIds.clear();
                
                // now add all default-validator entries (could be zero)
                defaultValidatorIds.addAll(application.getDefaultValidatorIds());
            }
            else
            {
                //If isDefaultValidatorsPresent() is false, and there are still 
                //default validators, it means they were added using annotations, so
                //they are not affected by the empty entry according to section 3.5.3
                defaultAnnotatedValidatorIds.addAll(application.getDefaultValidatorIds());
            }
            
            systemEventListeners.addAll(application.getSystemEventListeners());
        }

        for (Converter converter : config.getConverters())
        {
            if (converter.getConverterId() != null)
            {
                converterById.put(converter.getConverterId(),converter
                        .getConverterClass());
            }
            if (converter.getForClass() != null)
            {
                converterByClass.put(converter.getForClass(),converter
                        .getConverterClass());
            }

            converterConfigurationByClassName.put(converter.getConverterClass(), converter);
        }

        for (RenderKit renderKit : config.getRenderKits())
        {
            String renderKitId = renderKit.getId();

            if (renderKitId == null)
            {
                renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }

            org.apache.myfaces.config.impl.digester.elements.RenderKit existing = renderKits.get(renderKitId);

            if (existing == null)
            {
                existing = new org.apache.myfaces.config.impl.digester.elements.RenderKit();
                existing.merge(renderKit);
                renderKits.put(renderKitId, existing);
                //renderKits.put(renderKitId, renderKit);
            }
            else
            {
                existing.merge(renderKit);
            }
        }

        for (FacesConfigExtension extension : config.getFacesConfigExtensions())
        {
            for (FaceletsProcessing faceletsProcessing : extension.getFaceletsProcessingList())
            {
                if (faceletsProcessing.getFileExtension() != null && faceletsProcessing.getFileExtension().length() > 0)
                {
                    faceletsProcessingByFileExtension.put(faceletsProcessing.getFileExtension(), faceletsProcessing);
                }
            }
        }

        lifecyclePhaseListeners.addAll(config.getLifecyclePhaseListener());
        managedBeans.addAll(config.getManagedBeans());
        navigationRules.addAll(config.getNavigationRules());
        facesVersion = config.getVersion();
        namedEvents.addAll(config.getNamedEvents());
    }

    /**
     * Add another ApplicationFactory class name
     * 
     * @param factoryClassName
     *            a class name
     */
    public void feedApplicationFactory(String factoryClassName)
    {
        applicationFactories.add(factoryClassName);
    }

    public void feedExceptionHandlerFactory(String factoryClassName)
    {
        exceptionHandlerFactories.add(factoryClassName);
    }

    public void feedExternalContextFactory(String factoryClassName)
    {
        externalContextFactories.add(factoryClassName);
    }

    /**
     * Add another FacesContextFactory class name
     * 
     * @param factoryClassName
     *            a class name
     */
    public void feedFacesContextFactory(String factoryClassName)
    {
        facesContextFactories.add(factoryClassName);
    }

    /**
     * Add another LifecycleFactory class name
     * 
     * @param factoryClassName
     *            a class name
     */
    public void feedLifecycleFactory(String factoryClassName)
    {
        lifecycleFactories.add(factoryClassName);
    }

    public void feedViewDeclarationLanguageFactory(String factoryClassName)
    {
        ViewDeclarationLanguageFactories.add(factoryClassName);
    }

    public void feedPartialViewContextFactory(String factoryClassName)
    {
        partialViewContextFactories.add(factoryClassName);
    }

    /**
     * Add another RenderKitFactory class name
     * 
     * @param factoryClassName
     *            a class name
     */
    public void feedRenderKitFactory(String factoryClassName)
    {
        renderKitFactories.add(factoryClassName);
    }

    public void feedTagHandlerDelegateFactory(String factoryClassName)
    {
        tagHandlerDelegateFactories.add(factoryClassName);
    }

    public void feedVisitContextFactory(String factoryClassName)
    {
        visitContextFactories.add(factoryClassName);
    }

    /**
     * @return Collection over ApplicationFactory class names
     */
    public Collection<String> getApplicationFactoryIterator()
    {
        return applicationFactories;
    }

    public Collection<String> getExceptionHandlerFactoryIterator()
    {
        return exceptionHandlerFactories;
    }

    public Collection<String> getExternalContextFactoryIterator()
    {
        return externalContextFactories;
    }

    /**
     * @return Collection over FacesContextFactory class names
     */
    public Collection<String> getFacesContextFactoryIterator()
    {
        return facesContextFactories;
    }

    /**
     * @return Collection over LifecycleFactory class names
     */
    public Collection<String> getLifecycleFactoryIterator()
    {
        return lifecycleFactories;
    }

    public Collection<String> getViewDeclarationLanguageFactoryIterator()
    {
        return ViewDeclarationLanguageFactories;
    }

    public Collection<String> getPartialViewContextFactoryIterator()
    {
        return partialViewContextFactories;
    }

    /**
     * @return Collection over RenderKit factory class names
     */
    public Collection<String> getRenderKitFactoryIterator()
    {
        return renderKitFactories;
    }

    public Collection<String> getTagHandlerDelegateFactoryIterator()
    {
        return tagHandlerDelegateFactories;
    }

    public Collection<String> getVisitContextFactoryIterator()
    {
        return visitContextFactories;
    }

    /**
     * @return Collection over ActionListener class names
     */
    public Collection<String> getActionListenerIterator()
    {
        return new ArrayList<String>(actionListeners);
    }

    /**
     * @return the default render kit id
     */
    public String getDefaultRenderKitId()
    {
        return defaultRenderKitId;
    }

    /**
     * @return Collection over message bundle names
     */
    public String getMessageBundle()
    {
        return messageBundle;
    }

    /**
     * @return Collection over NavigationHandler class names
     */
    public Collection<String> getNavigationHandlerIterator()
    {
        return new ArrayList<String>(navigationHandlers);
    }

    /**
     * @return the partial traversal class name
     */
    public String getPartialTraversal ()
    {
        return partialTraversal;
    }
    
    /**
     * @return Collection over ResourceHandler class names
     */
    public Collection<String> getResourceHandlerIterator()
    {
        return new ArrayList<String>(resourceHandlers);
    }

    /**
     * @return Collection over ViewHandler class names
     */
    public Collection<String> getViewHandlerIterator()
    {
        return new ArrayList<String>(viewHandlers);
    }

    /**
     * @return Collection over StateManager class names
     */
    public Collection<String> getStateManagerIterator()
    {
        return new ArrayList<String>(stateManagers);
    }

    /**
     * @return Collection over PropertyResolver class names
     */
    public Collection<String> getPropertyResolverIterator()
    {
        return new ArrayList<String>(propertyResolver);
    }

    /**
     * @return Collection over VariableResolver class names
     */
    public Collection<String> getVariableResolverIterator()
    {

        return new ArrayList<String>(variableResolver);
    }

    /**
     * @return the default locale name
     */
    public String getDefaultLocale()
    {
        if (localeConfig != null)
        {
            return localeConfig.getDefaultLocale();
        }
        return null;
    }

    /**
     * @return Collection over supported locale names
     */
    public Collection<String> getSupportedLocalesIterator()
    {
        List<String> locale;
        if (localeConfig != null)
        {
            locale = localeConfig.getSupportedLocales();
        }
        else
        {
            locale = Collections.emptyList();
        }

        return locale;
    }

    /**
     * @return Collection over all defined component types
     */
    public Collection<String> getComponentTypes()
    {
        return components.keySet();
    }

    /**
     * @return component class that belongs to the given component type
     */
    public String getComponentClass(String componentType)
    {
        return components.get(componentType);
    }

    /**
     * @return Collection over all defined converter ids
     */
    public Collection<String> getConverterIds()
    {
        return converterById.keySet();
    }

    /**
     * @return Collection over all classes with an associated converter
     */
    public Collection<String> getConverterClasses()
    {
        return converterByClass.keySet();
    }

    public Collection<String> getConverterConfigurationByClassName()
    {
        return converterConfigurationByClassName.keySet();
    }

    public Converter getConverterConfiguration(String converterClassName)
    {
        return converterConfigurationByClassName.get(converterClassName);
    }

    /**
     * @return converter class that belongs to the given converter id
     */
    public String getConverterClassById(String converterId)
    {
        return converterById.get(converterId);
    }

    /**
     * @return converter class that is associated with the given class name
     */
    public String getConverterClassByClass(String className)
    {
        return converterByClass.get(className);
    }

    /**
     * @return Collection over all defined default validator ids
     */
    public Collection<String> getDefaultValidatorIds ()
    {
        List<String> allDefaultValidatorIds = new ArrayList<String>();
        allDefaultValidatorIds.addAll(defaultAnnotatedValidatorIds);
        allDefaultValidatorIds.addAll(defaultValidatorIds);
        return allDefaultValidatorIds;
    }
    
    /**
     * @return Collection over all defined validator ids
     */
    public Collection<String> getValidatorIds()
    {
        return validators.keySet();
    }

    /**
     * @return validator class name that belongs to the given validator id
     */
    public String getValidatorClass(String validatorId)
    {
        return validators.get(validatorId);
    }

    /**
     * @return Collection over {@link org.apache.myfaces.config.element.ManagedBean ManagedBean}s
     */
    public Collection<ManagedBean> getManagedBeans()
    {
        return managedBeans;
    }

    /**
     * @return Collection over {@link org.apache.myfaces.config.element.NavigationRule NavigationRule}s
     */
    public Collection<NavigationRule> getNavigationRules()
    {
        return navigationRules;
    }

    /**
     * @return Collection over all defined renderkit ids
     */
    public Collection<String> getRenderKitIds()
    {
        return renderKits.keySet();
    }

    /**
     * @return renderkit class name for given renderkit id
     */
    public Collection<String> getRenderKitClasses(String renderKitId)
    {
        return renderKits.get(renderKitId).getRenderKitClasses();
    }

    /**
     * @return Iterator over
     * {@link org.apache.myfaces.config.element.ClientBehaviorRenderer ClientBehaviorRenderer}s
     * for the given renderKitId
     */
    public Collection<ClientBehaviorRenderer> getClientBehaviorRenderers (String renderKitId)
    {
        return renderKits.get (renderKitId).getClientBehaviorRenderers();
    }
    
    /**
     * @return Collection over {@link org.apache.myfaces.config.element.Renderer Renderer}s for the given renderKitId
     */
    public Collection<Renderer> getRenderers(String renderKitId)
    {
        return renderKits.get(renderKitId).getRenderer();
    }

    /**
     * @return Collection over {@link javax.faces.event.PhaseListener} implementation class names
     */
    public Collection<String> getLifecyclePhaseListeners()
    {
        return lifecyclePhaseListeners;
    }

    public Collection<ResourceBundle> getResourceBundles()
    {
        return resourceBundles;
    }

    public Collection<String> getElResolvers()
    {
        return elResolvers;
    }

    public Collection<SystemEventListener> getSystemEventListeners()
    {        
        return systemEventListeners;
    }
    
    public Collection<Behavior> getBehaviors ()
    {
        return behaviors;
    }
    
    public String getFacesVersion ()
    {
        return facesVersion;
    }
    
    public Collection<NamedEvent> getNamedEvents()
    {
        return namedEvents;
    }
    
    public Collection<FaceletsProcessing> getFaceletsProcessing()
    {
        return faceletsProcessingByFileExtension.values();
    }

    public FaceletsProcessing getFaceletsProcessingConfiguration(String fileExtension)
    {
        return faceletsProcessingByFileExtension.get(fileExtension);
    }

    @Override
    public void feedFaceletCacheFactory(String factoryClassName)
    {
        faceletCacheFactories.add(factoryClassName);
    }

    @Override
    public Collection<String> getFaceletCacheFactoryIterator()
    {
        return faceletCacheFactories;
    }

}
