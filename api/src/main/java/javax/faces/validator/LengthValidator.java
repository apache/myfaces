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
public class LengthValidator extends NumberRangeValidator {
	// FIELDS
	public static final String MAXIMUM_MESSAGE_ID = "javax.faces.validator.LengthValidator.MAXIMUM";
	public static final String MINIMUM_MESSAGE_ID = "javax.faces.validator.LengthValidator.MINIMUM";
	public static final String VALIDATOR_ID = "javax.faces.Length";

	// CONSTRUCTORS
	public LengthValidator() {
		super(null, null, false);
	}

	public LengthValidator(int maximum) {
		super(null, new Integer(maximum), false);
	}

	public LengthValidator(int maximum, int minimum) {
		super(new Integer(minimum), new Integer(maximum), false);
	}

	// Abstract methods implementation
	protected Comparable getValue(Number number) {
		return new Integer(number.toString().length());
	}

	protected Comparable getValue(String str) throws NumberFormatException {
		return new Integer(str.length());
	}

	protected String getMaximumMessageId() {
		return MAXIMUM_MESSAGE_ID;
	}

	protected String getMinimumMessageId() {
		return MINIMUM_MESSAGE_ID;
	}

	protected String getTypeMessageId() {
		return null;
	}

	// SETTER & GETTER
	public int getMaximum() {
		return _maximum != null ? _maximum.intValue() : Integer.MAX_VALUE;
	}

	public void setMaximum(int maximum) {
		_maximum = new Integer(maximum);
	}

	public int getMinimum() {
		return _minimum != null ? _minimum.intValue() : 0;
	}

	public void setMinimum(int minimum) {
		_minimum = new Integer(minimum);
	}

	// MISC
	public boolean equals(Object o) {
		if (!(o instanceof LengthValidator)) {
			return false;
		}
		return super.equals(o);
	}
}
