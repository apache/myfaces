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
import jakarta.faces.flow.builder.ReturnBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.ReturnNodeImpl;
import org.apache.myfaces.view.facelets.el.ELText;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class ReturnBuilderImpl extends ReturnBuilder
{
    private FlowBuilderImpl flowBuilder;
    private FlowImpl facesFlow;
    private ReturnNodeImpl returnNode;

    public ReturnBuilderImpl(FlowBuilderImpl flowBuilder, FlowImpl facesFlow, String returnNodeId)
    {
        this.flowBuilder = flowBuilder;
        this.facesFlow = facesFlow;
        this.returnNode = new ReturnNodeImpl(returnNodeId);
        this.facesFlow.putReturn(returnNodeId, returnNode);
    }

    @Override
    public ReturnBuilder fromOutcome(String outcome)
    {
        if (ELText.isLiteral(outcome))
        {
            this.returnNode.setFromOutcome(outcome);
        }
        else
        {
            this.returnNode.setFromOutcome(flowBuilder.createValueExpression(outcome));
        }
        return this;
    }

    @Override
    public ReturnBuilder fromOutcome(ValueExpression outcome)
    {
        this.returnNode.setFromOutcome(outcome);
        return this;
    }

    @Override
    public ReturnBuilder markAsStartNode()
    {
        facesFlow.setStartNodeId(returnNode.getId());
        return this;
    }
    
}
