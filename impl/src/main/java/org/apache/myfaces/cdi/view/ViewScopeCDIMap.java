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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import java.util.stream.Collectors;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;

/**
 *
 * @author Leonardo Uribe
 */
public class ViewScopeCDIMap implements Map<String, Object>
{
    private String viewScopeId;
    private ContextualStorage storage;

    public ViewScopeCDIMap(String viewScopeId)
    {
        this.viewScopeId = viewScopeId;
    }
    
    private ContextualStorage getStorage()
    {
        if (storage != null && !storage.isActivated())
        {
            storage = null;
        }
        if (storage == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            BeanManager beanManager = CDIUtils.getBeanManager(facesContext);

            ViewScopeContextualStorageHolder bean = CDIUtils.get(beanManager, ViewScopeContextualStorageHolder.class);
            
            storage = bean.getContextualStorage(viewScopeId);
        }
        return storage;
    }

    private Map<Object, ContextualInstanceInfo<?>> getCreationalContextInstances()
    {
        return getStorage().getStorage();
    }
    
    public String getViewScopeId()
    {
        return viewScopeId;
    }

    @Override
    public int size()
    {
        return getCreationalContextInstances().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getCreationalContextInstances().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return getCreationalContextInstances().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        if (value != null)
        {
            for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : getCreationalContextInstances().entrySet())
            {
                if (entry.getValue() != null && value.equals(entry.getValue().getContextualInstance()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object get(Object key)
    {
        ContextualInstanceInfo<?> info = getCreationalContextInstances().get(key);
        return info == null ? null : info.getContextualInstance();
    }

    @Override
    public Object put(String key, Object value)
    {
        ContextualInstanceInfo info = new ContextualInstanceInfo();
        info.setContextualInstance(value);
        return getCreationalContextInstances().put(key, info);
    }

    @Override
    public Object remove(Object key)
    {
        ContextualInstanceInfo info = getCreationalContextInstances().remove(key);
        return info == null ? null : info.getContextualInstance();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        // If the scope was already destroyed through an invalidateSession(), the storage instance
        // that is holding this map could be obsolete, so we need to grab the right instance from
        // the bean holder.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ViewScopeContext.destroyAll(facesContext, viewScopeId);
    }

    @Override
    public Set<String> keySet()
    {
        return getCreationalContextInstances().keySet()
                .stream()
                .map(e -> (String) e)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Object> values()
    {
        List<Object> values = new ArrayList<>(getCreationalContextInstances().size());

        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : getCreationalContextInstances().entrySet())
        {
            ContextualInstanceInfo info = entry.getValue();
            if (info != null)
            {
                values.add(info.getContextualInstance());
            }
        }

        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        Set<Entry<String, Object>> values = new HashSet<>();
  
        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : getCreationalContextInstances().entrySet())
        {
            ContextualInstanceInfo info = entry.getValue();
            if (info != null)
            {
                values.add(new EntryWrapper(entry.getKey()));
            }
        }

        return values;
    }
    
    private class EntryWrapper<String, Object> implements Entry<String, Object>
    {
        private final Object key;
        
        public EntryWrapper(Object key)
        {
            this.key = key;
        }

        @Override
        public String getKey()
        {
            return (String) key;
        }

        @Override
        public Object getValue()
        {
            ContextualInstanceInfo<?> info = getCreationalContextInstances().get(key);

            return (Object) (info == null ? null : info.getContextualInstance());
        }

        @Override
        public Object setValue(Object value)
        {
            ContextualInstanceInfo info = getCreationalContextInstances().get(key);

            Object oldValue = null;
            if (info != null)
            {
                info.setContextualInstance(value);
            }
            else
            {
                info = new ContextualInstanceInfo();
                info.setContextualInstance(value);
                getCreationalContextInstances().put(key, info);
            }
            return oldValue;
        }
    }
}
