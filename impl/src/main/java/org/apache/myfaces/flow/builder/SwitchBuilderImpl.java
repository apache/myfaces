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
import jakarta.faces.flow.builder.SwitchBuilder;
import jakarta.faces.flow.builder.SwitchCaseBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.SwitchNodeImpl;
import org.apache.myfaces.view.facelets.el.ELText;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class SwitchBuilderImpl extends SwitchBuilder
{
    private FlowBuilderImpl flowBuilder;
    private FlowImpl facesFlow;
    private SwitchNodeImpl switchNodeImpl;
    private SwitchCaseBuilderImpl lastSwitchCaseBuilderImpl;

    public SwitchBuilderImpl(FlowBuilderImpl flowBuilder, FlowImpl facesFlow, String switchNodeId)
    {
        this.flowBuilder = flowBuilder;
        this.facesFlow = facesFlow;
        this.switchNodeImpl = new SwitchNodeImpl(switchNodeId);
        this.facesFlow.putSwitch(switchNodeId, switchNodeImpl);
        this.lastSwitchCaseBuilderImpl = new SwitchCaseBuilderImpl(
            this.flowBuilder, this.facesFlow, this, this.switchNodeImpl);
    }

    @Override
    public SwitchCaseBuilder switchCase()
    {
        return this.lastSwitchCaseBuilderImpl.switchCase();
    }

    @Override
    public SwitchCaseBuilder defaultOutcome(String outcome)
    {
        if (ELText.isLiteral(outcome))
        {
            this.switchNodeImpl.setDefaultOutcome(outcome);
        }
        else
        {
            this.switchNodeImpl.setDefaultOutcome(flowBuilder.createValueExpression(outcome));
        }
        return lastSwitchCaseBuilderImpl;
    }

    @Override
    public SwitchCaseBuilder defaultOutcome(ValueExpression outcome)
    {
        this.switchNodeImpl.setDefaultOutcome(outcome);
        return lastSwitchCaseBuilderImpl;
    }

    @Override
    public SwitchBuilder markAsStartNode()
    {
        facesFlow.setStartNodeId(switchNodeImpl.getId());
        return this;
    }    
}
