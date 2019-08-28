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

import java.util.function.Supplier;

/**
 * Inspired by commons-lang LazyInitializer.
 *
 * @param <T> The type to be lazy initialized.
 */
public class Lazy<T>
{

    private static final Object NOT_INITIALIZED = new Object();

    @SuppressWarnings("unchecked")
    private volatile T value = (T) NOT_INITIALIZED;
    private volatile Supplier<T> init;

    public Lazy(Supplier<T> init)
    {
        this.init = init;
    }
    
    public synchronized void reset(Supplier<T> init)
    {
        this.init = init;
        this.value = (T) NOT_INITIALIZED;
    }

    public synchronized void reset(T value)
    {
        this.value = value;
    }
    
    public T get()
    {
        T result = value;

        if (result == NOT_INITIALIZED)
        {
            synchronized (this)
            {
                result = value;
                if (result == NOT_INITIALIZED)
                {
                    value = init.get();
                    result = value;
                }
            }
        }

        return result;
    }

    public boolean isInitialized()
    {
        return value != NOT_INITIALIZED;
    }
}
