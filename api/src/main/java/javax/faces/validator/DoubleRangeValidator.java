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
public class DoubleRangeValidator extends NumberRangeValidator {
	// FIELDS
	public static final String VALIDATOR_ID = "javax.faces.DoubleRange";
	public static final String MAXIMUM_MESSAGE_ID = "javax.faces.validator.DoubleRangeValidator.MAXIMUM";
	public static final String MINIMUM_MESSAGE_ID = "javax.faces.validator.DoubleRangeValidator.MINIMUM";
	public static final String TYPE_MESSAGE_ID = "javax.faces.validator.DoubleRangeValidator.TYPE";

	// CONSTRUCTORS
	public DoubleRangeValidator() {
		super(null, null);
	}

	public DoubleRangeValidator(double maximum) {
		super(null, new Double(maximum));
	}

	public DoubleRangeValidator(double maximum, double minimum) {
		super(new Double(minimum), new Double(maximum));
	}

	// Abstract methods implementation
	protected Comparable getValue(Number number) {
		if(number instanceof Double) {
			return (Double) number;
		}
		return new Double(number.longValue());
	}

	protected Comparable getValue(String str) throws NumberFormatException {
		return new Double(str);
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
	public double getMaximum() {
		return _maximum != null ? _maximum.doubleValue() : Double.MAX_VALUE;
	}

	public void setMaximum(double maximum) {
		_maximum = new Double(maximum);
	}

	public double getMinimum() {
		return _minimum != null ? _minimum.doubleValue() : Double.MIN_VALUE;
	}

	public void setMinimum(double minimum) {
		_minimum = new Double(minimum);
	}

	// MISC
	public boolean equals(Object o) {
		if (!(o instanceof DoubleRangeValidator)) {
			return false;
		}
		return super.equals(o);
	}
}
