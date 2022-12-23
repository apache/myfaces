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
package org.apache.myfaces.cdi.view;

import java.lang.annotation.Annotation;
import java.util.Map;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;

/**
 * Minimal implementation of ViewTransientScope.
 */
@Typed()
public class ViewTransientScopeContext implements Context
{
    public static final String VIEW_TRANSIENT_SCOPED_STORAGE = "oam.VIEW_TRANSIENT_SCOPED_STORAGE";

    private BeanManager beanManager;
    
    public ViewTransientScopeContext(BeanManager beanManager)
    {
        this.beanManager = beanManager;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewTransientScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return isActive(FacesContext.getCurrentInstance());
    }

    public boolean isActive(FacesContext facesContext)
    {
        if (facesContext == null)
        {
            return false;
        }
        else if (facesContext.getViewRoot() == null)
        {
            return false;
        }
        return true;
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
            throw new IllegalStateException("FacesContext cannot be found when resolving bean " +bean.toString());
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
        ContextualStorage contextualStorage = (ContextualStorage) 
                facesContext.getViewRoot().getTransientStateHelper().getTransient(VIEW_TRANSIENT_SCOPED_STORAGE);
        if (contextualStorage == null && createIfNotExist)
        {
            contextualStorage = new ContextualStorage(beanManager, false);
            facesContext.getViewRoot().getTransientStateHelper()
                    .putTransient(VIEW_TRANSIENT_SCOPED_STORAGE, contextualStorage);
        }

        return contextualStorage;
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

    public static void destroyAll(FacesContext facesContext)
    {
        if (facesContext == null
                || facesContext.getViewRoot() == null)
        {
            return;
        }

        ContextualStorage storage = (ContextualStorage) facesContext.getViewRoot()
                .getTransientStateHelper().putTransient(VIEW_TRANSIENT_SCOPED_STORAGE, null);
        if (storage != null)
        {
            Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
            for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet())
            {
                Contextual bean = storage.getBean(entry.getKey());

                ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
                bean.destroy(contextualInstanceInfo.getContextualInstance(), 
                    contextualInstanceInfo.getCreationalContext());
            }
        }
    }
}
