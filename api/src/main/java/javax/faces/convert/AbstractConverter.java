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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * An abstract base class for all basic converters
 * 
 * @author Nikolay Petrov
 * @version $Revision$ $Date$
 */
abstract class AbstractConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (context == null) {
			throw new NullPointerException("facesContext");
		}
		if (component == null) {
			throw new NullPointerException("uiComponent");
		}

		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		if (trimmedValue.length() == 0) {
			return null;
		}

		try {
			return getAsObject(trimmedValue);
		} catch (Exception e) {
			throw new ConverterException(_MessageUtils.getErrorMessage(context, getConversionMessageId(),
					getMessageArguments(component, value)), e);
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		if (context == null) {
			throw new NullPointerException("facesContext");
		}
		if (component == null) {
			throw new NullPointerException("uiComponent");
		}

		if (value == null) {
			return "";
		}
		if (value instanceof String) {
			return (String) value;
		}

		try {
			return getAsString(value);
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	protected Object[] getMessageArguments(UIComponent component, String value) {
		return new Object[] {component.getId(), value};
	}
	
	protected abstract Object getAsObject(String value) throws Exception;

	protected abstract String getAsString(Object value) throws Exception;

	protected abstract String getConversionMessageId();
}
