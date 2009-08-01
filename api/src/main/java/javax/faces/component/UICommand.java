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

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * 
 * UICommand is a base abstraction for components that implement ActionSource.
 * 
 * <h4>Events:</h4>
 * <table border="1" width="100%" cellpadding="3" summary="">
 * <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 * <th align="left">Type</th>
 * <th align="left">Phases</th>
 * <th align="left">Description</th>
 * </tr>
 * <tr class="TableRowColor">
 * <td valign="top"><code>javax.faces.event.ActionEvent</code></td>
 * <td valign="top" nowrap>Invoke Application<br>
 * Apply Request Values</td>
 * <td valign="top">Event delivered when the "action" of the component has been
 * invoked; for example, by clicking on a button. The action may result in page
 * navigation.</td>
 * </tr>
 * </table>
 */
@JSFComponent(defaultRendererType = "javax.faces.Button")
public class UICommand extends UIComponentBase implements ActionSource2
{
    public static final String COMPONENT_TYPE = "javax.faces.Command";
    public static final String COMPONENT_FAMILY = "javax.faces.Command";

    private MethodExpression _actionExpression;
    private MethodBinding _actionListener;

    /**
     * Construct an instance of the UICommand.
     */
    public UICommand()
    {
        setRendererType("javax.faces.Button");
    }

    /**
     * Specifies the action to take when this command is invoked.
     * <p>
     * If the value is an expression, it is expected to be a method 
     * binding EL expression that identifies an action method. An action method
     * accepts no parameters and has a String return value, called the action
     * outcome, that identifies the next view displayed. The phase that this
     * event is fired in can be controlled via the immediate attribute.
     * </p>
     * <p>
     * If the value is a string literal, it is treated as a navigation outcome
     * for the current view.  This is functionally equivalent to a reference to
     * an action method that returns the string literal.
     * </p>
     * 
     * @deprecated Use getActionExpression() instead.
     */
    public MethodBinding getAction()
    {
        MethodExpression actionExpression = getActionExpression();
        if (actionExpression instanceof _MethodBindingToMethodExpression)
        {
            return ((_MethodBindingToMethodExpression) actionExpression)
                    .getMethodBinding();
        }
        if (actionExpression != null)
        {
            return new _MethodExpressionToMethodBinding(actionExpression);
        }
        return null;
    }

    /**
     * @deprecated Use setActionExpression instead.
     */
    public void setAction(MethodBinding action)
    {
        if (action != null)
        {
            setActionExpression(new _MethodBindingToMethodExpression(action));
        }
        else
        {
            setActionExpression(null);
        }
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        super.broadcast(event);

        if (event instanceof ActionEvent)
        {
            FacesContext context = getFacesContext();

            MethodBinding mb = getActionListener();
            if (mb != null)
            {
                mb.invoke(context, new Object[]
                { event });
            }

            ActionListener defaultActionListener = context.getApplication()
                    .getActionListener();
            if (defaultActionListener != null)
            {
                defaultActionListener.processAction((ActionEvent) event);
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
    public boolean isImmediate()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, Boolean.FALSE);
    }

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

    private boolean _isSetActionExpression()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.actionExpressionSet);
        return value == null ? false : value;
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
    @JSFProperty(stateHolder = true, returnSignature = "java.lang.Object", jspName = "action", clientEvent="action")
    public MethodExpression getActionExpression()
    {
        if (_actionExpression != null)
        {
            return _actionExpression;
        }
        ValueExpression expression = getValueExpression("actionExpression");
        if (expression != null)
        {
            return (MethodExpression) expression.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    public void setActionExpression(MethodExpression actionExpression)
    {
        this._actionExpression = actionExpression;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.actionExpressionSet,Boolean.TRUE);
        }
    }
    
