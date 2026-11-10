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
package jakarta.faces.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.faces.event.PhaseId;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * MYFACES-4776: a Serializable collection whose class cannot be instantiated, such as List.of() or
 * Collections.unmodifiableList(), is saved whole by {@link UIComponentBase#saveAttachedState}, the same as any other
 * Serializable object, so {@link UIComponentBase#restoreAttachedState} restores it with its exact type instead of
 * failing with "Could not restore StateHolder ... (missing no-args constructor?)".
 * <p>
 * Each case is restored without serialization, as with server side state saving, and after serialization, as with
 * client side state saving.
 */
public class UIComponentBaseRestoreCollectionTest extends AbstractFacesTestCase
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
    }

    @Test
    public void testListOf() throws Exception
    {
        assertRestoredWithExactType(List.of("b", "a", "b"));
    }

    @Test
    public void testListCopyOf() throws Exception
    {
        assertRestoredWithExactType(List.copyOf(Arrays.asList("b", "a", "b")));
    }

    @Test
    public void testArraysAsList() throws Exception
    {
        assertRestoredWithExactType(Arrays.asList("b", "a", "b"));
    }

    @Test
    public void testUnmodifiableList() throws Exception
    {
        assertRestoredWithExactType(Collections.unmodifiableList(new LinkedList<>(List.of("b", "a", "b"))));
    }

    @Test
    public void testSingletonList() throws Exception
    {
        assertRestoredWithExactType(Collections.singletonList("b"));
    }

    @Test
    public void testEmptyList() throws Exception
    {
        assertRestoredWithExactType(Collections.emptyList());
    }

    @Test
    public void testSetOf() throws Exception
    {
        assertRestoredWithExactType(Set.of("a", "b", "c"));
    }

    @Test
    public void testUnmodifiableSetKeepsOrder() throws Exception
    {
        Set<String> set = Collections.unmodifiableSet(new LinkedHashSet<>(List.of("c", "a", "b")));
        assertSavedWhole(set);
        for (Object restored : restore(set))
        {
            Assertions.assertEquals(set.getClass(), restored.getClass());
            Assertions.assertEquals(List.of("c", "a", "b"), List.copyOf((Set<?>) restored));
        }
    }

    @Test
    public void testQueueWithoutNoArgConstructor() throws Exception
    {
        Collection<String> queue = Collections.asLifoQueue(new ArrayDeque<>());
        queue.add("a");
        queue.add("b");
        assertSavedWhole(queue);
        for (Object restored : restore(queue))
        {
            Assertions.assertEquals(queue.getClass(), restored.getClass());
            Assertions.assertEquals(List.copyOf(queue), List.copyOf((Collection<?>) restored));
        }
    }

    /**
     * A collection with an accessible zero-argument constructor is still saved item by item and restored into a new
     * instance of its class.
     */
    @Test
    public void testCollectionWithNoArgConstructorIsRestoredIntoNewInstance() throws Exception
    {
        LinkedList<String> linkedList = new LinkedList<>(List.of("b", "a", "b"));
        assertSavedItemByItem(linkedList);
        for (Object restored : restore(linkedList))
        {
            Assertions.assertNotSame(linkedList, restored);
            Assertions.assertEquals(LinkedList.class, restored.getClass());
            Assertions.assertEquals(linkedList, restored);
        }

        TreeSet<String> treeSet = new TreeSet<>(List.of("c", "a", "b"));
        assertSavedItemByItem(treeSet);
        for (Object restored : restore(treeSet))
        {
            Assertions.assertEquals(TreeSet.class, restored.getClass());
            Assertions.assertEquals(treeSet, restored);
        }
    }

    /**
     * A zero-argument constructor which restoreAttachedState() cannot access, here a private one, counts as no
     * zero-argument constructor, the same as for clazz.newInstance() in restoreAttachedState().
     */
    @Test
    public void testPrivateNoArgConstructorIsSavedWhole() throws Exception
    {
        PrivateConstructorList list = PrivateConstructorList.of("b", "a");
        assertSavedWhole(list);
        assertRestoredWithExactType(list);
    }

    /**
     * A package-private zero-argument constructor in the package of UIComponentBase is accessible, so the collection
     * is still saved item by item and restored into a new instance of its class.
     */
    @Test
    public void testAccessiblePackagePrivateNoArgConstructorIsSavedItemByItem() throws Exception
    {
        PackagePrivateConstructorList list = new PackagePrivateConstructorList();
        list.add("b");
        list.add("a");
        assertSavedItemByItem(list);
        for (Object restored : restore(list))
        {
            Assertions.assertNotSame(list, restored);
            Assertions.assertEquals(PackagePrivateConstructorList.class, restored.getClass());
            Assertions.assertEquals(list, restored);
        }
    }

    /**
     * A collection saved whole is serialized as it is, so its items must be Serializable: otherwise serializing the
     * state fails, while restoring it without serialization works.
     */
    @Test
    public void testNotSerializableItemFailsOnlyWhenSerialized() throws Exception
    {
        Object item = new Object();
        List<Object> list = List.of(item);
        Object state = UIComponentBase.saveAttachedState(facesContext, list);
        Assertions.assertSame(list, state);
        Assertions.assertSame(item, ((List<?>) UIComponentBase.restoreAttachedState(facesContext, state)).get(0));
        Assertions.assertThrows(NotSerializableException.class, () -> serialize(state));
    }

    /**
     * A collection whose class cannot be instantiated and which is not Serializable either still cannot be restored.
     */
    @Test
    public void testNotSerializableCollectionWithoutNoArgConstructorFails()
    {
        Map<String, Integer> map = new HashMap<>(Map.of("a", 1));
        Object state = UIComponentBase.saveAttachedState(facesContext, map.keySet());
        Assertions.assertThrows(RuntimeException.class, () -> UIComponentBase.restoreAttachedState(facesContext,
                state));
    }

    /**
     * The reproducer of MYFACES-4776: Java code sets an unmodifiable list as the value of a UIData, which must survive
     * a state saving round trip of the component, both as full state and as delta state, and both without
     * serialization (server side state saving) and with it (client side state saving).
     */
    @Test
    public void testUIDataValueSetToListOf() throws Exception
    {
        List<String> value = List.of("b", "a", "b");
        for (boolean delta : new boolean[] {false, true})
        {
            for (boolean serialized : new boolean[] {false, true})
            {
                UIData table = new UIData();
                table.setId("table");
                if (delta)
                {
                    table.markInitialState();
                }
                table.setValue(value);

                Object state = table.saveState(facesContext);
                if (serialized)
                {
                    state = serialize(state);
                }

                UIData restored = new UIData();
                restored.setId("table");
                if (delta)
                {
                    restored.markInitialState();
                }
                restored.restoreState(facesContext, state);

                String message = "delta=" + delta + ", serialized=" + serialized;
                Assertions.assertEquals(value.getClass(), restored.getValue().getClass(), message);
                Assertions.assertEquals(value, restored.getValue(), message);
            }
        }
    }

    private void assertSavedWhole(Collection<?> collection)
    {
        Assertions.assertSame(collection, UIComponentBase.saveAttachedState(facesContext, collection));
    }

    private void assertSavedItemByItem(Collection<?> collection)
    {
        Assertions.assertInstanceOf(_AttachedCollectionStateWrapper.class,
                UIComponentBase.saveAttachedState(facesContext, collection));
    }

    private void assertRestoredWithExactType(Collection<?> collection) throws Exception
    {
        assertSavedWhole(collection);
        for (Object restored : restore(collection))
        {
            Assertions.assertEquals(collection.getClass(), restored.getClass());
            Assertions.assertEquals(collection, restored);
        }
    }

    /**
     * Returns the collection restored without and with serialization of the saved state.
     */
    private List<Object> restore(Collection<?> collection) throws Exception
    {
        Object state = UIComponentBase.saveAttachedState(facesContext, collection);
        return List.of(UIComponentBase.restoreAttachedState(facesContext, state),
                UIComponentBase.restoreAttachedState(facesContext, serialize(state)));
    }

    private static Object serialize(Object state) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes))
        {
            out.writeObject(state);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())))
        {
            return in.readObject();
        }
    }

    public static final class PrivateConstructorList extends ArrayList<String>
    {
        private static final long serialVersionUID = 1L;

        private PrivateConstructorList()
        {
        }

        static PrivateConstructorList of(String... items)
        {
            PrivateConstructorList list = new PrivateConstructorList();
            list.addAll(Arrays.asList(items));
            return list;
        }
    }

    static final class PackagePrivateConstructorList extends ArrayList<String>
    {
        private static final long serialVersionUID = 1L;

        PackagePrivateConstructorList()
        {
        }
    }
}
