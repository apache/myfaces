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
package org.apache.myfaces.config.impl.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.myfaces.config.element.ContractMapping;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class ApplicationImpl extends org.apache.myfaces.config.element.Application implements Serializable
{
    private List<String> actionListener;
    private List<String> defaultRenderkitId;
    private List<String> defaultValidatorIds;
    private List<String> messageBundle;
    private List<String> navigationHandler;
    private List<String> resourceHandler;
    private List<String> viewHandler;
    private List<String> stateManager;
    private List<String> propertyResolver;
    private List<String> variableResolver;
    private List<org.apache.myfaces.config.element.LocaleConfig> localeConfig;
    private List<String> elResolver;
    private List<org.apache.myfaces.config.element.ResourceBundle> resourceBundle;
    private List<org.apache.myfaces.config.element.SystemEventListener> systemEventListeners;
    private List<ContractMapping> resourceLibraryContractMappings;
    private List<String> searchKeywordResolver;
    private List<String> searchExpressionHandler;
    
    private boolean defaultValidatorsPresent = false;
    
    public void addActionListener(String listener)
    {
        if (actionListener == null)
        {
            actionListener = new ArrayList<>();
        }
        actionListener.add(listener);
    }

    public void addDefaultRenderkitId(String id)
    {
        if (defaultRenderkitId == null)
        {
            defaultRenderkitId = new ArrayList<>();
        }
        defaultRenderkitId.add(id);
    }
    
    public void addDefaultValidatorId (String id)
    {
        if (defaultValidatorIds == null)
        {
            defaultValidatorIds = new ArrayList<>();
        }
        defaultValidatorIds.add(id);
    }
    
    public void addMessageBundle(String bundle)
    {
        if (messageBundle == null)
        {
            messageBundle = new ArrayList<>();
        }
        messageBundle.add(bundle);
    }

    public void addNavigationHandler(String handler)
    {
        if (navigationHandler == null)
        {
            navigationHandler = new ArrayList<>();
        }
        navigationHandler.add(handler);
    }

    public void addStateManager(String manager)
    {
        if (stateManager == null)
        {
            stateManager = new ArrayList<>();
        }
        stateManager.add(manager);
    }
    
    public void addSystemEventListener(org.apache.myfaces.config.element.SystemEventListener systemEventListener)
    {
        if (systemEventListeners == null)
        {
            systemEventListeners = new ArrayList<>();
        }
        systemEventListeners.add (systemEventListener);
    }
    
    public void addPropertyResolver(String resolver)
    {
        if (propertyResolver == null)
        {
            propertyResolver = new ArrayList<>();
        }
        propertyResolver.add(resolver);
    }

    public void addVariableResolver(String handler)
    {
        if (variableResolver == null)
        {
            variableResolver = new ArrayList<>();
        }
        variableResolver.add(handler);
    }

    public void addLocaleConfig(org.apache.myfaces.config.element.LocaleConfig config)
    {
        if (localeConfig == null)
        {
            localeConfig = new ArrayList<>();
        }
        localeConfig.add(config);
    }

    public void addResourceHandler(String handler)
    {
        if (resourceHandler == null)
        {
            resourceHandler = new ArrayList<>();
        }
        resourceHandler.add(handler);
    }

    public void addViewHandler(String handler)
    {
        if (viewHandler == null)
        {
            viewHandler = new ArrayList<>();
        }
        viewHandler.add(handler);
    }

    public void addElResolver(String handler)
    {
        if (elResolver == null)
        {
            elResolver = new ArrayList<>();
        }
        elResolver.add(handler);
    }

    public void addResourceBundle(org.apache.myfaces.config.element.ResourceBundle bundle)
    {
        if (resourceBundle == null)
        {
            resourceBundle = new ArrayList<>();
        }
        resourceBundle.add(bundle);
    }

    @Override
    public List<String> getActionListener()
    {
        if (actionListener == null)
        {
            return Collections.emptyList();
        }
        return actionListener;
    }

    @Override
    public List<String> getDefaultRenderkitId()
    {
        if (defaultRenderkitId == null)
        {
            return Collections.emptyList();
        }
        return defaultRenderkitId;
    }
    
    @Override
    public List<String> getDefaultValidatorIds()
    {
        if (defaultValidatorIds == null)
        {
            return Collections.emptyList();
        }
        return defaultValidatorIds;
    }
    
    @Override
    public List<String> getMessageBundle()
    {
        if (messageBundle == null)
        {
            return Collections.emptyList();
        }
        return messageBundle;
    }

    @Override
    public List<String> getNavigationHandler()
    {
        if (navigationHandler == null)
        {
            return Collections.emptyList();
        }
        return navigationHandler;
    }

    @Override
    public List<String> getResourceHandler()
    {
        if (resourceHandler == null)
        {
            return Collections.emptyList();
        }
        return resourceHandler;
    }
    
    @Override
    public List<org.apache.myfaces.config.element.SystemEventListener> getSystemEventListeners()
    {
        if (systemEventListeners == null)
        {
            return Collections.emptyList();
        }
        return systemEventListeners;
    }
    
    @Override
    public List<String> getViewHandler()
    {
        if (viewHandler == null)
        {
            return Collections.emptyList();
        }
        return viewHandler;
    }

    @Override
    public List<String> getStateManager()
    {
        if (stateManager == null)
        {
            return Collections.emptyList();
        }
        return stateManager;
    }

    @Override
    public List<String> getPropertyResolver()
    {
        if (propertyResolver == null)
        {
            return Collections.emptyList();
        }
        return propertyResolver;
    }

    @Override
    public List<String> getVariableResolver()
    {
        if (variableResolver == null)
        {
            return Collections.emptyList();
        }
        return variableResolver;
    }

    @Override
    public List<org.apache.myfaces.config.element.LocaleConfig> getLocaleConfig()
    {
        if (localeConfig == null)
        {
            return Collections.emptyList();
        }
        return localeConfig;
    }

    @Override
    public List<String> getElResolver()
    {
        if (elResolver == null)
        {
            return Collections.emptyList();
        }
        return elResolver;
    }

    @Override
    public List<org.apache.myfaces.config.element.ResourceBundle> getResourceBundle()
    {
        if (resourceBundle == null)
        {
            return Collections.emptyList();
        }
        return resourceBundle;
    }

    @Override
    public boolean isDefaultValidatorsPresent()
    {
        return defaultValidatorsPresent;
    }

    public void setDefaultValidatorsPresent()
    {
        defaultValidatorsPresent = true;
    }

    @Override
    public List<ContractMapping> getResourceLibraryContractMappings()
    {
        if (resourceLibraryContractMappings == null)
        {
            return Collections.emptyList();
        }
        return resourceLibraryContractMappings;
    }
    
    public void addResourceLibraryContractMapping(ContractMapping mapping)
    {
        if (resourceLibraryContractMappings == null)
        {
            resourceLibraryContractMappings = new ArrayList<>();
        }
        resourceLibraryContractMappings.add(mapping);
    }

    @Override
    public List<String> getSearchKeywordResolver()
    {
        if (searchKeywordResolver == null)
        {
            return Collections.emptyList();
        }
        return searchKeywordResolver;
    }
    
    public void addSearchKeywordResolver(String resolver)
    {
        if (searchKeywordResolver == null)
        {
            searchKeywordResolver = new ArrayList<>();
        }
        searchKeywordResolver.add(resolver);
    }

    @Override
    public List<String> getSearchExpressionHandler()
    {
        if (searchExpressionHandler == null)
        {
            return Collections.emptyList();
        }
        return searchExpressionHandler;
    }
    
    public void addSearchExpressionHandler(String handler)
    {
        if (searchExpressionHandler == null)
        {
            searchExpressionHandler = new ArrayList<>();
        }
        searchExpressionHandler.add(handler);
    }

}
