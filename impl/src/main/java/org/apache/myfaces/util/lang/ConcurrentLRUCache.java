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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A bounded, thread-safe cache backed by a {@link ConcurrentHashMap}, using a CLOCK (second chance)
 * eviction strategy.
 *
 * <p>Reads are the hot path of this cache and must not contend: a {@link #get(java.lang.Object)} only
 * sets a flag on the entry itself instead of updating a counter shared by all entries. The flag is
 * evaluated and cleared by the eviction run, so it means "accessed since the last eviction", which is
 * a sufficient approximation of recency for the caches in this project.</p>
 *
 * <p>Note that this is therefore not a true LRU (least-recently-used) cache. An eviction run drops
 * entries in the following order until the lowerWaterMark is reached:</p>
 * <ol>
 *   <li>entries which were not accessed since the previous run, the one inserted first going first,</li>
 *   <li>entries which were accessed since the previous run, the one inserted first going first.</li>
 * </ol>
 *
 * <p>A just inserted entry counts as not accessed, but is the last one of its group to be evicted,
 * so it only gets dropped if the cache has to shrink past every other candidate.</p>
 *
 * <p>Eviction is triggered by {@link #put(java.lang.Object, java.lang.Object)} once the cache grew
 * beyond the upperWaterMark and runs on the calling thread. Only one thread evicts at a time, all
 * others continue without blocking.</p>
 */
public class ConcurrentLRUCache<K, V>
{
    private final ConcurrentHashMap<K, CacheEntry<V>> map;
    private final int upperWaterMark;
    private final int lowerWaterMark;

    private final AtomicBoolean evicting = new AtomicBoolean();
    private final AtomicLong sequence = new AtomicLong();

    /**
     * @param upperWaterMark the size which triggers an eviction run; must be &gt; 0
     * @param lowerWaterMark the size an eviction run brings the cache down to;
     *                       must be &gt;= 0 and &lt; upperWaterMark
     */
    public ConcurrentLRUCache(int upperWaterMark, int lowerWaterMark)
    {
        if (upperWaterMark < 1)
        {
            throw new IllegalArgumentException("upperWaterMark must be > 0");
        }
        if (lowerWaterMark < 0)
        {
            throw new IllegalArgumentException("lowerWaterMark must be >= 0");
        }
        if (lowerWaterMark >= upperWaterMark)
        {
            throw new IllegalArgumentException("lowerWaterMark must be < upperWaterMark");
        }

        this.upperWaterMark = upperWaterMark;
        this.lowerWaterMark = lowerWaterMark;
        this.map = new ConcurrentHashMap<>(upperWaterMark);
    }

    public V get(K key)
    {
        CacheEntry<V> entry = map.get(key);
        if (entry == null)
        {
            return null;
        }

        // protect the entry on the next eviction run; the check avoids the volatile write on the
        // common path, where the flag is already set
        if (!entry.referenced)
        {
            entry.referenced = true;
        }

        return entry.value;
    }

    /**
     * Puts the value into the cache, unless it is <code>null</code>.
     *
     * @return the previously mapped value or <code>null</code>
     */
    public V put(K key, V value)
    {
        if (value == null)
        {
            return null;
        }

        CacheEntry<V> previous = map.put(key, new CacheEntry<>(value, sequence.incrementAndGet()));

        if (map.size() > upperWaterMark)
        {
            evict();
        }

        return previous == null ? null : previous.value;
    }

    public V remove(K key)
    {
        CacheEntry<V> entry = map.remove(key);
        return entry == null ? null : entry.value;
    }

    public void clear()
    {
        map.clear();
    }

    public int size()
    {
        return map.size();
    }

    private void evict()
    {
        if (!evicting.compareAndSet(false, true))
        {
            // another thread is already evicting and will bring the size down for us
            return;
        }

        try
        {
            int size = map.size();
            if (size <= lowerWaterMark)
            {
                return;
            }

            for (Candidate<K, V> candidate : sortedSnapshot(evictionOrder()))
            {
                if (size <= lowerWaterMark)
                {
                    break;
                }

                // identity based removal, so an entry which was concurrently replaced by another
                // thread is kept
                if (map.remove(candidate.key, candidate.entry))
                {
                    size--;
                }
            }

            // start a new observation period: every surviving entry has to be accessed again to be
            // protected on the next run
            for (CacheEntry<V> entry : map.values())
            {
                entry.referenced = false;
            }
        }
        finally
        {
            evicting.set(false);
        }
    }

    /**
     * Orders the entries the way the eviction drops them: not accessed before accessed, inserted
     * first before inserted last.
     */
    private Comparator<Candidate<K, V>> evictionOrder()
    {
        return Comparator
                .comparing((Candidate<K, V> candidate) -> candidate.referenced)
                .thenComparingLong(candidate -> candidate.entry.sequence);
    }

    private List<Candidate<K, V>> sortedSnapshot(Comparator<Candidate<K, V>> order)
    {
        List<Candidate<K, V>> candidates = new ArrayList<>(map.size());
        for (Map.Entry<K, CacheEntry<V>> entry : map.entrySet())
        {
            candidates.add(new Candidate<>(entry.getKey(), entry.getValue()));
        }

        candidates.sort(order);

        return candidates;
    }

    /**
     * Returns at most <code>n</code> entries, approximately ordered from the most recently used to
     * the least recently used one, which is the reverse of the order the eviction drops them in.
     *
     * <p>The order is an approximation: entries accessed since the last eviction run rank before
     * entries which were not and, within both groups, the entry inserted last ranks first.</p>
     */
    public Map<K, V> getLatestAccessedItems(int n)
    {
        return collect(n, evictionOrder().reversed());
    }

    /**
     * Returns at most <code>n</code> entries, approximately ordered from the least recently used to
     * the most recently used one, which is the order the eviction drops them in.
     *
     * @see #getLatestAccessedItems(int)
     */
    public Map<K, V> getOldestAccessedItems(int n)
    {
        return collect(n, evictionOrder());
    }

    private Map<K, V> collect(int n, Comparator<Candidate<K, V>> order)
    {
        Map<K, V> result = new LinkedHashMap<>();
        if (n <= 0)
        {
            return result;
        }

        for (Candidate<K, V> candidate : sortedSnapshot(order))
        {
            if (result.size() >= n)
            {
                break;
            }

            result.put(candidate.key, candidate.entry.value);
        }

        return result;
    }

    /**
     * A cache entry prepared for sorting. The access flag is captured on creation, because a
     * comparator which reads it directly would violate its own contract as soon as another thread
     * accesses an entry while the sort is running.
     */
    private static final class Candidate<K, V>
    {
        private final K key;
        private final CacheEntry<V> entry;
        private final boolean referenced;

        private Candidate(K key, CacheEntry<V> entry)
        {
            this.key = key;
            this.entry = entry;
            this.referenced = entry.referenced;
        }
    }

    /**
     * Deliberately does not override equals/hashCode: the eviction relies on the identity based
     * {@link ConcurrentHashMap#remove(java.lang.Object, java.lang.Object)} to not drop an entry
     * which was concurrently replaced by another thread.
     */
    private static final class CacheEntry<V>
    {
        private final V value;
        private final long sequence;
        private volatile boolean referenced;

        private CacheEntry(V value, long sequence)
        {
            this.value = value;
            this.sequence = sequence;
        }

        @Override
        public String toString()
        {
            return "value: " + value + " sequence: " + sequence + " referenced: " + referenced;
        }
    }
}
