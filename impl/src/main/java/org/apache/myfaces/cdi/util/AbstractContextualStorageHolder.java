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
package org.apache.myfaces.cdi.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.myfaces.cdi.JsfApplicationArtifactHolder;
import org.apache.myfaces.context.ExceptionHandlerImpl;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;

public abstract class AbstractContextualStorageHolder<T extends ContextualStorage> implements Serializable
{
    @Inject
    protected JsfApplicationArtifactHolder applicationContextBean;
    
    @Inject
    protected BeanManager beanManager;
    
    protected Map<String, T> storageMap;

    public AbstractContextualStorageHolder()
    {
    }

    @PostConstruct
    public void init()
    {
        storageMap = new ConcurrentHashMap<>();
        
        FacesContext facesContext = FacesContext.getCurrentInstance();

        Object context = facesContext.getExternalContext().getContext();
        if (context instanceof ServletContext)
        {
            JsfApplicationArtifactHolder appBean = CDIUtils.get(beanManager, JsfApplicationArtifactHolder.class);
            if (appBean.getServletContext() != null)
            {
                appBean.setServletContext((ServletContext) context);
            }
        }
    }

    /**
     *
     * This method will replace the storageMap and with a new empty one.
     * This method can be used to properly destroy the BeanHolder beans without having to sync heavily.
     * Any {@link jakarta.enterprise.inject.spi.Bean#destroy(Object, jakarta.enterprise.context.spi.CreationalContext)}
     * should be performed on the returned old storage map.
     *
     * @return the old storageMap.
     */
    public Map<String, T> forceNewStorage()
    {
        Map<String, T> oldStorageMap = storageMap;
        storageMap = new ConcurrentHashMap<>();
        return oldStorageMap;
    }

    public Map<String, T> getStorageMap()
    {
        return storageMap;
    }
    
    public T getContextualStorage(String slotId)
    {
        return getContextualStorage(slotId, true);
    }
    
    public T getContextualStorage(String slotId, boolean create)
    {
        if (storageMap == null)
        {
            if (!create)
            {
                return null;
            }
            
            storageMap = new ConcurrentHashMap<>();
        }

        T storage = storageMap.get(slotId);
        if (storage == null && create)
        {
            storage = newContextualStorage(slotId);
            storageMap.put(slotId, storage);
        }
        return storage;
    }

    protected abstract T newContextualStorage(String slotId);
    
    @PreDestroy
    public void preDestroy()
    {
        // After some testing done two things are clear:
        // 1. jetty +  weld call @PreDestroy at the end of the request
        // 2. use a HttpServletListener in tomcat + owb does not work, because
        //    CDI listener is executed first.
        // So we need a mixed approach using both a listener and @PreDestroy annotations.
        // When the first one in being called replace the storages with a new map 
        // and call PreDestroy, when the second one is called, it founds an empty map
        // and the process stops. A hack to get ServletContext from CDI is required to
        // provide a valid FacesContext instance.
        Map<String, T> oldContextStorages = forceNewStorage();
        if (!oldContextStorages.isEmpty())
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null && applicationContextBean.getServletContext() != null)
            {
                try
                {
                    ServletContext servletContext = applicationContextBean.getServletContext();
                    ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, false);
                    ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
                    facesContext = new StartupFacesContextImpl(externalContext, 
                            externalContext, exceptionHandler, false);
                    for (T contextualStorage : oldContextStorages.values())
                    {
                        destroyAll(contextualStorage, facesContext);
                    }
                }
                finally
                {
                    facesContext.release();
                }
            }
            else
            {
                for (T contextualStorage : oldContextStorages.values())
                {
                    destroyAll(contextualStorage, facesContext);
                }
            }
        }
    }

    public void destroyAll(FacesContext facesContext)
    {
        if (storageMap == null || storageMap.isEmpty())
        {
            return;
        }

        // we replace the old BeanHolder beans with a new storage Map
        // an afterwards destroy the old Beans without having to care about any syncs.
        // This behavior also helps as a check to avoid destroy the same beans twice.
        Map<String, T> oldContextStorages = forceNewStorage();

        for (T contextualStorage : oldContextStorages.values())
        {
            destroyAll(contextualStorage, facesContext);
        }
    }

    public void destroyAll(T contextualStorage, FacesContext facesContext)
    {
        if (facesContext == null)
        {
            facesContext = FacesContext.getCurrentInstance();
        }
        
        boolean tempFacesContext = false;
        if (facesContext == null && applicationContextBean.getServletContext() != null)
        {
            ServletContext servletContext = applicationContextBean.getServletContext();
            ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, false);
            ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
            facesContext = new StartupFacesContextImpl(externalContext, externalContext, exceptionHandler, false);
            tempFacesContext = true;
        }

        try
        {
            Map<Object, ContextualInstanceInfo<?>> contextMap = contextualStorage.getStorage();

            for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet())
            {  
                Contextual bean = contextualStorage.getBean(entry.getKey());

                ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
                bean.destroy(contextualInstanceInfo.getContextualInstance(), 
                    contextualInstanceInfo.getCreationalContext());
            }

            contextMap.clear();

            contextualStorage.deactivate();
        }
        finally
        {
            if (tempFacesContext)
            {
                facesContext.release();
            }
        }
    }

    public void destroyAll(FacesContext context, String slotId)
    {
        if (storageMap == null || storageMap.isEmpty())
        {
            return;
        }

        T contextualStorage = storageMap.remove(slotId);
        destroyAll(contextualStorage, context);
    }

    public static <T extends AbstractContextualStorageHolder> T getInstance(FacesContext facesContext,
            Class<T> contextManagerClass)
    {
        if (facesContext == null
                || facesContext.getExternalContext() == null
                || facesContext.getExternalContext().getSession(false) == null)
        {
            return null;
        }

        BeanManager beanManager = CDIUtils.getBeanManager(facesContext.getExternalContext());
        if (beanManager == null)
        {
            return null;
        }

        if (!CDIUtils.isSessionScopeActive(beanManager))
        {
            return null;
        }

        T cached = (T) facesContext.getExternalContext().getSessionMap().get(contextManagerClass.getClass().getName());
        if (cached == null)
        {
            cached = CDIUtils.getOptional(beanManager, contextManagerClass);
            if (cached != null)
            {
                facesContext.getExternalContext().getSessionMap().put(contextManagerClass.getClass().getName(),
                        cached);
            }
        }

        return cached;
    }
}
