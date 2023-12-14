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

import jakarta.el.MethodExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.PhaseId;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.apache.myfaces.view.facelets.pool.ViewPool;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class ActionListenerImpl implements ActionListener
{
    @Override
    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        FacesContext facesContext = actionEvent.getFacesContext();
        Application application = facesContext.getApplication();
        UIComponent component = actionEvent.getComponent();
        
        MethodExpression methodExpression = null;
        
        String fromAction = null;
        String outcome = null;
        
        if (component instanceof ActionSource source)
        {
            // Must be an instance of ActionSource, so don't look on action if the actionExpression is set 
            methodExpression = source.getActionExpression();            
        }

        if (methodExpression != null)
        {
            fromAction = methodExpression.getExpressionString();

            Object objOutcome = methodExpression.invoke(facesContext.getELContext(), null);
            if (objOutcome != null)
            {
                outcome = objOutcome.toString();
            }
            
        }
        
        UIViewRoot root = facesContext.getViewRoot();
        ViewPoolProcessor processor = ViewPoolProcessor.getInstance(facesContext);
        ViewPool pool = (processor != null) ? processor.getViewPool(facesContext, root) : null;
        if (pool != null
                && pool.isDeferredNavigationEnabled()
                && processor.isViewPoolStrategyAllowedForThisView(facesContext, root)
                && (PhaseId.INVOKE_APPLICATION.equals(facesContext.getCurrentPhaseId())
                    || PhaseId.APPLY_REQUEST_VALUES.equals(facesContext.getCurrentPhaseId())))
        {
            NavigationHandler navigationHandler = application.getNavigationHandler();
            if (navigationHandler instanceof ConfigurableNavigationHandler handler)
            {
                NavigationCase navigationCase = handler.
                    getNavigationCase(facesContext, fromAction, outcome);
                if (navigationCase != null)
                {
                    // Deferred invoke navigation. The first one wins
                    if (!facesContext.getAttributes().containsKey(ViewPoolProcessor.INVOKE_DEFERRED_NAVIGATION))
                    {
                        String toFlowDocumentId = (component != null) ? 
                            (String) component.getAttributes().get(ActionListener.TO_FLOW_DOCUMENT_ID_ATTR_NAME) : null;
                        if (toFlowDocumentId != null)
                        {
                            facesContext.getAttributes().put(ViewPoolProcessor.INVOKE_DEFERRED_NAVIGATION, 
                                    new Object[]{fromAction, outcome, toFlowDocumentId});
                        }
                        else
                        {
                            facesContext.getAttributes().put(ViewPoolProcessor.INVOKE_DEFERRED_NAVIGATION, 
                                    new Object[]{fromAction, outcome});
                        }
                    }
                }
            }
        }
        else
        {
            NavigationHandler navigationHandler = application.getNavigationHandler();
            String toFlowDocumentId = (component != null) ? 
                (String) component.getAttributes().get(ActionListener.TO_FLOW_DOCUMENT_ID_ATTR_NAME) : null;

            if (toFlowDocumentId != null)
            {
                navigationHandler.handleNavigation(facesContext, fromAction, outcome, toFlowDocumentId);
            }
            else
            {
                navigationHandler.handleNavigation(facesContext, fromAction, outcome);
            }
            //Render Response if needed
            facesContext.renderResponse();
        }
    }
}
