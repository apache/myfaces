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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.el.ValueExpression;
import jakarta.faces.flow.builder.NavigationCaseBuilder;
import org.apache.myfaces.flow.FlowImpl;
import org.apache.myfaces.flow.NavigationCaseImpl;

/**
 *
 * @author lu4242
 */
public class NavigationCaseBuilderImpl extends NavigationCaseBuilder
{
    private FlowImpl facesFlow;
    private FlowBuilderImpl flowBuilder;
    private NavigationCaseImpl navigationCaseImpl;
    
    public NavigationCaseBuilderImpl(FlowBuilderImpl flowBuilder, FlowImpl facesFlow)
    {
        this.flowBuilder = flowBuilder;
        this.facesFlow = facesFlow;
        this.navigationCaseImpl = new NavigationCaseImpl();
    }
    
    @Override
    public NavigationCaseBuilder fromViewId(String fromViewId)
    {
        // This is the best place to add the navigation case into the flow, because
        // fromViewId is required (cannot be null, and null does not mean '*')
        if (this.navigationCaseImpl.getFromViewId() != null)
        {
            this.facesFlow.removeNavigationCase(navigationCaseImpl);
        }
        if (fromViewId != null)
        {
            this.navigationCaseImpl.setFromViewId(fromViewId);
            this.facesFlow.addNavigationCase(navigationCaseImpl);
        }
        return this;
    }

    @Override
    public NavigationCaseBuilder fromAction(String fromAction)
    {
        this.navigationCaseImpl.setFromAction(fromAction);
        return this;
    }

    @Override
    public NavigationCaseBuilder fromOutcome(String fromOutcome)
    {
        this.navigationCaseImpl.setFromOutcome(fromOutcome);
        return this;
    }

    @Override
    public NavigationCaseBuilder toViewId(String toViewId)
    {
        this.navigationCaseImpl.setToViewId(toViewId);
        return this;
    }

    @Override
    public NavigationCaseBuilder toFlowDocumentId(String toFlowDocumentId) 
    {
        this.navigationCaseImpl.setToFlowDocumentId(toFlowDocumentId);
        return this;
    }

    @Override
    public NavigationCaseBuilder condition(String condition)
    {
        this.navigationCaseImpl.setConditionExpression(null);
        this.navigationCaseImpl.setCondition(condition);
        return this;
    }

    @Override
    public NavigationCaseBuilder condition(ValueExpression condition)
    {
        this.navigationCaseImpl.setCondition(null);
        this.navigationCaseImpl.setConditionExpression(condition);
        return this;
    }

    @Override
    public RedirectBuilder redirect()
    {
        this.navigationCaseImpl.setRedirect(true);
        return new RedirectBuilderImpl();
    }
    
    public class RedirectBuilderImpl extends RedirectBuilder
    {
        @Override
        public RedirectBuilder parameter(String name, String value)
        {
            Map<String, List<String>> map = navigationCaseImpl.getParameters();
            if (map == null)
            {
                map = new HashMap<>(3, 1f);
                navigationCaseImpl.setParameters(map);
            }
            
            List<String> values = map.computeIfAbsent(name, k -> new ArrayList<>());
            values.add(value);

            return this;
        }

        @Override
        public RedirectBuilder includeViewParams()
        {
            navigationCaseImpl.setIncludeViewParams(true);
            return this;
        }
    }
}
