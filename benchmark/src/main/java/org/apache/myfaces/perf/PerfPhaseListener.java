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

package org.apache.myfaces.perf;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Records per-(viewId, phase) wall-clock time across the Faces lifecycle. Implementation-agnostic
 * (uses only the jakarta.faces API), so the same listener runs under MyFaces and Mojarra. Results
 * are dumped by {@link PerfStatsServlet}.
 */
public class PerfPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = 1L;

    /** key = viewId + "|" + phaseName -> accumulators */
    public static final Map<String, Stat> STATS = new ConcurrentHashMap<>();

    /** per-thread phase start timestamps, indexed by PhaseId ordinal (RENDER_RESPONSE == 6). */
    private static final ThreadLocal<long[]> STARTS = ThreadLocal.withInitial(() -> new long[8]);

    public static final class Stat
    {
        public final LongAdder count = new LongAdder();
        public final LongAdder totalNanos = new LongAdder();
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        int ord = event.getPhaseId().getOrdinal();
        if (ord >= 0 && ord < 8)
        {
            STARTS.get()[ord] = System.nanoTime();
        }
    }

    @Override
    public void afterPhase(PhaseEvent event)
    {
        int ord = event.getPhaseId().getOrdinal();
        if (ord < 0 || ord >= 8)
        {
            return;
        }
        long start = STARTS.get()[ord];
        if (start == 0L)
        {
            return;
        }
        long elapsed = System.nanoTime() - start;

        String viewId = null;
        if (event.getFacesContext().getViewRoot() != null)
        {
            viewId = event.getFacesContext().getViewRoot().getViewId();
        }
        if (viewId == null)
        {
            viewId = "?";
        }
        String key = viewId + "|" + event.getPhaseId().getName();
        Stat stat = STATS.computeIfAbsent(key, k -> new Stat());
        stat.count.increment();
        stat.totalNanos.add(elapsed);
    }
}
