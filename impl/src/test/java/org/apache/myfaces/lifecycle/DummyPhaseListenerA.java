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
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

/**
 *
 * @author lu4242
 */
public class DummyPhaseListenerA implements PhaseListener
{

    @Override
    public void afterPhase(PhaseEvent event)
    {
        getMsgList(event.getFacesContext()).add("DummyPhaseListenerA afterPhase");
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        getMsgList(event.getFacesContext()).add("DummyPhaseListenerA beforePhase");
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.RENDER_RESPONSE;
    }

    public static List<String> getMsgList(FacesContext facesContext)
    {
        List<String> list = (List<String>) facesContext.getAttributes().get("msgList");
        if (list == null)
        {
            list = new ArrayList<String>();
            facesContext.getAttributes().put("msgList", list);
        }
        return list;
    }
}
