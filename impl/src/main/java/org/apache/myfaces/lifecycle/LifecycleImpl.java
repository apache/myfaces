/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;
import org.apache.myfaces.util.DebugUtils;
import org.apache.myfaces.shared_impl.util.RestoreStateUtils;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.portlet.PortletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * @author Manfred Geiler
 */
public class LifecycleImpl
        extends Lifecycle
{
    private static final Log log = LogFactory.getLog(LifecycleImpl.class);

    private final List _phaseListenerList = new ArrayList();

    /**
     * Lazy cache for returning _phaseListenerList as an Array.
     */
    private PhaseListener[] _phaseListenerArray = null;

    public LifecycleImpl()
    {
        // hide from public access
    }

    public void execute(FacesContext facesContext)
        throws FacesException
    {
        if (restoreView(facesContext))
        {
            return;
        }

        if (applyRequestValues(facesContext))
        {
            return;
        }

        if (processValidations(facesContext))
        {
            return;
        }

        if (updateModelValues(facesContext))
        {
            return;
        }

        invokeApplication(facesContext);
    }


    // Phases

    /**
     * Restore View (JSF.2.2.1)
     * @return true, if immediate rendering should occur
     */
    private boolean restoreView(FacesContext facesContext)
        throws FacesException
    {
    		boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) log.trace("entering restoreView in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.RESTORE_VIEW);

        if(isResponseComplete(facesContext, "restoreView", true))
        {
        		// have to skips this phase
        		return true;
        }
        if (shouldRenderResponse(facesContext, "restoreView", true)) 
        {
			skipFurtherProcessing = true;
		}

        // Derive view identifier
        String viewId = deriveViewId(facesContext);

        if(viewId == null)
        {
            ExternalContext externalContext = facesContext.getExternalContext();

            if(!externalContext.getRequestServletPath().endsWith("/"))
            {
                try
                {
                    externalContext.redirect(externalContext.getRequestServletPath()+"/");
                    facesContext.responseComplete();
                    return true;
                }
                catch (IOException e)
                {
                    throw new FacesException("redirect failed",e);
                }
            }
        }

        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();

        //boolean viewCreated = false;
        UIViewRoot viewRoot = viewHandler.restoreView(facesContext, viewId);
        if (viewRoot == null)
        {
            viewRoot = viewHandler.createView(facesContext, viewId);
            viewRoot.setViewId(viewId);
            facesContext.renderResponse();
            //viewCreated = true;
        }

        facesContext.setViewRoot(viewRoot);

        /* This section has been disabled because it causes some bug.
         * Be careful if you need to re-enable it.
         * Furthermore, for an unknown reason, it seems that by default
         * it is executed (i.e. log.isTraceEnabled() is true).
         * Bug example :
         * This traceView causes DebugUtil.printComponent to print all the attributes
         * of the view components.
         * And if you have a data table within an aliasBean, this causes the data table
         * to initialize it's value attribute while the alias isn't set.
         * So, the value initializes with an UIData.EMPTY_DATA_MODEL, and not with the aliased one.
         * But as it's initialized, it will not try to get the value from the ValueBinding next
         * time it needs to.
         * I expect this to cause more similar bugs.
         * TODO : Completely remove or be SURE by default it's not executed, and it has no more side-effects.

        if (log.isTraceEnabled())
        {
            //Note: DebugUtils Logger must also be in trace level
            DebugUtils.traceView(viewCreated ? "Newly created view" : "Restored view");
        }*/

        if (facesContext.getExternalContext().getRequestParameterMap().isEmpty())
        {
            //no POST or query parameters --> set render response flag
            facesContext.renderResponse();
        }

        RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(facesContext, viewRoot);

        informPhaseListenersAfter(facesContext, PhaseId.RESTORE_VIEW);

        if (isResponseComplete(facesContext, "restoreView", false)
				|| shouldRenderResponse(facesContext, "restoreView", false))
        {
        		// since this phase is completed we don't need to return right away even if the response is completed
        		skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled()) log.trace("exiting restoreView in " + LifecycleImpl.class.getName());
        return skipFurtherProcessing;
    }


    /**
     * Apply Request Values (JSF.2.2.2)
     * @return true, if response is complete
     */
    private boolean applyRequestValues(FacesContext facesContext)
        throws FacesException
    {
    		boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) log.trace("entering applyRequestValues in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.APPLY_REQUEST_VALUES);

        if(isResponseComplete(facesContext, "applyRequestValues", true))
        {
        		// have to return right away
        		return true;
        }
        if(shouldRenderResponse(facesContext, "applyRequestValues", true))
        {
        		skipFurtherProcessing = true;
        }

        facesContext.getViewRoot().processDecodes(facesContext);

        informPhaseListenersAfter(facesContext, PhaseId.APPLY_REQUEST_VALUES);


        if (isResponseComplete(facesContext, "applyRequestValues", false)
				|| shouldRenderResponse(facesContext, "applyRequestValues", false))
        {
        		// since this phase is completed we don't need to return right away even if the response is completed
        		skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled())
			log.trace("exiting applyRequestValues in "
					+ LifecycleImpl.class.getName());
        return skipFurtherProcessing;
    }


    /**
     * Process Validations (JSF.2.2.3)
     * @return true, if response is complete
     */
    private boolean processValidations(FacesContext facesContext) throws FacesException
    {
        boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) log.trace("entering processValidations in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.PROCESS_VALIDATIONS);

        if(isResponseComplete(facesContext, "processValidations", true))
        {
        		// have to return right away
        		return true;
        }
        if(shouldRenderResponse(facesContext, "processValidations", true))
        {
        		skipFurtherProcessing = true;
        }

        facesContext.getViewRoot().processValidators(facesContext);

        informPhaseListenersAfter(facesContext, PhaseId.PROCESS_VALIDATIONS);

		if (isResponseComplete(facesContext, "processValidations", false)
				|| shouldRenderResponse(facesContext, "processValidations", false))
        {
        		// since this phase is completed we don't need to return right away even if the response is completed
        		skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled()) log.trace("exiting processValidations in " + LifecycleImpl.class.getName());
        return skipFurtherProcessing;
    }


    /**
     * Update Model Values (JSF.2.2.4)
     * @return true, if response is complete
     */
    private boolean updateModelValues(FacesContext facesContext) throws FacesException
    {
	    boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) log.trace("entering updateModelValues in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.UPDATE_MODEL_VALUES);

        if(isResponseComplete(facesContext, "updateModelValues", true))
        {
        		// have to return right away
        		return true;
        }
        if(shouldRenderResponse(facesContext, "updateModelValues", true))
        {
        		skipFurtherProcessing = true;
        }

        facesContext.getViewRoot().processUpdates(facesContext);

        informPhaseListenersAfter(facesContext, PhaseId.UPDATE_MODEL_VALUES);

		if (isResponseComplete(facesContext, "updateModelValues", false)
				|| shouldRenderResponse(facesContext, "updateModelValues", false))
        {
        		// since this phase is completed we don't need to return right away even if the response is completed
        		skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled()) log.trace("exiting updateModelValues in " + LifecycleImpl.class.getName());

        return skipFurtherProcessing;
    }


    /**
     * Invoke Application (JSF.2.2.5)
     * @return true, if response is complete
     */
    private boolean invokeApplication(FacesContext facesContext)
        throws FacesException
    {
	    boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) log.trace("entering invokeApplication in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.INVOKE_APPLICATION);

        if(isResponseComplete(facesContext, "invokeApplication", true))
        {
        		// have to return right away
        		return true;
        }
        if(shouldRenderResponse(facesContext, "invokeApplication", true))
        {
        		skipFurtherProcessing = true;
        }

        facesContext.getViewRoot().processApplication(facesContext);

        informPhaseListenersAfter(facesContext, PhaseId.INVOKE_APPLICATION);

		if (isResponseComplete(facesContext, "invokeApplication", false)
				|| shouldRenderResponse(facesContext, "invokeApplication", false))
        {
        		// since this phase is completed we don't need to return right away even if the response is completed
        		skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled()) log.trace("exiting invokeApplication in " + LifecycleImpl.class.getName());

        return skipFurtherProcessing;
    }


    public void render(FacesContext facesContext) throws FacesException
    {
    		// if the response is complete we should not be invoking the phase listeners
        if(isResponseComplete(facesContext, "render", true))
        {
        		return;
        }
        if (log.isTraceEnabled()) log.trace("entering renderResponse in " + LifecycleImpl.class.getName());

        informPhaseListenersBefore(facesContext, PhaseId.RENDER_RESPONSE);
        // also possible that one of the listeners completed the response
        if(isResponseComplete(facesContext, "render", true))
        {
        		return;
        }
        Application application = facesContext.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        
        try
        {
            viewHandler.renderView(facesContext, facesContext.getViewRoot());
        }
        catch (IOException e)
        {
            throw new FacesException(e.getMessage(), e);
        }

        informPhaseListenersAfter(facesContext, PhaseId.RENDER_RESPONSE);
        if (log.isTraceEnabled())
        {
            //Note: DebugUtils Logger must also be in trace level
            DebugUtils.traceView("View after rendering");
        }

        if (log.isTraceEnabled()) log.trace("exiting renderResponse in " + LifecycleImpl.class.getName());
    }


    private boolean isResponseComplete(FacesContext facesContext, String phase, boolean before) {
    		boolean flag = false;
        if (facesContext.getResponseComplete())
        {
            if (log.isDebugEnabled())
				log.debug("exiting from lifecycle.execute in " + phase
						+ " because getResponseComplete is true from one of the " +
						(before ? "before" : "after") + " listeners");
            flag = true;
        }
        return flag;
    }

    private boolean shouldRenderResponse(FacesContext facesContext, String phase, boolean before) {
    		boolean flag = false;
        if (facesContext.getRenderResponse())
        {
            if (log.isDebugEnabled())
				log.debug("exiting from lifecycle.execute in " + phase
						+ " because getRenderResponse is true from one of the " + 
						(before ? "before" : "after") + " listeners");
            flag = true;
        }
        return flag;
	}

    private static String deriveViewId(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();

        if (PortletUtil.isPortletRequest(facesContext))
        {
            PortletRequest request = (PortletRequest)externalContext.getRequest();
            return request.getParameter(MyFacesGenericPortlet.VIEW_ID);
        }

        String viewId = externalContext.getRequestPathInfo();  //getPathInfo
        if (viewId == null)
        {
            //No extra path info found, so it is propably extension mapping
            viewId = externalContext.getRequestServletPath();  //getServletPath
            DebugUtils.assertError(viewId != null,
                                   log, "RequestServletPath is null, cannot determine viewId of current page.");
            if(viewId==null)
                return null;

            //TODO: JSF Spec 2.2.1 - what do they mean by "if the default ViewHandler implementation is used..." ?
            String defaultSuffix = externalContext.getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
            String suffix = defaultSuffix != null ? defaultSuffix : ViewHandler.DEFAULT_SUFFIX;
            DebugUtils.assertError(suffix.charAt(0) == '.',
                                   log, "Default suffix must start with a dot!");

            int dot = viewId.lastIndexOf('.');
            if (dot == -1)
            {
                log.error("Assumed extension mapping, but there is no extension in " + viewId);
                viewId=null;
            }
            else
            {
                viewId = viewId.substring(0, dot) + suffix;
            }
        }

        return viewId;
    }


    public void addPhaseListener(PhaseListener phaseListener)
    {
        if(phaseListener == null)
        {
            throw new NullPointerException("PhaseListener must not be null.");
        }
        synchronized(_phaseListenerList)
        {
            _phaseListenerList.add(phaseListener);
            _phaseListenerArray = null; // reset lazy cache array
        }
    }

    public void removePhaseListener(PhaseListener phaseListener)
    {
        if(phaseListener == null)
        {
            throw new NullPointerException("PhaseListener must not be null.");
        }
        synchronized(_phaseListenerList)
        {
            _phaseListenerList.remove(phaseListener);
            _phaseListenerArray = null; // reset lazy cache array
        }
    }

    public PhaseListener[] getPhaseListeners()
    {
        synchronized(_phaseListenerList)
        {
            // (re)build lazy cache array if necessary
            if (_phaseListenerArray == null)
            {
                _phaseListenerArray = (PhaseListener[])_phaseListenerList.toArray(new PhaseListener[_phaseListenerList.size()]);
            }
            return _phaseListenerArray;
        }
    }


    private void informPhaseListenersBefore(FacesContext facesContext, PhaseId phaseId)
    {
        PhaseListener[] phaseListeners = getPhaseListeners();
        for (int i = 0; i < phaseListeners.length; i++)
        {
            PhaseListener phaseListener = phaseListeners[i];
            int listenerPhaseId = phaseListener.getPhaseId().getOrdinal();
            if (listenerPhaseId == PhaseId.ANY_PHASE.getOrdinal() ||
                listenerPhaseId == phaseId.getOrdinal())
            {
                phaseListener.beforePhase(new PhaseEvent(facesContext, phaseId, this));
            }
        }

    }

    private void informPhaseListenersAfter(FacesContext facesContext, PhaseId phaseId)
    {
        PhaseListener[] phaseListeners = getPhaseListeners();
        for (int i = 0; i < phaseListeners.length; i++)
        {
            PhaseListener phaseListener = phaseListeners[i];
            int listenerPhaseId = phaseListener.getPhaseId().getOrdinal();
            if (listenerPhaseId == PhaseId.ANY_PHASE.getOrdinal() ||
                listenerPhaseId == phaseId.getOrdinal())
            {
                phaseListener.afterPhase(new PhaseEvent(facesContext, phaseId, this));
            }
        }

    }

}
