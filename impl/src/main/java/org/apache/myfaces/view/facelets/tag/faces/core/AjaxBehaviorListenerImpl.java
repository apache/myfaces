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
package org.apache.myfaces.view.facelets.tag.faces.core;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.AjaxBehaviorListener;

/**
 * Wraps a method expression in a AjaxBehaviorListener.
 *
 * The listener attribute can either point to a method taking an {@link AjaxBehaviorEvent} or
 * to a method taking no arguments. The method taking the event is invoked first; only if that throws a
 * {@link MethodNotFoundException}, the method taking no arguments is invoked.
 */
public final class AjaxBehaviorListenerImpl implements AjaxBehaviorListener, PartialStateHolder
{
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private MethodExpression _exprOneArg;
    private MethodExpression _exprZeroArg;
    private boolean _transient;
    private boolean _initialStateMarked;

    public AjaxBehaviorListenerImpl ()
    {
    }

    public AjaxBehaviorListenerImpl(MethodExpression exprOneArg)
    {
        this(exprOneArg, null);
    }

    public AjaxBehaviorListenerImpl(MethodExpression exprOneArg, MethodExpression exprZeroArg)
    {
        _exprOneArg = exprOneArg;
        _exprZeroArg = exprZeroArg;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event) throws AbortProcessingException
    {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try
        {
            // first try to invoke the MethodExpression with one argument
            _exprOneArg.invoke(elContext, new Object[] { event });
        }
        catch (MethodNotFoundException mnfeOneArg)
        {
            if (_exprZeroArg == null)
            {
                throw mnfeOneArg;
            }

            try
            {
                // if that fails try to invoke the MethodExpression with zero arguments
                _exprZeroArg.invoke(elContext, EMPTY_OBJECT_ARRAY);
            }
            catch (MethodNotFoundException mnfeZeroArg)
            {
                // if that fails too rethrow the original MethodNotFoundException
                throw mnfeOneArg;
            }
        }
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Object[] values)
        {
            _exprOneArg = (MethodExpression) values[0];
            _exprZeroArg = (MethodExpression) values[1];
        }
        else
        {
            _exprOneArg = (MethodExpression) state;
            _exprZeroArg = null;
        }
    }

    @Override
    public Object saveState(FacesContext context)
    {
        if (initialStateMarked())
        {
            return null;
        }
        return new Object[] { _exprOneArg, _exprZeroArg };
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        _transient = newTransientValue;
    }

    @Override
    public void clearInitialState()
    {
        _initialStateMarked = false;
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
    }
}
