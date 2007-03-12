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
package org.apache.myfaces.portlet;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: test this portlet view handler in portlet environment
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class PortletViewHandler extends ViewHandlerWrapper
{
    private final ViewHandler _viewHandler;

    private static final Log log = LogFactory.getLog(PortletViewHandler.class);

    public PortletViewHandler(ViewHandler viewHandler)
    {
        _viewHandler = viewHandler;
    }

    @Override
    protected ViewHandler getWrapped()
    {
        return _viewHandler;
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        if (PortletUtil.isPortletRequest(context))
        {
            PortletRequest request = (PortletRequest) context.getExternalContext().getRequest();
            String portletViewId = request.getParameter(MyFacesGenericPortlet.VIEW_ID);
            Application application = context.getApplication();
            ViewHandler applicationViewHandler = application.getViewHandler();
            String renderKitId = applicationViewHandler.calculateRenderKitId(context);
            UIViewRoot viewRoot = application.getStateManager().restoreView(context, portletViewId, renderKitId);
            return viewRoot;
        }
        return super.restoreView(context, viewId);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        UIViewRoot viewRoot = super.createView(context, viewId);
        if (PortletUtil.isPortletRequest(context))
        {
            PortletRequest request = (PortletRequest) context.getExternalContext().getRequest();
            viewRoot.setViewId(request.getParameter(MyFacesGenericPortlet.VIEW_ID));
        }
        return viewRoot;
    }

    @Override
    public String getActionURL(FacesContext context, String viewId)
    {
        if (PortletUtil.isRenderResponse(context))
        {
            RenderResponse response = (RenderResponse) context.getExternalContext().getResponse();
            PortletURL url = response.createActionURL();
            url.setParameter(MyFacesGenericPortlet.VIEW_ID, viewId);
            return url.toString();
        }
        return super.getActionURL(context, viewId);
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException
    {
        if (PortletUtil.isPortletRequest(context))
        {
            if (viewToRender.isRendered())
            {
                if (log.isTraceEnabled())
                    log.trace("It is a portlet request. Dispatching to view");
                context.getExternalContext().dispatch(viewToRender.getViewId());
            }
        }
        else
        {
            super.renderView(context, viewToRender);
        }
    }
}
