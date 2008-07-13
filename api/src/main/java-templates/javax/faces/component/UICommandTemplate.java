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
package javax.faces.component;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.faces.el.MethodBinding;
import javax.el.MethodExpression;

/**
 * TODO: comment this!
 *
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UICommandTemplate extends UIComponentBase
{
    /**/ // lines starting with /**/ are magically ignored from the generated class

    /**/public abstract boolean isImmediate();
    /**/public abstract ActionListener[] getActionListeners();
    /**/public abstract MethodExpression getActionExpression();
    /**/public abstract void setActionExpression(MethodExpression methodExpression);

    /**
     * @deprecated Use setActionExpression instead.
     */
    public void setAction(MethodBinding action)
    {
        if(action != null)
        {
            setActionExpression(new _MethodBindingToMethodExpression(action));
        } 
        else
        {
            setActionExpression(null);
        }
    }

    /**
     * @deprecated Use getActionExpression() instead.
     */
    public MethodBinding getAction()
    {
        MethodExpression actionExpression = getActionExpression();
        if (actionExpression instanceof _MethodBindingToMethodExpression) {
            return ((_MethodBindingToMethodExpression)actionExpression).getMethodBinding();
        }
        if(actionExpression != null)
        {
            return new _MethodExpressionToMethodBinding(actionExpression);
        }
        return null;
    }

    @Override
    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        super.broadcast(event);

        if (event instanceof ActionEvent)
        {
            FacesContext context = getFacesContext();
            
            MethodBinding mb = getActionListener();
            if(mb != null)
            {
                mb.invoke(context, new Object[] { event });
            }

            ActionListener defaultActionListener
                    = context.getApplication().getActionListener();
            if (defaultActionListener != null)
            {
                defaultActionListener.processAction((ActionEvent)event);
            }
        }
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        if (event != null && event instanceof ActionEvent)
        {
            if (isImmediate())
            {
                event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
            }
            else
            {
                event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            }
        }
        super.queueEvent(event);
    }
}
