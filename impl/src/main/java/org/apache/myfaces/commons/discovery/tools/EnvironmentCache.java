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
package org.apache.myfaces.commons.discovery.tools;

import java.util.HashMap;

import org.apache.myfaces.commons.discovery.jdk.JDKHooks;


/**
 * Cache by a 'key' unique to the environment:
 * 
 * - ClassLoader::groupContext::Object Cache
 *         Cache : HashMap
 *         Key   : Thread Context Class Loader (<code>ClassLoader</code>)
 *         Value : groupContext::SPI Cache (<code>HashMap</code>)
 * 
 * //- groupContext::Object Cache
 * //         Cache : HashMap
 * //         Key   : groupContext (<code>String</code>)
 * //        Value : <code>Object</code>
 * 
 * When we 'release', it is expected that the caller of the 'release'
 * have the same thread context class loader... as that will be used
 * to identify cached entries to be released.
 * 
 * @author Richard A. Sitze
 */
public class EnvironmentCache {
    /**
     * Allows null key, important as default groupContext is null.
     * 
     * We will manage synchronization directly, so all caches are implemented
     * as HashMap (unsynchronized).
     * 
     */
    private static final HashMap root_cache = new HashMap();

    /**
     * Initial hash size for SPI's, default just seem TO big today..
     */
    public static final int smallHashSize = 13;
    
    /**
     * Get object keyed by classLoader.
     */
    public static synchronized Object get(ClassLoader classLoader)
    {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        return root_cache.get(classLoader);
    }
    
    /**
     * Put service keyed by spi & classLoader.
     */
    public static synchronized void put(ClassLoader classLoader, Object object)
    {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        if (object != null) {
            root_cache.put(classLoader, object);
        }
    }


    /********************** CACHE-MANAGEMENT SUPPORT **********************/
    
    /**
     * Release all internal references to previously created service
     * instances associated with the current thread context class loader.
     * The <code>release()</code> method is called for service instances that
     * implement the <code>Service</code> interface.
     *
     * This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static synchronized void release() {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        root_cache.remove(JDKHooks.getJDKHooks().getThreadContextClassLoader());
    }
    
    
    /**
     * Release any internal references to a previously created service
     * instance associated with the current thread context class loader.
     * If the SPI instance implements <code>Service</code>, then call
     * <code>release()</code>.
     */
    public static synchronized void release(ClassLoader classLoader) {
        /**
         * 'null' (bootstrap/system class loader) thread context class loader
         * is ok...  Until we learn otherwise.
         */
        root_cache.remove(classLoader);
    }
}
