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

import java.util.List;
import java.util.ArrayList;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class Application extends ElementBaseImpl
{

    private List actionListener = new ArrayList();
    private List defaultRenderkitId = new ArrayList();
    private List messageBundle = new ArrayList();
    private List navigationHandler = new ArrayList();
    private List viewHandler = new ArrayList();
    private List stateManager = new ArrayList();
    private List propertyResolver = new ArrayList();
    private List variableResolver = new ArrayList();
    private List localeConfig = new ArrayList();


    public void addActionListener(String listener)
    {
        actionListener.add(listener);
    }


    public void addDefaultRenderkitId(String id)
    {
        defaultRenderkitId.add(id);
    }


    public void addMessageBundle(String bundle)
    {
        messageBundle.add(bundle);
    }


    public void addNavigationHandler(String handler)
    {
        navigationHandler.add(handler);
    }


    public void addStateManager(String manager)
    {
        stateManager.add(manager);
    }


    public void addPropertyResolver(String resolver)
    {
        propertyResolver.add(resolver);
    }


    public void addVariableResolver(String handler)
    {
        variableResolver.add(handler);
    }


    public void addLocaleConfig(LocaleConfig config)
    {
        localeConfig.add(config);
    }


    public void addViewHandler(String handler)
    {
        viewHandler.add(handler);
    }


    public List getActionListener()
    {
        return actionListener;
    }


    public List getDefaultRenderkitId()
    {
        return defaultRenderkitId;
    }


    public List getMessageBundle()
    {
        return messageBundle;
    }


    public List getNavigationHandler()
    {
        return navigationHandler;
    }


    public List getViewHandler()
    {
        return viewHandler;
    }


    public List getStateManager()
    {
        return stateManager;
    }


    public List getPropertyResolver()
    {
        return propertyResolver;
    }


    public List getVariableResolver()
    {
        return variableResolver;
    }


    public List getLocaleConfig()
    {
        return localeConfig;
    }
}
