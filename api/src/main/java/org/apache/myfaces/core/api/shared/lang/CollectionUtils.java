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
package org.apache.myfaces.core.api.shared.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CollectionUtils
{
    public static void forEach(Object value, Consumer<Object> callback)
    {
        if (value != null && value.getClass().isArray())
        {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++)
            {
                callback.accept(Array.get(value, i));
            }
        }
        else if (value instanceof ArrayList)
        {
            ArrayList<?> arrayList = (ArrayList) value;
            for (int i = 0; i < arrayList.size(); i++)
            {
                callback.accept(arrayList.get(i));
            }
        }
        else if (value instanceof Iterable)
        {
            // value is Iterable --> Collection, DataModel,...
            Iterator<?> iterator = ((Iterable<?>) value).iterator();
            while (iterator.hasNext())
            {
                callback.accept(iterator.next());
            }
        }
        else if (value instanceof Map)
        {
            Map<Object, Object> map = ((Map<Object, Object>) value);
            for (Map.Entry<Object, Object> entry : map.entrySet())
            {
                callback.accept(entry);
            }
        }
        else if (value instanceof Stream)
        {
            ((Stream<?>) value).forEach(callback);
        }
        else
        {
            callback.accept(value);
        }
    }
}
