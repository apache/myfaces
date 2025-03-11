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
package org.apache.myfaces.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.el.MethodExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.FlowNode;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.ReturnNode;
import jakarta.faces.flow.SwitchNode;
import jakarta.faces.flow.ViewNode;
import jakarta.faces.lifecycle.ClientWindow;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowImpl extends Flow implements Freezable
{
    private MethodExpression initializer;
    private MethodExpression finalizer;
    private String startNodeId;
    private String id;
    private String definingDocumentId;
    
    private Map<String, FlowNode> flowNodeMap;
    
    // The idea is use a normal HashMap, since there will not be modifications
    // after initialization ( all setters must call checkInitialized() )
    private Map<String, Parameter> inboundParametersMap;
    private Map<String, FlowCallNode> flowCallsMap;
    private List<MethodCallNode> methodCallsList;
    private Map<String, ReturnNode> returnsMap;
    private Map<String, SwitchNode> switchesMap;
    private List<ViewNode> viewsList;
    
    // Note this class should be thread safe and immutable once
    // the flow is initialized or placed into service by the runtime.
    private Map<String, Parameter> unmodifiableInboundParametersMap;
    private Map<String, FlowCallNode> unmodifiableFlowCallsMap;
    private List<MethodCallNode> unmodifiableMethodCallsList;
    private Map<String, ReturnNode> unmodifiableReturnsMap;
    private Map<String, SwitchNode> unmodifiableSwitchesMap;
    private List<ViewNode> unmodifiableViewsList;
    
    private Map<String, Set<NavigationCase>> navigationCases;
    private Map<String, Set<NavigationCase>> unmodifiableNavigationCases;
    
    // No need to make it volatile, because FlowImpl instances are
    // created and initialized only at application startup, by a single
    // thread.
    private boolean initialized;
    
    public FlowImpl()
    {
        flowNodeMap = new HashMap<>();
        inboundParametersMap = new HashMap<>();
        flowCallsMap = new HashMap<>();
        methodCallsList = new ArrayList<>();
        returnsMap = new HashMap<>();
        switchesMap = new HashMap<>();
        viewsList = new ArrayList<>();
        navigationCases = new HashMap<>();
        
        // Collections.unmodifiableMap(...) uses delegation pattern, so as long
        // as we don't modify _inboundParametersMap in the wrong time, it
        // will be thread safe and immutable.
        unmodifiableInboundParametersMap = Collections.unmodifiableMap(inboundParametersMap);
        unmodifiableFlowCallsMap = Collections.unmodifiableMap(flowCallsMap);
        unmodifiableMethodCallsList = Collections.unmodifiableList(methodCallsList);
        unmodifiableReturnsMap = Collections.unmodifiableMap(returnsMap);
        unmodifiableSwitchesMap = Collections.unmodifiableMap(switchesMap);
        unmodifiableViewsList = Collections.unmodifiableList(viewsList);
        
        unmodifiableNavigationCases = Collections.unmodifiableMap(navigationCases);
    }
    
    @Override
    public void freeze()
    {
        initialized = true;
        
        for (Map.Entry<String, Parameter> entry : inboundParametersMap.entrySet())
        {
            if (entry.getValue() instanceof Freezable)
            {
                ((Freezable)entry.getValue()).freeze();
            }
        }
            
        for (Map.Entry<String, FlowCallNode> entry : flowCallsMap.entrySet())
        {
            if (entry.getValue() instanceof Freezable)
            {
                ((Freezable)entry.getValue()).freeze();
            }
        }

        for (MethodCallNode value : methodCallsList)
        {
            if (value instanceof Freezable freezable)
            {
                freezable.freeze();
            }
        }

        for (Map.Entry<String, ReturnNode> entry : returnsMap.entrySet())
        {
            if (entry.getValue() instanceof Freezable)
            {
                ((Freezable)entry.getValue()).freeze();
            }
        }

        for (Map.Entry<String, SwitchNode> entry : switchesMap.entrySet())
        {
            if (entry.getValue() instanceof Freezable)
            {
                ((Freezable)entry.getValue()).freeze();
            }
        }
        
        for (ViewNode value : viewsList)
        {
            if (value instanceof Freezable freezable)
            {
                freezable.freeze();
            }
        }
    }

    @Override
    public String getClientWindowFlowId(ClientWindow curWindow)
    {
        String id = getId();
        String documentId = getDefiningDocumentId();
        // Faces Flow relies on ClientWindow feature, so it should be enabled,
        // and the expected id cannot be null.
        String windowId = curWindow.getId();
        StringBuilder sb = new StringBuilder( id.length() + 1 + windowId.length() );
        sb.append(windowId).append('_').append(documentId).append('_').append(id);
        return sb.toString();
    }

    @Override
    public String getDefiningDocumentId()
    {
        return definingDocumentId;
    }
    
    public void setDefiningDocumentId(String definingDocumentId)
    {
        checkInitialized();
        this.definingDocumentId = definingDocumentId;
    }

    @Override
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        checkInitialized();
        this.id = id;
    }

    @Override
    public MethodExpression getInitializer()
    {
        return initializer;
    }
    
    public void setInitializer(MethodExpression initializer)
    {
        checkInitialized();
        this.initializer = initializer;
    }

    @Override
    public MethodExpression getFinalizer()
    {
        return finalizer;
    }
    
    public void setFinalizer(MethodExpression finalizer)
    {
        checkInitialized();
        this.finalizer = finalizer;
    }

    @Override
    public String getStartNodeId()
    {
        return startNodeId;
    }
    
    public void setStartNodeId(String startNodeId)
    {
        checkInitialized();
        this.startNodeId = startNodeId;
    }
    
    @Override
    public Map<String, Parameter> getInboundParameters()
    {
        return unmodifiableInboundParametersMap;
    }
    
    public void putInboundParameter(String key, Parameter value)
    {
        checkInitialized();
        inboundParametersMap.put(key, value);
    }
    
    @Override
    public Map<String, FlowCallNode> getFlowCalls()
    {
        return unmodifiableFlowCallsMap;
    }
    
    public void putFlowCall(String key, FlowCallNode value)
    {
        checkInitialized();
        flowCallsMap.put(key, value);
        flowNodeMap.put(value.getId(), value);
    }

    @Override
    public List<MethodCallNode> getMethodCalls()
    {
        return unmodifiableMethodCallsList;
    }

    public void addMethodCall(MethodCallNode value)
    {
        checkInitialized();
        methodCallsList.add(value);
        flowNodeMap.put(value.getId(), value);
    }

    @Override
    public Map<String, ReturnNode> getReturns()
    {
        return unmodifiableReturnsMap;
    }
    
    public void putReturn(String key, ReturnNode value)
    {
        checkInitialized();
        returnsMap.put(key, value);
        flowNodeMap.put(value.getId(), value);
    }

    @Override
    public Map<String, SwitchNode> getSwitches()
    {
        return unmodifiableSwitchesMap;
    }
    
    public void putSwitch(String key, SwitchNode value)
    {
        checkInitialized();
        switchesMap.put(key, value);
        flowNodeMap.put(value.getId(), value);
    }

    @Override
    public List<ViewNode> getViews()
    {
        return unmodifiableViewsList;
    }
    
    public void addView(ViewNode value)
    {
        checkInitialized();
        viewsList.add(value);
        flowNodeMap.put(value.getId(), value);
    }

    @Override
    public FlowCallNode getFlowCall(Flow targetFlow)
    {
        FacesContext facesContext = null;
        for (Map.Entry<String, FlowCallNode> entry : flowCallsMap.entrySet())
        {
            if (facesContext == null)
            {
                facesContext = FacesContext.getCurrentInstance();
            }
            String calledDocumentId = entry.getValue().getCalledFlowDocumentId(facesContext);
            String calledFlowId = entry.getValue().getCalledFlowId(facesContext);
            if (targetFlow.getDefiningDocumentId().equals(calledDocumentId) &&
                targetFlow.getId().equals(calledFlowId) )
            {
                return entry.getValue();
            }
        }
        return null;
    }
    
    @Override
    public FlowNode getNode(String nodeId)
    {
        return flowNodeMap.get(nodeId);
    }
    
    public void addNavigationCases(String fromViewId, Set<NavigationCase> navigationCases)
    {
        checkInitialized();

        Set<NavigationCase> navigationCaseSet = this.navigationCases.computeIfAbsent(fromViewId,
                k -> new HashSet<>());
        navigationCaseSet.addAll(navigationCases);
    }
    
    public void addNavigationCase(NavigationCase navigationCase)
    {
        checkInitialized();

        Set<NavigationCase> navigationCaseSet = navigationCases.computeIfAbsent(navigationCase.getFromViewId(),
                k -> new HashSet<>());
        navigationCaseSet.add(navigationCase);
    }
    
    public void removeNavigationCase(NavigationCase navigationCase)
    {
        checkInitialized();
        Set<NavigationCase> navigationCaseSet = navigationCases.get(navigationCase.getFromViewId());
        if (navigationCaseSet == null)
        {
            return;
        }
        navigationCaseSet.remove(navigationCase);
    }

    private void checkInitialized() throws IllegalStateException
    {
        if (initialized)
        {
            throw new IllegalStateException("Flow is immutable once initialized");
        }
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        return unmodifiableNavigationCases;
    }
    
}
