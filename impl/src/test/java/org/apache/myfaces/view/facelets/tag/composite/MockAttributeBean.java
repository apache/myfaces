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
package org.apache.myfaces.view.facelets.tag.composite;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.validator.ValidatorException;

public class MockAttributeBean
{
    private String value = "value1";

    public String getStyle()
    {
        return "style1";
    }
    
    public String getStyleClass()
    {
        return "styleclass1";
    }
    
    public String getJavaProperty()
    {
        return "javaproperty1";
    }
    
    public String doSomethingFunny(String a)
    {
        return "somethingFunny"+a;
    }
    
    public String doSomeAction()
    {
        return "someAction";
    }
    
    public String getValue()
    {
        return value;
    }
    
    private boolean actionListener1Called = false;
    
    public boolean isActionListener1Called()
    {
        return actionListener1Called;
    }
    
    public void setActionListener1Called(boolean value)
    {
        actionListener1Called = value;
    }
    
    public void doSomeActionListener1()
    {
        actionListener1Called = true;
    }

    private boolean actionListener2Called = false;
    
    public boolean isActionListener2Called()
    {
        return actionListener2Called;
    }
    
    public void setActionListener2Called(boolean value)
    {
        actionListener2Called = value;
    }

    public void doSomeActionListener2(ActionEvent evt)
    {
        actionListener2Called = true;
    }
    
    private boolean valueChangeListener1Called = false;
    
    public boolean isValueChangeListener1Called()
    {
        return valueChangeListener1Called;
    }
    
    public void setValueChangeListener1Called(boolean value)
    {
        valueChangeListener1Called = value;
    }
    
    public void doSomeValueChangeListener1() throws AbortProcessingException
    {
        valueChangeListener1Called = true;
    }

    private boolean valueChangeListener2Called = false;
    
    public boolean isValueChangeListener2Called()
    {
        return valueChangeListener2Called;
    }
    
    public void setValueChangeListener2Called(boolean value)
    {
        valueChangeListener2Called = value;
    }

    public void doSomeValueChangeListener2(ValueChangeEvent evt) throws AbortProcessingException
    {
        valueChangeListener2Called = true;
    }
    
    private boolean validator1Called = false;
    
    public boolean isValidator1Called()
    {
        return validator1Called;
    }
    
    public void setValidator1Called(boolean value)
    {
        validator1Called = value;
    }
    
    public void doSomeValidator1(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        validator1Called = true;
    }

    private ActionListener submitActionListener;
    private ActionListener cancelActionListener;
    
    private boolean submitActionListenerCalled = false;
    private boolean cancelActionListenerCalled = false;
    
    public ActionListener getSubmitActionListener()
    {
        if (submitActionListener == null)
        {
            submitActionListener = new ActionListener(){
    
                public void processAction(ActionEvent actionEvent)
                        throws AbortProcessingException
                {
                    //System.out.println("Submit ActionListener executed");
                    //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Submit ActionListener executed"));
                    submitActionListenerCalled = true;
                }
            };
        }
        return submitActionListener;
    }
    
    public ActionListener getCancelActionListener()
    {
        if (cancelActionListener == null)
        {
            cancelActionListener = new ActionListener()
            {
                
                public void processAction(ActionEvent actionEvent)
                        throws AbortProcessingException
                {
                    //System.out.println("Cancel ActionListener executed");
                    //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cancel ActionListener executed"));
                    cancelActionListenerCalled = true;
                }
            };
        }
        return cancelActionListener;
    }
    
    public String cancelAction()
    {
        return "testActionMethodTypeCancel";
    }
    
    public boolean isSubmitActionListenerCalled()
    {
        return submitActionListenerCalled;
    }

    public boolean isCancelActionListenerCalled()
    {
        return cancelActionListenerCalled;
    }

    public void setSubmitActionListenerCalled(boolean submitActionListenerCalled)
    {
        this.submitActionListenerCalled = submitActionListenerCalled;
    }

    public void setCancelActionListenerCalled(boolean cancelActionListenerCalled)
    {
        this.cancelActionListenerCalled = cancelActionListenerCalled;
    }

    private String name;
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
