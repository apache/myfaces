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
package org.apache.myfaces.lifecycle;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewExpiredException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AfterAddToParentEvent;
import javax.faces.event.PhaseId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the Restore View Phase (JSF Spec 2.2.1)
 * 
 * @author Nikolay Petrov
 * @author Bruno Aranda (JSF 1.2)
 * @version $Revision$ $Date$
 * 
 */
class RestoreViewExecutor implements PhaseExecutor
{

    private static final Log log = LogFactory.getLog(RestoreViewExecutor.class);
    private RestoreViewSupport _restoreViewSupport;

    public boolean execute(FacesContext facesContext)
    {
        if (facesContext == null)
        {
            throw new FacesException("FacesContext is null");
        }

        // init the View
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        viewHandler.initView(facesContext);

        UIViewRoot viewRoot = facesContext.getViewRoot();

        RestoreViewSupport restoreViewSupport = getRestoreViewSupport();

        if (viewRoot != null)
        {
            if (log.isTraceEnabled())
                log.trace("View already exists in the FacesContext");

            viewRoot.setLocale(facesContext.getExternalContext().getRequestLocale());
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
            return false;
        }

        String viewId = restoreViewSupport.calculateViewId(facesContext);

        // Determine if this request is a postback or initial request
        if (restoreViewSupport.isPostback(facesContext))
        { // If the request is a postback
            if (log.isTraceEnabled())
                log.trace("Request is a postback");

            // call ViewHandler.restoreView(), passing the FacesContext instance for the current request and the 
            // view identifier, and returning a UIViewRoot for the restored view.
            viewRoot = viewHandler.restoreView(facesContext, viewId);
            if (viewRoot == null)
            {
                // If the return from ViewHandler.restoreView() is null, throw a ViewExpiredException with an 
                // appropriate error message.
                throw new ViewExpiredException("No saved view state could be found for the view identifier: " + viewId,
                    viewId);
            }
            
            // Restore binding
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
            
            // Store the restored UIViewRoot in the FacesContext.
            facesContext.setViewRoot(viewRoot);
        }
        else
        { // If the request is a non-postback
            if (log.isTraceEnabled())
                log.trace("Request is not a postback. New UIViewRoot will be created");

            // call ViewHandler.createView(), passing the FacesContext instance for the current request and 
            // the view identifier
            viewRoot = viewHandler.createView(facesContext, viewId);
            
            // Subscribe the newly created UIViewRoot instance to the AfterAddToParent event, passing the 
            // UIViewRoot instance itself as the listener.
            viewRoot.subscribeToEvent(AfterAddToParentEvent.class, viewRoot);
            
            // Store the new UIViewRoot instance in the FacesContext.
            facesContext.setViewRoot(viewRoot);
            
            // Call renderResponse() on the FacesContext.
            facesContext.renderResponse();
            
            // Publish an AfterAddToParent event with the created UIViewRoot as the event source.
            application.publishEvent(AfterAddToParentEvent.class, viewRoot);
        }

        return false;
    }

    protected RestoreViewSupport getRestoreViewSupport()
    {
        if (_restoreViewSupport == null)
        {
            _restoreViewSupport = new DefaultRestoreViewSupport();
        }
        return _restoreViewSupport;
    }

    /**
     * @param restoreViewSupport
     *            the restoreViewSupport to set
     */
    public void setRestoreViewSupport(RestoreViewSupport restoreViewSupport)
    {
        _restoreViewSupport = restoreViewSupport;
    }

    public PhaseId getPhase()
    {
        return PhaseId.RESTORE_VIEW;
    }
}
