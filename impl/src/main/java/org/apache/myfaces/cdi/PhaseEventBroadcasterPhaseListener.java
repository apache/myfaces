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
package org.apache.myfaces.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AfterPhase;
import jakarta.faces.event.BeforePhase;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.inject.Inject;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.util.lang.Lazy;

public class PhaseEventBroadcasterPhaseListener implements PhaseListener
{
    private Lazy<PhaseEventBroadcaster> broadcaster = new Lazy<>(() ->
    {
        BeanManager beanManager = CDIUtils.getBeanManager(FacesContext.getCurrentInstance());
        if (beanManager == null)
        {
            return null;
        }
        return CDIUtils.get(beanManager, PhaseEventBroadcaster.class);
    });

    @Override
    public void beforePhase(PhaseEvent phaseEvent)
    {
        PhaseEventBroadcaster broadcaster = this.broadcaster.get();
        if (broadcaster != null)
        {
            broadcaster.broadcastBeforeEvent(phaseEvent);
        }
    }

    @Override
    public void afterPhase(PhaseEvent phaseEvent)
    {
        PhaseEventBroadcaster broadcaster = this.broadcaster.get();
        if (broadcaster != null)
        {
            broadcaster.broadcastAfterEvent(phaseEvent);
        }
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    @ApplicationScoped
    public static class PhaseEventBroadcaster
    {
        @Inject
        private Event<PhaseEvent> phaseEvent;

        @Inject
        @BeforePhase(PhaseId.ANY_PHASE)
        private Event<PhaseEvent> beforeAnyPhaseEvent;

        @Inject
        @AfterPhase(PhaseId.ANY_PHASE)
        private Event<PhaseEvent> afterAnyPhaseEvent;

        protected void broadcastBeforeEvent(PhaseEvent phaseEvent)
        {
            this.phaseEvent.select(BeforePhase.Literal.of(phaseEvent.getPhaseId())).fire(phaseEvent);
            this.beforeAnyPhaseEvent.fire(phaseEvent);
        }

        protected void broadcastAfterEvent(PhaseEvent phaseEvent)
        {
            this.phaseEvent.select(AfterPhase.Literal.of(phaseEvent.getPhaseId())).fire(phaseEvent);
            this.afterAnyPhaseEvent.fire(phaseEvent);
        }
    }
}
