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
package org.apache.myfaces.context.flash;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import java.io.Serializable;
import org.apache.myfaces.util.lang.LRULinkedHashMap;

/**
 * This class is a wrapper used to deal with concurrency issues when accessing the inner LRUMap.
 */
class FlashClientWindowTokenCollection implements Serializable
{
    private LRULinkedHashMap<String, String> map;

    public FlashClientWindowTokenCollection(int capacity)
    {
        this.map = new LRULinkedHashMap<>(capacity, (eldest) ->
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Flash flash = facesContext.getExternalContext().getFlash();
            if (flash != null)
            {
                ReleasableFlash rf = null;
                while (flash != null)
                {
                    if (flash instanceof ReleasableFlash releasableFlash)
                    {
                        rf = releasableFlash;
                        break;
                    }
                    if (flash instanceof FacesWrapper wrapper)
                    {
                        flash = (Flash) wrapper.getWrapped();
                    }
                    else
                    {
                        flash = null;
                    }
                }
                if (rf != null)
                {
                    rf.clearFlashMap(facesContext, eldest.getKey(), eldest.getValue());
                }
            }
        });
    }

    public FlashClientWindowTokenCollection()
    {
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
}
