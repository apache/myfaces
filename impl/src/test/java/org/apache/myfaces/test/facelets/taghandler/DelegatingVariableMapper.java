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
package org.apache.myfaces.test.facelets.taghandler;

import java.util.HashMap;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.view.facelets.FaceletContext;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * VariableMapper wrapper used by {@link TagAttributeHandler} (MYFACES-4589 tests).
 * <p>
 * Derived from OmniFaces {@code org.omnifaces.el.DelegatingVariableMapper} (Apache License 2.0, Copyright OmniFaces).
 * </p>
 *
 * @author Arjan Tijms (OmniFaces)
 */
public class DelegatingVariableMapper extends VariableMapper
{

    private final VariableMapper wrapped;
    private final Map<String, ValueExpression> variables = new HashMap<>();

    public DelegatingVariableMapper(VariableMapper wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ValueExpression resolveVariable(String name)
    {
        if (!name.isEmpty() && name.charAt(0) == '@')
        {
            return wrapped.resolveVariable(name.substring(1));
        }
        if (!variables.containsKey(name))
        {
            return wrapped.resolveVariable(name);
        }
        return variables.get(name);
    }

    /**
     * Resolve wrapped variable of given name.
     *
     * @param faceletContext used to read the enclosing Facelets template parameter layer; required so the
     *                       parent binding is not confused with the inner one when nested
     *                       {@link jakarta.el.VariableMapper} delegates strip {@code @name} (MYFACES-4585).
     */
    public ValueExpression resolveWrappedVariable(String name, FaceletContext faceletContext)
    {
        ValueExpression wrappedVariable = wrapped.resolveVariable(name);
        ValueExpression globalVariable = variables.get(name);

        // Reference equality only: ValueExpression.equals() is often implemented as expression-string equality,
        // so two distinct VEs for the same EL (e.g. #{autoCompleteDepartmentConverter}) compare equal. That
        // falsely matched the "parent alias" / "unset" case and made o:tagAttribute / t:tagAttribute apply the
        // default (MYFACES-4585: converter became "" and EL called suggest() on String).
        if (wrappedVariable == globalVariable)
        {
            return null;
        }

        ValueExpression parentVariable = null;
        if (faceletContext instanceof AbstractFaceletContext actx)
        {
            parentVariable = actx.resolveTemplateParameterInEnclosingContext(name);
        }

        if (wrappedVariable == parentVariable)
        {
            return null;
        }

        return wrappedVariable;
    }

    @Override
    public ValueExpression setVariable(String name, ValueExpression expression)
    {
        return variables.put(name, expression);
    }

    /**
     * Sets wrapped variable of given name with given value expression.
     */
    public ValueExpression setWrappedVariable(String name, ValueExpression expression)
    {
        return wrapped.setVariable(name, expression);
    }
}
