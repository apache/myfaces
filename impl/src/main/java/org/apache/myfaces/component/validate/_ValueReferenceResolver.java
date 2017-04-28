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

package org.apache.myfaces.component.validate;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.el.CompositeComponentExpressionHolder;

/**
 *
 */
/**
 * This class inspects the EL expression and returns a ValueReferenceWrapper
 * when Unified EL is not available.
 */
final class _ValueReferenceResolver extends ELResolver
{
    private final ELResolver resolver;

    /**
     * This is a simple solution to keep track of the resolved objects,
     * since ELResolver provides no way to know if the current ELResolver
     * is the last one in the chain. By assigning (and effectively overwriting)
     * this field, we know that the value after invoking the chain is always
     * the last one.
     *
     * This solution also deals with nested objects (like: #{myBean.prop.prop.prop}.
     */
    private _ValueReferenceWrapper lastObject;

    /**
     * Constructor is only used internally.
     * @param elResolver An ELResolver from the current ELContext.
     */
    _ValueReferenceResolver(final ELResolver elResolver)
    {
        this.resolver = elResolver;
    }

    /**
     * This method can be used to extract the ValueReferenceWrapper from the given ValueExpression.
     *
     * @param valueExpression The ValueExpression to resolve.
     * @param elCtx The ELContext, needed to parse and execute the expression.
     * @return The ValueReferenceWrapper.
     */
    public static _ValueReferenceWrapper resolve(ValueExpression valueExpression, final ELContext elCtx)
    {
        _ValueReferenceResolver resolver = new _ValueReferenceResolver(elCtx.getELResolver());
        ELContext elCtxDecorator = new _ELContextDecorator(elCtx, resolver);
        
        valueExpression.getValue(elCtxDecorator);
        
        while (resolver.lastObject.getBase() instanceof CompositeComponentExpressionHolder)
        {
            valueExpression = ((CompositeComponentExpressionHolder) resolver.lastObject.getBase())
                                  .getExpression((String) resolver.lastObject.getProperty());
            valueExpression.getValue(elCtxDecorator);
        }

        return resolver.lastObject;
    }

    /**
     * This method is the only one that matters. It keeps track of the objects in the EL expression.
     *
     * It creates a new ValueReferenceWrapper and assigns it to lastObject.
     *
     * @param context The ELContext.
     * @param base The base object, may be null.
     * @param property The property, may be null.
     * @return The resolved value
     */
    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
    {
        lastObject = new _ValueReferenceWrapper(base, property);
        return resolver.getValue(context, base, property);
    }

    // ############################ Standard delegating implementations ############################
    public Class<?> getType(final ELContext ctx, final Object base, final Object property)
    {
        return resolver.getType(ctx, base, property);
    }

    public void setValue(final ELContext ctx, final Object base, final Object property, final Object value)
    {
        resolver.setValue(ctx, base, property, value);
    }

    public boolean isReadOnly(final ELContext ctx, final Object base, final Object property)
    {
        return resolver.isReadOnly(ctx, base, property);
    }

    public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext ctx, final Object base)
    {
        return resolver.getFeatureDescriptors(ctx, base);
    }

    public Class<?> getCommonPropertyType(final ELContext ctx, final Object base)
    {
        return resolver.getCommonPropertyType(ctx, base);
    }
}

