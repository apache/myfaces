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
package org.apache.myfaces.view.facelets.el;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import jakarta.faces.FacesWrapper;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * 
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class ContextAwareTagValueExpression
        extends ValueExpression
        implements Externalizable, FacesWrapper<ValueExpression>, ContextAware
{

    private static final long serialVersionUID = 1L;

    private ValueExpression wrapped; 
    private Location location;
    private String qName;

    public ContextAwareTagValueExpression()
    {
        super();
    }

    public ContextAwareTagValueExpression(Location location, String qName, ValueExpression valueExpression)
    {
        this.location = location;
        this.qName = qName;
        this.wrapped = valueExpression;
    }

    public ContextAwareTagValueExpression(TagAttribute tagAttribute, ValueExpression valueExpression)
    {
        this.location = tagAttribute.getLocation();
        this.qName = tagAttribute.getQName();
        this.wrapped = valueExpression;
    }

    @Override
    public Class<?> getExpectedType()
    {
        return wrapped.getExpectedType();
    }

    @Override
    public Class<?> getType(ELContext context)
    {
        try
        {
            return wrapped.getType(context);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new ContextAwarePropertyNotFoundException(getLocation(), getLocalExpressionString(), getQName(),pnfe);
        }
        catch (ELException e)
        {
            throw new ContextAwareELException(getLocation(), getLocalExpressionString(), getQName(), e);
        }
    }

    @Override
    public Object getValue(ELContext context)
    {
        try
        {
            return wrapped.getValue(context);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new ContextAwarePropertyNotFoundException(getLocation(), getLocalExpressionString(), getQName(),pnfe);
        }
        catch (ELException e)
        {
            throw new ContextAwareELException(getLocation(), getLocalExpressionString(), getQName(), e);
        }
        //Not necessary because NullPointerException by null context never occur and should not be wrapped
        //catch (Exception e)
        //{
        //    throw new ContextAwareException(getLocation(), getLocalExpressionString(), getQName(), e);
        //}
    }
    
    private String getLocalExpressionString()
    {
        String expressionString = null;
        try
        {
            expressionString = getExpressionString();
        }
        catch (Throwable t)
        {
            //swallo it because it is not important
        }
        return expressionString;
    }

    @Override
    public boolean isReadOnly(ELContext context)
    {
        try
        {
            return wrapped.isReadOnly(context);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new ContextAwarePropertyNotFoundException(getLocation(), getLocalExpressionString(), getQName(),pnfe);
        }
        catch (ELException e)
        {
            throw new ContextAwareELException(getLocation(), getLocalExpressionString(), getQName(), e);
        }
    }

    @Override
    public void setValue(ELContext context, Object value)
    {
        try
        {
            wrapped.setValue(context, value);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new ContextAwarePropertyNotFoundException(getLocation(), getLocalExpressionString(), getQName(),pnfe);
        }
        catch (PropertyNotWritableException pnwe)
        {
            throw new ContextAwarePropertyNotWritableException(getLocation(), getLocalExpressionString(),
                                                               getQName(), pnwe);
        }
        catch (ELException e)
        {
            throw new ContextAwareELException(getLocation(), getLocalExpressionString(), getQName(), e);
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return wrapped.equals(obj);
    }

    @Override
    public String getExpressionString()
    {
        return wrapped.getExpressionString();
    }

    @Override
    public int hashCode()
    {
        return wrapped.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return wrapped.isLiteralText();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        wrapped = (ValueExpression) in.readObject();
        location = (Location) in.readObject();
        qName = in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(wrapped);
        out.writeObject(location);
        out.writeUTF(qName);
    }

    @Override
    public String toString()
    {
        return location + ": " + wrapped;
    }

    @Override
    public ValueExpression getWrapped()
    {
        return wrapped;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }
    
    @Override
    public String getQName()
    {
        return qName;
    }
    
    @Override
    public ValueReference getValueReference(ELContext context)
    {
        try
        {
            return getWrapped().getValueReference(context);
        }
        catch (PropertyNotFoundException pnfe)
        {
            throw new ContextAwarePropertyNotFoundException(getLocation(), getExpressionString(), getQName() ,  pnfe);
        }
        catch (ELException e)
        {
            throw new ContextAwareELException(getLocation(), getExpressionString(), getQName(),  e);
        }
    }
}
