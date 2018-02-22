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

package org.apache.myfaces.util;

import java.util.Iterator;

/**
 *
 */
public abstract class SkipMatchIterator<T> implements Iterator<T>
{
    private Iterator<T> delegate;
    private T value;


    public SkipMatchIterator(Iterator<T> delegate)
    {
        this.delegate = delegate;
        this.value = null;
    }

    @Override
    public boolean hasNext()
    {
        if (this.value != null)
        {
            return true;
        }
        if (delegate.hasNext())
        {
            do 
            {
                this.value = delegate.next();
                if (match(value))
                {
                    //Skip
                    value = null;
                }
            }
            while (value == null && delegate.hasNext());

            if (value != null)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public T next()
    {
        T value = this.value;
        do 
        {
            if (value == null)
            {
                if (hasNext())
                {
                    value = null;
                    return this.value;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                this.value = null;
                return value;
            }
        }
        while (value == null);
    }
    
    protected abstract boolean match(T instance);
}
