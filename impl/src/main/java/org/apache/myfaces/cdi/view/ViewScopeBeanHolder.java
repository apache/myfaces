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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;

/**
 *
 * @author Leonardo Uribe
 */
@SessionScoped
public class ViewScopeBeanHolder implements Serializable
{
    /**
     * key: the windowId for the browser tab or window
     * value: the {@link ViewScopeContextualStorage} which holds all the
     * {@link javax.enterprise.inject.spi.Bean}s.
     */
    private Map<String, ViewScopeContextualStorage> storageMap = 
        new ConcurrentHashMap<String, ViewScopeContextualStorage>();
    
    private static final Random RANDOM_GENERATOR = new Random();
    
    private static final String VIEW_SCOPE_PREFIX = "oam.view.SCOPE";
    
    public static final String VIEW_SCOPE_PREFIX_KEY = VIEW_SCOPE_PREFIX+".KEY";
    
    public ViewScopeBeanHolder()
    {
    }
    
    @PostConstruct
    public void init()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getSessionMap().put(VIEW_SCOPE_PREFIX_KEY,
            1);
    }
    
    /**
     * This method will return the ViewScopeContextualStorage or create a new one
     * if no one is yet assigned to the current windowId.
     * @param beanManager we need the CDI {@link BeanManager} for serialisation.
     * @param windowId the windowId for the current browser tab or window.
     */
    public ViewScopeContextualStorage getContextualStorage(
        BeanManager beanManager, String viewScopeId)
    {
        ViewScopeContextualStorage contextualStorage = storageMap.get(viewScopeId);
        if (contextualStorage == null)
        {
            synchronized (this)
            {            
                contextualStorage = storageMap.get(viewScopeId);
                if (contextualStorage == null)
                {            
                    contextualStorage = new ViewScopeContextualStorage(beanManager);
                    storageMap.put(viewScopeId, contextualStorage);
                }
            }
        }
        return contextualStorage;
    }

    public Map<String, ViewScopeContextualStorage> getStorageMap()
    {
        return storageMap;
    }

    /**
     *
     * This method will replace the storageMap and with
     * a new empty one.
     * This method can be used to properly destroy the WindowBeanHolder beans
     * without having to sync heavily. Any
     * {@link javax.enterprise.inject.spi.Bean#destroy(Object, javax.enterprise.context.spi.CreationalContext)}
     * should be performed on the returned old storage map.
     * @return the old storageMap.
     */
    public Map<String, ViewScopeContextualStorage> forceNewStorage()
    {
        Map<String, ViewScopeContextualStorage> oldStorageMap = storageMap;
        storageMap = new ConcurrentHashMap<String, ViewScopeContextualStorage>();
        return oldStorageMap;
    }

    /**
     * This method properly destroys all current &#064;WindowScoped beans
     * of the active session and also prepares the storage for new beans.
     * It will automatically get called when the session context closes
     * but can also get invoked manually, e.g. if a user likes to get rid
     * of all it's &#064;ViewScoped beans.
     */
    //@PreDestroy
    public void destroyBeans()
    {
        // we replace the old windowBeanHolder beans with a new storage Map
        // an afterwards destroy the old Beans without having to care about any syncs.
        Map<String, ViewScopeContextualStorage> oldWindowContextStorages = forceNewStorage();

        for (ViewScopeContextualStorage contextualStorage : oldWindowContextStorages.values())
        {
            ViewScopeContextImpl.destroyAllActive(contextualStorage);
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

}
