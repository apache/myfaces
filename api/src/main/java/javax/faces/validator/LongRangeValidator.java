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

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
public class LongRangeValidator extends NumberRangeValidator {
	// FIELDS
	public static final String MAXIMUM_MESSAGE_ID = "javax.faces.validator.LongRangeValidator.MAXIMUM";
	public static final String MINIMUM_MESSAGE_ID = "javax.faces.validator.LongRangeValidator.MINIMUM";
	public static final String TYPE_MESSAGE_ID = "javax.faces.validator.LongRangeValidator.TYPE";
	public static final String VALIDATOR_ID = "javax.faces.LongRange";

	// CONSTRUCTORS
	public LongRangeValidator() {
		super(null, null);
	}

	public LongRangeValidator(long maximum) {
		super(null, new Long(maximum));
	}

	public LongRangeValidator(long maximum, long minimum) {
		super(new Long(minimum), new Long(maximum));
	}

	protected Comparable getValue(Number number) {
		if(number instanceof Long) {
			return (Long) number;
		}
		return new Long(number.longValue());
	}

	// Abstract methods implementation
	protected Comparable getValue(String str) throws NumberFormatException {
		return new Long(str);
	}

	protected String getMaximumMessageId() {
		return MAXIMUM_MESSAGE_ID;
	}

	protected String getMinimumMessageId() {
		return MINIMUM_MESSAGE_ID;
	}

	protected String getTypeMessageId() {
		return TYPE_MESSAGE_ID;
	}

	// GETTER & SETTER
	public long getMaximum() {
		return _maximum != null ? _maximum.longValue() : Long.MAX_VALUE;
	}

	public void setMaximum(long maximum) {
		_maximum = new Long(maximum);
	}

	public long getMinimum() {
		return _minimum != null ? _minimum.longValue() : Long.MIN_VALUE;
	}

	public void setMinimum(long minimum) {
		_minimum = new Long(minimum);
	}

	// MISC
	public boolean equals(Object o) {
		if (!(o instanceof LongRangeValidator)) {
			return false;
		}
		return super.equals(o);
	}
}
