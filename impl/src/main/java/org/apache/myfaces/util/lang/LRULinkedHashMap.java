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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> implements Serializable
{
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private final int capacity;
    private SerializableConsumer<Map.Entry<K, V>> expiredCallback;

    public LRULinkedHashMap(int capacity)
    {
        // 1 extra element as add happens before remove (101), and load factor big
        // enough to avoid triggering resize.  True = keep in access order.
        super(capacity + 1, DEFAULT_LOAD_FACTOR, true);
        this.capacity = capacity;
    }

    public LRULinkedHashMap(int capacity, SerializableConsumer<Map.Entry<K, V>> expiredCallback)
    {
        this(capacity);
        this.expiredCallback = expiredCallback;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        boolean remove = size() > capacity;
        if (remove && expiredCallback != null)
        {
            expiredCallback.accept(eldest);
        }
        return remove;
    }
}
