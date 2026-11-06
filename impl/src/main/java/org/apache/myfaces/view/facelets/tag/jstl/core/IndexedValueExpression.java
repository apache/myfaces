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
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;

/**
 * Binds the var of a c:forEach row to the live element of the items (a List, array or Collection),
 * never to a copy saved with the view state (MYFACES-4775).
 *
 * <p>
 * The row remembers the element it was rendered with and looks it up in the live items:
 * </p>
 * <ol>
 * <li>the element at the rendered position, if it equals the rendered element,</li>
 * <li>otherwise the first element that equals the rendered element, so the row follows its element
 * when items were added or removed in front of it,</li>
 * <li>otherwise, if the rendered position is gone or now holds the element another row of the loop
 * was rendered with, the element vanished: the row reads the rendered element and writes go
 * nowhere,</li>
 * <li>otherwise the element at the rendered position, like ui:repeat. This covers elements whose
 * <code>equals</code> does not hold for a copy saved with the view state (identity equality, or
 * equality over content with identity equality like a List of such elements).</li>
 * </ol>
 *
 * <p>
 * The var is writable for a List or an array.
 * </p>
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class IndexedValueExpression extends ValueExpression
{
    private static final long serialVersionUID = 1L;

    static final int VANISHED = -1;

    private final IterationItems items;
    private final int i;
    private final int ordinal;

    /**
     * The element this row was rendered with, or <code>null</code> if unknown or not serializable.
     */
    private Object rendered;

    public IndexedValueExpression(ValueExpression items, int i)
    {
        this(new IterationItems(items), i, null);
    }

    IndexedValueExpression(IterationItems items, int i, Object rendered)
    {
        this.items = items;
        this.i = i;
        this.rendered = rendered instanceof Serializable ? rendered : null;
        this.ordinal = items.addRow(this);
    }

    int getOrdinal()
    {
        return ordinal;
    }

    Object getRendered()
    {
        return rendered;
    }

    @Override
    public Object getValue(ELContext context)
    {
        IterationItems.Snapshot snapshot = items.get(context);
        int index = snapshot.indexOf(this);
        return index == VANISHED ? rendered : snapshot.elements[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(ELContext context, Object value)
    {
        IterationItems.Snapshot snapshot = items.get(context);
        int index = snapshot.indexOf(this);
        if (index == VANISHED)
        {
            // the element is gone from the items, its value goes nowhere
            return;
        }
        if (snapshot.value instanceof List<?> list)
        {
            try
            {
                ((List<Object>) list).set(index, value);
            }
            catch (UnsupportedOperationException e)
            {
                throw new PropertyNotWritableException(getExpressionString(), e);
            }
        }
        else if (snapshot.value.getClass().isArray())
        {
            Array.set(snapshot.value, index, value);
        }
        else
        {
            throw new PropertyNotWritableException(getExpressionString());
        }
        snapshot.elements[index] = value;
        rendered = value instanceof Serializable ? value : null;
        snapshot.changed();
    }

    /**
     * The position of the element of this row in the live elements of the given snapshot, or
     * {@link #VANISHED}. O(1), the lookups of the snapshot are built once per phase on the first
     * row that is not at its rendered position.
     */
    int locate(IterationItems.Snapshot snapshot)
    {
        Object[] elements = snapshot.elements;
        if (rendered == null)
        {
            return i < elements.length ? i : VANISHED;
        }
        if (i < elements.length && matches(elements[i]))
        {
            return i;
        }
        int position = snapshot.firstPositionOf(rendered);
        if (position >= 0)
        {
            return position;
        }
        if (i >= elements.length || snapshot.isRenderedByOtherRow(this, elements[i]))
        {
            return VANISHED;
        }
        return i;
    }

    boolean matches(Object element)
    {
        return rendered != null && (element == rendered || (element != null && rendered.equals(element)));
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        Object value = items.get(context).value;
        return !(value instanceof List || (value != null && value.getClass().isArray()));
    }

    @Override
    public Class getType(ELContext context)
    {
        Object value = items.get(context).value;
        return value != null && value.getClass().isArray() ? value.getClass().getComponentType() : Object.class;
    }

    @Override
    public Class getExpectedType()
    {
        return Object.class;
    }

    @Override
    public String getExpressionString()
    {
        return items.getItems().getExpressionString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return items.getItems().equals(obj);
    }

    @Override
    public int hashCode()
    {
        return items.getItems().hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return false;
    }
}
