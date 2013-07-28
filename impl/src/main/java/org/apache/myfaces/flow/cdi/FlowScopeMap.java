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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;

/**
 * 
 *
 * @author Leonardo Uribe
 */
public class FlowScopeMap implements Map
{
    
    public FlowScopeMap()
    {
    }

    public int size()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        int size = 0;
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap.size() >= 0)
            {
                size += flowScopeMap.size();
            }
        }
        return size;
    }
    
    private Map getCurrentMap()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        String flowMapKey = flow.getClientWindowFlowId(
            facesContext.getExternalContext().getClientWindow());
        return FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public boolean containsKey(Object key)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(Object value)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap.containsValue(value))
            {
                return true;
            }
        }
        return false;
    }

    public Object get(Object key)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            
            if (flowScopeMap.containsKey(key))
            {
                return flowScopeMap.get(key);
            }
        }
        return null;
    }

    public Object put(Object key, Object value)
    {
        return getCurrentMap().put(key, value);
    }

    public Object remove(Object key)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            Object removedValue = flowScopeMap.remove(key);
            if (removedValue != null)
            {
                return removedValue;
            }
        }
        return null;
    }

    public void putAll(Map m)
    {
        getCurrentMap().putAll(m);
    }

    public void clear()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = FlowScopedContextImpl.getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = FlowScopedContextImpl.getFlowScopedMap(facesContext, flowMapKey);
            flowScopeMap.clear();
        }
    }

    public Set keySet()
    {
        return getCurrentMap().keySet();
    }

    public Collection values()
    {
        return getCurrentMap().values();
    }

    public Set entrySet()
    {
        return getCurrentMap().entrySet();
    }
}
