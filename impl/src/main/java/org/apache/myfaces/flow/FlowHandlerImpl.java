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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.application.NavigationHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowCallNode;
import javax.faces.flow.FlowHandler;
import javax.faces.flow.FlowNode;
import javax.faces.flow.Parameter;
import javax.faces.flow.ReturnNode;
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
    private final static String ROOT_LAST_VIEW_ID = "oam.flow.ROOT_LAST_VIEW_ID.";
    
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
        
        
        _FlowContextualInfo info = getCurrentFlowReference(context, clientWindow);
        if (info == null)
        {
            return null;
        }
        FlowReference flowReference = info.getFlowReference();
        return getFlow(context, flowReference.getDocumentId(), flowReference.getId());
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
                    new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()), toViewId);
            doAfterEnterFlow(context, targetFlow, outboundParameters);
        }
        else if (targetFlow == null)
        {
            // Getting out of the flow, since targetFlow is null, just clear the stack
            List<_FlowContextualInfo> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            if (currentFlowStack != null)
            {
                //currentFlowStack.clear();
                for (int i = currentFlowStack.size()-1; i >= 0; i--)
                {
                    _FlowContextualInfo fci = currentFlowStack.get(i);
                    FlowReference fr = fci.getFlowReference();
                    doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                    popFlowReference(context, clientWindow, currentFlowStack, i);
                }
            }            
        }
        else
        {
            // Both sourceFlow and targetFlow are not null, so we need to check the direction
            // If targetFlow is on the stack, remove elements until get there.
            // If targetFlow is not there, add it to the stack.
            List<_FlowContextualInfo> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            if (currentFlowStack != null)
            {
                FlowReference targetFlowReference = new FlowReference(
                        targetFlow.getDefiningDocumentId(), targetFlow.getId());
                int targetFlowIndex = -1;
                for (int j = currentFlowStack.size()-1; j >= 0; j--)
                {
                    if (targetFlowReference.equals(currentFlowStack.get(j).getFlowReference()))
                    {
                        targetFlowIndex = j;
                        break;
                    }
                }
                if (targetFlowIndex >= 0)
                {
                    for (int i = currentFlowStack.size()-1; i > targetFlowIndex; i--)
                    {
                        _FlowContextualInfo fci = currentFlowStack.get(i);
                        FlowReference fr = fci.getFlowReference();
                        doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                        popFlowReference(context, clientWindow, currentFlowStack, i);
                    }
                }
                else
                {
                    // sourceFlow should match.
                    FlowReference sourceFlowReference = new FlowReference(
                            sourceFlow.getDefiningDocumentId(), sourceFlow.getId());
                    if ( sourceFlowReference.equals(
                        currentFlowStack.get(currentFlowStack.size()-1).getFlowReference()) )
                    {
                        Map<String, Object> outboundParameters = doBeforeEnterFlow(context,
                            targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
                        outboundCallNodeProcessed = true;
                        pushFlowReference(context, clientWindow, 
                                new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()), toViewId);
                        doAfterEnterFlow(context, targetFlow, outboundParameters);
                    }
                    else
                    {
                        // Chain gets broken. Clear stack and start again.
                        for (int i = currentFlowStack.size()-1; i >= 0; i--)
                        {
                            _FlowContextualInfo fci = currentFlowStack.get(i);
                            FlowReference fr = fci.getFlowReference();
                            doBeforeExitFlow(context, getFlow(context, fr.getDocumentId(), fr.getId()));
                            popFlowReference(context, clientWindow, currentFlowStack, i);
                        }
                        
                        Map<String, Object> outboundParameters = doBeforeEnterFlow(context,
                            targetFlow, !outboundCallNodeProcessed ? outboundCallNode : null);
                        outboundCallNodeProcessed = true;
                        pushFlowReference(context, clientWindow, 
                                new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()), toViewId);
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
                        new FlowReference(targetFlow.getDefiningDocumentId(), targetFlow.getId()), toViewId);
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

        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return false;
        }
        FlowReference reference = new FlowReference(definingDocumentId, id);
        
        for (_FlowContextualInfo info : currentFlowStack)
        {
            if (reference.equals(info.getFlowReference()))
            {
                return true;
            }
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
        String flowDocumentIdRequestParam = (String) context.getExternalContext().
            getRequestParameterMap().get(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME);
        
        if (flowDocumentIdRequestParam != null)
        {
            String flowIdRequestParam = (String) context.getExternalContext().
                getRequestParameterMap().get(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME);
            
            if (flowIdRequestParam == null)
            {
                // If we don't have an fromOutcome, it is not possible to calculate the transitions
                // involved.
                return;
            }
            
            FlowHandler flowHandler = context.getApplication().getFlowHandler();
            ConfigurableNavigationHandler nh = 
                (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
            
            if (FlowHandler.NULL_FLOW.equals(flowDocumentIdRequestParam))
            {
                // It is a return node. The trick here is we need to calculate
                // where the flow should return, because that information was not passed
                // in the parameters of the link. 
                String toFlowDocumentId = FlowHandler.NULL_FLOW;
                String targetFlowId = null;
                String fromOutcome = flowIdRequestParam;
                
                boolean failed = false;
                int i = 0;
                while (FlowHandler.NULL_FLOW.equals(toFlowDocumentId) && !failed)
                {
                    Flow currentFlow = flowHandler.getCurrentFlow(context);
                    if (currentFlow == null)
                    {
                        failed = true;
                        break;
                    }
                    String currentLastDisplayedViewId = flowHandler.getLastDisplayedViewId(context);
                    FlowNode node = currentFlow.getNode(fromOutcome);
                    if (node instanceof ReturnNode)
                    {
                        // Get the navigation case using the outcome
                        flowHandler.pushReturnMode(context);
                        currentFlow = flowHandler.getCurrentFlow(context);
                        i++;
                        
                        NavigationCase navCase = nh.getNavigationCase(context, null, 
                            ((ReturnNode) node).getFromOutcome(context), FlowHandler.NULL_FLOW);

                        if (navCase == null)
                        {
                            if (currentLastDisplayedViewId != null)
                            {
                                if (currentFlow != null)
                                {
                                    toFlowDocumentId = currentFlow.getDefiningDocumentId();
                                    targetFlowId = currentFlow.getId();
                                }
                                else
                                {
                                    // No active flow
                                    toFlowDocumentId = null;
                                }
                            }
                            else
                            {
                                // Invalid state because no navCase and 
                                // no saved lastDisplayedViewId into session
                                failed = true;
                            }
                        }
                        else
                        {
                            if (FlowHandler.NULL_FLOW.equals(navCase.getToFlowDocumentId()))
                            {
                                fromOutcome = navCase.getFromOutcome();
                            }
                            else
                            {
                                // The absence of FlowHandler.NULL_FLOW means the return went somewhere else.
                                if (currentFlow != null)
                                {
                                    toFlowDocumentId = currentFlow.getDefiningDocumentId();
                                    targetFlowId = currentFlow.getId();
                                }
                                else
                                {
                                    // No active flow
                                    toFlowDocumentId = null;
                                }
                            }
                        }
                    }
                    else
                    {
                        failed = true;
                    }
                }
                for (int j = 0; j<i; j++)
                {
                    flowHandler.popReturnMode(context);
                }
                if (failed)
                {
                    // Do nothing.
                }
                else 
                {
                    Flow targetFlow = targetFlowId == null ? null : 
                        getFlow(context, toFlowDocumentId, targetFlowId);
                    //Call transitions.
                    flowHandler.transition(context, 
                        flowHandler.getCurrentFlow(context),
                        targetFlow, null, context.getViewRoot().getViewId());
                }
            }
            else
            {
                // This transition is for start a new flow. In this case 
                // FlowHandler.FLOW_ID_REQUEST_PARAM_NAME could be the flow name to enter
                // or the flow call node to activate.
                Flow currentFlow = flowHandler.getCurrentFlow(context);
                FlowNode node = currentFlow != null ? currentFlow.getNode(flowIdRequestParam) : null;
                Flow targetFlow = null;
                FlowCallNode outboundCallNode = null;
                if (node != null && node instanceof FlowCallNode)
                {
                    outboundCallNode = (FlowCallNode) node;
                    
                    String calledFlowDocumentId = outboundCallNode.getCalledFlowDocumentId(context);
                    if (calledFlowDocumentId == null)
                    {
                        calledFlowDocumentId = currentFlow.getDefiningDocumentId();
                    }
                    targetFlow = flowHandler.getFlow(context, 
                        calledFlowDocumentId, 
                        outboundCallNode.getCalledFlowId(context));
                    if (targetFlow == null && !"".equals(calledFlowDocumentId))
                    {
                        targetFlow = flowHandler.getFlow(context, "", 
                            outboundCallNode.getCalledFlowId(context));
                    }
                    
                    //targetFlow = flowHandler.getFlow(context, 
                    //    outboundCallNode.getCalledFlowDocumentId(context), 
                    //    outboundCallNode.getCalledFlowId(context));
                }
                else
                {
                    targetFlow = flowHandler.getFlow(context, flowDocumentIdRequestParam, flowIdRequestParam);
                }
                
                if (targetFlow != null)
                {
                    // Invoke transition
                    flowHandler.transition(context, 
                        currentFlow, targetFlow, outboundCallNode, context.getViewRoot().getViewId());

                    // TODO: Handle 2 or more flow start.
                    boolean failed = false;
                    
                    String startNodeId = targetFlow.getStartNodeId();
                    while (startNodeId != null && !failed)
                    {
                        NavigationCase navCase = nh.getNavigationCase(context, null, 
                                    startNodeId, targetFlow.getDefiningDocumentId());
                        
                        if (navCase != null && navCase.getToFlowDocumentId() != null)
                        {
                            currentFlow = flowHandler.getCurrentFlow(context);
                            node = currentFlow.getNode(navCase.getFromOutcome());
                            if (node != null && node instanceof FlowCallNode)
                            {
                                outboundCallNode = (FlowCallNode) node;
                                
                                String calledFlowDocumentId = outboundCallNode.getCalledFlowDocumentId(context);
                                if (calledFlowDocumentId == null)
                                {
                                    calledFlowDocumentId = currentFlow.getDefiningDocumentId();
                                }
                                targetFlow = flowHandler.getFlow(context, 
                                    calledFlowDocumentId, 
                                    outboundCallNode.getCalledFlowId(context));
                                if (targetFlow == null && !"".equals(calledFlowDocumentId))
                                {
                                    targetFlow = flowHandler.getFlow(context, "", 
                                        outboundCallNode.getCalledFlowId(context));
                                }

                                //targetFlow = flowHandler.getFlow(context, 
                                //    outboundCallNode.getCalledFlowDocumentId(context), 
                                //    outboundCallNode.getCalledFlowId(context));
                            }
                            else
                            {
                                String calledFlowDocumentId = navCase.getToFlowDocumentId();
                                if (calledFlowDocumentId == null)
                                {
                                    calledFlowDocumentId = currentFlow.getDefiningDocumentId();
                                }
                                targetFlow = flowHandler.getFlow(context, 
                                    calledFlowDocumentId, 
                                    navCase.getFromOutcome());
                                if (targetFlow == null && !"".equals(calledFlowDocumentId))
                                {
                                    targetFlow = flowHandler.getFlow(context, "", 
                                        navCase.getFromOutcome());
                                }
                                //targetFlow = flowHandler.getFlow(context, 
                                //    , navCase.getFromOutcome());
                            }
                            if (targetFlow != null)
                            {
                                flowHandler.transition(context, 
                                    currentFlow, targetFlow, outboundCallNode, context.getViewRoot().getViewId());
                                startNodeId = targetFlow.getStartNodeId();
                            }
                            else
                            {
                                startNodeId = null;
                            }
                        }
                        else
                        {
                            startNodeId = null;
                        }
                    }
                }
                
            }
        }
        // throw new UnsupportedOperationException("Not supported yet.");
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
    
    private _FlowContextualInfo getCurrentFlowReference(FacesContext context, ClientWindow clientWindow)
    {
        if ( Boolean.TRUE.equals(context.getAttributes().get(RETURN_MODE)) )
        {
            List<_FlowContextualInfo> returnFlowList = getCurrentReturnModeFlowStack(
                    context, clientWindow, CURRENT_FLOW_REQUEST_STACK);
            if (returnFlowList != null && !returnFlowList.isEmpty())
            {
                _FlowContextualInfo info = returnFlowList.get(returnFlowList.size()-1);
                return info;
            }
            return null;
        }
        else
        {
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
            String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
            List<_FlowContextualInfo> currentFlowStack = 
                (List<_FlowContextualInfo>) sessionMap.get(currentFlowMapKey);
            if (currentFlowStack == null)
            {
                return null;
            }
            return currentFlowStack.size() > 0 ? 
                currentFlowStack.get(currentFlowStack.size()-1) : null;
        }
    }
    
    private void pushFlowReference(FacesContext context, ClientWindow clientWindow, FlowReference flowReference,
        String toViewId)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) sessionMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            currentFlowStack = new ArrayList<_FlowContextualInfo>(4);
            sessionMap.put(currentFlowMapKey, currentFlowStack);
        }
        if (!currentFlowStack.isEmpty())
        {
            currentFlowStack.get(currentFlowStack.size()-1).setLastDisplayedViewId(context.getViewRoot().getViewId());
        }
        else
        {
            //Save root lastDisplayedViewId
            context.getExternalContext().getSessionMap().put(ROOT_LAST_VIEW_ID + clientWindow.getId(), 
                context.getViewRoot().getViewId());
        }
        currentFlowStack.add(new _FlowContextualInfo(flowReference, toViewId));
    }
    
    private void popFlowReference(FacesContext context, ClientWindow clientWindow,
        List<_FlowContextualInfo> currentFlowStack, int i)
    {
        currentFlowStack.remove(i);
        if (currentFlowStack.isEmpty())
        {
            // Remove it from session but keep it in request scope.
            context.getAttributes().put(ROOT_LAST_VIEW_ID, 
                context.getExternalContext().getSessionMap().remove(ROOT_LAST_VIEW_ID + clientWindow.getId()));
        }
    }
    
    private List<_FlowContextualInfo> getCurrentFlowStack(FacesContext context, ClientWindow clientWindow)
    {
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String currentFlowMapKey = CURRENT_FLOW_STACK + clientWindow.getId();
        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) sessionMap.get(currentFlowMapKey);
        return currentFlowStack;
    }

    @Override
    public String getLastDisplayedViewId(FacesContext context)
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
        
        _FlowContextualInfo info = getCurrentFlowReference(context, clientWindow);
        if (info == null)
        {
            String lastDisplayedViewId = (String) context.getAttributes().get(ROOT_LAST_VIEW_ID);
            if (lastDisplayedViewId == null)
            {
                lastDisplayedViewId = (String) context.getExternalContext().getSessionMap().
                    get(ROOT_LAST_VIEW_ID + clientWindow.getId());
            }
            return lastDisplayedViewId;
        }
        return info.getLastDisplayedViewId();
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
            List<_FlowContextualInfo> currentFlowStack = getCurrentFlowStack(context, clientWindow);
            
            Map<Object, Object> attributesMap = context.getAttributes();
            String returnFlowMapKey = CURRENT_FLOW_REQUEST_STACK + clientWindow.getId();
            List<_FlowContextualInfo> returnFlowStack = new ArrayList<_FlowContextualInfo>(currentFlowStack);
            attributesMap.put(returnFlowMapKey, returnFlowStack);
            context.getAttributes().put(RETURN_MODE, Boolean.TRUE);
        }
        
        _FlowContextualInfo flowReference = popFlowReferenceReturnMode(context, 
            clientWindow, CURRENT_FLOW_REQUEST_STACK);
        pushFlowReferenceReturnMode(context, clientWindow, FLOW_RETURN_STACK, flowReference);
    }

    @Override
    public void popReturnMode(FacesContext context)
    {
        ClientWindow clientWindow = context.getExternalContext().getClientWindow();
        
        if (clientWindow == null)
        {
            return;
        }
        
        _FlowContextualInfo flowReference = popFlowReferenceReturnMode(context, clientWindow, FLOW_RETURN_STACK);
        pushFlowReferenceReturnMode(context, clientWindow, CURRENT_FLOW_REQUEST_STACK, flowReference);
        
        Map<Object, Object> attributesMap = context.getAttributes();
        String returnFlowMapKey = FLOW_RETURN_STACK + clientWindow.getId();
        List<_FlowContextualInfo> returnFlowStack = (List<_FlowContextualInfo>) attributesMap.get(returnFlowMapKey);
        if (returnFlowStack != null && returnFlowStack.isEmpty())
        {
            context.getAttributes().put(RETURN_MODE, Boolean.FALSE);
        }
    }

    private void pushFlowReferenceReturnMode(FacesContext context, ClientWindow clientWindow,
            String stackKey, _FlowContextualInfo flowReference)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) attributesMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            currentFlowStack = new ArrayList<_FlowContextualInfo>(4);
            attributesMap.put(currentFlowMapKey, currentFlowStack);
        }
        currentFlowStack.add(flowReference);
    }

    private _FlowContextualInfo popFlowReferenceReturnMode(FacesContext context, ClientWindow clientWindow,
            String stackKey)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) attributesMap.get(currentFlowMapKey);
        if (currentFlowStack == null)
        {
            return null;
        }
        return currentFlowStack.size() > 0 ? currentFlowStack.remove(currentFlowStack.size()-1) : null;
    }
    
    private List<_FlowContextualInfo> getCurrentReturnModeFlowStack(FacesContext context, ClientWindow clientWindow,
            String stackKey)
    {
        Map<Object, Object> attributesMap = context.getAttributes();
        String currentFlowMapKey = stackKey + clientWindow.getId();
        List<_FlowContextualInfo> currentFlowStack = (List<_FlowContextualInfo>) attributesMap.get(currentFlowMapKey);
        return currentFlowStack;
    }
}
