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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.SwitchCase;
import jakarta.faces.flow.SwitchNode;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class SwitchNodeImpl extends SwitchNode implements Freezable
{
    private String defaultOutcome;
    private ValueExpression defaultOutcomeEL;
    private String id;
    
    private List<SwitchCase> cases;
    private List<SwitchCase> unmodifiableCases;

    private boolean initialized;

    public SwitchNodeImpl(String switchNodeId)
    {
        this.id = switchNodeId;
        cases = new ArrayList<>();
        unmodifiableCases = Collections.unmodifiableList(cases);
    }
    
    @Override
    public List<SwitchCase> getCases()
    {
        return unmodifiableCases;
    }
    
    public void addCase(SwitchCase switchCase)
    {
        checkInitialized();
        cases.add(switchCase);
    }

    @Override
    public String getDefaultOutcome(FacesContext context)
    {
        if (defaultOutcomeEL != null)
        {
            return defaultOutcomeEL.getValue(context.getELContext());
        }
        return defaultOutcome;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void freeze()
    {
        initialized = true;
        
        for (SwitchCase switchCase : cases)
        {
            if (switchCase instanceof Freezable freezable)
            {
                freezable.freeze();
            }
        }
    }
    
    private void checkInitialized() throws IllegalStateException
    {
        if (initialized)
        {
            throw new IllegalStateException("Flow is immutable once initialized");
        }
    }
    
    public void setDefaultOutcome(String defaultOutcome)
    {
        checkInitialized();
        this.defaultOutcome = defaultOutcome;
        this.defaultOutcomeEL = null;
    }
    
    public void setDefaultOutcome(ValueExpression defaultOutcome)
    {
        checkInitialized();
        this.defaultOutcomeEL = defaultOutcome;
        this.defaultOutcome = null;
    }

    public void setId(String id)
    {
        checkInitialized();
        this.id = id;
    }
}
