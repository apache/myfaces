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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CopyOnWriteStateMapTest
{
    @Test
    void newMap_isEmpty()
    {
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        assertTrue(rows.isEmpty());
        assertNull(rows.get("t:0"));
        assertTrue(rows.save().isEmpty());
    }

    @Test
    void restore_doesNotCopy()
    {
        Map<String, String> saved = new HashMap<>(Map.of("t:0", "a"));
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(saved);
        assertEquals("a", rows.get("t:0"));
        assertSame(saved, rows.save(), "an unchanged restored map is saved again as it is");
    }

    @Test
    void putAfterRestore_doesNotChangeSavedMap()
    {
        Map<String, String> saved = new HashMap<>(Map.of("t:0", "a"));
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(saved);
        rows.put("t:1", "b");
        rows.put("t:0", "c");
        assertEquals(Map.of("t:0", "a"), saved);
        assertEquals("c", rows.get("t:0"));
        assertEquals("b", rows.get("t:1"));
    }

    @Test
    void removeAfterRestore_doesNotChangeSavedMap()
    {
        Map<String, String> saved = new HashMap<>(Map.of("t:0", "a"));
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(saved);
        rows.remove("t:0");
        assertEquals(Map.of("t:0", "a"), saved);
        assertNull(rows.get("t:0"));
    }

    @Test
    void removeOfMissingKey_doesNotCopy()
    {
        Map<String, String> saved = new HashMap<>(Map.of("t:0", "a"));
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(saved);
        rows.remove("t:1");
        assertSame(saved, rows.save());
    }

    @Test
    void clearAfterRestore_doesNotChangeSavedMap()
    {
        Map<String, String> saved = new HashMap<>(Map.of("t:0", "a"));
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(saved);
        rows.clear();
        assertTrue(rows.isEmpty());
        assertEquals(Map.of("t:0", "a"), saved);
    }

    @Test
    void changeAfterSave_doesNotChangeSavedMap()
    {
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.put("t:0", "a");
        Map<String, String> saved = rows.save();
        rows.put("t:0", "b");
        rows.put("t:1", "c");
        assertEquals(Map.of("t:0", "a"), saved);

        Map<String, String> savedAgain = rows.save();
        rows.clear();
        assertEquals(Map.of("t:0", "b", "t:1", "c"), savedAgain);
        assertTrue(rows.isEmpty());
    }

    @Test
    void changesAfterFirstCopy_doNotCopyAgain()
    {
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.restore(new HashMap<>(Map.of("t:0", "a")));
        rows.put("t:1", "b");
        Map<String, String> copy = rows.save();
        // save() hands out the copy itself
        assertEquals(Map.of("t:0", "a", "t:1", "b"), copy);
        assertSame(copy, rows.save());
    }

    @Test
    void restoreOfNullOrEmpty_isEmpty()
    {
        CopyOnWriteStateMap<String> rows = new CopyOnWriteStateMap<>();
        rows.put("t:0", "a");
        rows.restore(null);
        assertTrue(rows.isEmpty());
        rows.put("t:0", "a");
        rows.restore(new HashMap<>());
        assertTrue(rows.isEmpty());
        rows.put("t:1", "b");
        assertEquals("b", rows.get("t:1"));
    }
}
