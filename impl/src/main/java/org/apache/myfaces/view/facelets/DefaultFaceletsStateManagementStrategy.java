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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitContextFactory;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRemoveFromViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import jakarta.faces.view.ViewMetadata;
import org.apache.myfaces.application.StateManagerImpl;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.util.lang.HashMapUtils;
import org.apache.myfaces.component.visit.MyFacesVisitHints;
import org.apache.myfaces.view.facelets.compiler.CheckDuplicateIdFaceletUtils;
import org.apache.myfaces.view.facelets.pool.ViewEntry;
import org.apache.myfaces.view.facelets.pool.ViewPool;
import org.apache.myfaces.view.facelets.pool.ViewStructureMetadata;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.jsf.FaceletState;

/**
 * This class implements partial state saving feature when facelets
 * is used to render pages. (Theorically it could be applied on jsp case too,
 * but all considerations below should be true before apply it).
 * 
 * The following considerations apply for this class:
 * 
 * 1. This StateManagementStrategy should only be active if jakarta.faces.PARTIAL_STATE_SAVING
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
 * 6. It is not possible to use jakarta.faces.component.visit API to traverse the component
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
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
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


    private static final Object[] EMPTY_STATES = new Object[]{null, null};

    private static final String UNIQUE_ID_COUNTER_KEY =
              "oam.view.uniqueIdCounter";
    
    private ViewDeclarationLanguageFactory _vdlFactory;
    
    private RenderKitFactory _renderKitFactory = null;
    
    private VisitContextFactory _visitContextFactory = null;
    
    private String checkIdsProductionMode;

    private ViewPoolProcessor _viewPoolProcessor;
    
    public DefaultFaceletsStateManagementStrategy()
    {
        this(FacesContext.getCurrentInstance());
    }
    
    public DefaultFaceletsStateManagementStrategy(FacesContext context)
    {
        _vdlFactory = (ViewDeclarationLanguageFactory)
                FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
        _viewPoolProcessor = ViewPoolProcessor.getInstance(context);
        checkIdsProductionMode = MyfacesConfig.getCurrentInstance(context).getCheckIdProductionMode();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public UIViewRoot restoreView (FacesContext context, String viewId, String renderKitId)
    {
        Map<String, Object> states;
        UIViewRoot view = null;
        
        ResponseStateManager manager =
                getRenderKitFactory().getRenderKit(context, renderKitId).getResponseStateManager();

        // The value returned here is expected to be false (set by RestoreViewExecutor), but
        //we don't know if some ViewHandler wrapper could change it, so it is better to save the value.
        final boolean oldContextEventState = context.isProcessingEvents();
        
        // Get previous state from ResponseStateManager.
        Object[] state = (Object[]) manager.getState(context, viewId);
        if (state == null)
        {
            //No state could be restored, return null causing ViewExpiredException
            return null;
        }
        
        if (state[1] instanceof Object[])
        {
            Object[] fullState = (Object[]) state[1]; 
            view = (UIViewRoot) internalRestoreTreeStructure((TreeStructComponent)fullState[0]);

            if (view != null)
            {
                context.setViewRoot (view);
                view.processRestoreState(context, fullState[1]);
                
                // If the view is restored fully, it is necessary to refresh RequestViewContext, otherwise at
                // each ajax request new components associated with @ResourceDependency annotation will be added
                // to the tree, making the state bigger without real need.
                RequestViewContext.getCurrentInstance(context).
                        refreshRequestViewContext(context, view);
                
                if (fullState.length == 3 && fullState[2] != null)
                {
                    context.setResourceLibraryContracts((List) UIComponentBase.
                        restoreAttachedState(context, fullState[2]));
                }
            }
        }
        else
        {
            // Per the spec: build the view.
            ViewDeclarationLanguage vdl = _vdlFactory.getViewDeclarationLanguage(viewId);
            Object faceletViewState = null;
            try
            {
                ViewMetadata metadata = vdl.getViewMetadata (context, viewId);
                
                if (metadata != null)
                {
                    view = metadata.createMetadataView(context);
                    
                    // If no view and response complete there is no need to continue
                    if (view == null && context.getResponseComplete())
                    {
                        return null;
                    }
                }
                if (view == null)
                {
                    view = context.getApplication().getViewHandler().createView(context, viewId);
                }
                
                context.setViewRoot(view); 
                boolean skipBuildView = false;
                if (state[1] != null)
                {
                    // Since JSF 2.2, UIViewRoot.restoreViewScopeState() must be called, but
                    // to get the state of the root, it is necessary to force calculate the
                    // id from this location. Remember in this point, PSS is enabled, so the
                    // code match with the assigment done in 
                    // FaceletViewDeclarationLanguage.buildView()
                    states = (Map<String, Object>) state[1];
                    faceletViewState = UIComponentBase.restoreAttachedState(
                            context,states.get(ComponentSupport.FACELET_STATE_INSTANCE));
                    if (faceletViewState != null && _viewPoolProcessor != null)
                    {
                        ViewPool viewPool = _viewPoolProcessor.getViewPool(context, view);
                        if (viewPool != null)
                        {
                            ViewStructureMetadata viewMetadata = viewPool.retrieveDynamicViewStructureMetadata(
                                context, view, (FaceletState) faceletViewState);
                            if (viewMetadata != null)
                            {
                                ViewEntry entry = viewPool.popDynamicStructureView(context, view,
                                        (FaceletState) faceletViewState);
                                if (entry != null)
                                {
                                    skipBuildView = true;
                                    _viewPoolProcessor.cloneAndRestoreView(context, view, entry, viewMetadata);
                                }
                            }
                        }
                    }
                    if (view.getId() == null)
                    {
                        view.setId(view.createUniqueId(context, null));
                    }
                    if (faceletViewState != null)
                    {
                        FaceletState newFaceletState = (FaceletState) view.getAttributes().get(
                                ComponentSupport.FACELET_STATE_INSTANCE);
                        if (newFaceletState != null)
                        {
                            newFaceletState.restoreState(context, 
                                    ((FaceletState)faceletViewState).saveState(context));
                            faceletViewState = newFaceletState;
                        }
                        else
                        {
                            view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE,  faceletViewState);
                        }
                    }
                    if (state.length == 3)
                    {
                        //Jump to where the count is
                        view.getAttributes().put(UNIQUE_ID_COUNTER_KEY, state[2]);
                    }
                    Object viewRootState = states.get(view.getClientId(context));
                    if (viewRootState != null)
                    {
                        try
                        {
                            view.pushComponentToEL(context, view);
                            view.restoreViewScopeState(context, viewRootState);
                        }
                        finally
                        {
                            view.popComponentFromEL(context);
                        }
                    }
                }
                // On RestoreViewExecutor, setProcessingEvents is called first to false
                // and then to true when postback. Since we need listeners registered to PostAddToViewEvent
                // event to be handled, we should enable it again. For partial state saving we need this listeners
                // be called from here and relocate components properly.
                if (!skipBuildView)
                {
                    try 
                    {
                        context.setProcessingEvents (true);
                        vdl.buildView(context, view);
                        // In the latest code related to PostAddToView, it is
                        // triggered no matter if it is applied on postback. It seems that MYFACES-2389, 
                        // TRINIDAD-1670 and TRINIDAD-1671 are related.
                        suscribeListeners(view);
                    }
                    finally
                    {
                        context.setProcessingEvents (oldContextEventState);
                    }
                }
            }
            catch (Throwable e)
            {
                throw new FacesException ("unable to create view \"" + viewId + '"', e);
            }

            // Stateless mode only for transient views and non stateless mode for
            // stateful views. This check avoid apply state over a stateless view.
            boolean statelessMode = manager.isStateless(context, viewId);
            if (statelessMode && !view.isTransient())
            {
                throw new IllegalStateException("View is not transient");
            }
            if (!statelessMode && view.isTransient())
            {
                throw new IllegalStateException("Cannot apply state over stateless view");
            }
            
            if (state[1] != null)
            {
                states = (Map<String, Object>) state[1];
                //Save the last unique id counter key in UIViewRoot
                Integer lastUniqueIdCounter = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
                // Retrieve the facelet state before restore anything. The reason is
                // it could be necessary to restore the bindings map from here.
                FaceletState oldFaceletState = (FaceletState) view.getAttributes().get(
                        ComponentSupport.FACELET_STATE_INSTANCE);
                
                // Visit the children and restore their state.
                boolean emptyState = false;
                boolean containsFaceletState = states.containsKey(ComponentSupport.FACELET_STATE_INSTANCE);
                if (states.isEmpty())
                {
                    emptyState = true; 
                }
                else if (states.size() == 1 && containsFaceletState)
                {
                    emptyState = true; 
                }

                //Restore state of current components
                if (!emptyState)
                {
                    // Check if there is only one component state
                    // and that state is UIViewRoot instance (for example when using ViewScope)
                    if ((states.size() == 1 && !containsFaceletState)
                            || (states.size() == 2 && containsFaceletState))
                    {
                        Object viewState = states.get(view.getClientId(context));
                        if (viewState != null)
                        {
                            restoreViewRootOnlyFromMap(context,viewState, view);
                        }
                        else
                        {
                            //The component is not viewRoot, restore as usual.
                            restoreStateFromMap(context, states, view);
                        }
                    }
                    else
                    {
                        restoreStateFromMap(context, states, view);
                    }
                }
                if (faceletViewState != null)
                {
                    // Make sure binding map
                    if (oldFaceletState != null && oldFaceletState.getBindings() != null
                            && !oldFaceletState.getBindings().isEmpty())
                    {
                        // Be sure the new facelet state has the binding map filled from the old one.
                        // When vdl.buildView() is called by restoreView, FaceletState.bindings map is filled, but
                        // when view pool is enabled, vdl.buildView() could restore the view, but create an alternate
                        // FaceletState instance, different from the one restored. In this case, the restored instance
                        // has precedence, but we need to fill bindings map using the entries from the instance that
                        // comes from the view pool.
                        FaceletState newFaceletState = (FaceletState) faceletViewState;
                        for (Map.Entry<String, Map<String, ValueExpression>> entry : 
                                oldFaceletState.getBindings().entrySet())
                        {
                            for (Map.Entry<String, ValueExpression> entry2 : entry.getValue().entrySet())
                            {
                                ValueExpression expr = newFaceletState.getBinding(entry.getKey(), entry2.getKey());
                                if (expr == null)
                                {
                                    newFaceletState.putBinding(entry.getKey(), entry2.getKey(), entry2.getValue());
                                }
                            }
                        }
                        view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE,  newFaceletState);
                    }
                    else
                    {
                        //restore bindings
                        view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE,  faceletViewState);
                    }
                }
                if (lastUniqueIdCounter != null)
                {
                    Integer newUniqueIdCounter = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
                    if (newUniqueIdCounter != null && lastUniqueIdCounter > newUniqueIdCounter)
                    {
                        // The unique counter was restored by a side effect of 
                        // restoreState() over UIViewRoot with a lower count,
                        // to avoid a component duplicate id exception we need to fix the count.
                        view.getAttributes().put(UNIQUE_ID_COUNTER_KEY, lastUniqueIdCounter);
                    }
                }
                handleDynamicAddedRemovedComponents(context, view, states);
            }
        }
        return view;
    }
    
    public void handleDynamicAddedRemovedComponents(FacesContext context, UIViewRoot view, Map<String, Object> states)
    {
        List<String> clientIdsRemoved = getClientIdsRemoved(view);
        if (clientIdsRemoved != null)
        {
            Set<String> idsRemovedSet = new HashSet<>(HashMapUtils.calcCapacity(clientIdsRemoved.size()));
            context.getAttributes().put(FaceletViewDeclarationLanguage.REMOVING_COMPONENTS_BUILD, Boolean.TRUE);
            try
            {
                RemoveComponentCallback removeCallback = null;
                
                // perf: clientIds are ArrayList: see method registerOnAddRemoveList(String)
                for (int i = 0, size = clientIdsRemoved.size(); i < size; i++)
                {
                    String clientId = clientIdsRemoved.get(i);
                    if (!idsRemovedSet.contains(clientId))
                    {
                        if (removeCallback == null)
                        {
                            removeCallback = new RemoveComponentCallback();
                        }
                        removeCallback.setComponentFound(false);

                        view.invokeOnComponent(context, clientId, removeCallback);
                        if (removeCallback.isComponentFound())
                        {
                            //Add only if component found
                            idsRemovedSet.add(clientId);
                        }
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
            if (!clientIdsAdded.isEmpty())
            {
                Set<String> idsAddedSet = new HashSet<>(HashMapUtils.calcCapacity(clientIdsAdded.size()));
                AddComponentCallback addCallback = null;

                // perf: clientIds are ArrayList: see method setClientsIdsAdded(String)
                for (int i = 0, size = clientIdsAdded.size(); i < size; i++)
                {
                    String clientId = clientIdsAdded.get(i);
                    if (idsAddedSet.contains(clientId))
                    {
                        continue;
                    }

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
                                if (addCallback == null)
                                {
                                    addCallback = new AddComponentCallback();
                                }
                                addCallback.setAddedState(addedState);

                                final String parentClientId = (String) addedState[0];
                                view.invokeOnComponent(context, parentClientId, addCallback);
                            }
                        }
                    }
                    idsAddedSet.add(clientId);
                }
            }

            // Reset this list, because it will be calculated later when the view is being saved
            // in the right order, preventing duplicates (see COMPONENT_ADDED_AFTER_BUILD_VIEW for details).
            clientIdsAdded.clear();
            
            // This call only has sense when components has been added programatically, because if facelets has control
            // over all components in the component tree, build the initial state and apply the state will have the
            // same effect.
            RequestViewContext.getCurrentInstance(context).refreshRequestViewContext(context, view);
        }
    }

    public static class RemoveComponentCallback implements ContextCallback
    {
        private boolean componentFound;
        
        public RemoveComponentCallback()
        {
            this.componentFound = false;
        }
        
        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (target.getParent() != null && !target.getParent().getChildren().remove(target))
            {
                String key = null;
                if (target.getParent().getFacetCount() > 0)
                {
                    for (Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet())
                    {
                        if (entry.getValue() == target)
                        {
                            key = entry.getKey();
                            break;
                        }
                    }
                }
                if (key != null)
                {
                    UIComponent removedTarget = target.getParent().getFacets().remove(key);
                    if (removedTarget != null)
                    {
                        this.componentFound = true;
                    }
                }
            }
            else
            {
                this.componentFound = true;
            }
        }
        
        public boolean isComponentFound()
        {
            return componentFound;
        }

        public void setComponentFound(boolean componentFound)
        {
            this.componentFound = componentFound;
        }
    }

    public static class AddComponentCallback implements ContextCallback
    {
        private Object[] addedState;
        
        public AddComponentCallback()
        {
        }
        
        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
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
                UIComponent child = internalRestoreTreeStructure((TreeStructComponent)addedState[3]);
                child.processRestoreState(context, addedState[4]);
                
                boolean done = false;
                // Is the child a facelet controlled component?
                if (child.getAttributes().containsKey(ComponentSupport.MARK_CREATED))
                {
                    // By effect of c:forEach it is possible that the component can be duplicated
                    // in the component tree, so what we need to do as a fallback is replace the
                    // component in the spot with the restored version.
                    UIComponent parent = target;
                    if (parent.getChildCount() > 0)
                    {
                        String tagId = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                        if (childIndex < parent.getChildCount())
                        {
                            // Try to find the component quickly 
                            UIComponent dup = parent.getChildren().get(childIndex);
                            if (tagId.equals(dup.getAttributes().get(ComponentSupport.MARK_CREATED)))
                            {
                                // Replace
                                parent.getChildren().remove(childIndex.intValue());
                                parent.getChildren().add(childIndex, child);
                                done = true;
                            }
                        }
                        if (!done)
                        {
                            // Fallback to iteration
                            for (int i = 0, childCount = parent.getChildCount(); i < childCount; i ++)
                            {
                                UIComponent dup = parent.getChildren().get(i);
                                if (tagId.equals(dup.getAttributes().get(ComponentSupport.MARK_CREATED)))
                                {
                                    // Replace
                                    parent.getChildren().remove(i);
                                    parent.getChildren().add(i, child);
                                    done = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!done)
                {
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
        }

        public Object[] getAddedState()
        {
            return addedState;
        }

        public void setAddedState(Object[] addedState)
        {
            this.addedState = addedState;
        }
    }

    @Override
    public Object saveView(FacesContext context)
    {
        UIViewRoot view = context.getViewRoot();
        Object states;
        
        if (view == null)
        {
            // Not much that can be done.
            return null;
        }
        
        Object serializedView = context.getAttributes().get(StateManagerImpl.SERIALIZED_VIEW_REQUEST_ATTR);
        
        //Note on ajax case the method saveState could be called twice: once before start
        //document rendering and the other one when it is called StateManager.getViewState method.
        if (serializedView == null)
        {
            // Make sure the client IDs are unique per the spec.
            if (context.isProjectStage(ProjectStage.Production))
            {
                if (MyfacesConfig.CHECK_ID_PRODUCTION_MODE_AUTO.equals(checkIdsProductionMode))
                {
                    CheckDuplicateIdFaceletUtils.checkIdsStatefulComponents(context, view);
                }
                else if (MyfacesConfig.CHECK_ID_PRODUCTION_MODE_TRUE.equals(checkIdsProductionMode))
                {
                    CheckDuplicateIdFaceletUtils.checkIds(context, view);
                }
            }
            else
            {
                CheckDuplicateIdFaceletUtils.checkIds(context, view);
            }
            
            // Create save state objects for every component.
            
            boolean viewResetable = false;
            int count = 0;
            Object faceletViewState = null;
            boolean saveViewFully = view.getAttributes().containsKey(COMPONENT_ADDED_AFTER_BUILD_VIEW);
            if (saveViewFully)
            {
                ensureClearInitialState(view);
                Object rlcStates = !context.getResourceLibraryContracts().isEmpty() ? 
                    UIComponentBase.saveAttachedState(context, 
                                new ArrayList<>(context.getResourceLibraryContracts())) : null;
                states = new Object[]{
                            internalBuildTreeStructureToSave(view),
                            view.processSaveState(context), rlcStates};
            }
            else
            {
                states = new HashMap<>();

                faceletViewState = view.getAttributes().get(ComponentSupport.FACELET_STATE_INSTANCE);
                if (faceletViewState != null)
                {
                    ((Map<String, Object>) states).put(ComponentSupport.FACELET_STATE_INSTANCE,
                            UIComponentBase.saveAttachedState(context, faceletViewState));
                    //Do not save on UIViewRoot
                    view.getAttributes().remove(ComponentSupport.FACELET_STATE_INSTANCE);
                    view.getTransientStateHelper().putTransient(
                            ComponentSupport.FACELET_STATE_INSTANCE, faceletViewState);
                }
                if (_viewPoolProcessor != null
                        && _viewPoolProcessor.isViewPoolEnabledForThisView(context, view))
                {
                    SaveStateAndResetViewCallback cb = saveStateOnMapVisitTreeAndReset(
                            context,
                            (Map<String,Object>) states,
                            view,
                            Boolean.TRUE.equals(context.getAttributes().get(ViewPoolProcessor.FORCE_HARD_RESET)));
                    viewResetable = cb.isViewResetable();
                    count = cb.getCount();
                }
                else
                {
                    saveStateOnMapVisitTree(context,(Map<String,Object>) states, view);
                }
                
                if (((Map<String,Object>) states).isEmpty())
                {
                    states = null;
                }
            }
            
            Integer uniqueIdCount = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
            if (uniqueIdCount != null && !uniqueIdCount.equals(1))
            {
                serializedView = new Object[] { null, states, uniqueIdCount };
            }
            else if (states == null)
            {
                serializedView = EMPTY_STATES;
            }
            else
            {
                serializedView = new Object[] { null, states };
            }
            
            //If view cache enabled store the view state into the pool
            if (!saveViewFully && _viewPoolProcessor != null)
            {
                if (viewResetable)
                {
                    _viewPoolProcessor.pushResetableView(context, view, (FaceletState) faceletViewState);
                }
                else
                {
                    _viewPoolProcessor.pushPartialView(context, view, (FaceletState) faceletViewState, count);
                }
            }
            
            context.getAttributes().put(StateManagerImpl.SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
        }
        
        return serializedView;
    }
    
    private void restoreViewRootOnlyFromMap(final FacesContext context, final Object viewState, final UIComponent view)
    {
        // Only viewState found, process it but skip tree
        // traversal, saving some time.
        try
        {
            //Restore view
            view.pushComponentToEL(context, view);
            if (viewState != null && !(viewState instanceof AttachedFullStateWrapper))
            {
                try
                {
                    view.restoreState(context, viewState);
                }
                catch(Exception e)
                {
                    throw new IllegalStateException(
                            "Error restoring component: " + view.getClientId(context), e);
                }
            }
        }
        finally
        {
             view.popComponentFromEL(context);
        }
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
            Object state = states.get(component.getClientId(context));
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
                    throw new IllegalStateException(
                            "Error restoring component: " + component.getClientId(context), e);
                }
            }
    
            //Scan children
            if (component.getChildCount() > 0)
            {                
                List<UIComponent> children  = component.getChildren();
                for (int i = 0; i < children.size(); i++)
                {
                    UIComponent child = children.get(i);
                    if (child != null && !child.isTransient())
                    {
                        restoreStateFromMap(context, states, child);
                    }
                }
            }
    
            //Scan facets
            if (component.getFacetCount() > 0)
            {
                Map<String, UIComponent> facetMap = component.getFacets();
                for (Map.Entry<String, UIComponent> entry : facetMap.entrySet())
                {
                    UIComponent child = entry.getValue();
                    if (child != null && !child.isTransient())
                    {
                        restoreStateFromMap(context, states, child);
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
    private void registerOnAddRemoveList(FacesContext facesContext, String clientId)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        List<String> clientIdsAdded = (List<String>) getClientIdsAdded(uiViewRoot);
        if (clientIdsAdded == null)
        {
            //Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);

        List<String> clientIdsRemoved = (List<String>) getClientIdsRemoved(uiViewRoot);
        if (clientIdsRemoved == null)
        {
            //Create a set that preserve insertion order
            clientIdsRemoved = new ArrayList<>();
        }

        clientIdsRemoved.add(clientId);

        setClientsIdsRemoved(uiViewRoot, clientIdsRemoved);
    }
    
    @SuppressWarnings("unchecked")
    private void registerOnAddList(FacesContext facesContext, String clientId)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        List<String> clientIdsAdded = (List<String>) getClientIdsAdded(uiViewRoot);
        if (clientIdsAdded == null)
        {
            //Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);
    }

    private void saveStateOnMapVisitTree(final FacesContext facesContext, final Map<String,Object> states,
            final UIViewRoot uiViewRoot)
    {
        facesContext.getAttributes().put(MyFacesVisitHints.SKIP_ITERATION_HINT, Boolean.TRUE);
        try
        {
            uiViewRoot.visitTree(getVisitContextFactory().getVisitContext(facesContext, null,
                    MyFacesVisitHints.SET_SKIP_ITERATION), new VisitCallback()
            {
                @Override
                public VisitResult visit(VisitContext context, UIComponent target)
                {
                    FacesContext facesContext = context.getFacesContext();
                    Object state;
                    
                    if ((target == null) || target.isTransient())
                    {
                        // No need to bother with these components or their children.
                        
                        return VisitResult.REJECT;
                    }
                    
                    ComponentState componentAddedAfterBuildView
                            = (ComponentState) target.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);
                    
                    //Note if UIViewRoot has this marker, JSF 1.2 like state saving is used.
                    if (componentAddedAfterBuildView != null && (target.getParent() != null))
                    {
                        if (ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView))
                        {
                            registerOnAddRemoveList(facesContext, target.getClientId(facesContext));
                            target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                        }
                        else if (ComponentState.ADD.equals(componentAddedAfterBuildView))
                        {
                            registerOnAddList(facesContext, target.getClientId(facesContext));
                            target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                        }
                        else if (ComponentState.ADDED.equals(componentAddedAfterBuildView))
                        {
                            registerOnAddList(facesContext, target.getClientId(facesContext));
                        }
                        ensureClearInitialState(target);
                        //Save all required info to restore the subtree.
                        //This includes position, structure and state of subtree
                        
                        int childIndex = target.getParent().getChildren().indexOf(target);
                        if (childIndex >= 0)
                        {
                            states.put(target.getClientId(facesContext), new AttachedFullStateWrapper( 
                                    new Object[]{
                                        target.getParent().getClientId(facesContext),
                                        null,
                                        childIndex,
                                        internalBuildTreeStructureToSave(target),
                                        target.processSaveState(facesContext)}));
                        }
                        else
                        {
                            String facetName = null;
                            if (target.getParent().getFacetCount() > 0)
                            {
                                for (Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet()) 
                                {
                                    if (target.equals(entry.getValue()))
                                    {
                                        facetName = entry.getKey();
                                        break;
                                    }
                                }
                            }
                            states.put(target.getClientId(facesContext),new AttachedFullStateWrapper(new Object[]{
                                    target.getParent().getClientId(facesContext),
                                    facetName,
                                    null,
                                    internalBuildTreeStructureToSave(target),
                                    target.processSaveState(facesContext)}));
                        }
                        return VisitResult.REJECT;
                    }
                    else if (target.getParent() != null)
                    {
                        state = target.saveState (facesContext);
                        if (state != null)
                        {
                            // Save by client ID into our map.
                            states.put(target.getClientId(facesContext), state);
                        }
                        
                        return VisitResult.ACCEPT;
                    }
                    else
                    {
                        //Only UIViewRoot has no parent in a component tree.
                        return VisitResult.ACCEPT;
                    }
                }
            });
        }
        finally
        {
            facesContext.getAttributes().remove(MyFacesVisitHints.SKIP_ITERATION_HINT);
        }
        if (!uiViewRoot.isTransient())
        {
            Object state = uiViewRoot.saveState (facesContext);
            if (state != null)
            {
                // Save by client ID into our map.
                states.put(uiViewRoot.getClientId(facesContext), state);
            }
        }
    }
    
    
    private SaveStateAndResetViewCallback saveStateOnMapVisitTreeAndReset(final FacesContext facesContext,
            final Map<String,Object> states, final UIViewRoot uiViewRoot, boolean forceHardReset)
    {
        facesContext.getAttributes().put(MyFacesVisitHints.SKIP_ITERATION_HINT, Boolean.TRUE);
        SaveStateAndResetViewCallback callback = new SaveStateAndResetViewCallback(
                facesContext.getViewRoot(), states, forceHardReset);
        if (forceHardReset)
        {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, 
                    ViewPoolProcessor.RESET_MODE_HARD);
        }
        else
        {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, 
                    ViewPoolProcessor.RESET_MODE_SOFT);
        }
        try
        {
            if (_viewPoolProcessor != null && 
                !_viewPoolProcessor.isViewPoolEnabledForThisView(facesContext, uiViewRoot))
            {
                callback.setViewResetable(false);
            }
            
            // Check if the view has removed components. If that so, it
            // means there is some manipulation over the component tree that
            // can be rollback, so it is ok to set the view as resetable.
            if (callback.isViewResetable())
            {
                List<String> removedIds = getClientIdsRemoved(uiViewRoot);
                if (removedIds != null && !removedIds.isEmpty())
                {
                    callback.setViewResetable(false);
                }
            }

            try
            {
                uiViewRoot.visitTree(getVisitContextFactory().getVisitContext(
                        facesContext, null, MyFacesVisitHints.SET_SKIP_ITERATION), callback);
            }
            finally
            {
                facesContext.getAttributes().remove(MyFacesVisitHints.SKIP_ITERATION_HINT);
            }
            
            if (callback.isViewResetable() && callback.isRemoveAddedComponents())
            {
                List<String> clientIdsToRemove = getClientIdsAdded(uiViewRoot);

                if (clientIdsToRemove != null)
                {
                    RemoveComponentCallback removeCallback = null;
                    
                    // perf: clientIds are ArrayList: see method registerOnAddRemoveList(String)
                    for (int i = 0, size = clientIdsToRemove.size(); i < size; i++)
                    {
                        if (removeCallback == null)
                        {
                            removeCallback = new RemoveComponentCallback();
                        }
                        removeCallback.setComponentFound(false);
                        
                        String clientId = clientIdsToRemove.get(i);
                        uiViewRoot.invokeOnComponent(facesContext, clientId, removeCallback);
                    }
                }
            }

            Object state = uiViewRoot.saveState(facesContext);
            if (state != null)
            {
                // Save by client ID into our map.
                states.put(uiViewRoot.getClientId (facesContext), state);

                //Hard reset (or reset and check state again)
                Integer oldResetMode = (Integer) uiViewRoot.getAttributes().put(
                        ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_HARD);
                state = uiViewRoot.saveState(facesContext);
                uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, oldResetMode);
                if (state != null)
                {
                    callback.setViewResetable(false);
                }
            }
        }
        finally
        {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, 
                    ViewPoolProcessor.RESET_MODE_OFF);
        }
        return callback;
    }
    
    private class SaveStateAndResetViewCallback implements VisitCallback
    {
        private final Map<String, Object> states;
        private final UIViewRoot view;
        private boolean viewResetable;
        private boolean skipRoot;
        private int count;
        private boolean forceHardReset;
        private boolean removeAddedComponents;
        
        public SaveStateAndResetViewCallback(UIViewRoot view, Map<String, Object> states, boolean forceHardReset)
        {
            this.states = states;
            this.view = view;
            this.viewResetable = true;
            this.skipRoot = true;
            this.count = 0;
            this.forceHardReset = forceHardReset;
            this.removeAddedComponents = false;
        }
        
        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            FacesContext facesContext = context.getFacesContext();
            Object state;
            this.count++;

            if ((target == null) || target.isTransient())
            {
                // No need to bother with these components or their children.
                return VisitResult.REJECT;
            }
            
            if (skipRoot && target instanceof UIViewRoot)
            {
                //UIViewRoot should be scanned at last.
                skipRoot = false;
                return VisitResult.ACCEPT;
            }

            ComponentState componentAddedAfterBuildView
                    = (ComponentState) target.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);

            //Note if UIViewRoot has this marker, JSF 1.2 like state saving is used.
            if (componentAddedAfterBuildView != null && (target.getParent() != null))
            {
                //Set this view as not resetable.
                //setViewResetable(false);
                // Enable flag to remove added components later
                setRemoveAddedComponents(true);
                if (forceHardReset)
                {
                    // The ideal is remove the added component here but visitTree does not support that
                    // kind of tree manipulation.
                    if (isViewResetable() &&
                        ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView))
                    {
                        setViewResetable(false);
                    }
                    // it is not important to save anything, skip
                    return VisitResult.REJECT;
                }
                if (ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView))
                {
                    //If the view has removed components, set the view as non resetable
                    setViewResetable(false);
                    registerOnAddRemoveList(facesContext, target.getClientId(facesContext));
                    target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                }
                else if (ComponentState.ADD.equals(componentAddedAfterBuildView))
                {
                    registerOnAddList(facesContext, target.getClientId(facesContext));
                    target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                }
                else if (ComponentState.ADDED.equals(componentAddedAfterBuildView))
                {
                    // Later on the check of removed components we'll see if the view
                    // is resetable or not.
                    registerOnAddList(facesContext, target.getClientId(facesContext));
                }
                ensureClearInitialState(target);
                //Save all required info to restore the subtree.
                //This includes position, structure and state of subtree

                int childIndex = target.getParent().getChildren().indexOf(target);
                if (childIndex >= 0)
                {
                    states.put(target.getClientId(facesContext), new AttachedFullStateWrapper( 
                            new Object[]{
                                target.getParent().getClientId(facesContext),
                                null,
                                childIndex,
                                internalBuildTreeStructureToSave(target),
                                target.processSaveState(facesContext)}));
                }
                else
                {
                    String facetName = null;
                    if (target.getParent().getFacetCount() > 0)
                    {
                        for (Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet()) 
                        {
                            if (target.equals(entry.getValue()))
                            {
                                facetName = entry.getKey();
                                break;
                            }
                        }
                    }
                    states.put(target.getClientId(facesContext), new AttachedFullStateWrapper(new Object[]{
                            target.getParent().getClientId(facesContext),
                            facetName,
                            null,
                            internalBuildTreeStructureToSave(target),
                            target.processSaveState(facesContext)}));
                }
                return VisitResult.REJECT;
            }
            else if (target.getParent() != null)
            {
                if (forceHardReset)
                {
                    // force hard reset set reset move on top
                    state = target.saveState (facesContext);
                    if (state != null)
                    {
                        setViewResetable(false);
                        return VisitResult.REJECT;
                    }
                }
                else
                {
                    state = target.saveState (facesContext);

                    if (state != null)
                    {
                        // Save by client ID into our map.
                        states.put(target.getClientId (facesContext), state);

                        if (isViewResetable())
                        {
                            //Hard reset (or reset and check state again)
                            Integer oldResetMode = (Integer) view.getAttributes().put(
                                    ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, 
                                    ViewPoolProcessor.RESET_MODE_HARD);
                            state = target.saveState (facesContext);
                            view.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, oldResetMode);
                            if (state != null)
                            {
                                setViewResetable(false);
                            }
                        }
                    }
                }

                return VisitResult.ACCEPT;
            }
            else
            {
                //Only UIViewRoot has no parent in a component tree.
                return VisitResult.ACCEPT;
            }
        }
        
        public boolean isViewResetable()
        {
            return viewResetable;
        }

        public void setViewResetable(boolean viewResetable)
        {
            this.viewResetable = viewResetable;
        }
        
        public int getCount()
        {
            return count;
        }

        public boolean isRemoveAddedComponents()
        {
            return removeAddedComponents;
        }

        public void setRemoveAddedComponents(boolean removeAddedComponents)
        {
            this.removeAddedComponents = removeAddedComponents;
        }
    }
    
    protected void ensureClearInitialState(UIComponent c)
    {
        c.clearInitialState();
        if (c.getChildCount() > 0)
        {
            for (int i = 0, childCount = c.getChildCount(); i < childCount; i++)
            {
                UIComponent child = c.getChildren().get(i);
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
        boolean listenerSubscribed = false;
        List<SystemEventListener> pavList = uiViewRoot.getViewListenersForEventClass(PostAddToViewEvent.class);
        if (pavList != null)
        {
            for (SystemEventListener listener : pavList)
            {
                if (listener instanceof PostAddPreRemoveFromViewListener)
                {
                    listenerSubscribed = true;
                    break;
                }
            }
        }
        if (!listenerSubscribed)
        {
            PostAddPreRemoveFromViewListener componentListener = new PostAddPreRemoveFromViewListener();
            uiViewRoot.subscribeToViewEvent(PostAddToViewEvent.class, componentListener);
            uiViewRoot.subscribeToViewEvent(PreRemoveFromViewEvent.class, componentListener);
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
    
    protected VisitContextFactory getVisitContextFactory()
    {
        if (_visitContextFactory == null)
        {
            _visitContextFactory = (VisitContextFactory)FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        }
        return _visitContextFactory;
    }

    
    public static class PostAddPreRemoveFromViewListener implements SystemEventListener
    {
        private transient FacesContext _facesContext;
        
        private transient Boolean _isRefreshOnTransientBuildPreserveState;

        @Override
        public boolean isListenerForSource(Object source)
        {
            // PostAddToViewEvent and PreRemoveFromViewEvent are
            // called from UIComponentBase.setParent
            return (source instanceof UIComponent);
        }
        
        private boolean isRefreshOnTransientBuildPreserveState()
        {
            if (_isRefreshOnTransientBuildPreserveState == null)
            {
                _isRefreshOnTransientBuildPreserveState = MyfacesConfig.getCurrentInstance(_facesContext)
                        .isRefreshTransientBuildOnPSSPreserveState();
            }
            return _isRefreshOnTransientBuildPreserveState;
        }

        @Override
        public void processEvent(SystemEvent event)
        {
            UIComponent component = (UIComponent) event.getSource();
            
            if (component.isTransient())
            {
                return;
            }
            
            // This is a view listener. It is not saved on the state and this listener
            // is suscribed each time the view is restored, so we can cache facesContext
            // here
            if (_facesContext == null)
            {
                _facesContext = FacesContext.getCurrentInstance();
            }
            
            if (event instanceof PostAddToViewEvent)
            {
                if (!isRefreshOnTransientBuildPreserveState() &&
                    Boolean.TRUE.equals(_facesContext.getAttributes().get(StateManager.IS_BUILDING_INITIAL_STATE)))
                {
                    return;
                }

                //PostAddToViewEvent
                component.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADD);
            }
            else
            {
                //FacesContext facesContext = FacesContext.getCurrentInstance();
                // In this case if we are removing components on build, it is not necessary to register
                // again the current id, and its more, it could cause a concurrent exception. But note
                // we need to propagate PreRemoveFromViewEvent, otherwise the view will not be restored
                // correctly.
                if (FaceletViewDeclarationLanguage.isRemovingComponentBuild(_facesContext))
                {
                    return;
                }

                if (!isRefreshOnTransientBuildPreserveState() &&
                    FaceletCompositionContext.getCurrentInstance(_facesContext) != null &&
                    (component.getAttributes().containsKey(ComponentSupport.MARK_CREATED) ||
                     component.getAttributes().containsKey(ComponentSupport.COMPONENT_ADDED_BY_HANDLER_MARKER))
                    )
                {
                    // Components removed by facelets algorithm does not need to be registered
                    // unless preserve state mode is used, because PSS initial state is changed
                    // to restore delta properly.
                    // MYFACES-3554 It is possible to find use cases where a component
                    // created by a facelet tag is changed dynamically in some way in render
                    // response time, so we need to check here also when facelets algorithm
                    // is running or not. 
                    return;
                }
                
                //PreRemoveFromViewEvent
                UIViewRoot uiViewRoot = _facesContext.getViewRoot();
                
                List<String> clientIdsRemoved = getClientIdsRemoved(uiViewRoot);
                if (clientIdsRemoved == null)
                {
                    //Create a set that preserve insertion order
                    clientIdsRemoved = new ArrayList<>();
                }
                clientIdsRemoved.add(component.getClientId(_facesContext));
                setClientsIdsRemoved(uiViewRoot, clientIdsRemoved);
            }
        }
    }
    
    private static TreeStructComponent internalBuildTreeStructureToSave(UIComponent component)
    {
        TreeStructComponent structComp = new TreeStructComponent(component.getClass().getName(),
                                                                 component.getId());

        //children
        if (component.getChildCount() > 0)
        {
            List<TreeStructComponent> structChildList = new ArrayList<>();
            for (int i = 0, childCount = component.getChildCount(); i < childCount; i++)
            {
                UIComponent child = component.getChildren().get(i);     
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
        if (component.getFacetCount() > 0)
        {
            Map<String, UIComponent> facetMap = component.getFacets();
            List<Object[]> structFacetList = new ArrayList<>();
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
    
    private static UIComponent internalRestoreTreeStructure(TreeStructComponent treeStructComp)
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
