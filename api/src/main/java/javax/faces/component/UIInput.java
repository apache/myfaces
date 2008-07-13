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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.FacesException;
import java.util.ArrayList;
import java.util.List;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   type = "javax.faces.Input"
 *   family = "javax.faces.Input"
 *   desc = "UIInput"
 *   
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIInput
        extends UIOutput
        implements EditableValueHolder
{
    public static final String CONVERSION_MESSAGE_ID = "javax.faces.component.UIInput.CONVERSION";
    public static final String REQUIRED_MESSAGE_ID = "javax.faces.component.UIInput.REQUIRED";
    private static final String ERROR_HANDLING_EXCEPTION_LIST = "org.apache.myfaces.errorHandling.exceptionList";

    private static final Validator[] EMPTY_VALIDATOR_ARRAY = new Validator[0];

    private Object _submittedValue = null;
    private boolean _localValueSet = false;
    private boolean _valid = true;
    private MethodBinding _validator = null;
    private MethodBinding _valueChangeListener = null;
    private List _validatorList = null;

    // use javadoc inherited from EditableValueHolder
    /**
     * @JSFProperty
     *   tagExcluded = "true"
     */
    public Object getSubmittedValue()
    {
        return _submittedValue;
    }

    // use javadoc inherited from EditableValueHolder
    public void setSubmittedValue(Object submittedValue)
    {
        _submittedValue = submittedValue;
    }

    /**
     * Store the specified object as the "local value" of this component.
     * The value-binding named "value" (if any) is ignored; the object is
     * only stored locally on this component. During the "update model"
     * phase, if there is a value-binding named "value" then this local
     * value will be stored via that value-binding and the "local value"
     * reset to null.
     */
    public void setValue(Object value)
    {
        setLocalValueSet(true);
        super.setValue(value);
    }

    public Object getValue()
    {
        if (isLocalValueSet()) return super.getLocalValue();
        return super.getValue();
    }

    // use javadoc inherited from EditableValueHolder
    /**
     * @JSFProperty
     *   tagExcluded = "true"
     */
    public boolean isLocalValueSet()
    {
        return _localValueSet;
    }

    // use javadoc inherited from EditableValueHolder
    public void setLocalValueSet(boolean localValueSet)
    {
        _localValueSet = localValueSet;
    }

    // use javadoc inherited from EditableValueHolder
    /**
     * @JSFProperty
     *   tagExcluded = "true"
     */
    public boolean isValid()
    {
        return _valid;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValid(boolean valid)
    {
        _valid = valid;
    }

    // use javadoc inherited from EditableValueHolder    
    /**
     * A method binding EL expression, accepting FacesContext, UIComponent,
     * and Object parameters, and returning void, that validates the
     * component's local value.
     * 
     * @JSFProperty
     *   stateHolder="true"
     *   returnSignature="void"
     *   methodSignature="javax.faces.context.FacesContext,javax.faces.component.UIComponent,java.lang.Object"
     */
    public MethodBinding getValidator()
    {
        return _validator;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValidator(MethodBinding validator)
    {
        _validator = validator;
    }

    // use javadoc inherited from EditableValueHolder
    /**
     * A method binding EL expression, accepting a ValueChangeEvent parameter
     * and returning void. The specified method is invoked if this component
     * is modified. The phase that this handler is fired in can be controlled
     * via the immediate attribute.
     * 
     * @JSFProperty
     *   stateHolder="true"
     *   returnSignature="void"
     *   methodSignature="javax.faces.event.ValueChangeEvent"
     */
    public MethodBinding getValueChangeListener()
    {
        return _valueChangeListener;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValueChangeListener(MethodBinding valueChangeListener)
    {
        _valueChangeListener = valueChangeListener;
    }

    /**
     * Set the "submitted value" of this component from the relevant data
     * in the current servlet request object.
     * <p>
     * If this component is not rendered, then do nothing; no output would
     * have been sent to the client so no input is expected.
     * <p>
     * Invoke the inherited functionality, which typically invokes the
     * renderer associated with this component to extract and set this
     * component's "submitted value".
     * <p>
     * If this component is marked "immediate", then immediately apply
     * validation to the submitted value found. On error, call context
     * method "renderResponse" which will force processing to leap to
     * the "render response" phase as soon as the "decode" step has
     * completed for all other components.
     */
    public void processDecodes(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        super.processDecodes(context);
        if (isImmediate())
        {
            try
            {
                validate(context);
            }
            catch (RuntimeException e)
            {
                context.renderResponse();
                throw e;
            }
            if (!isValid())
            {
                context.renderResponse();
            }
        }
    }

    public void processValidators(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        super.processValidators(context);

        if (!isImmediate())
        {
            try
            {
                validate(context);
            }
            catch (RuntimeException e)
            {
                context.renderResponse();
                throw e;
            }
            if (!isValid())
            {
                context.renderResponse();
            }
        }
    }

    public void processUpdates(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        super.processUpdates(context);

        try
        {
            updateModel(context);
        }
        catch (RuntimeException e)
        {
            context.renderResponse();
            throw e;
        }
        if (!isValid())
        {
            context.renderResponse();
        }
    }

    public void decode(FacesContext context)
    {
        //We (re)set to valid, so that component automatically gets (re)validated
        setValid(true);
        super.decode(context);
    }

    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        // invoke standard listeners attached to this component first
        super.broadcast(event);

        //Check if the event is applicable for ValueChangeListener
        if (event instanceof ValueChangeEvent){
            // invoke the single listener defined directly on the component
            MethodBinding valueChangeListenerBinding = getValueChangeListener();
            if (valueChangeListenerBinding != null)
            {
                try
                {
                    valueChangeListenerBinding.invoke(getFacesContext(),
                                                      new Object[]{event});
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
        }
    }

    public void updateModel(FacesContext context)
    {
        if (!isValid()) return;
        if (!isLocalValueSet()) return;
        ValueBinding vb = getValueBinding("value");
        if (vb == null) return;
        try
        {
            vb.setValue(context, getLocalValue());
            setValue(null);
            setLocalValueSet(false);
        }
         catch (Exception e)
        {
            //Object[] args = {getId()};
            context.getExternalContext().log(e.getMessage(), e);
            _MessageUtils.addErrorMessage(context, this,CONVERSION_MESSAGE_ID,new Object[]{getId()});
            setValid(false);

            /* we are not allowed to throw exceptions here - we still need the full stack-trace later on
             * to process it later in our error-handler
             */
            queueExceptionInRequest(context, vb, e);
        }
    }

    /**
     * For development and production, we want to offer a single point
     * to which error-handlers can attach. So we queue up all ocurring
     * exceptions and later pass them to the configured error-handler.
     *
     * @param context
     * @param binding
     * @param e
     */
    private void queueExceptionInRequest(FacesContext context, ValueBinding binding, Exception e) {
        List li = (List) context.getExternalContext().getRequestMap().get(ERROR_HANDLING_EXCEPTION_LIST);
        if(null==li) {
            li = new ArrayList();
            context.getExternalContext().getRequestMap().put(ERROR_HANDLING_EXCEPTION_LIST, li);
        }
        li.add(new FacesException("Exception while setting value for expression : "+
            binding.getExpressionString()+" of component with path : "
            + _ComponentUtils.getPathToComponent(this),e));
    }

    protected void validateValue(FacesContext context,Object convertedValue)
    {
        boolean empty = convertedValue == null ||
                        (convertedValue instanceof String
                         && ((String)convertedValue).length() == 0);

        if (isRequired() && empty)
        {
            _MessageUtils.addErrorMessage(context, this, REQUIRED_MESSAGE_ID,new Object[]{getId()});
            setValid(false);
            return;
        }

        if (!empty)
        {
            _ComponentUtils.callValidators(context, this, convertedValue);
        }

    }

    /**
     * Determine whether the new value is valid, and queue a ValueChangeEvent
     * if necessary.
     * <p>
     * The "submitted value" is converted to the necessary type; conversion
     * failure is reported as an error and validation processing terminates
     * for this component. See documentation for method getConvertedValue
     * for details on the conversion process.
     * <p>
     * Any validators attached to this component are then run, passing
     * the converted value.
     * <p>
     * The old value of this component is then fetched (possibly involving
     * the evaluation of a value-binding expression, ie invoking a method
     * on a user object). The old value is compared to the new validated
     * value, and if they are different then a ValueChangeEvent is queued
     * for later processing.
     * <p>
     * On successful completion of this method:
     * <ul>
     * <li> isValid() is true
     * <li> isLocalValueSet() is true
     * <li> submittedValue is reset to null
     * <li> a ValueChangeEvent is queued if the new value != old value
     * </ul> 
     */
    public void validate(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");

        try {

            Object submittedValue = getSubmittedValue();
            if (submittedValue == null) return;

            Object convertedValue = getConvertedValue(context, submittedValue);

            if (!isValid()) return;

            validateValue(context, convertedValue);

            if (!isValid()) return;

            Object previousValue = getValue();
            setValue(convertedValue);
            setSubmittedValue(null);
            if (compareValues(previousValue, convertedValue))
            {
                queueEvent(new ValueChangeEvent(this, previousValue, convertedValue));
            }
        }
        catch (Exception ex)
        {
            throw new FacesException("Exception while validating component with path : "+_ComponentUtils.getPathToComponent(this),ex);
        }
    }

    /**
     * Convert the provided object to the desired value.
     * <p>
     * If there is a renderer for this component, then call the renderer's
     * getConvertedValue method. While this can of course be implemented in
     * any way the renderer desires, it typically performs exactly the same
     * processing that this method would have done anyway (ie that described
     * below for the no-renderer case).
     * <p>
     * Otherwise:
     * <ul>
     * <li>If the submittedValue is not a String then just return the
     *   submittedValue unconverted.
     * <li>If there is no "value" value-binding then just return the
     *   submittedValue unconverted.
     * <li>Use introspection to determine the type of the target 
     *   property specified by the value-binding, and then use
     *   Application.createConverter to find a converter that can
     *   map from String to the required type. Apply the converter
     *   to the submittedValue and return the result. 
     * </ul>
     */
    protected Object getConvertedValue(FacesContext context, Object submittedValue)
    {
        try
        {
            Renderer renderer = getRenderer(context);
            if (renderer != null)
            {
                return renderer.getConvertedValue(context, this, submittedValue);
            }
            else if (submittedValue instanceof String)
            {
                Converter converter = _SharedRendererUtils.findUIOutputConverter(context, this);
                if (converter != null)
                {
                    return converter.getAsObject(context, this, (String)submittedValue);
                }
            }
        }
        catch (ConverterException e)
        {
            FacesMessage facesMessage = e.getFacesMessage();
            if (facesMessage != null)
            {
                context.addMessage(getClientId(context), facesMessage);
            }
            else
            {
                _MessageUtils.addErrorMessage(context, this, CONVERSION_MESSAGE_ID,new Object[]{getId()});
            }
            setValid(false);
        }
        return submittedValue;
    }



    protected boolean compareValues(Object previous,
                                      Object value)
    {
        return previous==null?(value!=null):(!previous.equals(value));
    }

    public void addValidator(Validator validator)
    {
        if (validator == null) throw new NullPointerException("validator");
        if (_validatorList == null)
        {
            _validatorList = new ArrayList();
        }
        _validatorList.add(validator);
    }

    public Validator[] getValidators()
    {
        return _validatorList != null ?
               (Validator[])_validatorList.toArray(new Validator[_validatorList.size()]) :
               EMPTY_VALIDATOR_ARRAY;
    }

    public void removeValidator(Validator validator)
    {
        if (validator == null) throw new NullPointerException("validator");
        if (_validatorList != null)
        {
            _validatorList.remove(validator);
        }
    }

    public void addValueChangeListener(ValueChangeListener listener)
    {
        addFacesListener(listener);
    }

    public ValueChangeListener[] getValueChangeListeners()
    {
        return (ValueChangeListener[])getFacesListeners(ValueChangeListener.class);
    }

    public void removeValueChangeListener(ValueChangeListener listener)
    {
        removeFacesListener(listener);
    }

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[9];
        values[0] = super.saveState(context);
        values[1] = _immediate;
        values[2] = Boolean.valueOf(_localValueSet);
        values[3] = _required;
        values[4] = _submittedValue;
        values[5] = Boolean.valueOf(_valid);
        values[6] = saveAttachedState(context, _validator);
        values[7] = saveAttachedState(context, _valueChangeListener);
        values[8] = saveAttachedState(context, _validatorList);
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _immediate = (Boolean)values[1];
        _localValueSet = ((Boolean)values[2]).booleanValue();
        _required = (Boolean)values[3];
        _submittedValue = values[4];
        _valid = ((Boolean)values[5]).booleanValue();
        _validator = (MethodBinding)restoreAttachedState(context, values[6]);
        _valueChangeListener = (MethodBinding)restoreAttachedState(context, values[7]);
        _validatorList = (List)restoreAttachedState(context, values[8]);

    }


    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Input";
    public static final String COMPONENT_FAMILY = "javax.faces.Input";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Text";
    private static final boolean DEFAULT_IMMEDIATE = false;
    private static final boolean DEFAULT_REQUIRED = false;

    private Boolean _immediate = null;
    private Boolean _required = null;

    public UIInput()
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
     * A boolean value that identifies the phase during which value change
     * events should fire. During normal event processing, value change
     * events are fired during the "process validations" phase of request
     * processing. If this attribute is set to "true", these methods are
     * fired instead at the end of the "apply request values" phase.
     * 
     * @JSFProperty
     */
    public boolean isImmediate()
    {
        if (_immediate != null) return _immediate.booleanValue();
        ValueBinding vb = getValueBinding("immediate");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_IMMEDIATE;
    }

    public void setRequired(boolean required)
    {
        _required = Boolean.valueOf(required);
    }

    /**
     * A boolean value that indicates whether an input value is required.
     * If this value is true, and no input value is provided, the error
     * message javax.faces.component.UIInput.REQUIRED is posted.
     * 
     * @JSFProperty
     */
    public boolean isRequired()
    {
        if (_required != null) return _required.booleanValue();
        ValueBinding vb = getValueBinding("required");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_REQUIRED;
    }


    //------------------ GENERATED CODE END ---------------------------------------
}
