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

/**
 *
 */
public class CopyBeanInterceptorELResolver extends ELResolver
{
    private final ELResolver resolver;
    private final Object base;
    private final Object copy;


    /**
     * Constructor is only used internally.
     * @param elResolver An ELResolver from the current ELContext.
     */
    public CopyBeanInterceptorELResolver(final ELResolver elResolver, Object base, Object copy)
    {
        this.resolver = elResolver;
        this.base = base;
        this.copy = copy;
    }

    @Override
    public Object getValue(final ELContext context, final Object base, final Object property)
    {
        Object value = resolver.getValue(context, base, property);
        if (this.base == value || this.base.equals(value))
        {
            //Pass the copy instead
            return this.copy;
        }
        else
        {
            return value;
        }
    }

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
