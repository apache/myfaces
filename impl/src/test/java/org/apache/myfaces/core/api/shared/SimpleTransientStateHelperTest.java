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
 */
package org.apache.myfaces.core.api.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTransientStateHelperTest
{
    enum Key { A, B, C, D, E, F, G }

    private SimpleTransientStateHelper helper;

    @BeforeEach
    void setUp()
    {
        helper = new SimpleTransientStateHelper();
    }

    // ── getTransient ──────────────────────────────────────────────────────────

    @Test
    void getTransient_missingKey_returnsNull()
    {
        assertNull(helper.getTransient(Key.A));
    }

    @Test
    void getTransient_missingKey_returnsDefaultValue()
    {
        assertEquals("default", helper.getTransient(Key.A, "default"));
    }

    @Test
    void getTransient_storedNull_returnsNull_notDefault()
    {
        // FIX 1: stored null must not be conflated with "key absent"
        helper.putTransient(Key.A, null);
        // getTransient(key) should return null (stored value)
        assertNull(helper.getTransient(Key.A));
        // getTransient(key, default) should also return null, not "default"
        assertNull(helper.getTransient(Key.A, "default"),
                "stored null should be returned as-is, not replaced by defaultValue");
    }

    // ── putTransient ──────────────────────────────────────────────────────────

    @Test
    void putTransient_newKey_returnsNull()
    {
        assertNull(helper.putTransient(Key.A, "value"));
    }

    @Test
    void putTransient_existingKey_returnsOldValue()
    {
        helper.putTransient(Key.A, "first");
        assertEquals("first", helper.putTransient(Key.A, "second"));
    }

    @Test
    void putTransient_existingKey_updatesValue()
    {
        helper.putTransient(Key.A, "first");
        helper.putTransient(Key.A, "second");
        assertEquals("second", helper.getTransient(Key.A));
    }

    @Test
    void putTransient_multipleKeys_allRetrievable()
    {
        for (Key k : Key.values()) helper.putTransient(k, k.name());
        for (Key k : Key.values()) assertEquals(k.name(), helper.getTransient(k));
    }

    @Test
    void putTransient_beyondInitialCapacity_growsCorrectly()
    {
        // initial capacity fits 3 pairs; push well beyond to exercise growth
        for (int i = 0; i < Key.values().length; i++)
        {
            helper.putTransient(Key.values()[i], i);
        }
        for (int i = 0; i < Key.values().length; i++)
        {
            assertEquals(i, helper.getTransient(Key.values()[i]));
        }
    }

    // ── removeTransient ───────────────────────────────────────────────────────

    @Test
    void removeTransient_existingKey_removesEntry()
    {
        helper.putTransient(Key.A, "value");
        helper.removeTransient(Key.A);
        assertNull(helper.getTransient(Key.A));
    }

    @Test
    void removeTransient_missingKey_noOp()
    {
        assertDoesNotThrow(() -> helper.removeTransient(Key.A));
    }

    @Test
    void removeTransient_middleEntry_otherEntriesIntact()
    {
        helper.putTransient(Key.A, "a");
        helper.putTransient(Key.B, "b");
        helper.putTransient(Key.C, "c");
        helper.removeTransient(Key.B);
        assertEquals("a", helper.getTransient(Key.A));
        assertNull(helper.getTransient(Key.B));
        assertEquals("c", helper.getTransient(Key.C));
    }

    @Test
    void removeTransient_lastEntry_otherEntriesIntact()
    {
        helper.putTransient(Key.A, "a");
        helper.putTransient(Key.B, "b");
        helper.removeTransient(Key.B);
        assertEquals("a", helper.getTransient(Key.A));
        assertNull(helper.getTransient(Key.B));
    }

    @Test
    void removeTransient_thenPut_reusesSizeCorrectly()
    {
        helper.putTransient(Key.A, "a");
        helper.putTransient(Key.B, "b");
        helper.removeTransient(Key.A);
        helper.putTransient(Key.C, "c");
        assertNull(helper.getTransient(Key.A));
        assertEquals("b", helper.getTransient(Key.B));
        assertEquals("c", helper.getTransient(Key.C));
    }

    // ── setOrRemoveTransient ──────────────────────────────────────────────────

    @Test
    void setOrRemoveTransient_nonNullValue_putsEntry()
    {
        helper.setOrRemoveTransient(Key.A, "value");
        assertEquals("value", helper.getTransient(Key.A));
    }

    @Test
    void setOrRemoveTransient_nullValue_removesEntry()
    {
        helper.putTransient(Key.A, "value");
        helper.setOrRemoveTransient(Key.A, null);
        assertNull(helper.getTransient(Key.A));
    }

    @Test
    void setOrRemoveTransient_nullOnMissingKey_noOp()
    {
        assertDoesNotThrow(() -> helper.setOrRemoveTransient(Key.A, null));
        assertNull(helper.getTransient(Key.A));
    }

    // ── reset ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsAllEntries()
    {
        helper.putTransient(Key.A, "a");
        helper.putTransient(Key.B, "b");
        helper.resetTransientState();
        assertNull(helper.getTransient(Key.A));
        assertNull(helper.getTransient(Key.B));
    }

    @Test
    void reset_thenPut_worksCorrectly()
    {
        helper.putTransient(Key.A, "a");
        helper.resetTransientState();
        helper.putTransient(Key.B, "b");
        assertNull(helper.getTransient(Key.A));
        assertEquals("b", helper.getTransient(Key.B));
    }

    // ── saveTransientState / restoreTransientState ────────────────────────────

    @Test
    void save_emptyHelper_returnsNull()
    {
        assertNull(helper.saveTransientState(null));
    }

    @Test
    void saveRestore_roundtrip_preservesAllEntries()
    {
        helper.putTransient(Key.A, "a");
        helper.putTransient(Key.B, "b");
        helper.putTransient(Key.C, "c");

        Object snapshot = helper.saveTransientState(null);
        helper.resetTransientState();
        helper.restoreTransientState(null, snapshot);

        assertEquals("a", helper.getTransient(Key.A));
        assertEquals("b", helper.getTransient(Key.B));
        assertEquals("c", helper.getTransient(Key.C));
    }

    @Test
    void restore_null_clearsHelper()
    {
        helper.putTransient(Key.A, "a");
        helper.restoreTransientState(null, null);
        assertNull(helper.getTransient(Key.A));
    }

    @Test
    void save_snapshotIsIndependentOfLiveData()
    {
        // FIX 2: putTransient after save must not corrupt the snapshot
        helper.putTransient(Key.A, "a");
        Object snapshot = helper.saveTransientState(null);

        // mutate live state after saving
        helper.putTransient(Key.A, "mutated");
        helper.putTransient(Key.B, "b");
        helper.putTransient(Key.C, "c");
        helper.putTransient(Key.D, "d"); // may trigger grow, reallocating _data

        // restore from snapshot — must reflect state at save time
        helper.restoreTransientState(null, snapshot);
        assertEquals("a", helper.getTransient(Key.A),
                "snapshot must not be affected by mutations after save");
        assertNull(helper.getTransient(Key.B));
    }

    @Test
    void restore_snapshotIsIndependentOfSubsequentPuts()
    {
        // FIX 2: restoreTransientState must clone the snapshot so that
        // a subsequent grow of _data does not corrupt the saved snapshot
        helper.putTransient(Key.A, "a");
        Object snapshot = helper.saveTransientState(null);

        helper.restoreTransientState(null, snapshot);

        // push enough puts to force a grow of _data
        helper.putTransient(Key.B, "b");
        helper.putTransient(Key.C, "c");
        helper.putTransient(Key.D, "d");
        helper.putTransient(Key.E, "e");

        // restore again from the same snapshot
        helper.restoreTransientState(null, snapshot);
        assertEquals("a", helper.getTransient(Key.A),
                "snapshot must survive a grow that follows restoreTransientState");
        assertNull(helper.getTransient(Key.B));
    }

    @Test
    void growth_doesNotLoseEntries()
    {
        // FIX 3: doubling growth must not drop entries across multiple resizes
        String[] expected = new String[Key.values().length];
        for (int i = 0; i < Key.values().length; i++)
        {
            expected[i] = "val" + i;
            helper.putTransient(Key.values()[i], expected[i]);
        }
        for (int i = 0; i < Key.values().length; i++)
        {
            assertEquals(expected[i], helper.getTransient(Key.values()[i]),
                    "entry lost after growth for key " + Key.values()[i]);
        }
    }
}