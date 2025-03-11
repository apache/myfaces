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

import java.util.List;
import java.util.Map;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author lu4242
 */
public class NavigationCaseImpl extends NavigationCase implements Freezable
{
    private String condition;
    private String fromAction;
    private String fromOutcome;
    private String fromViewId;
    private String toViewId;
    private String toFlowDocumentId;
    private boolean includeViewParams;
    private boolean redirect;
    private Map<String, List<String>> parameters;
    private ValueExpression conditionExpression;
    private ValueExpression toViewIdExpression;

    private boolean initialized;

    public NavigationCaseImpl()
    {
        super(null, null, null, null, null, null, false, false);
    }

    public NavigationCaseImpl(String fromViewId, String fromAction, String fromOutcome, String condition, 
            String toViewId,
            Map<String, List<String>> parameters, boolean redirect, boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, parameters, redirect, includeViewParams);
        this.condition = condition;
        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.toViewId = toViewId;
        this.toFlowDocumentId = null;
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;
        this.parameters = parameters;
    }

    public NavigationCaseImpl(String fromViewId, String fromAction, String fromOutcome, String condition, 
            String toViewId,
            String toFlowDocumentId, Map<String, List<String>> parameters, boolean redirect,
            boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, parameters, redirect, 
                includeViewParams);
        this.condition = condition;
        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.toViewId = toViewId;
        this.toFlowDocumentId = toFlowDocumentId;
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;
        this.parameters = parameters;

    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        checkInitialized();
        this.condition = condition;
    }

    @Override
    public String getFromAction()
    {
        return fromAction;
    }

    public void setFromAction(String fromAction)
    {
        checkInitialized();
        this.fromAction = fromAction;
    }

    @Override
    public String getFromOutcome()
    {
        return fromOutcome;
    }

    public void setFromOutcome(String fromOutcome)
    {
        checkInitialized();
        this.fromOutcome = fromOutcome;
    }

    @Override
    public String getFromViewId()
    {
        return fromViewId;
    }

    public void setFromViewId(String fromViewId)
    {
        checkInitialized();
        this.fromViewId = fromViewId;
    }

    public String getToViewId()
    {
        return toViewId;
    }

    public void setToViewId(String toViewId)
    {
        checkInitialized();
        this.toViewId = toViewId;
    }

    @Override
    public String getToFlowDocumentId()
    {
        return toFlowDocumentId;
    }

    public void setToFlowDocumentId(String toFlowDocumentId)
    {
        checkInitialized();
        this.toFlowDocumentId = toFlowDocumentId;
    }

    @Override
    public boolean isIncludeViewParams()
    {
        return includeViewParams;
    }

    public void setIncludeViewParams(boolean includeViewParams)
    {
        checkInitialized();
        this.includeViewParams = includeViewParams;
    }

    @Override
    public boolean isRedirect()
    {
        return redirect;
    }

    public void setRedirect(boolean redirect)
    {
        checkInitialized();
        this.redirect = redirect;
    }
    
    @Override
    public Map<String, List<String>> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, List<String>> parameters)
    {
        checkInitialized();
        this.parameters = parameters;
    }

    public ValueExpression getConditionExpression()
    {
        return conditionExpression;
    }

    public void setConditionExpression(ValueExpression conditionExpression)
    {
        checkInitialized();
        this.conditionExpression = conditionExpression;
    }

    public ValueExpression getToViewIdExpression()
    {
        return toViewIdExpression;
    }

    public void setToViewIdExpression(ValueExpression toViewIdExpression)
    {
        checkInitialized();
        this.toViewIdExpression = toViewIdExpression;
    }

    @Override
    public Boolean getCondition(FacesContext context)
    {
        if (condition == null)
        {
            return null;
        }

        ValueExpression expression = _getConditionExpression(context);

        return Boolean.TRUE.equals(expression.getValue(context.getELContext()));
    }

    private ValueExpression _getConditionExpression(FacesContext context)
    {
        assert condition != null;

        if (conditionExpression == null)
        {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            conditionExpression = factory.createValueExpression(context.getELContext(), condition, Boolean.class);
        }

        return conditionExpression;
    }

    @Override
    public String getToViewId(FacesContext context)
    {
        if (toViewId == null)
        {
            return null;
        }

        ValueExpression expression = _getToViewIdExpression(context);

        return (expression.isLiteralText())
                ? expression.getExpressionString() : expression.getValue(context.getELContext());
    }

    private ValueExpression _getToViewIdExpression(FacesContext context)
    {
        assert toViewId != null;

        if (toViewIdExpression == null)
        {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            toViewIdExpression = factory.createValueExpression(context.getELContext(), toViewId, String.class);
        }

        return toViewIdExpression;
    }

    @Override
    public boolean hasCondition()
    {
        return condition != null && condition.length() > 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 47 * hash + (this.condition != null ? this.condition.hashCode() : 0);
        hash = 47 * hash + (this.fromAction != null ? this.fromAction.hashCode() : 0);
        hash = 47 * hash + (this.fromOutcome != null ? this.fromOutcome.hashCode() : 0);
        hash = 47 * hash + (this.fromViewId != null ? this.fromViewId.hashCode() : 0);
        hash = 47 * hash + (this.toViewId != null ? this.toViewId.hashCode() : 0);
        hash = 47 * hash + (this.toFlowDocumentId != null ? this.toFlowDocumentId.hashCode() : 0);
        hash = 47 * hash + (this.includeViewParams ? 1 : 0);
        hash = 47 * hash + (this.redirect ? 1 : 0);
        hash = 47 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        hash = 47 * hash + (this.conditionExpression != null ? this.conditionExpression.hashCode() : 0);
        hash = 47 * hash + (this.toViewIdExpression != null ? this.toViewIdExpression.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final NavigationCaseImpl other = (NavigationCaseImpl) obj;
        if ((this.condition == null) ? (other.condition != null) : !this.condition.equals(other.condition))
        {
            return false;
        }
        if ((this.fromAction == null) ? (other.fromAction != null) : !this.fromAction.equals(other.fromAction))
        {
            return false;
        }
        if ((this.fromOutcome == null) ? (other.fromOutcome != null) : 
                !this.fromOutcome.equals(other.fromOutcome))
        {
            return false;
        }
        if ((this.fromViewId == null) ? (other.fromViewId != null) : !this.fromViewId.equals(other.fromViewId))
        {
            return false;
        }
        if ((this.toViewId == null) ? (other.toViewId != null) : !this.toViewId.equals(other.toViewId))
        {
            return false;
        }
        if ((this.toFlowDocumentId == null) ? (other.toFlowDocumentId != null) : 
                !this.toFlowDocumentId.equals(other.toFlowDocumentId))
        {
            return false;
        }
        if (this.includeViewParams != other.includeViewParams)
        {
            return false;
        }
        if (this.redirect != other.redirect)
        {
            return false;
        }
        if (this.parameters != other.parameters && (this.parameters == null || 
                !this.parameters.equals(other.parameters)))
        {
            return false;
        }
        if (this.conditionExpression != other.conditionExpression && (this.conditionExpression == null || 
                !this.conditionExpression.equals(other.conditionExpression)))
        {
            return false;
        }
        if (this.toViewIdExpression != other.toViewIdExpression && (this.toViewIdExpression == null || 
                !this.toViewIdExpression.equals(other.toViewIdExpression)))
        {
            return false;
        }
        return true;
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
            throw new IllegalStateException("Flow is immutable once initialized");
        }
    }
}
