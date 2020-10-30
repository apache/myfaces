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
import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesWrapper;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 *
 * @author Leonardo Uribe
 */
public class RedirectMethodExpressionValueExpressionActionListener
       implements ActionListener, FacesWrapper<ValueExpression>, Externalizable
{
    private ValueExpression valueExpression;

    public RedirectMethodExpressionValueExpressionActionListener()
    {
    }
    
    public RedirectMethodExpressionValueExpressionActionListener(ValueExpression valueExpression)
    {
        this.valueExpression = valueExpression;
    }

    @Override
    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        ELContext elContext = actionEvent.getFacesContext().getELContext();
        getMethodExpression(elContext).invoke(elContext, new Object[]{actionEvent});
    }

    private MethodExpression getMethodExpression(ELContext context)
    {
        Object meOrVe = valueExpression.getValue(context);
        if (meOrVe instanceof MethodExpression)
        {
            return (MethodExpression) meOrVe;
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
