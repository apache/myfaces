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
package org.apache.myfaces.el.resolver;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;

import java.util.Map;

/**
 * See Faces 1.2 spec section 5.6.2.7
 * 
 * @author Stan Silvert
 */
public class ScopedAttributeResolver extends ELResolver
{

    /**
     * Creates a new instance of ScopedAttributeResolver
     */
    public ScopedAttributeResolver()
    {
    }

    @Override
    public void setValue(final ELContext context, final Object base, final Object property, final Object value)
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        if (base != null)
        {
            return;
        }

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }

        final Map<String, Object> scopedMap = findScopedMap(facesContext(context), property);
        if (scopedMap != null)
        {
            scopedMap.put((String)property, value);
        }
        else
        {
            externalContext(context).getRequestMap().put((String)property, value);
        }

        context.setPropertyResolved(true);
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (base == null)
        {
            context.setPropertyResolved(true);
        }

        return false;
    }

    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException
    {
        if (base != null)
        {
            return null;
        }

        if (property == null)
        {
            throw new PropertyNotFoundException();
        }

        context.setPropertyResolved(true);

        final FacesContext facesContext = facesContext(context);
        if (facesContext == null)
        {
            return null;
        }
        final ExternalContext extContext = facesContext.getExternalContext();
        if (extContext == null)
        {
            return null;
        }

        final boolean startup = (extContext instanceof StartupServletExternalContextImpl);

        // A single get() per scope instead of findScopedMap()'s containsKey()+get(): servlet
        // request/session/application attributes are never null, so a non-null get() means the key
        // is present. This halves the attribute-map lookups for the very hot case of resolving a
        // UIData/UIRepeat row var (e.g. #{row}) once per cell. Mirrors the reference
        // ScopedAttributeELResolver. The view map is a plain Map that may legitimately hold a null
        // value, so it keeps the containsKey() check.
        Object value;

        // request scope (not available at startup)
        if (!startup)
        {
            value = extContext.getRequestMap().get(property);
            if (value != null)
            {
                return value;
            }
        }

        // jsf 2.0 view scope
        UIViewRoot root = facesContext.getViewRoot();
        if (root != null)
        {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null && viewMap.containsKey(property))
            {
                return viewMap.get(property);
            }
        }

        // session scope (not available at startup)
        if (!startup)
        {
            value = extContext.getSessionMap().get(property);
            if (value != null)
            {
                return value;
            }
        }

        // application scope
        value = extContext.getApplicationMap().get(property);
        if (value != null)
        {
            return value;
        }

        return null;
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException
    {

        if (base != null)
        {
            return null;
        }
        if (property == null)
        {
            throw new PropertyNotFoundException();
        }

        context.setPropertyResolved(true);
        return Object.class;
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context, final Object base)
    {

        if (base != null)
        {
            return null;
        }

        return String.class;
    }

    // returns null if not found
    private static Map<String, Object> findScopedMap(final FacesContext facesContext, final Object property)
    {
        if (facesContext == null)
        {
            return null;
        }
        
        final ExternalContext extContext = facesContext.getExternalContext();
        if (extContext == null)
        {
            return null;
        }

        final boolean startup = (extContext instanceof StartupServletExternalContextImpl);
        Map<String, Object> scopedMap;

        // request scope (not available at startup)
        if (!startup)
        {
            scopedMap = extContext.getRequestMap();
            if (scopedMap.containsKey(property))
            {
                return scopedMap;
            }
        }

        // jsf 2.0 view scope
        UIViewRoot root = facesContext.getViewRoot();
        if (root != null)
        {
            scopedMap = root.getViewMap(false);
            if (scopedMap != null && scopedMap.containsKey(property))
            {
                return scopedMap;
            }
        }

        // session scope (not available at startup)
        if (!startup)
        {
            scopedMap = extContext.getSessionMap();
            if (scopedMap.containsKey(property))
            {
                return scopedMap;
            }
        }

        // application scope
        scopedMap = extContext.getApplicationMap();
        if (scopedMap.containsKey(property))
        {
            return scopedMap;
        }

        // not found
        return null;
    }

    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext)context.getContext(FacesContext.class);
    }

    private static ExternalContext externalContext(final ELContext context)
    {
        FacesContext facesContext = facesContext(context);
        if (facesContext != null)
        {
            return facesContext(context).getExternalContext();
        }

        return null;
    }

}
