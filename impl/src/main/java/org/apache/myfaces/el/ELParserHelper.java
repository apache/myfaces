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
package org.apache.myfaces.el;

import java.io.StringReader;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.ReferenceSyntaxException;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.myfaces.shared_impl.util.StringUtils;

import org.apache.commons.el.ArraySuffix;
import org.apache.commons.el.BinaryOperatorExpression;
import org.apache.commons.el.Coercions;
import org.apache.commons.el.ComplexValue;
import org.apache.commons.el.ConditionalExpression;
import org.apache.commons.el.Expression;
import org.apache.commons.el.ExpressionString;
import org.apache.commons.el.FunctionInvocation;
import org.apache.commons.el.Literal;
import org.apache.commons.el.Logger;
import org.apache.commons.el.NamedValue;
import org.apache.commons.el.PropertySuffix;
import org.apache.commons.el.UnaryOperatorExpression;
import org.apache.commons.el.ValueSuffix;
import org.apache.commons.el.parser.ELParser;
import org.apache.commons.el.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class to implement support functionality to "morph" JSP EL into JSF
 * EL
 *
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ELParserHelper
{
    static final Log           log    = LogFactory.getLog(ELParserHelper.class);
    public static final Logger LOGGER = new Logger(System.out);

    private ELParserHelper()
    {
        // util class, do not instantiate
    }

    /**
     * Gets the parsed form of the given expression string. Returns either an
     * Expression or ExpressionString.
     */
    public static Object parseExpression(String expressionString)
    {
        expressionString = toJspElExpression(expressionString);

        ELParser parser = new ELParser(new StringReader(expressionString));
        try
        {
            Object expression = parser.ExpressionString();
            if (!(expression instanceof Expression)
                && !(expression instanceof ExpressionString))
            {
                throw new ReferenceSyntaxException("Invalid expression: '"
                    + expressionString
                    + "'. Parsed Expression of unexpected type "
                    + expression.getClass().getName());
            }

            replaceSuffixes(expression);

            return expression;
        }
        catch (ParseException e)
        {
            String msg = "Invalid expression: '" + expressionString + "'";
            throw new ReferenceSyntaxException(msg, e);
        }
    }

    /**
     * Convert ValueBinding syntax #{ } to JSP EL syntax ${ }
     *
     * @param expressionString <code>ValueBinding</code> reference expression
     *
     * @return JSP EL compatible expression
     */
    static String toJspElExpression(String expressionString)
    {
        StringBuffer sb = new StringBuffer(expressionString.length());
        int remainsPos = 0;

        for (int posOpenBrace = expressionString.indexOf('{'); posOpenBrace >= 0;
            posOpenBrace = expressionString.indexOf('{', remainsPos))
        {
            if (posOpenBrace > 0)
            {
                if( posOpenBrace-1 > remainsPos )
                    sb.append(expressionString.substring(remainsPos, posOpenBrace - 1));

                if (expressionString.charAt(posOpenBrace - 1) == '$')
                {
                    sb.append("${'${'}");
                    remainsPos = posOpenBrace+1;
                    continue;
                }
                else if (expressionString.charAt(posOpenBrace - 1) == '#')
                {
//                    // TODO: should use \\ as escape for \ always, not just when before #{
//                    // allow use of '\' as escape symbol for #{ (for compatibility with Sun's extended implementation)
//                    if (isEscaped(expressionString, posOpenBrace - 1))
//                    {
//                      escapes: {
//                            for (int i = sb.length() - 1; i >= 0; i--)
//                            {
//                                if (sb.charAt(i) != '\\')
//                                {
//                                    sb.setLength(
//                                        sb.length() - (sb.length() - i) / 2);
//                                    break escapes;
//                                }
//                            }
//                            sb.setLength(sb.length() / 2);
//                        }
//                        sb.append("#{");
//                    }
//                    else
//                    {
                        sb.append("${");
                        int posCloseBrace = indexOfMatchingClosingBrace(expressionString, posOpenBrace);
                        sb.append(expressionString.substring(posOpenBrace + 1, posCloseBrace + 1));
                        remainsPos = posCloseBrace + 1;
                        continue;
//                    }
                }else{
                    if( posOpenBrace > remainsPos )
                        sb.append( expressionString.charAt(posOpenBrace - 1) );
                }
            }

            // Standalone brace
            sb.append('{');
            remainsPos = posOpenBrace + 1;
        }

        sb.append(expressionString.substring(remainsPos));

        // Create a new String to shrink mem size since we are caching
        return new String(sb.toString());
    }

    private static int findQuote(String expressionString, int start)
    {
        int indexofSingleQuote = expressionString.indexOf('\'', start);
        int indexofDoubleQuote = expressionString.indexOf('"', start);
        return StringUtils.minIndex(indexofSingleQuote, indexofDoubleQuote);
    }

    /**
     * Return the index of the matching closing brace, skipping over quoted text
     *
     * @param expressionString string to search
     * @param indexofOpeningBrace the location of opening brace to match
     *
     * @return the index of the matching closing brace
     *
     * @throws ReferenceSyntaxException if matching brace cannot be found
     */
    private static int indexOfMatchingClosingBrace(String expressionString,
        int indexofOpeningBrace)
    {
        int len = expressionString.length();
        int i = indexofOpeningBrace + 1;

        // Loop through quoted strings
        for (;;)
        {
            if (i >= len)
            {
                throw new ReferenceSyntaxException(
                    "Missing closing brace. Expression: '" + expressionString
                        + "'");
            }

            int indexofClosingBrace = expressionString.indexOf('}', i);
            i = StringUtils.minIndex(indexofClosingBrace, findQuote(
                expressionString, i));

            if (i < 0)
            {
                // No delimiter found
                throw new ReferenceSyntaxException(
                    "Missing closing brace. Expression: '" + expressionString
                        + "'");
            }

            // 1. If quoted literal, find closing quote
            if (i != indexofClosingBrace)
            {
                i = indexOfMatchingClosingQuote(expressionString, i) + 1;
                if (i == 0)
                {
                    // Note: if no match, i==0 because -1 + 1 = 0
                    throw new ReferenceSyntaxException(
                        "Missing closing quote. Expression: '"
                            + expressionString + "'");
                }
            }
            else
            {
                // Closing brace
                return i;
            }
        }
    }

    /**
     * Returns the index of the matching closing quote, skipping over escaped
     * quotes
     *
     * @param expressionString string to scan
     * @param indexOfOpeningQuote start from this position in the string
     * @return -1 if no match, the index of closing quote otherwise
     */
    private static int indexOfMatchingClosingQuote(String expressionString,
        int indexOfOpeningQuote)
    {
        char quote = expressionString.charAt(indexOfOpeningQuote);
        for (int i = expressionString.indexOf(quote, indexOfOpeningQuote + 1);
            i >= 0; i = expressionString.indexOf(quote, i + 1))
        {
            if (!isEscaped(expressionString, i))
            {
                return i;
            }
        }

        // No matching quote found
        return -1;
    }

    private static boolean isEscaped(String expressionString, int i)
    {
        int escapeCharCount = 0;
        while ((--i >= 0) && (expressionString.charAt(i) == '\\'))
        {
            escapeCharCount++;
        }

        return (escapeCharCount % 2) != 0;
    }

    /**
     * Replaces all <code>ValueSuffix</code>es with custom implementation
     * ValueSuffexes that use JSF <code>PropertyResolver</code> insted of JSP
     * EL one.
     *
     * @param expression <code>Expression</code> or
     *        <code>ExpressionString</code> instance
     * @param application <code>Application</code> instance to get
     *        <code>PropertyResolver</code> from
     */
    private static void replaceSuffixes(Object expression)
    {
        if (expression instanceof Expression)
        {
            replaceSuffixes((Expression) expression);
        }
        else if (expression instanceof ExpressionString)
        {
            replaceSuffixes((ExpressionString) expression);
        }
        else
        {
            throw new IllegalStateException(
                "Expression element of unknown class: "
                    + expression.getClass().getName());
        }
    }

    private static void replaceSuffixes(ExpressionString expressionString)
    {
        Object[] expressions = expressionString.getElements();
        for (int i = 0, len = expressions.length; i < len; i++)
        {
            Object expression = expressions[i];
            if (expression instanceof Expression)
            {
                replaceSuffixes((Expression) expression);
            }
            else if (expression instanceof ExpressionString)
            {
                replaceSuffixes((ExpressionString) expression);
            }
            else if (!(expression instanceof String))
            {
                throw new IllegalStateException(
                    "Expression element of unknown class: "
                        + expression.getClass().getName());
            }
            // ignore Strings
        }
    }

    static void replaceSuffixes(Expression expression)
    {
        if (expression instanceof BinaryOperatorExpression)
        {
            BinaryOperatorExpression boe = (BinaryOperatorExpression) expression;
            replaceSuffixes(boe.getExpression());
            for (int i = 0; i < boe.getExpressions().size(); i++)
            {
                replaceSuffixes(boe.getExpressions().get(i));
            }
        }
        else if (expression instanceof ComplexValue)
        {
            replaceSuffixes((ComplexValue) expression);
        }
        else if (expression instanceof ConditionalExpression)
        {
            ConditionalExpression conditionalExpression =
                (ConditionalExpression) expression;
            replaceSuffixes(conditionalExpression.getTrueBranch());
            replaceSuffixes(conditionalExpression.getFalseBranch());
        }
        else if (expression instanceof UnaryOperatorExpression)
        {
            replaceSuffixes(((UnaryOperatorExpression) expression)
                .getExpression());
        }

        // ignore the remaining expression types
        else if (!(expression instanceof FunctionInvocation
            || expression instanceof Literal || expression instanceof NamedValue))
        {
            throw new IllegalStateException(
                "Expression element of unknown class: "
                    + expression.getClass().getName());
        }
    }

    private static void replaceSuffixes(ComplexValue complexValue)
    {
        Application application = FacesContext.getCurrentInstance()
            .getApplication();

        List suffixes = complexValue.getSuffixes();
        for (int i = 0, len = suffixes.size(); i < len; i++)
        {
            ValueSuffix suffix = (ValueSuffix) suffixes.get(i);
            if (suffix instanceof PropertySuffix)
            {
                if (suffix instanceof MyPropertySuffix)
                {
                    throw new IllegalStateException(
                        "Suffix is MyPropertySuffix and must not be");
                }

                suffixes.set(i, new MyPropertySuffix((PropertySuffix) suffix,
                    application));
            }
            else if (suffix instanceof ArraySuffix)
            {
                if (suffix instanceof MyArraySuffix)
                {
                    throw new IllegalStateException(
                        "Suffix is MyArraySuffix and must not be");
                }

                suffixes.set(i, new MyArraySuffix((ArraySuffix) suffix,
                    application));
            }
            else
            {
                throw new IllegalStateException("Unknown suffix class: "
                    + suffix.getClass().getName());
            }
        }
    }

    private static Integer coerceToIntegerWrapper(Object base, Object index)
        throws EvaluationException, ELException
    {
        Integer integer = Coercions.coerceToInteger(index, LOGGER);
        if (integer != null)
        {
            return integer;
        }
        throw new ReferenceSyntaxException(
            "Cannot convert index to int for base " + base.getClass().getName()
                + " and index " + index);
    }

    /**
     * Coerces <code>index</code> to Integer for array types, or returns
     * <code>null</code> for non-array types.
     *
     * @param base Object for the base
     * @param index Object for the index
     * @return Integer a valid Integer index, or null if not an array type
     *
     * @throws ELException if exception occurs trying to coerce to Integer
     * @throws EvaluationException if base is array type but cannot convert
     *         index to Integer
     */
    public static Integer toIndex(Object base, Object index)
        throws ELException, EvaluationException
    {
        if ((base instanceof List) || (base.getClass().isArray()))
        {
            return coerceToIntegerWrapper(base, index);
        }
        if (base instanceof UIComponent)
        {
            try
            {
                return coerceToIntegerWrapper(base, index);
            }
            catch (Throwable t)
            {
                // treat as simple property
                return null;
            }
        }

        // If not an array type
        return null;
    }

    /**
     * Override ArraySuffix.evaluate() to use our property resolver
     */
    public static class MyArraySuffix extends ArraySuffix
    {
        private Application _application;

        public MyArraySuffix(ArraySuffix arraySuffix, Application application)
        {
            super(arraySuffix.getIndex());
            replaceSuffixes(getIndex());
            _application = application;
        }

        /**
         * Evaluates the expression in the given context, operating on the given
         * value, using JSF property resolver.
         */
        public Object evaluate(Object base, VariableResolver variableResolver,
            FunctionMapper functions, Logger logger)
            throws ELException
        {
            // Check for null value
            if (base == null)
            {
                return null;
            }

            // Evaluate the index
            Object indexVal = getIndex().evaluate(variableResolver, functions,
                logger);
            if (indexVal == null)
            {
                return null;
            }

            Integer index = toIndex(base, indexVal);
            if (index == null)
            {
                return _application.getPropertyResolver().getValue(base,
                    indexVal);
            }
            else
            {
                return _application.getPropertyResolver().getValue(base,
                    index.intValue());
            }
        }
    }

    public static class MyPropertySuffix extends PropertySuffix
    {
        private Application _application;

        public MyPropertySuffix(PropertySuffix propertySuffix,
            Application application)
        {
            super(propertySuffix.getName());
            _application = application;
        }

        /**
         * Evaluates the expression in the given context, operating on the given
         * value, using JSF property resolver.
         */
        public Object evaluate(Object base, VariableResolver variableResolver,
            FunctionMapper functions, Logger logger)
            throws ELException
        {
            // Check for null value
            if (base == null)
            {
                return null;
            }

            // Evaluate the index
            String indexVal = getName();
            if (indexVal == null)
            {
                return null;
            }

            Integer index = toIndex(base, indexVal);
            if (index == null)
            {
                return _application.getPropertyResolver().getValue(base,
                    indexVal);
            }
            else
            {
                return _application.getPropertyResolver().getValue(base,
                    index.intValue());
            }
        }
    }
}
