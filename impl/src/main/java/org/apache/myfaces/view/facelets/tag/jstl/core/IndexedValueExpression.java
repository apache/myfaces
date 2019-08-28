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

import javax.el.ELContext;
import javax.el.ValueExpression;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class IndexedValueExpression extends ValueExpression
{
    private static final long serialVersionUID = 1L;

    private final Integer i;
    private final ValueExpression orig;

    public IndexedValueExpression(ValueExpression orig, int i)
    {
        this.i = i;
        this.orig = orig;
    }

    @Override
    public Object getValue(ELContext context)
    {
        Object base = this.orig.getValue(context);
        if (base != null)
        {
            context.setPropertyResolved(false);
            return context.getELResolver().getValue(context, base, i);
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object value)
    {
        Object base = this.orig.getValue(context);
        if (base != null)
        {
            context.setPropertyResolved(false);
            context.getELResolver().setValue(context, base, i, value);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        Object base = this.orig.getValue(context);
        if (base != null)
        {
            context.setPropertyResolved(false);
            return context.getELResolver().isReadOnly(context, base, i);
        }
        return true;
    }

    @Override
    public Class getType(ELContext context)
    {
        Object base = this.orig.getValue(context);
        if (base != null)
        {
            context.setPropertyResolved(false);
            return context.getELResolver().getType(context, base, i);
        }
        return null;
    }

    @Override
    public Class getExpectedType()
    {
        return Object.class;
    }

    @Override
    public String getExpressionString()
    {
        return this.orig.getExpressionString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.orig.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return this.orig.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return false;
    }

}
