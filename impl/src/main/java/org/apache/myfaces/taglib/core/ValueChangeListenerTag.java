/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.taglib.core;

import javax.faces.component.EditableValueHolder;
import javax.faces.event.ValueChangeListener;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ValueChangeListenerTag
        extends GenericListenerTag<EditableValueHolder, ValueChangeListener>
{
    private static final long serialVersionUID = 2155190261951046892L;

    public ValueChangeListenerTag()
    {
        super(EditableValueHolder.class);
    }

    protected void addListener(EditableValueHolder editableValueHolder, ValueChangeListener valueChangeListener)
    {
        editableValueHolder.addValueChangeListener(valueChangeListener);
    }
}
