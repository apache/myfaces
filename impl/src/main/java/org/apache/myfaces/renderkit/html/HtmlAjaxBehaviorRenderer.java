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
package org.apache.myfaces.renderkit.html;


import jakarta.faces.FacesException;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.ClientBehaviorRenderer;

import org.apache.myfaces.renderkit.html.util.AjaxScriptBuilder;
import org.apache.myfaces.shared.renderkit.html.util.SharedStringBuilder;


/**
 * @author Werner Punz  (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlAjaxBehaviorRenderer extends ClientBehaviorRenderer
{


    private static final String ERR_NO_AJAX_BEHAVIOR = "The behavior must be an instance of AjaxBehavior";

    private static final String AJAX_SB = "oam.renderkit.AJAX_SB";

    @Override
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior)
    {
        assertBehavior(behavior);
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
        if (ajaxBehavior.isDisabled() || !component.isRendered())
        {
            return;
        }

        dispatchBehaviorEvent(component, ajaxBehavior);
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior)
    {
        assertBehavior(behavior);
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;

        if (ajaxBehavior.isDisabled())
        {
            return null;
        }

        StringBuilder retVal = SharedStringBuilder.get(behaviorContext.getFacesContext(), AJAX_SB, 60);

        AjaxScriptBuilder.build(behaviorContext.getFacesContext(),
                retVal,
                behaviorContext.getComponent(),
                behaviorContext.getSourceId(),
                behaviorContext.getEventName(),
                ajaxBehavior.getExecute(),
                ajaxBehavior.getRender(),
                ajaxBehavior.getDelay(),
                ajaxBehavior.isResetValues(),
                ajaxBehavior.getOnerror(),
                ajaxBehavior.getOnevent(),
                behaviorContext.getParameters());

        return retVal.toString();
    }

    private void dispatchBehaviorEvent(UIComponent component, AjaxBehavior ajaxBehavior)
    {
        AjaxBehaviorEvent event = new AjaxBehaviorEvent(component, ajaxBehavior);

        boolean isImmediate = false;
        if (ajaxBehavior.isImmediateSet())
        {
            isImmediate = ajaxBehavior.isImmediate();
        }
        else
        {
            isImmediate = isComponentImmediate(component);
        }

        PhaseId phaseId = isImmediate ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.INVOKE_APPLICATION;

        event.setPhaseId(phaseId);

        component.queueEvent(event);
    }


    private boolean isComponentImmediate(UIComponent component)
    {
        boolean isImmediate = false;
        if (component instanceof EditableValueHolder)
        {
            isImmediate = ((EditableValueHolder)component).isImmediate();
        }
        else if (component instanceof ActionSource)
        {
            isImmediate = ((ActionSource)component).isImmediate();
        }
        return isImmediate;
    }

    private void assertBehavior(ClientBehavior behavior)
    {
        if (!(behavior instanceof AjaxBehavior))
        {
            throw new FacesException(ERR_NO_AJAX_BEHAVIOR);
        }
    }

}
