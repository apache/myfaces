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
package org.apache.myfaces.flow.builder;

import jakarta.el.ValueExpression;
import jakarta.faces.flow.builder.SwitchCaseBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.SwitchCaseImpl;
import org.apache.myfaces.flow.SwitchNodeImpl;
import org.apache.myfaces.view.facelets.el.ELText;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class SwitchCaseBuilderImpl extends SwitchCaseBuilder
{
    private FlowBuilderImpl flowBuilder;
    private FlowImpl facesFlow;
    private SwitchBuilderImpl switchBuilderImpl;
    private SwitchNodeImpl switchNodeImpl;
    private SwitchCaseImpl switchCase;

    public SwitchCaseBuilderImpl(FlowBuilderImpl flowBuilder, FlowImpl facesFlow, 
        SwitchBuilderImpl switchBuilderImpl, SwitchNodeImpl switchNode)
    {
        this.flowBuilder = flowBuilder;
        this.facesFlow = facesFlow;
        this.switchBuilderImpl = switchBuilderImpl;
        this.switchNodeImpl = switchNode;
        this.switchCase = null;
    }
    
    @Override
    public SwitchCaseBuilder condition(String expression)
    {
        if (ELText.isLiteral(expression))
        {
            this.switchCase.setCondition(Boolean.valueOf(expression));
        }
        else
        {
            this.switchCase.setCondition(flowBuilder.createValueExpression(expression));
        }
        return this;
    }

    @Override
    public SwitchCaseBuilder condition(ValueExpression expression)
    {
        this.switchCase.setCondition(expression);
        return this;
    }

    @Override
    public SwitchCaseBuilder fromOutcome(String outcome)
    {
        this.switchCase.setFromOutcome(outcome);
        return this;
    }

    @Override
    public SwitchCaseBuilder switchCase()
    {
        this.switchCase =  new SwitchCaseImpl();
        this.switchNodeImpl.addCase(this.switchCase);
        return this;
    }
}
