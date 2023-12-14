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
package jakarta.faces.component;

import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFListener;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * 
 * UICommand is a base abstraction for components that implement ActionSource.
 * 
 */
@JSFComponent(defaultRendererType = "jakarta.faces.Button")
public class UICommand extends UIComponentBase implements ActionSource
{
    public static final String COMPONENT_TYPE = "jakarta.faces.Command";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Command";

    /**
     * Construct an instance of the UICommand.
     */
    public UICommand()
    {
        setRendererType("jakarta.faces.Button");
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        super.broadcast(event);

        if (event instanceof ActionEvent actionEvent)
        {
            FacesContext context = getFacesContext();

            ActionListener defaultActionListener = context.getApplication()
                    .getActionListener();
            if (defaultActionListener != null)
            {
                defaultActionListener.processAction(actionEvent);
            }
        }
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        if (event != null && event instanceof ActionEvent)
        {
            UIComponent component = event.getComponent();
            if (component instanceof ActionSource source)
            {
                if (source.isImmediate())
                {
                    event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                }
                else
                {
                    event.setPhaseId(PhaseId.INVOKE_APPLICATION);
                }
            }
        }
        super.queueEvent(event);
    }

    /**
     * A boolean value that identifies the phase during which action events
     * should fire.
     * <p>
     * During normal event processing, action methods and action listener methods are fired during the
     * "invoke application" phase of request processing. If this attribute is set to "true", these methods
     * are fired instead at the end of the "apply request values" phase.
     * </p>
     */
    @JSFProperty
    @Override
    public boolean isImmediate()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, Boolean.FALSE);
    }

    @Override
    public void setImmediate(boolean immediate)
    {
        getStateHelper().put(PropertyKeys.immediate, immediate );
    }

    /**
     * The text to display to the user for this command component.
     */
    @JSFProperty
    public Object getValue()
    {
        return  getStateHelper().eval(PropertyKeys.value);
    }

    public void setValue(Object value)
    {
        getStateHelper().put(PropertyKeys.value, value );
    }

    /**
     * The action to take when this command is invoked.
     * <p>
     * If the value is an expression, it is expected to be a method binding EL expression that identifies
     * an action method. An action method accepts no parameters and has a String return value, called the
     * action outcome, that identifies the next view displayed. The phase that this event is fired in
     * can be controlled via the immediate attribute.
     * </p>
     * <p> 
     * If the value is a string literal, it is treated as a navigation outcome for the current view. This
     * is functionally equivalent to a reference to an action method that returns the string literal.
     * </p>
     */
    @JSFProperty(stateHolder=true, returnSignature = "java.lang.Object", clientEvent="action")
    @Override
    public MethodExpression getActionExpression()
    {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.actionExpression);
    }

    @Override
    public void setActionExpression(MethodExpression actionExpression)
    {
        getStateHelper().put(PropertyKeys.actionExpression, actionExpression);
    }

    @Override
    public void addActionListener(ActionListener listener)
    {
        addFacesListener(listener);
    }

    @Override
    public void removeActionListener(ActionListener listener)
    {
        removeFacesListener(listener);
    }

    /**
     * Event delivered when the "action" of the component has been
     * invoked; for example, by clicking on a button. The action may result 
     * in page navigation.
     */
    @JSFListener(event="jakarta.faces.event.ActionEvent",
            phases="Invoke Application, Apply Request Values")
    @Override
    public ActionListener[] getActionListeners()
    {
        return (ActionListener[]) getFacesListeners(ActionListener.class);
    }

    enum PropertyKeys
    {
         immediate
        , value
        , actionExpression
        , actionListener
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
