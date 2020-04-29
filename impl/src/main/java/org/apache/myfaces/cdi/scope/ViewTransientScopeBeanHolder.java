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
package org.apache.myfaces.cdi.scope;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;


/**
 * Stateless class to deal with ViewTransientScope. This scope depends on the current FacesContext.
 */
public class ViewTransientScopeBeanHolder
{
    
    public static final String VIEW_TRANSIENT_SCOPE_MAP = "oam.VIEW_TRANSIENT_SCOPE_MAP";
    
    public static final String VIEW_TRANSIENT_SCOPE_MAP_INFO = "oam.VIEW_TRANSIENT_SCOPE_MAP_INFO";
    
    public ViewTransientScopeBeanHolder()
    {
    }
    
    public void init()
    {
    }
    
    public static ContextualStorage getContextualStorage(BeanManager beanManager, FacesContext facesContext)
    {
        ContextualStorage contextualStorage = (ContextualStorage) 
                facesContext.getViewRoot().getTransientStateHelper().getTransient(VIEW_TRANSIENT_SCOPE_MAP);
        if (contextualStorage == null)
        {
            contextualStorage = new ContextualStorage(beanManager, false, false);
            facesContext.getViewRoot().getTransientStateHelper()
                    .putTransient(VIEW_TRANSIENT_SCOPE_MAP, contextualStorage);
        }

        return contextualStorage;
    }
    
    public ContextualStorage getContextualStorageNoCreate(BeanManager beanManager, FacesContext facesContext)
    {
        return (ContextualStorage) facesContext.getViewRoot()
                .getTransientStateHelper().getTransient(VIEW_TRANSIENT_SCOPE_MAP);
    }

    public Map<Object, Object> getFacesScopeMap(
        BeanManager beanManager, FacesContext facesContext, boolean create)
    {
        Map<Object, Object> map = null;
        if (create)
        {
            ContextualStorage contextualStorage = getContextualStorage(
                beanManager, facesContext);
            ContextualInstanceInfo info = contextualStorage.getStorage().get(VIEW_TRANSIENT_SCOPE_MAP_INFO);
            if (info == null)
            {
                info = new ContextualInstanceInfo<Object>();
                contextualStorage.getStorage().put(VIEW_TRANSIENT_SCOPE_MAP_INFO, info);
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
            ContextualStorage contextualStorage = getContextualStorageNoCreate(
                beanManager, facesContext);
            if (contextualStorage != null)
            {
                ContextualInstanceInfo info = contextualStorage.getStorage().get(VIEW_TRANSIENT_SCOPE_MAP_INFO);
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
     * 
     * @param facesContext
     * @return the old storageMap.
     */
    public ContextualStorage forceNewStorage(FacesContext facesContext)
    {
        return (ContextualStorage) facesContext.getViewRoot()
                .getTransientStateHelper().putTransient(VIEW_TRANSIENT_SCOPE_MAP, null);
    }

    public void destroyBeans(FacesContext facesContext)
    {
        ContextualStorage oldWindowContextStorages = forceNewStorage(facesContext);
        if (oldWindowContextStorages != null)
        {
            ViewTransientScopedContextImpl.destroyAllActive(oldWindowContextStorages);
        }
    }

}
