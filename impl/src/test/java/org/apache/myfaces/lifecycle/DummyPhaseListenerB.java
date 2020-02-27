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

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import static org.apache.myfaces.lifecycle.DummyPhaseListenerA.getMsgList;

/**
 *
 * @author lu4242
 */
public class DummyPhaseListenerB implements PhaseListener
{

    @Override
    public void afterPhase(PhaseEvent event)
    {
        getMsgList(event.getFacesContext()).add("DummyPhaseListenerB afterPhase");
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        getMsgList(event.getFacesContext()).add("DummyPhaseListenerB beforePhase");
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.RENDER_RESPONSE;
    }
    
}
