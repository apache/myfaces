/*
 * Copyright 2004 The Apache Software Foundation.
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
package javax.faces.component;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.*;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UICommand
        extends UIComponentBase
        implements ActionSource2
{
    private _MethodBindingToActionListener _actionListener = null;
    private MethodExpression _action = null;

    /**
     * @deprecated Use setActionExpression instead.
     */
    public void setAction(MethodBinding action)
    {
        setActionExpression(new _MethodBindingToMethodExpression(action));
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
        
        return new _MethodExpressionToMethodBinding(actionExpression);
    }

    /**
     * @deprecated Use addActionListener() instead.
     */
    public void setActionListener(MethodBinding actionListener)
    {
        // remove previous listener
        if (_actionListener != null) {
            removeActionListener(_actionListener);
        }
        
        _actionListener = new _MethodBindingToActionListener(actionListener);
        addActionListener(_actionListener);
    }

    /**
     * @deprecated Use getActionListeners() instead.
     */
    public MethodBinding getActionListener()
    {
        if (_actionListener == null) return null;
        
        return _actionListener.getMethodBinding();
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
    
// ----- ActionSource2 methods ----
    public void setActionExpression(MethodExpression action) {
        _action = action;
    }

    public MethodExpression getActionExpression() {
        return _action;
    }
// ---------------------------------

    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        super.broadcast(event);

        if (event instanceof ActionEvent)
        {
            FacesContext context = getFacesContext();

            for (ActionListener listener : getActionListeners())
            {
                listener.processAction((ActionEvent)event);
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

    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? (Object)vb.getValue(getFacesContext()) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, _action);
        values[2] = saveAttachedState(context, _actionListener);
        values[3] = _immediate;
        values[4] = _value;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _action = (MethodExpression)restoreAttachedState(context, values[1]); // changed this line - TODO: fix src generator
        _actionListener = (_MethodBindingToActionListener)restoreAttachedState(context, values[2]); // changed this line - TODO: fix src generator
        _immediate = (Boolean)values[3];
        _value = (Object)values[4];
    }
    //------------------ GENERATED CODE END ---------------------------------------

}
