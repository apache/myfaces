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
package org.apache.myfaces.mock;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;

import junit.framework.Assert;

/**
 * Verifies the current component on stack in method getValue. Created for MYFACES-3157
 */
public class MockRenderedValueExpression extends org.apache.myfaces.test.el.MockValueExpression {

    private final UIComponent toVerify;
    
    private final Object value;

    /**
     * @param toVerify UIComponent instance verified in getValue 
     * @param value value returned from getValue()
     */
    public MockRenderedValueExpression(String expression, Class<?> expectedType, UIComponent toVerify, Object value) {
        super(expression, expectedType);
        this.toVerify = toVerify;
        this.value = value;
    }
    
    @Override
    public Object getValue(ELContext elContext) {
      FacesContext facesContext = (FacesContext) elContext.getContext(FacesContext.class);
      UIComponent currentComponent = UIComponent.getCurrentComponent(facesContext);
      Assert.assertEquals("If this ValueExpression is evaluated, component on stack must be actual" , currentComponent , toVerify);
      return value;
    }

    /**
     * Sets up a simple parent/child for testing  MYFACES-3157
     */
    public static UIComponent setUpComponentStack(FacesContext facesContext, UIComponent underTest, Object value) {
        UIPanel parent = new UIPanel();
        parent.getChildren().add(underTest);
        
        MockRenderedValueExpression ve = new MockRenderedValueExpression("#{component.id eq 'testId'}", Boolean.class, underTest, value);
        underTest.setValueExpression("rendered", ve);
        
       // simlulate that parent panel encodes children and is on the stack:
       parent.pushComponentToEL(facesContext, null);
       return parent;
    }
}
