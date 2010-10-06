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

import javax.faces.application.ResourceDependency;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 *
 * @author  Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 799765 $ $Date: 2009-07-31 17:55:49 -0500 (vie, 31 jul 2009) $
 * @since 2.0.2
 */
public class RequestViewContext
{

    public static final String VIEW_CONTEXT_KEY = "oam.VIEW_CONTEXT";
    
    private Map<ResourceDependency, Boolean> addedResources = new HashMap<ResourceDependency,Boolean>();
    
    private Map<Class<?>, Boolean> processedClasses = new HashMap<Class<?>,Boolean>();

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
}
