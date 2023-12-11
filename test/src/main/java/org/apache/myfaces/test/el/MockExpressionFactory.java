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

package org.apache.myfaces.test.el;

import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

/**
 * <p>Mock implementation of <code>ExpressionFactory</code>.</p>
 *
 * @since 1.0.0
 */
public class MockExpressionFactory extends ExpressionFactory
{

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of MockExpressionFactory */
    public MockExpressionFactory()
    {
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>Literal numeric value for zero.</p>
     */
    private static final Integer ZERO = Integer.valueOf(0);

    // ----------------------------------------------------- Mock Object Methods

    // ----------------------------------------------- ExpressionFactory Methods

    @Override
    public Object coerceToType(Object object, Class targetType)
    {

        // Check for no conversion necessary
        if ((targetType == null) || Object.class.equals(targetType))
        {
            return object;
        }

        // Coerce to String if appropriate
        if (String.class.equals(targetType))
        {
            if (object == null)
            {
                return "";
            }
            else if (object instanceof String)
            {
                return object;
            }
            else
            {
                return object.toString();
            }
        }

        // Coerce to Number (or a subclass of Number) if appropriate
        if (isNumeric(targetType))
        {
            if (object == null)
            {
                return coerce(ZERO, targetType);
            }
            else if ("".equals(object))
            {
                return coerce(ZERO, targetType);
            }
            else if (object instanceof String string)
            {
                return coerce(string, targetType);
            }
            else if (isNumeric(object.getClass()))
            {
                return coerce((Number) object, targetType);
            }
            throw new IllegalArgumentException("Cannot convert " + object
                    + " to Number");
        }

        // Coerce to Boolean if appropriate
        if (Boolean.class.equals(targetType) || (Boolean.TYPE == targetType))
        {
            if (object == null)
            {
                return Boolean.FALSE;
            }
            else if ("".equals(object))
            {
                return Boolean.FALSE;
            }
            else if ((object instanceof Boolean)
                    || (object.getClass() == Boolean.TYPE))
            {
                return object;
            }
            else if (object instanceof String string)
            {
                return Boolean.valueOf(string);
            }
            throw new IllegalArgumentException("Cannot convert " + object
                    + " to Boolean");
        }

        // Coerce to Character if appropriate
        if (Character.class.equals(targetType)
                || (Character.TYPE == targetType))
        {
            if (object == null)
            {
                return Character.valueOf((char) 0);
            }
            else if ("".equals(object))
            {
                return Character.valueOf((char) 0);
            }
            else if (object instanceof String string)
            {
                return Character.valueOf(string.charAt(0));
            }
            else if (isNumeric(object.getClass()))
            {
                return Character.valueOf((char) ((Number) object).shortValue());
            }
            else if ((object instanceof Character)
                    || (object.getClass() == Character.TYPE))
            {
                return object;
            }
            throw new IllegalArgumentException("Cannot convert " + object
                    + " to Character");
        }
        
        if (targetType.isEnum())
        {
            if (object == null || "".equals(object))
            {
                return null;
            }
            if (targetType.isAssignableFrom(object.getClass()))
            {
                return object;
            }
            
            if (!(object instanceof String))
            {
                throw new IllegalArgumentException("Cannot convert " + object + " to Enum");
            }

            Enum<?> result;
            try
            {
                 result = Enum.valueOf(targetType, (String) object);
                 return result;
            }
            catch (IllegalArgumentException iae)
            {
                throw new IllegalArgumentException("Cannot convert " + object + " to Enum");
            }
        }

        // Is the specified value type-compatible already?
        if ((object != null) && targetType.isAssignableFrom(object.getClass()))
        {
            return object;
        }

        // new to spec
        if (object == null)
        {
            return null;
        }

        // We do not know how to perform this conversion
        throw new IllegalArgumentException("Cannot convert " + object + " to "
                + targetType.getName());

    }

    @Override
    public MethodExpression createMethodExpression(ELContext context,
            String expression, Class expectedType, Class[] signature)
    {

        return new MockMethodExpression(expression, signature, expectedType);

    }

    @Override
    public ValueExpression createValueExpression(ELContext context,
            String expression, Class expectedType)
    {

        return new MockCompositeValueExpression(expression, expectedType);

    }

    @Override
    public ValueExpression createValueExpression(Object instance,
            Class expectedType)
    {

        return new MockVariableValueExpression(instance, expectedType);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Coerce the specified value to the specified Number subclass.</p>
     *
     * @param value Value to be coerced
     * @param type Destination type
     */
    private Number coerce(Number value, Class type)
    {

        if ((type == Byte.TYPE) || (type == Byte.class))
        {
            return Byte.valueOf(value.byteValue());
        }
        else if ((type == Double.TYPE) || (type == Double.class))
        {
            return Double.valueOf(value.doubleValue());
        }
        else if ((type == Float.TYPE) || (type == Float.class))
        {
            return Float.valueOf(value.floatValue());
        }
        else if ((type == Integer.TYPE) || (type == Integer.class))
        {
            return Integer.valueOf(value.intValue());
        }
        else if ((type == Long.TYPE) || (type == Long.class))
        {
            return Long.valueOf(value.longValue());
        }
        else if ((type == Short.TYPE) || (type == Short.class))
        {
            return Short.valueOf(value.shortValue());
        }
        else if (type == BigDecimal.class)
        {
            if (value instanceof BigDecimal)
            {
                return value;
            }
            else if (value instanceof BigInteger integer)
            {
                return new BigDecimal(integer);
            }
            else
            {
                return new BigDecimal(value.doubleValue());
            }
        }
        else if (type == BigInteger.class)
        {
            if (value instanceof BigInteger)
            {
                return value;
            }
            else if (value instanceof BigDecimal decimal)
            {
                return decimal.toBigInteger();
            }
            else
            {
                return BigInteger.valueOf(value.longValue());
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to "
                + type.getName());

    }

    /**
     * <p>Coerce the specified value to the specified Number subclass.</p>
     *
     * @param value Value to be coerced
     * @param type Destination type
     */
    private Number coerce(String value, Class type)
    {

        if ((type == Byte.TYPE) || (type == Byte.class))
        {
            return Byte.valueOf(value);
        }
        else if ((type == Double.TYPE) || (type == Double.class))
        {
            return Double.valueOf(value);
        }
        else if ((type == Float.TYPE) || (type == Float.class))
        {
            return Float.valueOf(value);
        }
        else if ((type == Integer.TYPE) || (type == Integer.class))
        {
            return Integer.valueOf(value);
        }
        else if ((type == Long.TYPE) || (type == Long.class))
        {
            return Long.valueOf(value);
        }
        else if ((type == Short.TYPE) || (type == Short.class))
        {
            return Short.valueOf(value);
        }
        else if (type == BigDecimal.class)
        {
            return new BigDecimal(value);
        }
        else if (type == BigInteger.class)
        {
            return new BigInteger(value);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to "
                + type.getName());

    }

    /**
     * <p>Return <code>true</code> if the specified type is numeric.</p>
     *
     * @param type Type to check
     */
    private boolean isNumeric(Class type)
    {

        return (type == Byte.TYPE) || (type == Byte.class)
                || (type == Double.TYPE) || (type == Double.class)
                || (type == Float.TYPE) || (type == Float.class)
                || (type == Integer.TYPE) || (type == Integer.class)
                || (type == Long.TYPE) || (type == Long.class)
                || (type == Short.TYPE) || (type == Short.class)
                || (type == BigDecimal.class) || (type == BigInteger.class);

    }

}
