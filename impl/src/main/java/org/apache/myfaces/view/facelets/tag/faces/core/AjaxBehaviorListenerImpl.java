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

import jakarta.el.MethodExpression;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.AjaxBehaviorListener;

/**
 * Wraps a method expression in a AjaxBehaviorListener
 */
public final class AjaxBehaviorListenerImpl implements AjaxBehaviorListener, PartialStateHolder
{
    private MethodExpression _expr;
    private boolean _transient;
    private boolean _initialStateMarked;

    public AjaxBehaviorListenerImpl ()
    {
    }

    public AjaxBehaviorListenerImpl(MethodExpression expr)
    {
        _expr = expr;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event) throws AbortProcessingException
    {
        _expr.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] { event });
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
        _expr = (MethodExpression) state;
    }

    @Override
    public Object saveState(FacesContext context)
    {
        if (initialStateMarked())
        {
            return null;
        }
        return _expr;
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
