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
package org.apache.myfaces.view.facelets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PreRemoveFromViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;
import javax.faces.view.ViewMetadata;

import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.shared.util.HashMapUtils;

/**
 * This class implements partial state saving feature when facelets
 * is used to render pages. (Theorically it could be applied on jsp case too,
 * but all considerations below should be true before apply it).
 * 
 * The following considerations apply for this class:
 * 
 * 1. This StateManagementStrategy should only be active if javax.faces.PARTIAL_STATE_SAVING
 *    config param is active(true). See javadoc on StateManager for details.
 * 2. A map using component clientId as keys are used to hold the state.
 * 3. Each component has a valid id after ViewDeclarationLanguage.buildView().
 *    This implies that somewhere, every TagHandler that create an UIComponent 
 *    instance should call setId and assign it.
 * 4. Every TagHandler that create an UIComponent instance should call markInitialState
 *    after the component is populated. Otherwise, full state is always saved.
 * 5. A SystemEventListener is used to keep track for added and removed components, listen
 *    PostAddToViewEvent and PreRemoveFromViewEvent event triggered by UIComponent.setParent()
 *    method.
 * 6. It is not possible to use javax.faces.component.visit API to traverse the component
 *    tree during save/restore, because UIData.visitTree traverse all rows and we only need
 *    to restore state per component (not per row).
 * 7. It is necessary to preserve the order of the children added/removed between requests.
 * 8. Added and removed components could be seen as subtrees. This imply that we need to save
 *    the structure of the added components subtree and remove one component could be remove
 *    all its children and facets from view inclusive.
 * 9. It is necessary to save and restore the list of added/removed components between several
 *    requests.
 * 10.All components ids removed in any moment of time must be preserved.
 * 11.Each component must be restored only once.
 * 11.The order is important for ids added when it is traversed the tree, otherwise the algorithm 
 *    could change the order in which components will be restored.  
 * 
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 793245 $ $Date: 2009-07-11 18:50:53 -0500 (Sat, 11 Jul 2009) $
 * @since 2.0
 *
 */
public class DefaultFaceletsStateManagementStrategy extends StateManagementStrategy
{
    public static final String CLIENTIDS_ADDED = "oam.CLIENTIDS_ADDED";
    
    public static final String CLIENTIDS_REMOVED = "oam.CLIENTIDS_REMOVED";
    
    /**
     * Key used on component attribute map to indicate if a component was added
     * after build view, so itself and all descendants should not use partial
     * state saving. There are two possible values:
     * 
     * Key not present: The component uses pss.
     * ComponentState.ADD: The component was added to the view after build view.
     * ComponentState.REMOVE_ADD: The component was removed/added to the view. Itself and all
     * descendants should be saved and restored, but we have to unregister/register
     * from CLIENTIDS_ADDED and CLIENTIDS_REMOVED lists. See ComponentSupport.markComponentToRestoreFully
     * for details.
     * ComponentState.ADDED: The component has been added or removed/added, but it has
     * been already processed.
     */
    public  static final String COMPONENT_ADDED_AFTER_BUILD_VIEW = "oam.COMPONENT_ADDED_AFTER_BUILD_VIEW"; 
    
    private static final String SERIALIZED_VIEW_REQUEST_ATTR = 
        StateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    
    private ViewDeclarationLanguageFactory _vdlFactory;
    
    private RenderKitFactory _renderKitFactory = null;
    
