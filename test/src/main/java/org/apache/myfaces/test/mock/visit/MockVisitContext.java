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

package org.apache.myfaces.test.mock.visit;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>Mock implementation of <code>VisitContext</code>.</p>
 * <p/>
 * $Id$
 *
 * @since 1.0.0
 */
public class MockVisitContext extends VisitContext
{

    private final FacesContext _facesContext;
    private final Set<VisitHint> _hints;

    public MockVisitContext(FacesContext facesContext)
    {
        this(facesContext, null);
    }

    public MockVisitContext(FacesContext facesContext, Set<VisitHint> hints)
    {
        if (facesContext == null)
        {
            throw new NullPointerException();
        }

        _facesContext = facesContext;

        // Copy and store hints - ensure unmodifiable and non-empty
        EnumSet<VisitHint> hintsEnumSet = ((hints == null) || (hints.isEmpty())) ? EnumSet
                .noneOf(VisitHint.class)
                : EnumSet.copyOf(hints);

        _hints = Collections.unmodifiableSet(hintsEnumSet);
    }

    @Override
    public FacesContext getFacesContext()
    {
        return _facesContext;
    }

    @Override
    public Set<VisitHint> getHints()
    {
        return _hints;
    }

    @Override
    public Collection<String> getIdsToVisit()
    {
        return ALL_IDS;
    }

    @Override
    public Collection<String> getSubtreeIdsToVisit(UIComponent component)
    {
        // Make sure component is a NamingContainer
        if (!(component instanceof NamingContainer))
        {
            throw new IllegalArgumentException("Component is not a NamingContainer: " + component);
        }

        return ALL_IDS;
    }

    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback)
    {
        return callback.visit(this, component);
    }
}
