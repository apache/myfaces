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
package org.apache.myfaces.view.facelets.el;

import java.lang.reflect.Field;

import jakarta.el.VariableMapper;
import jakarta.faces.FacesWrapper;

/**
 * Peels Facelets-time {@link VariableMapper} overlays that are <em>not</em> {@link VariableMapperBase} subclasses.
 * <p>
 * If the overlay implements {@link FacesWrapper} and {@link FacesWrapper#getWrapped()} is a
 * {@link VariableMapper}, that delegate is used (no reflection). Otherwise a {@code wrapped} field of type
 * {@link VariableMapper} is read reflectively for common third-party delegators. Those names must not apply while
 * applying composite-component consumer markup ({@code cc:insertChildren} / {@code cc:insertFacet}), or EL in the
 * consumer body can be shadowed (MYFACES-4589). MyFaces mappers extend {@link VariableMapperBase} and are left
 * unchanged.
 * </p>
 */
public final class VariableMapperOverlaySupport
{

    private VariableMapperOverlaySupport()
    {
    }

    /**
     * Removes consecutive non-{@link VariableMapperBase} wrappers, preferring {@link FacesWrapper#getWrapped()} when
     * applicable, otherwise reading a {@code wrapped} {@link VariableMapper} field reflectively.
     *
     * @param top current mapper (may be {@code null})
     * @return delegate chain with those overlays removed, or {@code top}
     */
    public static VariableMapper unwrapExternalWrappedDelegateChain(VariableMapper top)
    {
        VariableMapper m = top;
        while (m != null && !(m instanceof VariableMapperBase))
        {
            VariableMapper inner = readInnerDelegate(m);
            if (inner == null || inner == m)
            {
                break;
            }
            m = inner;
        }
        return m;
    }

    private static VariableMapper readInnerDelegate(VariableMapper m)
    {
        if (m instanceof FacesWrapper<?> wrapper)
        {
            Object w = wrapper.getWrapped();
            if (w instanceof VariableMapper vm)
            {
                return vm;
            }
        }
        return readWrappedVariableMapperReflective(m);
    }

    private static VariableMapper readWrappedVariableMapperReflective(VariableMapper m)
    {
        try
        {
            Field f = m.getClass().getDeclaredField("wrapped");
            if (!VariableMapper.class.isAssignableFrom(f.getType()))
            {
                return null;
            }
            f.setAccessible(true);
            Object v = f.get(m);
            return v instanceof VariableMapper vm ? vm : null;
        }
        catch (ReflectiveOperationException | SecurityException e)
        {
            return null;
        }
    }
}
