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

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.CompositeComponentExpressionHolder;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.util.lang.StringUtils;
import org.apache.myfaces.view.facelets.tag.composite.CompositeComponentBeanInfo;

/**
 * Composite component attribute EL resolver.  See Faces spec, section 5.6.2.2.
 */

public final class CompositeComponentELResolver extends ELResolver
{
    private static final String ATTRIBUTES_MAP = "attrs";
 
    private static final String PARENT_COMPOSITE_COMPONENT = "parent";
    
    private static final String COMPOSITE_COMPONENT_ATTRIBUTES_MAPS = 
        "org.apache.myfaces.COMPOSITE_COMPONENT_ATTRIBUTES_MAPS";
    
    private MyfacesConfig myfacesConfig;
    
    public CompositeComponentELResolver(MyfacesConfig myfacesConfig)
    {
        this.myfacesConfig = myfacesConfig;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        // Per the spec, return String.class.
        return String.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        // Per the spec, do nothing.
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        if (base != null
                && property != null
                && base instanceof CompositeComponentAttributesMapWrapper
                && property instanceof String)
        {
            FacesContext facesContext = facesContext(context);
            if (facesContext == null)
            {
                facesContext = FacesContext.getCurrentInstance();
            }
            if (facesContext == null)
            {
                return null;
            }

            if (!myfacesConfig.isStrictJsf2CCELResolver())
            {
                // handle Faces 2.2 spec revisions:
                // code resembles that found in Mojarra because it originates from
                // the same contributor, whose ICLA is on file
                Class<?> exprType = null;
                Class<?> metaType = null;

                CompositeComponentAttributesMapWrapper evalMap = (CompositeComponentAttributesMapWrapper) base;
                ValueExpression ve = evalMap.getExpression((String) property);
                if (ve != null)
                {
                    exprType = ve.getType(context);
                }

                if (StringUtils.isNotBlank((String) property))
                {
                    if (evalMap.propertyDescriptors != null)
                    {
                        for (PropertyDescriptor pd : evalMap.propertyDescriptors)
                        {
                            if (property.equals(pd.getName()))
                            {
                                metaType = resolveType(context, pd);
                                break;
                            }
                        }
                    }
                }
                if (metaType != null)
                {
                    // override exprType only if metaType is narrower:
                    if (exprType == null || exprType.isAssignableFrom(metaType))
                    {
                        context.setPropertyResolved(true);
                        return metaType;
                    }
                }
                return exprType;
            }
        }

        // Per the spec, return null.
        return null;
    }

