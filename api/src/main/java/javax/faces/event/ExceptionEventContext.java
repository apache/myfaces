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
package javax.faces.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
*
* @since 2.0
* @author Leonardo Uribe (latest modification by $Author$)
* @version $Revision$ $Date$
*/
public class ExceptionEventContext implements SystemEventListenerHolder
{
    public static final String IN_BEFORE_PHASE_KEY = ExceptionEventContext.class
            .getName()
            + ".IN_BEFORE_PHASE";
    public static final String IN_AFTER_PHASE_KEY = ExceptionEventContext.class
            .getName()
            + ".IN_AFTER_PHASE";

    private final FacesContext _facesContext;
    private final Throwable _throwable;
    private final UIComponent _component;
    private final PhaseId _phaseId;
    private final Map<Object, Object> _attributes;

    public ExceptionEventContext(FacesContext facesContext, Throwable throwable)
    {
        this(facesContext, throwable, null, null);
    }

    public ExceptionEventContext(FacesContext facesContext,
            Throwable throwable, UIComponent component)
    {
        this(facesContext, throwable, component, null);
    }

    public ExceptionEventContext(FacesContext facesContext,
            Throwable throwable, UIComponent component, PhaseId phaseId)
    {
        _facesContext = facesContext;
        _throwable = throwable;
        _component = component;
        _phaseId = phaseId;
        _attributes = new HashMap<Object, Object>();
    }

    public FacesContext getContext()
    {
        return _facesContext;
    }

    public Throwable getException()
    {
        return _throwable;
    }

    public UIComponent getComponent()
    {
        return _component;
    }

    public PhaseId getPhaseId()
    {
        return _phaseId;
    }

    public Map<Object, Object> getAttributes()
    {
        return _attributes;
    }

    public List<SystemEventListener> getListenersForEventClass(
            Class<? extends SystemEvent> facesEventClass)
    {
        List<SystemEventListener> ret = new ArrayList<SystemEventListener>(1);
        ret.add(_facesContext.getExceptionHandler());
        return ret;
    }
}
