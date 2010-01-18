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

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.view.ViewDeclarationLanguage;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * 
 * @author Nikolay Petrov
 * 
 *         render response phase (JSF Spec 2.2.6)
 */
class RenderResponseExecutor implements PhaseExecutor
{
    public boolean execute(FacesContext facesContext)
    {
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot root;
        String viewId;
        String newViewId;
        
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
            
            // TODO: JSF 2.0 section 2.2.6, it says if the current response
            // is a partial response(ajax), then there must be no content written
            // outside of the f:view. This has sense only on jsp case, because            
            // we don't control jsp rendering and in this way we prevent unwanted
            // rendering. But note f:ajax only works on facelets, and f:view
            // tag handler only set properties for the current view root. It's
            // more, in facelets, every thing that render is a UIComponent instance,
            // so it is inside view root.
            // Anyway, we should put the expected behavior (take a look at 
            // context.servlet.ResponseSwitch) here and enable rendering when
            // PartialViewContextImpl.processPartialRendering(UIComponent, PhaseId)
            // do its own work, but for now it is 
            
            viewHandler.renderView(facesContext, root);
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
