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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>ValueExpression</code>.</p>
 *
 * <p>This implementation supports a limited subset of overall expression functionality:</p>
 * <ul>
 * <li>A literal string that contains no expression delimiters.</li>
 * <li>An expression that starts with "#{" or "${", and ends with "}".</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class MockValueExpression extends ValueExpression
{

    // ------------------------------------------------------------ Constructors

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -8649071428507512623L;

    /**
     * <p>Construct a new expression for the specified expression string.</p>
     *
     * @param expression Expression string to be evaluated
     * @param expectedType Expected type of the result
     */
    public MockValueExpression(String expression, Class expectedType)
    {

        if (expression == null)
        {
            throw new NullPointerException("Expression string cannot be null");
        }
        this.expression = expression;
        this.expectedType = expectedType;
        parse();

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The parsed elements of this expression.</p>
     */
    private String[] elements = null;

    /**
     * <p>The expected result type for <code>getValue()</code> calls.</p>
     */
    private Class expectedType = null;

    /**
     * <p>The original expression string used to create this expression.</p>
     */
    private String expression = null;

    // ------------------------------------------------------ Expression Methods

    /**
     * <p>Return <code>true</code> if this expression is equal to the
     * specified expression.</p>
     *
     * @param obj Object to be compared
     */
    public boolean equals(Object obj)
    {

        if ((obj != null) & (obj instanceof ValueExpression))
        {
            return expression.equals(((ValueExpression) obj)
                    .getExpressionString());
        }
        else
        {
            return false;
        }

    }

    /**
     * <p>Return the original String used to create this expression,
     * unmodified.</p>
     */
    public String getExpressionString()
    {

        return this.expression;

    }

    /**
     * <p>Return the hash code for this expression.</p>
     */
    public int hashCode()
    {

        return this.expression.hashCode();

    }

    /**
     * <p>Return <code>true</code> if the expression string for this expression
     * contains only literal text.</p>
     */
    public boolean isLiteralText()
    {

        return (expression.indexOf("${") < 0) && (expression.indexOf("#{") < 0);

    }

    // ------------------------------------------------- ValueExpression Methods

    /**
     * <p>Return the type that the result of this expression will
     * be coerced to.</p>
     */
    public Class getExpectedType()
    {

        return this.expectedType;

    }

    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return the most general type that is acceptable for the
     * value passed in a <code>setValue()</code> call.</p>
     *
     * @param context ELContext for this evaluation
     */
    public Class getType(ELContext context)
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        Object value = getValue(context);
        if (value == null)
        {
            if (isLiteralText())
            {
                return String.class;
            }

            ELResolver resolver = context.getELResolver();
            Object base = null;
            for (int i = 0; i < elements.length - 1; i++)
            {
                base = resolver.getValue(context, base, elements[i]);
            }
            return resolver.getType(context, base, elements[elements.length - 1]);
        }
        else
        {
            return value.getClass();
        }
    }

    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return the result.</p>
     *
     * @param context ELContext for this evaluation
     */
    public Object getValue(ELContext context)
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        if (isLiteralText())
        {
            return expression;
        }

        FacesContext fcontext = (FacesContext) context
                .getContext(FacesContext.class);

        ELResolver resolver = context.getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length; i++)
        {
            base = resolver.getValue(context, base, elements[i]);
        }
        return fcontext.getApplication().getExpressionFactory().coerceToType(
                base, getExpectedType());

    }

    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return <code>true</code> if a call to <code>setValue()</code>
     * will always fail.</p>
     *
     * @param context ELContext for this evaluation
     */
    public boolean isReadOnly(ELContext context)
    {

        if (context == null)
        {
            throw new NullPointerException();
        }
        if (isLiteralText())
        {
            return true;
        }

        ELResolver resolver = context.getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length - 1; i++)
        {
            base = resolver.getValue(context, base, elements[i]);
        }
        return resolver
                .isReadOnly(context, base, elements[elements.length - 1]);

    }

    /**
     * <p>Evaluate this expression relative to the specified context,
     * and set the result to the specified value.</p>
     *
     * @param context ELContext for this evaluation
     * @param value Value to which the result should be set
     */
    public void setValue(ELContext context, Object value)
    {

        if (context == null)
        {
            throw new NullPointerException();
        }

        ELResolver resolver = context.getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length - 1; i++)
        {
            base = resolver.getValue(context, base, elements[i]);
        }
        resolver.setValue(context, base, elements[elements.length - 1], value);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>Parse the expression string into its constituent elemetns.</p>
     */
    private void parse()
    {

        if (isLiteralText())
        {
            elements = new String[0];
            return;
        }

        if (expression.startsWith("${") || expression.startsWith("#{"))
        {
            if (expression.endsWith("}"))
            {
                elements = ExpressionTokenizer.tokenize(expression.substring(2, expression.length() - 1));
            }
            else
            {
                throw new IllegalArgumentException(expression);
            }
        }
        else
        {
            throw new IllegalArgumentException(expression);
        }

    }

}
