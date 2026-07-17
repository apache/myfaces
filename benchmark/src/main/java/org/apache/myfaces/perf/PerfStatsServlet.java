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

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Plain servlet (no Faces dependency) that dumps the per-(viewId, phase) timings accumulated by
 * {@link PerfPhaseListener}. {@code GET /perf-stats} prints a table; {@code GET /perf-stats?reset=1}
 * clears the accumulators (call it after warmup, before the measured run).
 */
@WebServlet("/perf-stats")
public class PerfStatsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        if (req.getParameter("reset") != null)
        {
            PerfPhaseListener.STATS.clear();
            resp.setContentType("text/plain");
            resp.getWriter().println("reset");
            return;
        }

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.printf("%-40s %-22s %10s %14s %12s%n", "viewId", "phase", "count", "total_us", "avg_us");
        out.println("-".repeat(102));

        List<String> keys = new ArrayList<>(PerfPhaseListener.STATS.keySet());
        keys.sort(String::compareTo);
        for (String key : keys)
        {
            PerfPhaseListener.Stat stat = PerfPhaseListener.STATS.get(key);
            long count = stat.count.sum();
            long totalUs = stat.totalNanos.sum() / 1000L;
            double avgUs = count == 0 ? 0 : (double) totalUs / count;
            int bar = key.indexOf('|');
            String viewId = key.substring(0, bar);
            String phase = key.substring(bar + 1);
            out.printf("%-40s %-22s %10d %14d %12.2f%n", viewId, phase, count, totalUs, avgUs);
        }
    }
}