    private boolean _isSetActionListener()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.actionListenerSet);
        return value == null ? false : value;
    }

    /**
     * A method binding EL expression that identifies an action listener method to be invoked if
     * this component is activated by the user.
     * <p>
     * An action listener method accepts a parameter of type javax.faces.event.ActionEvent and returns void.
     * The phase that this event is fired in can be controlled via the immediate attribute.
     * 
     * @deprecated
     */
    @JSFProperty(stateHolder = true, returnSignature = "void", methodSignature = "javax.faces.event.ActionEvent")
    public MethodBinding getActionListener()
    {
        if (_actionListener != null)
        {
            return _actionListener;
        }
        ValueExpression expression = getValueExpression("actionListener");
        if (expression != null)
        {
            return (MethodBinding) expression.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * @deprecated
     */
    @JSFProperty(returnSignature="void",methodSignature="javax.faces.event.ActionEvent")
    public void setActionListener(MethodBinding actionListener)
    {
        this._actionListener = actionListener;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.actionListenerSet,Boolean.TRUE);
        }
    }

    public void addActionListener(ActionListener listener)
    {
        addFacesListener(listener);
    }

    public void removeActionListener(ActionListener listener)
    {
        removeFacesListener(listener);
    }

    public ActionListener[] getActionListeners()
    {
        return (ActionListener[]) getFacesListeners(ActionListener.class);
    }

    enum PropertyKeys
    {
         immediate
        , value
        , actionExpressionSet
        , actionListenerSet
    }

    public void markInitialState()
    {
        super.markInitialState();
        if (_actionListener != null && 
            _actionListener instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_actionListener).markInitialState();
        }
        if (_actionExpression != null && 
            _actionExpression instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_actionExpression).markInitialState();
        }
    }
    
    public void clearInitialState()
    {
        if (initialStateMarked())
        {
            super.clearInitialState();
            if (_actionListener != null && 
                _actionListener instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_actionListener).clearInitialState();
            }
            if (_actionExpression != null && 
                _actionExpression instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_actionExpression).clearInitialState();
            }
        }
    }

    @Override
    public Object saveState(FacesContext facesContext)
    {
        if (initialStateMarked())
        {
            boolean nullDelta = true;
            Object parentSaved = super.saveState(facesContext);
            Object actionListenerSaved = null;
            if (!_isSetActionListener() &&
                _actionListener != null && _actionListener instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _actionListener;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    actionListenerSaved = new _AttachedDeltaWrapper(_actionListener.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetActionListener() || _actionListener != null)
            {
                //Full
                actionListenerSaved = saveAttachedState(facesContext,_actionListener);
                nullDelta = false;
            }        
            Object actionExpressionSaved = null;
            if (!_isSetActionExpression() &&
                _actionExpression != null && _actionExpression instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _actionExpression;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    actionExpressionSaved = new _AttachedDeltaWrapper(_actionExpression.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetActionExpression() || _actionExpression != null)
            {
                //Full
                actionExpressionSaved = saveAttachedState(facesContext,_actionExpression);
                nullDelta = false;
            }        
            if (parentSaved == null && nullDelta)
            {
                //No values
                return null;
            }
            
            Object[] values = new Object[3];
            values[0] = parentSaved;
            values[1] = actionListenerSaved;
            values[2] = actionExpressionSaved;
            return values;
        }
        else
        {
            Object[] values = new Object[3];
            values[0] = super.saveState(facesContext);
            values[1] = saveAttachedState(facesContext,_actionListener);
            values[2] = saveAttachedState(facesContext,_actionExpression);
            return values;
        }
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
        if (state == null)
        {
            return;
        }
        
        Object[] values = (Object[])state;
        super.restoreState(facesContext,values[0]);
        if (values[1] instanceof _AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_actionListener).restoreState(facesContext, ((_AttachedDeltaWrapper) values[1]).getWrappedStateObject());
        }
        else
        {
            //Full
            _actionListener = (javax.faces.el.MethodBinding) restoreAttachedState(facesContext,values[1]);
        }         
        if (values[2] instanceof _AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_actionExpression).restoreState(facesContext, ((_AttachedDeltaWrapper) values[2]).getWrappedStateObject());
        }
        else
        {
            //Full
            _actionExpression = (javax.el.MethodExpression) restoreAttachedState(facesContext,values[2]);
        }         
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
