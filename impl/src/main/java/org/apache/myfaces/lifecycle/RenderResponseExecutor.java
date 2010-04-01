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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.view.ViewDeclarationLanguage;

import org.apache.myfaces.context.servlet.ResponseSwitch;
import org.apache.myfaces.util.ExternalContextUtils;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * 
 * @author Nikolay Petrov
 * 
 *         render response phase (JSF Spec 2.2.6)
 */
class RenderResponseExecutor implements PhaseExecutor
{
    
    private static final Logger log = Logger.getLogger(RenderResponseExecutor.class.getName());
    
    public boolean execute(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot root;
        String viewId;
        String newViewId;
        
        // JSF 2.0 section 2.2.6: if the current response
        // is a partial response, then there must be 
        // no content written outside of the f:view
        if (facesContext.getPartialViewContext().isPartialRequest())
        {
            // try to get (or create) a ResponseSwitch and turn off the output
            Object response = facesContext.getExternalContext().getResponse();
            ResponseSwitch responseSwitch = ExternalContextUtils.getResponseSwitch(response);
            if (responseSwitch == null)
            {
                // no ResponseSwitch installed yet - create one 
                responseSwitch = ExternalContextUtils.createResponseSwitch(response);
                if (responseSwitch != null)
                {
                    // install the ResponseSwitch
                    facesContext.getExternalContext().setResponse(responseSwitch);
                }
            }
            if (responseSwitch != null)
            {
                responseSwitch.setEnabled(false);
            }
        }
        
        try
        {
            // do-while, because the view might change in PreRenderViewEvent-listeners
            do
            {
                root = facesContext.getViewRoot();
                viewId = root.getViewId();
                
                ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(
                        facesContext, viewId);
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
                
                newViewId = facesContext.getViewRoot().getViewId();
            }
            while ((newViewId == null && viewId != null) 
                    || (newViewId != null && !newViewId.equals(viewId)));
            
            viewHandler.renderView(facesContext, root);
            
            // log all unhandled FacesMessages, don't swallow them
            if (!facesContext.getMessageList().isEmpty())
            {
                StringBuilder builder = new StringBuilder();
                boolean shouldLog = false;
                for (FacesMessage message : facesContext.getMessageList())
                {
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

    public PhaseId getPhase()
    {
        return PhaseId.RENDER_RESPONSE;
    }
}
