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
import org.apache.myfaces.test.util.JsfVersion;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

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

    @Override
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
            viewHandler.initView(facesContext);
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

            viewRoot.setLocale(facesContext.getExternalContext().getRequestLocale());
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
                    throw new ViewExpiredException(
                        "The expected view was not returned for the view identifier: " + viewId, viewId);
                }
                else
                {
                    throw new RuntimeException(
                            "The expected view was not returned for the view identifier: " + viewId);
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

    public void setRestoreViewSupport(RestoreViewSupport restoreViewSupport)
    {
        _restoreViewSupport = restoreViewSupport;
    }

    @Override
    public PhaseId getPhase()
    {
        return PhaseId.RESTORE_VIEW;
    }
}
