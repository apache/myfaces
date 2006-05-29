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
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/**
 * Converts a MethodBinding to a MethodExpression
 *
 * @author Stan Silvert
 */
public class MethodBindingToMethodExpression extends MethodExpression implements StateHolder {
    
    private static final ExpressionFactory expFactory;
    
    static {
        ApplicationFactory appFactory = 
                    (ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = appFactory.getApplication();
        expFactory = application.getExpressionFactory();
    }
    
    private MethodBinding methodBinding;
    
    private MethodExpression methodExpression;
    private boolean paramTypesKnown = false;
    
    /**
     * No-arg constructor used during restoreState
     */
    public MethodBindingToMethodExpression() {
        
    }
    
    /** Creates a new instance of MethodBindingToMethodExpression */
    public MethodBindingToMethodExpression(MethodBinding methodBinding) {
        this.methodBinding = methodBinding;
        
        if (!(methodBinding instanceof StateHolder)) {
            throw new IllegalArgumentException("methodBinding must be an instance of StateHolder");
        }
        
        // We can't determine the expectecParamTypes from the MethodBinding
        // until someone calls invoke.
        // Therefore, we will just create a new one when invoke is called.
        methodExpression = makeMethodExpression(methodBinding, new Class[0]);
    }
    
    private MethodExpression makeMethodExpression(MethodBinding methodBinding, Class[] expectedParamTypes) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        String expressionString = methodBinding.getExpressionString();
        if (expressionString == null) expressionString = "null";
        return expFactory.createMethodExpression(facesContext.getELContext(), 
                                                 expressionString, 
                                                 methodBinding.getType(facesContext),
                                                 expectedParamTypes);
    }
    
    /**
     * Return the wrapped MethodBinding.
     */
    public MethodBinding getMethodBinding() {
        return methodBinding;
    }

    /**
     * Note: MethodInfo.getParamTypes() may incorrectly return an empty
     * class array if invoke() has not been called.
     *
     * @throws IllegalStateException if expected params types have not been determined.
     */
    public MethodInfo getMethodInfo(ELContext context) 
        throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException {
        
        if (!paramTypesKnown) throw new IllegalStateException("MethodInfo unavailable until invoke is called.");
        
        return methodExpression.getMethodInfo(context);
    }

    public Object invoke(ELContext context, Object[] params) 
        throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException {
        
        if (!paramTypesKnown) {
            Class[] paramTypes = findParamTypes(params);
            methodExpression = makeMethodExpression(methodBinding, paramTypes);
            paramTypesKnown = true;
        }
        
        return methodExpression.invoke(context, params);
    }
    
    private Class[] findParamTypes(Object[] params) {
        Class[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getClass();
        }
        
        return paramTypes;
    }

    public boolean isLiteralText() {
        return methodExpression.isLiteralText();
    }

    public String getExpressionString() {
        return methodExpression.getExpressionString();
    }
    
    public boolean equals(Object obj) {
        return methodExpression.equals(obj);
    }
    
    public int hashCode() {
     return methodExpression.hashCode();
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] stateArray = (Object[])state;
        try {
            methodBinding = (MethodBinding)Thread.currentThread()
                                                 .getContextClassLoader()
                                                 .loadClass((String)stateArray[0])
                                                 .newInstance();
        } catch (Exception e) {
            throw new FacesException(e);
        }
        
        ((StateHolder)methodBinding).restoreState(context, stateArray[1]);
        paramTypesKnown = ((Boolean)stateArray[2]).booleanValue();
        methodExpression = makeMethodExpression(methodBinding, (Class[])stateArray[3]);
    }

    public Object saveState(FacesContext context) {
        Object[] state = new Object[4];
        state[0] = methodBinding.getClass().getName();
        state[1] = ((StateHolder)methodBinding).saveState(context);
        state[2] = Boolean.valueOf(paramTypesKnown);
        state[3] = methodExpression.getMethodInfo(context.getELContext()).getParamTypes();
        return state;
    }

    public void setTransient(boolean newTransientValue) {
        ((StateHolder)methodBinding).setTransient(newTransientValue);
    }

    public boolean isTransient() {
        return ((StateHolder)methodBinding).isTransient();
    }
    
}
