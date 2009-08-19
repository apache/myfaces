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
package org.apache.myfaces.el.unified.resolver;

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.el.CompositeComponentExpressionHolder;

/**
 * Composite component attribute EL resolver.  See JSF spec, section 5.6.2.2.
 */

public final class CompositeComponentELResolver extends ELResolver
{
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        // Per the spec, return String.class.

        return String.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
            Object base)
    {
        // Per the spec, do nothing.

        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        // Per the spec, return null.

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        // Per the spec: base must not be null, an instance of UIComponent, and a composite
        // component.  Property must be a String.

        if ((base != null) && (base instanceof UIComponent)
                && UIComponent.isCompositeComponent((UIComponent) base)
                && (property != null))
        {
            String propName = property.toString();
            UIComponent baseComponent = (UIComponent) base;

            if (propName.equals("attrs"))
            {
                // Return a wrapped map that delegates all calls except get() and put().

                context.setPropertyResolved(true);

                return new AttributesMap(baseComponent.getAttributes(), context);
            }

            else if (propName.equals("parent"))
            {
                // Return the parent.

                context.setPropertyResolved(true);

                return UIComponent.getCompositeComponentParent(baseComponent);
            }
        }

        // Otherwise, spec says to do nothing (return null).

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        // Per the spec, return true.

        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property,
            Object value)
    {
        // Per the spec, do nothing.
    }

    // Wrapper map for composite component attributes.  Follows spec, section 5.6.2.2, table 5-11.
    //
    // TODO: This map could be cached at request level (or context). If we have many attribute 
    // lookups in a composite component, a map is created per each lookup, and it is possible to 
    // reduce object allocation. Put this map on view scope is not wanted because it will be
    // saved an restored, and there is no way to restore the reference to the component
    // holding this object.
    private final class AttributesMap implements CompositeComponentExpressionHolder,
            Map<String, Object>
    {

        private final BeanInfo _beanInfo;
        private final Map<String, Object> _originalMap;
        private final ELContext _elContext;

        private AttributesMap(Map<String, Object> _originalMap, ELContext context)
        {
            this._originalMap =_originalMap;
            this._beanInfo = (BeanInfo) _originalMap.get(UIComponent.BEANINFO_KEY);
            this._elContext = context;
        }

        @Override
        public ValueExpression getExpression(String name)
        {
            Object valueExpr = _originalMap.get(name);

            // TODO: spec's not clear, I guess this is what we're supposed to do...

            return ((valueExpr instanceof ValueExpression) ? (ValueExpression) valueExpr
                    : null);
        }

        @Override
        public void clear()
        {
            _originalMap.clear();
        }

        @Override
        public boolean containsKey(Object key)
        {
            return _originalMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value)
        {
            return _originalMap.containsValue(value);
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet()
        {
            return _originalMap.entrySet();
        }

        @Override
        public Object get(Object key)
        {
            Object obj = _originalMap.get(key);

            // Per the spec, if the result is a ValueExpression, evaluate it and return the value.

            if (obj != null)
            {
                if (obj instanceof ValueExpression)
                {
                    return ((ValueExpression) obj).getValue(_elContext);
                }
                else
                {
                    return obj;                    
                }
            }
            else
            {
                // TODO: each call to getPropertyDescriptors() create one array.
                // we need to save its value but first we need to cache this whole class
                // at request level.
                for (PropertyDescriptor attribute : _beanInfo.getPropertyDescriptors())
                {
                    if (attribute.getName().equals(key))
                    {
                        obj = attribute.getValue("default");
                        break;
                    }
                    
                }
                if (obj != null && obj instanceof ValueExpression)
                {
                    return ((ValueExpression) obj).getValue(_elContext);
                }
                else
                {
                    return obj;                    
                }
            }
        }

        @Override
        public boolean isEmpty()
        {
            return _originalMap.isEmpty();
        }

        @Override
        public Set<String> keySet()
        {
            return _originalMap.keySet();
        }

        @Override
        public Object put(String key, Object value)
        {
            Object obj = _originalMap.get(key);

            // Per the spec, if the result is a ValueExpression, call setValue().

            if ((obj != null) && (obj instanceof ValueExpression))
            {
                ((ValueExpression) obj).setValue(_elContext, value);

                return null;
            }

            // TODO: spec doesn't say.  I assume we just delegate instead of returning null...
            // -= Leonardo Uribe =- Really this map is used to resolve ValueExpressions 
            // like #{cc.attrs.somekey}, so the value returned is not expected to be used, 
            // but is better to delegate to keep the semantic of this method.
            return _originalMap.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m)
        {
            for (String key : m.keySet())
            {
                put(key, m.get(key));
            }
        }

        @Override
        public Object remove(Object key)
        {
            return _originalMap.remove(key);
        }

        @Override
        public int size()
        {
            return _originalMap.size();
        }

        @Override
        public Collection<Object> values()
        {
            return _originalMap.values();
        }
    }
}
