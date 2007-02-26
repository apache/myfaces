/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.impl.digester;

import org.apache.myfaces.config.FacesConfigDispenser;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.Converter;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.Factory;
import org.apache.myfaces.config.impl.digester.elements.LocaleConfig;
import org.apache.myfaces.config.impl.digester.elements.RenderKit;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;

import javax.faces.render.RenderKitFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class DigesterFacesConfigDispenserImpl implements
        FacesConfigDispenser<FacesConfig>
{

    private List<FacesConfig> configs = new ArrayList<FacesConfig>();
    private List<String> applicationFactories = new ArrayList<String>();
    private List<String> facesContextFactories = new ArrayList<String>();
    private List<String> lifecycleFactories = new ArrayList<String>();
    private List<String> renderKitFactories = new ArrayList<String>();
    private Map<String, String> components = new HashMap<String, String>();
    private Map<String, String> validators = new HashMap<String, String>();
    private String defaultRenderKitId;
    private LocaleConfig localeConfig;
    private List<String> actionListeners = new ArrayList<String>();
    private List<String> lifecyclePhaseListeners = new ArrayList<String>();
    private String messageBundle;
    private List<String> navigationHandlers = new ArrayList<String>();
    private List<String> viewHandlers = new ArrayList<String>();
    private List<String> stateManagers = new ArrayList<String>();
    private List<String> propertyResolver = new ArrayList<String>();
    private List<String> variableResolver = new ArrayList<String>();
    private Map<String, String> converterById = new HashMap<String, String>();
    private Map<String, String> converterByClass = new HashMap<String, String>();
    private Map<String, Converter> converterConfigurationByClassName = new HashMap<String, Converter>();
    private Map<String, RenderKit> renderKits = new LinkedHashMap<String, RenderKit>();
    private List<ManagedBean> managedBeans = new ArrayList<ManagedBean>();
    private List<NavigationRule> navigationRules = new ArrayList<NavigationRule>();
    private List<ResourceBundle> resourceBundles = new ArrayList<ResourceBundle>();
    private List<String> elResolvers = new ArrayList<String>();

    /**
     * Add another unmarshalled faces config object.
     * 
     * @param facesConfig
     *            unmarshalled faces config object
     */
    public void feed(FacesConfig config)
    {
        configs.add(config);
        for (Iterator iterator = config.getFactories().iterator(); iterator
                .hasNext();)
        {
            Factory factory = (Factory) iterator.next();
            applicationFactories.addAll(factory.getApplicationFactory());
            facesContextFactories.addAll(factory.getFacesContextFactory());
            lifecycleFactories.addAll(factory.getLifecycleFactory());
            renderKitFactories.addAll(factory.getRenderkitFactory());
        }
        components.putAll(config.getComponents());
        validators.putAll(config.getValidators());

        for (Iterator iterator = config.getApplications().iterator(); iterator
                .hasNext();)
        {
            Application application = (Application) iterator.next();
            if (!application.getDefaultRenderkitId().isEmpty())
            {
                defaultRenderKitId = application
                        .getDefaultRenderkitId().get(
                                application.getDefaultRenderkitId().size() - 1);
            }
            if (!application.getMessageBundle().isEmpty())
            {
                messageBundle = application.getMessageBundle().get(
                        application.getMessageBundle().size() - 1);
            }
            if (!application.getLocaleConfig().isEmpty())
            {
                localeConfig = application.getLocaleConfig()
                        .get(application.getLocaleConfig().size() - 1);
            }
            actionListeners.addAll(application.getActionListener());
            navigationHandlers.addAll(application.getNavigationHandler());
            viewHandlers.addAll(application.getViewHandler());
            stateManagers.addAll(application.getStateManager());
            propertyResolver.addAll(application.getPropertyResolver());
            variableResolver.addAll(application.getVariableResolver());
            resourceBundles.addAll(application.getResourceBundle());
            elResolvers.addAll(application.getElResolver());
        }
        for (Iterator iterator = config.getConverters().iterator(); iterator
                .hasNext();)
        {
            Converter converter = (Converter) iterator.next();

            if (converter.getConverterId() != null)
            {
                converterById.put(converter.getConverterId(), converter
                        .getConverterClass());
            }
            else
            {
                converterByClass.put(converter.getForClass(), converter
                        .getConverterClass());
            }

            converterConfigurationByClassName.put(
                    converter.getConverterClass(), converter);
        }

        for (Iterator iterator = config.getRenderKits().iterator(); iterator
                .hasNext();)
        {
            RenderKit renderKit = (RenderKit) iterator.next();
            String renderKitId = renderKit.getId();

            if (renderKitId == null)
            {
                renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }

            RenderKit existing = renderKits.get(renderKitId);

            if (existing == null)
            {
                renderKits.put(renderKit.getId(), renderKit);
            }
            else
            {
                existing.merge(renderKit);
            }
        }
        lifecyclePhaseListeners.addAll(config.getLifecyclePhaseListener());
        managedBeans.addAll(config.getManagedBeans());
        navigationRules.addAll(config.getNavigationRules());
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

    /**
     * @return Iterator over ApplicationFactory class names
     */
    public Iterator<String> getApplicationFactoryIterator()
    {
        return applicationFactories.iterator();
    }

    /**
     * @return Iterator over FacesContextFactory class names
     */
    public Iterator<String> getFacesContextFactoryIterator()
    {
        return facesContextFactories.iterator();
    }

    /**
     * @return Iterator over LifecycleFactory class names
     */
    public Iterator<String> getLifecycleFactoryIterator()
    {
        return lifecycleFactories.iterator();
    }

    /**
     * @return Iterator over RenderKit factory class names
     */
    public Iterator<String> getRenderKitFactoryIterator()
    {
        return renderKitFactories.iterator();
    }

    /**
     * @return Iterator over ActionListener class names
     */
    public Iterator<String> getActionListenerIterator()
    {
        List<String> listeners = new ArrayList<String>(actionListeners);
        return listeners.iterator();
    }

    /**
     * @return the default render kit id
     */
    public String getDefaultRenderKitId()
    {
        return defaultRenderKitId;
    }

    /**
     * @return Iterator over message bundle names
     */
    public String getMessageBundle()
    {
        return messageBundle;
    }

    /**
     * @return Iterator over NavigationHandler class names
     */
    public Iterator<String> getNavigationHandlerIterator()
    {
        List<String> handlers = new ArrayList<String>(navigationHandlers);
        return handlers.iterator();
    }

    /**
     * @return Iterator over ViewHandler class names
     */
    public Iterator<String> getViewHandlerIterator()
    {
        List<String> handlers = new ArrayList<String>(viewHandlers);
        return handlers.iterator();
    }

    /**
     * @return Iterator over StateManager class names
     */
    public Iterator<String> getStateManagerIterator()
    {
        List<String> managers = new ArrayList<String>(stateManagers);
        return managers.iterator();
    }

    /**
     * @return Iterator over PropertyResolver class names
     */
    public Iterator<String> getPropertyResolverIterator()
    {
        List<String> resolver = new ArrayList<String>(propertyResolver);
        return resolver.iterator();
    }

    /**
     * @return Iterator over VariableResolver class names
     */
    public Iterator<String> getVariableResolverIterator()
    {
        List<String> resolver = new ArrayList<String>(variableResolver);

        return resolver.iterator();
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
     * @return Iterator over supported locale names
     */
    public Iterator getSupportedLocalesIterator()
    {
        if (localeConfig != null)
        {
            return localeConfig.getSupportedLocales().iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * @return Iterator over all defined component types
     */
    public Iterator getComponentTypes()
    {
        return components.keySet().iterator();
    }

    /**
     * @return component class that belongs to the given component type
     */
    public String getComponentClass(String componentType)
    {
        return components.get(componentType);
    }

    /**
     * @return Iterator over all defined converter ids
     */
    public Iterator<String> getConverterIds()
    {
        return converterById.keySet().iterator();
    }

    /**
     * @return Iterator over all classes with an associated converter
     */
    public Iterator<String> getConverterClasses()
    {
        return converterByClass.keySet().iterator();
    }

    public Iterator<String> getConverterConfigurationByClassName()
    {
        return converterConfigurationByClassName.keySet().iterator();
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
     * @return Iterator over all defined validator ids
     */
    public Iterator getValidatorIds()
    {
        return validators.keySet().iterator();
    }

    /**
     * @return validator class name that belongs to the given validator id
     */
    public String getValidatorClass(String validatorId)
    {
        return validators.get(validatorId);
    }

    /**
     * @return Iterator over
     *         {@link org.apache.myfaces.config.element.ManagedBean ManagedBean}s
     */
    public Iterator<ManagedBean> getManagedBeans()
    {
        return managedBeans.iterator();
    }

    /**
     * @return Iterator over
     *         {@link org.apache.myfaces.config.element.NavigationRule NavigationRule}s
     */
    public Iterator<NavigationRule> getNavigationRules()
    {
        return navigationRules.iterator();
    }

    /**
     * @return Iterator over all defined renderkit ids
     */
    public Iterator<String> getRenderKitIds()
    {
        return renderKits.keySet().iterator();
    }

    /**
     * @return renderkit class name for given renderkit id
     */
    public String getRenderKitClass(String renderKitId)
    {
        RenderKit renderKit = renderKits.get(renderKitId);
        return renderKit.getRenderKitClass();
    }

    /**
     * @return Iterator over
     *         {@link org.apache.myfaces.config.element.Renderer Renderer}s for
     *         the given renderKitId
     */
    public Iterator<Renderer> getRenderers(String renderKitId)
    {
        RenderKit renderKit = renderKits.get(renderKitId);
        return renderKit.getRenderer().iterator();
    }

    /**
     * @return Iterator over {@link javax.faces.event.PhaseListener}
     *         implementation class names
     */
    public Iterator<String> getLifecyclePhaseListeners()
    {
        return lifecyclePhaseListeners.iterator();
    }

    public Iterator<ResourceBundle> getResourceBundles()
    {
        return resourceBundles.iterator();
    }

    public Iterator<String> getElResolvers()
    {
        return elResolvers.iterator();
    }

}
