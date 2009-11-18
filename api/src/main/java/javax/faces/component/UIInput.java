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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.event.*;
import javax.faces.render.Renderer;
import javax.faces.validator.BeanValidator;
import javax.faces.validator.Validator;
import java.util.*;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFListener;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * UICommand is a base abstraction for components that implement ActionSource.
 * <p>
 * See the javadoc for this class in the <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF
 * Specification</a> for further details.
 * <p>
 */
@JSFComponent(defaultRendererType = "javax.faces.Text")
public class UIInput extends UIOutput implements EditableValueHolder
{
    public static final String COMPONENT_TYPE = "javax.faces.Input";
    public static final String COMPONENT_FAMILY = "javax.faces.Input";

    public static final String CONVERSION_MESSAGE_ID = "javax.faces.component.UIInput.CONVERSION";
    public static final String REQUIRED_MESSAGE_ID = "javax.faces.component.UIInput.REQUIRED";
    public static final String UPDATE_MESSAGE_ID = "javax.faces.component.UIInput.UPDATE";
    private static final String ERROR_HANDLING_EXCEPTION_LIST = "org.apache.myfaces.errorHandling.exceptionList";

    // -=Leonardo Uribe =- According to http://wiki.java.net/bin/view/Projects/Jsf2MR1ChangeLog 
    // this constant will be made public on 2.1. For now, since this param is handled in
    // 2.0, we should do it as well.
    private static final String EMPTY_VALUES_AS_NULL_PARAM_NAME = "javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL";
    public static final String VALIDATE_EMPTY_FIELDS_PARAM_NAME = "javax.faces.VALIDATE_EMPTY_FIELDS";

    private static final Validator[] EMPTY_VALIDATOR_ARRAY = new Validator[0];

    private MethodBinding _validator;
    private _DeltaList<Validator> _validatorList;

    private MethodBinding _valueChangeListener;

