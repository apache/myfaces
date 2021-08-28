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

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;
import java.util.Map;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.view.ViewScopeProxyMap;

/**
 * CDI Context to handle &#064;{@link ViewScoped} beans.
 * 
 * @author Leonardo Uribe
 */
@Typed()
public class ViewScopeContext implements Context
{

    /**
     * needed for serialisation and passivationId
     */
    private BeanManager beanManager;
    
    private boolean passivatingScope;

    public ViewScopeContext(BeanManager beanManager)
    {
        this.beanManager = beanManager;
        this.passivatingScope = beanManager.isPassivatingScope(getScope());
    }

    protected ViewScopeContextualStorageHolder getStorageHolder(FacesContext facesContext)
    {
        return ViewScopeContextualStorageHolder.getInstance(facesContext, true);
    }

    public String getCurrentViewScopeId(boolean create)
    {        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ViewScopeProxyMap map = (ViewScopeProxyMap) facesContext.getViewRoot().getViewMap(create);
        if (map != null)
        {
            String id = map.getViewScopeId();
            if (id == null && create)
            {
                // Force create
                map.forceDelegateCreation(facesContext);
                id = map.getViewScopeId();
            }
            return id;
        }
        return null;
    }

    protected ViewScopeContextualStorage getContextualStorage(FacesContext facesContext, boolean createIfNotExist)
    {
        String viewScopeId = getCurrentViewScopeId(createIfNotExist);
        if (createIfNotExist && viewScopeId == null)
        {
            throw new ContextNotActiveException(
                this.getClass().getSimpleName() + ": no viewScopeId set for the current view yet!");
        }
        if (viewScopeId != null)
        {
            return getStorageHolder(facesContext).getContextualStorage(viewScopeId, createIfNotExist);
        }
        return null;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return isActive(FacesContext.getCurrentInstance());
    }

    public boolean isActive(FacesContext facesContext)
    {
        if (facesContext == null || facesContext.getViewRoot() == null)
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

        // force session creation if ViewScoped is used
        facesContext.getExternalContext().getSession(true);
        
        ViewScopeContextualStorage storage = getContextualStorage(facesContext, false);
        if (storage == null)
        {
            return null;
        }

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));
        if (contextualInstanceInfo == null)
        {
            return null;
        }

        return (T) contextualInstanceInfo.getContextualInstance();
    }

    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        checkActive(facesContext);

        if (passivatingScope && !(bean instanceof PassivationCapable))
        {
            throw new IllegalStateException(bean.toString() +
                    " doesn't implement " + PassivationCapable.class.getName());
        }

        // force session creation if ViewScoped is used
        facesContext.getExternalContext().getSession(true);
        
        ViewScopeContextualStorage storage = getContextualStorage(facesContext, true);

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

    public static void destroyAll(FacesContext facesContext)
    {
        ViewScopeContextualStorageHolder manager = ViewScopeContextualStorageHolder.getInstance(facesContext);
        if (manager != null)
        {
            manager.destroyAll(facesContext);
        }
    }

    public static void destroyAll(FacesContext facesContext, String viewScopeId)
    {
        ViewScopeContextualStorageHolder manager = ViewScopeContextualStorageHolder.getInstance(facesContext);
        if (manager != null)
        {
            manager.destroyAll(facesContext, viewScopeId);
        }
    }
}