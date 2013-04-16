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
package org.apache.myfaces.el;

import javax.el.ELContext;
import javax.el.ValueExpression;

public class LiteralValueExpression extends ValueExpression
{

    private final Object _value;

    public LiteralValueExpression(Object value)
    {
        _value = value;
    }

    @Override
    public Class<?> getExpectedType()
    {
        return _value == null ? Object.class : _value.getClass();
    }

    @Override
    public Class<?> getType(ELContext context)
    {
        return getExpectedType();
    }

    @Override
    public Object getValue(ELContext context)
    {
        return _value;
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj)
    {
        return (_value != null && _value.equals(obj)) || _value == obj;
    }

    @Override
    public String getExpressionString()
    {
        return _value == null ? "" : _value.toString();
    }

    @Override
    public int hashCode()
    {
        return _value == null ? 0 : _value.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return true;
    }

}
