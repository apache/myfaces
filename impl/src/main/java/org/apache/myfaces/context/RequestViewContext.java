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
package org.apache.myfaces.context;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

/**
 *
 * @author  Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0.2
 */
public class RequestViewContext
{

    public static final String VIEW_CONTEXT_KEY = "oam.VIEW_CONTEXT";
    
    public static final String RESOURCE_DEPENDENCY_INSPECTED_CLASS = "oam.RDClass";
    
    private static final String SKIP_ITERATION_HINT = "javax.faces.visit.SKIP_ITERATION";
    
    private static final Set<VisitHint> VISIT_HINTS = Collections.unmodifiableSet( 
            EnumSet.of(VisitHint.SKIP_ITERATION));
    
    private Map<ResourceDependency, Boolean> addedResources;
    
    // No lazy init: every view has one (UIView.class) or more classes to process   
    private Map<Class<?>, Boolean> processedClasses = new HashMap<Class<?>,Boolean>();
    
    private Map<String, Boolean> renderTargetMap = null;

    static public RequestViewContext getCurrentInstance()
    {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return getCurrentInstance(ctx);
    }
    
    static public RequestViewContext getCurrentInstance(FacesContext ctx)
    {
        return getCurrentInstance(ctx, ctx.getViewRoot());
    }
    
    @SuppressWarnings("unchecked")
    static public RequestViewContext getCurrentInstance(FacesContext ctx, UIViewRoot root)
    {
        Map<UIViewRoot, RequestViewContext> map
                = (Map<UIViewRoot, RequestViewContext>) ctx.getAttributes().get(VIEW_CONTEXT_KEY);
        RequestViewContext rvc = null;        
        if (map == null)
        {
            map = new HashMap<UIViewRoot, RequestViewContext>();
            rvc = new RequestViewContext();
            map.put(root, rvc);
            ctx.getAttributes().put(VIEW_CONTEXT_KEY, map);
            return rvc;
        }
        else
        {
            rvc = map.get(root); 
            if (rvc == null)
            {
                rvc = new RequestViewContext();
                map.put(root, rvc);
            }
            return rvc;
        }
    }

    public boolean isResourceDependencyAlreadyProcessed(ResourceDependency dependency)
    {
        if (addedResources == null)
        {
            return false;
        }
        return addedResources.containsKey(dependency); 
    }
    
    public void setResourceDependencyAsProcessed(ResourceDependency dependency)
    {
        if (addedResources == null)
        {
            addedResources = new HashMap<ResourceDependency,Boolean>();
        }
        addedResources.put(dependency, true);
    }

    public boolean isClassAlreadyProcessed(Class<?> inspectedClass)
    {
        return processedClasses.containsKey(inspectedClass);
    }

    public void setClassProcessed(Class<?> inspectedClass)
    {
        processedClasses.put(inspectedClass, Boolean.TRUE);
    }
    
    public boolean isRenderTarget(String target)
    {
        if (renderTargetMap != null)
        {
            return Boolean.TRUE.equals(renderTargetMap.get(target));
        }
        return false;
    }
    
    public void setRenderTarget(String target, boolean value)
    {
        if (renderTargetMap == null)
        {
            renderTargetMap = new HashMap<String, Boolean>(8);
        }
        renderTargetMap.put(target, value);
    }
    
    /**
     * Scans UIViewRoot facets with added component resources by the effect of
     * @ResourceDependency annotation, and register the associated inspected classes
     * so new component resources will not be added to the component tree again and again.
     * 
     * @param facesContext
     * @param root 
     */
    public void refreshRequestViewContext(FacesContext facesContext, UIViewRoot root)
    {
        for (Map.Entry<String, UIComponent> entry : root.getFacets().entrySet())
        {
            UIComponent facet = entry.getValue();
            if (facet.getId() != null && facet.getId().startsWith("javax_faces_location_"))
            {
                try
                {
                    facesContext.getAttributes().put(SKIP_ITERATION_HINT, Boolean.TRUE);

                    VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, VISIT_HINTS);
                    facet.visitTree(visitContext, new RefreshViewContext());
                }
                finally
                {
                    // We must remove hint in finally, because an exception can break this phase,
                    // but lifecycle can continue, if custom exception handler swallows the exception
                    facesContext.getAttributes().remove(SKIP_ITERATION_HINT);
                }
            }
        }
    }
    
    private class RefreshViewContext implements VisitCallback
    {

        public VisitResult visit(VisitContext context, UIComponent target)
        {
            Class<?> inspectedClass = (Class<?>)target.getAttributes().get(RESOURCE_DEPENDENCY_INSPECTED_CLASS);
            if (inspectedClass != null)
            {
                setClassProcessed(inspectedClass);
            }            
            return VisitResult.ACCEPT;
        }
    }
}