    public DefaultFaceletsStateManagementStrategy ()
    {
        _vdlFactory = (ViewDeclarationLanguageFactory)FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
        //TODO: This object should be application scoped and shared
        //between jsp and facelets
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public UIViewRoot restoreView (FacesContext context, String viewId, String renderKitId)
    {
        ResponseStateManager manager;
        Object state[];
        Map<String, Object> states;
        UIViewRoot view = null;
 
        // The value returned here is expected to be false (set by RestoreViewExecutor), but
        //we don't know if some ViewHandler wrapper could change it, so it is better to save the value.
        final boolean oldContextEventState = context.isProcessingEvents();
        // Get previous state from ResponseStateManager.
        manager = getRenderKitFactory().getRenderKit(context, renderKitId).getResponseStateManager();
        
        //state = (Object[]) getStateCache().restoreSerializedView(context, viewId, manager.getState(context, viewId));
        state = (Object[]) manager.getState(context, viewId);
        
        if (state == null)
        {
            //No state could be restored, return null causing ViewExpiredException
            return null;
        }
        
        if (state[1] instanceof Object[])
        {
            Object[] fullState = (Object[]) state[1]; 
            view = (UIViewRoot) internalRestoreTreeStructure((TreeStructComponent)fullState[0]);

            if (view != null) {
                context.setViewRoot (view);
                view.processRestoreState(context, fullState[1]);
            }
        }
        else
        {
            // Per the spec: build the view.
            ViewDeclarationLanguage vdl = _vdlFactory.getViewDeclarationLanguage(viewId);
            try {
                ViewMetadata metadata = vdl.getViewMetadata (context, viewId);
                
                Collection<UIViewParameter> viewParameters = null;
                
                if (metadata != null)
                {
                    view = metadata.createMetadataView(context);
                    
                    if (view != null)
                    {
                        viewParameters = metadata.getViewParameters(view);
                    }
                }
                if (view == null)
                {
                    view = context.getApplication().getViewHandler().createView(context, viewId);
                }
                
                context.setViewRoot (view); 
                
                // TODO: Why is necessary enable event processing?
                // ANS: On RestoreViewExecutor, setProcessingEvents is called first to false
                // and then to true when postback. Since we need listeners registered to PostAddToViewEvent
                // event to be handled, we should enable it again. We are waiting a response from EG about
                // the behavior of those listeners, because for partial state saving we need this listeners
                // be called from here and relocate components properly, but for now we have to let this code as is.
                try 
                {
                    context.setProcessingEvents (true);
                    vdl.buildView (context, view);
                    // In the latest code related to PostAddToView, it is
                    // triggered no matter if it is applied on postback. It seems that MYFACES-2389, 
                    // TRINIDAD-1670 and TRINIDAD-1671 are related.
                    // This code is no longer necessary, but better let it here.
                    //_publishPostBuildComponentTreeOnRestoreViewEvent(context, view);
                    suscribeListeners(view);
                }
                finally
                {
                    context.setProcessingEvents (oldContextEventState);
                }
            }
            catch (Throwable e) {
                throw new FacesException ("unable to create view \"" + viewId + "\"", e);
            }

            if (state != null && state[1] != null)
            {
                states = (Map<String, Object>) state[1];
                
                // Visit the children and restore their state.
                
                //view.visitTree (VisitContext.createVisitContext (context), new RestoreStateVisitor (states));
                
                //Restore state of current components
                restoreStateFromMap(context, states, view);
                
                // TODO: handle dynamic add/removes as mandated by the spec.  Not sure how to do handle this yet.
                List<String> clientIdsRemoved = getClientIdsRemoved(view);
                
                if (clientIdsRemoved != null)
                {
                    Set<String> idsRemovedSet = new HashSet<String>(HashMapUtils.calcCapacity(clientIdsRemoved.size()));
                    context.getAttributes().put(FaceletViewDeclarationLanguage.REMOVING_COMPONENTS_BUILD, Boolean.TRUE);
                    try
                    {
                        for (String clientId : clientIdsRemoved)
                        {
                            if (!idsRemovedSet.contains(clientId))
                            {
                                view.invokeOnComponent(context, clientId, new ContextCallback()
                                    {
                                        public void invokeContextCallback(FacesContext context,
                                                UIComponent target)
                                        {
                                            if (target.getParent() != null)
                                            {
                                                if (!target.getParent().getChildren().remove(target))
                                                {
                                                    String key = null;
                                                    if (target.getParent().getFacetCount() > 0)
                                                    {
                                                        for (Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet())
                                                        {
                                                            if (entry.getValue()==target)
                                                            {
                                                                key = entry.getKey();
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (key != null)
                                                    {
                                                        target.getParent().getFacets().remove(key);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                idsRemovedSet.add(clientId);
                            }
                        }
                        clientIdsRemoved.clear();
                        clientIdsRemoved.addAll(idsRemovedSet);
                    }
                    finally
                    {
                        context.getAttributes().remove(FaceletViewDeclarationLanguage.REMOVING_COMPONENTS_BUILD);
                    }
                }
                
                List<String> clientIdsAdded = getClientIdsAdded(view);
                if (clientIdsAdded != null)
                {
                    Set<String> idsAddedSet = new HashSet<String>(HashMapUtils.calcCapacity(clientIdsAdded.size()));
                    for (String clientId : clientIdsAdded)
                    {
                        if (!idsAddedSet.contains(clientId))
                        {
                            final AttachedFullStateWrapper wrapper = (AttachedFullStateWrapper) states.get(clientId);
                            if (wrapper != null)
                            {
                                final Object[] addedState = (Object[]) wrapper.getWrappedStateObject(); 
                                if (addedState != null)
                                {
                                    if (addedState.length == 2)
                                    {
                                        view = (UIViewRoot) internalRestoreTreeStructure((TreeStructComponent) addedState[0]);
                                        view.processRestoreState(context, addedState[1]);
                                        break;
                                    }
                                    else
                                    {
                                        final String parentClientId = (String) addedState[0];
                                        view.invokeOnComponent(context, parentClientId, new ContextCallback()
                                        {
                                            public void invokeContextCallback(FacesContext context,
                                                    UIComponent target)
                                            {
                                                if (addedState[1] != null)
                                                {
                                                    String facetName = (String) addedState[1];
                                                    UIComponent child = internalRestoreTreeStructure((TreeStructComponent) addedState[3]);
                                                    child.processRestoreState(context, addedState[4]);
                                                    target.getFacets().put(facetName,child);
                                                }
                                                else
                                                {
                                                    Integer childIndex = (Integer) addedState[2];
                                                    UIComponent child = internalRestoreTreeStructure((TreeStructComponent) addedState[3]);
                                                    child.processRestoreState(context, addedState[4]);
                                                    try
                                                    {
                                                        target.getChildren().add(childIndex, child);
                                                    }
                                                    catch (IndexOutOfBoundsException e)
                                                    {
                                                        // We can't be sure about where should be this 
                                                        // item, so just add it. 
                                                        target.getChildren().add(child);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                            idsAddedSet.add(clientId);
                        }
                    }
                    // Reset this list, because it will be calculated later when the view is being saved
                    // in the right order, preventing duplicates (see COMPONENT_ADDED_AFTER_BUILD_VIEW for details).
                    clientIdsAdded.clear();
                }
            }
        }
        
        // Restore binding, because UIViewRoot.processRestoreState() is never called
        //the event processing has to be enabled because of the restore view event triggers
        //TODO ask the EG the this is a spec violation if we do it that way
        //see Section 2.2.1
        // TODO: Why is necessary enable event processing?
        // ANS: On RestoreViewExecutor, setProcessingEvents is called first to false
        // and then to true when postback. Since we need listeners registered to PostAddToViewEvent
        // event to be handled, we should enable it again. We are waiting a response from EG about
        // the behavior of those listeners (see comment on vdl.buildView). 
        // -= Leonardo Uribe =- I think enable event processing in this point does not have any
        // side effect. Enable it allows programatically add components when binding is set with 
        // pss enabled. That feature works without pss, so we should preserve backward behavior.
        // Tomahawk t:aliasBean example creating components on binding requires this to work.
        //context.setProcessingEvents(true);
        //try {
        //    view.visitTree(VisitContext.createVisitContext(context), new RestoreStateCallback());
        //} finally {
        //    context.setProcessingEvents(oldContextEventState);
        //}
        return view;
    }
    
    /*
    private static void _publishPostBuildComponentTreeOnRestoreViewEvent(FacesContext context, UIComponent component)
    {
        context.getApplication().publishEvent(context, PostBuildComponentTreeOnRestoreViewEvent.class, UIComponent.class, component);
        
        if (component.getChildCount() > 0)
        {
            // PostAddToViewEvent could cause component relocation
            // (h:outputScript, h:outputStylesheet, composite:insertChildren, composite:insertFacet)
            // so we need to check if the component was relocated or not
            List<UIComponent> children = component.getChildren();
            UIComponent child = null;
            UIComponent currentChild = null;
            int i = 0;
            while (i < children.size())
            {
                child = children.get(i);
                // Iterate over the same index if the component was removed
                // This prevents skip components when processing
                do 
                {
                    _publishPostBuildComponentTreeOnRestoreViewEvent(context, child);
                    currentChild = child;
                }
                while ((i < children.size()) &&
                       ((child = children.get(i)) != currentChild) );
                i++;
            }
        }
        if (component.getFacetCount() > 0)
        {
            for (UIComponent child : component.getFacets().values())
            {
                _publishPostBuildComponentTreeOnRestoreViewEvent(context, child);
            }
        }        
    }*/

    @Override
    public Object saveView (FacesContext context)
    {
        UIViewRoot view = context.getViewRoot();
        Object states;
        
        if (view == null) {
            // Not much that can be done.
            
            return null;
        }
        
        if (view.isTransient()) {
            // Must return null immediately per spec.
            
            return null;
        }
        
        ExternalContext externalContext = context.getExternalContext();
        
        Object serializedView = externalContext.getRequestMap()
            .get(SERIALIZED_VIEW_REQUEST_ATTR);
        
        //Note on ajax case the method saveState could be called twice: once before start
        //document rendering and the other one when it is called StateManager.getViewState method.
        if (serializedView == null)
        {
                    
            // Make sure the client IDs are unique per the spec.
            
            checkIds (context, view, new HashSet<String>());
            
            // Create save state objects for every component.
            
            //view.visitTree (VisitContext.createVisitContext (context), new SaveStateVisitor (states));
            
            if (view.getAttributes().containsKey(COMPONENT_ADDED_AFTER_BUILD_VIEW))
            {
                ensureClearInitialState(view);
                states = new Object[]{
                            internalBuildTreeStructureToSave(view),
                            view.processSaveState(context)};
            }
            else
            {
                states = new HashMap<String, Object>();

                saveStateOnMap(context,(Map<String,Object>) states, view);
                
                if ( ((Map<String,Object>)states).isEmpty())
                {
                    states = null;
                }
            }
            
            // TODO: not sure the best way to handle dynamic adds/removes as mandated by the spec.
            
            // As required by ResponseStateManager, the return value is an Object array.  First
            // element is the structure object, second is the state map.
        
            serializedView = new Object[] { null, states };
            
            //externalContext.getRequestMap().put(SERIALIZED_VIEW_REQUEST_ATTR,
            //        getStateCache().encodeSerializedState(context, serializedView));

            externalContext.getRequestMap().put(SERIALIZED_VIEW_REQUEST_ATTR, serializedView);

            /*
            if (context.getApplication().getStateManager().isSavingStateInClient(context))
            {
                serializedView = new Object[] { null, states };
            }
            else
            {
                // On server side state saving, the structure field is used to save the view sequence.
                // Originally, on JspStateManagerImpl this is done in writeState method, not in saveView,
                // but note that on ajax case the state is both saved and written using StateManager.getViewState,
                // so we must save it early
                serializedView = new Object[] {Integer.toString(helper.getNextViewSequence(context), Character.MAX_RADIX), states};
            }
            externalContext.getRequestMap().put(DefaultFaceletsStateManagementHelper.SERIALIZED_VIEW_REQUEST_ATTR,
                    serializedView);
            */
        }
        
        //if (!context.getApplication().getStateManager().isSavingStateInClient(context))
        //{
        //getStateCache().saveSerializedView(context, serializedView);
        //}
        
        return serializedView;
    }
    
    private void restoreStateFromMap(final FacesContext context, final Map<String,Object> states,
            final UIComponent component)
    {
        if (states == null)
        {
            return;
        }
        
        try
        {
            //Restore view
            component.pushComponentToEL(context, component);
            Object state = states.get(component.getClientId());
            if (state != null)
            {
                if (state instanceof AttachedFullStateWrapper)
                {
                    //Don't restore this one! It will be restored when the algorithm remove and add it.
                    return;
                }
                try
                {
                    component.restoreState(context, state);
                }
                catch(Exception e)
                {
                    throw new IllegalStateException("Error restoring component: "+component.getClientId(), e);
                }
            }
    
            //Scan children
            if (component.getChildCount() > 0)
            {
                //String currentClientId = component.getClientId();
                
                List<UIComponent> children  = component.getChildren();
                for (int i = 0; i < children.size(); i++)
                {
                    UIComponent child = children.get(i);
                    if (child != null && !child.isTransient())
                    {
                        restoreStateFromMap( context, states, child);
                    }
                }
            }
    
            //Scan facets
            Map<String, UIComponent> facetMap = component.getFacets();
            if (!facetMap.isEmpty())
            {
                //String currentClientId = component.getClientId();
                
                for (Map.Entry<String, UIComponent> entry : facetMap.entrySet())
                {
                    UIComponent child = entry.getValue();
                    if (child != null && !child.isTransient())
                    {
                        //String facetName = entry.getKey();
                        restoreStateFromMap( context, states, child);
                    }
                }
            }
        }
        finally
        {
            component.popComponentFromEL(context);
        }
    }
    
    static List<String> getClientIdsAdded(UIViewRoot root)
    {
        return (List<String>) root.getAttributes().get(CLIENTIDS_ADDED);
    }
    
    static void setClientsIdsAdded(UIViewRoot root, List<String> clientIdsList)
    {
        root.getAttributes().put(CLIENTIDS_ADDED, clientIdsList);
    }
    
    static List<String> getClientIdsRemoved(UIViewRoot root)
    {
        return (List<String>) root.getAttributes().get(CLIENTIDS_REMOVED);
    }
    
    static void setClientsIdsRemoved(UIViewRoot root, List<String> clientIdsList)
    {
        root.getAttributes().put(CLIENTIDS_REMOVED, clientIdsList);
    }
    
    @SuppressWarnings("unchecked")
    private void registerOnAddRemoveList(String clientId)
    {
        UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();

        List<String> clientIdsAdded = (List<String>) getClientIdsAdded(uiViewRoot);
        if (clientIdsAdded == null)
        {
            //Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<String>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);

        List<String> clientIdsRemoved = (List<String>) getClientIdsRemoved(uiViewRoot);
        if (clientIdsRemoved == null)
        {
            //Create a set that preserve insertion order
            clientIdsRemoved = new ArrayList<String>();
        }

        clientIdsRemoved.add(clientId);

        setClientsIdsRemoved(uiViewRoot, clientIdsRemoved);
    }
    
    @SuppressWarnings("unchecked")
    private void registerOnAddList(String clientId)
    {
        UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();

        List<String> clientIdsAdded = (List<String>) getClientIdsAdded(uiViewRoot);
        if (clientIdsAdded == null)
        {
            //Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<String>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);
    }
            
    private void saveStateOnMap(final FacesContext context, final Map<String,Object> states,
            final UIComponent component)
    {
        ComponentState componentAddedAfterBuildView = null;
        try
        {
            component.pushComponentToEL(context, component);
            
            //Scan children
            if (component.getChildCount() > 0)
            {
                String currentClientId = component.getClientId();
                
                List<UIComponent> children  = component.getChildren();
                for (int i = 0; i < children.size(); i++)
                {
                    UIComponent child = children.get(i);
                    if (child != null && !child.isTransient())
                    {
                        componentAddedAfterBuildView = (ComponentState) child.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);
                        if (componentAddedAfterBuildView != null)
                        {
                            if (ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddRemoveList(child.getClientId());
                                child.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                            }
                            else if (ComponentState.ADD.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddList(child.getClientId());
                                child.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                            }
                            else if (ComponentState.ADDED.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddList(child.getClientId());
                            }
                            ensureClearInitialState(child);
                            //Save all required info to restore the subtree.
                            //This includes position, structure and state of subtree
                            states.put(child.getClientId(), new AttachedFullStateWrapper( 
                                    new Object[]{
                                        currentClientId,
                                        null,
                                        i,
                                        internalBuildTreeStructureToSave(child),
                                        child.processSaveState(context)}));
                        }
                        else
                        {
                            saveStateOnMap( context, states, child);
                        }
                    }
                }
            }
    
            //Scan facets
            Map<String, UIComponent> facetMap = component.getFacets();
            if (!facetMap.isEmpty())
            {
                String currentClientId = component.getClientId();
                
                for (Map.Entry<String, UIComponent> entry : facetMap.entrySet())
                {
                    UIComponent child = entry.getValue();
                    if (child != null && !child.isTransient())
                    {
                        String facetName = entry.getKey();
                        componentAddedAfterBuildView = (ComponentState) child.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);
                        if (componentAddedAfterBuildView != null)
                        {
                            if (ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddRemoveList(child.getClientId());
                                child.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                            }
                            else if (ComponentState.ADD.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddList(child.getClientId());
                                child.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                            }
                            else if (ComponentState.ADDED.equals(componentAddedAfterBuildView))
                            {
                                registerOnAddList(child.getClientId());
                            }
                            //Save all required info to restore the subtree.
                            //This includes position, structure and state of subtree
                            ensureClearInitialState(child);
                            states.put(child.getClientId(),new AttachedFullStateWrapper(new Object[]{
                                currentClientId,
                                facetName,
                                null,
                                internalBuildTreeStructureToSave(child),
                                child.processSaveState(context)}));
                        }
                        else
                        {
                            saveStateOnMap( context, states, child);
                        }
                    }
                }
            }
            
            //Save state        
            Object savedState = component.saveState(context);
            
            //Only save if the value returned is null
            if (savedState != null)
            {
                states.put(component.getClientId(), savedState);            
            }
        }
        finally
        {
            component.popComponentFromEL(context);
        }
    }
    
    protected void ensureClearInitialState(UIComponent c)
    {
        c.clearInitialState();
        if (c.getChildCount() > 0)
        {
            for (UIComponent child : c.getChildren())
            {
                ensureClearInitialState(child);
            }
        }
        if (c.getFacetCount() > 0)
        {
            for (UIComponent child : c.getFacets().values())
            {
                ensureClearInitialState(child);
            }
        }
    }
    
    public void suscribeListeners(UIViewRoot uiViewRoot)
    {
        PostAddPreRemoveFromViewListener componentListener = new PostAddPreRemoveFromViewListener();
        uiViewRoot.subscribeToViewEvent(PostAddToViewEvent.class, componentListener);
        uiViewRoot.subscribeToViewEvent(PreRemoveFromViewEvent.class, componentListener);
    }
    
    private void checkIds (FacesContext context, UIComponent component, Set<String> existingIds)
    {
        String id;
        Iterator<UIComponent> children;
        
        if (component == null) {
            return;
        }
        
        // Need to use this form of the client ID method so we generate the client-side ID.
        
        id = component.getClientId (context);
        
        if (existingIds.contains (id)) {
            throw new IllegalStateException ("component with duplicate id \"" + id + "\" found");
        }
        
        existingIds.add (id);
        
        children = component.getFacetsAndChildren();
        
        while (children.hasNext()) {
            checkIds (context, children.next(), existingIds);
        }
    }
    
    protected RenderKitFactory getRenderKitFactory()
    {
        if (_renderKitFactory == null)
        {
            _renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return _renderKitFactory;
    }
    
    /*
    private static class RestoreStateCallback implements VisitCallback
    {
        private PostRestoreStateEvent event;

        public VisitResult visit(VisitContext context, UIComponent target)
        {
            if (event == null)
            {
                event = new PostRestoreStateEvent(target);
            }
            else
            {
                event.setComponent(target);
            }

            // call the processEvent method of the current component.
            // The argument event must be an instance of AfterRestoreStateEvent whose component
            // property is the current component in the traversal.
            target.processEvent(event);
            
            return VisitResult.ACCEPT;
        }
    }*/
    
    /*
    private class RestoreStateVisitor implements VisitCallback {
        private Map<String, Object> states;
        
        private RestoreStateVisitor (Map<String, Object> states)
        {
            this.states = states;
        }
        
        @Override
        public VisitResult visit (VisitContext context, UIComponent target)
        {
            FacesContext facesContext = context.getFacesContext();
            Object state = states.get (target.getClientId (facesContext));
            
            if (state != null) {
                target.restoreState (facesContext, state);
            }
            
            return VisitResult.ACCEPT;
        }
    }
    
    private class SaveStateVisitor implements VisitCallback {
        private Map<String, Object> states;
        
        private SaveStateVisitor (Map<String, Object> states)
        {
            this.states = states;
        }
        
        @Override
        public VisitResult visit (VisitContext context, UIComponent target)
        {
            FacesContext facesContext = context.getFacesContext();
            Object state;
            
            if ((target == null) || target.isTransient()) {
                // No need to bother with these components or their children.
                
                return VisitResult.REJECT;
            }
            
            state = target.saveState (facesContext);
            
            if (state != null) {
                // Save by client ID into our map.
                
                states.put (target.getClientId (facesContext), state);
            }
            
            return VisitResult.ACCEPT;
        }
    }
    */
    
    public static class PostAddPreRemoveFromViewListener implements SystemEventListener
    {

        public boolean isListenerForSource(Object source)
        {
            // PostAddToViewEvent and PreRemoveFromViewEvent are
            // called from UIComponentBase.setParent
            return (source instanceof UIComponent);
        }

        public void processEvent(SystemEvent event)
        {
            UIComponent component = (UIComponent) event.getSource();
            
            if (component.isTransient())
            {
                return;
            }
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (FaceletViewDeclarationLanguage.isRefreshingTransientBuild(facesContext))
            {
                return;
            }
            
            if (event instanceof PostAddToViewEvent)
            {
                //PostAddToViewEvent
                component.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADD);
            }
            else
            {
                // In this case if we are removing components on build, it is not necessary to register
                // again the current id, and its more, it could cause a concurrent exception. But note
                // we need to propagate PreRemoveFromViewEvent, otherwise the view will not be restored
                // correctly.
                if (FaceletViewDeclarationLanguage.isRemovingComponentBuild(facesContext))
                {
                    return;
                }
                
                //PreRemoveFromViewEvent
                UIViewRoot uiViewRoot = facesContext.getViewRoot();
                
                List<String> clientIdsRemoved = getClientIdsRemoved(uiViewRoot);
                if (clientIdsRemoved == null)
                {
                    //Create a set that preserve insertion order
                    clientIdsRemoved = new ArrayList<String>();
                }
                clientIdsRemoved.add(component.getClientId());
                setClientsIdsRemoved(uiViewRoot, clientIdsRemoved);
            }
        }
    }
    
    private TreeStructComponent internalBuildTreeStructureToSave(UIComponent component)
    {
        TreeStructComponent structComp = new TreeStructComponent(component.getClass().getName(),
                                                                 component.getId());

        //children
        if (component.getChildCount() > 0)
        {
            List<TreeStructComponent> structChildList = new ArrayList<TreeStructComponent>();
            for (UIComponent child : component.getChildren())
            {
                if (!child.isTransient())
                {
                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child);
                    structChildList.add(structChild);
                }
            }
            
            TreeStructComponent[] childArray = structChildList.toArray(new TreeStructComponent[structChildList.size()]);
            structComp.setChildren(childArray);
        }

        //facets
        Map<String, UIComponent> facetMap = component.getFacets();
        if (!facetMap.isEmpty())
        {
            List<Object[]> structFacetList = new ArrayList<Object[]>();
            for (Map.Entry<String, UIComponent> entry : facetMap.entrySet())
            {
                UIComponent child = entry.getValue();
                if (!child.isTransient())
                {
                    String facetName = entry.getKey();
                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child);
                    structFacetList.add(new Object[] {facetName, structChild});
                }
            }
            
            Object[] facetArray = structFacetList.toArray(new Object[structFacetList.size()]);
            structComp.setFacets(facetArray);
        }

        return structComp;
    }
    
    private UIComponent internalRestoreTreeStructure(TreeStructComponent treeStructComp)
    {
        String compClass = treeStructComp.getComponentClass();
        String compId = treeStructComp.getComponentId();
        UIComponent component = (UIComponent)ClassUtils.newInstance(compClass);
        component.setId(compId);

        //children
        TreeStructComponent[] childArray = treeStructComp.getChildren();
        if (childArray != null)
        {
            List<UIComponent> childList = component.getChildren();
            for (int i = 0, len = childArray.length; i < len; i++)
            {
                UIComponent child = internalRestoreTreeStructure(childArray[i]);
                childList.add(child);
            }
        }

        //facets
        Object[] facetArray = treeStructComp.getFacets();
        if (facetArray != null)
        {
            Map<String, UIComponent> facetMap = component.getFacets();
            for (int i = 0, len = facetArray.length; i < len; i++)
            {
                Object[] tuple = (Object[])facetArray[i];
                String facetName = (String)tuple[0];
                TreeStructComponent structChild = (TreeStructComponent)tuple[1];
                UIComponent child = internalRestoreTreeStructure(structChild);
                facetMap.put(facetName, child);
            }
        }

        return component;
    }

    public static class TreeStructComponent implements Serializable
    {
        private static final long serialVersionUID = 5069109074684737231L;
        private String _componentClass;
        private String _componentId;
        private TreeStructComponent[] _children = null; // Array of children
        private Object[] _facets = null; // Array of Array-tuples with Facetname and TreeStructComponent

        TreeStructComponent(String componentClass, String componentId)
        {
            _componentClass = componentClass;
            _componentId = componentId;
        }

        public String getComponentClass()
        {
            return _componentClass;
        }

        public String getComponentId()
        {
            return _componentId;
        }

        void setChildren(TreeStructComponent[] children)
        {
            _children = children;
        }

        TreeStructComponent[] getChildren()
        {
            return _children;
        }

        Object[] getFacets()
        {
            return _facets;
        }

        void setFacets(Object[] facets)
        {
            _facets = facets;
        }
    }
    
}
