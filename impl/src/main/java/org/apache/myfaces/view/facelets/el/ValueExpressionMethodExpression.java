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
import java.util.Objects;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * This MethodExpression contains a ValueExpression which resolves to 
 * the "real" MethodExpression that should be invoked. This is needed 
 * when the MethodExpression is on the parent composite component attribute map.
 * See FaceletViewDeclarationLanguage.retargetMethodExpressions() for details.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ValueExpressionMethodExpression extends MethodExpression 
    implements FacesWrapper<ValueExpression>, Externalizable
{
    
    private static final long serialVersionUID = -2847633717581167765L;
    
    private ValueExpression valueExpression;
    
    public ValueExpressionMethodExpression()
    {
    }
    
    public ValueExpressionMethodExpression(ValueExpression valueExpression)
    {
        this.valueExpression = valueExpression;   
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context)
    {
        MethodExpression me = getMethodExpression(context);
        if (me != null)
        {
            return me.getMethodInfo(context);
        }
        return null;
    }

    @Override
    public Object invoke(ELContext context, Object[] params)
    {
        MethodExpression me = getMethodExpression(context);
        if (me != null)
        {        
            return me.invoke(context, params);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj)
    {
        MethodExpression me = getMethodExpression();
        if (me != null)
        {        
            return me.equals(obj);
        }
        if (!(obj instanceof ValueExpressionMethodExpression))
        {
            return false;
        }

        ValueExpressionMethodExpression other = (ValueExpressionMethodExpression) obj;
        return Objects.equals(this.valueExpression, other.valueExpression);
    }

    @Override
    public String getExpressionString()
    {
        return valueExpression.getExpressionString();
    }

    @Override
    public int hashCode()
    {
        MethodExpression me = getMethodExpression();
        if (me != null)
        {        
            return me.hashCode();
        }
        return valueExpression.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        MethodExpression me = getMethodExpression();
        if (me != null)
        {
            return me.isLiteralText();
        }
        return valueExpression.isLiteralText();
    }
    
    private MethodExpression getMethodExpression()
    {
        return getMethodExpression(FacesContext.getCurrentInstance().getELContext());
    }
    
    private MethodExpression getMethodExpression(ELContext context)
    {
        Object meOrVe = valueExpression.getValue(context);
        if (meOrVe instanceof MethodExpression expression)
        {
            return expression;
        }
        else if (meOrVe instanceof ValueExpression)
        {
            while (meOrVe != null && meOrVe instanceof ValueExpression)
            {
                meOrVe = ((ValueExpression)meOrVe).getValue(context);
            }
            return (MethodExpression) meOrVe;
        }
        else
        {
            return null;
        }
    }

    @Override
    public ValueExpression getWrapped()
    {
        return valueExpression;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.valueExpression = (ValueExpression) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(this.valueExpression);
    }
}
