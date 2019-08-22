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

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.view.ViewScopeProxyMap;

/**
 * CDI Context to handle &#064;{@link ViewScoped} beans.
 * 
 * @author Leonardo Uribe
 */
@Typed()
public class ViewScopeContextImpl implements Context
{

    /**
     * needed for serialisation and passivationId
     */
    private BeanManager beanManager;
    
    private boolean passivatingScope;

    public ViewScopeContextImpl(BeanManager beanManager)
    {
        this.beanManager = beanManager;
        this.passivatingScope = beanManager.isPassivatingScope(getScope());
    }

    protected ViewScopeBeanHolder getViewScopeBeanHolder()
    {
        return getViewScopeBeanHolder(FacesContext.getCurrentInstance());
    }
    
    protected ViewScopeBeanHolder getViewScopeBeanHolder(FacesContext facesContext)
    {
        return (ViewScopeBeanHolder)facesContext.getExternalContext().getApplicationMap().computeIfAbsent(
                "oam.view.ViewScopeBeanHolder",
                k -> CDIUtils.get(beanManager, ViewScopeBeanHolder.class));
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
                map.forceCreateWrappedMap(facesContext);
                id = map.getViewScopeId();
            }
            return id;
        }
        return null;
    }

    protected ViewScopeContextualStorage getContextualStorage(boolean createIfNotExist)
    {
        String viewScopeId = getCurrentViewScopeId(createIfNotExist);
        if (createIfNotExist && viewScopeId == null)
        {
            throw new ContextNotActiveException(
                "ViewScopeContextImpl: no viewScopeId set for the current view yet!");
        }
        if (viewScopeId != null)
        {
            return getViewScopeBeanHolder().getContextualStorage(beanManager, viewScopeId);
        }
        return null;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewScoped.class;
    }

    /**
     * The WindowContext is active once a current windowId is set for the current Thread.
     * @return
     */
    @Override
    public boolean isActive()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null)
        {
            return facesContext.getViewRoot() != null;
        }
        else
        {
            // No FacesContext means no view scope active.
            return false;
        }
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        checkActive();

        // force session creation if ViewScoped is used
        FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        
        ViewScopeContextualStorage storage = getContextualStorage(false);
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
        checkActive();

        if (passivatingScope && !(bean instanceof PassivationCapable))
        {
            throw new IllegalStateException(bean.toString() +
                    " doesn't implement " + PassivationCapable.class.getName());
        }

        // force session creation if ViewScoped is used
        FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        
        ViewScopeContextualStorage storage = getContextualStorage(true);

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));

        if (contextualInstanceInfo != null)
        {
            @SuppressWarnings("unchecked")
            final T instance =  (T) contextualInstanceInfo.getContextualInstance();

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
        ViewScopeContextualStorage storage = getContextualStorage(false);
        if (storage == null)
        {
            return false;
        }
        ContextualInstanceInfo<?> contextualInstanceInfo = 
            storage.getStorage().get(storage.getBeanKey(bean));

        if (contextualInstanceInfo == null)
        {
            return false;
        }

        bean.destroy(contextualInstanceInfo.getContextualInstance(), 
            contextualInstanceInfo.getCreationalContext());

        return true;
    }

    /**
     * destroys all the Contextual Instances in the Storage returned by
     * {@link #getContextualStorage(boolean)}.
     */
    public void destroyAllActive()
    {
        ViewScopeContextualStorage storage = getContextualStorage(false);
        if (storage == null)
        {
            return;
        }

        destroyAllActive(storage);
    }

    public static void destroyAllActive(ViewScopeContextualStorage storage)
    {
        destroyAllActive(storage, FacesContext.getCurrentInstance());
    }

    public static void destroyAllActive(ViewScopeContextualStorage storage, FacesContext facesContext)
    {
        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();

        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet())
        {
            if (!(entry.getKey() instanceof ViewScopeContextualKey))
            {            
                Contextual bean = storage.getBean(facesContext, entry.getKey());

                ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
                bean.destroy(contextualInstanceInfo.getContextualInstance(), 
                    contextualInstanceInfo.getCreationalContext());
            }
        }

        contextMap.clear();
        
        storage.deactivate();
    }
    
    /**
     * Make sure that the Context is really active.
     * @throws ContextNotActiveException if there is no active
     *         Context for the current Thread.
     */
    protected void checkActive()
    {
        if (!isActive())
        {
            throw new ContextNotActiveException("CDI context with scope annotation @"
                + getScope().getName() + " is not active with respect to the current thread");
        }
    }

}