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
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.CompositeComponentExpressionHolder;

/**
 * Composite component attribute EL resolver.  See JSF spec, section 5.6.2.2.
 */

public final class CompositeComponentELResolver extends ELResolver
{
    private static final String ATTRIBUTES_MAP = "attrs".intern();
    
    private static final String PARENT_COMPOSITE_COMPONENT = "parent".intern();
    
    private static final String COMPOSITE_COMPONENT_ATTRIBUTES_MAPS = 
        "org.apache.myfaces.COMPOSITE_COMPONENT_ATTRIBUTES_MAPS";
    //private static final String COMPOSITE_COMPONENT_GET_VALUE_EXPRESSION =
    //    "org.apache.myfaces.COMPOSITE_COMPONENT_GET_VALUE_EXPRESSION";
    
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

            if (propName.equals(ATTRIBUTES_MAP))
            {
                // Return a wrapped map that delegates all calls except get() and put().

                context.setPropertyResolved(true);

                return _getCompositeComponentAttributesMapWrapper(baseComponent, context);
            }

            else if (propName.equals(PARENT_COMPOSITE_COMPONENT))
            {
                // Return the parent.

                context.setPropertyResolved(true);

                return UIComponent.getCompositeComponentParent(baseComponent);
            }
        }

        // Otherwise, spec says to do nothing (return null).

        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> _getCompositeComponentAttributesMapWrapper(
            UIComponent baseComponent, ELContext elContext)
    {
        Map<Object, Object> contextMap = (Map<Object, Object>) facesContext(
                elContext).getAttributes();

        // We use a WeakHashMap<UIComponent, WeakReference<Map<String, Object>>> to
        // hold attribute map wrappers by two reasons:
        //
        // 1. The wrapper is used multiple times for a very short amount of time (in fact on current request).
        // 2. The original attribute map has an inner reference to UIComponent, so we need to wrap it
        //    with WeakReference.
        //
        Map<UIComponent, WeakReference<Map<String, Object>>> compositeComponentAttributesMaps = 
            (Map<UIComponent, WeakReference<Map<String, Object>>>) contextMap
                .get(COMPOSITE_COMPONENT_ATTRIBUTES_MAPS);

        Map<String, Object> attributesMap = null;
        WeakReference<Map<String, Object>> weakReference;
        if (compositeComponentAttributesMaps != null)
        {
            weakReference = compositeComponentAttributesMaps.get(baseComponent);
            if (weakReference != null)
            {
                attributesMap = weakReference.get();                
            }
            if (attributesMap == null)
            {
                //create a wrapper map
                attributesMap = new CompositeComponentAttributesMapWrapper(
                        baseComponent, elContext);
                compositeComponentAttributesMaps.put(baseComponent,
                        new WeakReference<Map<String, Object>>(attributesMap));
            }
        }
        else
        {
            //Create both required maps
            attributesMap = new CompositeComponentAttributesMapWrapper(
                    baseComponent, elContext);
            compositeComponentAttributesMaps = new WeakHashMap<UIComponent, WeakReference<Map<String, Object>>>();
            compositeComponentAttributesMaps.put(baseComponent,
                    new WeakReference<Map<String, Object>>(attributesMap));
            contextMap.put(COMPOSITE_COMPONENT_ATTRIBUTES_MAPS,
                    compositeComponentAttributesMaps);
        }
        return attributesMap;
    }
    
    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext)context.getContext(FacesContext.class);
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
    private final class CompositeComponentAttributesMapWrapper 
            implements CompositeComponentExpressionHolder, Map<String, Object>
    {

        private final UIComponent _component;
        private final BeanInfo _beanInfo;
        private final Map<String, Object> _originalMap;
        private final ELContext _elContext;
        private final PropertyDescriptor [] _propertyDescriptors;

        private CompositeComponentAttributesMapWrapper(UIComponent component, ELContext context)
        {
            this._component = component;
            this._originalMap = component.getAttributes();
            this._beanInfo = (BeanInfo) _originalMap.get(UIComponent.BEANINFO_KEY);
            this._elContext = context;
            this._propertyDescriptors = _beanInfo.getPropertyDescriptors();
        }

        public ValueExpression getExpression(String name)
        {
            ValueExpression valueExpr = _component.getValueExpression(name);

            return ((valueExpr instanceof ValueExpression) 
                    ? (ValueExpression) valueExpr : null);
        }

        public void clear()
        {
            _originalMap.clear();
        }

        public boolean containsKey(Object key)
        {
            return _originalMap.containsKey(key);
        }

        public boolean containsValue(Object value)
        {
            return _originalMap.containsValue(value);
        }

        public Set<java.util.Map.Entry<String, Object>> entrySet()
        {
            return _originalMap.entrySet();
        }

        public Object get(Object key)
        {
            Object obj = _originalMap.get(key);
            if (obj != null)
            {
                // _originalMap is a _ComponentAttributesMap and thus any
                // ValueExpressions will be evaluated by the call to
                // _originalMap.get(). The only case in which we really will
                // get a ValueExpression here is when a ValueExpression itself
                // is stored as an attribute. But in this case we really want to 
                // get the ValueExpression. So we don't have to evaluate possible
                // ValueExpressions here, but can return obj directly.
                return obj;
            }
            else
            {
                for (PropertyDescriptor attribute : _propertyDescriptors)
                {
                    if (attribute.getName().equals(key))
                    {
                        obj = attribute.getValue("default");
                        break;
                    }
                }
                // We have to check for a ValueExpression and also evaluate it
                // here, because in the PropertyDescriptor the default values are
                // always stored as (Tag-)ValueExpressions.
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
        
        public boolean isEmpty()
        {
            return _originalMap.isEmpty();
        }

        public Set<String> keySet()
        {
            return _originalMap.keySet();
        }

        public Object put(String key, Object value)
        {
            ValueExpression valueExpression = _component.getValueExpression(key);
            
            // Per the spec, if the result is a ValueExpression, call setValue().
            if (valueExpression != null)
            {
                valueExpression.setValue(_elContext, value);

                return null;
            }

            // Really this map is used to resolve ValueExpressions like 
            // #{cc.attrs.somekey}, so the value returned is not expected to be used, 
            // but is better to delegate to keep the semantic of this method.
            return _originalMap.put(key, value);
        }

        public void putAll(Map<? extends String, ? extends Object> m)
        {
            for (String key : m.keySet())
            {
                put(key, m.get(key));
            }
        }

        public Object remove(Object key)
        {
            return _originalMap.remove(key);
        }

        public int size()
        {
            return _originalMap.size();
        }

        public Collection<Object> values()
        {
            return _originalMap.values();
        }
    }
}
