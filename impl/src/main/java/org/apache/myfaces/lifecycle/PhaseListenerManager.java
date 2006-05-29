/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;
import javax.el.MethodExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class encapsulates the logic used to call PhaseListeners.  It was 
 * needed because of issue 9 of the JSF 1.2 spec.  See section 11.3 for more
 * details.
 *
 * @author Stan Silvert
 */
class PhaseListenerManager {
    
    private static final Log log = LogFactory.getLog(PhaseListenerManager.class);
    
    private Lifecycle lifecycle;
    private FacesContext facesContext;
    private PhaseListener[] phaseListeners;
    private MethodExpression uiViewRootBeforeListener;
    private MethodExpression uiViewRootAfterListener;
    
    // Tracks success in the beforePhase.  Listeners that throw an exception
    // in beforePhase or were never called because a previous listener threw
    // an exception should not have its afterPhase called
    private Map<PhaseId, boolean[]> listenerSuccessMap = new HashMap<PhaseId, boolean[]>();
    
    private Map<PhaseId, Boolean> uiViewRootListenerSuccessMap = new HashMap<PhaseId, Boolean>();
    
    /** Creates a new instance of PhaseListenerManager */
    PhaseListenerManager(Lifecycle lifecycle, 
                         FacesContext facesContext, 
                         PhaseListener[] phaseListeners) {
        this.lifecycle = lifecycle;
        this.facesContext = facesContext;
        this.phaseListeners = phaseListeners;
    }
    
    private boolean isListenerForThisPhase(PhaseListener phaseListener, PhaseId phaseId) {
        int listenerPhaseId = phaseListener.getPhaseId().getOrdinal();
        return (listenerPhaseId == PhaseId.ANY_PHASE.getOrdinal() ||
                listenerPhaseId == phaseId.getOrdinal());
    }
    
    private boolean isListenerForThisPhase(MethodExpression phaseListener, PhaseId phaseId) {
        return (phaseListener != null) && (phaseId != PhaseId.RESTORE_VIEW);
    }
    
    // these variables can't be set until the Apply Request Values phase
    private void setUIViewRootListeners(PhaseId phaseId) {
        if (phaseId != PhaseId.APPLY_REQUEST_VALUES) return;
        
        this.uiViewRootBeforeListener = facesContext.getViewRoot().getBeforePhaseListener();
        this.uiViewRootAfterListener = facesContext.getViewRoot().getAfterPhaseListener();
    }
    
    void informPhaseListenersBefore(PhaseId phaseId) {
        setUIViewRootListeners(phaseId);
        boolean[] beforePhaseSuccess = new boolean[phaseListeners.length];
        listenerSuccessMap.put(phaseId, beforePhaseSuccess);
        
        PhaseEvent phaseEvent = new PhaseEvent(facesContext, phaseId, lifecycle);
        for (int i = 0; i < phaseListeners.length; i++) {
            PhaseListener phaseListener = phaseListeners[i];
            if (isListenerForThisPhase(phaseListener, phaseId)) {
                try {
                    phaseListener.beforePhase(phaseEvent);
                    beforePhaseSuccess[i] = true;
                } catch (Exception e) {
                    beforePhaseSuccess[i] = false; // redundant - for clarity
                    log.error("Exception in PhaseListener " + phaseId.toString() + " beforePhase.", e);
                    return;
                }
            }
        }
        
        uiViewRootListenerSuccessMap.put(phaseId, Boolean.FALSE);
        if (isListenerForThisPhase(this.uiViewRootBeforeListener, phaseId)) {
            try {
                invokeMethodExpression(this.uiViewRootBeforeListener, phaseEvent);
                uiViewRootListenerSuccessMap.put(phaseId, Boolean.TRUE);
            } catch (Exception e) {
                log.error("Exception in UIViewRoot BeforePhaseListener " + phaseId.toString() + " beforePhase.", e);
                return;
            }
        }
    }
    
    void invokeMethodExpression(MethodExpression methodExpression, PhaseEvent event) throws Exception {
        methodExpression.invoke(facesContext.getELContext(), new Object[]{event});
    }

    void informPhaseListenersAfter(PhaseId phaseId) {
        boolean[] beforePhaseSuccess = listenerSuccessMap.get(phaseId);
        
        PhaseEvent phaseEvent = new PhaseEvent(facesContext, phaseId, lifecycle);
        for (int i = phaseListeners.length - 1; i >= 0; i--)  {
            PhaseListener phaseListener = phaseListeners[i];
            if (isListenerForThisPhase(phaseListener, phaseId) 
                && beforePhaseSuccess[i]) {
                try {
                    phaseListener.afterPhase(phaseEvent);
                } catch (Exception e) {
                    log.error("Exception in PhaseListener " + phaseId.toString() + " afterPhase", e);
                }
            }
        }

        if (isListenerForThisPhase(this.uiViewRootAfterListener, phaseId) && 
                uiViewRootListenerSuccessMap.get(phaseId).booleanValue()) {
            try {
                invokeMethodExpression(this.uiViewRootAfterListener, phaseEvent);
            } catch (Exception e) {
                log.error("Exception in UIViewRoot AfterPhaseListener " + phaseId.toString() + " afterPhase.", e);
                return;
            }
        }
    }
}
