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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class IterationStatusExpression extends ValueExpression
{

    private static final long serialVersionUID = 1L;

    private final IterationStatus status;

    public IterationStatusExpression(IterationStatus status)
    {
        this.status = status;
    }

    @Override
    public Object getValue(ELContext context)
    {
        return this.status;
    }

    @Override
    public void setValue(ELContext context, Object value)
    {
        throw new UnsupportedOperationException("Cannot set IterationStatus");
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        return true;
    }

    @Override
    public Class getType(ELContext context)
    {
        return IterationStatus.class;
    }

    @Override
    public Class getExpectedType()
    {
        return IterationStatus.class;
    }

    @Override
    public String getExpressionString()
    {
        return this.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.status.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return this.status.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return this.status.toString();
    }

}
