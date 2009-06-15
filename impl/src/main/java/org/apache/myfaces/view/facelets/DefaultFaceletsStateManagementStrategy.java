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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.shared_impl.renderkit.RendererUtils;

public class DefaultFaceletsStateManagementStrategy extends StateManagementStrategy
{
    private ViewDeclarationLanguage vdl;
    
    public DefaultFaceletsStateManagementStrategy (ViewDeclarationLanguage vdl)
    {
        this.vdl = vdl;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public UIViewRoot restoreView (FacesContext context, String viewId, String renderKitId)
    {
        ResponseStateManager manager;
        Object state[];
        Map<String, Object> states;
        
        UIViewRoot view;
        
        // Per the spec: build the view.
        
        try {
            view = vdl.createView (context, viewId);
            
            vdl.buildView (context, view);
            
            context.setViewRoot (view);
        }
        
        catch (Throwable e) {
            throw new FacesException ("unable to create view \"" + viewId + "\"", e);
        }
        
        // Get previous state from ResponseStateManager.
        
        manager = RendererUtils.getResponseStateManager (context, renderKitId);
        
        state = (Object[]) manager.getState (context, viewId);
        states = (Map<String, Object>) state[1];
        
        // Visit the children and restore their state.
        
        view.visitTree (VisitContext.createVisitContext (context), new RestoreStateVisitor (states));
        
        // TODO: handle dynamic add/removes as mandated by the spec.  Not sure how to do handle this yet.
        
        return view;
    }

    @Override
    public Object saveView (FacesContext context)
    {
        UIViewRoot view = context.getViewRoot();
        HashMap<String, Object> states;
        
        if (view == null) {
            // Not much that can be done.
            
            return null;
        }
        
        if (view.isTransient()) {
            // Must return null immediately per spec.
            
            return null;
        }
        
        // Make sure the client IDs are unique per the spec.
        
        checkIds (context, view, new HashSet<String>());
        
        // Create save state objects for every component.
        
        states = new HashMap<String, Object>();
        
        view.visitTree (VisitContext.createVisitContext (context), new SaveStateVisitor (states));
        
        // TODO: not sure the best way to handle dynamic adds/removes as mandated by the spec.
        
        // As required by ResponseStateManager, the return value is an Object array.  First
        // element is the structure object, second is the state map.
        
        return new Object[] { null, states };
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
}
