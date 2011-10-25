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
package org.apache.myfaces.application;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class ActionListenerImpl implements ActionListener
{
    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        UIComponent component = actionEvent.getComponent();
        
        MethodExpression methodExpression = null;
        MethodBinding methodBinding = null;
        
        String fromAction = null;
        String outcome = null;
        
        if (component instanceof ActionSource2)
        {
            // Must be an instance of ActionSource2, so don't look on action if the actionExpression is set 
            methodExpression = ((ActionSource2) component).getActionExpression();            
        }
        if (methodExpression == null && component instanceof ActionSource)
        {
            // Backwards compatibility for pre-1.2.
            methodBinding = ((ActionSource) component).getAction();
        }
        
        if (methodExpression != null)
        {
            fromAction = methodExpression.getExpressionString();
            try
            {
                Object objOutcome = methodExpression.invoke(facesContext.getELContext(), null);
                if (objOutcome != null)
                {
                    outcome = objOutcome.toString();
                }
            }
            catch (ELException e)
            {
                // "... If that fails for any reason, throw an AbortProcessingException,
                // including the cause of the failure ..."
                // -= Leonardo Uribe =- after discussing this topic on MYFACES-3199,
                // the conclusion is the part is an advice
                // for the developer implementing a listener in a method expressions
                // that could be wrapped by this class.
                // The spec wording is poor but, to keep this coherently with
                // ExceptionHandler API, the spec and code on UIViewRoot we need:
                // 2a) "exception is instance of APE or any of the causes of
                // the exception are an APE,
                // DON'T publish ExceptionQueuedEvent and terminate processing for current event".
                // 2b) for any other exception publish ExceptionQueuedEvent and continue broadcast processing.
                Throwable cause = e.getCause();
                AbortProcessingException ape = null;
                if (cause != null)
                {
                    do
                    {
                        if (cause != null && cause instanceof AbortProcessingException)
                        {
                            ape = (AbortProcessingException) cause;
                            break;
                        }
                        cause = cause.getCause();
                    }
                    while (cause != null);
                }
                
                if (ape != null)
                {
                    // 2a) "exception is instance of APE or any of the causes of the exception are an APE, 
                    // DON'T publish ExceptionQueuedEvent and terminate processing for current event".
                    // To do this throw an AbortProcessingException here, later on UIViewRoot.broadcastAll,
                    // this exception will be received and stored to handle later.
                    throw ape;
                }
                
                // Since this ActionListener is the one who handles navigation, if we have another different exception
                // here we can't queue it on ExceptionHandler stack, instead throw it as a FacesException.
                throw new FacesException("Error calling action method of component with id "
                                         + actionEvent.getComponent().getClientId(facesContext), e);
            }
            catch (RuntimeException e)
            {
                throw new FacesException("Error calling action method of component with id "
                                         + actionEvent.getComponent().getClientId(facesContext), e);
            }
        }
        
        else if (methodBinding != null)
        {
            fromAction = methodBinding.getExpressionString();
            try
            {
                Object objOutcome = methodBinding.invoke(facesContext, null);

                if (objOutcome != null)
                {
                    outcome = objOutcome.toString();
                }
            }
            catch (EvaluationException e)
            {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof AbortProcessingException)
                {
                    throw (AbortProcessingException)cause;
                }
   
                throw new FacesException("Error calling action method of component with id "
                                         + actionEvent.getComponent().getClientId(facesContext), e);
                
            }
            catch (RuntimeException e)
            {
                throw new FacesException("Error calling action method of component with id "
                                         + actionEvent.getComponent().getClientId(facesContext), e);
            }
        }
        
        NavigationHandler navigationHandler = application.getNavigationHandler();
        navigationHandler.handleNavigation(facesContext, fromAction, outcome);
        //Render Response if needed
        facesContext.renderResponse();

    }
}
