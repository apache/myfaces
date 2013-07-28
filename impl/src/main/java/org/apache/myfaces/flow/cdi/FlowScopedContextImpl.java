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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;
import javax.faces.flow.FlowScoped;
import javax.faces.lifecycle.ClientWindow;

/**
 * Minimal implementation of FlowScope.
 * 
 * @TODO: We need something better for this part. The problem is the beans
 * are just put under the session map using the old know SubKeyMap hack used
 * in Flash object, but CDI implementations like OWB solves the passivation
 * problem better. The ideal is provide a myfaces specific SPI interface, to
 * allow provide custom implementation of this detail.
 * 
 * @TODO: FlowHandler.transition() method should call this object when the
 * user enter or exit a flow.
 *
 * @author Leonardo Uribe
 */
public class FlowScopedContextImpl implements Context
{
    private static final String FLOW_PREFIX = "oam.FacesFlow";
    
    static final String FLOW_SCOPE_MAP = FLOW_PREFIX + ".MAP";
    
    static final String FLOW_SESSION_MAP_SUBKEY_PREFIX = FLOW_PREFIX + ".SCOPE";
    
    static final String FLOW_ACTIVE_FLOWS = FLOW_PREFIX + ".ACTIVE_FLOWS";
    
    /**
     * Token separator.
     */
    static final char SEPARATOR_CHAR = '.';

    public Class<? extends Annotation> getScope()
    {
        return FlowScoped.class;
    }

    public <T> T get(Contextual<T> component, CreationalContext<T> creationalContext)
    {
        Bean<T> bean = (Bean<T>) component;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap != null)
            {
                if(flowScopeMap.containsKey(bean.getName())) 
                {
                    return (T) flowScopeMap.get(bean.getName());
                }
            }
        }
        
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        if (flow != null)
        {
            String flowMapKey = flow.getClientWindowFlowId(
                facesContext.getExternalContext().getClientWindow());
            
            Map flowScopeMap = getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap != null)
            {
                T t = bean.create(creationalContext);
                flowScopeMap.put(bean.getName(), t);
                return t;
            }
        }
        return null;
    }
    
    static Map getFlowScopedMap(FacesContext facesContext, String flowMapKey)
    {
        String baseKey = FLOW_SCOPE_MAP + SEPARATOR_CHAR + flowMapKey;
        Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
        Map<String, Object> map = (Map<String, Object>) requestMap.get(baseKey);
        if (map == null)
        {
            String fullToken = FLOW_SESSION_MAP_SUBKEY_PREFIX + SEPARATOR_CHAR + flowMapKey;
            map =  _createSubKeyMap(facesContext, fullToken);
            requestMap.put(baseKey, map);
        }
        return map;
    }
    
    public static List<String> getActiveFlowMapKeys(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = FLOW_ACTIVE_FLOWS + SEPARATOR_CHAR + cw.getId();
        List<String> activeFlowKeys = (List<String>) facesContext.
            getExternalContext().getSessionMap().get(baseKey);
        if (activeFlowKeys == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return activeFlowKeys;
        }
    }
    
    public static void createCurrentFlowScope(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = FLOW_ACTIVE_FLOWS + SEPARATOR_CHAR + cw.getId();
        
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        String flowMapKey = flow.getClientWindowFlowId(
            facesContext.getExternalContext().getClientWindow());

        List<String> activeFlowKeys = (List<String>) facesContext.
            getExternalContext().getSessionMap().get(baseKey);
        if (activeFlowKeys == null)
        {
            activeFlowKeys = new ArrayList<String>();
            
        }
        activeFlowKeys.add(flowMapKey);
        facesContext.getExternalContext().getSessionMap().put(baseKey, activeFlowKeys);
    }
    
    public static void destroyCurrentFlowScope(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = FLOW_ACTIVE_FLOWS + SEPARATOR_CHAR + cw.getId();
        
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        String flowMapKey = flow.getClientWindowFlowId(
            facesContext.getExternalContext().getClientWindow());

        Map flowScopeMap = getFlowScopedMap(facesContext, flowMapKey);
        flowScopeMap.clear();
        
        List<String> activeFlowKeys = (List<String>) facesContext.
            getExternalContext().getSessionMap().get(baseKey);
        if (activeFlowKeys != null && !activeFlowKeys.isEmpty())
        {
            activeFlowKeys.remove(flowMapKey);
        }
    }
    
    /**
     * Create a new subkey-wrapper of the session map with the given prefix.
     * This wrapper is used to implement the maps for the flash scope.
     * For more information see the SubKeyMap doc.
     */
    private static Map<String, Object> _createSubKeyMap(FacesContext context, String prefix)
    {
        ExternalContext external = context.getExternalContext();
        Map<String, Object> sessionMap = external.getSessionMap();

        return new SubKeyMap<Object>(sessionMap, prefix);
    }

    public <T> T get(Contextual<T> component)
    {
        Bean bean = (Bean) component;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> activeFlowMapKeys = getActiveFlowMapKeys(facesContext);
        for (String flowMapKey : activeFlowMapKeys)
        {
            Map flowScopeMap = getFlowScopedMap(facesContext, flowMapKey);
            if (flowScopeMap != null)
            {
                if(flowScopeMap.containsKey(bean.getName()))
                {
                    return (T) flowScopeMap.get(bean.getName());
                }
            }
        }
        return null;
    }

    public boolean isActive()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            return false;
        }
        Flow flow = facesContext.getApplication().
            getFlowHandler().getCurrentFlow(facesContext);
        
        return flow != null;
    }
}
