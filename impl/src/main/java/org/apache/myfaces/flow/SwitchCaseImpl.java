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
package org.apache.myfaces.flow;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.SwitchCase;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class SwitchCaseImpl extends SwitchCase implements Freezable
{
    private String fromOutcome;
    
    private Boolean condition;
    private ValueExpression conditionEL;

    private boolean initialized;

    @Override
    public String getFromOutcome()
    {
        return fromOutcome;
    }

    @Override
    public Boolean getCondition(FacesContext context)
    {
        if (conditionEL != null)
        {
            Object value = conditionEL.getValue(context.getELContext());
            if (value instanceof String string)
            {
                return Boolean.valueOf(string);
            }
            return (Boolean) value;
        }
        return condition;
    }
    
    @Override
    public void freeze()
    {
        initialized = true;
    }
    
    private void checkInitialized() throws IllegalStateException
    {
        if (initialized)
        {
            throw new IllegalStateException("Flow is immutable once initialized");
        }
    }

    public void setFromOutcome(String fromOutcome)
    {
        checkInitialized();
        this.fromOutcome = fromOutcome;
    }

    public void setCondition(Boolean condition)
    {
        checkInitialized();
        this.condition = condition;
        this.conditionEL = null;
    }

    public void setCondition(ValueExpression conditionEL)
    {
        checkInitialized();
        this.conditionEL = conditionEL;
        this.condition = null;
    }
}
