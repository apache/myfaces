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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import javax.faces.application.NavigationHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowCallNode;
import javax.faces.flow.FlowHandler;
import javax.faces.flow.Parameter;
import javax.faces.lifecycle.ClientWindow;
import org.apache.myfaces.spi.FacesFlowProvider;
import org.apache.myfaces.spi.FacesFlowProviderFactory;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowHandlerImpl extends FlowHandler
{
    private final static String CURRENT_FLOW_STACK = "oam.flow.STACK.";
    
    private final static String RETURN_MODE = "oam.flow.RETURN_MODE";
    private final static String FLOW_RETURN_STACK = "oam.flow.RETURN_STACK.";
    private final static String CURRENT_FLOW_REQUEST_STACK = "oam.flow.REQUEST_STACK.";
    
    private Map<String, Map<String, Flow>> _flowMapByDocumentId;
    private Map<String, Flow> _flowMapById;
    
    private FacesFlowProvider _facesFlowProvider;
    
    public FlowHandlerImpl()
    {
        _flowMapByDocumentId = new ConcurrentHashMap<String, Map<String, Flow>>();
        _flowMapById = new ConcurrentHashMap<String, Flow>();
    }

    @Override
    public Flow getFlow(FacesContext context, String definingDocumentId, String id)
    {
        checkNull(context, "context");
        checkNull(definingDocumentId, "definingDocumentId");
        checkNull(id, "id");
        
        // First try the combination.
        Map<String, Flow> flowMap = _flowMapByDocumentId.get(definingDocumentId);
        if (flowMap != null)
        {
            Flow flow = flowMap.get(id);
            if (flow != null)
            {
                return flow;
            }
        }
        
        //if definingDocumentId is an empty string, 
        if ("".equals(definingDocumentId))
        {
            return _flowMapById.get(id);
        }
        return null;
    }

    @Override
    public void addFlow(FacesContext context, Flow toAdd)
    {
        checkNull(context, "context");
        checkNull(toAdd, "toAdd");
        
        String id = toAdd.getId();
        String definingDocumentId = toAdd.getDefiningDocumentId();
        
        if (id == null)
        {
            throw new IllegalArgumentException("Flow must have a non null id");
        }
        else if (id.length() == 0)
        {
            throw new IllegalArgumentException("Flow must have a non empty id");
        }
        if (definingDocumentId == null)
        {
            throw new IllegalArgumentException("Flow must have a non null definingDocumentId");
        }
        
        Map<String, Flow> flowMap = _flowMapByDocumentId.get(definingDocumentId);
        if (flowMap == null)
        {
            flowMap = new ConcurrentHashMap<String, Flow>();
            _flowMapByDocumentId.put(definingDocumentId, flowMap);
        }
        flowMap.put(id, toAdd);
        
        Flow duplicateFlow = _flowMapById.get(id);
        if (duplicateFlow != null)
        {
            // There are two flows with the same flowId.
            // Give priority to the flow with no defining document id
            if ("".equals(toAdd.getDefiningDocumentId()))
            {
                _flowMapById.put(id, toAdd);
            }
            else if ("".equals(duplicateFlow.getDefiningDocumentId()))
            {
                // Already added, skip
            }
            else
            {
                // Put the last one
                _flowMapById.put(id, toAdd);
            }
        }
        else
        {
            _flowMapById.put(id, toAdd);
        }

        // Once the flow is added to the map, it is still necessary to 
        // pass the flow to the ConfigurableNavigationHandler, so it can be
        // inspected for navigation rules. This is the best place to do that because
        // the spec says "... Called by the flow system to cause the flow to 
        // be inspected for navigation rules... " (note it says "flow system" not
        // "configuration system" where the calls to addFlow() are done).
        invokeInspectFlow(context, context.getApplication().getNavigationHandler(), toAdd);
    }

    @Override
    public Flow getCurrentFlow(FacesContext context)
    {
        Object session = context.getExternalContext().getSession(false);
        if (session == null)
        {
            return null;
        }
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        if (clientWindow == null)
        {
            return null;
        }
        
        
        FlowReference flowReference = getCurrentFlowReference(context, clientWindow);
        //String flowReference = getCurrentFlowReference(context, clientWindow);
        if (flowReference == null)
        {
            return null;
        }
        return getFlow(context, flowReference.getDocumentId(), flowReference.getId());
        //return getFlow(context, null, flowReference);
    }
    
    @Override
    public void transition(FacesContext context, Flow sourceFlow, Flow targetFlow, 
        FlowCallNode outboundCallNode, String toViewId)
    {
        checkNull(context, "context");
        checkNull(toViewId, "toViewId");
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        boolean outboundCallNodeProcessed = false;
        if (clientWindow == null)
        {
            return;
        }
        
        // TODO: Implement me! In theory here lies the push/pop logic, but to know how it works, it is necessary
        // to add the code inside NavigationHandlerImpl first. For now, the logic only allows 1 flow at the time
        // but this should work with multiple nested flows.
        if (sourceFlow == null && targetFlow == null)
        {
            return;
        }

        if (sourceFlow == null)
        {
            // Entering a flow
            Map<String, Object> outboundParameters = doBeforeEnterFlow(context, 
                targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
            outboundCallNodeProcessed = true;
            pushFlowReference(context, clientWindow, 
                    new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()));
            doAfterEnterFlow(context, targetFlow, outboundParameters);
        }
        else if (targetFlow == null)
        {
            // Getting out of the flow, since targetFlow is null, just clear the stack
            List<FlowReference> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            if (currentFlowStack != null)
            {
                //currentFlowStack.clear();
                for (int i = currentFlowStack.size()-1; i >= 0; i--)
                {
                    FlowReference fr = currentFlowStack.get(i);
                    doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                    currentFlowStack.remove(i);
                }
            }            
        }
        else
        {
            // Both sourceFlow and targetFlow are not null, so we need to check the direction
            // If targetFlow is on the stack, remove elements until get there.
            // If targetFlow is not there, add it to the stack.
            List<FlowReference> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            if (currentFlowStack != null)
            {
                FlowReference targetFlowReference = new FlowReference(
                        targetFlow.getDefiningDocumentId(), targetFlow.getId());
                int targetFlowIndex = currentFlowStack.lastIndexOf(targetFlowReference);
                if (targetFlowIndex >= 0)
                {
                    for (int i = currentFlowStack.size()-1; i > targetFlowIndex; i--)
                    {
                        FlowReference fr = currentFlowStack.get(i);
                        doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                        currentFlowStack.remove(i);
                    }
                }
                else
                {
                    // sourceFlow should match.
                    FlowReference sourceFlowReference = new FlowReference(
                            sourceFlow.getDefiningDocumentId(), sourceFlow.getId());
                    if ( sourceFlowReference.equals(currentFlowStack.get(currentFlowStack.size()-1)) )
                    {
                        Map<String, Object> outboundParameters = doBeforeEnterFlow(context,
                            targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
                        outboundCallNodeProcessed = true;
                        pushFlowReference(context, clientWindow, 
                                new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()));
                        doAfterEnterFlow(context, targetFlow, outboundParameters);
                    }
                    else
                    {
                        // Chain gets broken. Clear stack and start again.
                        //currentFlowStack.clear();
                        for (int i = currentFlowStack.size()-1; i >= 0; i--)
                        {
                            FlowReference fr = currentFlowStack.get(i);
                            doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                            currentFlowStack.remove(i);
                        }
                        
                        Map<String, Object> outboundParameters = doBeforeEnterFlow(context,
                            targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
                        outboundCallNodeProcessed = true;
                        pushFlowReference(context, clientWindow, 
                                new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()));
                        doAfterEnterFlow(context, targetFlow, outboundParameters);
                    }
                }
            }
            else
            {
                Map<String, Object> outboundParameters = doBeforeEnterFlow(context, 
                    targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
                outboundCallNodeProcessed = true;
                pushFlowReference(context, clientWindow, 
                        new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()));
                doAfterEnterFlow(context, targetFlow, outboundParameters);
            }
        }
    }
    
    private Map<String, Object> doBeforeEnterFlow(FacesContext context, Flow flow, FlowCallNode outboundCallNode)
    {
        Map<String, Object> outboundParameters = null;
        if (outboundCallNode != null && !outboundCallNode.getOutboundParameters().isEmpty())
        {
            outboundParameters = new HashMap<String, Object>();
            for (Map.Entry<String, Parameter> entry : outboundCallNode.getOutboundParameters().entrySet())
            {
                Parameter parameter = entry.getValue();
                if (parameter.getValue() != null)
                {
                    outboundParameters.put(entry.getKey(), parameter.getValue().getValue(context.getELContext()));
                }
            }
        }
        return outboundParameters;
    }
    
    private void doAfterEnterFlow(FacesContext context, Flow flow, Map<String, Object> outboundParameters)
    {
        getFacesFlowProvider(context).doAfterEnterFlow(context, flow);
        
        if (flow.getInitializer() != null)
        {
            flow.getInitializer().invoke(context.getELContext(), null);
        }
        
        if (outboundParameters != null)
        {
            for (Map.Entry<String, Parameter> entry : flow.getInboundParameters().entrySet())
            {
                Parameter parameter = entry.getValue();
                if (parameter.getValue() != null && outboundParameters.containsKey(entry.getKey()))
                {
                    parameter.getValue().setValue(context.getELContext(), outboundParameters.get(entry.getKey()));
                }
            }
        }
    }
    
    public FacesFlowProvider getFacesFlowProvider(FacesContext facesContext)
    {
        if (_facesFlowProvider == null)
        {
            FacesFlowProviderFactory factory = 
                FacesFlowProviderFactory.getFacesFlowProviderFactory(
                    facesContext.getExternalContext());
            _facesFlowProvider = factory.getFacesFlowProvider(
                    facesContext.getExternalContext());
        }
        return _facesFlowProvider;
    }
    
    private void doBeforeExitFlow(FacesContext context, Flow flow)
    {
        getFacesFlowProvider(context).doBeforeExitFlow(context, flow);
        
        if (flow.getFinalizer() != null)
        {
            flow.getFinalizer().invoke(context.getELContext(), null);
        }
    }

    @Override
    public boolean isActive(FacesContext context, String definingDocumentId, String id)
    {
        checkNull(context, "context");
        checkNull(definingDocumentId, "definingDocumentId");
        checkNull(id, "id");
        
        Object session = context.getExternalContext().getSession(false);
        if (session != null)
        {
            return false;
        }
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        if (clientWindow == null)
        {
            return false;
        }
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();

        List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return false;
        }
        FlowReference reference = new FlowReference(definingDocumentId, id);
        if (currentFlowStack.contains(reference))
        {
            return true;
        }
        return false;
    }

    @Override
    public Map<Object, Object> getCurrentFlowScope()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return getFacesFlowProvider(facesContext).getCurrentFlowScope(facesContext);
    }

    @Override
    public void clientWindowTransition(FacesContext context)
    {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO: Something like this should be included in the spec.
     * 
     * @param context
     * @return 
     */
    private List<Flow> getFlowStack(FacesContext context)
    {
        Object session = context.getExternalContext().getSession(false);
        if (session != null)
        {
            return Collections.emptyList();
        }
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        if (clientWindow == null)
        {
            return Collections.emptyList();
        }
        
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return Collections.emptyList();
        }

        // Convert flowReference list into Flow list
        List<Flow> flowList = new ArrayList<Flow>(currentFlowStack.size());
        for (FlowReference flowReference : currentFlowStack)
        {
            flowList.add(getFlow(context, flowReference.getDocumentId(), flowReference.getId()));
        }
        return flowList;
    }

    private void checkNull(final Object o, final String param)
    {
        if (o == null)
        {
            throw new NullPointerException(param + " can not be null.");
        }
    }
    
    private void invokeInspectFlow(FacesContext context, NavigationHandler navHandler, Flow toAdd)
    {
        if (navHandler instanceof ConfigurableNavigationHandler)
        {
            ((ConfigurableNavigationHandler)navHandler).inspectFlow(context, toAdd);
        }
        else if (navHandler instanceof NavigationHandlerWrapper)
        {
            invokeInspectFlow(context, ((NavigationHandlerWrapper)navHandler).getWrapped(), toAdd);
        }
    }
    
    private FlowReference getCurrentFlowReference(FacesContext context, ClientWindow clientWindow)
    {
        if ( Boolean.TRUE.equals(context.getAttributes().get(RETURN_MODE)) )
        {
            List<FlowReference> returnFlowList = getCurrentReturnModeFlowStack(
                    context, clientWindow, CURRENT_FLOW_REQUEST_STACK);
            if (returnFlowList != null && !returnFlowList.isEmpty())
            {
                return returnFlowList.get(returnFlowList.size()-1);
            }
            return null;
        }
        else
        {
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
            String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
            List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
            if (currentFlowStack == null)
            {
                return null;
            }
            return currentFlowStack.size() > 0 ? currentFlowStack.get(currentFlowStack.size()-1) : null;
        }
    }
    
    private void pushFlowReference(FacesContext context, ClientWindow clientWindow, FlowReference flowReference)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            currentFlowStack = new ArrayList<FlowReference>(4);
            sessionMap.put(currentFlowMapKey, currentFlowStack);
        }
        currentFlowStack.add(flowReference);
    }

    private FlowReference popFlowReference(FacesContext context, ClientWindow clientWindow)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return null;
        }
        return currentFlowStack.size() > 0 ? currentFlowStack.remove(currentFlowStack.size()-1) : null;
    }
    
    private List<FlowReference> getCurrentFlowStack(FacesContext context, ClientWindow clientWindow)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) sessionMap.get(currentFlowMapKey);
        return currentFlowStack;
    }

    @Override
    public String getLastDisplayedViewId(FacesContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pushReturnMode(FacesContext context)
    {
        // The return mode is a way to allow NavigationHandler to know the context
        // without expose it. The idea is call pushReturnMode()/popReturnMode() and
        // then check for getCurrentFlow(). 
        //
        // Remember the navigation algorithm is split in two parts:
        // - Calculates the navigation
        // - Perform the navigation
        //
        // Generated links requires only to perform the first one, but the operations
        // are only perfomed when the transition between pages occur or in a get request
        // when there is a pending navigation. 
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        
        if (clientWindow == null)
        {
            return;
        }
        
        if ( Boolean.TRUE.equals(context.getAttributes().get(RETURN_MODE)) )
        {
            // Return mode active
            
        }
        else
        {
            // Return mode not active, activate it, copy the current flow stack.
            List<FlowReference> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            
            Map<Object, Object> attributesMap = context.getAttributes();
            String returnFlowMapKey = CURRENT_FLOW_REQUEST_STACK + clientWindow.getId();
            List<FlowReference> returnFlowStack = new ArrayList<FlowReference>(currentFlowStack);
            attributesMap.put(returnFlowMapKey, returnFlowStack);
            context.getAttributes().put(RETURN_MODE, Boolean.TRUE);
        }
        
        FlowReference flowReference = popFlowReferenceReturnMode(context, clientWindow, CURRENT_FLOW_REQUEST_STACK);
        pushFlowReferenceReturnMode(context, clientWindow, FLOW_RETURN_STACK, flowReference);
        //popFlowReferenceReturnMode(context, clientWindow);
    }

    @Override
    public void popReturnMode(FacesContext context)
    {
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        
        if (clientWindow == null)
        {
            return;
        }
        
        FlowReference flowReference = popFlowReferenceReturnMode(context, clientWindow, CURRENT_FLOW_REQUEST_STACK);
        pushFlowReferenceReturnMode(context, clientWindow, FLOW_RETURN_STACK, flowReference);
        
        Map<Object, Object> attributesMap = context.getAttributes();
        String returnFlowMapKey = CURRENT_FLOW_REQUEST_STACK + clientWindow.getId();
        List<FlowReference> returnFlowStack = (List<FlowReference>) attributesMap.get(returnFlowMapKey);
        if (returnFlowStack != null && returnFlowStack.isEmpty())
        {
            context.getAttributes().put(RETURN_MODE, Boolean.FALSE);
        }
    }

    private void pushFlowReferenceReturnMode(FacesContext context, ClientWindow clientWindow,
            String stackKey, FlowReference flowReference)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) attributesMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            //currentFlowStack = new LinkedList<FlowReference>();
            currentFlowStack = new ArrayList<FlowReference>(4);
            attributesMap.put(currentFlowMapKey, currentFlowStack);
        }
        currentFlowStack.add(flowReference);
    }

    private FlowReference popFlowReferenceReturnMode(FacesContext context, ClientWindow clientWindow,
            String stackKey)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) attributesMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return null;
        }
        return currentFlowStack.size() > 0 ? currentFlowStack.remove(currentFlowStack.size()-1) : null;
    }
    
    private List<FlowReference> getCurrentReturnModeFlowStack(FacesContext context, ClientWindow clientWindow,
            String stackKey)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        //LinkedList<FlowReference> currentFlowStack = (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
        List<FlowReference> currentFlowStack = (List<FlowReference>) attributesMap.get(currentFlowMapKey);
        return currentFlowStack;
    }
        
    private class FlowContext
    {
        public final static String FLOW_CONTEXT_KEY = "oam.flow.CTX";
        
        private List<String> currentFlowStack;
        private int index;

        public FlowContext getFlowContext(FacesContext facesContext)
        {
            FlowContext fctx = (FlowContext) facesContext.getAttributes().get(FLOW_CONTEXT_KEY);
            if (fctx == null)
            {
                fctx = new FlowContext();
            }
            return fctx;
        }
        
        public List<String> getCurrentFlowStack(FacesContext facesContext)
        {
            if (currentFlowStack == null)
            {
                Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                String currentFlowMapKey = CURRENT_FLOW_STACK + 
                    facesContext.getExternalContext().getClientWindow().getId();
                // LinkedList<FlowReference> currentFlowStack = 
                // (LinkedList<FlowReference>) sessionMap.get(currentFlowMapKey);
                currentFlowStack = (List<String>) sessionMap.get(currentFlowMapKey);
            }
            return currentFlowStack;
        }
    }
}
