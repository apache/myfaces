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
package org.apache.myfaces.view;

import jakarta.enterprise.inject.spi.BeanManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PreDestroyViewMapEvent;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.cdi.view.ViewScopeContextualStorageHolder;
import org.apache.myfaces.cdi.view.ViewScopeCDIMap;
import org.apache.myfaces.util.ExternalSpecifications;

/**
 * This wrapper has these objectives:
 * 
 * - Isolate the part that needs to be saved with the view (viewScopeId) from
 *   the part that should remain into session (bean map). This class will be
 *   serialized when UIViewRoot.saveState() is called.
 * - Decouple the way how the view scope map is stored. For example, in 
 *   CDI view scope a session scope bean is used, and in default view scope
 *   the same session map is used but using a prefix.
 *
 * @author Leonardo Uribe
 */
public class ViewScopeProxyMap extends HashMap<String, Object> implements StateHolder
{
    private String viewScopeId;
    private transient Map<String, Object> delegate;

    public ViewScopeProxyMap()
    {
    }
    
    public String getViewScopeId()
    {
        return viewScopeId;
    }
    
    public void forceDelegateCreation(FacesContext facesContext)
    {
        getDelegate();
    }
    
    public Map<String, Object> getDelegate()
    {
        if (delegate == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            // for non-CDI environments or unittests
            if (facesContext == null || !ExternalSpecifications.isCDIAvailable(facesContext.getExternalContext()))
            {
                delegate = new HashMap<>();
            }
            else
            {
                if (viewScopeId == null)
                {
                    BeanManager beanManager = CDIUtils.getBeanManager(facesContext);
                    ViewScopeContextualStorageHolder beanHolder =
                            CDIUtils.get(beanManager, ViewScopeContextualStorageHolder.class);
                    viewScopeId = beanHolder.generateUniqueViewScopeId();
                }
                delegate = new ViewScopeCDIMap(viewScopeId);
            }
        }
        return delegate;
    }
    
    @Override
    public int size()
    {
        return getDelegate().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getDelegate().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return getDelegate().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return getDelegate().containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return getDelegate().get(key);
    }

    @Override
    public Object put(String key, Object value)
    {
        return getDelegate().put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return getDelegate().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        getDelegate().putAll(m);
    }

    @Override
    public void clear()
    {
        /*
         * The returned Map must be implemented such that calling clear() on the Map causes
         * Application.publishEvent(java.lang.Class, java.lang.Object) to be called, passing
         * ViewMapDestroyedEvent.class as the first argument and this UIViewRoot instance as the second argument.
         */
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getApplication().publishEvent(facesContext, 
                PreDestroyViewMapEvent.class, facesContext.getViewRoot());
        
        getDelegate().clear();
    }

    @Override
    public Set<String> keySet()
    {
        return getDelegate().keySet();
    }

    @Override
    public Collection<Object> values()
    {
        return getDelegate().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return getDelegate().entrySet();
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        viewScopeId = (String) state;
    }

    @Override
    public Object saveState(FacesContext context)
    {
        return viewScopeId;
    }

    @Override
    public boolean isTransient()
    {
        return false;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
    }
    
}
