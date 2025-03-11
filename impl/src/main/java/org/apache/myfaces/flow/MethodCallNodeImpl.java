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
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class MethodCallNodeImpl extends MethodCallNode implements Freezable
{
    private String id;
    private MethodExpression methodExpression;
    private ValueExpression outcome;
    
    private List<Parameter> parameters;
    private List<Parameter> unmodifiableParameters;
    
    private boolean initialized;
    
    public MethodCallNodeImpl(String methodCallNodeId)
    {
        this.id = methodCallNodeId;
        this.parameters = new ArrayList<>();
        this.unmodifiableParameters = Collections.unmodifiableList(parameters);
    }

    @Override
    public MethodExpression getMethodExpression()
    {
        return methodExpression;
    }

    @Override
    public ValueExpression getOutcome()
    {
        return outcome;
    }

    @Override
    public List<Parameter> getParameters()
    {
        return unmodifiableParameters;
    }
    
    public void addParameter(Parameter parameter)
    {
        checkInitialized();
        parameters.add(parameter);
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
        
        for (Parameter value : parameters)
        {
            if (value instanceof Freezable freezable)
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

    public void setMethodExpression(MethodExpression methodExpression)
    {
        checkInitialized();
        this.methodExpression = methodExpression;
    }

    public void setOutcome(ValueExpression outcome)
    {
        checkInitialized();
        this.outcome = outcome;
    }

    public void setId(String id)
    {
        checkInitialized();
        this.id = id;
    }
}
