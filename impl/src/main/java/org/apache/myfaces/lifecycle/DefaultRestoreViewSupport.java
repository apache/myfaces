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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.util.Assert;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultRestoreViewSupport implements RestoreViewSupport
{
    private static final String JAVAX_SERVLET_INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";

    private static final String JAVAX_SERVLET_INCLUDE_PATH_INFO = "javax.servlet.include.path_info";

    //private final Log log = LogFactory.getLog(DefaultRestoreViewSupport.class);
    private final Logger log = Logger.getLogger(DefaultRestoreViewSupport.class.getName());

    public void processComponentBinding(FacesContext facesContext, UIComponent component)
    {
        // JSF 2.0: Old hack related to t:aliasBean was fixed defining a event that traverse
        // whole tree and let components to override UIComponent.processEvent() method to include it.
        component.visitTree(VisitContext.createVisitContext(facesContext), new RestoreStateCallback());
        
        /*
        ValueExpression binding = component.getValueExpression("binding");
        if (binding != null)
        {
            binding.setValue(facesContext.getELContext(), component);
        }

        // This part is for make compatibility with t:aliasBean, because
        // this components has its own code before and after binding is
        // set for child components.
        RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(facesContext, component);

        // The required behavior for the spec is call recursively this method
        // for walk the component tree.
        // for (Iterator<UIComponent> iter = component.getFacetsAndChildren(); iter.hasNext();)
        // {
        // processComponentBinding(facesContext, iter.next());
        // }
         */
    }

    public String calculateViewId(FacesContext facesContext)
    {
        Assert.notNull(facesContext);
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();

        String viewId = (String) requestMap.get(JAVAX_SERVLET_INCLUDE_PATH_INFO);
        boolean traceEnabled = log.isLoggable(Level.FINEST);
        if (viewId != null)
        {
            if (traceEnabled)
            {
                log.finest("Calculated viewId '" + viewId + "' from request param '" + JAVAX_SERVLET_INCLUDE_PATH_INFO
                        + "'");
            }
        }
        else
        {
            viewId = externalContext.getRequestPathInfo();
            if (viewId != null && traceEnabled)
            {
                log.finest("Calculated viewId '" + viewId + "' from request path info");
            }
        }

        if (viewId == null)
        {
            viewId = (String) requestMap.get(JAVAX_SERVLET_INCLUDE_SERVLET_PATH);
            if (viewId != null && traceEnabled)
            {
                log.finest("Calculated viewId '" + viewId + "' from request param '"
                        + JAVAX_SERVLET_INCLUDE_SERVLET_PATH + "'");
            }
        }

        if (viewId == null)
        {
            viewId = externalContext.getRequestServletPath();
            if (viewId != null && traceEnabled)
            {
                log.finest("Calculated viewId '" + viewId + "' from request servlet path");
            }
        }

        if (viewId == null)
        {
            throw new FacesException("Could not determine view id.");
        }

        return viewId;
    }

    public boolean isPostback(FacesContext facesContext)
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String renderkitId = viewHandler.calculateRenderKitId(facesContext);
        ResponseStateManager rsm = RendererUtils.getResponseStateManager(facesContext, renderkitId);
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
