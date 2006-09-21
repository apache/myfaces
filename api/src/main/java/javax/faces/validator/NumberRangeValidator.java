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
package javax.faces.validator;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * An abstract base class for all number range validators including LengthValidator
 * 
 * @author Nikolay Petrov
 * @version $Revision$ $Date$
 */
abstract class NumberRangeValidator implements Validator, StateHolder {

	protected Number _minimum;
	protected Number _maximum;
	private boolean _transient = false;
	
	private boolean _checkRange;

	public NumberRangeValidator(Number minimum, Number maximum) {
		this(minimum, maximum, true);
	}

	public NumberRangeValidator(Number minimum, Number maximum, boolean checkRange) {
		this._minimum = minimum;
		this._maximum = maximum;
		this._checkRange = checkRange;
	}
	
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (context == null) {
			throw new NullPointerException("facesContext");
		}
		if (component == null) {
			throw new NullPointerException("uiComponent");
		}

		if (value == null) {
			return;
		}
		
		Comparable compValue = parseValue(context, component, value);
		if (_checkRange && _minimum != null && _maximum != null && isNotInRange(compValue, _minimum, _maximum)) {
			Object[] args = { _minimum, _maximum, component.getId()};
			throw new ValidatorException(_MessageUtils.getErrorMessage(context, NOT_IN_RANGE_MESSAGE_ID, args));
		} else if (_minimum != null && isSmaller(compValue, _minimum)) {
			Object[] args = { _minimum, component.getId() };
			throw new ValidatorException(_MessageUtils.getErrorMessage(context, getMinimumMessageId(), args));
		} else if (_maximum != null && isBigger(compValue, _maximum)) {
			Object[] args = { _minimum, component.getId() };
			throw new ValidatorException(_MessageUtils.getErrorMessage(context, getMaximumMessageId(), args));
		}
	}

	protected Comparable parseValue(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		if (value instanceof Number) {
			return getValue((Number) value);
		}
		
		try {
			return getValue(value.toString());
		} catch (NumberFormatException e) {
			Object[] args = { component.getId() };
			throw new ValidatorException(_MessageUtils.getErrorMessage(context, getTypeMessageId(), args));
		}
	}
	
	protected abstract Comparable getValue(Number number);
	
	protected abstract Comparable getValue(String str) throws NumberFormatException;
	
	protected abstract String getMinimumMessageId();
	
	protected abstract String getMaximumMessageId();
	
	protected abstract String getTypeMessageId();
	
	public boolean isTransient() {
		return _transient;
	}

	public void setTransient(boolean newTransientValue) {
		this._transient = newTransientValue;
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[2];
		values[0] = _maximum;
		values[1] = _minimum;
		return values;
	}

	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		_maximum = (Number) values[0];
		_minimum = (Number) values[1];
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof NumberRangeValidator)) {
			return false;
		}

		NumberRangeValidator other = (NumberRangeValidator) obj;
		if (_maximum != null ? !_maximum.equals(other._maximum) : other._maximum != null) {
			return false;
		}
		if (_minimum != null ? !_minimum.equals(other._minimum) : other._minimum != null) {
			return false;
		}

		return true;
	}

	private boolean isNotInRange(Comparable compValue, Number minValue, Number maxValue) {
		return isSmaller(compValue, minValue) || isBigger(compValue, maxValue);
	}
	
	private boolean isSmaller(Comparable compValue, Number other) {
		return compValue.compareTo(other) < 0;
	}
	
	private boolean isBigger(Comparable compValue, Number other) {
		return compValue.compareTo(other) > 0;
	}	
}
