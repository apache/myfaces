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
package org.apache.myfaces.core.api.shared;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.CompositeComponentExpressionHolder;

public class ValueReferenceResolver
{
    /**
     * Get the ValueReference from the ValueExpression.
     *
     * @param valueExpression The ValueExpression for value.
     * @param context The FacesContext.
     * @return A ValueReferenceWrapper with the necessary information about the ValueReference.
     */
    public static ValueReference resolve(ValueExpression valueExpression, FacesContext context)
    {
        ELContext elCtx = context.getELContext();
        return ValueReferenceResolver.resolve(valueExpression, elCtx);
    }

    /**
     * This method can be used to extract the ValueReference from the given ValueExpression.
     *
     * @param valueExpression The ValueExpression to resolve.
     * @param elCtx The ELContext, needed to parse and execute the expression.
     * @return The ValueReferenceWrapper.
     */
    public static ValueReference resolve(ValueExpression valueExpression, ELContext elCtx)
    {
        ValueReference valueReference = valueExpression.getValueReference(elCtx);
        
        while (valueReference != null && valueReference.getBase() instanceof CompositeComponentExpressionHolder)
        {
            valueExpression = ((CompositeComponentExpressionHolder) valueReference.getBase())
                                  .getExpression((String) valueReference.getProperty());
            if (valueExpression == null)
            {
                break;
            }
            valueReference = valueExpression.getValueReference(elCtx);
        }

        return valueReference;
    }
}
