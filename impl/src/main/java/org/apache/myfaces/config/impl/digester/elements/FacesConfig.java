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
package org.apache.myfaces.config.impl.digester.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class FacesConfig
{

    private List<Application> applications = new ArrayList<Application>();
    private List<Factory> factories = new ArrayList<Factory>();
    private Map<String, String> components = new HashMap<String, String>();
    private List<Converter> converters = new ArrayList<Converter>();
    private List<ManagedBean> managedBeans = new ArrayList<ManagedBean>();
    private List<NavigationRule> navigationRules = new ArrayList<NavigationRule>();
    private List<RenderKit> renderKits = new ArrayList<RenderKit>();
    private List<String> lifecyclePhaseListener = new ArrayList<String>();
    private Map<String, String> validators = new HashMap<String, String>();
    
    private String metadataComplete;
    //Ordering variables
    //This information are not merged, and helps
    //with preprocessing of faces-config files
    private String name;
    private AbsoluteOrdering absoluteOrdering;
    private Ordering ordering;

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

    public List<Application> getApplications()
    {
        return applications;
    }

    public List<Factory> getFactories()
    {
        return factories;
    }

    public Map<String, String> getComponents()
    {
        return components;
    }

    public List<Converter> getConverters()
    {
        return converters;
    }

    public List<ManagedBean> getManagedBeans()
    {
        return managedBeans;
    }

    public List<NavigationRule> getNavigationRules()
    {
        return navigationRules;
    }

    public List<RenderKit> getRenderKits()
    {
        return renderKits;
    }

    public List<String> getLifecyclePhaseListener()
    {
        return lifecyclePhaseListener;
    }

    public Map<String, String> getValidators()
    {
        return validators;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public AbsoluteOrdering getAbsoluteOrdering()
    {
        return absoluteOrdering;
    }

    public void setAbsoluteOrdering(AbsoluteOrdering absoluteOrdering)
    {
        this.absoluteOrdering = absoluteOrdering;
    }

    public Ordering getOrdering()
    {
        return ordering;
    }

    public void setOrdering(Ordering ordering)
    {
        this.ordering = ordering;
    }

    public String getMetadataComplete()
    {
        return metadataComplete;
    }

    public void setMetadataComplete(String metadataComplete)
    {
        this.metadataComplete = metadataComplete;
    }
}
