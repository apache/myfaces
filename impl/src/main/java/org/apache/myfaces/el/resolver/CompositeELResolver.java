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
package org.apache.myfaces.el.resolver;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeELResolver extends jakarta.el.CompositeELResolver
{
    private Collection<ELResolver> _elResolvers;

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base)
    {
        Collection<ELResolver> resolvers = _elResolvers;
        if (resolvers == null)
        {
            resolvers = Collections.emptyList();
        }
        
        return new CompositeIterator(context, base, resolvers.iterator());
    }

    /**
     * @param elResolver
     */
    @Override
    public final synchronized void add(final ELResolver elResolver)
    {
        super.add(elResolver);

        if (_elResolvers == null)
        {
            _elResolvers = new ArrayList<>();
        }

        _elResolvers.add(elResolver);
    }

    private static class CompositeIterator implements Iterator<FeatureDescriptor>
    {
        private final ELContext _context;
        private final Object _base;
        private final Iterator<ELResolver> _elResolvers;

        private FeatureDescriptor _nextFD;
        private Iterator<FeatureDescriptor> _currentFDIter;

        public CompositeIterator(final ELContext context, final Object base, final Iterator<ELResolver> elResolvers)
        {
            _context = context;
            _base = base;
            _elResolvers = elResolvers;
        }

        @Override
        public boolean hasNext()
        {
            if (_nextFD != null)
            {
                return true;
            }
            if (_currentFDIter != null)
            {
                while (_nextFD == null && _currentFDIter.hasNext())
                {
                    _nextFD = _currentFDIter.next();
                }
            }
            if (_nextFD == null)
            {
                if (_elResolvers.hasNext())
                {
                    _currentFDIter = _elResolvers.next().getFeatureDescriptors(_context, _base);
                }
                else
                {
                    return false;
                }
            }
            return hasNext();
        }

        @Override
        public FeatureDescriptor next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            FeatureDescriptor next = _nextFD;
            _nextFD = null;
            return next;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }
}
