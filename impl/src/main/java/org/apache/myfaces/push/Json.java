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
package org.apache.myfaces.push;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * A simple JSON encoder.
 *
 * See org.omnifaces.util.Json version 1.2 file licensed under ASL v2.0 
 *      org.omnifaces.util.Utils
 *      Copyright 2016 OmniFaces and the original author or authors.
 * 
 * @author Arjan Tijms
 * @author Bauke Scholtz
 */
public final class Json
{
    // Constants ------------------------------------------------------------------------------------------------------
    private static final String ERROR_INVALID_BEAN = "Cannot introspect object of type '%s' as bean.";
    private static final String ERROR_INVALID_GETTER = "Cannot invoke getter of property '%s' of bean '%s'.";

    // Constructors ---------------------------------------------------------------------------------------------------
    private Json()
    {
        // Hide constructor.
    }

    // Encode ---------------------------------------------------------------------------------------------------------
    /**
     * Encodes the given object as JSON. This supports the standard types {@link Boolean}, {@link Number},
     * {@link CharSequence} and {@link Date}. If the given object type does not match any of them, then it will attempt
     * to inspect the object as a javabean whereby the public properties (with public getters) will be encoded as a JS
     * object. It also supports {@link Collection}s, {@link Map}s and arrays of them, even nested ones. The {@link Date}
     * is formatted in RFC 1123 format, so you can if necessary just pass it straight to <code>new Date()</code> in
     * JavaScript.
     *
     * @param object The object to be encoded as JSON.
     * @return The JSON-encoded representation of the given object.
     * @throws IllegalArgumentException When the given object or one of its properties cannot be inspected as a bean.
     */
    public static String encode(Object object)
    {
        StringBuilder builder = new StringBuilder();
        encode(object, builder);
        return builder.toString();
    }

    /**
     * Method allowing tail recursion (prevents potential stack overflow on deeply nested structures).
     */
    private static void encode(Object object, StringBuilder builder)
    {
        if (object == null)
        {
            builder.append("null");
        }
        else if (object instanceof Boolean || object instanceof Number)
        {
            builder.append(object.toString());
        }
        else if (object instanceof CharSequence)
        {
            builder.append('"').append(escapeJS(object.toString(), false)).append('"');
        }
        else if (object instanceof Date date)
        {
            builder.append('"').append(formatRFC1123(date)).append('"');
        }
        else if (object instanceof Collection<?> collection)
        {
            encodeCollection(collection, builder);
        }
        else if (object.getClass().isArray())
        {
            encodeArray(object, builder);
        }
        else if (object instanceof Map<?, ?> map)
        {
            encodeMap(map, builder);
        }
        else if (object instanceof Class<?> class1)
        {
            encode(class1.getName(), builder);
        }
        else
        {
            encodeBean(object, builder);
        }
    }

    /**
     * Encode a Java collection as JS array.
     */
    private static void encodeCollection(Collection<?> collection, StringBuilder builder)
    {
        builder.append('[');
        int i = 0;

        for (Object element : collection)
        {
            if (i++ > 0)
            {
                builder.append(',');
            }

            encode(element, builder);
        }

        builder.append(']');
    }

    /**
     * Encode a Java array as JS array.
     */
    private static void encodeArray(Object array, StringBuilder builder)
    {
        builder.append('[');
        int length = Array.getLength(array);

        for (int i = 0; i < length; i++)
        {
            if (i > 0)
            {
                builder.append(',');
            }

            encode(Array.get(array, i), builder);
        }

        builder.append(']');
    }

    /**
     * Encode a Java map as JS object.
     */
    private static void encodeMap(Map<?, ?> map, StringBuilder builder)
    {
        builder.append('{');
        int i = 0;

        for (Entry<?, ?> entry : map.entrySet())
        {
            if (i++ > 0)
            {
                builder.append(',');
            }

            encode(String.valueOf(entry.getKey()), builder);
            builder.append(':');
            encode(entry.getValue(), builder);
        }

        builder.append('}');
    }

    /**
     * Encode a Java bean as JS object.
     */
    private static void encodeBean(Object bean, StringBuilder builder)
    {
        BeanInfo beanInfo;

        try
        {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        }
        catch (IntrospectionException e)
        {
            throw new IllegalArgumentException(
                    ERROR_INVALID_BEAN.formatted(bean.getClass()), e);
        }

        builder.append('{');
        int i = 0;

        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
        {
            if (property.getReadMethod() == null || "class".equals(property.getName()))
            {
                continue;
            }

            Object value;

            try
            {
                value = property.getReadMethod().invoke(bean);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(
                        ERROR_INVALID_GETTER.formatted(property.getName(), bean.getClass()), e);
            }

            if (value == null)
            {
                continue;
            }

            if (i++ > 0)
            {
                builder.append(',');
            }

            encode(property.getName(), builder);
            builder.append(':');
            encode(value, builder);
        }

        builder.append('}');
    }

    
    // Escaping/unescaping --------------------------------------------------------------------------------------------
    
