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
import jakarta.faces.lifecycle.ClientWindowScoped;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;

/**
 * Minimal implementation of ClientWindowScope.
 */
@Typed()
public class ClientWindowScopeContext implements Context
{
    public static final String CLIENT_WINDOW_SCOPE_MAP = "oam.CLIENT_WINDOW_SCOPE_MAP";

    private BeanManager beanManager;
    
    public ClientWindowScopeContext(BeanManager beanManager)
    {
        this.beanManager = beanManager;
    }

    /**
     * An implementation has to return the underlying storage which
     * contains the items held in the Context.
     *
     * @param createIfNotExist whether a ContextualStorage shall get created if it doesn't yet exist.
     * @param facesContext 
     * 
     * @return the underlying storage
     */
    protected ContextualStorage getContextualStorage(boolean createIfNotExist, FacesContext facesContext)
    {
        if (facesContext == null)
        {
            throw new ContextNotActiveException(this.getClass().getName() + ": no current active FacesContext");
        }

        Map<String, Object> sessionMap =
                facesContext.getExternalContext().getSessionMap();
        Map<String, ContextualStorage> contextualStorageMap =
                (Map<String, ContextualStorage>) sessionMap.get(CLIENT_WINDOW_SCOPE_MAP);
        if (contextualStorageMap == null)
        {
            if (!createIfNotExist)
            {
                return null;
            }

            contextualStorageMap = new ConcurrentHashMap<>();
            sessionMap.put(CLIENT_WINDOW_SCOPE_MAP, contextualStorageMap);
        }

        String clientWindowId = getCurrentClientWindowId();
        ContextualStorage contextualStorage = contextualStorageMap.get(clientWindowId);
        if (contextualStorage == null)
        {
            if (!createIfNotExist)
            {
                return null;
            }

            contextualStorage = new ContextualStorage(beanManager, false);
            contextualStorageMap.put(clientWindowId, contextualStorage);
        }

        return contextualStorage;
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
    
    protected String getCurrentClientWindowId()
    {
        return FacesContext.getCurrentInstance().getExternalContext().getClientWindow().getId();
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        checkActive(facesContext);

        if (facesContext != null)
        {
            ContextualStorage storage = getContextualStorage(false, facesContext);
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

        ContextualStorage storage = getContextualStorage(true, facesContext);

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

    /**
     * Destroy the Contextual Instance of the given Bean.
     * @param bean dictates which bean shall get cleaned up
     * @return <code>true</code> if the bean was destroyed, <code>false</code> if there was no such bean.
     */
    public boolean destroy(Contextual bean)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ContextualStorage storage = getContextualStorage(false, facesContext);
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

    /**
     * Make sure that the context is really active.
     *
     * @param facesContext the current {@link FacesContext}.
     * @throws ContextNotActiveException if there is no active context for the current thread.
     */
    protected void checkActive(FacesContext facesContext)
    {
        if (!isActive(facesContext))
        {
            throw new ContextNotActiveException("CDI context with scope annotation @"
                + getScope().getName() + " is not active with respect to the current thread");
        }
    }

    public static void onSessionDestroyed(FacesContext facesContext)
    {
        destroyAllActive(facesContext);
    }
    
    public static void destroyAllActive(FacesContext facesContext)
    {
        if (facesContext == null)
        {
            return;
        }
        
        Map<String, Object> sessionMap =
                facesContext.getExternalContext().getSessionMap();
        Map<String, ContextualStorage> contextualStorageMap =
                (Map<String, ContextualStorage>) sessionMap.get(CLIENT_WINDOW_SCOPE_MAP);
        if (contextualStorageMap == null || contextualStorageMap.isEmpty())
        {
            return;
        }

        Iterator<String> iterator = contextualStorageMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String clientWindowId = iterator.next();
            iterator.remove();

            ContextualStorage contextualStorage = contextualStorageMap.get(clientWindowId);
            destroyAllActive(facesContext, contextualStorage);
        }
    }
    
    public static void destroyAllActive(FacesContext facesContext, String clientWindowId)
    {
        if (facesContext == null)
        {
            return;
        }
        
        Map<String, Object> sessionMap =
                facesContext.getExternalContext().getSessionMap();
        Map<String, ContextualStorage> contextualStorageMap =
                (Map<String, ContextualStorage>) sessionMap.get(CLIENT_WINDOW_SCOPE_MAP);
        if (contextualStorageMap == null || contextualStorageMap.isEmpty())
        {
            return;
        }
        
        ContextualStorage contextualStorage = contextualStorageMap.remove(clientWindowId);
        destroyAllActive(facesContext, contextualStorage);
    }
    
    protected static void destroyAllActive(FacesContext facesContext, ContextualStorage contextualStorage)
    {
        if (contextualStorage == null)
        {
            return;
        }
        
        Map<Object, ContextualInstanceInfo<?>> contextMap = contextualStorage.getStorage();
        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet())
        {
            Contextual bean = contextualStorage.getBean(entry.getKey());

            ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
            bean.destroy(contextualInstanceInfo.getContextualInstance(), 
                contextualInstanceInfo.getCreationalContext());
        }
    }
}
