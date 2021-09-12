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

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.application.TreeStructureManager;
import org.apache.myfaces.application.viewstate.StateCacheUtils;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.core.api.shared.ComponentUtils;

public class FullStateManagementStrategy extends StateManagementStrategy
{
    private static final Logger LOG = Logger.getLogger(FullStateManagementStrategy.class.getName());

    private RenderKitFactory renderKitFactory;

    public static final String SERIALIZED_VIEW_REQUEST_ATTR = 
            FullStateManagementStrategy.class.getName() + ".SERIALIZED_VIEW";

    public FullStateManagementStrategy(FacesContext context)
    {
        
    }
    
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Entering restoreView - viewId: " + viewId + " ; renderKitId: " + renderKitId);
        }

        UIViewRoot uiViewRoot = null;

        RenderKit renderKit = getRenderKitFactory().getRenderKit(context, renderKitId);
        ResponseStateManager responseStateManager = renderKit.getResponseStateManager();

        Object state = responseStateManager.getState(context, viewId);
        if (state != null)
        {
            Object[] stateArray = (Object[])state;
            uiViewRoot = TreeStructureManager.restoreTreeStructure(((Object[]) stateArray[0])[0]);

            if (uiViewRoot != null)
            {
                context.setViewRoot(uiViewRoot);
                uiViewRoot.processRestoreState(context, stateArray[1]);

                RequestViewContext.getCurrentInstance(context).refreshRequestViewContext(
                        context, uiViewRoot);

                // If state is saved fully, there outer f:view tag handler will not be executed,
                // so "contracts" attribute will not be set properly. We need to save it and
                // restore it from here. With PSS, the view will always be built so it is not
                // necessary to save it on the state.
                Object rlc = ((Object[]) stateArray[0])[1];
                if (rlc != null)
                {
                    context.setResourceLibraryContracts((List) UIComponentBase.
                        restoreAttachedState(context, rlc));
                }
            }
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Exiting restoreView - " + viewId);
        }

        return uiViewRoot;
    }

    @Override
    public Object saveView(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();
        if (uiViewRoot.isTransient())
        {
            return null;
        }
        
        Object serializedView = null;
        ResponseStateManager responseStateManager = facesContext.getRenderKit().getResponseStateManager();

        try
        {
            facesContext.getAttributes().put(StateManager.IS_SAVING_STATE, Boolean.TRUE);
    
            // In StateManagementStrategy.saveView there is a check for transient at
            // start, but the same applies for VDL without StateManagementStrategy,
            // so this should be checked before call parent (note that parent method
            // does not do this check).
            if (uiViewRoot.isTransient())
            {
                return null;
            }
    
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Entering saveSerializedView");
            }
    
            checkForDuplicateIds(facesContext, facesContext.getViewRoot(), new HashSet<>());
    
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Processing saveSerializedView - Checked for duplicate Ids");
            }
    
            // SerializedView already created before within this request?
            serializedView = facesContext.getAttributes().get(SERIALIZED_VIEW_REQUEST_ATTR);
            if (serializedView == null)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Processing saveSerializedView - create new serialized view");
                }
    
                // first call to saveSerializedView --> create SerializedView
                Object treeStruct = getTreeStructureToSave(facesContext);
                Object compStates = getComponentStateToSave(facesContext);
                Object rlcStates = !facesContext.getResourceLibraryContracts().isEmpty() ? 
                    UIComponentBase.saveAttachedState(facesContext, 
                                new ArrayList<>(facesContext.getResourceLibraryContracts())) : null;
                serializedView = new Object[] {
                        new Object[]{ treeStruct, rlcStates },
                        compStates};
                facesContext.getAttributes().put(SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
    
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Processing saveSerializedView - new serialized view created");
                }
            }
            
            // If MyfacesResponseStateManager is used, give the option to do
            // additional operations for save the state if is necessary.
            if (StateCacheUtils.isMyFacesResponseStateManager(responseStateManager))
            {
                StateCacheUtils.getMyFacesResponseStateManager(responseStateManager).
                        saveState(facesContext, serializedView);
            }
    
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Exiting saveView");
            }
        }
        finally
        {
            facesContext.getAttributes().remove(StateManager.IS_SAVING_STATE);
        }

        return serializedView;
    }
    
    private static void checkForDuplicateIds(FacesContext context,
                                             UIComponent component,
                                             Set<String> ids)
    {
        String id = component.getId();
        if (id != null && !ids.add(id))
        {
            throw new IllegalStateException("Client-id : " + id +
                                            " is duplicated in the faces tree. Component : " + 
                                            component.getClientId(context)+", path: " +
                                            ComponentUtils.getPathToComponent(component));
        }
        
        if (component instanceof NamingContainer)
        {
            ids = new HashSet<>();
        }

        if (component.getFacetCount() > 0)
        {
            for (UIComponent facet : component.getFacets().values())
            {
                checkForDuplicateIds(context, facet, ids);
            }
        }
        for (int i = 0, childCount = component.getChildCount(); i < childCount; i++)
        {
            UIComponent child = component.getChildren().get(i);
            checkForDuplicateIds(context, child, ids);
        }
    }
    
    protected RenderKitFactory getRenderKitFactory()
    {
        if (renderKitFactory == null)
        {
            renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return renderKitFactory;
    }

    protected Object getComponentStateToSave(FacesContext facesContext)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Entering getComponentStateToSave");
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }

        Object serializedComponentStates = viewRoot.processSaveState(facesContext);
        //Locale is a state attribute of UIViewRoot and need not be saved explicitly
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Exiting getComponentStateToSave");
        }
        return serializedComponentStates;
    }

    /**
     * Return an object which contains info about the UIComponent type
     * of each node in the view tree. This allows an identical UIComponent
     * tree to be recreated later, though all the components will have
     * just default values for their members.
     */
    protected Object getTreeStructureToSave(FacesContext facesContext)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Entering getTreeStructureToSave");
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }

        Object retVal = TreeStructureManager.buildTreeStructureToSave(viewRoot);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Exiting getTreeStructureToSave");
        }
        return retVal;
    }
}
