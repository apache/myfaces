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

import java.lang.reflect.Array;
import java.util.Iterator;

public class ArrayIterator implements Iterator<Object>
{
    protected final Object array;
    protected int i;
    protected final int len;

    public ArrayIterator(Object src)
    {
        this.i = 0;
        this.array = src;
        this.len = Array.getLength(src);
    }

    @Override
    public boolean hasNext()
    {
        return this.i < this.len;
    }

    @Override
    public Object next()
    {
        return Array.get(this.array, this.i++);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
