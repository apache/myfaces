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

package org.apache.myfaces.test.mock.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.test.util.Jsf11Utils;
import org.apache.myfaces.test.util.Jsf12Utils;
import org.apache.myfaces.test.util.JsfVersion;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

/**
 * Implements the Restore View Phase (JSF Spec 2.2.1)
 * 
 * @author Nikolay Petrov
 * @author Bruno Aranda (JSF 1.2)
 * @version $Revision: 517403 $ $Date: 2007-03-12 22:17:00 +0100 (Mo, 12 Mrz 2007) $
 * @since 1.0.0
 */
class RestoreViewExecutor implements PhaseExecutor
{

    private static final Logger log = Logger.getLogger(RestoreViewExecutor.class.getName());
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
        if (JsfVersion.supports12())
        {
          Jsf12Utils.initView(facesContext, viewHandler);
        }
        else
        {
          // nothing to do
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();

        RestoreViewSupport restoreViewSupport = getRestoreViewSupport();

        if (viewRoot != null)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "View already exists in the FacesContext");
            }

            viewRoot.setLocale(facesContext.getExternalContext()
                    .getRequestLocale());
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
            return false;
        }

        String viewId = restoreViewSupport.calculateViewId(facesContext);

        // Determine if this request is a postback or initial request
        if (restoreViewSupport.isPostback(facesContext))
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Request is a postback");
            }

            viewRoot = viewHandler.restoreView(facesContext, viewId);
            if (viewRoot == null)
            {
              if (JsfVersion.supports12())
              {
                Jsf12Utils.throwViewExpiredException(viewId);
              }
              else
              {
                Jsf11Utils.throwViewExpiredException(viewId);
              }
            }
            restoreViewSupport.processComponentBinding(facesContext, viewRoot);
        }
        else
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Request is not a postback. New UIViewRoot will be created");
            }

            viewRoot = viewHandler.createView(facesContext, viewId);
            facesContext.renderResponse();
        }

        facesContext.setViewRoot(viewRoot);

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

    /**
     * TODO place that stuff into the default view handler implementation.
     */
    private static String deriveViewId(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();

        //        if (PortletUtil.isPortletRequest(facesContext))
        //        {
        //            PortletRequest request = (PortletRequest) externalContext.getRequest();
        //            return request.getParameter(MyFacesGenericPortlet.VIEW_ID);
        //        }
        //
        String viewId = externalContext.getRequestPathInfo(); // getPathInfo
        if (viewId == null)
        {
            // No extra path info found, so it is propably extension mapping
            viewId = externalContext.getRequestServletPath(); // getServletPath
            //            DebugUtils.assertError(viewId != null, log,
            //                    "RequestServletPath is null, cannot determine viewId of current page.");
            if (viewId == null)
            {
                return null;
            }

            // TODO: JSF Spec 2.2.1 - what do they mean by "if the default
            // ViewHandler implementation is used..." ?
            String defaultSuffix = externalContext
                    .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
            String suffix = defaultSuffix != null ? defaultSuffix
                    : ViewHandler.DEFAULT_SUFFIX;
            //            DebugUtils.assertError(suffix.charAt(0) == '.', log, "Default suffix must start with a dot!");

            int dot = viewId.lastIndexOf('.');
            if (dot == -1)
            {
                log.log(Level.SEVERE, "Assumed extension mapping, but there is no extension in "
                                + viewId);
                viewId = null;
            }
            else
            {
                viewId = viewId.substring(0, dot) + suffix;
            }
        }

        return viewId;
    }
}
