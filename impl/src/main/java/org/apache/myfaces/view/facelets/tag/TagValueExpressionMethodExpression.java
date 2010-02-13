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
package org.apache.myfaces.view.facelets.tag;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.faces.view.facelets.TagAttribute;

/**
 * This class is used to retrieve a MethodExpression indirectly, "jumping" between composite component
 * attribute maps. 
 * 
 * @author Leonardo Uribe
 * @version $Id: TagValueExpression.java,v 1.7 2008/07/13 19:01:42 rlubke Exp $
 */
public final class TagValueExpressionMethodExpression extends MethodExpression implements Externalizable
{

    private static final long serialVersionUID = 1L;

    private ValueExpression orig;

    private String attr;

    public TagValueExpressionMethodExpression()
    {
        super();
    }

    public TagValueExpressionMethodExpression(TagAttribute attr, ValueExpression orig)
    {
        this.attr = attr.toString();
        this.orig = orig;
    }

    private Object getValue(ELContext context)
    {
        try
        {
            return this.orig.getValue(context);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new PropertyNotFoundException(this.attr + ": " + pnfe.getMessage(), pnfe.getCause());
        }
        catch (ELException e)
        {
            throw new ELException(this.attr + ": " + e.getMessage(), e.getCause());
        }
    }

    public boolean equals(Object obj)
    {
        return this.orig.equals(obj);
    }

    public String getExpressionString()
    {
        return this.orig.getExpressionString();
    }

    public int hashCode()
    {
        return this.orig.hashCode();
    }

    public boolean isLiteralText()
    {
        return this.orig.isLiteralText();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.orig = (ValueExpression) in.readObject();
        this.attr = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(this.orig);
        out.writeUTF(this.attr);
    }

    public String toString()
    {
        return this.attr + ": " + this.orig;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context)
    {
        try
        {
            final MethodExpression expr = (MethodExpression)this.getValue(context);
            if (expr != null)
            {
                return expr.getMethodInfo(context);
            }
            else
            {
                throw new PropertyNotFoundException(this.attr);
            }
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new PropertyNotFoundException(this.attr + ": " + pnfe.getMessage(), pnfe.getCause());
        }
        catch (MethodNotFoundException mnfe)
        {
            throw new MethodNotFoundException(this.attr + ": " + mnfe.getMessage(), mnfe.getCause());
        }
        catch (ELException e)
        {
            throw new ELException(this.attr + ": " + e.getMessage(), e.getCause());
        }
    }

    @Override
    public Object invoke(ELContext context, Object[] params)
    {
        try
        {
            final MethodExpression expr = (MethodExpression)this.getValue(context);
            if (expr != null)
            {
                return expr.invoke(context, params);
            }
            else
            {
                throw new PropertyNotFoundException(this.attr);
            }
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new PropertyNotFoundException(this.attr + ": " + pnfe.getMessage(), pnfe.getCause());
        }
        catch (MethodNotFoundException mnfe)
        {
            throw new MethodNotFoundException(this.attr + ": " + mnfe.getMessage(), mnfe.getCause());
        }
        catch (ELException e)
        {
            throw new ELException(this.attr + ": " + e.getMessage(), e.getCause());
        }
    }
}