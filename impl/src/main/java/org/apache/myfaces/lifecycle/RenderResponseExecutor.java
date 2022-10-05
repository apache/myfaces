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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.config.webparameters.MyfacesConfig;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostRenderViewEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * Implements the render response phase (Faces Spec 2.2.6)
 * 
 * @author Nikolay Petrov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class RenderResponseExecutor extends PhaseExecutor
{
    private static final Logger log = Logger.getLogger(RenderResponseExecutor.class.getName());
    
    @Override
    public boolean execute(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot root;
        UIViewRoot previousRoot;
        String viewId;
        String newViewId;
        boolean isNotSameRoot;
        int loops = 0;
        int maxLoops = 15;
        
        if (facesContext.getViewRoot() == null)
        {
            throw new ViewNotFoundException("A view is required to execute "+facesContext.getCurrentPhaseId());
        }

        forceSessionCreation(facesContext);

        try
        {
            // do-while, because the view might change in PreRenderViewEvent-listeners
            do
            {
                root = facesContext.getViewRoot();
                previousRoot = root;
                viewId = root.getViewId();
                
                ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, viewId);
                if (vdl != null)
                {
                    vdl.buildView(facesContext, root);
                }
                
                // publish a PreRenderViewEvent: note that the event listeners
                // of this event can change the view, so we have to perform the algorithm 
                // until the viewId does not change when publishing this event.
                application.publishEvent(facesContext, PreRenderViewEvent.class, root);
                
                // was the response marked as complete by an event listener?
                if (facesContext.getResponseComplete())
                {
                    return false;
                }

                root = facesContext.getViewRoot();
                
                newViewId = root.getViewId();
                
                isNotSameRoot = !( (newViewId == null ? newViewId == viewId : newViewId.equals(viewId) ) && 
                        previousRoot.equals(root) ); 
                
                loops++;
            }
            while ((newViewId == null && viewId != null) 
                    || (newViewId != null && (!newViewId.equals(viewId) || isNotSameRoot ) ) && loops < maxLoops);
            
            if (loops == maxLoops)
            {
                // PreRenderView reach maxLoops - probably a infinitive recursion:
                Level level = facesContext.isProjectStage(ProjectStage.Production)
                        ? Level.FINE
                        : Level.WARNING;
                if (log.isLoggable(level))
                {
                    log.log(level, "Cicle over buildView-PreRenderViewEvent on RENDER_RESPONSE phase "
                                   + "reaches maximal limit, please check listeners for infinite recursion.");
                }
            }

            viewHandler.renderView(facesContext, root);

            application.publishEvent(facesContext, PostRenderViewEvent.class, root);

            // log all unhandled FacesMessages, don't swallow them
            // perf: org.apache.myfaces.context.servlet.FacesContextImpl.getMessageList() creates
            // new Collections.unmodifiableList with every invocation->  call it only once
            // and messageList is RandomAccess -> use index based loop
            List<FacesMessage> messageList = facesContext.getMessageList();
            if (!messageList.isEmpty())
            {
                StringBuilder builder = new StringBuilder();
                boolean shouldLog = false;
                for (int i = 0, size = messageList.size(); i < size; i++)
                {
                    FacesMessage message = messageList.get(i);
                    if (!message.isRendered())
                    {
                        builder.append("\n- ");
                        builder.append(message.getDetail());
                        
                        shouldLog = true;
                    }
                }
                if (shouldLog)
                {
                    log.log(Level.WARNING, "There are some unhandled FacesMessages, " +
                            "this means not every FacesMessage had a chance to be rendered.\n" +
                            "These unhandled FacesMessages are: " + builder.toString());
                }
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public PhaseId getPhase()
    {
        return PhaseId.RENDER_RESPONSE;
    }

    /**
     * Create a session if the ALWAYS_FORCE_SESSION_CREATION param is set to true, or if the
     * current view is not transient and server side state saving is in use.
     * 
     * Note: if the current view is transient or client side state saving is in use, it is 
     * not technically correct to create a session here, since a session should not be
     * required for those cases and creating one will cause undesirable memory usage.  
     * However, if we do not create a session before rendering begins and view or session
     * scope beans are created later on, then the response might be committed before those 
     * scopes have a chance to create a session and so the session cookie will not be set.
     * See MYFACES-4309
     * 
     * @param FacesContext
     */
    private void forceSessionCreation(FacesContext context) 
    {
        if (context.getExternalContext().getSession(false) == null) 
        {
            ExternalContext ec = context.getExternalContext();
            if (MyfacesConfig.getCurrentInstance(ec).isAlwaysForceSessionCreation() 
                    || (!context.getViewRoot().isTransient() 
                    && !context.getApplication().getStateManager().isSavingStateInClient(context))) 
            {
                context.getExternalContext().getSession(true);
            }
        }
    }
}
