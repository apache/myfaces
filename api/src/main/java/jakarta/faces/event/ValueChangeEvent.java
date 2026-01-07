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
package jakarta.faces.event;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public class ValueChangeEvent<T> extends FacesEvent
{
    private static final long serialVersionUID = -2490528664421353795L;

    private T _oldValue;
    private T _newValue;

    public ValueChangeEvent(FacesContext facesContext, UIComponent uiComponent, T oldValue, T newValue)
    {
        super(facesContext, uiComponent);
        if (uiComponent == null)
        {
            throw new IllegalArgumentException("uiComponent");
        }
        _oldValue = oldValue;
        _newValue = newValue;
    }

    public ValueChangeEvent(UIComponent uiComponent, T oldValue, T newValue)
    {
        super(uiComponent);
        if (uiComponent == null)
        {
            throw new IllegalArgumentException("uiComponent");
        }
        _oldValue = oldValue;
        _newValue = newValue;
    }

    public T getNewValue()
    {
        return _newValue;
    }

    public T getOldValue()
    {
        return _oldValue;
    }

    @Override
    public boolean isAppropriateListener(FacesListener facesListeners)
    {
        return facesListeners instanceof ValueChangeListener;
    }

    @Override
    public void processListener(FacesListener facesListeners)
    {
        ((ValueChangeListener) facesListeners).processValueChange(this);
    }
}
