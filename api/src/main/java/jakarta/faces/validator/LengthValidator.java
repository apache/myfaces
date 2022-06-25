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
package jakarta.faces.validator;

import org.apache.myfaces.core.api.shared.MessageUtils;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFValidator;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * Creates a validator and associateds it with the nearest parent
 * UIComponent.  When invoked, the validator ensures that values are
 * valid strings with a length that lies within the minimum and maximum
 * values specified.
 * 
 * Commonly associated with a h:inputText entity.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 */
@JSFValidator(name = "f:validateLength", bodyContent = "empty")
@JSFJspProperty(
    name="binding", 
    returnType = "jakarta.faces.validator.LengthValidator",
    longDesc = "A ValueExpression that evaluates to a LengthValidator.")
public class LengthValidator
        implements Validator, PartialStateHolder
{
    // FIELDS
    public static final String     MAXIMUM_MESSAGE_ID = "jakarta.faces.validator.LengthValidator.MAXIMUM";
    public static final String     MINIMUM_MESSAGE_ID = "jakarta.faces.validator.LengthValidator.MINIMUM";
    public static final String     VALIDATOR_ID        = "jakarta.faces.Length";

    private Integer _minimum = null;
    private Integer _maximum = null;
    private boolean _transient = false;
    private boolean _initialStateMarked = false;

    // CONSTRUCTORS
    public LengthValidator()
    {
    }

    public LengthValidator(int maximum)
    {
        _maximum = maximum;
    }

    public LengthValidator(int maximum, int minimum)
    {
        _maximum = maximum;
        _minimum = minimum;
    }

    // VALIDATE
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value)
            throws ValidatorException
    {
        Assert.notNull(facesContext, "facesContext");
        Assert.notNull(uiComponent, "uiComponent");

        if (value == null)
        {
            return;
        }

        int length = value instanceof String ?
            ((String)value).length() : value.toString().length();

        if (_minimum != null)
        {
            if (length < _minimum)
            {
                Object[] args = {_minimum,MessageUtils.getLabel(facesContext, uiComponent)};
                throw new ValidatorException(MessageUtils.getErrorMessage(facesContext, MINIMUM_MESSAGE_ID, args));
            }
        }

        if (_maximum != null)
        {
            if (length > _maximum)
            {
                Object[] args = {_maximum,MessageUtils.getLabel(facesContext, uiComponent)};
                throw new ValidatorException(MessageUtils.getErrorMessage(facesContext, MAXIMUM_MESSAGE_ID, args));
            }
        }
    }

    // SETTER & GETTER
    
    /** 
     * The largest value that should be considered valid.
     * 
     */
    @JSFProperty(deferredValueType="java.lang.Integer")
    public int getMaximum()
    {
        return _maximum != null ? _maximum : 0;
    }

    public void setMaximum(int maximum)
    {
        _maximum = Integer.valueOf(maximum);
        clearInitialState();
    }

    /**
     * The smallest value that should be considered valid.
     *  
     */
    @JSFProperty(deferredValueType="java.lang.Integer")
    public int getMinimum()
    {
        return _minimum != null ? _minimum : 0;
    }

    public void setMinimum(int minimum)
    {
        _minimum = Integer.valueOf(minimum);
        clearInitialState();
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }

    @Override
    public void setTransient(boolean transientValue)
    {
        _transient = transientValue;
    }

    // RESTORE & SAVE STATE
    @Override
    public Object saveState(FacesContext context)
    {
        Assert.notNull(context, "context");

        if (!initialStateMarked())
        {
            Object[] values = new Object[2];
            values[0] = _maximum;
            values[1] = _minimum;
            return values;
        }
        return null;
    }

    @Override
    public void restoreState(FacesContext context,
                             Object state)
    {
        Assert.notNull(context, "context");

        if (state != null)
        {
            Object[] values = (Object[]) state;
            _maximum = (Integer)values[0];
            _minimum = (Integer)values[1];
        }
    }

    // MISC
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof LengthValidator))
        {
            return false;
        }

        LengthValidator lengthValidator = (LengthValidator)o;

        if (_maximum != null ? !_maximum.equals(lengthValidator._maximum) : lengthValidator._maximum != null)
        {
            return false;
        }
        if (_minimum != null ? !_minimum.equals(lengthValidator._minimum) : lengthValidator._minimum != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = _minimum != null ? _minimum.hashCode() : 0;
        result = 31 * result + (_maximum != null ? _maximum.hashCode() : 0);
        return result;
    }

    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
    }
    
    @JSFProperty(faceletsOnly=true)
    @SuppressWarnings("unused")
    private Boolean isDisabled()
    {
        return null;
    }
    
    @JSFProperty(faceletsOnly=true)
    @SuppressWarnings("unused")
    private String getFor()
    {
        return null;
    }
}
