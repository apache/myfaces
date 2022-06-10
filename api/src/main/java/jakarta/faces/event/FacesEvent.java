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

package jakarta.faces.event;

import java.util.EventObject;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public abstract class FacesEvent extends EventObject
{
    private PhaseId _phaseId;
    
    private transient FacesContext facesContext;

    public FacesEvent(FacesContext facesContext, UIComponent uiComponent)
    {
        this(uiComponent);
        this.facesContext = facesContext;
    }
    
    public FacesEvent(UIComponent uiComponent)
    {
        super(uiComponent);
        if (uiComponent == null)
        {
            throw new IllegalArgumentException("uiComponent");
        }

        _phaseId = PhaseId.ANY_PHASE;
    }

    public abstract boolean isAppropriateListener(FacesListener faceslistener);

    public abstract void processListener(FacesListener faceslistener);

    public UIComponent getComponent()
    {
        return (UIComponent) getSource();
    }

    public void queue()
    {
        ((UIComponent) getSource()).queueEvent(this);
    }

    public PhaseId getPhaseId()
    {
        return _phaseId;
    }

    public void setPhaseId(PhaseId phaseId)
    {
        if (phaseId == null)
        {
            throw new IllegalArgumentException("phaseId");
        }
        _phaseId = phaseId;
    }
    
    public FacesContext getFacesContext()
    {
        if (facesContext == null)
        {
            facesContext = FacesContext.getCurrentInstance();
        }
        return facesContext;
    }
}
