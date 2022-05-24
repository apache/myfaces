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
package org.apache.myfaces.el;

import jakarta.el.ELResolver;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import java.util.HashMap;
import java.util.Map;

public class TracingELResolverPhaseListener implements PhaseListener
{
    private static final String TRACING_INFOS = TracingELResolverPhaseListener.class.getName() + "#TRACING_INFOS";

    private static class TracingInfo
    {
        int getValue = 0;
        int getValueHits = 0;
        int getType = 0;
        int getTypeHits = 0;
    }

    public static void getValue(Class<? extends ELResolver> resolver)
    {
        getTracingInfo(resolver).getValue += 1;
    }

    public static void getValueHit(Class<? extends ELResolver> resolver)
    {
        getTracingInfo(resolver).getValueHits += 1;
    }
    
    public static void getType(Class<? extends ELResolver> resolver)
    {
        getTracingInfo(resolver).getType += 1;
    }
    
    public static void getTypeHit(Class<? extends ELResolver> resolver)
    {
        getTracingInfo(resolver).getTypeHits += 1;
    }

    public static TracingInfo getTracingInfo(Class<? extends ELResolver> resolver)
    {
        return getTracingInfos().computeIfAbsent(resolver, (k) -> new TracingInfo());
    }

    public static Map<Class<? extends ELResolver>, TracingInfo> getTracingInfos()
    {
        return (Map<Class<? extends ELResolver>, TracingInfo>) FacesContext.getCurrentInstance().getAttributes()
                .computeIfAbsent(TRACING_INFOS, (k) -> new HashMap<>());
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.RENDER_RESPONSE;
    }

    @Override
    public void afterPhase(PhaseEvent event)
    {
        ExternalContext externalContext = event.getFacesContext().getExternalContext();
        for (Map.Entry<Class<? extends ELResolver>, TracingInfo> entry : getTracingInfos().entrySet())
        {
            TracingInfo info = entry.getValue();

            externalContext.log("### ELResolver: " + entry.getKey().getName());
            externalContext.log("    getValue - invocations " + info.getValue + "; hits: " + info.getValueHits);
            externalContext.log("    getType - invocations " + info.getType + "; hits: " + info.getTypeHits);
        }
    }
}
