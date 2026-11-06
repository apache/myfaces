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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

/**
 * The live items of one c:forEach, shared by all its rows (MYFACES-4775).
 *
 * <p>
 * The items expression is evaluated once per lifecycle phase and its elements are held as array,
 * so a row reads its element in O(1) - also for a Set - no matter how often it accesses its var.
 * The evaluated items are cached in the FacesContext attributes and dropped on every phase change,
 * so a change of the items within a phase (other than through the var of a row) is seen only in the
 * next phase.
 * </p>
 *
 * <p>
 * The cache is keyed by this instance, which belongs to one build of the c:forEach, and not by the
 * items expression, because a cached items expression can be shared by several builds (e.g.
 * <code>#{cc.attrs.items}</code> of two composite component instances) and then resolves to
 * different items.
 * </p>
 */
final class IterationItems implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final String CACHE_KEY = IterationItems.class.getName();
    private static final Object[] EMPTY = new Object[0];

    private final ValueExpression items;

    /**
     * The rows of this build of the c:forEach.
     */
    private final List<IndexedValueExpression> rows = new ArrayList<>();

    IterationItems(ValueExpression items)
    {
        this.items = items;
    }

    /**
     * Creates the items with the value the c:forEach just evaluated, so it is not evaluated again
     * in the current phase.
     */
    IterationItems(ValueExpression items, FacesContext facesContext, Object value)
    {
        this.items = items;
        if (facesContext != null)
        {
            getCache(facesContext).put(this, new Snapshot(this, value));
        }
    }

    ValueExpression getItems()
    {
        return items;
    }

    /**
     * Adds a row and returns its ordinal.
     */
    int addRow(IndexedValueExpression row)
    {
        rows.add(row);
        return rows.size() - 1;
    }

    Snapshot get(ELContext context)
    {
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        if (facesContext == null)
        {
            return new Snapshot(this, items.getValue(context));
        }

        Map<IterationItems, Snapshot> cache = getCache(facesContext);
        Snapshot snapshot = cache.get(this);
        if (snapshot == null)
        {
            snapshot = new Snapshot(this, items.getValue(context));
            cache.put(this, snapshot);
        }
        return snapshot;
    }

    private static Map<IterationItems, Snapshot> getCache(FacesContext facesContext)
    {
        Map<Object, Object> attributes = facesContext.getAttributes();
        PhaseId phase = facesContext.getCurrentPhaseId();
        Cache cache = (Cache) attributes.get(CACHE_KEY);
        if (cache == null || cache.phase != phase)
        {
            cache = new Cache(phase);
            attributes.put(CACHE_KEY, cache);
        }
        return cache.snapshots;
    }

    private static final class Cache
    {
        private final PhaseId phase;
        private final Map<IterationItems, Snapshot> snapshots = new IdentityHashMap<>();

        private Cache(PhaseId phase)
        {
            this.phase = phase;
        }
    }

    /**
     * The items as evaluated in one phase.
     */
    static final class Snapshot
    {
        private static final int UNRESOLVED = -2;

        /**
         * The evaluated items, the target of writes.
         */
        final Object value;

        /**
         * The elements of the items, indexed by their iteration position.
         */
        final Object[] elements;

        private final IterationItems owner;

        /**
         * The located position of each row in {@link #elements} by its ordinal, so a row is located
         * at most once per phase. Lives only as long as this snapshot, so a row never keeps the items
         * of an earlier request.
         */
        private int[] positions;

        /**
         * The first position of each element, built on the first row that is not at its rendered
         * position, so locating all rows stays O(n).
         */
        private Map<Object, Integer> firstPositions;

        /**
         * The number of rows rendered with each element, built on the first row whose element is
         * not found.
         */
        private Map<Object, Integer> renderedCounts;

        private Snapshot(IterationItems owner, Object value)
        {
            this.owner = owner;
            this.value = value;
            this.elements = toArray(value);
        }

        int indexOf(IndexedValueExpression row)
        {
            int ordinal = row.getOrdinal();
            if (positions == null || ordinal >= positions.length)
            {
                int oldLength = positions == null ? 0 : positions.length;
                positions = positions == null
                        ? new int[owner.rows.size()]
                        : Arrays.copyOf(positions, owner.rows.size());
                Arrays.fill(positions, oldLength, positions.length, UNRESOLVED);
            }
            int position = positions[ordinal];
            if (position == UNRESOLVED)
            {
                position = row.locate(this);
                positions[ordinal] = position;
            }
            return position;
        }

        /**
         * The first position of an element equal to the given one, or -1.
         */
        int firstPositionOf(Object element)
        {
            if (firstPositions == null)
            {
                firstPositions = new HashMap<>(elements.length * 2);
                for (int k = 0; k < elements.length; k++)
                {
                    firstPositions.putIfAbsent(elements[k], k);
                }
            }
            Integer position = firstPositions.get(element);
            return position == null ? -1 : position;
        }

        /**
         * Whether the given element is the one another row than the given one was rendered with.
         */
        boolean isRenderedByOtherRow(IndexedValueExpression row, Object element)
        {
            if (renderedCounts == null)
            {
                renderedCounts = new HashMap<>(owner.rows.size() * 2);
                for (int k = 0; k < owner.rows.size(); k++)
                {
                    Object rendered = owner.rows.get(k).getRendered();
                    if (rendered != null)
                    {
                        renderedCounts.merge(rendered, 1, Integer::sum);
                    }
                }
            }
            Integer count = renderedCounts.get(element);
            return count != null && count - (row.matches(element) ? 1 : 0) > 0;
        }

        /**
         * An element or a rendered element changed through the var of a row.
         */
        void changed()
        {
            firstPositions = null;
            renderedCounts = null;
        }


        private static Object[] toArray(Object value)
        {
            if (value instanceof Object[] array)
            {
                return array;
            }
            if (value instanceof Collection<?> collection)
            {
                return collection.toArray();
            }
            if (value != null && value.getClass().isArray())
            {
                Object[] array = new Object[Array.getLength(value)];
                for (int i = 0; i < array.length; i++)
                {
                    array[i] = Array.get(value, i);
                }
                return array;
            }
            return EMPTY;
        }
    }
}