    /**
     * Construct an instance of the UIInput.
     */
    public UIInput()
    {
        setRendererType("javax.faces.Text");
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    /**
     * Store the specified object as the "local value" of this component. The value-binding named "value" (if any) is
     * ignored; the object is only stored locally on this component. During the "update model" phase, if there is a
     * value-binding named "value" then this local value will be stored via that value-binding and the "local value"
     * reset to null.
     */
    @Override
    public void setValue(Object value)
    {
        setLocalValueSet(true);
        super.setValue(value);
    }
    
    /**
     * Return the current value of this component.
     * <p>
     * If a submitted value has been converted but not yet pushed into the
     * model, then return that locally-cached value (see isLocalValueSet).
     * <p>
     * Otherwise, evaluate an EL expression to fetch a value from the model. 
     */
    public Object getValue()
    {
        if (isLocalValueSet()) return super.getLocalValue();
        return super.getValue();
    }

    /**
     * Set the "submitted value" of this component from the relevant data in the current servlet request object.
     * <p>
     * If this component is not rendered, then do nothing; no output would have been sent to the client so no input is
     * expected.
     * <p>
     * Invoke the inherited functionality, which typically invokes the renderer associated with this component to
     * extract and set this component's "submitted value".
     * <p>
     * If this component is marked "immediate", then immediately apply validation to the submitted value found. On
     * error, call context method "renderResponse" which will force processing to leap to the "render
     * response" phase as soon as the "decode" step has completed for all other components.
     */
    @Override
    public void processDecodes(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        if (!isRendered())
        {
            return;
        }
        super.processDecodes(context);
        if (isImmediate())
        {
            try
            {
                //Pre validation event dispatch for component
                context.getApplication().publishEvent(context,  PreValidateEvent.class, UIComponent.class, this);

                validate(context);
            }
            catch (RuntimeException e)
            {
                context.renderResponse();
                throw e;
            }
            finally
            {
                context.getApplication().publishEvent(context,  PostValidateEvent.class, UIComponent.class, this);
            }
            if (!isValid())
            {
                context.renderResponse();
            }
        }
    }

    @Override
    public void processValidators(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        if (!isRendered())
        {
            return;
        }

        super.processValidators(context);

        if (!isImmediate())
        {
            try
            {
                //Pre validation event dispatch for component
                context.getApplication().publishEvent(context,  PreValidateEvent.class, UIComponent.class, this);

                validate(context);
            }
            catch (RuntimeException e)
            {
                context.renderResponse();
                throw e;
            }
            finally
            {
                context.getApplication().publishEvent(context,  PostValidateEvent.class, UIComponent.class, this);
            }
            if (!isValid())
            {
                context.renderResponse();
            }
        }
    }

    @Override
    public void processUpdates(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        if (!isRendered())
        {
            return;
        }

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

    @Override
    public void decode(FacesContext context)
    {
        // We (re)set to valid, so that component automatically gets (re)validated
        setValid(true);
        super.decode(context);
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        // invoke standard listeners attached to this component first
        super.broadcast(event);

        // Check if the event is applicable for ValueChangeListener
        if (event instanceof ValueChangeEvent)
        {
            // invoke the single listener defined directly on the component
            MethodBinding valueChangeListenerBinding = getValueChangeListener();
            if (valueChangeListenerBinding != null)
            {
                try
                {
                    valueChangeListenerBinding.invoke(getFacesContext(), new Object[] { event });
                }
                catch (EvaluationException e)
                {
                    Throwable cause = e.getCause();
                    if (cause != null && cause instanceof AbortProcessingException)
                    {
                        throw (AbortProcessingException) cause;
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
        if (!isValid())
        {
            return;
        }
        if (!isLocalValueSet())
        {
            return;
        }
        ValueExpression expression = getValueExpression("value");
        if (expression == null)
        {
            return;
        }

        try
        {
            expression.setValue(context.getELContext(), getLocalValue());
            setValue(null);
            setLocalValueSet(false);
        }
        catch (Exception e)
        {
            context.getExternalContext().log(e.getMessage(), e);
            _MessageUtils.addErrorMessage(context, this, UPDATE_MESSAGE_ID, new Object[] { _MessageUtils.getLabel(
                context, this) });
            setValid(false);

            /*
             * we are not allowed to throw exceptions here - we still need the full stack-trace later on to process it
             * later in our error-handler
             */
            queueExceptionInRequest(context, expression, e);
        }
    }

    /**
     * For development and production, we want to offer a single point to which error-handlers can attach. So we queue
     * up all ocurring exceptions and later pass them to the configured error-handler.
     */
    @SuppressWarnings("unchecked")
    private void queueExceptionInRequest(FacesContext context, ValueExpression expression, Exception e)
    {
        Map<String, Object> requestScope = context.getExternalContext().getRequestMap();
        List<FacesException> li = (List<FacesException>) requestScope.get(ERROR_HANDLING_EXCEPTION_LIST);
        if (null == li)
        {
            li = new ArrayList<FacesException>();
            context.getExternalContext().getRequestMap().put(ERROR_HANDLING_EXCEPTION_LIST, li);
        }

        li.add(new FacesException("Exception while setting value for expression : " + expression.getExpressionString()
                + " of component with path : " + _ComponentUtils.getPathToComponent(this), e));
    }

    protected void validateValue(FacesContext context, Object convertedValue)
    {
        boolean empty = convertedValue == null
                || (convertedValue instanceof String && ((String) convertedValue).length() == 0);

        if (isValid() && isRequired() && empty)
        {
            if (getRequiredMessage() != null)
            {
                String requiredMessage = getRequiredMessage();
                context.addMessage(this.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    requiredMessage, requiredMessage));
            }
            else
            {
                _MessageUtils.addErrorMessage(context, this, REQUIRED_MESSAGE_ID,
                    new Object[] { _MessageUtils.getLabel(context, this) });
            }
            setValid(false);
            return;
        }

        if (!empty || shouldValidateEmptyFields(context))
        {
            _ComponentUtils.callValidators(context, this, convertedValue);
        }
    }

    private boolean shouldValidateEmptyFields(FacesContext context)
    {
        ExternalContext extCtx = context.getExternalContext();
        String validateEmptyFields = (String) extCtx.getInitParameter(VALIDATE_EMPTY_FIELDS_PARAM_NAME);
        if (validateEmptyFields == null)
        {
            validateEmptyFields = (String) extCtx.getApplicationMap().get(VALIDATE_EMPTY_FIELDS_PARAM_NAME);
        }

        // null means the same as auto.
        if (validateEmptyFields == null)
        {
            validateEmptyFields = "auto";
        }
        else
        {
            // The environment variables are case insensitive.
            validateEmptyFields = validateEmptyFields.toLowerCase();
        }

        if (validateEmptyFields.equals("auto") && _ExternalSpecifications.isBeanValidationAvailable)
        {
            return true;
        }
        else if (validateEmptyFields.equals("true"))
        {
            return true;
        }
        return false;
    }

    /**
     * Determine whether the new value is valid, and queue a ValueChangeEvent if necessary.
     * <p>
     * The "submitted value" is converted to the necessary type; conversion failure is reported as an error and
     * validation processing terminates for this component. See documentation for method getConvertedValue for details
     * on the conversion process.
     * <p>
     * Any validators attached to this component are then run, passing the converted value.
     * <p>
     * The old value of this component is then fetched (possibly involving the evaluation of a value-binding expression,
     * ie invoking a method on a user object). The old value is compared to the new validated value, and if they are
     * different then a ValueChangeEvent is queued for later processing.
     * <p>
     * On successful completion of this method:
     * <ul>
     * <li>isValid() is true
     * <li>isLocalValueSet() is true
     * <li>submittedValue is reset to null
     * <li>a ValueChangeEvent is queued if the new value != old value
     * </ul>
     */
    public void validate(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");


        try
        {
            Object submittedValue = getSubmittedValue();
            if (submittedValue == null)
            {
                return;
            }

            // Begin new JSF 2.0 requirement (INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL)
            String contextParam = context.getExternalContext().getInitParameter(EMPTY_VALUES_AS_NULL_PARAM_NAME);
            if (contextParam != null && contextParam.toLowerCase().equals("true"))
            {
                if (submittedValue.toString().length() == 0)
                {
                    setSubmittedValue(null);
                    submittedValue = null;
                }
            }
            // End new JSF 2.0 requirement (INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL)

            Object convertedValue = getConvertedValue(context, submittedValue);

            if (!isValid())
                return;

            validateValue(context, convertedValue);

            if (!isValid())
                return;

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
            throw new FacesException("Exception while validating component with path : "
                    + _ComponentUtils.getPathToComponent(this), ex);
        }
    }

    /**
     * Convert the provided object to the desired value.
     * <p>
     * If there is a renderer for this component, then call the renderer's getConvertedValue method. While this can of
     * course be implemented in any way the renderer desires, it typically performs exactly the same processing that
     * this method would have done anyway (ie that described below for the no-renderer case).
     * <p>
     * Otherwise:
     * <ul>
     * <li>If the submittedValue is not a String then just return the submittedValue unconverted.
     * <li>If there is no "value" value-binding then just return the submittedValue unconverted.
     * <li>Use introspection to determine the type of the target property specified by the value-binding, and then use
     * Application.createConverter to find a converter that can map from String to the required type. Apply the
     * converter to the submittedValue and return the result.
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
                    return converter.getAsObject(context, this, (String) submittedValue);
                }
            }
        }
        catch (ConverterException e)
        {
            String converterMessage = getConverterMessage();
            if (converterMessage != null)
            {
                context.addMessage(getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    converterMessage, converterMessage));
            }
            else
            {
                FacesMessage facesMessage = e.getFacesMessage();
                if (facesMessage != null)
                {
                    context.addMessage(getClientId(context), facesMessage);
                }
                else
                {
                    _MessageUtils.addErrorMessage(context, this, CONVERSION_MESSAGE_ID,
                        new Object[] { _MessageUtils.getLabel(context, this) });
                }
            }
            setValid(false);
        }
        return submittedValue;
    }

    protected boolean compareValues(Object previous, Object value)
    {
        return previous == null ? (value != null) : (!previous.equals(value));
    }

    /**
     * @since 1.2
     */
    public void resetValue()
    {
        setSubmittedValue(null);
        setValue(null);
        setLocalValueSet(false);
        setValid(true);
    }

    /**
     * A boolean value that identifies the phase during which action events should fire.
     * <p>
     * During normal event processing, action methods and action listener methods are fired during the
     * "invoke application" phase of request processing. If this attribute is set to "true", these methods are fired
     * instead at the end of the "apply request values" phase.
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
     * A boolean value that indicates whether an input value is required.
     * <p>
     * If this value is true and no input value is provided by a postback operation, then the "requiredMessage" text is
     * registered as a FacesMessage for the request, and validation fails.
     * </p>
     * <p>
     * Default value: false.
     * </p>
     */
    @JSFProperty(defaultValue = "false")
    public boolean isRequired()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.required, Boolean.FALSE);
    }

    public void setRequired(boolean required)
    {
        getStateHelper().put(PropertyKeys.required, required ); 
    }

    /**
     * Text to be displayed to the user as an error message when conversion of a submitted value to the target type
     * fails.
     * <p>
     * </p>
     */
    @JSFProperty
    public String getConverterMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.converterMessage);
    }

    public void setConverterMessage(String converterMessage)
    {
        getStateHelper().put(PropertyKeys.converterMessage, converterMessage );
    }

    /**
     * Text to be displayed to the user as an error message when this component is marked as "required" but no input
     * data is present during a postback (ie the user left the required field blank).
     */
    @JSFProperty
    public String getRequiredMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.requiredMessage);
    }

    public void setRequiredMessage(String requiredMessage)
    {
        getStateHelper().put(PropertyKeys.requiredMessage, requiredMessage );
    }

    private boolean _isSetValidator()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.validatorSet);
        return value == null ? false : value;
    }

    /**
     * A method-binding EL expression which is invoked during the validation phase for this component.
     * <p>
     * The invoked method is expected to check the submitted value for this component, and if not acceptable then report
     * a validation error for the component.
     * </p>
     * <p>
     * The method is expected to have the prototype
     * </p>
     * <code>public void aMethod(FacesContext, UIComponent,Object)</code>
     * 
     * @deprecated
     */
    @SuppressWarnings("dep-ann")
    @JSFProperty(stateHolder = true, returnSignature = "void", methodSignature = "javax.faces.context.FacesContext,javax.faces.component.UIComponent,java.lang.Object")
    public MethodBinding getValidator()
    {
        if (_validator != null)
        {
            return _validator;
        }
        ValueExpression expression = getValueExpression("validator");
        if (expression != null)
        {
            return (MethodBinding) expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /** See getValidator.
     *  
     * @deprecated 
     */
    public void setValidator(MethodBinding validator)
    {
        this._validator = validator;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.validatorSet,Boolean.TRUE);
        }
    }

    /** See getValidator. */
    public void addValidator(Validator validator)
    {
        if (validator == null)
        {
            throw new NullPointerException("validator");
        }
        
        if (_validatorList == null)
        {
            _validatorList = new _DeltaList<Validator>(new ArrayList<Validator>());
        }

        _validatorList.add(validator);
        
        // The argument validator must be inspected for the presence of the ResourceDependency annotation.
        _handleAnnotations(FacesContext.getCurrentInstance(), validator);
    }

    /** See getValidator. */
    public void removeValidator(Validator validator)
    {
        if (validator == null || _validatorList == null)
            return;

        _validatorList.remove(validator);
    }

    /** See getValidator. */
    public Validator[] getValidators()
    {
        return _validatorList == null ? EMPTY_VALIDATOR_ARRAY
                : _validatorList.toArray(new Validator[_validatorList.size()]);
    }

    /**
     * Text which will be shown if validation fails.
     */
    @JSFProperty
    public String getValidatorMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.validatorMessage);
    }

    public void setValidatorMessage(String validatorMessage)
    {
        getStateHelper().put(PropertyKeys.validatorMessage, validatorMessage );
    }

    private boolean _isSetValueChangeListener()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.valueChangeListenerSet);
        return value == null ? false : value;
    }

    /**
     * A method which is invoked during postback processing for the current view if the submitted value for this
     * component is not equal to the value which the "value" expression for this component returns.
     * <p>
     * The phase in which this method is invoked can be controlled via the immediate attribute.
     * </p>
     * 
     * @deprecated
     */
    @JSFProperty(stateHolder = true, returnSignature = "void", methodSignature = "javax.faces.event.ValueChangeEvent", clientEvent="valueChange")
    public MethodBinding getValueChangeListener()
    {
        if (_valueChangeListener != null)
        {
            return _valueChangeListener;
        }
        ValueExpression expression = getValueExpression("valueChangeListener");
        if (expression != null)
        {
            return (MethodBinding) expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * See getValueChangeListener.
     * 
     * @deprecated
     */
    public void setValueChangeListener(MethodBinding valueChangeListener)
    {
        this._valueChangeListener = valueChangeListener;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.valueChangeListenerSet,Boolean.TRUE);
        }
    }

    /**
     * Specifies whether the component's value is currently valid, ie whether the validators attached to this component
     * have allowed it.
     */
    @JSFProperty(defaultValue = "true", tagExcluded = true)
    public boolean isValid()
    {
        Object value = getStateHelper().get(PropertyKeys.valid);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return true; 
    }

    public void setValid(boolean valid)
    {
        getStateHelper().put(PropertyKeys.valid, valid );
    }

    /**
     * Specifies whether a local value is currently set.
     * <p>
     * If false, values are being retrieved from any attached ValueBinding.
     */
    @JSFProperty(defaultValue = "false", tagExcluded = true)
    public boolean isLocalValueSet()
    {
        Object value = getStateHelper().get(PropertyKeys.localValueSet);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;
    }

    public void setLocalValueSet(boolean localValueSet)
    {
        getStateHelper().put(PropertyKeys.localValueSet, localValueSet );
    }

    /**
     * Gets the current submitted value. This value, if non-null, is set by the Renderer to store a possibly invalid
     * value for later conversion or redisplay, and has not yet been converted into the proper type for this component
     * instance. This method should only be used by the decode() and validate() method of this component, or its
     * corresponding Renderer; however, user code may manually set it to null to erase any submitted value.
     */
    @JSFProperty(tagExcluded = true)
    public Object getSubmittedValue()
    {
        return  getStateHelper().get(PropertyKeys.submittedValue);
    }

    public void setSubmittedValue(Object submittedValue)
    {
        getStateHelper().put(PropertyKeys.submittedValue, submittedValue );
    }

    public void addValueChangeListener(ValueChangeListener listener)
    {
        addFacesListener(listener);
    }

    public void removeValueChangeListener(ValueChangeListener listener)
    {
        removeFacesListener(listener);
    }

    /**
     * The valueChange event is delivered when the value attribute
     * is changed.
     */
    @JSFListener(event="javax.faces.event.ValueChangeEvent")
    public ValueChangeListener[] getValueChangeListeners()
    {
        return (ValueChangeListener[]) getFacesListeners(ValueChangeListener.class);
    }

    enum PropertyKeys
    {
         immediate
        , required
        , converterMessage
        , requiredMessage
        , validatorSet
        , validatorListSet
        , validatorMessage
        , valueChangeListenerSet
        , valid
        , localValueSet
        , submittedValue
    }
    
    public void markInitialState()
    {
        super.markInitialState();
        if (_validator != null && 
            _validator instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_validator).markInitialState();
        }
        if (_validatorList != null && 
            _validatorList instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_validatorList).markInitialState();
        }
        if (_valueChangeListener != null && 
            _valueChangeListener instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_valueChangeListener).markInitialState();
        }
    }
    
    public void clearInitialState()
    {
        if (initialStateMarked())
        {
            super.clearInitialState();
            if (_validator != null && 
                _validator instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_validator).clearInitialState();
            }
            if (_validatorList != null && 
                _validatorList instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_validatorList).clearInitialState();
            }
            if (_valueChangeListener != null && 
                _valueChangeListener instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_valueChangeListener).clearInitialState();
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
            Object validatorSaved = null;
            if (!_isSetValidator() &&
                _validator != null && _validator instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _validator;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    validatorSaved = new _AttachedDeltaWrapper(_validator.getClass(),
                        attachedState);
                }
            }
            else if (_isSetValidator() || _validator != null)
            {
                //Full
                validatorSaved = saveAttachedState(facesContext,_validator);
                nullDelta = false;
            }        
            Object valueChangeListenerSaved = null;
            if (!_isSetValueChangeListener() &&
                _valueChangeListener != null && _valueChangeListener instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _valueChangeListener;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    valueChangeListenerSaved = new _AttachedDeltaWrapper(_valueChangeListener.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetValueChangeListener() || _valueChangeListener != null)
            {
                //Full
                valueChangeListenerSaved = saveAttachedState(facesContext,_valueChangeListener);
                nullDelta = false;
            }        
            
            Object validatorListSaved = saveValidatorList(facesContext);
            if (parentSaved == null && validatorListSaved == null && nullDelta)
            {
                //No values
                return null;
            }
            
            Object[] values = new Object[4];
            values[0] = parentSaved;
            values[1] = validatorSaved;
            values[2] = valueChangeListenerSaved;
            values[3] = validatorListSaved;
            return values;
        }
        else
        {
            Object[] values = new Object[4];
            values[0] = super.saveState(facesContext);
            values[1] = saveAttachedState(facesContext,_validator);
            values[2] = saveAttachedState(facesContext,_valueChangeListener);
            values[3] = saveValidatorList(facesContext);
            return values;
        }
    }

    @SuppressWarnings("unchecked")
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
            ((StateHolder)_validator).restoreState(facesContext, ((_AttachedDeltaWrapper) values[1]).getWrappedStateObject());
        }
        else
        {
            //Full
            _validator = (javax.faces.el.MethodBinding) restoreAttachedState(facesContext,values[1]);
        }         
        if (values[2] instanceof _AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_valueChangeListener).restoreState(facesContext, ((_AttachedDeltaWrapper) values[2]).getWrappedStateObject());
        }
        else
        {
            //Full
            _valueChangeListener = (javax.faces.el.MethodBinding) restoreAttachedState(facesContext,values[2]);
        }
        if (values[3] instanceof _AttachedDeltaWrapper)
        {
            //Delta
            if (_validatorList != null)
            {
                ((StateHolder)_validatorList).restoreState(facesContext,
                        ((_AttachedDeltaWrapper) values[3]).getWrappedStateObject());
            }
        }
        else if (values[3] != null || !initialStateMarked())
        {
            //Full
            _validatorList = (_DeltaList<Validator>)
                restoreAttachedState(facesContext,values[3]);
        }
    }
    
    private Object saveValidatorList(FacesContext facesContext)
    {
        PartialStateHolder holder = (PartialStateHolder) _validatorList;
        if (initialStateMarked() && _validatorList != null && holder.initialStateMarked())
        {                
            Object attachedState = holder.saveState(facesContext);
            if (attachedState != null)
            {
                return new _AttachedDeltaWrapper(_validatorList.getClass(),
                        attachedState);
            }
            //_validatorList instances once is created never changes, we can return null
            return null;
        }
        else
        {
            return saveAttachedState(facesContext,_validatorList);
        }            
    }

    /**
     * Check if a value is empty or not. Since we don't know the class of
     * value we have to check and deal with it properly.
     * 
     * @since 2.0
     * @param value
     * @return
     */
    public static boolean isEmpty(Object value)
    {
        if (value == null)
        {
            return true;
        }
        else if (value instanceof String)
        {
            if ( ((String)value).length() <= 0 )
            {
                return true;
            }
        }
        else if (value instanceof Collection)
        {
            if ( ((Collection)value).isEmpty())
            {
                return true;
            }
        }
        else if (value.getClass().isArray())
        {
            if (java.lang.reflect.Array.getLength(value) <= 0)
            {
                return true;
            }
        }
        else if (value instanceof Map)
        {
            if ( ((Map)value).isEmpty())
            {
                return true;
            }
        }
        return false;
    }
}
