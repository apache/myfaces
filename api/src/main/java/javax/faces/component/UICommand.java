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
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.*;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   type = "javax.faces.Command"
 *   family = "javax.faces.Command"
 *   desc = "UICommand executes an action"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UICommand
        extends UIComponentBase
        implements ActionSource
{
    private MethodBinding _action = null;
    private MethodBinding _actionListener = null;

    public void setAction(MethodBinding action)
    {
        _action = action;
    }

    /**
     * Specifies the action to take when this command is invoked.
     *
     * If the value is an expression, it is expected to be a method 
     * binding EL expression that identifies an action method. An action method
     * accepts no parameters and has a String return value, called the action
     * outcome, that identifies the next view displayed. The phase that this
     * event is fired in can be controlled via the immediate attribute.
     *
     * If the value is a string literal, it is treated as a navigation outcome
     * for the current view.  This is functionally equivalent to a reference to
     * an action method that returns the string literal.
     * 
     * @JSFProperty
     *   returnSignature="java.lang.String"
     */
    public MethodBinding getAction()
    {
        return _action;
    }

    public void setActionListener(MethodBinding actionListener)
    {
        _actionListener = actionListener;
    }

    /**
     * A method binding EL expression that identifies an action listener method
     * to be invoked if this component is activated by the user. An action
     * listener method accepts a parameter of type javax.faces.event.ActionEvent
     * and returns void. The phase that this event is fired in can be controlled
     * via the immediate attribute.
     *  
     * @JSFProperty
     *   returnSignature="void"
     *   methodSignature="javax.faces.event.ActionEvent"
     */
    public MethodBinding getActionListener()
    {
        return _actionListener;
    }

    public void addActionListener(ActionListener listener)
    {
        addFacesListener(listener);
    }

    public ActionListener[] getActionListeners()
    {
        return (ActionListener[])getFacesListeners(ActionListener.class);
    }

    public void removeActionListener(ActionListener listener)
    {
        removeFacesListener(listener);
    }

    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        super.broadcast(event);

        if (event instanceof ActionEvent)
        {
            FacesContext context = getFacesContext();

            MethodBinding actionListenerBinding = getActionListener();
            if (actionListenerBinding != null)
            {
                try
                {
                    actionListenerBinding.invoke(context, new Object[] {event});
                }
                catch (EvaluationException e)
                {
                    Throwable cause = e.getCause();
                    if (cause != null && cause instanceof AbortProcessingException)
                    {
                        throw (AbortProcessingException)cause;
                    }
                    else
                    {
                        throw e;
                    }
                }
            }

            ActionListener defaultActionListener
                    = context.getApplication().getActionListener();
            if (defaultActionListener != null)
            {
                defaultActionListener.processAction((ActionEvent)event);
            }
        }
    }

    public void queueEvent(FacesEvent event)
    {
        if (event != null && this == event.getSource() && event instanceof ActionEvent)
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


    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Command";
    public static final String COMPONENT_FAMILY = "javax.faces.Command";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Button";
    private static final boolean DEFAULT_IMMEDIATE = false;

    private Boolean _immediate = null;
    private Object _value = null;

    public UICommand()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setImmediate(boolean immediate)
    {
        _immediate = Boolean.valueOf(immediate);
    }

    

    /**
     * A boolean value that identifies the phase during which action events
     * should fire. During normal event processing, action methods and
     * action listener methods are fired during the "invoke application"
     * phase of request processing. If this attribute is set to "true",
     * these methods are fired instead at the end of the "apply request
     * values" phase.
     * 
     * @JSFProperty
     *   defaultValue="false"
     */
    public boolean isImmediate()
    {
        if (_immediate != null) return _immediate.booleanValue();
        ValueBinding vb = getValueBinding("immediate");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_IMMEDIATE;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    /**
     * The initial value of this component.
     * 
     * @JSFProperty
     */
    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }



    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, _action);
        values[2] = saveAttachedState(context, _actionListener);
        values[3] = _immediate;
        values[4] = _value;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _action = (MethodBinding)restoreAttachedState(context, values[1]);
        _actionListener = (MethodBinding)restoreAttachedState(context, values[2]);
        _immediate = (Boolean)values[3];
        _value = values[4];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
