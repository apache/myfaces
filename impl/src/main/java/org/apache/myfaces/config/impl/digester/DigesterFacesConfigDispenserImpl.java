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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.config.FacesConfigDispenser;
import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.Converter;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.Factory;
import org.apache.myfaces.config.impl.digester.elements.LocaleConfig;
import org.apache.myfaces.config.impl.digester.elements.RenderKit;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class DigesterFacesConfigDispenserImpl implements FacesConfigDispenser
{

    private List configs = new ArrayList();
    private List applicationFactories = new ArrayList();
    private List facesContextFactories = new ArrayList();
    private List lifecycleFactories = new ArrayList();
    private List renderKitFactories = new ArrayList();
    private Map components = new HashMap();
    private Map validators = new HashMap();
    private String defaultRenderKitId;
    private LocaleConfig localeConfig;
    private List actionListeners = new ArrayList();
    private List lifecyclePhaseListeners = new ArrayList();
    private String messageBundle;
    private List navigationHandlers = new ArrayList();
    private List viewHandlers = new ArrayList();
    private List stateManagers = new ArrayList();
    private List propertyResolver = new ArrayList();
    private List variableResolver = new ArrayList();
    private Map converterById = new HashMap();
    private Map converterByClass = new HashMap();
    private Map converterConfigurationByClassName = new HashMap();
    private Map renderKits = new HashMap();
    private List managedBeans = new ArrayList();
    private List navigationRules = new ArrayList();


    /**
     * Add another unmarshalled faces config object.
     *
     * @param facesConfig unmarshalled faces config object
     */
    public void feed(Object facesConfig)
    {
        FacesConfig config = (FacesConfig) facesConfig;
        configs.add(config);
        for (Iterator iterator = config.getFactories().iterator(); iterator.hasNext();)
        {
            Factory factory = (Factory) iterator.next();
            applicationFactories.addAll(factory.getApplicationFactory());
            facesContextFactories.addAll(factory.getFacesContextFactory());
            lifecycleFactories.addAll(factory.getLifecycleFactory());
            renderKitFactories.addAll(factory.getRenderkitFactory());
        }
        components.putAll(config.getComponents());
        validators.putAll(config.getValidators());

        for (Iterator iterator = config.getApplications().iterator(); iterator.hasNext();)
        {
            Application application = (Application) iterator.next();
            if (!application.getDefaultRenderkitId().isEmpty())
            {
                defaultRenderKitId = (String) application.getDefaultRenderkitId().get(application.getDefaultRenderkitId().size() - 1);
            }
            if (!application.getMessageBundle().isEmpty())
            {
                messageBundle = (String) application.getMessageBundle().get(application.getMessageBundle().size() - 1);
            }
            if (!application.getLocaleConfig().isEmpty())
            {
                localeConfig = (LocaleConfig) application.getLocaleConfig().get(application.getLocaleConfig().size() - 1);
            }
            actionListeners.addAll(application.getActionListener());
            navigationHandlers.addAll(application.getNavigationHandler());
            viewHandlers.addAll(application.getViewHandler());
            stateManagers.addAll(application.getStateManager());
            propertyResolver.addAll(application.getPropertyResolver());
            variableResolver.addAll(application.getVariableResolver());
        }
        for (Iterator iterator = config.getConverters().iterator(); iterator.hasNext();)
        {
            Converter converter = (Converter) iterator.next();

            if (converter.getConverterId() != null)
            {
                converterById.put(converter.getConverterId(), converter.getConverterClass());
            }
            else
            {
                converterByClass.put(converter.getForClass(), converter.getConverterClass());
            }

            converterConfigurationByClassName.put(converter.getConverterClass(),converter);
        }

        for (Iterator iterator = config.getRenderKits().iterator(); iterator.hasNext();)
        {
            RenderKit renderKit = (RenderKit) iterator.next();
            String renderKitId = renderKit.getId();

            if (renderKitId == null) {
                renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }

            RenderKit existing = (RenderKit) renderKits.get(renderKitId);

            if (existing == null) {
                renderKits.put(renderKit.getId(), renderKit);
            } else {
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
     * @param factoryClassName a class name
     */
    public void feedApplicationFactory(String factoryClassName)
    {
        applicationFactories.add(factoryClassName);
    }


    /**
     * Add another FacesContextFactory class name
     *
     * @param factoryClassName a class name
     */
    public void feedFacesContextFactory(String factoryClassName)
    {
        facesContextFactories.add(factoryClassName);
    }


    /**
     * Add another LifecycleFactory class name
     *
     * @param factoryClassName a class name
     */
    public void feedLifecycleFactory(String factoryClassName)
    {
        lifecycleFactories.add(factoryClassName);
    }


    /**
     * Add another RenderKitFactory class name
     *
     * @param factoryClassName a class name
     */
    public void feedRenderKitFactory(String factoryClassName)
    {
        renderKitFactories.add(factoryClassName);
    }


    /**
     * @return Iterator over ApplicationFactory class names
     */
    public Iterator getApplicationFactoryIterator()
    {
        return applicationFactories.iterator();
    }


    /**
     * @return Iterator over FacesContextFactory class names
     */
    public Iterator getFacesContextFactoryIterator()
    {
        return facesContextFactories.iterator();
    }


    /**
     * @return Iterator over LifecycleFactory class names
     */
    public Iterator getLifecycleFactoryIterator()
    {
        return lifecycleFactories.iterator();
    }


    /**
     * @return Iterator over RenderKit factory class names
     */
    public Iterator getRenderKitFactoryIterator()
    {
        return renderKitFactories.iterator();
    }


    /**
     * @return Iterator over ActionListener class names
     */
    public Iterator getActionListenerIterator()
    {
        List listeners = new ArrayList(actionListeners);
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
    public Iterator getNavigationHandlerIterator()
    {
        List handlers = new ArrayList(navigationHandlers);
        return handlers.iterator();
    }


    /**
     * @return Iterator over ViewHandler class names
     */
    public Iterator getViewHandlerIterator()
    {
        List handlers = new ArrayList(viewHandlers);
        return handlers.iterator();
    }


    /**
     * @return Iterator over StateManager class names
     */
    public Iterator getStateManagerIterator()
    {
        List managers = new ArrayList(stateManagers);
        return managers.iterator();
    }


    /**
     * @return Iterator over PropertyResolver class names
     */
    public Iterator getPropertyResolverIterator()
    {
        List resolver = new ArrayList(propertyResolver);
        return resolver.iterator();
    }


    /**
     * @return Iterator over VariableResolver class names
     */
    public Iterator getVariableResolverIterator()
    {
        List resolver = new ArrayList(variableResolver);

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
        return (String) components.get(componentType);
    }


    /**
     * @return Iterator over all defined converter ids
     */
    public Iterator getConverterIds()
    {
        return converterById.keySet().iterator();
    }


    /**
     * @return Iterator over all classes with an associated converter
     */
    public Iterator getConverterClasses()
    {
        return converterByClass.keySet().iterator();
    }

    public Iterator getConverterConfigurationByClassName()
    {
        return converterConfigurationByClassName.keySet().iterator();
    }

    public Converter getConverterConfiguration(String converterClassName)
    {
        return (Converter) converterConfigurationByClassName.get(converterClassName);
    }


    /**
     * @return converter class that belongs to the given converter id
     */
    public String getConverterClassById(String converterId)
    {
        return (String) converterById.get(converterId);
    }


    /**
     * @return converter class that is associated with the given class name
     */
    public String getConverterClassByClass(String className)
    {
        return (String) converterByClass.get(className);
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
        return (String) validators.get(validatorId);
    }


    /**
     * @return Iterator over {@link org.apache.myfaces.config.element.ManagedBean ManagedBean}s
     */
    public Iterator getManagedBeans()
    {
        return managedBeans.iterator();
    }


    /**
     * @return Iterator over {@link org.apache.myfaces.config.element.NavigationRule NavigationRule}s
     */
    public Iterator getNavigationRules()
    {
        return navigationRules.iterator();
    }


    /**
     * @return Iterator over all defined renderkit ids
     */
    public Iterator getRenderKitIds()
    {
        return renderKits.keySet().iterator();
    }


    /**
     * @return renderkit class name for given renderkit id
     */
    public String getRenderKitClass(String renderKitId)
    {
        RenderKit renderKit = (RenderKit) renderKits.get(renderKitId);
        return renderKit.getRenderKitClass();
    }


    /**
     * @return Iterator over {@link org.apache.myfaces.config.element.Renderer Renderer}s for the given renderKitId
     */
    public Iterator getRenderers(String renderKitId)
    {
        RenderKit renderKit = (RenderKit) renderKits.get(renderKitId);
        return renderKit.getRenderer().iterator();
    }


    /**
     * @return Iterator over {@link javax.faces.event.PhaseListener} implementation class names
     */
    public Iterator getLifecyclePhaseListeners()
    {
        return lifecyclePhaseListeners.iterator();
    }

}
