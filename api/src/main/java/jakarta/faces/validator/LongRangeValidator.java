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
 * UIComponent.  When invoked, the validator ensures that values
 * are valid longs that lie within the minimum and maximum values specified.
 * 
 * Commonly associated with a h:inputText entity.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 */
@JSFValidator(name = "f:validateLongRange", bodyContent = "empty")
@JSFJspProperty(
    name="binding", 
    returnType = "jakarta.faces.validator.LongRangeValidator",
    longDesc = "A ValueExpression that evaluates to a LongRangeValidator.")
public class LongRangeValidator
        implements Validator, PartialStateHolder
{
    // FIELDS
    public static final String MAXIMUM_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.MAXIMUM";
    public static final String MINIMUM_MESSAGE_ID =    "jakarta.faces.validator.LongRangeValidator.MINIMUM";
    public static final String TYPE_MESSAGE_ID       = "jakarta.faces.validator.LongRangeValidator.TYPE";
    public static final String VALIDATOR_ID       = "jakarta.faces.LongRange";
    public static final String NOT_IN_RANGE_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.NOT_IN_RANGE";
    
    private Long _minimum = null;
    private Long _maximum = null;
    private boolean _transient = false;
    private boolean _initialStateMarked = false;

    // CONSTRUCTORS
    public LongRangeValidator()
    {
    }

    public LongRangeValidator(long maximum)
    {
        _maximum = Long.valueOf(maximum);
    }

    public LongRangeValidator(long maximum,
                              long minimum)
    {
        _maximum = Long.valueOf(maximum);
        _minimum = Long.valueOf(minimum);
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

        double dvalue = parseLongValue(facesContext, uiComponent,value);
        if (_minimum != null && _maximum != null)
        {
            if (dvalue < _minimum || dvalue > _maximum)
            {
                Object[] args = {_minimum, _maximum,MessageUtils.getLabel(facesContext, uiComponent)};
                throw new ValidatorException(MessageUtils.getErrorMessage(facesContext,
                                                                           NOT_IN_RANGE_MESSAGE_ID, args));
            }
        }
        else if (_minimum != null)
        {
            if (dvalue < _minimum)
            {
                Object[] args = {_minimum,MessageUtils.getLabel(facesContext, uiComponent)};
                throw new ValidatorException(MessageUtils.getErrorMessage(facesContext, MINIMUM_MESSAGE_ID, args));
            }
        }
        else if (_maximum != null)
        {
            if (dvalue > _maximum)
            {
                Object[] args = {_maximum,MessageUtils.getLabel(facesContext, uiComponent)};
                throw new ValidatorException(MessageUtils.getErrorMessage(facesContext, MAXIMUM_MESSAGE_ID, args));
            }
        }
    }

    private long parseLongValue(FacesContext facesContext, UIComponent uiComponent, Object value)
        throws ValidatorException
    {
        if (value instanceof Number)
        {
            return ((Number)value).longValue();
        }

        try
        {
            return Long.parseLong(value.toString());
        }
        catch (NumberFormatException e)
        {
            Object[] args = {MessageUtils.getLabel(facesContext, uiComponent)};
            throw new ValidatorException(MessageUtils.getErrorMessage(facesContext, TYPE_MESSAGE_ID, args));
        }
        
    }


     // GETTER & SETTER
    
    /** 
     * The largest value that should be considered valid.
     * 
     */
    @JSFProperty(deferredValueType="java.lang.Long")
    public long getMaximum()
    {
        return _maximum != null ? _maximum : Long.MAX_VALUE;
    }

    public void setMaximum(long maximum)
    {
        _maximum = Long.valueOf(maximum);
        clearInitialState();
    }

    /**
     * The smallest value that should be considered valid.
     *  
     */
    @JSFProperty(deferredValueType="java.lang.Long")
    public long getMinimum()
    {
        return _minimum != null ? _minimum.longValue() : Long.MIN_VALUE;
    }

    public void setMinimum(long minimum)
    {
        _minimum = Long.valueOf(minimum);
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
    public void restoreState(FacesContext context, Object state)
    {
        Assert.notNull(context, "context");

        if (state != null)
        {
            Object[] values = (Object[])state;
            _maximum = (Long)values[0];
            _minimum = (Long)values[1];
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
        if (!(o instanceof LongRangeValidator))
        {
            return false;
        }

        LongRangeValidator longRangeValidator = (LongRangeValidator)o;

        if (_maximum != null ? !_maximum.equals(longRangeValidator._maximum) : longRangeValidator._maximum != null)
        {
            return false;
        }
        if (_minimum != null ? !_minimum.equals(longRangeValidator._minimum) : longRangeValidator._minimum != null)
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
    private Boolean isDisabled()
    {
        return null;
    }
    
    @JSFProperty(faceletsOnly=true)
    private String getFor()
    {
        return null;
    }
}
