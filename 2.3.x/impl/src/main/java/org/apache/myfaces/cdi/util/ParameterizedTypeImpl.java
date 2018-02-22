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
package org.apache.myfaces.cdi.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class ParameterizedTypeImpl implements ParameterizedType
{
    private final Type ownerType;
    private final Class<?> rawType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments)
    {
        this(null, rawType, actualTypeArguments);
    }

    public ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] actualTypeArguments)
    {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type getOwnerType()
    {
        return ownerType;
    }

    @Override
    public Type getRawType()
    {
        return rawType;
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return actualTypeArguments;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ParameterizedTypeImpl other = (ParameterizedTypeImpl) obj;
        if (!Objects.equals(this.ownerType, other.ownerType))
        {
            return false;
        }
        if (!Objects.equals(this.rawType, other.rawType))
        {
            return false;
        }
        if (!Arrays.deepEquals(this.actualTypeArguments, other.actualTypeArguments))
        {
            return false;
        }
        return true;
    }
}
