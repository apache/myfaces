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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

class UILeaf extends UIComponentBase
{

    private final static Map<String, UIComponent> facets = new HashMap<String, UIComponent>()
    {

        @Override
        public void putAll(Map<? extends String, ? extends UIComponent> map)
        {
            // do nothing
        }

        @Override
        public UIComponent put(String name, UIComponent value)
        {
            return null;
        }
    };

    private UIComponent parent;
    
    private _ComponentAttributesMap attributesMap;

    @Override
    public Map<String, Object> getAttributes()
    {
        // Since all components extending UILeaf are only transient references
        // to text or instructions, we can do the following simplifications:  
        // 1. Don't use reflection to retrieve properties, because it will never
        // be done
        // 2. Since the only key that will be saved here is MARK_ID, we can create
        // a small map of size 2. In practice, this will prevent create a lot of
        // maps, like the ones used on state helper
        if (attributesMap == null)
        {
            attributesMap = new _ComponentAttributesMap();
        }
        return attributesMap;
    }

    @Override
    public void clearInitialState()
    {
       //this component is transient, so it can be marked, because it does not have state!
    }

    @Override
    public boolean initialStateMarked()
    {
        //this component is transient, so it can be marked, because it does not have state!
        return false;
    }

    @Override
    public void markInitialState()
    {
        //this component is transient, so no need to do anything
    }

    
    @Override
    public void processEvent(ComponentSystemEvent event)
            throws AbortProcessingException
    {
        //Do nothing, because UILeaf will not need to handle "binding" property
    }

    @Override
    @SuppressWarnings("deprecation")
    public ValueBinding getValueBinding(String binding)
    {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setValueBinding(String name, ValueBinding binding)
    {
        // do nothing
    }

    @Override
    public ValueExpression getValueExpression(String name)
    {
        return null;
    }

    @Override
    public void setValueExpression(String name, ValueExpression arg1)
    {
        // do nothing
    }

    public String getFamily()
    {
        return "facelets.LiteralText";
    }

    @Override
    public UIComponent getParent()
    {
        return this.parent;
    }

    @Override
    public void setParent(UIComponent parent)
    {
        this.parent = parent;
    }

    @Override
    public boolean isRendered()
    {
        return true;
    }

    @Override
    public void setRendered(boolean rendered)
    {
        // do nothing
    }

    @Override
    public String getRendererType()
    {
        return null;
    }

    @Override
    public void setRendererType(String rendererType)
    {
        // do nothing
    }

    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public List<UIComponent> getChildren()
    {
        List<UIComponent> children = Collections.emptyList();
        return children;
    }

    @Override
    public int getChildCount()
    {
        return 0;
    }

    @Override
    public UIComponent findComponent(String id)
    {
        return null;
    }

    @Override
    public Map<String, UIComponent> getFacets()
    {
        return facets;
    }

    @Override
    public int getFacetCount()
    {
        return 0;
    }

    @Override
    public UIComponent getFacet(String name)
    {
        return null;
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren()
    {
        List<UIComponent> childrenAndFacets = Collections.emptyList();
        
        return childrenAndFacets.iterator();
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        // do nothing
    }

    @Override
    public void decode(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void encodeBegin(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeEnd(FacesContext faces) throws IOException
    {
        // do nothing
    }

    @Override
    public void encodeAll(FacesContext faces) throws IOException
    {
        this.encodeBegin(faces);
    }

    @Override
    protected void addFacesListener(FacesListener faces)
    {
        // do nothing
    }

    @Override
    protected FacesListener[] getFacesListeners(Class faces)
    {
        return null;
    }

    @Override
    protected void removeFacesListener(FacesListener faces)
    {
        // do nothing
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        // do nothing
    }

    @Override
    public void processRestoreState(FacesContext faces, Object state)
    {
        // do nothing
    }

    @Override
    public void processDecodes(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void processValidators(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public void processUpdates(FacesContext faces)
    {
        // do nothing
    }

    @Override
    public Object processSaveState(FacesContext faces)
    {
        return null;
    }

    @Override
    protected FacesContext getFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected Renderer getRenderer(FacesContext faces)
    {
        return null;
    }

    @Override
    public Object saveState(FacesContext faces)
    {
        return null;
    }

    @Override
    public void restoreState(FacesContext faces, Object state)
    {
        // do nothing
    }

    @Override
    public boolean isTransient()
    {
        return true;
    }

    @Override
    public void setTransient(boolean tranzient)
    {
        // do nothing
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback)
            throws FacesException
    {
        //this component will never be a target for a callback, so always return false.
        return false;
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        // the visiting is complete and it shouldn't affect the visiting of the other
        // children of the parent component, therefore return false
        return false;
    }

    private static class _ComponentAttributesMap implements Map<String, Object>, Serializable
    {
        private static final long serialVersionUID = -4459484500489059515L;
        
        private Map<String, Object> _attributes = null;
        
        _ComponentAttributesMap()
        {
        }
        
        public int size()
        {
            return _attributes == null ? 0 : _attributes.size();
        }
        
        public void clear()
        {
            if (_attributes != null)
            {
                _attributes.clear();
            }
        }
        
        public boolean isEmpty()
        {
            return _attributes == null ? false : _attributes.isEmpty();
        }
        
        public boolean containsKey(Object key)
        {
            checkKey(key);
        
            return (_attributes == null ? false :_attributes.containsKey(key));
        }
        
        public boolean containsValue(Object value)
        {
            return (_attributes == null) ? false : _attributes.containsValue(value);
        }
        
        public Collection<Object> values()
        {
            return getUnderlyingMap().values();
        }
        
        public void putAll(Map<? extends String, ? extends Object> t)
        {
            for (Map.Entry<? extends String, ? extends Object> entry : t.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }

        public Set<Entry<String, Object>> entrySet()
        {
            return getUnderlyingMap().entrySet();
        }

        public Set<String> keySet()
        {
            return getUnderlyingMap().keySet();
        }
        
        public Object get(Object key)
        {
            checkKey(key);

            if ("rendered".equals(key))
            {
                return true;
            }
            if ("transient".equals(key))
            {
                return true;
            }

            return (_attributes == null) ? null : _attributes.get(key);
        }
        
        public Object remove(Object key)
        {
            checkKey(key);

            return (_attributes == null) ? null : _attributes.remove(key);
        }
        
        public Object put(String key, Object value)
        {
            checkKey(key);

            return getUnderlyingMap().put(key, value);
        }
        
        private void checkKey(Object key)
        {
            if (key == null)
            {
                throw new NullPointerException("key");
            }
            if (!(key instanceof String))
            {
                throw new ClassCastException("key is not a String");
            }
        }
        
        Map<String, Object> getUnderlyingMap()
        {
            if (_attributes == null)
            {
                _attributes = new HashMap<String, Object>(2,1);
            }
            return _attributes;
        }
        
        public boolean equals(Object obj)
        {
            return getUnderlyingMap().equals(obj);
        }
        
        public int hashCode()
        {
            return getUnderlyingMap().hashCode();
        }
    }
}
