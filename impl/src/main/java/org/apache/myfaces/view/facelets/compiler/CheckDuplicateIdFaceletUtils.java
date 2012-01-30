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
package org.apache.myfaces.view.facelets.compiler;

import java.util.HashSet;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author Leonardo Uribe
 */
public final class CheckDuplicateIdFaceletUtils
{
    
    public static void checkIdsStatefulComponents (FacesContext context, UIComponent view)
    {
        checkIdsStatefulComponents (context, view, new HashSet<String>());
    }

    private static void checkIdsStatefulComponents (FacesContext context, 
            UIComponent component, Set<String> existingIds)
    {
        String id;
        
        if (component == null)
        {
            return;
        }
        
        // Need to use this form of the client ID method so we generate the client-side ID.
        
        id = component.getClientId (context);
        
        if (existingIds.contains (id))
        {
            throw new IllegalStateException ("component with duplicate id \"" + id + "\" found");
        }
        
        existingIds.add (id);
        
        int facetCount = component.getFacetCount();
        if (facetCount > 0)
        {
            for (UIComponent facet : component.getFacets().values())
            {
                if (!(facet instanceof UILeaf))
                {
                    checkIdsStatefulComponents (context, facet, existingIds);
                }
            }
        }
        for (int i = 0, childCount = component.getChildCount(); i < childCount; i++)
        {
            UIComponent child = component.getChildren().get(i);
            if (!(child instanceof UILeaf))
            {
                checkIdsStatefulComponents (context, child, existingIds);
            }
        }
    }

    public static void checkIds (FacesContext context, UIComponent view)
    {
        checkIds (context, view, new HashSet<String>());
    }
    
    private static void checkIds (FacesContext context, UIComponent component, Set<String> existingIds)
    {
        String id;
        
        if (component == null)
        {
            return;
        }
        
        // Need to use this form of the client ID method so we generate the client-side ID.
        
        id = component.getClientId (context);
        
        if (existingIds.contains (id))
        {
            throw new IllegalStateException ("component with duplicate id \"" + id + "\" found");
        }
        
        existingIds.add (id);
        
        int facetCount = component.getFacetCount();
        if (facetCount > 0)
        {
            for (UIComponent facet : component.getFacets().values())
            {
                checkIds (context, facet, existingIds);
            }
        }
        for (int i = 0, childCount = component.getChildCount(); i < childCount; i++)
        {
            UIComponent child = component.getChildren().get(i);
            checkIds (context, child, existingIds);
        }
    }
}
