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
package org.apache.myfaces.flow.cdi;

import java.io.Serializable;
import org.apache.myfaces.util.lang.LRULinkedHashMap;

/**
 * This class is a wrapper used to deal with concurrency issues when accessing the inner LRUMap.
 */
class FacesFlowClientWindowCollection implements Serializable
{
    private static final long serialVersionUID = 1L;

    private LRULinkedHashMap<String, String> map;
    private FlowScopeBeanHolder holder;

    public FacesFlowClientWindowCollection()
    {
    }
    
    public FacesFlowClientWindowCollection(int capacity)
    {
        this.map = new LRULinkedHashMap<>(capacity, (eldest) ->
        {
            holder.clearFlowMap(eldest.getKey());
        });
    }

    public synchronized void put(String key, String value)
    {
        map.put(key, value);
    }
    
    public synchronized String get(String key)
    {
        return map.get(key);
    }
    
    public synchronized void remove(String key)
    {
        map.remove(key);
    }
    
    public synchronized boolean isEmpty()
    {
        return map.isEmpty();
    }
    
    public void setFlowScopeBeanHolder(FlowScopeBeanHolder holder)
    {
        this.holder = holder;
    }
}
