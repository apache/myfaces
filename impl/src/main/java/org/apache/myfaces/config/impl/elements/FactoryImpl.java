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

/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class FactoryImpl extends org.apache.myfaces.config.element.Factory implements Serializable
{
    private List<String> applicationFactories;
    private List<String> exceptionHandlerFactories;
    private List<String> externalContextFactories;
    private List<String> facesContextFactories;
    private List<String> lifecycleFactories;
    private List<String> viewDeclarationLanguageFactories;
    private List<String> partialViewContextFactories;
    private List<String> renderKitFactories;
    private List<String> tagHandlerDelegateFactories;
    private List<String> visitContextFactories;
    private List<String> faceletCacheFactories;
    private List<String> flowHandlerFactories;
    private List<String> flashFactories;
    private List<String> clientWindowFactories;
    private List<String> searchExpressionContextFactories;

    public void addApplicationFactory(String factory)
    {
        if (applicationFactories == null)
        {
            applicationFactories = new ArrayList<>();
        }
        applicationFactories.add(factory);
    }

    public void addExceptionHandlerFactory(String factory)
    {
        if (exceptionHandlerFactories == null)
        {
            exceptionHandlerFactories = new ArrayList<>();
        }
        exceptionHandlerFactories.add(factory);
    }

    public void addExternalContextFactory(String factory)
    {
        if (externalContextFactories == null)
        {
            externalContextFactories = new ArrayList<>();
        }
        externalContextFactories.add(factory);
    }

    public void addFacesContextFactory(String factory)
    {
        if (facesContextFactories == null)
        {
            facesContextFactories = new ArrayList<>();
        }
        facesContextFactories.add(factory);
    }

    public void addLifecycleFactory(String factory)
    {
        if (lifecycleFactories == null)
        {
            lifecycleFactories = new ArrayList<>();
        }
        lifecycleFactories.add(factory);
    }

    public void addViewDeclarationLanguageFactory(String factory)
    {
        if (viewDeclarationLanguageFactories == null)
        {
            viewDeclarationLanguageFactories = new ArrayList<>();
        }
        viewDeclarationLanguageFactories.add(factory);
    }

    public void addPartialViewContextFactory(String factory)
    {
        if (partialViewContextFactories == null)
        {
            partialViewContextFactories = new ArrayList<>();
        }
        partialViewContextFactories.add(factory);
    }

    public void addRenderkitFactory(String factory)
    {
        if (renderKitFactories == null)
        {
            renderKitFactories = new ArrayList<>();
        }
        renderKitFactories.add(factory);
    }

    public void addTagHandlerDelegateFactory(String factory)
    {
        if (tagHandlerDelegateFactories == null)
        {
            tagHandlerDelegateFactories = new ArrayList<>();
        }
        tagHandlerDelegateFactories.add(factory);
    }

    public void addVisitContextFactory(String factory)
    {
        if (visitContextFactories == null)
        {
            visitContextFactories = new ArrayList<>();
        }
        visitContextFactories.add(factory);
    }
    
    public void addFaceletCacheFactory(String factory)
    {
        if (faceletCacheFactories == null)
        {
            faceletCacheFactories = new ArrayList<>();
        }
        faceletCacheFactories.add(factory);
    }

    public void addFlashFactory(String factory)
    {
        if (flashFactories == null)
        {
            flashFactories = new ArrayList<>();
        }
        flashFactories.add(factory);
    }
    
    public void addFlowHandlerFactory(String factory)
    {
        if (flowHandlerFactories == null)
        {
            flowHandlerFactories = new ArrayList<>();
        }
        flowHandlerFactories.add(factory);
    }

    public void addClientWindowFactory(String factory)
    {
        if (clientWindowFactories == null)
        {
            clientWindowFactories = new ArrayList<>();
        }
        clientWindowFactories.add(factory);
    }

    public void addSearchExpressionContextFactory(String factory)
    {
        if (searchExpressionContextFactories == null)
        {
            searchExpressionContextFactories = new ArrayList<>();
        }
        searchExpressionContextFactories.add(factory);
    }

    @Override
    public List<String> getApplicationFactory()
    {
        if (applicationFactories == null)
        {
            return Collections.emptyList();
        }
        return applicationFactories;
    }

    @Override
    public List<String> getExceptionHandlerFactory()
    {
        if (exceptionHandlerFactories == null)
        {
            return Collections.emptyList();
        }
        return exceptionHandlerFactories;
    }

    @Override
    public List<String> getExternalContextFactory()
    {
        if (externalContextFactories == null)
        {
            return Collections.emptyList();
        }
        return externalContextFactories;
    }

    @Override
    public List<String> getFacesContextFactory()
    {
        if (facesContextFactories == null)
        {
            return Collections.emptyList();
        }
        return facesContextFactories;
    }

    @Override
    public List<String> getLifecycleFactory()
    {
        if (lifecycleFactories == null)
        {
            return Collections.emptyList();
        }
        return lifecycleFactories;
    }

    @Override
    public List<String> getViewDeclarationLanguageFactory()
    {
        if (viewDeclarationLanguageFactories == null)
        {
            return Collections.emptyList();
        }
        return viewDeclarationLanguageFactories;
    }

    @Override
    public List<String> getPartialViewContextFactory()
    {
        if (partialViewContextFactories == null)
        {
            return Collections.emptyList();
        }
        return partialViewContextFactories;
    }

    @Override
    public List<String> getRenderkitFactory()
    {
        if (renderKitFactories == null)
        {
            return Collections.emptyList();
        }
        return renderKitFactories;
    }

    @Override
    public List<String> getTagHandlerDelegateFactory()
    {
        if (tagHandlerDelegateFactories == null)
        {
            return Collections.emptyList();
        }
        return tagHandlerDelegateFactories;
    }

    @Override
    public List<String> getVisitContextFactory()
    {
        if (visitContextFactories == null)
        {
            return Collections.emptyList();
        }
        return visitContextFactories;
    }

    @Override
    public List<String> getFaceletCacheFactory()
    {
        if (faceletCacheFactories == null)
        {
            return Collections.emptyList();
        }
        return faceletCacheFactories;
    }

    @Override
    public List<String> getFlashFactory()
    {
        if (flashFactories == null)
        {
            return Collections.emptyList();
        }
        return flashFactories;
    }

    @Override
    public List<String> getFlowHandlerFactory()
    {
        if (flowHandlerFactories == null)
        {
            return Collections.emptyList();
        }
        return flowHandlerFactories;
    }

    @Override
    public List<String> getClientWindowFactory()
    {
        if (clientWindowFactories == null)
        {
            return Collections.emptyList();
        }
        return clientWindowFactories;
    }

    @Override
    public List<String> getSearchExpressionContextFactory()
    {
        if (searchExpressionContextFactories == null)
        {
            return Collections.emptyList();
        }
        return searchExpressionContextFactories;
    }

}