    private static final int UNICODE_3_BYTES = 0xfff;
    private static final int UNICODE_2_BYTES = 0xff;
    private static final int UNICODE_1_BYTE = 0xf;
    private static final int UNICODE_END_PRINTABLE_ASCII = 0x7f;
    private static final int UNICODE_BEGIN_PRINTABLE_ASCII = 0x20;    
    
    /**
     * Escapes the given string according the JavaScript code rules. This escapes among others the special characters,
     * the whitespace, the quotes and the unicode characters. Useful whenever you want to use a Java string variable as
     * a JavaScript string variable.
     *
     * @param string The string to be escaped according the JavaScript code rules.
     * @param escapeSingleQuote Whether to escape single quotes as well or not. Set to <code>false</code> if you want to
     * escape it for usage in JSON.
     * @return The escaped string according the JavaScript code rules.
     */
    public static String escapeJS(String string, boolean escapeSingleQuote)
    {
        if (string == null)
        {
            return null;
        }

        StringBuilder builder = new StringBuilder(string.length());

        for (char c : string.toCharArray())
        {
            if (c > UNICODE_3_BYTES)
            {
                builder.append("\\u").append(Integer.toHexString(c));
            }
            else if (c > UNICODE_2_BYTES)
            {
                builder.append("\\u0").append(Integer.toHexString(c));
            }
            else if (c > UNICODE_END_PRINTABLE_ASCII)
            {
                builder.append("\\u00").append(Integer.toHexString(c));
            }
            else if (c < UNICODE_BEGIN_PRINTABLE_ASCII)
            {
                escapeJSControlCharacter(builder, c);
            }
            else
            {
                escapeJSASCIICharacter(builder, c, escapeSingleQuote);
            }
        }

        return builder.toString();
    }

    private static void escapeJSControlCharacter(StringBuilder builder, char c)
    {
        switch (c)
        {
            case '\b':
                builder.append('\\').append('b');
                break;
            case '\n':
                builder.append('\\').append('n');
                break;
            case '\t':
                builder.append('\\').append('t');
                break;
            case '\f':
                builder.append('\\').append('f');
                break;
            case '\r':
                builder.append('\\').append('r');
                break;
            default:
                if (c > UNICODE_1_BYTE)
                {
                    builder.append("\\u00").append(Integer.toHexString(c));
                }
                else
                {
                    builder.append("\\u000").append(Integer.toHexString(c));
                }

                break;
        }
    }

    private static void escapeJSASCIICharacter(StringBuilder builder, char c, boolean escapeSingleQuote)
    {
        switch (c)
        {
            case '\'':
                if (escapeSingleQuote)
                {
                    builder.append('\\');
                }
                builder.append('\'');
                break;
            case '"':
                builder.append('\\').append('"');
                break;
            case '\\':
                builder.append('\\').append('\\');
                break;
            case '/':
                builder.append('\\').append('/');
                break;
            default:
                builder.append(c);
                break;
        }
    }
    
    // Dates ----------------------------------------------------------------------------------------------------------
    
    private static final String PATTERN_RFC1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");
    
    /**
     * Formats the given {@link Date} to a string in RFC1123 format. This format is used in HTTP headers and in
     * JavaScript <code>Date</code> constructor.
     *
     * @param date The <code>Date</code> to be formatted to a string in RFC1123 format.
     * @return The formatted string.
     * @since 1.2
     */
    public static String formatRFC1123(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_RFC1123_DATE, Locale.US);
        sdf.setTimeZone(TIMEZONE_GMT);
        return sdf.format(date);
    }

    /**
     * Parses the given string in RFC1123 format to a {@link Date} object.
     *
     * @param string The string in RFC1123 format to be parsed to a <code>Date</code> object.
     * @return The parsed <code>Date</code>.
     * @throws ParseException When the given string is not in RFC1123 format.
     * @since 1.2
     */
    public static Date parseRFC1123(String string) throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_RFC1123_DATE, Locale.US);
        return sdf.parse(string);
    }

}
