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
package org.apache.myfaces.core.api.shared;

import jakarta.faces.component.EditableValueHolder;
import java.io.Serializable;

public class EditableValueHolderState implements Serializable
{
    public static final EditableValueHolderState EMPTY = new ImmutableEditableValueHolderState();

    private static final long serialVersionUID = 2920252657338389849L;

    private boolean localValueSet;
    private Object submittedValue;
    private boolean valid;
    private Object localValue;

    public EditableValueHolderState()
    {
        this.localValueSet = false;
        this.valid = true;
    }        

    public EditableValueHolderState(EditableValueHolder evh)
    {
        this();
        saveState(evh);
    }        

    public boolean isLocalValueSet() 
    {
        return localValueSet;
    }

    public Object getSubmittedValue()
    {
        return submittedValue;
    }

    public boolean isValid()
    {
        return valid;
    }

    public Object getLocalValue()
    {
        return localValue;
    }

    @Override
    public String toString()
    {
        return "EditableValueHolderState{"
                + "localValueSet=" + localValueSet
                + ", submittedValue=" + submittedValue
                + ", valid=" + valid
                + ", localValue=" + localValue + '}';
    }

    public void saveState(EditableValueHolder evh)
    {
        this.localValue = evh.getLocalValue();
        this.localValueSet = evh.isLocalValueSet();
        this.valid = evh.isValid();
        this.submittedValue = evh.getSubmittedValue();
    }

    public void restoreState(EditableValueHolder evh)
    {
        // UIInput.setValue always calls setLocalValueSet(true) per spec, even when value is null.
        // When the snapshot has localValueSet=false that forces a transient-state add followed
        // immediately by a remove — wasteful in tight UIRepeat loops.
        //
        // Optimisation: skip setValue entirely when both the snapshot localValue and the
        // component's current localValue are null — the StateHelper is already in the correct
        // state and no write is needed. This is the common case in APPLY_REQUEST_VALUES and
        // UPDATE_MODEL_VALUES where no conversion has happened yet.
        // When either side is non-null, the normal setValue path is required to push/clear the
        // value through the StateHelper.
        if (localValue != null || evh.getLocalValue() != null)
        {
            evh.setValue(localValue);
        }
        evh.setLocalValueSet(localValueSet);
        evh.setValid(valid);
        evh.setSubmittedValue(submittedValue);
    }

    public static EditableValueHolderState create(EditableValueHolder evh)
    {
        if (isEmpty(evh))
        {
            return null;
        }

        return new EditableValueHolderState(evh);
    }

    public static boolean isEmpty(EditableValueHolder evh)
    {
        // Check transient values first
        return evh.isValid()
                && !evh.isLocalValueSet()
                && evh.getSubmittedValue() == null
                && evh.getLocalValue() == null;
    }

    public static class ImmutableEditableValueHolderState extends EditableValueHolderState
    {
        @Override
        public void saveState(EditableValueHolder evh)
        {
            throw new UnsupportedOperationException();
        }
    }
}
