/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.el.convert;

import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.el.PropertyResolver;

/**
 * Wrapper that converts a VariableResolver into an ELResolver.
 * See JSF 1.2 spec section 5.6.1.6
 *
 * @author Stan Silvert
 */
public class PropertyResolverToELResolver extends ELResolver {
    private PropertyResolver propertyResolver;
    
    private ExpressionFactory expressionFactory;
    
    /**
     * Creates a new instance of PropertyResolverToELResolver
     */
    public PropertyResolverToELResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
        
        ApplicationFactory appFactory = (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        this.expressionFactory = appFactory.getApplication().getExpressionFactory();
    }

    public void setValue(ELContext context, Object base, Object property, Object value) 
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        
        if ( (base == null) || (property == null)) return;
        
        context.setPropertyResolved(true);

        try {
            if (needsCoersion(base)) {
               propertyResolver.setValue(base, coerceToInt(property), value);
               return;
            }

            propertyResolver.setValue(base, property, value);
            
        } catch (Exception e) {
            context.setPropertyResolved(false);
            throw new ELException(e);
        }
        
    }

    public boolean isReadOnly(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if ( (base == null) || (property == null)) return true;
        
        context.setPropertyResolved(true);

        try {
            if (needsCoersion(base)) {
               return propertyResolver.isReadOnly(base, coerceToInt(property));
            }

            return propertyResolver.isReadOnly(base, property);
            
        } catch (Exception e) {
            context.setPropertyResolved(false);
            throw new ELException(e);
        }
    }
    
    public Object getValue(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if ( (base == null) || (property == null)) return null;
        
        context.setPropertyResolved(true);

        try {
            if (needsCoersion(base)) {
               return propertyResolver.getValue(base, coerceToInt(property));
            }

            return propertyResolver.getValue(base, property);
            
        } catch (Exception e) {
            context.setPropertyResolved(false);
            throw new ELException(e);
        }
    }

    public Class<?> getType(ELContext context, Object base, Object property) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        if ( (base == null) || (property == null)) return null;
        
        context.setPropertyResolved(true);

        try {
            if (needsCoersion(base)) {
               return propertyResolver.getType(base, coerceToInt(property));
            }

            return propertyResolver.getType(base, property);
            
        } catch (Exception e) {
            context.setPropertyResolved(false);
            throw new ELException(e);
        }
    }
    
    public Iterator getFeatureDescriptors(ELContext context, Object base) {
        
        return null;
    }

    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        
        if (base == null) return null;
        
        return Object.class;
    }
    
    private boolean needsCoersion(Object base) {
        return (base instanceof List) || base.getClass().isArray();
    }
    
    private int coerceToInt(Object property) throws Exception {
        Integer coerced = (Integer)expressionFactory.coerceToType(property, Integer.class);
        return coerced.intValue();
    }
    
}
