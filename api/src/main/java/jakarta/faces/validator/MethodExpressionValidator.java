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
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * {@link Validator} which wraps a {@link MethodExpression}.
 * <p>
 * Since Faces 5.0, this class implements {@link PartialStateHolder}. Its state is set only by its constructor, or by
 * {@link #restoreState(FacesContext, Object)} from a full state. It does not change after the build of the view, so
 * while {@link #initialStateMarked()} returns <code>true</code>, {@link #saveState(FacesContext)} returns
 * <code>null</code>.
 * </p>
 */
public class MethodExpressionValidator implements Validator<Object>, PartialStateHolder
{

    private MethodExpression methodExpression;

    private boolean isTransient = false;

    private boolean initialStateMarked = false;

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

    /**
     * Restores the <code>MethodExpression</code>. A <code>null</code> state is ignored, as returned by
     * {@link #saveState(FacesContext)} while the initial state is marked.
     */
    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Assert.notNull(context, "context");

        if (state == null)
        {
            return;
        }
        methodExpression = (MethodExpression) state;
    }

    /**
     * Returns <code>null</code> while {@link #initialStateMarked()} returns <code>true</code>. Otherwise the
     * <code>MethodExpression</code> is saved.
     */
    @Override
    public Object saveState(FacesContext context)
    {
        Assert.notNull(context, "context");

        if (initialStateMarked)
        {
            return null;
        }
        return methodExpression;
    }

    @Override
    public void markInitialState()
    {
        initialStateMarked = true;
    }

    @Override
    public boolean initialStateMarked()
    {
        return initialStateMarked;
    }

    @Override
    public void clearInitialState()
    {
        initialStateMarked = false;
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
