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
package javax.faces.validator;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
/**
 * Creates a validator and associateds it with the nearest parent
 * UIComponent.  When invoked, the validator ensures that values
 * are valid longs that lie within the minimum and maximum values specified.
 * 
 * Commonly associated with a h:inputText entity.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFValidator
 *   name="f:validateLongRange"
 *   bodyContent="empty"
 *   tagClass="org.apache.myfaces.taglib.core.ValidateLongRangeTag" 
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
public class LongRangeValidator
        implements Validator, StateHolder
{
    // FIELDS
    public static final String MAXIMUM_MESSAGE_ID = "javax.faces.validator.LongRangeValidator.MAXIMUM";
    public static final String MINIMUM_MESSAGE_ID =    "javax.faces.validator.LongRangeValidator.MINIMUM";
    public static final String TYPE_MESSAGE_ID       = "javax.faces.validator.LongRangeValidator.TYPE";
    public static final String VALIDATOR_ID       = "javax.faces.LongRange";

    private Long _minimum = null;
    private Long _maximum = null;
    private boolean _transient = false;

    // CONSTRUCTORS
    public LongRangeValidator()
    {
    }

    public LongRangeValidator(long maximum)
    {
        _maximum = new Long(maximum);
    }

    public LongRangeValidator(long maximum,
                              long minimum)
    {
        _maximum = new Long(maximum);
        _minimum = new Long(minimum);
    }

    // VALIDATE
    public void validate(FacesContext facesContext,
                         UIComponent uiComponent,
                         Object value)
            throws ValidatorException
    {
        if (facesContext == null) throw new NullPointerException("facesContext");
        if (uiComponent == null) throw new NullPointerException("uiComponent");

        if (value == null)
        {
            return;
        }

        double dvalue = parseLongValue(facesContext, uiComponent,value);
        if (_minimum != null && _maximum != null)
        {
            if (dvalue < _minimum.longValue() ||
                dvalue > _maximum.longValue())
            {
                Object[] args = {_minimum, _maximum,uiComponent.getId()};
                throw new ValidatorException(_MessageUtils.getErrorMessage(facesContext, NOT_IN_RANGE_MESSAGE_ID, args));
            }
        }
        else if (_minimum != null)
        {
            if (dvalue < _minimum.longValue())
            {
                Object[] args = {_minimum,uiComponent.getId()};
                throw new ValidatorException(_MessageUtils.getErrorMessage(facesContext, MINIMUM_MESSAGE_ID, args));
            }
        }
        else if (_maximum != null)
        {
            if (dvalue > _maximum.longValue())
            {
                Object[] args = {_maximum,uiComponent.getId()};
                throw new ValidatorException(_MessageUtils.getErrorMessage(facesContext, MAXIMUM_MESSAGE_ID, args));
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
        else
        {
            try
            {
                return Long.parseLong(value.toString());
            }
            catch (NumberFormatException e)
            {
                Object[] args = {uiComponent.getId()};
                throw new ValidatorException(_MessageUtils.getErrorMessage(facesContext, TYPE_MESSAGE_ID, args));
            }
        }
    }


     // GETTER & SETTER
    
    /** 
     * The largest value that should be considered valid.
     * 
     * @JSFProperty
     */    
    public long getMaximum()
    {
        return _maximum != null ? _maximum.longValue() : Long.MAX_VALUE;
    }

    public void setMaximum(long maximum)
    {
        _maximum = new Long(maximum);
    }

    /**
     * The smallest value that should be considered valid.
     *  
     * @JSFProperty
     */
    public long getMinimum()
    {
        return _minimum != null ? _minimum.longValue() : Long.MIN_VALUE;
    }

    public void setMinimum(long minimum)
    {
        _minimum = new Long(minimum);
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean transientValue)
    {
        _transient = transientValue;
    }

    // RESTORE & SAVE STATE
    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[2];
        values[0] = _maximum;
        values[1] = _minimum;
        return values;
    }

    public void restoreState(FacesContext context,
                             Object state)
    {
        Object values[] = (Object[])state;
        _maximum = (Long)values[0];
        _minimum = (Long)values[1];
    }

    // MISC
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof LongRangeValidator)) return false;

        final LongRangeValidator longRangeValidator = (LongRangeValidator)o;

        if (_maximum != null ? !_maximum.equals(longRangeValidator._maximum) : longRangeValidator._maximum != null) return false;
        if (_minimum != null ? !_minimum.equals(longRangeValidator._minimum) : longRangeValidator._minimum != null) return false;

        return true;
    }

}
