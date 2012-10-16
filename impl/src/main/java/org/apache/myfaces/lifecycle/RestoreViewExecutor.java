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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewExpiredException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.renderkit.ErrorPageWriter;

/**
 * Implements the Restore View Phase (JSF Spec 2.2.1)
 * 
 * @author Nikolay Petrov (latest modification by $Author$)
 * @author Bruno Aranda (JSF 1.2)
 * @version $Revision$ $Date$
 */
class RestoreViewExecutor extends PhaseExecutor
{

    //private static final Log log = LogFactory.getLog(RestoreViewExecutor.class);
    private static final Logger log = Logger.getLogger(RestoreViewExecutor.class.getName());
    
    private RestoreViewSupport _restoreViewSupport;
    
    @Override
    public void doPrePhaseActions(FacesContext facesContext)
    {
        // Call initView() on the ViewHandler. 
        // This will set the character encoding properly for this request.
        // Note that we are doing this here, because we need the character encoding
        // to be set as early as possible (before any PhaseListener is executed).
        facesContext.getApplication().getViewHandler().initView(facesContext);
    }

    public boolean execute(FacesContext facesContext)
    {
        if (facesContext == null)
        {
            throw new FacesException("FacesContext is null");
        }

        // get some required Objects
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        RestoreViewSupport restoreViewSupport = getRestoreViewSupport(facesContext);

        // Examine the FacesContext instance for the current request. If it already contains a UIViewRoot
        if (viewRoot != null)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("View already exists in the FacesContext");
            }
            
            // Set the locale on this UIViewRoot to the value returned by the getRequestLocale() method on the
            // ExternalContext for this request
            viewRoot.setLocale(facesContext.getExternalContext().getRequestLocale());
            
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
            
            // invoke the afterPhase MethodExpression of UIViewRoot
            _invokeViewRootAfterPhaseListener(facesContext);
            
