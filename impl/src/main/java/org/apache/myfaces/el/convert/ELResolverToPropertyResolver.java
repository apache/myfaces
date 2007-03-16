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

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

/**
 *
 * @author Stan Silvert
 */
public class ELResolverToPropertyResolver extends PropertyResolver {
    
    private ELResolver elResolver;
    
    /**
     * Creates a new instance of ELResolverToPropertyResolver
     */
    public ELResolverToPropertyResolver(ELResolver elResolver) {
        this.elResolver = elResolver;
    }

    public boolean isReadOnly(Object base, int index) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.isReadOnly(elContext(), base, new Integer(index));
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
        
    }
    
    public boolean isReadOnly(Object base, Object property) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.isReadOnly(elContext(), base, property);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
        
    }

    public Object getValue(Object base, int index) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.getValue(elContext(), base, new Integer(index));
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
        
    }

    public Object getValue(Object base, Object property) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.getValue(elContext(), base, property);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }
    
    public Class getType(Object base, int index) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.getType(elContext(), base, new Integer(index));
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }
    
    public Class getType(Object base, Object property) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return elResolver.getType(elContext(), base, property);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }

    public void setValue(Object base, Object property, Object value) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            elResolver.setValue(elContext(), base, property, value);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }

    public void setValue(Object base, int index, Object value) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            elResolver.setValue(elContext(), base, new Integer(index), value);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
        
    }

    private ELContext elContext() {
        return FacesContext.getCurrentInstance().getELContext();
    }
    
}
