/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class InstrumentingPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = -3222250142846233648L;
    private PhaseId listenPhaseId = null;
    private PhaseId eventPhaseId = null;
    private boolean before = true;
    private boolean after = true;
    private boolean render = false;
    private boolean complete = false;
    private List<PhaseId> afterPhases = new ArrayList<PhaseId>();
    private List<PhaseId> beforePhases = new ArrayList<PhaseId>();

    public InstrumentingPhaseListener()
    {
    }

    public InstrumentingPhaseListener(PhaseId interestingPhaseId)
    {
        this.listenPhaseId = interestingPhaseId;
    }

    public void afterPhase(PhaseEvent event)
    {
        afterPhases.add(event.getPhaseId());
        if (null != eventPhaseId && event.getPhaseId().equals(eventPhaseId))
        {
            if (after && render)
            {
                event.getFacesContext().renderResponse();
            }
            else if (after && complete)
            {
                event.getFacesContext().responseComplete();
            }
        }
    }

    public void beforePhase(PhaseEvent event)
    {
        beforePhases.add(event.getPhaseId());
        if (null != eventPhaseId && event.getPhaseId().equals(eventPhaseId))
        {
            if (before && render)
            {
                event.getFacesContext().renderResponse();
            }
            else if (before && complete)
            {
                event.getFacesContext().responseComplete();
            }
        }
    }

    public boolean isBefore()
    {
        return before;
    }

    public void setBefore(boolean before)
    {
        this.before = before;
    }

    public boolean isAfter()
    {
        return after;
    }

    public void setAfter(boolean after)
    {
        this.after = after;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    public boolean isRender()
    {
        return render;
    }

    public void setRender(boolean render)
    {
        this.render = render;
    }

    public PhaseId getPhaseId()
    {
        if (null == listenPhaseId)
        {
            return PhaseId.ANY_PHASE;
        }
        return listenPhaseId;
    }

    public void setEventPhaseId(PhaseId phaseId)
    {
        this.eventPhaseId = phaseId;
    }

    public List<PhaseId> getAfterPhases()
    {
        return afterPhases;
    }

    public List<PhaseId> getBeforePhases()
    {
        return beforePhases;
    }

}
