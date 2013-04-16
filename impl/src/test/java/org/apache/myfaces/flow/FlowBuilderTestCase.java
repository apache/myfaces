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

import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowCallNode;
import javax.faces.flow.MethodCallNode;
import javax.faces.flow.Parameter;
import javax.faces.flow.ReturnNode;
import javax.faces.flow.SwitchCase;
import javax.faces.flow.SwitchNode;
import javax.faces.flow.ViewNode;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.SwitchCaseBuilder;
import org.apache.myfaces.flow.builder.FlowBuilderImpl;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author lu4242
 */
public class FlowBuilderTestCase extends AbstractJsfTestCase
{
    
    @Test
    public void testFlowBuilderSimple()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1").
            initializer("#{bean.init}").finalizer("#{bean.destroy}");
        
        Flow flow = flowBuilder.getFlow();
        Assert.assertNotNull(flow);
        Assert.assertEquals("flow1", flow.getId());
        Assert.assertEquals("faces-flow1.xhtml", flow.getDefiningDocumentId());
        Assert.assertEquals("#{bean.init}", flow.getInitializer().getExpressionString());
        Assert.assertEquals("#{bean.destroy}", flow.getFinalizer().getExpressionString());
    }
    
    @Test
    public void testFlowBuilderReturn()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");
        flowBuilder.returnNode("returnNode").markAsStartNode().fromOutcome("mynode");
        
        Flow flow = flowBuilder.getFlow();
        Assert.assertNotNull(flow);
        Assert.assertEquals("returnNode", flow.getStartNodeId());
        
        ReturnNode returnNode = flow.getReturns().get("returnNode");
        Assert.assertNotNull(returnNode);
        Assert.assertEquals("returnNode", returnNode.getId());
        Assert.assertEquals("mynode", returnNode.getFromOutcome(facesContext));
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
        Assert.assertNotNull(flow);
        Assert.assertEquals("switch1", flow.getStartNodeId());
        
        SwitchNode switchNode = flow.getSwitches().get("switch1");
        Assert.assertNotNull(switchNode);
        Assert.assertEquals("exit", switchNode.getDefaultOutcome(facesContext));
        
        SwitchCase scn = switchNode.getCases().get(0);
        Assert.assertTrue(scn.getCondition(facesContext));
        Assert.assertEquals("case1", scn.getFromOutcome());
        
        scn = switchNode.getCases().get(1);
        Assert.assertTrue(scn.getCondition(facesContext));
        Assert.assertEquals("caseB", scn.getFromOutcome());

        scn = switchNode.getCases().get(2);
        Assert.assertTrue(scn.getCondition(facesContext));
        Assert.assertEquals("caseC", scn.getFromOutcome());
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
        Assert.assertNotNull(flow);
        Assert.assertEquals("y", flow.getStartNodeId());
        
        ViewNode viewNode = flow.getViews().get(0);
        Assert.assertEquals("x", viewNode.getId());
        Assert.assertEquals("x.xhtml", viewNode.getVdlDocumentId());
        
        viewNode = flow.getViews().get(1);
        Assert.assertEquals("y", viewNode.getId());
        Assert.assertEquals("y.xhtml", viewNode.getVdlDocumentId());
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
        Assert.assertNotNull(flow);

        Parameter param = flow.getInboundParameters().get("name1");
        Assert.assertEquals("name1", param.getName());
        Assert.assertEquals("value1", param.getValue().getValue(facesContext.getELContext()));
        
        param = flow.getInboundParameters().get("name2");
        Assert.assertEquals("name2", param.getName());
        Assert.assertEquals("asdf", param.getValue().getValue(facesContext.getELContext()));        
        
        param = flow.getInboundParameters().get("name3");
        Assert.assertEquals("name3", param.getName());
        Assert.assertEquals("value3", param.getValue().getValue(facesContext.getELContext()));        
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
        Assert.assertNotNull(flow);
        Assert.assertEquals("method1", flow.getStartNodeId());
        
        MethodCallNode mcn = flow.getMethodCalls().get(0);
        Assert.assertEquals("method1", mcn.getId());
        Assert.assertEquals("case1", mcn.getOutcome().getValue(facesContext.getELContext()));
        Assert.assertEquals("#{bean.check}", mcn.getMethodExpression().getExpressionString());
        
        mcn = flow.getMethodCalls().get(1);
        Assert.assertEquals("method2", mcn.getId());
        Assert.assertEquals("case2", mcn.getOutcome().getValue(facesContext.getELContext()));
        Assert.assertEquals("#{bean.checkTo}", mcn.getMethodExpression().getExpressionString());
    }
    
    @Test
    public void testFlowBuilderFlowCall()
    {
        FlowBuilder flowBuilder = new FlowBuilderImpl();
        flowBuilder.id("faces-flow1.xhtml", "flow1");

        flowBuilder.flowCallNode("goToFlow2").outboundParameter("name1", "value1").
            flowReference("faces-flow2.xhtml", "flow2").markAsStartNode();
        
        Flow flow = flowBuilder.getFlow();
        Assert.assertNotNull(flow);
        Assert.assertEquals("goToFlow2", flow.getStartNodeId());
        
        FlowCallNode flowCallNode = flow.getFlowCalls().get("goToFlow2");
        Assert.assertNotNull(flowCallNode);
        Assert.assertEquals("flow2", flowCallNode.getCalledFlowId(facesContext));
        Assert.assertEquals("faces-flow2.xhtml", flowCallNode.getCalledFlowDocumentId(facesContext));
        
        Parameter param = flowCallNode.getOutboundParameters().get("name1");
        Assert.assertEquals("name1", param.getName());
        Assert.assertEquals("value1", param.getValue().getValue(facesContext.getELContext()));
    }

}
