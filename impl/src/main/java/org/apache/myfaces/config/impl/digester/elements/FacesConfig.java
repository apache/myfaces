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
package org.apache.myfaces.config.impl.digester.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.Converter;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class FacesConfig
{

    private List applications = new ArrayList();
    private List factories = new ArrayList();
    private Map components = new HashMap();
    private List converters = new ArrayList();
    private List managedBeans = new ArrayList();
    private List navigationRules = new ArrayList();
    private List renderKits = new ArrayList();
    private List lifecyclePhaseListener = new ArrayList();
    private Map validators = new HashMap();


    public void addApplication(Application application)
    {
        applications.add(application);
    }


    public void addFactory(Factory factory)
    {
        factories.add(factory);
    }


    public void addComponent(String componentType, String componentClass)
    {
        components.put(componentType, componentClass);
    }


    public void addConverter(Converter converter)
    {
        converters.add(converter);
    }


    public void addManagedBean(ManagedBean bean)
    {
        managedBeans.add(bean);
    }


    public void addNavigationRule(NavigationRule rule)
    {
        navigationRules.add(rule);
    }


    public void addRenderKit(RenderKit renderKit)
    {
        renderKits.add(renderKit);
    }


    public void addLifecyclePhaseListener(String value)
    {
        lifecyclePhaseListener.add(value);
    }


    public void addValidator(String id, String validatorClass)
    {
        validators.put(id, validatorClass);
    }


    public List getApplications()
    {
        return applications;
    }


    public List getFactories()
    {
        return factories;
    }


    public Map getComponents()
    {
        return components;
    }


    public List getConverters()
    {
        return converters;
    }


    public List getManagedBeans()
    {
        return managedBeans;
    }


    public List getNavigationRules()
    {
        return navigationRules;
    }


    public List getRenderKits()
    {
        return renderKits;
    }


    public List getLifecyclePhaseListener()
    {
        return lifecyclePhaseListener;
    }


    public Map getValidators()
    {
        return validators;
    }
}
