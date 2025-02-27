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
package org.apache.myfaces.cdi.clientwindow;

import java.lang.annotation.Annotation;
import java.util.Map;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.lifecycle.ClientWindowScoped;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;

/**
 * Minimal implementation of ClientWindowScope.
 */
@Typed()
public class ClientWindowScopeContext implements Context
{
    private BeanManager beanManager;
    
    public ClientWindowScopeContext(BeanManager beanManager)
    {
        this.beanManager = beanManager;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ClientWindowScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return isActive(FacesContext.getCurrentInstance());
    }

    public boolean isActive(FacesContext facesContext)
    {
        if (facesContext == null || facesContext.getExternalContext().getClientWindow() == null)
        {
            return false;
        }

        return true;
    }

    protected ContextualStorage getContextualStorage(FacesContext facesContext, boolean createIfNotExist)
    {
        String windowId = getCurrentClientWindowId(facesContext);
        if (windowId != null)
        {
            ClientWindowScopeContextualStorageHolder storageHolder = getStorageHolder(facesContext);
            if (storageHolder != null)
            {
                return storageHolder.getContextualStorage(windowId, createIfNotExist);
            }
        }
        return null;
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        checkActive(facesContext);

        if (facesContext != null)
        {
            ContextualStorage storage = getContextualStorage(facesContext, false);
            if (storage != null)
            {
                Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
                ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));

                if (contextualInstanceInfo != null)
                {
                    return (T) contextualInstanceInfo.getContextualInstance();
                }
            }
        }
        else
        {
            throw new IllegalStateException("FacesContext cannot be found when resolving bean " + bean.toString());
        }
        return null;
    }

    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        checkActive(facesContext);

        ContextualStorage storage = getContextualStorage(facesContext, true);

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));

        if (contextualInstanceInfo != null)
        {
            @SuppressWarnings("unchecked")
            final T instance = (T) contextualInstanceInfo.getContextualInstance();

            if (instance != null)
            {
                return instance;
            }
        }

        return storage.createContextualInstance(bean, creationalContext);
    }

    protected void checkActive(FacesContext facesContext)
    {
        if (!isActive(facesContext))
        {
            throw new ContextNotActiveException("CDI context with scope annotation @"
                + getScope().getName() + " is not active with respect to the current thread");
        }
    }

    protected ClientWindowScopeContextualStorageHolder getStorageHolder(FacesContext context)
    {
        return ClientWindowScopeContextualStorageHolder.getInstance(context, true);
    }

    protected String getCurrentClientWindowId(FacesContext context)
    {
        return context.getExternalContext().getClientWindow().getId();
    }

    public boolean destroy(Contextual bean)
    {
        ContextualStorage storage = getContextualStorage(FacesContext.getCurrentInstance(), false);
        if (storage == null)
        {
            return false;
        }
        
        ContextualInstanceInfo<?> contextualInstanceInfo = storage.getStorage().get(storage.getBeanKey(bean));
        if (contextualInstanceInfo == null)
        {
            return false;
        }

        bean.destroy(contextualInstanceInfo.getContextualInstance(), contextualInstanceInfo.getCreationalContext());

        return true;
    }

    public static void destroyAll(FacesContext facesContext)
    {
        ClientWindowScopeContextualStorageHolder manager =
                ClientWindowScopeContextualStorageHolder.getInstance(facesContext);
        if (manager != null)
        {
            manager.destroyAll(facesContext);
        }
    }
    
    public static void destroyAll(FacesContext context, String clientWindowId)
    {
        ClientWindowScopeContextualStorageHolder manager = ClientWindowScopeContextualStorageHolder
                .getInstance(context);
        if (manager != null)
        {
            manager.destroyAll(context, clientWindowId);
        }
    }

    public static void pushClientWindow(FacesContext context, ClientWindow clientWindow)
    {
        ClientWindowScopeContextualStorageHolder manager = ClientWindowScopeContextualStorageHolder
                .getInstance(context, true);
        if (manager != null)
        {
            manager.pushClientWindow(context, clientWindow);
        }
    }
}