            return false;
        }
        
        String viewId = restoreViewSupport.calculateViewId(facesContext);

        // Determine if the current request is an attempt by the 
        // servlet container to display an error page.
        // If the request is an error page request, the servlet container
        // is required to set the request parameter "javax.servlet.error.message".
        final boolean errorPageRequest = facesContext.getExternalContext().getRequestMap()
                                                 .get("javax.servlet.error.message") != null;
        
        // Determine if this request is a postback or an initial request.
        // But if it is an error page request, do not treat it as a postback (since 2.0)
        if (!errorPageRequest && restoreViewSupport.isPostback(facesContext))
        { // If the request is a postback
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Request is a postback");
            }

            try
            {
                facesContext.setProcessingEvents(false);
                // call ViewHandler.restoreView(), passing the FacesContext instance for the current request and the 
                // view identifier, and returning a UIViewRoot for the restored view.
                viewRoot = viewHandler.restoreView(facesContext, viewId);
                if (viewRoot == null)
                {
                    // If the return from ViewHandler.restoreView() is null, throw a ViewExpiredException with an 
                    // appropriate error message.
                    throw new ViewExpiredException("No saved view state could be found for the view identifier: "
                                                   + viewId, viewId);
                }
                
                // Store the restored UIViewRoot in the FacesContext.
                facesContext.setViewRoot(viewRoot);
            }
            finally
            {
                facesContext.setProcessingEvents(true);
            }
            
            // Restore binding
            // See https://javaserverfaces-spec-public.dev.java.net/issues/show_bug.cgi?id=806
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
        }
        else
        { // If the request is a non-postback
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("Request is not a postback. New UIViewRoot will be created");
            }
            
            //viewHandler.deriveViewId(facesContext, viewId)
            //restoreViewSupport.deriveViewId(facesContext, viewId)
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, 
                    viewHandler.deriveLogicalViewId(facesContext, viewId));
            
            // viewHandler.deriveLogicalViewId() could trigger an InvalidViewIdException, which
            // it is handled internally sending a 404 error code set the response as complete.
            if (facesContext.getResponseComplete())
            {
                return true;
            }
            
            if (vdl != null)
            {
                ViewMetadata metadata = vdl.getViewMetadata(facesContext, viewId);
                
                Collection<UIViewParameter> viewParameters = null;
                
                if (metadata != null)
                {
                    viewRoot = metadata.createMetadataView(facesContext);
                    
                    if (viewRoot != null)
                    {
                        viewParameters = ViewMetadata.getViewParameters(viewRoot);
                    }
                    else if(facesContext.getResponseComplete())
                    {
                        // this can happen if the current request is a debug request,
                        // in this case no further processing is necessary
                        return true;
                    }
                }
    
                // If viewParameters is not an empty collection DO NOT call renderResponse
                if ( !(viewParameters != null && !viewParameters.isEmpty()) )
                {
                    // Call renderResponse() on the FacesContext.
                    facesContext.renderResponse();
                }
            }
            else
            {
                // Call renderResponse
                facesContext.renderResponse();
            }
            
            // viewRoot can be null here, if ...
            //   - we don't have a ViewDeclarationLanguage (e.g. when using facelets-1.x)
            //   - there is no view metadata or metadata.createMetadataView() returned null
            if (viewRoot == null)
            {
                // call ViewHandler.createView(), passing the FacesContext instance for the current request and 
                // the view identifier
                viewRoot = viewHandler.createView(facesContext, viewId);
            }
            
            // Subscribe the newly created UIViewRoot instance to the AfterAddToParent event, passing the 
            // UIViewRoot instance itself as the listener.
            // -= Leonardo Uribe =- This line it is not necessary because it was
            // removed from jsf 2.0 section 2.2.1 when pass from EDR2 to Public Review 
            // viewRoot.subscribeToEvent(PostAddToViewEvent.class, viewRoot);
            
            // Store the new UIViewRoot instance in the FacesContext.
            facesContext.setViewRoot(viewRoot);
            
            // Publish an AfterAddToParent event with the created UIViewRoot as the event source.
            application.publishEvent(facesContext, PostAddToViewEvent.class, viewRoot);
        }

        // add the ErrorPageBean to the view map to fully support 
        // facelet error pages, if we are in ProjectStage Development
        // and currently generating an error page
        if (errorPageRequest && facesContext.isProjectStage(ProjectStage.Development))
        {
            facesContext.getViewRoot().getViewMap()
                    .put(ErrorPageWriter.ERROR_PAGE_BEAN_KEY, new ErrorPageWriter.ErrorPageBean());
        }
        
        // invoke the afterPhase MethodExpression of UIViewRoot
        _invokeViewRootAfterPhaseListener(facesContext);
        
        return false;
    }
    
    /**
     * Invoke afterPhase MethodExpression of UIViewRoot.
     * Note: In this phase it is not possible to invoke the beforePhase method, because we
     * first have to restore the view to get its attributes. Also it is not really possible
     * to call the afterPhase method inside of UIViewRoot for this phase, thus it was decided
     * in the JSF 2.0 spec rev A to put this here.
     * @param facesContext
     */
    private void _invokeViewRootAfterPhaseListener(FacesContext facesContext)
    {
        // get the UIViewRoot (note that it must not be null at this point)
        UIViewRoot root = facesContext.getViewRoot();
        MethodExpression afterPhaseExpression = root.getAfterPhaseListener();
        if (afterPhaseExpression != null)
        {
            PhaseEvent event = new PhaseEvent(facesContext, getPhase(), _getLifecycle(facesContext));
            try
            {
                afterPhaseExpression.invoke(facesContext.getELContext(), new Object[] { event });
            }
            catch (Throwable t) 
            {
                log.log(Level.SEVERE, "An Exception occured while processing " +
                        afterPhaseExpression.getExpressionString() + 
                        " in Phase " + getPhase(), t);
            }
        }
    }
    
    /**
     * Gets the current Lifecycle instance from the LifecycleFactory
     * @param facesContext
     * @return
     */
    private Lifecycle _getLifecycle(FacesContext facesContext)
    {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        String id = facesContext.getExternalContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        if (id == null)
        {
            id = LifecycleFactory.DEFAULT_LIFECYCLE;
        }
        return factory.getLifecycle(id);  
    }
    
    protected RestoreViewSupport getRestoreViewSupport()
    {
        return getRestoreViewSupport(FacesContext.getCurrentInstance());
    }
    
    protected RestoreViewSupport getRestoreViewSupport(FacesContext context)
    {
        if (_restoreViewSupport == null)
        {
            _restoreViewSupport = new DefaultRestoreViewSupport(context);
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
