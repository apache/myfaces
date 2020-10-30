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

import java.util.Iterator;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.LifecycleFactory;

/**
 * The class will initialize the resolver for JSP
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class ResolverForJSPInitializer implements PhaseListener
{
    private final ELResolverBuilder _resolverBuilder;
    private volatile boolean initialized;
    private final jakarta.el.CompositeELResolver _resolverForJSP;

    public ResolverForJSPInitializer(final ELResolverBuilder resolverBuilder,
                                     final jakarta.el.CompositeELResolver resolverForJSP)
    {
        _resolverBuilder = resolverBuilder;
        _resolverForJSP = resolverForJSP;
    }

    @Override
    public void beforePhase(final PhaseEvent event)
    {
        if (!initialized)
        {
            synchronized(this)
            {
                if (!initialized)
                {
                    FacesContext facesContext = event.getFacesContext();
                    facesContext = (facesContext == null) ? FacesContext.getCurrentInstance() : facesContext;
                    _resolverBuilder.build(facesContext, _resolverForJSP);

                    LifecycleFactory factory = (LifecycleFactory) 
                            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
                    for (Iterator<String> iter = factory.getLifecycleIds(); iter.hasNext();)
                    {
                        factory.getLifecycle(iter.next()).removePhaseListener(this);
                    }
                    
                    initialized = true;
                }
            }
        }
    }

    @Override
    public void afterPhase(final PhaseEvent event)
    {
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

}
