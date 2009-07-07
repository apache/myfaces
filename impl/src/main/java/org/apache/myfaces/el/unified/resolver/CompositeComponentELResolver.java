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

import java.beans.FeatureDescriptor;
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

public final class CompositeComponentELResolver extends ELResolver {
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        // Per the spec, return String.class.
        
        return String.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        // Per the spec, do nothing.
        
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        // Per the spec, return null.
        
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // Per the spec: base must not be null, an instance of UIComponent, and a composite
        // component.  Property must be a String.
        
        if ((base != null) && (base instanceof UIComponent) && UIComponent.isCompositeComponent
            ((UIComponent) base) && (property != null)) {
            String propName = property.toString();
            UIComponent baseComponent = (UIComponent) base;
            
            if (propName.equals ("attrs")) {
                // Return a wrapped map that delegates all calls except get() and put().
                
                context.setPropertyResolved (true);
                
                return new AttributesMap (baseComponent.getAttributes(), context);
            }
            
            else if (propName.equals ("parent")) {
                // Return the parent.
                
                context.setPropertyResolved (true);
                
                return UIComponent.getCompositeComponentParent (baseComponent);
            }
        }
        
        // Otherwise, spec says to do nothing (return null).
        
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        // Per the spec, return true.
        
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        // Per the spec, do nothing.
    }
    
    // Wrapper map for composite component attributes.  Follows spec, section 5.6.2.2, table 5-11.
    
    private class AttributesMap implements CompositeComponentExpressionHolder, Map<String, Object> {
        private Map<String, Object> originalMap;
        private ELContext context;
        
        private AttributesMap (Map<String, Object> originalMap, ELContext context) {
            this.originalMap = originalMap;
            this.context = context;
        }
        
        @Override
        public ValueExpression getExpression(String name) {
            Object valueExpr = originalMap.get (name);
            
            // TODO: spec's not clear, I guess this is what we're supposed to do...
            
            return ((valueExpr instanceof ValueExpression) ? (ValueExpression) valueExpr : null);
        }

        @Override
        public void clear() {
            originalMap.clear();
        }

        @Override
        public boolean containsKey(Object key) {
            return originalMap.containsKey (key);
        }

        @Override
        public boolean containsValue(Object value) {
            return originalMap.containsValue (value);
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            return originalMap.entrySet();
        }

        @Override
        public Object get(Object key) {
            Object obj = originalMap.get (key);
            
            // Per the spec, if the result is a ValueExpression, evaluate it and return the value.
            
            if ((obj != null) && (obj instanceof ValueExpression)) {
                return ((ValueExpression) obj).getValue (context);
            }
            
            return obj;
        }

        @Override
        public boolean isEmpty() {
            return originalMap.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return originalMap.keySet();
        }

        @Override
        public Object put(String key, Object value) {
            Object obj = originalMap.get (key);
            
            // Per the spec, if the result is a ValueExpression, call setValue().
            
            if ((obj != null) && (obj instanceof ValueExpression)) {
                ((ValueExpression) obj).setValue (context, value);
                
                return null;
            }
            
            // TODO: spec doesn't say.  I assume we just delegate instead of returning null...
            
            return originalMap.put (key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            for (String key : m.keySet()) {
                put (key, m.get (key));
            }
        }

        @Override
        public Object remove(Object key) {
            return originalMap.remove (key);
        }

        @Override
        public int size() {
            return originalMap.size();
        }

        @Override
        public Collection<Object> values() {
            return originalMap.values();
        }
    }
}
