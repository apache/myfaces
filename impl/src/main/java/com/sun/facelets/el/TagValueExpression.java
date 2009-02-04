/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.el;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;

import com.sun.facelets.tag.TagAttribute;

/**
 * 
 * 
 * @author Jacob Hookom
 * @version $Id: TagValueExpression.java,v 1.7 2008/07/13 19:01:42 rlubke Exp $
 */
public final class TagValueExpression extends ValueExpression implements Externalizable
{

    private static final long serialVersionUID = 1L;

    private ValueExpression orig;

    private String attr;

    public TagValueExpression()
    {
        super();
    }

    public TagValueExpression(TagAttribute attr, ValueExpression orig)
    {
        this.attr = attr.toString();
        this.orig = orig;
    }

    public Class getExpectedType()
    {
        return this.orig.getExpectedType();
    }

    public Class getType(ELContext context)
    {
        try
        {
            return this.orig.getType(context);
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

    public Object getValue(ELContext context)
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

    public boolean isReadOnly(ELContext context)
    {
        try
        {
            return this.orig.isReadOnly(context);
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

    public void setValue(ELContext context, Object value)
    {
        try
        {
            this.orig.setValue(context, value);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new PropertyNotFoundException(this.attr + ": " + pnfe.getMessage(), pnfe.getCause());
        }
        catch (PropertyNotWritableException pnwe)
        {
            throw new PropertyNotWritableException(this.attr + ": " + pnwe.getMessage(), pnwe.getCause());
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
}
