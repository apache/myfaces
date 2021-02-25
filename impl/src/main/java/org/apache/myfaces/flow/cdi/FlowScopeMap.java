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
package org.apache.myfaces.flow.cdi;

import org.apache.myfaces.cdi.util.CDIUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.Collections;

/**
 * 
 *
 * @author Leonardo Uribe
 */
public class FlowScopeMap implements Map
{
    private BeanManager beanManager;
    private String currentClientWindowFlowId;
    private FlowScopeBeanHolder beanHolder;
    private boolean beanHolderInitialized = false;
    
    public FlowScopeMap(BeanManager beanManager, String currentClientWindowFlowId)
    {
        this.beanManager = beanManager;
        this.currentClientWindowFlowId = currentClientWindowFlowId;
    }

    private Map<Object, Object> getWrapped(boolean create)
    {
        if (beanHolder == null)
        {
            if (create)
            {
                beanHolder = CDIUtils.get(beanManager, FlowScopeBeanHolder.class);
            }
            else if (!beanHolderInitialized)
            {
                beanHolder = CDIUtils.get(beanManager, FlowScopeBeanHolder.class, false);
                beanHolderInitialized = true;
            }
        }

        if (beanHolder == null)
        {
            return null;
        }
        return beanHolder.getFlowScopeMap(beanManager, currentClientWindowFlowId, create);
    }
    
    @Override
    public int size()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? 0 : wrapped.size();
    }
    
    @Override
    public boolean isEmpty()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? true : wrapped.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? false : wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? false : wrapped.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? null : wrapped.get(key);
    }

    @Override
    public Object put(Object key, Object value)
    {
        Map<Object, Object> wrapped = getWrapped(true);
        return wrapped.put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? null : wrapped.remove(key);
    }

    @Override
    public void putAll(Map m)
    {
        Map<Object, Object> wrapped = getWrapped(true);
        wrapped.putAll(m);
    }

    @Override
    public void clear()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        if (wrapped == null)
        {
            return;
        }
        wrapped.clear();
    }

    @Override
    public Set keySet()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? Collections.emptySet() : wrapped.keySet();
    }

    @Override
    public Collection values()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? Collections.emptyList() : wrapped.values();
    }

    @Override
    public Set entrySet()
    {
        Map<Object, Object> wrapped = getWrapped(false);
        return wrapped == null ? Collections.emptySet() : wrapped.entrySet();
    }
}
