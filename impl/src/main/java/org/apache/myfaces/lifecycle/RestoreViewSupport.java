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

import java.util.logging.Logger;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitContextFactory;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import org.apache.myfaces.component.visit.MyFacesVisitHints;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class RestoreViewSupport
{
    private static final Logger log = Logger.getLogger(RestoreViewSupport.class.getName());

    private RenderKitFactory renderKitFactory = null;
    private VisitContextFactory visitContextFactory = null;

    public RestoreViewSupport(FacesContext facesContext)
    {
        visitContextFactory = (VisitContextFactory) FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    }

    public void processComponentBinding(FacesContext facesContext, UIViewRoot root)
    {
        // Faces 2.0: Old hack related to t:aliasBean was fixed defining a event that traverse
        // whole tree and let components to override UIComponent.processEvent() method to include it.
        
        // Remove this hack SKIP_ITERATION_HINT and use VisitHints.SKIP_ITERATION in Faces 2.1 only
        // is not possible, because jsf 2.0 API-based libraries can use the String
        // hint, Faces21-based libraries can use both.
        try
        {
            facesContext.getAttributes().put(MyFacesVisitHints.SKIP_ITERATION_HINT, Boolean.TRUE);
            facesContext.getApplication().publishEvent(facesContext, PostRestoreStateEvent.class, root);

            VisitContext visitContext = visitContextFactory.getVisitContext(facesContext,
                    null, MyFacesVisitHints.SET_SKIP_ITERATION);
            root.visitTree(visitContext, new RestoreStateCallback());
        }
        finally
        {
            // We must remove hint in finally, because an exception can break this phase,
            // but lifecycle can continue, if custom exception handler swallows the exception
            facesContext.getAttributes().remove(MyFacesVisitHints.SKIP_ITERATION_HINT);
        }
    }

    public boolean isPostback(FacesContext facesContext)
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String renderkitId = viewHandler.calculateRenderKitId(facesContext);
        ResponseStateManager rsm = renderKitFactory.getRenderKit(facesContext, renderkitId).getResponseStateManager();
        return rsm.isPostback(facesContext);
    }

    private static class RestoreStateCallback implements VisitCallback
    {
        private PostRestoreStateEvent event;

        @Override
        public VisitResult visit(VisitContext context, UIComponent target)
        {
            if (event == null)
            {
                event = new PostRestoreStateEvent(target);
            }
            else
            {
                event.setComponent(target);
            }

            // call the processEvent method of the current component.
            // The argument event must be an instance of AfterRestoreStateEvent whose component
            // property is the current component in the traversal.
            target.processEvent(event);
            
            return VisitResult.ACCEPT;
        }
    }
}
