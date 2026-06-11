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

import java.util.Arrays;

import jakarta.faces.component.TransientStateHelper;
import jakarta.faces.context.FacesContext;

/**
 * Lightweight {@link TransientStateHelper} implementation for request-scoped
 * component state.
 *
 * <p>Uses a flat {@code Object[]} with a two-level key comparison: identity
 * ({@code ==}) first for a fast path on enum-constant keys (e.g.
 * {@code valid}, {@code localValueSet}, {@code submittedValue}), then
 * {@code equals()} as a fallback for String or other keys that may be
 * re-instantiated after Java serialization/deserialization (a deserialized
 * String is a new heap object, not the interned literal).
 *
 * <p>Save and restore clone a compact array of only the live pairs, making
 * the round-trip O(n) with minimal allocation — critical for {@code UIRepeat},
 * which saves and restores the transient state of every child component on
 * every row iteration.
 */
public class SimpleTransientStateHelper implements TransientStateHelper
{
    // Flat interleaved [key0, val0, key1, val1, ...].
    // Keys are compared with == first (fast path for enum constants), then equals() as a
    // fallback so that String keys survive Java serialization/deserialization correctly.
    // package-private so UIComponent can transfer _data when upgrading to _DeltaStateHelper.
    private Object[] _data;
    private int _size;

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Override
    public Object getTransient(Object key)
    {
        return getTransient(key, null);
    }

    /**
     * Scans directly so a stored {@code null} value is returned as-is, not
     * conflated with "key absent" the way a {@code v != null} delegation would.
     */
    @Override
    public Object getTransient(Object key, Object defaultValue)
    {
        Object[] d = _data;
        if (d == null)
        {
            return defaultValue;
        }
        int n = _size << 1;
        for (int i = 0; i < n; i += 2)
        {
            if (d[i] == key || (key != null && key.equals(d[i])))
            {
                return d[i + 1]; // returns even when value is null
            }
        }
        return defaultValue;
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    @Override
    public Object putTransient(Object key, Object value)
    {
        Object[] d = _data;
        int n = _size << 1;

        // update existing entry if present
        if (d != null)
        {
            for (int i = 0; i < n; i += 2)
            {
                if (d[i] == key || (key != null && key.equals(d[i])))
                {
                    Object old = d[i + 1];
                    d[i + 1] = value;
                    return old;
                }
            }
        }

        // new entry — grow if needed, doubling capacity to avoid O(n) reallocs.
        // Initial size 6 (3 pairs) matches the common component load; first grow
        // doubles to 12 to comfortably fit the occasional 5-key component.
        int needed = n + 2;
        if (d == null || needed > d.length)
        {
            int newCap = Math.max(needed, d != null ? d.length * 2 : 6);
            d = Arrays.copyOf(d != null ? d : new Object[0], newCap);
            _data = d;
        }
        d[n]     = key;
        d[n + 1] = value;
        _size++;
        return null;
    }

    public void removeTransient(Object key)
    {
        Object[] d = _data;
        if (d == null)
        {
            return;
        }
        int n = _size << 1;
        for (int i = 0; i < n; i += 2)
        {
            if (d[i] == key || (key != null && key.equals(d[i])))
            {
                // swap-with-last to avoid shifting — safe for flat arrays
                // (unlike open-addressing tables which need backward-shift deletion)
                int last = n - 2;
                if (i != last)
                {
                    d[i]     = d[last];
                    d[i + 1] = d[last + 1];
                }
                d[last]     = null;
                d[last + 1] = null;
                _size--;
                return;
            }
        }
    }

    public void setOrRemoveTransient(Object key, Object value)
    {
        if (value != null)
        {
            putTransient(key, value);
        }
        else
        {
            removeTransient(key);
        }
    }

    // ── Save / restore ────────────────────────────────────────────────────────

    /**
     * Returns a compact snapshot of only the live key-value pairs.
     * The snapshot is independent of the live array — a subsequent
     * {@code putTransient} that grows {@code _data} will not affect it.
     */
    @Override
    public Object saveTransientState(FacesContext context)
    {
        if (_size == 0)
        {
            return null;
        }
        return Arrays.copyOf(_data, _size << 1);
    }

    @Override
    public void restoreTransientState(FacesContext context, Object state)
    {
        if (state == null)
        {
            _data = null;
            _size = 0;
        }
        else
        {
            // Assign the snapshot directly — no clone needed here because
            // saveTransientState already returns a trimmed copy (Arrays.copyOf),
            // not the live _data reference.  As a result:
            //   • mutations to the previous live _data do not touch this snapshot
            //   • the first putTransient with a NEW key always triggers growth
            //     (trimmed capacity == _size * 2, no slack), reallocating _data
            //     and leaving the snapshot object unaffected
            //   • in-place updates (putTransient on an existing key) do mutate the
            //     snapshot array, but UIRepeat always overwrites its stored reference
            //     with the next saveTransientState() call before reusing it
            _data = (Object[]) state;
            _size = _data.length >> 1;
        }
    }

    public void resetTransientState()
    {
        _data = null;
        _size = 0;
    }
}