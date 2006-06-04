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
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * Wraps a ValueBinding inside a ValueExpression.  Also allows access to
 * the original ValueBinding object.
 *
 * Although ValueExpression implements Serializable, this class implements
 * StateHolder instead.
 *
 * ATTENTION: If you make changes to this class, treat 
 * javax.faces.component._ValueBindingToValueExpression
 * accordingly.
 *
 * @author Stan Silvert
 * @see javax.faces.component._ValueBindingToValueExpression
 */
public class ValueBindingToValueExpression extends ValueExpression implements StateHolder {
    
    
    private static final ExpressionFactory expFactory;
    
    static {
        ApplicationFactory appFactory = 
                    (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = appFactory.getApplication();
        expFactory = application.getExpressionFactory();
    } 
    
    private ValueBinding valueBinding;
    
    private ValueExpression valueExpression;
    
    /**
     * No-arg constructor used during restoreState
     */
    public ValueBindingToValueExpression() {
        
    }
    
    /** Creates a new instance of ValueBindingToValueExpression */
    public ValueBindingToValueExpression(ValueBinding valueBinding) {
        
        if (!(valueBinding instanceof StateHolder)) {
            throw new IllegalArgumentException("valueBinding must be an instance of StateHolder");
        }
        
        this.valueBinding = valueBinding;
        setValueExpression(valueBinding);
    }

    
    private void setValueExpression(ValueBinding valueBinding) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        String expressionString = valueBinding.getExpressionString();
        
        // TODO: figure out if this is right.  It seems to cause problems
        //       if I pass in the EL constant "null" as the expressionString
        if (expressionString == null) expressionString = "";
        
        valueExpression = expFactory.createValueExpression(facesContext.getELContext(), 
                                                           expressionString, 
                                                           valueBinding.getType(facesContext));
    } 
    
    public ValueBinding getValueBinding() {
        return valueBinding;
    }

    public boolean isReadOnly(ELContext context) 
        throws NullPointerException, PropertyNotFoundException, ELException {
            return valueExpression.isReadOnly(context);
    }

    public Object getValue(ELContext context) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        return valueExpression.getValue(context);
    }

    public Class<?> getType(ELContext context) 
        throws NullPointerException, PropertyNotFoundException, ELException {
        
        return valueExpression.getType(context);
    }

    public void setValue(ELContext context, Object value) 
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        
        valueExpression.setValue(context, value);
    }

    public boolean equals(Object obj) {
        return valueExpression.equals(obj);
    }

    public boolean isLiteralText() {
        return valueExpression.isLiteralText();
    }

    public int hashCode() {
        return valueExpression.hashCode();
    }

    public String getExpressionString() {
        return valueExpression.getExpressionString();
    }

    public Class<?> getExpectedType() {
        return valueExpression.getExpectedType();
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] stateArray = (Object[])state;
        try {
            valueBinding = (ValueBinding)Thread.currentThread()
                                               .getContextClassLoader()
                                               .loadClass((String)stateArray[0])
                                               .newInstance();
        } catch (Exception e) {
            throw new FacesException(e);
        }
        
        ((StateHolder)valueBinding).restoreState(context, stateArray[1]);
        setValueExpression(valueBinding);
    }

    public Object saveState(FacesContext context) {
        Object[] state = new Object[2];
        state[0] = valueBinding.getClass().getName();
        state[1] = ((StateHolder)valueBinding).saveState(context);
        return state;
    }

    public void setTransient(boolean newTransientValue) {
        ((StateHolder)valueBinding).setTransient(newTransientValue);
    }

    public boolean isTransient() {
        return ((StateHolder)valueBinding).isTransient();
    }
    
}