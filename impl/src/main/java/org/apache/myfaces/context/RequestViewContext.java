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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.application.ResourceDependency;
import javax.faces.component.UIViewRoot;
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
    
    private Map<ResourceDependency, Boolean> addedResources = new HashMap<ResourceDependency,Boolean>();
    
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
        Map<UIViewRoot, RequestViewContext> map = (Map<UIViewRoot, RequestViewContext>) ctx.getAttributes().get(VIEW_CONTEXT_KEY);
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
        return addedResources.containsKey(dependency); 
    }
    
    public void setResourceDependencyAsProcessed(ResourceDependency dependency)
    {
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
}
