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

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * <p>Mock implementation of <code>Lifecycle</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockLifecycle extends Lifecycle
{

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>List of event listeners for this instance.</p>
     */
    private List phaseListenerList = new ArrayList();
    private PhaseExecutor[] lifecycleExecutors;
    private PhaseExecutor renderExecutor;

    public MockLifecycle()
    {
        lifecycleExecutors = new PhaseExecutor[] { new RestoreViewExecutor(),
                new ApplyRequestValuesExecutor(),
                new ProcessValidationsExecutor(),
                new UpdateModelValuesExecutor(),
                new InvokeApplicationExecutor() };

        renderExecutor = new RenderResponseExecutor();
    }

    // ------------------------------------------------------- Lifecycle Methods

    @Override
    public void addPhaseListener(PhaseListener listener)
    {
        phaseListenerList.add(listener);
    }

    @Override
    public void execute(FacesContext context) throws FacesException
    {
        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this,context, getPhaseListeners());
        for (int executorIndex = 0; executorIndex < lifecycleExecutors.length; executorIndex++)
        {
            if (executePhase(context, lifecycleExecutors[executorIndex],
                    phaseListenerMgr))
            {
                return;
            }
        }
    }

    private boolean executePhase(FacesContext facesContext,
            PhaseExecutor executor, PhaseListenerManager phaseListenerMgr)
            throws FacesException
    {
        boolean skipFurtherProcessing = false;

        try
        {
            phaseListenerMgr.informPhaseListenersBefore(executor.getPhase());

            if (isResponseComplete(facesContext, executor.getPhase(), true))
            {
                // have to return right away
                return true;
            }
            if (shouldRenderResponse(facesContext, executor.getPhase(), true))
            {
                skipFurtherProcessing = true;
            }

            if (executor.execute(facesContext))
            {
                return true;
            }
        }
        finally
        {
            phaseListenerMgr.informPhaseListenersAfter(executor.getPhase());
        }

        if (isResponseComplete(facesContext, executor.getPhase(), false)
                || shouldRenderResponse(facesContext, executor.getPhase(),
                        false))
        {
            // since this phase is completed we don't need to return right away even if the response is completed
            skipFurtherProcessing = true;
        }

        return skipFurtherProcessing;
    }

    @Override
    public PhaseListener[] getPhaseListeners()
    {
        return (PhaseListener[]) phaseListenerList.toArray(new PhaseListener[phaseListenerList.size()]);
    }

    @Override
    public void removePhaseListener(PhaseListener listener)
    {
        phaseListenerList.remove(listener);
    }

    @Override
    public void render(FacesContext context) throws FacesException
    {
        // if the response is complete we should not be invoking the phase listeners
        if (isResponseComplete(context, renderExecutor.getPhase(), true))
        {
            return;
        }

        PhaseListenerManager phaseListenerMgr = new PhaseListenerManager(this,
                context, getPhaseListeners());

        try
        {
            phaseListenerMgr.informPhaseListenersBefore(renderExecutor.getPhase());
            // also possible that one of the listeners completed the response
            if (isResponseComplete(context, renderExecutor.getPhase(), true))
            {
                return;
            }

            renderExecutor.execute(context);
        }
        finally
        {
            phaseListenerMgr.informPhaseListenersAfter(renderExecutor.getPhase());
        }

    }

    private boolean isResponseComplete(FacesContext facesContext, PhaseId phase, boolean before)
    {
        boolean flag = false;
        if (facesContext.getResponseComplete())
        {
            flag = true;
        }
        return flag;
    }

    private boolean shouldRenderResponse(FacesContext facesContext, PhaseId phase, boolean before)
    {
        boolean flag = false;
        if (facesContext.getRenderResponse())
        {
            flag = true;
        }
        return flag;
    }

}
