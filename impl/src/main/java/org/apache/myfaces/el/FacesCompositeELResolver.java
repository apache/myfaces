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

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;

/**
 * A {@link CompositeELResolver} that specialises two hot paths without changing resolution semantics
 * or the (spec-defined, user-customisable) resolver order.
 *
 * <ol>
 * <li><b>convertToType</b> visits only the sub-resolvers that actually override
 * {@link ELResolver#convertToType}. A resolver that inherits it returns {@code null} without marking
 * the property resolved, so it can never contribute a conversion; skipping it is behaviour-identical.
 * Faces registers ~16 resolvers and a String-coercing cell (e.g. <code>#{row.value}</code>) coerces
 * on every cell, so the full-chain walk was a large slice of the render hot path.</li>
 *
 * <li><b>getValue with {@code base == null}</b> (variable resolution, e.g. resolving the
 * <code>row</code> in <code>#{row.value}</code>) skips the sub-resolvers that are <em>provably</em>
 * property-only - the standard {@code jakarta.el} Map/List/Array/Bean/Record/Optional resolvers, whose
 * {@code getValue} returns {@code null} for a {@code null} base. Every other resolver - the Faces
 * resolvers, {@code FlashELResolver}, and any user-registered resolver - is still consulted, in the
 * original order, so a resolver that legitimately resolves a {@code null} base (like
 * {@code FlashELResolver} for {@code #{flash}}) is never skipped. Deriving the root set by
 * <em>exclusion</em> of known-safe resolvers (rather than a whitelist, which
 * {@link ELResolver#getCommonPropertyType} cannot supply reliably) is what keeps this safe for
 * custom resolvers and the custom ordering.</li>
 * </ol>
 *
 * <p>{@code getValue} with a non-null base, and {@code getType}/{@code setValue}/{@code isReadOnly},
 * keep the stock full-chain behaviour.
 */
public class FacesCompositeELResolver extends CompositeELResolver
{
    private ELResolver[] convertResolvers = new ELResolver[0];
    private ELResolver[] rootResolvers = new ELResolver[0];

    @Override
    public void add(ELResolver elResolver)
    {
        super.add(elResolver);

        if (overridesConvertToType(elResolver))
        {
            convertResolvers = append(convertResolvers, elResolver);
        }
        if (!isPropertyOnly(elResolver))
        {
            rootResolvers = append(rootResolvers, elResolver);
        }
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        if (base == null)
        {
            // Variable resolution: only resolvers that can handle a null base are consulted, in order.
            // The excluded ones (Map/List/Array/Bean/Record/Optional) return null for a null base, so
            // the first resolver to resolve is identical to the full chain walk - just fewer no-op calls.
            context.setPropertyResolved(false);
            final ELResolver[] resolvers = rootResolvers;
            for (int i = 0, len = resolvers.length; i < len; i++)
            {
                Object value = resolvers[i].getValue(context, null, property);
                if (context.isPropertyResolved())
                {
                    return value;
                }
            }
            return null;
        }
        return super.getValue(context, base, property);
    }

    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> targetType) throws ELException
    {
        // Only resolvers that actually implement convertToType can resolve the conversion; the rest
        // inherit the ELResolver no-op default. Visiting only the former is equivalent to the full
        // chain walk (same resolvers, same order) but skips the no-op calls per coercion.
        context.setPropertyResolved(false);
        final ELResolver[] resolvers = convertResolvers;
        for (int i = 0, len = resolvers.length; i < len; i++)
        {
            T value = resolvers[i].convertToType(context, obj, targetType);
            if (context.isPropertyResolved())
            {
                return value;
            }
        }
        return null;
    }

    private static ELResolver[] append(ELResolver[] array, ELResolver elResolver)
    {
        ELResolver[] copy = new ELResolver[array.length + 1];
        System.arraycopy(array, 0, copy, 0, array.length);
        copy[array.length] = elResolver;
        return copy;
    }

    private static boolean overridesConvertToType(ELResolver elResolver)
    {
        try
        {
            return elResolver.getClass().getMethod("convertToType",
                    ELContext.class, Object.class, Class.class).getDeclaringClass() != ELResolver.class;
        }
        catch (NoSuchMethodException e)
        {
            // convertToType is public on ELResolver itself, so this cannot happen
            return false;
        }
    }

    /**
     * True only for the standard {@code jakarta.el} resolvers that resolve a property against a base
     * object and therefore always return {@code null} for a {@code null} base. This is a conservative
     * allow-list of known-safe-to-skip types: anything not matched here (a Faces resolver, a custom
     * resolver, {@code FlashELResolver}, an unknown wrapper) is treated as root-eligible and still
     * consulted for a {@code null} base.
     */
    private static boolean isPropertyOnly(ELResolver elResolver)
    {
        return elResolver instanceof MapELResolver
                || elResolver instanceof ListELResolver
                || elResolver instanceof ArrayELResolver
                || elResolver instanceof BeanELResolver
                || isOptionalOrRecord(elResolver);
    }

    private static boolean isOptionalOrRecord(ELResolver elResolver)
    {
        // jakarta.el 6.0 types; referenced by name so this class still loads under EL 5.
        final String name = elResolver.getClass().getName();
        return "jakarta.el.OptionalELResolver".equals(name) || "jakarta.el.RecordELResolver".equals(name);
    }
}
