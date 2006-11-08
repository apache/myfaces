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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;
import org.apache.myfaces.shared_impl.util.RestoreStateUtils;
import org.apache.myfaces.util.DebugUtils;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * @author Nikolay Petrov
 *
 * Restore view phase (JSF Spec 2.2.1)
 */
class RestoreViewExecutor implements PhaseExecutor {

	private static final Log log = LogFactory.getLog(LifecycleImpl.class);

	public boolean execute(FacesContext facesContext) {
		if(facesContext.getViewRoot() != null) {
			facesContext.getViewRoot().setLocale(facesContext.getExternalContext().getRequestLocale());
			RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(facesContext, facesContext.getViewRoot());
			return false;
		}

		// Derive view identifier
		String viewId = deriveViewId(facesContext);

		if (viewId == null) {
			ExternalContext externalContext = facesContext.getExternalContext();

			if(externalContext.getRequestServletPath() == null) {
				return true;
			}
			
			if (!externalContext.getRequestServletPath().endsWith("/")) {
				try {
					externalContext.redirect(externalContext.getRequestServletPath() + "/");
					facesContext.responseComplete();
					return true;
				} catch (IOException e) {
					throw new FacesException("redirect failed", e);
				}
			}
		}

		Application application = facesContext.getApplication();
		ViewHandler viewHandler = application.getViewHandler();

		// boolean viewCreated = false;
		UIViewRoot viewRoot = viewHandler.restoreView(facesContext, viewId);
		if (viewRoot == null) {
			viewRoot = viewHandler.createView(facesContext, viewId);
			viewRoot.setViewId(viewId);
			facesContext.renderResponse();
			// viewCreated = true;
		}

		facesContext.setViewRoot(viewRoot);

		if (facesContext.getExternalContext().getRequestParameterMap().isEmpty()) {
			// no POST or query parameters --> set render response flag
			facesContext.renderResponse();
		}

		RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(facesContext, viewRoot);
		return false;
	}

	public PhaseId getPhase() {
		return PhaseId.RESTORE_VIEW;
	}

	private static String deriveViewId(FacesContext facesContext) {
		ExternalContext externalContext = facesContext.getExternalContext();

		if (PortletUtil.isPortletRequest(facesContext)) {
			PortletRequest request = (PortletRequest) externalContext.getRequest();
			return request.getParameter(MyFacesGenericPortlet.VIEW_ID);
		}

		String viewId = externalContext.getRequestPathInfo(); // getPathInfo
		if (viewId == null) {
			// No extra path info found, so it is propably extension mapping
			viewId = externalContext.getRequestServletPath(); // getServletPath
			DebugUtils.assertError(viewId != null, log,
					"RequestServletPath is null, cannot determine viewId of current page.");
			if (viewId == null)
				return null;

			// TODO: JSF Spec 2.2.1 - what do they mean by "if the default
			// ViewHandler implementation is used..." ?
			String defaultSuffix = externalContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
			String suffix = defaultSuffix != null ? defaultSuffix : ViewHandler.DEFAULT_SUFFIX;
			DebugUtils.assertError(suffix.charAt(0) == '.', log, "Default suffix must start with a dot!");

			int dot = viewId.lastIndexOf('.');
			if (dot == -1) {
				log.error("Assumed extension mapping, but there is no extension in " + viewId);
				viewId = null;
			} else {
				viewId = viewId.substring(0, dot) + suffix;
			}
		}

		return viewId;
	}
}
