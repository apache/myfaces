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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DateTimeConverter extends AbstractConverter implements StateHolder {
	// API field
	public static final String CONVERTER_ID = "javax.faces.DateTime";

	// internal constants
	private static final String CONVERSION_MESSAGE_ID = "javax.faces.convert.DateTimeConverter.CONVERSION";

	private static final TimeZone TIMEZONE_DEFAULT = TimeZone.getTimeZone("GMT");

	private String _dateStyle;
	private Locale _locale;
	private String _pattern;
	private String _timeStyle;
	private TimeZone _timeZone;
	private String _type;
	private boolean _transient;

	// Methods implementation
	protected Object getAsObject(String value) throws Exception {
		return prepareDateFormat().parse(value);
	}

	protected String getAsString(Object value) {
		return prepareDateFormat().format(value);
	}

	protected String getConversionMessageId() {
		return CONVERSION_MESSAGE_ID;
	}
	
	protected Object[] getMessageArguments(UIComponent component, String value) {
		return new Object[] {value, component.getId()};
	}

	private DateFormat prepareDateFormat() {
		DateFormat format = getDateFormat();
		// format cannot be lenient (JSR-127)
		format.setLenient(false);
		
		TimeZone tz = getTimeZone();
		if (tz != null) {
			format.setTimeZone(tz);
		}
		
		return format;
	}
	
	private DateFormat getDateFormat() {
		if(_pattern != null) {
			try {
				return new SimpleDateFormat(_pattern, getLocale());
			} catch (IllegalArgumentException iae) {
				throw new ConverterException("Invalid pattern", iae);
			}
		}
		
		return Type.getType(getType()).getFormatter(calcDateStyle(), calcTimeStyle(), getLocale());
	}

	private int calcDateStyle() {
		return Style.getStyleFormat(getDateStyle());
	}
	
	private int calcTimeStyle() {
		return Style.getStyleFormat(getTimeStyle());
	}

	// STATE SAVE/RESTORE
	public void restoreState(FacesContext facesContext, Object state) {
		Object[] values = (Object[]) state;
		_dateStyle = (String) values[0];
		_locale = (Locale) values[1];
		_pattern = (String) values[2];
		_timeStyle = (String) values[3];
		_timeZone = (TimeZone) values[4];
		_type = (String) values[5];
	}

	public Object saveState(FacesContext facesContext) {
		Object[] values = new Object[6];
		values[0] = _dateStyle;
		values[1] = _locale;
		values[2] = _pattern;
		values[3] = _timeStyle;
		values[4] = _timeZone;
		values[5] = _type;
		return values;
	}

	// GETTER & SETTER
	public String getDateStyle() {
		return _dateStyle != null ? _dateStyle : Style.DEFAULT.getName();
	}

	public void setDateStyle(String dateStyle) {
		//TODO: validate timeStyle
		_dateStyle = dateStyle;
	}

	public Locale getLocale() {
		if (_locale != null)
			return _locale;
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getViewRoot().getLocale();
	}

	public void setLocale(Locale locale) {
		_locale = locale;
	}

	public String getPattern() {
		return _pattern;
	}

	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	public String getTimeStyle() {
		return _timeStyle != null ? _timeStyle : Style.DEFAULT.getName();
	}

	public void setTimeStyle(String timeStyle) {
		//TODO: validate timeStyle
		_timeStyle = timeStyle;
	}

	public TimeZone getTimeZone() {
		return _timeZone != null ? _timeZone : TIMEZONE_DEFAULT;
	}

	public void setTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}

	public boolean isTransient() {
		return _transient;
	}

	public void setTransient(boolean aTransient) {
		_transient = aTransient;
	}

	public String getType() {
		return _type != null ? _type : Type.DATE.getName();
	}

	public void setType(String type) {
		//TODO: validate type
		_type = type;
	}
	
	private static class Style {
		
		private static final Style DEFAULT = new Style("default", DateFormat.DEFAULT);
		private static final Style MEDIUM = new Style("medium", DateFormat.MEDIUM);
		private static final Style SHORT = new Style("short", DateFormat.SHORT);
		private static final Style LONG = new Style("long", DateFormat.LONG);
		private static final Style FULL = new Style("full", DateFormat.FULL);
		
		private static final Style[] values = new Style[] {DEFAULT, MEDIUM, SHORT, LONG, FULL};
		
		public static Style getStyle(String name) {
			for(int i = 0;i < values.length;i++) {
				if(values[i]._name.equals(name)) {
					return values[i];
				}
			}
			
			throw new ConverterException("invalid style '" + name + "'");
		}
		
		private static int getStyleFormat(String name) {
			return getStyle(name).getFormat();
		}
		
		private String _name;
		private int _format;
		
		private Style(String name, int format) {
			this._name = name;
			this._format = format;
		}
		
		public String getName() {
			return _name;
		}
		
		public int getFormat() {
			return _format;
		}
	}
	
	private abstract static class Type {
		
		private static final Type DATE = new Type("date") {
			public DateFormat getFormatter(int dateStyle, int timeStyle, Locale locale) {
				return DateFormat.getDateInstance(dateStyle, locale);
			}
		};
		private static final Type TIME = new Type("time") {
			public DateFormat getFormatter(int dateStyle, int timeStyle, Locale locale) {
				return DateFormat.getTimeInstance(timeStyle, locale);
			}
		};
		private static final Type BOTH = new Type("both") {
			public DateFormat getFormatter(int dateStyle, int timeStyle, Locale locale) {
				return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
			}
		};
		
		private static final Type[] values = new Type[] {DATE, TIME, BOTH};

		public static Type getType(String name) {
			for(int i = 0;i < values.length;i++) {
				if(values[i]._name.equals(name)) {
					return values[i];
				}
			}
			
			throw new ConverterException("invalid type '" + name + "'");
		}
		
		private String _name;
		
		private Type(String name) {
			this._name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public abstract DateFormat getFormatter(int dateStyle, int timeStyle, Locale locale);
	}
}
