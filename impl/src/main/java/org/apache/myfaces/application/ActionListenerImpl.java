/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.myfaces.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.component.ActionSource;
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
public class ActionListenerImpl
    implements ActionListener
{
    private static final Log log = LogFactory.getLog(ActionListenerImpl.class);

    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        ActionSource actionSource = (ActionSource)actionEvent.getComponent();
        MethodBinding methodBinding = actionSource.getAction();

        String fromAction = null;
        String outcome = null;
        if (methodBinding != null)
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
                else
                {
                    throw new FacesException("Error calling action method of component with id " + actionEvent.getComponent().getClientId(facesContext), e);
                }
            }
            catch (RuntimeException e)
            {
                throw new FacesException("Error calling action method of component with id " + actionEvent.getComponent().getClientId(facesContext), e);
            }
        }

        NavigationHandler navigationHandler = application.getNavigationHandler();
        navigationHandler.handleNavigation(facesContext,
                                           fromAction,
                                           outcome);
		//Render Response if needed
		facesContext.renderResponse();

    }
}
