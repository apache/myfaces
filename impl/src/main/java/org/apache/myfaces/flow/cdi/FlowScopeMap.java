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

    private Map<Object, Object> getFlowScopeMap(boolean create)
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
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? 0 : map.size();
    }
    
    @Override
    public boolean isEmpty()
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? true : map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? false : map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? false : map.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? null : map.get(key);
    }

    @Override
    public Object put(Object key, Object value)
    {
        Map<Object, Object> map = getFlowScopeMap(true);
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? null : map.remove(key);
    }

    @Override
    public void putAll(Map m)
    {
        Map<Object, Object> map = getFlowScopeMap(true);
        map.putAll(m);
    }

    @Override
    public void clear()
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        if (map == null)
        {
            return;
        }
        map.clear();
    }

    @Override
    public Set keySet()
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? Collections.emptySet() : map.keySet();
    }

    @Override
    public Collection values()
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? Collections.emptyList() : map.values();
    }

    @Override
    public Set entrySet()
    {
        Map<Object, Object> map = getFlowScopeMap(false);
        return map == null ? Collections.emptySet() : map.entrySet();
    }
}
