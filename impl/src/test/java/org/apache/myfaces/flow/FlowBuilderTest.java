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

import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.ReturnNode;
import jakarta.faces.flow.SwitchCase;
import jakarta.faces.flow.SwitchNode;
import jakarta.faces.flow.ViewNode;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.SwitchCaseBuilder;
import org.apache.myfaces.flow.builder.FlowBuilderImpl;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author lu4242
 */
public class FlowBuilderTest extends AbstractFacesTestCase
{
    
    @Test
    public void testFlowBuilderSimple()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1").
            initializer("#{bean.init}").finalizer("#{bean.destroy}");
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("flow1", flow.getId());
        Assertions.assertEquals("faces-flow1.xhtml", flow.getDefiningDocumentId());
        Assertions.assertEquals("#{bean.init}", flow.getInitializer().getExpressionString());
        Assertions.assertEquals("#{bean.destroy}", flow.getFinalizer().getExpressionString());
    }
    
    @Test
    public void testFlowBuilderReturn()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        flowBuilder.returnNode("returnNode").markAsStartNode().fromOutcome("mynode");
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("returnNode", flow.getStartNodeId());
        
        ReturnNode returnNode = flow.getReturns().get("returnNode");
        Assertions.assertNotNull(returnNode);
        Assertions.assertEquals("returnNode", returnNode.getId());
        Assertions.assertEquals("mynode", returnNode.getFromOutcome(facesContext));
    }    

    @Test
    public void testFlowBuilderSwitch()
    {
        externalContext.getRequestMap().put("bean", new SimpleBean());
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        SwitchCaseBuilder switchCaseBuilder = flowBuilder.switchNode("switch1")
            .markAsStartNode().defaultOutcome("#{bean.outcome1}");
        switchCaseBuilder
            .switchCase().condition("true").fromOutcome("case1");
        switchCaseBuilder
            .switchCase().condition("#{bean.checkCond}").fromOutcome("caseB");
        switchCaseBuilder
            .switchCase().condition(
                application.getExpressionFactory().createValueExpression(Boolean.TRUE, Boolean.class))
                    .fromOutcome("caseC");
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("switch1", flow.getStartNodeId());
        
        SwitchNode switchNode = flow.getSwitches().get("switch1");
        Assertions.assertNotNull(switchNode);
        Assertions.assertEquals("exit", switchNode.getDefaultOutcome(facesContext));
        
        SwitchCase scn = switchNode.getCases().get(0);
        Assertions.assertTrue(scn.getCondition(facesContext));
        Assertions.assertEquals("case1", scn.getFromOutcome());
        
        scn = switchNode.getCases().get(1);
        Assertions.assertTrue(scn.getCondition(facesContext));
        Assertions.assertEquals("caseB", scn.getFromOutcome());

        scn = switchNode.getCases().get(2);
        Assertions.assertTrue(scn.getCondition(facesContext));
        Assertions.assertEquals("caseC", scn.getFromOutcome());
    }

    public static class SimpleBean {
        public boolean isCheckCond()
        {
            return true;
        }
        public String getOutcome1()
        {
            return "exit";
        }
        public String getValue()
        {
            return "asdf";
        }
        public void check()
        {
        }
        public void checkTo(FacesContext context)
        {
        }
    }
    
    @Test
    public void testFlowBuilderView()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        flowBuilder.viewNode("x", "x.xhtml");
        flowBuilder.viewNode("y", "y.xhtml").markAsStartNode();
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("y", flow.getStartNodeId());
        
        ViewNode viewNode = flow.getViews().get(0);
        Assertions.assertEquals("x", viewNode.getId());
        Assertions.assertEquals("x.xhtml", viewNode.getVdlDocumentId());
        
        viewNode = flow.getViews().get(1);
        Assertions.assertEquals("y", viewNode.getId());
        Assertions.assertEquals("y.xhtml", viewNode.getVdlDocumentId());
    }
    
    @Test
    public void testFlowBuilderInboundParameter()
    {
        externalContext.getRequestMap().put("bean", new SimpleBean());
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        flowBuilder.inboundParameter("name1", "value1");
        flowBuilder.inboundParameter("name2", "#{bean.value}");
        flowBuilder.inboundParameter("name3", 
            application.getExpressionFactory().createValueExpression("value3", String.class));
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);

        Parameter param = flow.getInboundParameters().get("name1");
        Assertions.assertEquals("name1", param.getName());
        Assertions.assertEquals("value1", param.getValue().getValue(facesContext.getELContext()));
        
        param = flow.getInboundParameters().get("name2");
        Assertions.assertEquals("name2", param.getName());
        Assertions.assertEquals("asdf", param.getValue().getValue(facesContext.getELContext()));        
        
        param = flow.getInboundParameters().get("name3");
        Assertions.assertEquals("name3", param.getName());
        Assertions.assertEquals("value3", param.getValue().getValue(facesContext.getELContext()));        
    }

    @Test
    public void testFlowBuilderMethodCall()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        flowBuilder.methodCallNode("method1").expression("#{bean.check}").defaultOutcome("case1").markAsStartNode();
        flowBuilder.methodCallNode("method2").expression("#{bean.checkTo}",
            new Class[]{FacesContext.class}).defaultOutcome("case2");
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("method1", flow.getStartNodeId());
        
        MethodCallNode mcn = flow.getMethodCalls().get(0);
        Assertions.assertEquals("method1", mcn.getId());
        Assertions.assertEquals("case1", mcn.getOutcome().getValue(facesContext.getELContext()));
        Assertions.assertEquals("#{bean.check}", mcn.getMethodExpression().getExpressionString());
        
        mcn = flow.getMethodCalls().get(1);
        Assertions.assertEquals("method2", mcn.getId());
        Assertions.assertEquals("case2", mcn.getOutcome().getValue(facesContext.getELContext()));
        Assertions.assertEquals("#{bean.checkTo}", mcn.getMethodExpression().getExpressionString());
    }
    
    @Test
    public void testFlowBuilderFlowCall()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");

        flowBuilder.flowCallNode("goToFlow2").outboundParameter("name1", "value1").
            flowReference("faces-flow2.xhtml", "flow2").markAsStartNode();
        
        Flow flow = flowBuilder.getFlow();
        Assertions.assertNotNull(flow);
        Assertions.assertEquals("goToFlow2", flow.getStartNodeId());
        
        FlowCallNode flowCallNode = flow.getFlowCalls().get("goToFlow2");
        Assertions.assertNotNull(flowCallNode);
        Assertions.assertEquals("flow2", flowCallNode.getCalledFlowId(facesContext));
        Assertions.assertEquals("faces-flow2.xhtml", flowCallNode.getCalledFlowDocumentId(facesContext));
        
        Parameter param = flowCallNode.getOutboundParameters().get("name1");
        Assertions.assertEquals("name1", param.getName());
        Assertions.assertEquals("value1", param.getValue().getValue(facesContext.getELContext()));
    }

}
