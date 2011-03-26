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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

public class MockAttributeBean
{

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
