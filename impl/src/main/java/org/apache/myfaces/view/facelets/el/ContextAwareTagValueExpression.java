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

    private ValueExpression _wrapped; 
    private Location _location;
    private String _qName;

    public ContextAwareTagValueExpression()
    {
        super();
    }

    public ContextAwareTagValueExpression(TagAttribute tagAttribute, ValueExpression valueExpression)
    {
        _location = tagAttribute.getLocation();
        _qName = tagAttribute.getQName();
        _wrapped = valueExpression;
    }

    @Override
    public Class<?> getExpectedType()
    {
        return _wrapped.getExpectedType();
    }

    @Override
    public Class<?> getType(ELContext context)
    {
        try
        {
            return _wrapped.getType(context);
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
            return _wrapped.getValue(context);
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
            return _wrapped.isReadOnly(context);
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
            _wrapped.setValue(context, value);
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
        return _wrapped.equals(obj);
    }

    @Override
    public String getExpressionString()
    {
        return _wrapped.getExpressionString();
    }

    @Override
    public int hashCode()
    {
        return _wrapped.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return _wrapped.isLiteralText();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _wrapped = (ValueExpression) in.readObject();
        _location = (Location) in.readObject();
        _qName = in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(_wrapped);
        out.writeObject(_location);
        out.writeUTF(_qName);
    }

    @Override
    public String toString()
    {
        return _location + ": " + _wrapped;
    }

    @Override
    public ValueExpression getWrapped()
    {
        return _wrapped;
    }

    @Override
    public Location getLocation()
    {
        return _location;
    }
    
    @Override
    public String getQName()
    {
        return _qName;
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
