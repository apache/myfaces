// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.

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
 * <td valign="top" nowrap>Invoke Application<br>Apply Request Values</td>
 * <td valign="top">Event delivered when the "action" of the component has been
invoked;  for example, by clicking on a button.  The action may result
in page navigation.</td>
 * </tr>
 * </table>
 */
@JSFComponent
(defaultRendererType = "javax.faces.Button"
)
public class UICommand extends UIComponentBase
                       implements ActionSource2
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.Command";
  static public final String COMPONENT_TYPE =
    "javax.faces.Command";

  /**
   * Construct an instance of the UICommand.
   */
  public UICommand()
  {
    setRendererType("javax.faces.Button");
  }
  

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

  // Property: immediate
  private boolean _immediate;
  private boolean _immediateSet;

  /**
   * Gets A boolean value that identifies the phase during which action events
   *         should fire. During normal event processing, action methods and
   *         action listener methods are fired during the "invoke application"
   *         phase of request processing. If this attribute is set to "true",
   *         these methods are fired instead at the end of the "apply request
   *         values" phase.
   *
   * @return  the new immediate value
   */
  @JSFProperty
  public boolean isImmediate()
  {
    if (_immediateSet)
    {
      return _immediate;
    }
    ValueExpression expression = getValueExpression("immediate");
    if (expression != null)
    {
      return (Boolean)expression.getValue(getFacesContext().getELContext());
    }
    return false;
  }

  /**
   * Sets A boolean value that identifies the phase during which action events
   *         should fire. During normal event processing, action methods and
   *         action listener methods are fired during the "invoke application"
   *         phase of request processing. If this attribute is set to "true",
   *         these methods are fired instead at the end of the "apply request
   *         values" phase.
   * 
   * @param immediate  the new immediate value
   */
  public void setImmediate(boolean immediate)
  {
    this._immediate = immediate;
    this._immediateSet = true;
  }

  // Property: value
  private Object _value;

  /**
   * Gets The initial value of this component.
   *
   * @return  the new value value
   */
  @JSFProperty
  public Object getValue()
  {
    if (_value != null)
    {
      return _value;
    }
    ValueExpression expression = getValueExpression("value");
    if (expression != null)
    {
      return expression.getValue(getFacesContext().getELContext());
    }
    return null;
  }

  /**
   * Sets The initial value of this component.
   * 
   * @param value  the new value value
   */
  public void setValue(Object value)
  {
    this._value = value;
  }

  // Property: actionExpression
  private MethodExpression _actionExpression;

  /**
   * Gets Specifies the action to take when this command is invoked.
   *         If the value is an expression, it is expected to be a method
   *         binding EL expression that identifies an action method. An action method
   *         accepts no parameters and has a String return value, called the action
   *         outcome, that identifies the next view displayed. The phase that this
   *         event is fired in can be controlled via the immediate attribute.
   * 
   *         If the value is a string literal, it is treated as a navigation outcome
   *         for the current view.  This is functionally equivalent to a reference to
   *         an action method that returns the string literal.
   *
   * @return  the new actionExpression value
   */
  @JSFProperty
  (stateHolder = true,
  returnSignature = "java.lang.Object",
  jspName = "action")
  public MethodExpression getActionExpression()
  {
    if (_actionExpression != null)
    {
      return _actionExpression;
    }
    ValueExpression expression = getValueExpression("actionExpression");
    if (expression != null)
    {
      return (MethodExpression)expression.getValue(getFacesContext().getELContext());
    }
    return null;
  }

  /**
   * Sets Specifies the action to take when this command is invoked.
   *         If the value is an expression, it is expected to be a method
   *         binding EL expression that identifies an action method. An action method
   *         accepts no parameters and has a String return value, called the action
   *         outcome, that identifies the next view displayed. The phase that this
   *         event is fired in can be controlled via the immediate attribute.
   * 
   *         If the value is a string literal, it is treated as a navigation outcome
   *         for the current view.  This is functionally equivalent to a reference to
   *         an action method that returns the string literal.
   * 
   * @param actionExpression  the new actionExpression value
   */
  public void setActionExpression(MethodExpression actionExpression)
  {
    this._actionExpression = actionExpression;
  }

  // Property: actionListener
  private MethodBinding _actionListener;

  /**
   * Gets A method binding EL expression that identifies an action listener method
   *         to be invoked if this component is activated by the user. An action
   *         listener method accepts a parameter of type javax.faces.event.ActionEvent
   *         and returns void. The phase that this event is fired in can be controlled
   *         via the immediate attribute.
   *
   * @return  the new actionListener value
   * @deprecated
   */
  @JSFProperty
  (stateHolder = true,
  returnSignature = "void",
  methodSignature = "javax.faces.event.ActionEvent")
  public MethodBinding getActionListener()
  {
    if (_actionListener != null)
    {
      return _actionListener;
    }
    ValueExpression expression = getValueExpression("actionListener");
    if (expression != null)
    {
      return (MethodBinding)expression.getValue(getFacesContext().getELContext());
    }
    return null;
  }

  /**
   * Sets A method binding EL expression that identifies an action listener method
   *         to be invoked if this component is activated by the user. An action
   *         listener method accepts a parameter of type javax.faces.event.ActionEvent
   *         and returns void. The phase that this event is fired in can be controlled
   *         via the immediate attribute.
   * 
   * @param actionListener  the new actionListener value
   * @deprecated
   */
  public void setActionListener(MethodBinding actionListener)
  {
    this._actionListener = actionListener;
  }

  /**
   * Adds a action listener.
   *
   * @param listener  the action listener to add
   */
  public void addActionListener(
    ActionListener listener)
  {
    addFacesListener(listener);
  }

  /**
   * Removes a action listener.
   *
   * @param listener  the action listener to remove
   */
  public void removeActionListener(
    ActionListener listener)
  {
    removeFacesListener(listener);
  }

  /**
   * Returns an array of attached action listeners.
   *
   * @return  an array of attached action listeners.
   */
  public ActionListener[] getActionListeners()
  {
    return (ActionListener[])getFacesListeners(ActionListener.class);
  }

  @Override
  public Object saveState(FacesContext facesContext)
  {
    Object[] values = new Object[6];
    values[0] = super.saveState(facesContext);
    values[1] = _immediate;
    values[2] = _immediateSet;
    values[3] = _value;
    values[4] = saveAttachedState(facesContext, _actionExpression);
    values[5] = saveAttachedState(facesContext, _actionListener);

    return values;
  }

  @Override
  public void restoreState(FacesContext facesContext, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(facesContext,values[0]);
    _immediate = (Boolean)values[1];
    _immediateSet = (Boolean)values[2];
    _value = values[3];
    _actionExpression = (MethodExpression)restoreAttachedState(facesContext, values[4]);
    _actionListener = (MethodBinding)restoreAttachedState(facesContext, values[5]);
  }

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }
}
