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
package javax.faces.convert;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * 
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class LongConverter extends AbstractConverter {
	private static final String CONVERSION_MESSAGE_ID = "javax.faces.convert.LongConverter.CONVERSION";

	public static final String CONVERTER_ID = "javax.faces.Long";

	// Methods implementation
	protected Object getAsObject(String value) {
		return Long.valueOf(value);
	}

	protected String getAsString(Object value) {
		return Long.toString(((Number) value).longValue());
	}

	protected String getConversionMessageId() {
		return CONVERSION_MESSAGE_ID;
	}
}
