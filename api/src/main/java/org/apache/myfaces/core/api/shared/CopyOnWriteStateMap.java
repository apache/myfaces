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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A map of per-row state of an iterating component (UIData, UIRepeat), keyed by the container client id of the row,
 * which is copied on its first change after it was saved or restored.
 * <p>
 * Without serialization (server side state saving, or the per-row state that an enclosing UIData or UIRepeat with
 * rowStatePreserved="true" keeps of a nested one) the saved state holds the map itself, not a copy. Such a map must
 * not be changed anymore, otherwise a later restore of the same saved state (back button, second tab, the enclosing
 * row) sees the changes. Copying the map on every save or restore would cost time for each request, also when the
 * component does not change its per-row state at all, so it is copied only when it is changed.
 * <p>
 * The values must not be changed in place, but replaced with {@link #put(String, Object)}: only the map is copied,
 * not the values.
 *
 * @param <V> the type of the per-row state
 */
public final class CopyOnWriteStateMap<V>
{
    private Map<String, V> map = Collections.emptyMap();

    /**
     * If {@link #map} may be referenced by a saved state, or is the immutable empty map, so it must be copied before
     * it is changed.
     */
    private boolean shared = true;

    public V get(String key)
    {
        return map.get(key);
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public void put(String key, V value)
    {
        writableMap().put(key, value);
    }

    public void remove(String key)
    {
        if (shared)
        {
            // no copy if there is nothing to remove, the common case when a row without state is left
            if (!map.containsKey(key))
            {
                return;
            }
            writableMap();
        }
        map.remove(key);
    }

    public void clear()
    {
        if (map.isEmpty())
        {
            return;
        }
        if (shared)
        {
            map = Collections.emptyMap();
        }
        else
        {
            map.clear();
        }
    }

    /**
     * Returns the map to save it. The saved state references it from now on, so it is copied before the next change.
     */
    public Map<String, V> save()
    {
        shared = true;
        return map;
    }

    /**
     * Restores the map from a saved state, without copying it: it is copied before the first change.
     *
     * @param saved the saved map, or null
     */
    @SuppressWarnings("unchecked")
    public void restore(Object saved)
    {
        Map<String, V> savedMap = (Map<String, V>) saved;
        map = savedMap == null || savedMap.isEmpty() ? Collections.emptyMap() : savedMap;
        shared = true;
    }

    private Map<String, V> writableMap()
    {
        if (shared)
        {
            map = new HashMap<>(map);
            shared = false;
        }
        return map;
    }
}
