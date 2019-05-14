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
package org.apache.myfaces.cdi;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;


/**
 * Stateless class to deal with Faces Scope. This scope depends on the current FacesContext.
 */
public class FacesScopeBeanHolder
{
    
    public static final String FACES_SCOPE_MAP = "oam.FACES_SCOPE_MAP";
    
    public static final String FACES_SCOPE_MAP_INFO = "oam.FACES_SCOPE_MAP_INFO";
    
    public FacesScopeBeanHolder()
    {
    }
    
    public void init()
    {
    }
    
    /**
     * This method will return the ContextualStorage or create a new one
     * if no one is yet assigned to the current flowClientWindowId.
     * @param beanManager we need the CDI {@link BeanManager} for serialisation.
     * @param facesContext the current FacesContext instance
     */
    public ContextualStorage getContextualStorage(BeanManager beanManager, FacesContext facesContext)
    {
        ContextualStorage contextualStorage = (ContextualStorage) facesContext.getAttributes().get(FACES_SCOPE_MAP);
        if (contextualStorage == null)
        {
            contextualStorage = new ContextualStorage(beanManager, false, false);
            facesContext.getAttributes().put(FACES_SCOPE_MAP, contextualStorage);
        }

        return contextualStorage;
    }
    
    public ContextualStorage getContextualStorageNoCreate(BeanManager beanManager, FacesContext facesContext)
    {
        return (ContextualStorage) facesContext.getAttributes().get(FACES_SCOPE_MAP);
    }

    public Map<Object, Object> getFacesScopeMap(BeanManager beanManager, FacesContext facesContext, boolean create)
    {
        Map<Object, Object> map = null;
        if (create)
        {
            ContextualStorage contextualStorage = getContextualStorage(beanManager, facesContext);
            ContextualInstanceInfo info = contextualStorage.getStorage().get(FACES_SCOPE_MAP_INFO);
            if (info == null)
            {
                info = new ContextualInstanceInfo<Object>();
                contextualStorage.getStorage().put(FACES_SCOPE_MAP_INFO, info);
            }
            map = (Map<Object, Object>) info.getContextualInstance();
            if (map == null)
            {
                map = new HashMap<Object,Object>();
                info.setContextualInstance(map);
            }
        }
        else
        {
            ContextualStorage contextualStorage = getContextualStorageNoCreate(beanManager, facesContext);
            if (contextualStorage != null)
            {
                ContextualInstanceInfo info = contextualStorage.getStorage().get(FACES_SCOPE_MAP_INFO);
                if (info != null)
                {
                    map = (Map<Object, Object>) info.getContextualInstance();
                }
            }
        }
        return map;
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
    public ContextualStorage forceNewStorage(FacesContext facesContext)
    {
        return (ContextualStorage) facesContext.getAttributes().remove(FACES_SCOPE_MAP);
    }

    /**
     * This method properly destroys all current &#064;FacesScoped beans
     * of the active session and also prepares the storage for new beans.
     * It will automatically get called when the session context closes
     * but can also get invoked manually, e.g. if a user likes to get rid
     * of all it's &#064;FacesScoped beans.
     */
    public void destroyBeans(FacesContext facesContext)
    {
        // we replace the old windowBeanHolder beans with a new storage Map
        // an afterwards destroy the old Beans without having to care about any syncs.
        ContextualStorage oldStorages = forceNewStorage(facesContext);
        if (oldStorages != null)
        {
            FacesScopeContextImpl.destroyAllActive(oldStorages);
        }
    }

}
