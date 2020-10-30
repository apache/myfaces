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

import java.util.HashMap;
import java.util.Map;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * <p>Mock implementation of <code>VariableMapper</code>.</p>
 *
 * @since 1.0.0
 */

public class MockVariableMapper extends VariableMapper
{

    // ------------------------------------------------------------ Constructors

    /** Creates a new instance of MockVariableMapper */
    public MockVariableMapper()
    {
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>Map of <code>ValueExpression</code>s, keyed by variable name.</p>
     */
    private Map expressions = new HashMap();

    // ----------------------------------------------------- Mock Object Methods

    // -------------------------------------------------- FunctionMapper Methods

    /** {@inheritDoc} */
    public ValueExpression resolveVariable(String variable)
    {

        return (ValueExpression) expressions.get(variable);

    }

    /** {@inheritDoc} */
    public ValueExpression setVariable(String variable,
            ValueExpression expression)
    {

        ValueExpression original = (ValueExpression) expressions.get(variable);
        if (expression == null)
        {
            expressions.remove(variable);
        }
        else
        {
            expressions.put(variable, expression);
        }
        return original;

    }

}
