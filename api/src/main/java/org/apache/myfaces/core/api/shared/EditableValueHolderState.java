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
        this.valid = evh.isValid();
        this.submittedValue = evh.getSubmittedValue();
        this.localValueSet = evh.isLocalValueSet();
    }

    public void restoreState(EditableValueHolder evh)
    {
        evh.setValue(localValue);
        evh.setValid(valid);
        evh.setSubmittedValue(submittedValue);
        evh.setLocalValueSet(localValueSet);
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
        return evh.getLocalValue() == null
                && evh.isValid()
                && evh.getSubmittedValue() == null;
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
