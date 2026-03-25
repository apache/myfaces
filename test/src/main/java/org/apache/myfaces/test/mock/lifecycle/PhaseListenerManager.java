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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * This class encapsulates the logic used to call PhaseListeners.  It was 
 * needed because of issue 9 of the JSF 1.2 spec.  See section 11.3 for more
 * details.
 *
 * @author Stan Silvert
 * @since 1.0.0
 */
class PhaseListenerManager
{

    private static final Logger log = Logger.getLogger(PhaseListenerManager.class.getName());

    private Lifecycle lifecycle;
    private FacesContext facesContext;
    private List<PhaseListener> phaseListeners;

    // Tracks success in the beforePhase.  Listeners that throw an exception
    // in beforePhase or were never called because a previous listener threw
    // an exception should not have its afterPhase called
    private boolean[] beforePhaseSuccessScratch;

    private int listenerCountForActivePhase;

    private PhaseId activePhaseIdForBeforeSuccess;
    private PhaseEvent phaseEventForActivePhase;

    /** Creates a new instance of PhaseListenerManager */
    PhaseListenerManager(Lifecycle lifecycle, FacesContext facesContext,
            List<PhaseListener> phaseListeners)
    {
        this.lifecycle = lifecycle;
        this.facesContext = facesContext;
        this.phaseListeners = phaseListeners;
    }

    private void ensureScratch(int minSize)
    {
        if (beforePhaseSuccessScratch == null || beforePhaseSuccessScratch.length < minSize)
        {
            beforePhaseSuccessScratch = new boolean[minSize];
        }
    }

    private boolean isListenerForThisPhase(PhaseListener phaseListener,
            PhaseId phaseId)
    {
        int listenerPhaseId = phaseListener.getPhaseId().getOrdinal();
        return (listenerPhaseId == PhaseId.ANY_PHASE.getOrdinal() || listenerPhaseId == phaseId
                .getOrdinal());
    }

    void informPhaseListenersBefore(PhaseId phaseId)
    {
        listenerCountForActivePhase = phaseListeners.size();
        ensureScratch(listenerCountForActivePhase);
        Arrays.fill(beforePhaseSuccessScratch, 0, listenerCountForActivePhase, false);
        activePhaseIdForBeforeSuccess = phaseId;

        PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
        phaseEventForActivePhase = event;

        for (int i = 0; i < listenerCountForActivePhase; i++)
        {
            PhaseListener phaseListener = phaseListeners.get(i);
            if (isListenerForThisPhase(phaseListener, phaseId))
            {
                try
                {
                    phaseListener.beforePhase(event);
                    beforePhaseSuccessScratch[i] = true;
                }
                catch (Exception e)
                {
                    beforePhaseSuccessScratch[i] = false; // redundant - for clarity
                    log.log(Level.SEVERE, "Exception in PhaseListener "
                            + phaseId.toString() + " beforePhase.", e);
                    return;
                }
            }
        }
    }

    void informPhaseListenersAfter(PhaseId phaseId)
    {
        if (activePhaseIdForBeforeSuccess != phaseId)
        {
            return;
        }

        PhaseEvent event = phaseEventForActivePhase;

        for (int i = listenerCountForActivePhase - 1; i >= 0; i--)
        {
            if (i >= phaseListeners.size())
            {
                continue;
            }
            PhaseListener phaseListener = phaseListeners.get(i);
            if (isListenerForThisPhase(phaseListener, phaseId)
                    && beforePhaseSuccessScratch[i])
            {
                try
                {
                    phaseListener.afterPhase(event);
                }
                catch (Exception e)
                {
                    log.log(Level.SEVERE, "Exception in PhaseListener "
                            + phaseId.toString() + " afterPhase", e);
                }
            }
        }

        activePhaseIdForBeforeSuccess = null;
        phaseEventForActivePhase = null;
    }
}
