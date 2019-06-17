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
import java.util.List;

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class FactoryImpl extends org.apache.myfaces.config.element.Factory implements Serializable
{
    private List<String> applicationFactories = new ArrayList<>();
    private List<String> exceptionHandlerFactories = new ArrayList<>();
    private List<String> externalContextFactories = new ArrayList<>();
    private List<String> facesContextFactories = new ArrayList<>();
    private List<String> lifecycleFactories = new ArrayList<>();
    private List<String> ViewDeclarationLanguageFactories = new ArrayList<>();
    private List<String> partialViewContextFactories = new ArrayList<>();
    private List<String> renderKitFactories = new ArrayList<>();
    private List<String> tagHandlerDelegateFactories = new ArrayList<>();
    private List<String> visitContextFactories = new ArrayList<>();
    private List<String> faceletCacheFactories = new ArrayList<>();
    private List<String> flowHandlerFactories = new ArrayList<>();
    private List<String> flashFactories = new ArrayList<>();
    private List<String> clientWindowFactories = new ArrayList<>();
    private List<String> searchExpressionContextFactories = new ArrayList<>();

    public void addApplicationFactory(String factory)
    {
        applicationFactories.add(factory);
    }

    public void addExceptionHandlerFactory(String factory)
    {
        exceptionHandlerFactories.add(factory);
    }

    public void addExternalContextFactory(String factory)
    {
        externalContextFactories.add(factory);
    }

    public void addFacesContextFactory(String factory)
    {
        facesContextFactories.add(factory);
    }

    public void addLifecycleFactory(String factory)
    {
        lifecycleFactories.add(factory);
    }

    public void addViewDeclarationLanguageFactory(String factory)
    {
        ViewDeclarationLanguageFactories.add(factory);
    }

    public void addPartialViewContextFactory(String factory)
    {
        partialViewContextFactories.add(factory);
    }

    public void addRenderkitFactory(String factory)
    {
        renderKitFactories.add(factory);
    }

    public void addTagHandlerDelegateFactory(String factory)
    {
        tagHandlerDelegateFactories.add(factory);
    }

    public void addVisitContextFactory(String factory)
    {
        visitContextFactories.add(factory);
    }
    
    public void addFaceletCacheFactory(String factory)
    {
        faceletCacheFactories.add(factory);
    }

    public void addFlashFactory(String factory)
    {
        flashFactories.add(factory);
    }
    
    public void addFlowHandlerFactory(String factory)
    {
        flowHandlerFactories.add(factory);
    }

    public void addClientWindowFactory(String factory)
    {
        clientWindowFactories.add(factory);
    }
    
    @Override
    public List<String> getApplicationFactory()
    {
        return applicationFactories;
    }

    @Override
    public List<String> getExceptionHandlerFactory()
    {
        return exceptionHandlerFactories;
    }

    @Override
    public List<String> getExternalContextFactory()
    {
        return externalContextFactories;
    }

    @Override
    public List<String> getFacesContextFactory()
    {
        return facesContextFactories;
    }

    @Override
    public List<String> getLifecycleFactory()
    {
        return lifecycleFactories;
    }

    @Override
    public List<String> getViewDeclarationLanguageFactory()
    {
        return ViewDeclarationLanguageFactories;
    }

    @Override
    public List<String> getPartialViewContextFactory()
    {
        return partialViewContextFactories;
    }

    @Override
    public List<String> getRenderkitFactory()
    {
        return renderKitFactories;
    }

    @Override
    public List<String> getTagHandlerDelegateFactory()
    {
        return tagHandlerDelegateFactories;
    }

    @Override
    public List<String> getVisitContextFactory()
    {
        return visitContextFactories;
    }

    @Override
    public List<String> getFaceletCacheFactory()
    {
        return faceletCacheFactories;
    }

    @Override
    public List<String> getFlashFactory()
    {
        return flashFactories;
    }

    @Override
    public List<String> getFlowHandlerFactory()
    {
        return flowHandlerFactories;
    }

    @Override
    public List<String> getClientWindowFactory()
    {
        return clientWindowFactories;
    }
    
    public void addSearchExpressionContextFactory(String factory)
    {
        searchExpressionContextFactories.add(factory);
    }
    
    @Override
    public List<String> getSearchExpressionContextFactory()
    {
        return searchExpressionContextFactories;
    }

}
