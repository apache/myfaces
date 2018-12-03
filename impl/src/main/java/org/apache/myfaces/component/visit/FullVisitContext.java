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
package org.apache.myfaces.component.visit;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import org.apache.myfaces.util.Assert;

/**
 * <p>
 * A VisitContext implementation that is used when performing a full component
 * tree visit.</p>
 *
 * @author Werner Punz, Blake Sullivan (latest modification by $Author$)
 * @version $Rev$ $Date$
 */
public class FullVisitContext extends VisitContext
{

    // The FacesContext for this request
    private final FacesContext _facesContext;

    // Our visit hints
    private final Set<VisitHint> _hints;
    
    /**
     * Creates a FullVisitorContext instance.
     *
     * @param facesContext the FacesContext for the current request
     * @throws NullPointerException if {@code facesContext} is {@code null}
     */
    public FullVisitContext(FacesContext facesContext)
    {
        this(facesContext, null);
    }

    /**
     * Creates a FullVisitorContext instance with the specified hints.
     *
     * @param facesContext the FacesContext for the current request
     * @param hints a the VisitHints for this visit
     * @throws NullPointerException if {@code facesContext} is {@code null}
     * @throws IllegalArgumentException if the phaseId is specified and hints
     * does not contain VisitHint.EXECUTE_LIFECYCLE
     */
    public FullVisitContext(FacesContext facesContext, Set<VisitHint> hints)
    {
        Assert.notNull(facesContext, "facesContext");

        _facesContext = facesContext;

        // Copy and store hints - ensure unmodifiable and non-empty
        EnumSet<VisitHint> hintsEnumSet = ((hints == null) || (hints.isEmpty()))
                ? EnumSet.noneOf(VisitHint.class)
                : EnumSet.copyOf(hints);

        _hints = Collections.unmodifiableSet(hintsEnumSet);
    }

    /**
     * @see VisitContext#getFacesContext VisitContext.getFacesContext()
     */
    @Override
    public FacesContext getFacesContext()
    {
        return _facesContext;
    }

    /**
     * @see VisitContext#getIdsToVisit VisitContext.getIdsToVisit()
     */
    @Override
    public Collection<String> getIdsToVisit()
    {
        // We always visits all ids
        return ALL_IDS;
    }

    /**
     * @see VisitContext#getSubtreeIdsToVisit
     * VisitContext.getSubtreeIdsToVisit()
     */
    @Override
    public Collection<String> getSubtreeIdsToVisit(UIComponent component)
    {
        // Make sure component is a NamingContainer
        if (!(component instanceof NamingContainer))
        {
            throw new IllegalArgumentException("Component is not a NamingContainer: " + component);
        }

        // We always visits all ids
        return ALL_IDS;
    }

    /**
     * @see VisitContext#getHints VisitContext.getHints
     */
    @Override
    public Set<VisitHint> getHints()
    {
        return _hints;
    }

    /**
     * @see VisitContext#invokeVisitCallback VisitContext.invokeVisitCallback()
     */
    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback)
    {
        // Nothing interesting here - just invoke the callback.
        // (PartialVisitContext.invokeVisitCallback() does all of the 
        // interesting work.)
        return callback.visit(this, component);
    }

}
