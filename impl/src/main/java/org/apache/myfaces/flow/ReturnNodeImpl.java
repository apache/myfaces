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
import jakarta.faces.flow.ReturnNode;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class ReturnNodeImpl extends ReturnNode implements Freezable
{
    private String fromOutcome;
    private ValueExpression fromOutcomeEL;
    private String id;

    private boolean initialized;

    public ReturnNodeImpl(String returnNodeId)
    {
        this.id = returnNodeId;
    }
    
    @Override
    public String getFromOutcome(FacesContext context)
    {
        if (fromOutcomeEL != null)
        {
            return fromOutcomeEL.getValue(context.getELContext());
        }
        return fromOutcome;
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
    }
    
    private void checkInitialized() throws IllegalStateException
    {
        if (initialized)
        {
            throw new IllegalStateException("Flow is inmutable once initialized");
        }
    }

    public void setFromOutcome(String fromOutcome)
    {
        checkInitialized();
        this.fromOutcome = fromOutcome;
        this.fromOutcomeEL = null;
    }
    
    public void setFromOutcome(ValueExpression fromOutcome)
    {
        checkInitialized();
        this.fromOutcomeEL = fromOutcome;
        this.fromOutcome = null;
    }

    public void setId(String id)
    {
        checkInitialized();
        this.id = id;
    }
}
