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

import java.util.Locale;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;

/**
 * This ELContext is used to hook into the EL handling, by decorating the
 * ELResolver chain with a custom ELResolver.
 */
public class ELContextDecorator extends ELContext
{
    private final ELContext ctx;
    private final ELResolver interceptingResolver;

    /**
     * Only used by ValueExpressionResolver.
     *
     * @param elContext The standard ELContext. All method calls, except getELResolver, are delegated to it.
     * @param interceptingResolver The ELResolver to be returned by getELResolver.
     */
    public ELContextDecorator(final ELContext elContext, final ELResolver interceptingResolver)
    {
        this.ctx = elContext;
        this.interceptingResolver = interceptingResolver;
    }

    /**
     * This is the important one, it returns the passed ELResolver.
     * @return The ELResolver passed into the constructor.
     */
    @Override
    public ELResolver getELResolver()
    {
        return interceptingResolver;
    }

    @Override
    public FunctionMapper getFunctionMapper()
    {
        return ctx.getFunctionMapper();
    }

    @Override
    public VariableMapper getVariableMapper()
    {
        return ctx.getVariableMapper();
    }

    @Override
    public void setPropertyResolved(final boolean resolved)
    {
        ctx.setPropertyResolved(resolved);
    }

    @Override
    public boolean isPropertyResolved()
    {
        return ctx.isPropertyResolved();
    }

    @Override
    public void putContext(final Class key, Object contextObject)
    {
        ctx.putContext(key, contextObject);
    }

    @Override
    public Object getContext(final Class key)
    {
        return ctx.getContext(key);
    }

    @Override
    public Locale getLocale()
    {
        return ctx.getLocale();
    }

    @Override
    public void setLocale(final Locale locale)
    {
        ctx.setLocale(locale);
    }
}
