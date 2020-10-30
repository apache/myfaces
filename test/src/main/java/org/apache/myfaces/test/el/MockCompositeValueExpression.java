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

import java.util.ArrayList;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

/**
 * A value expression implementation that is capable of handling composite expressions.
 * It handles composites expressions but creating a list of 'simple' expressions which 
 * are 'pure', only literal text or only references like #{}
 * 
 * @author Rudy De Busscher
 * @since 1.0.0
 */
public class MockCompositeValueExpression extends MockValueExpression
{

    private static final long serialVersionUID = 2645070462654392076L;

    private List<ValueExpression> valueExpressionChain;

    public MockCompositeValueExpression(String expression, Class expectedType)
    {

        super("#{}", expectedType);
        buildExpressionChain(expression, expectedType);

    }

    private void buildExpressionChain(String expression, Class expectedType)
    {
        valueExpressionChain = new ArrayList<ValueExpression>();
        StringBuilder parser = new StringBuilder(expression);
        int pos = getStartPositionOfReference(parser);
        while (pos > -1 || parser.length() > 0)
        {
            // We have a constant first
            if (pos > 0)
            {
                valueExpressionChain.add(new MockValueExpression(parser
                        .substring(0, pos), expectedType));
                parser.delete(0, pos);
            }
            // We have an el, maybe literal at the end
            if (pos == 0)
            {
                int posBracket = parser.indexOf("}");
                valueExpressionChain.add(new MockValueExpression(parser
                        .substring(0, posBracket + 1), expectedType));

                parser.delete(0, posBracket + 1);
            }
            // Only literal
            if (pos == -1)
            {
                valueExpressionChain.add(new MockValueExpression(parser
                        .toString(), expectedType));

                parser.setLength(0);
            }
            pos = getStartPositionOfReference(parser);
        }
    }

    @Override
    public Class getType(ELContext context)
    {
        switch (valueExpressionChain.size())
        {
        case 0:
            return null;
        case 1:
            return valueExpressionChain.get(0).getType(context);
        default:
            return String.class;
        }
    }

    @Override
    public Object getValue(ELContext context)
    {
        if (valueExpressionChain.size() > 1)
        {
            // Well only composite strings are supported.

            StringBuilder result = new StringBuilder();
            for (ValueExpression valueExpression : valueExpressionChain)
            {
                result.append(valueExpression.getValue(context));
            }
            return result.toString();
        }
        else
        {
            if (valueExpressionChain.size() == 1)
            {
                return valueExpressionChain.get(0).getValue(context);
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public void setValue(ELContext context, Object value)
    {
        if (!isReadOnly(context))
        {
            valueExpressionChain.get(0).setValue(context, value);
        }
        else
        {
            throw new IllegalArgumentException(
                    "We can only set value on NON composite expressions like #{foo}");
        }
    }

    @Override
    public String getExpressionString()
    {
        StringBuilder result = new StringBuilder();
        for (ValueExpression valueExpression : valueExpressionChain)
        {
            result.append(valueExpression.getExpressionString());
        }
        return result.toString();
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        return valueExpressionChain.size() > 1;
    }

    public static int getStartPositionOfReference(StringBuilder expressionPart)
    {
        int result;
        int pos1 = expressionPart.indexOf("#{");
        int pos2 = expressionPart.indexOf("${");

        if (pos1 == -1)
        {
            result = pos1;
        }
        else if (pos2 == -1)
        {
            result = pos1;
        }
        else
        {
            result = Math.min(pos1, pos2);
        }
        return result;

    }

}
