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
package org.apache.myfaces.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentLRUCacheTest
{
    @Test
    public void testPutAndGet()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);

        Assertions.assertNull(cache.get("a"));
        Assertions.assertEquals(0, cache.size());

        Assertions.assertNull(cache.put("a", "1"));
        Assertions.assertEquals("1", cache.get("a"));
        Assertions.assertEquals(1, cache.size());

        Assertions.assertNull(cache.get("b"));
    }

    @Test
    public void testPutReturnsPreviousValue()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);

        Assertions.assertNull(cache.put("a", "1"));
        Assertions.assertEquals("1", cache.put("a", "2"));
        Assertions.assertEquals("2", cache.get("a"));
        Assertions.assertEquals(1, cache.size());
    }

    @Test
    public void testPutIgnoresNullValue()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);

        Assertions.assertNull(cache.put("a", null));
        Assertions.assertEquals(0, cache.size());

        cache.put("a", "1");
        // a null value must not drop the existing mapping either
        Assertions.assertNull(cache.put("a", null));
        Assertions.assertEquals("1", cache.get("a"));
    }

    @Test
    public void testRemove()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        cache.put("a", "1");

        Assertions.assertNull(cache.remove("unknown"));
        Assertions.assertEquals("1", cache.remove("a"));
        Assertions.assertNull(cache.get("a"));
        Assertions.assertEquals(0, cache.size());
        Assertions.assertNull(cache.remove("a"));
    }

    @Test
    public void testClear()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 5);
        Assertions.assertEquals(5, cache.size());

        cache.clear();

        Assertions.assertEquals(0, cache.size());
        Assertions.assertNull(cache.get("k0"));
        Assertions.assertTrue(cache.getLatestAccessedItems(10).isEmpty());
    }

    @Test
    public void testInvalidWaterMarks()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ConcurrentLRUCache<>(0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ConcurrentLRUCache<>(-1, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ConcurrentLRUCache<>(10, 10));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ConcurrentLRUCache<>(10, 11));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ConcurrentLRUCache<>(10, -1));
    }

    @Test
    public void testNoEvictionBelowUpperWaterMark()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 10);

        Assertions.assertEquals(10, cache.size());
        Assertions.assertEquals(keys(0, 10), keysOf(cache));
    }

    @Test
    public void testEvictionDropsDownToLowerWaterMark()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 11);

        // the 11th put crosses the upper water mark, nothing was accessed, so the entries which
        // were inserted first are dropped
        Assertions.assertEquals(5, cache.size());
        Assertions.assertEquals(keys(6, 11), keysOf(cache));
    }

    @Test
    public void testEvictionKeepsAccessedEntries()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 10);

        // k0 and k1 are the oldest entries, but accessing them protects them
        Assertions.assertEquals("v0", cache.get("k0"));
        Assertions.assertEquals("v1", cache.get("k1"));

        cache.put("k10", "v10");

        Assertions.assertEquals(5, cache.size());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("k0", "k1", "k8", "k9", "k10")),
                keysOf(cache));
    }

    @Test
    public void testEvictionResetsTheAccessFlag()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 10);
        cache.get("k0");
        cache.get("k1");
        cache.put("k10", "v10");

        // k0, k1, k8, k9 and k10 survived; the eviction started a new observation period, so the
        // protection of k0 and k1 is gone and only the newly accessed k8 is protected now
        cache.get("k8");
        putRange(cache, 11, 17);

        Assertions.assertEquals(5, cache.size());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("k8", "k13", "k14", "k15", "k16")),
                keysOf(cache));
    }

    @Test
    public void testEvictionWithLowerWaterMarkZero()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(2, 0);
        putRange(cache, 0, 3);

        Assertions.assertEquals(0, cache.size());
    }

    @Test
    public void testRepeatedPutsStayBounded()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);

        for (int i = 0; i < 1000; i++)
        {
            cache.put("k" + i, "v" + i);
            Assertions.assertTrue(cache.size() <= 10, "size must never exceed the upper water mark");
        }

        Assertions.assertEquals(10, cache.size());
        Assertions.assertEquals(keys(990, 1000), keysOf(cache));
    }

    @Test
    public void testAccessedItemsReturnEveryEntry()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 5);

        // all five entries share the same state, none of them may be swallowed
        Assertions.assertEquals(5, cache.getLatestAccessedItems(5).size());
        Assertions.assertEquals(5, cache.getLatestAccessedItems(100).size());
        Assertions.assertEquals(5, cache.getOldestAccessedItems(100).size());

        Map<String, String> items = cache.getLatestAccessedItems(100);
        for (int i = 0; i < 5; i++)
        {
            Assertions.assertEquals("v" + i, items.get("k" + i));
        }
    }

    @Test
    public void testAccessedItemsRespectN()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        putRange(cache, 0, 5);

        Assertions.assertTrue(cache.getLatestAccessedItems(0).isEmpty());
        Assertions.assertTrue(cache.getLatestAccessedItems(-1).isEmpty());
        Assertions.assertTrue(cache.getOldestAccessedItems(0).isEmpty());
        Assertions.assertEquals(1, cache.getLatestAccessedItems(1).size());
        Assertions.assertEquals(3, cache.getOldestAccessedItems(3).size());
    }

    @Test
    public void testAccessedItemsOrder()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);
        cache.put("k0", "v0");
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        // k0 was accessed, so it ranks before the untouched entries; those are ordered by insertion
        cache.get("k0");

        Assertions.assertEquals(Arrays.asList("k0", "k2", "k1"),
                new ArrayList<>(cache.getLatestAccessedItems(3).keySet()));
        Assertions.assertEquals(Arrays.asList("k1", "k2", "k0"),
                new ArrayList<>(cache.getOldestAccessedItems(3).keySet()));
    }

    @Test
    public void testAccessedItemsOnEmptyCache()
    {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(10, 5);

        Assertions.assertTrue(cache.getLatestAccessedItems(10).isEmpty());
        Assertions.assertTrue(cache.getOldestAccessedItems(10).isEmpty());
    }

    @Test
    public void testConcurrentAccess() throws Exception
    {
        final int upperWaterMark = 200;
        final ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<>(upperWaterMark, 100);
        final int threads = 8;
        final int iterations = 20000;
        final int keySpace = 1000;

        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try
        {
            for (int i = 0; i < threads; i++)
            {
                executor.execute(() ->
                {
                    try
                    {
                        start.await();

                        for (int j = 0; j < iterations; j++)
                        {
                            String key = "k" + ThreadLocalRandom.current().nextInt(keySpace);

                            String value = cache.get(key);
                            if (value != null)
                            {
                                // a key must never be mapped to the value of another key
                                Assertions.assertEquals(valueOf(key), value);
                            }
                            else
                            {
                                cache.put(key, valueOf(key));
                            }

                            if ((j & 0xFF) == 0)
                            {
                                Assertions.assertTrue(cache.getLatestAccessedItems(10).size() <= 10);
                            }
                        }
                    }
                    catch (Throwable e)
                    {
                        failure.compareAndSet(null, e);
                    }
                    finally
                    {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            Assertions.assertTrue(done.await(60, TimeUnit.SECONDS), "threads did not finish in time");
        }
        finally
        {
            executor.shutdownNow();
        }

        if (failure.get() != null)
        {
            throw new AssertionError("concurrent access failed", failure.get());
        }

        // one quiescent put is enough to bring a cache which grew during the concurrent phase back
        // under the upper water mark
        cache.put("quiescent", "v");
        Assertions.assertTrue(cache.size() <= upperWaterMark,
                "size " + cache.size() + " exceeds the upper water mark " + upperWaterMark);
    }

    private static String valueOf(String key)
    {
        return "v" + key.substring(1);
    }

    private static void putRange(ConcurrentLRUCache<String, String> cache, int fromInclusive, int toExclusive)
    {
        for (int i = fromInclusive; i < toExclusive; i++)
        {
            cache.put("k" + i, "v" + i);
        }
    }

    private static Set<String> keys(int fromInclusive, int toExclusive)
    {
        Set<String> result = new LinkedHashSet<>();
        for (int i = fromInclusive; i < toExclusive; i++)
        {
            result.add("k" + i);
        }
        return result;
    }

    /**
     * Reads the keys without touching the access flags, which {@link ConcurrentLRUCache#get} would do.
     */
    private static Set<String> keysOf(ConcurrentLRUCache<String, String> cache)
    {
        return new LinkedHashSet<>(cache.getLatestAccessedItems(Integer.MAX_VALUE).keySet());
    }
}
