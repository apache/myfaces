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

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.util.DebugUtils;

/**
 * Implements the lifecycle as described in Spec. 1.0 PFD Chapter 2
 * @author Manfred Geiler
 * @author Nikolay Petrov
 */
public class LifecycleImpl
        extends Lifecycle
{
    private static final Log log = LogFactory.getLog(LifecycleImpl.class);

    private PhaseExecutor[] lifecycleExecutors;
    private PhaseExecutor renderExecutor;

    private final List _phaseListenerList = new ArrayList();

    /**
     * Lazy cache for returning _phaseListenerList as an Array.
     */
    private PhaseListener[] _phaseListenerArray = null;

    public LifecycleImpl() {
        // hide from public access
    	lifecycleExecutors = new PhaseExecutor[] {
    			new RestoreViewExecutor(),
    			new ApplyRequestValuesExecutor(),
    			new ProcessValidationsExecutor(),
    			new UpdateModelValuesExecutor(),
    			new InvokeApplicationExecutor()
    	};

    	renderExecutor = new RenderResponseExecutor();
    }

    public void execute(FacesContext facesContext) throws FacesException {
        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this, facesContext, getPhaseListeners());
        for(int executorIndex = 0;executorIndex < lifecycleExecutors.length;executorIndex++) {
        	if(executePhase(facesContext, lifecycleExecutors[executorIndex], phaseListenerMgr)) {
        		return;
        	}
        }
    }


    private boolean executePhase(FacesContext facesContext, PhaseExecutor executor,
    		PhaseListenerManager phaseListenerMgr) throws FacesException {
		boolean skipFurtherProcessing = false;
        if (log.isTraceEnabled()) {
        	log.trace("entering " + executor.getPhase() + " in " + LifecycleImpl.class.getName());
        }

        try {
            phaseListenerMgr.informPhaseListenersBefore(executor.getPhase());

            if(isResponseComplete(facesContext, executor.getPhase(), true)) {
                // have to return right away
            	return true;
            }
            if(shouldRenderResponse(facesContext, executor.getPhase(), true)) {
            	skipFurtherProcessing = true;
            }

            if(executor.execute(facesContext)) {
            	return true;
            }
        } finally {
            phaseListenerMgr.informPhaseListenersAfter(executor.getPhase());
        }


        if (isResponseComplete(facesContext, executor.getPhase(), false)
        		|| shouldRenderResponse(facesContext, executor.getPhase(), false)) {
        	// since this phase is completed we don't need to return right away even if the response is completed
        	skipFurtherProcessing = true;
        }

        if (!skipFurtherProcessing && log.isTraceEnabled()) {
			log.trace("exiting " + executor.getPhase() + " in " + LifecycleImpl.class.getName());
        }

        return skipFurtherProcessing;
    }

    public void render(FacesContext facesContext) throws FacesException {
    	// if the response is complete we should not be invoking the phase listeners
        if(isResponseComplete(facesContext, renderExecutor.getPhase(), true)) {
        	return;
        }
        if (log.isTraceEnabled()) log.trace("entering " + renderExecutor.getPhase() + " in " + LifecycleImpl.class.getName());

        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this, facesContext, getPhaseListeners());

        try {
            phaseListenerMgr.informPhaseListenersBefore(renderExecutor.getPhase());
            // also possible that one of the listeners completed the response
            if(isResponseComplete(facesContext, renderExecutor.getPhase(), true)) {
            	return;
            }

            renderExecutor.execute(facesContext);
        } finally {
            phaseListenerMgr.informPhaseListenersAfter(renderExecutor.getPhase());
        }

        if (log.isTraceEnabled()) {
            //Note: DebugUtils Logger must also be in trace level
            DebugUtils.traceView("View after rendering");
        }

        if (log.isTraceEnabled()) {
        	log.trace("exiting " + renderExecutor.getPhase() + " in " + LifecycleImpl.class.getName());
        }
    }

    private boolean isResponseComplete(FacesContext facesContext, PhaseId phase, boolean before) {
		boolean flag = false;
	    if (facesContext.getResponseComplete()) {
	        if (log.isDebugEnabled()) {
				log.debug("exiting from lifecycle.execute in " + phase
						+ " because getResponseComplete is true from one of the " +
						(before ? "before" : "after") + " listeners");
	        }
	        flag = true;
	    }
	    return flag;
	}

	private boolean shouldRenderResponse(FacesContext facesContext, PhaseId phase, boolean before) {
			boolean flag = false;
	    if (facesContext.getRenderResponse()) {
	        if (log.isDebugEnabled()) {
				log.debug("exiting from lifecycle.execute in " + phase
						+ " because getRenderResponse is true from one of the " +
						(before ? "before" : "after") + " listeners");
	        }
	        flag = true;
	    }
	    return flag;
	}

    public void addPhaseListener(PhaseListener phaseListener) {
		if (phaseListener == null) {
			throw new NullPointerException("PhaseListener must not be null.");
		}
		synchronized (_phaseListenerList) {
			_phaseListenerList.add(phaseListener);
			_phaseListenerArray = null; // reset lazy cache array
		}
	}

	public void removePhaseListener(PhaseListener phaseListener) {
		if (phaseListener == null) {
			throw new NullPointerException("PhaseListener must not be null.");
		}
		synchronized (_phaseListenerList) {
			_phaseListenerList.remove(phaseListener);
			_phaseListenerArray = null; // reset lazy cache array
		}
	}

	public PhaseListener[] getPhaseListeners() {
		synchronized (_phaseListenerList) {
			// (re)build lazy cache array if necessary
			if (_phaseListenerArray == null) {
				_phaseListenerArray = (PhaseListener[]) _phaseListenerList.toArray(new PhaseListener[_phaseListenerList
						.size()]);
			}
			return _phaseListenerArray;
		}
	}
}
