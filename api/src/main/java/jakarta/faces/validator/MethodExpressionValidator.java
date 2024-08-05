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

package jakarta.faces.validator;

import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public class MethodExpressionValidator implements Validator<Object>, StateHolder
{

    private MethodExpression methodExpression;

    private boolean isTransient = false;

    /** Creates a new instance of MethodExpressionValidator */
    public MethodExpressionValidator()
    {
    }

    public MethodExpressionValidator(MethodExpression methodExpression)
    {
        Assert.notNull(methodExpression, "methodExpression");
        this.methodExpression = methodExpression;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        Object[] params = new Object[3];
        params[0] = context;
        params[1] = component;
        params[2] = value;

        try
        {
            methodExpression.invoke(context.getELContext(), params);
        }
        catch (ELException e)
        {
            Throwable cause = e.getCause();
            ValidatorException vex = null;
            if (cause != null)
            {
                do
                {
                    if (cause != null && cause instanceof ValidatorException exception)
                    {
                        vex = exception;
                        break;
                    }
                    cause = cause.getCause();
                }
                while (cause != null);
            }
            if (vex != null)
            {
                throw vex;
            }
            else
            {
                throw e;
            }
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Assert.notNull(context, "context");

        methodExpression = (MethodExpression)state;
    }

    @Override
    public Object saveState(FacesContext context)
    {
        Assert.notNull(context, "context");

        return methodExpression;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        isTransient = newTransientValue;
    }

    @Override
    public boolean isTransient()
    {
        return isTransient;
    }

}
