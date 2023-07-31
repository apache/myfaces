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
import jakarta.faces.flow.builder.FlowCallBuilder;
import org.apache.myfaces.flow.FlowCallNodeImpl;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.ParameterImpl;
import org.apache.myfaces.view.facelets.el.ELText;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowCallBuilderImpl extends FlowCallBuilder
{
    private FlowBuilderImpl flowBuilder;
    private FlowImpl facesFlow;
    private FlowCallNodeImpl flowCallNode;

    public FlowCallBuilderImpl(FlowBuilderImpl flowBuilder, FlowImpl facesFlow, String flowCallNodeId)
    {
        this.flowBuilder = flowBuilder;
        this.facesFlow = facesFlow;
        this.flowCallNode = new FlowCallNodeImpl(flowCallNodeId);
        this.facesFlow.putFlowCall(flowCallNodeId, flowCallNode);
    }

    @Override
    public FlowCallBuilder flowReference(String flowDocumentId, String flowId)
    {
        if (ELText.isLiteral(flowDocumentId))
        {
            this.flowCallNode.setCalledFlowDocumentId(flowDocumentId);
        }
        else
        {
            this.flowCallNode.setCalledFlowDocumentId(flowBuilder.createValueExpression(flowDocumentId));
        }
        if (ELText.isLiteral(flowId))
        {
            this.flowCallNode.setCalledFlowId(flowId);
        }
        else
        {
            this.flowCallNode.setCalledFlowId(flowBuilder.createValueExpression(flowId));
        }
        return this;
    }

    @Override
    public FlowCallBuilder outboundParameter(String name, ValueExpression value)
    {
        this.flowCallNode.putOutboundParameter(name, new ParameterImpl(name, value));
        return this;
    }

    @Override
    public FlowCallBuilder outboundParameter(String name, String value)
    {
        this.flowCallNode.putOutboundParameter(name, new ParameterImpl(name, 
            this.flowBuilder.createValueExpression(value)));
        return this;
    }

    @Override
    public FlowCallBuilder markAsStartNode()
    {
        facesFlow.setStartNodeId(flowCallNode.getId());
        return this;
    }
    
}
