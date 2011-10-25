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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class FacesConfig extends org.apache.myfaces.config.element.FacesConfig implements Serializable
{

    private List<org.apache.myfaces.config.element.Application> applications
            = new ArrayList<org.apache.myfaces.config.element.Application>();
    private List<org.apache.myfaces.config.element.Factory> factories
            = new ArrayList<org.apache.myfaces.config.element.Factory>();
    private Map<String, String> components = new HashMap<String, String>();
    private List<org.apache.myfaces.config.element.Converter> converters
            = new ArrayList<org.apache.myfaces.config.element.Converter>();
    private List<org.apache.myfaces.config.element.ManagedBean> managedBeans
            = new ArrayList<org.apache.myfaces.config.element.ManagedBean>();
    private List<org.apache.myfaces.config.element.NavigationRule> navigationRules
            = new ArrayList<org.apache.myfaces.config.element.NavigationRule>();
    private List<org.apache.myfaces.config.element.RenderKit> renderKits
            = new ArrayList<org.apache.myfaces.config.element.RenderKit>();
    private List<String> lifecyclePhaseListener = new ArrayList<String>();
    private Map<String, String> validators = new HashMap<String, String>();
    private List<org.apache.myfaces.config.element.Behavior> behaviors
            = new ArrayList<org.apache.myfaces.config.element.Behavior>();
    private List<org.apache.myfaces.config.element.NamedEvent> namedEvents
            = new ArrayList<org.apache.myfaces.config.element.NamedEvent>();
    private List<org.apache.myfaces.config.element.FacesConfigExtension> facesConfigExtensions
            = new ArrayList<org.apache.myfaces.config.element.FacesConfigExtension>();
    
    private String metadataComplete;
    private String version;
    //Ordering variables
    //This information are not merged, and helps
    //with preprocessing of faces-config files
    private String name;
    private org.apache.myfaces.config.element.AbsoluteOrdering absoluteOrdering;
    private org.apache.myfaces.config.element.Ordering ordering;

    public void addApplication(org.apache.myfaces.config.element.Application application)
    {
        applications.add(application);
    }

    public void addFactory(org.apache.myfaces.config.element.Factory factory)
    {
        factories.add(factory);
    }

    public void addComponent(String componentType, String componentClass)
    {
        components.put(componentType, componentClass);
    }

    public void addConverter(org.apache.myfaces.config.element.Converter converter)
    {
        converters.add(converter);
    }

    public void addManagedBean(org.apache.myfaces.config.element.ManagedBean bean)
    {
        managedBeans.add(bean);
    }

    public void addNavigationRule(org.apache.myfaces.config.element.NavigationRule rule)
    {
        navigationRules.add(rule);
    }

    public void addRenderKit(org.apache.myfaces.config.element.RenderKit renderKit)
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
    
    public void addBehavior (org.apache.myfaces.config.element.Behavior behavior)
    {
        behaviors.add (behavior);
    }
    
    public void addNamedEvent (org.apache.myfaces.config.element.NamedEvent namedEvent)
    {
        namedEvents.add(namedEvent);
    }
    
    public List<org.apache.myfaces.config.element.Application> getApplications()
    {
        return applications;
    }

    public List<org.apache.myfaces.config.element.Factory> getFactories()
    {
        return factories;
    }

    public Map<String, String> getComponents()
    {
        return components;
    }

    public List<org.apache.myfaces.config.element.Converter> getConverters()
    {
        return converters;
    }

    public List<org.apache.myfaces.config.element.ManagedBean> getManagedBeans()
    {
        return managedBeans;
    }

    public List<org.apache.myfaces.config.element.NavigationRule> getNavigationRules()
    {
        return navigationRules;
    }

    public List<org.apache.myfaces.config.element.RenderKit> getRenderKits()
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
    
    public List<org.apache.myfaces.config.element.Behavior> getBehaviors ()
    {
        return behaviors;
    }
    
    public List<org.apache.myfaces.config.element.NamedEvent> getNamedEvents ()
    {
        return namedEvents;
    }
    
    public org.apache.myfaces.config.element.RenderKit getRenderKit(String renderKitId)
    {
        for (org.apache.myfaces.config.element.RenderKit rk : getRenderKits())
        {
            if (renderKitId != null && renderKitId.equals(rk.getId()))
            {
                return rk;
            }
            else if (renderKitId == null && rk.getId() == null)
            {
                return rk;
            }
        }
        return null;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public org.apache.myfaces.config.element.AbsoluteOrdering getAbsoluteOrdering()
    {
        return absoluteOrdering;
    }

    public void setAbsoluteOrdering(org.apache.myfaces.config.element.AbsoluteOrdering absoluteOrdering)
    {
        this.absoluteOrdering = absoluteOrdering;
    }

    public org.apache.myfaces.config.element.Ordering getOrdering()
    {
        return ordering;
    }

    public void setOrdering(org.apache.myfaces.config.element.Ordering ordering)
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
    
    public String getVersion ()
    {
        return version;
    }
    
    public void setVersion (String version)
    {
        this.version = version;
    }

    @Override
    public List<org.apache.myfaces.config.element.FacesConfigExtension> getFacesConfigExtensions()
    {
        return facesConfigExtensions;
    }
    
    public void addFacesConfigExtension(org.apache.myfaces.config.element.FacesConfigExtension elem)
    {
        facesConfigExtensions.add(elem);
    }
    
}
