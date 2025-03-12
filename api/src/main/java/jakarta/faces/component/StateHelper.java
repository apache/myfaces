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

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public interface StateHelper extends StateHolder
{
    public void add(Serializable key, Object value);

    public <T> T eval(Serializable key);

    public <T> T eval(Serializable key, T defaultValue);

    /**
     * 
     * @param key
     * @param defaultValueSupplier
     * @return 
     * 
     * @since 4.0
     */
    public <T> T eval(Serializable key, Supplier<T> defaultValueSupplier);

    /**
     *
     * @param stateHelper
     * @param key
     * @param defaultValueSupplier
     * @return
     * @param <T>
     *
     * @since 5.0
     */
    public default <T> T computeIfAbsent(Serializable key, Supplier<T> defaultValueSupplier) {
        T value = (T) get(key);
        if (value == null) {
            value = defaultValueSupplier.get();
            put(key, value);
        }
        return value;
    }

    public <T> T get(Serializable key);

    public <T> T put(Serializable key, T value);

    public <T> T put(Serializable key, String mapKey, T value);

    public <T> T remove(Serializable key);

    public <T> T remove(Serializable key, T valueOrKey);
}