    // adapted from CompositeMetadataTargetImpl#getPropertyType():
    private static Class<?> resolveType(ELContext context, PropertyDescriptor pd)
    {
        if (pd != null)
        {
            Object type = pd.getValue("type");
            if (type != null)
            {
                type = ((ValueExpression)type).getValue(context);
                if (type instanceof String)
                {
                    try
                    {
                        type = ClassUtils.javaDefaultTypeToClass((String)type);
                    }
                    catch (ClassNotFoundException e)
                    {
                        type = null;
                    }
                }
                return (Class<?>) type;
            }
            return pd.getPropertyType();
        }

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        // Per the spec: base must not be null, an instance of UIComponent, and a composite
        // component.  Property must be a String.

        if (base != null
                && base instanceof UIComponent
                && property != null
                && UIComponent.isCompositeComponent((UIComponent) base))
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
        Map<Object, Object> contextMap = (Map<Object, Object>) facesContext(elContext).getAttributes();

        // We use a WeakHashMap<UIComponent, WeakReference<Map<String, Object>>> to
        // hold attribute map wrappers by two reasons:
        //
        // 1. The wrapper is used multiple times for a very short amount of time (in fact on current request).
        // 2. The original attribute map has an inner reference to UIComponent, so we need to wrap it
        //    with WeakReference.
        //
        Map<UIComponent, WeakReference<Map<String, Object>>> compositeComponentAttributesMaps = 
            (Map<UIComponent, WeakReference<Map<String, Object>>>) contextMap.get(COMPOSITE_COMPONENT_ATTRIBUTES_MAPS);

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
                attributesMap = new CompositeComponentAttributesMapWrapper(baseComponent);
                compositeComponentAttributesMaps.put(baseComponent, new WeakReference<>(attributesMap));
            }
        }
        else
        {
            //Create both required maps
            attributesMap = new CompositeComponentAttributesMapWrapper(baseComponent);
            compositeComponentAttributesMaps = new WeakHashMap<>();
            compositeComponentAttributesMaps.put(baseComponent, new WeakReference<>(attributesMap));
            contextMap.put(COMPOSITE_COMPONENT_ATTRIBUTES_MAPS, compositeComponentAttributesMaps);
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
        private final UIComponent component;
        private final BeanInfo beanInfo;
        private final Map<String, Object> originalMap;
        private final PropertyDescriptor[] propertyDescriptors;
        private final CompositeComponentBeanInfo ccBeanInfo;

        private CompositeComponentAttributesMapWrapper(UIComponent component)
        {
            this.component = component;
            this.originalMap = component.getAttributes();
            this.beanInfo = (BeanInfo) originalMap.get(UIComponent.BEANINFO_KEY);
            this.propertyDescriptors = beanInfo.getPropertyDescriptors();
            this.ccBeanInfo = beanInfo instanceof CompositeComponentBeanInfo
                    ? (CompositeComponentBeanInfo) beanInfo
                    : null;
        }

        @Override
        public ValueExpression getExpression(String name)
        {
            ValueExpression ve = component.getValueExpression(name);
            return ve;
        }

        @Override
        public void clear()
        {
            originalMap.clear();
        }

        @Override
        public boolean containsKey(Object key)
        {
            boolean value = originalMap.containsKey(key);
            if (value)
            {
                return value;
            }
 
            if (ccBeanInfo != null)
            {
                PropertyDescriptor attribute = ccBeanInfo.getPropertyDescriptorsMap().get((String) key);
                if (attribute != null)
                {
                    return attribute.getValue("default") != null;
                }
            }
            else
            {
                for (PropertyDescriptor attribute : propertyDescriptors)
                {
                    if (attribute.getName().equals(key))
                    {
                        return attribute.getValue("default") != null;
                    }
                }
            }
 
            return false;
        }

        @Override
        public boolean containsValue(Object value)
        {
            return originalMap.containsValue(value);
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet()
        {
            return originalMap.entrySet();
        }

        @Override
        public Object get(Object key)
        {
            Object obj = originalMap.get(key);
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

            if (ccBeanInfo != null)
            {
                PropertyDescriptor attribute = ccBeanInfo.getPropertyDescriptorsMap().get(key);
                if (attribute != null)
                {
                    obj = attribute.getValue("default");
                }
            }
            else
            {
                for (PropertyDescriptor attribute : propertyDescriptors)
                {
                    if (attribute.getName().equals(key))
                    {
                        obj = attribute.getValue("default");
                        break;
                    }
                }
            }

            // We have to check for a ValueExpression and also evaluate it
            // here, because in the PropertyDescriptor the default values are
            // always stored as (Tag-)ValueExpressions.
            if (obj != null && obj instanceof ValueExpression)
            {
                return ((ValueExpression) obj).getValue(FacesContext.getCurrentInstance().getELContext());
            }

            return obj;
        }
        
        @Override
        public boolean isEmpty()
        {
            return originalMap.isEmpty();
        }

        @Override
        public Set<String> keySet()
        {
            return originalMap.keySet();
        }

        @Override
        public Object put(String key, Object value)
        {
            ValueExpression valueExpression = component.getValueExpression(key);
            
            // Per the spec, if the result is a ValueExpression, call setValue().
            if (valueExpression != null)
            {
                valueExpression.setValue(FacesContext.getCurrentInstance().getELContext(), value);
                return null;
            }

            // Really this map is used to resolve ValueExpressions like 
            // #{cc.attrs.somekey}, so the value returned is not expected to be used, 
            // but is better to delegate to keep the semantic of this method.
            return originalMap.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m)
        {
            for (Entry<? extends String, ? extends Object> entry : m.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Object remove(Object key)
        {
            return originalMap.remove(key);
        }

        @Override
        public int size()
        {
            return originalMap.size();
        }

        @Override
        public Collection<Object> values()
        {
            return originalMap.values();
        }
    }
}
