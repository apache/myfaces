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

import java.io.Serializable;
import java.util.Random;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.lang.annotation.Annotation;
import org.apache.myfaces.cdi.util.AbstractContextualStorageHolder;
import org.apache.myfaces.cdi.util.ContextualStorage;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.LRULinkedHashMap;

/**
 *
 * @author Leonardo Uribe
 */
@Typed(ViewScopeContextualStorageHolder.class)
@SessionScoped
public class ViewScopeContextualStorageHolder
        extends AbstractContextualStorageHolder<ContextualStorage>
        implements Serializable
{    
    private static final Random RANDOM_GENERATOR = new Random();

    /**
     * Tracks the active viewScopeIds in LRU order, so the least recently used view scope can be destroyed
     * if the max number of view scopes per session is exceeded.
     * Without this, view scopes would only be destroyed on session expiration when client side state saving
     * is used, as no view state (and so no viewScopeId) is tracked in the session.
     */
    private LRULinkedHashMap<String, String> viewScopeIdExpirationStack;

    public ViewScopeContextualStorageHolder()
    {
    }

    @PostConstruct
    @Override
    public void init()
    {
        super.init();

        getViewScopeIdExpirationStack(FacesContext.getCurrentInstance());
    }

    protected LRULinkedHashMap<String, String> getViewScopeIdExpirationStack(FacesContext facesContext)
    {
        if (viewScopeIdExpirationStack == null)
        {
            synchronized (this)
            {
                if (viewScopeIdExpirationStack == null)
                {
                    int numberOfViewsInSession =
                            MyfacesConfig.getCurrentInstance(facesContext).getNumberOfViewsInSession();
                    viewScopeIdExpirationStack = new LRULinkedHashMap<>(numberOfViewsInSession,
                            (eldest) -> destroyExpired(eldest.getKey()));
                }
            }
        }
        return viewScopeIdExpirationStack;
    }

    private void destroyExpired(String viewScopeId)
    {
        // the expiration stack itself is cleaned up by the LRULinkedHashMap
        if (storageMap != null)
        {
            ContextualStorage contextualStorage = storageMap.remove(viewScopeId);
            destroyAll(contextualStorage, FacesContext.getCurrentInstance());
        }
    }

    @Override
    public ContextualStorage getContextualStorage(String slotId, boolean create)
    {
        ContextualStorage storage = super.getContextualStorage(slotId, create);
        if (storage != null)
        {
            LRULinkedHashMap<String, String> stack =
                    getViewScopeIdExpirationStack(FacesContext.getCurrentInstance());
            synchronized (stack)
            {
                // access ordered, so this moves an existing viewScopeId to the end
                stack.put(slotId, "");
            }
        }
        return storage;
    }

    @Override
    public void destroyAll(FacesContext facesContext)
    {
        super.destroyAll(facesContext);

        if (viewScopeIdExpirationStack != null)
        {
            synchronized (viewScopeIdExpirationStack)
            {
                viewScopeIdExpirationStack.clear();
            }
        }
    }

    @Override
    public void destroyAll(FacesContext facesContext, String slotId)
    {
        super.destroyAll(facesContext, slotId);

        if (viewScopeIdExpirationStack != null)
        {
            synchronized (viewScopeIdExpirationStack)
            {
                viewScopeIdExpirationStack.remove(slotId);
            }
        }
    }

    public String generateUniqueViewScopeId()
    {
        // To ensure uniqueness we just use a random generator and we check
        // if the key is already used.
        String key;
        do 
        {
            key = Integer.toString(RANDOM_GENERATOR.nextInt());
        } while (storageMap.containsKey(key));
        return key;
    }

    @Override
    protected ContextualStorage newContextualStorage(String slotId)
    {
        return new ContextualStorage(beanManager, false, isPassivating());
    }

    protected static ViewScopeContextualStorageHolder getInstance(FacesContext facesContext)
    {
        return getInstance(facesContext, false);
    }
    
    protected static ViewScopeContextualStorageHolder getInstance(FacesContext facesContext, boolean create)
    {
        return getInstance(facesContext, ViewScopeContextualStorageHolder.class, create);
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewScoped.class;
    }
}
