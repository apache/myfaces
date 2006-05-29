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

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

/**
 * Converter for legacy ValueBinding objects.  See JSF 1.2 section 5.8.3
 *
 * @author Stan Silvert
 */
public class ValueExpressionToValueBinding extends ValueBinding implements StateHolder {
    private static final ExpressionFactory expFactory;
    
    static {
        ApplicationFactory appFactory = 
                    (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = appFactory.getApplication();
        expFactory = application.getExpressionFactory();
    }
    
    private ValueExpression valueExpression;
    
    private boolean isTransient = false;
    
    // required no-arg constructor for StateHolder
    public ValueExpressionToValueBinding() {
        valueExpression = null;
    }
    
    /** Creates a new instance of ValueExpressionToValueBinding */
    public ValueExpressionToValueBinding(ValueExpression valueExpression) {
        this.valueExpression = valueExpression;
    }

    public void setValue(FacesContext facesContext, Object value) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            valueExpression.setValue(facesContext.getELContext(), value);
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }

    public boolean isReadOnly(FacesContext facesContext) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return valueExpression.isReadOnly(facesContext.getELContext());
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
        
    }

    public Object getValue(FacesContext facesContext) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return valueExpression.getValue(facesContext.getELContext());
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }

    public Class getType(FacesContext facesContext) 
        throws EvaluationException, PropertyNotFoundException {
        
        try {
            return valueExpression.getExpectedType();
        } catch (javax.el.PropertyNotFoundException e) {
            throw new javax.faces.el.PropertyNotFoundException(e);
        } catch (ELException e) {
            throw new EvaluationException(e);
        }
    }

// -------- StateHolder methods -------------------------------------------
    
    public void restoreState(FacesContext facesContext, Object state) {
        Object[] stateArray = (Object[])state;
        String expressionString = (String)stateArray[0];
        Class type = (Class)stateArray[1];
        valueExpression = expFactory.createValueExpression(facesContext.getELContext(), 
                                                           expressionString, 
                                                           type);
    }

    public Object saveState(FacesContext context) {
        Object[] stateArray = new Object[3];
        stateArray[0] = valueExpression.getExpressionString();
        stateArray[1] = valueExpression.getExpectedType();
        return stateArray;
    }

    public void setTransient(boolean newTransientValue) {
        isTransient = newTransientValue;
    }

    public boolean isTransient() {
        return isTransient;
    }
    
}
