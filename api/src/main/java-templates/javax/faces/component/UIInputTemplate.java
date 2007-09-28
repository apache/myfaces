/*
* Copyright 2004-2006 The Apache Software Foundation.
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

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public class UIInputTemplate extends UIOutput implements EditableValueHolder
{
    public static final String CONVERSION_MESSAGE_ID = "javax.faces.component.UIInput.CONVERSION";
    public static final String REQUIRED_MESSAGE_ID = "javax.faces.component.UIInput.REQUIRED";
    public static final String UPDATE_MESSAGE_ID = "javax.faces.component.UIInput.UPDATE";
    private static final String ERROR_HANDLING_EXCEPTION_LIST = "org.apache.myfaces.errorHandling.exceptionList";

    /**
     * Store the specified object as the "local value" of this component.
     * The value-binding named "value" (if any) is ignored; the object is
     * only stored locally on this component. During the "update model"
     * phase, if there is a value-binding named "value" then this local
     * value will be stored via that value-binding and the "local value"
     * reset to null.
     */
    /**///setValue
    public void setValue(Object value)
    {
        setLocalValueSet(true);
        super.setValue(value);
    }

    /**
     * Set the "submitted value" of this component from the relevant data
     * in the current servet request object.
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
        if (!(event instanceof ValueChangeEvent))
        {
            throw new IllegalArgumentException("FacesEvent of class " + event.getClass().getName() + " not supported by UIInput");
        }

        // invoke standard listeners attached to this component first
        super.broadcast(event);

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

    public void updateModel(FacesContext context)
    {
        if (!isValid()) return;
        if (!isLocalValueSet()) return;
        ValueExpression expression = getValueExpression("value");
        if (expression == null) return;
        try
        {
        	expression.setValue(context.getELContext(), getLocalValue());
            setValue(null);
            setLocalValueSet(false);
        }
        catch (Exception e)
        {
            context.getExternalContext().log(e.getMessage(), e);
            _MessageUtils.addErrorMessage(context, this, UPDATE_MESSAGE_ID, new Object[]{_MessageUtils.getLabel(context,this)});
            setValid(false);

            /* we are not allowed to throw exceptions here - we still need the full stack-trace later on
             * to process it later in our error-handler
             */
            queueExceptionInRequest(context, expression, e);
        }
    }

    /**
     * For development and production, we want to offer a single point
     * to which error-handlers can attach. So we queue up all ocurring
     * exceptions and later pass them to the configured error-handler.
     *
     * @param context
     * @param expression
     * @param e
     */
    private void queueExceptionInRequest(FacesContext context, ValueExpression expression, Exception e) {
        List li = (List) context.getExternalContext().getRequestMap().get(ERROR_HANDLING_EXCEPTION_LIST);
        if(null==li) {
            li = new ArrayList();
            context.getExternalContext().getRequestMap().put(ERROR_HANDLING_EXCEPTION_LIST, li);

            li.add(new FacesException("Exception while setting value for expression : "+
                expression.getExpressionString()+" of component with path : "
                + _ComponentUtils.getPathToComponent(this),e));
        }
    }

    protected void validateValue(FacesContext context,Object convertedValue)
    {
        boolean empty = convertedValue == null ||
                        (convertedValue instanceof String
                         && ((String)convertedValue).length() == 0);

        if (isRequired() && empty)
        {
        	if(getRequiredMessage() != null) {
        		String requiredMessage = getRequiredMessage();
        		context.addMessage(this.getClientId(context),new FacesMessage(FacesMessage.SEVERITY_ERROR,requiredMessage,requiredMessage));
        	} else {
        		_MessageUtils.addErrorMessage(context, this, REQUIRED_MESSAGE_ID,new Object[]{_MessageUtils.getLabel(context,this)});
        	}
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
        	String converterMessage = getConverterMessage();
        	if(converterMessage != null) {
        		context.addMessage(getClientId(context),new FacesMessage(FacesMessage.SEVERITY_ERROR,converterMessage,converterMessage));
        	} else {
        		 FacesMessage facesMessage = e.getFacesMessage();
                 if (facesMessage != null)
                 {
                     context.addMessage(getClientId(context), facesMessage);
                 }
                 else
                 {
                     _MessageUtils.addErrorMessage(context, this, CONVERSION_MESSAGE_ID,new Object[]{_MessageUtils.getLabel(context,this)});
                 }
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
}
