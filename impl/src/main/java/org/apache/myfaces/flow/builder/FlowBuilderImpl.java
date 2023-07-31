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

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.FlowCallBuilder;
import jakarta.faces.flow.builder.MethodCallBuilder;
import jakarta.faces.flow.builder.NavigationCaseBuilder;
import jakarta.faces.flow.builder.ReturnBuilder;
import jakarta.faces.flow.builder.SwitchBuilder;
import jakarta.faces.flow.builder.ViewBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.ParameterImpl;

/**
 *
 * @since 2.2
 * @author Leonardo Uribe
 */
public class FlowBuilderImpl extends FlowBuilder
{
    private static final Class[] EMPTY_PARAMS = new Class[] { };

    private FlowImpl facesFlow;
    private FacesContext facesContext;

    public FlowBuilderImpl()
    {
        facesFlow = new FlowImpl();
    }

    public FlowBuilderImpl(FacesContext context)
    {
        super();
        facesContext = context;
    }

    @Override
    public FlowBuilder id(String definingDocumentId, String id)
    {
        facesFlow.setDefiningDocumentId(definingDocumentId);
        facesFlow.setId(id);
        return this;
    }

    @Override
    public ViewBuilder viewNode(String viewNodeId, String vdlDocumentId)
    {
        return new ViewBuilderImpl(facesFlow, viewNodeId, vdlDocumentId);
    }

    @Override
    public SwitchBuilder switchNode(String switchNodeId)
    {
        return new SwitchBuilderImpl(this, facesFlow, switchNodeId);
    }

    @Override
    public ReturnBuilder returnNode(String returnNodeId)
    {
        return new ReturnBuilderImpl(this, facesFlow, returnNodeId);
    }

    @Override
    public MethodCallBuilder methodCallNode(String methodCallNodeId)
    {
        return new MethodCallBuilderImpl(this, facesFlow, methodCallNodeId);
    }

    @Override
    public FlowCallBuilder flowCallNode(String flowCallNodeId)
    {
        return new FlowCallBuilderImpl(this, facesFlow, flowCallNodeId);
    }

    @Override
    public FlowBuilder initializer(MethodExpression methodExpression)
    {
        facesFlow.setInitializer(methodExpression);
        return this;
    }

    @Override
    public FlowBuilder initializer(String methodExpression)
    {
        facesFlow.setInitializer(createMethodExpression(methodExpression));
        return this;
    }

    @Override
    public FlowBuilder finalizer(MethodExpression methodExpression)
    {
        facesFlow.setFinalizer(methodExpression);
        return this;
    }

    @Override
    public FlowBuilder finalizer(String methodExpression)
    {
        facesFlow.setFinalizer(createMethodExpression(methodExpression));
        return this;
    }

    @Override
    public FlowBuilder inboundParameter(String name, ValueExpression value)
    {
        facesFlow.putInboundParameter(name, new ParameterImpl(name, value));
        return this;
    }

    @Override
    public FlowBuilder inboundParameter(String name, String value)
    {
        facesFlow.putInboundParameter(name, new ParameterImpl(name,
            createValueExpression(value)));
        return this;
    }

    @Override
    public Flow getFlow()
    {
        facesContext = null;
        return facesFlow;
    }

    /**
     * The idea is grab FacesContext just once and then when the flow is returned clear the variable.
     *
     * @return
     */
    FacesContext getFacesContext()
    {
        if (facesContext == null)
        {
            facesContext = FacesContext.getCurrentInstance();
        }
        return facesContext;
    }
    
    public MethodExpression createMethodExpression(String methodExpression)
    {
        FacesContext facesContext = getFacesContext();
        return facesContext.getApplication().getExpressionFactory().createMethodExpression(
            facesContext.getELContext(), methodExpression, null, EMPTY_PARAMS);
    }
    
    public MethodExpression createMethodExpression(String methodExpression, Class[] paramTypes)
    {
        FacesContext facesContext = getFacesContext();
        return facesContext.getApplication().getExpressionFactory().createMethodExpression(
            facesContext.getELContext(), methodExpression, null, paramTypes);
    }
    
    public ValueExpression createValueExpression(String value)
    {
        FacesContext facesContext = getFacesContext();
        return facesContext.getApplication().getExpressionFactory()
            .createValueExpression(facesContext.getELContext(), value, Object.class); 
    }

    @Override
    public NavigationCaseBuilder navigationCase()
    {
        return new NavigationCaseBuilderImpl(this, facesFlow);
    }
}
