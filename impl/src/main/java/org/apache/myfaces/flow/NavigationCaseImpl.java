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
    private String _condition;
    private String _fromAction;
    private String _fromOutcome;
    private String _fromViewId;
    private String _toViewId;
    private String _toFlowDocumentId;
    private boolean _includeViewParams;
    private boolean _redirect;
    private Map<String, List<String>> _parameters;
    private ValueExpression _conditionExpression;
    private ValueExpression _toViewIdExpression;

    private boolean _initialized;

    public NavigationCaseImpl()
    {
        super(null, null, null, null, null, null, false, false);
    }

    public NavigationCaseImpl(String fromViewId, String fromAction, String fromOutcome, String condition, 
            String toViewId,
            Map<String, List<String>> parameters, boolean redirect, boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, parameters, redirect, includeViewParams);
        _condition = condition;
        _fromViewId = fromViewId;
        _fromAction = fromAction;
        _fromOutcome = fromOutcome;
        _toViewId = toViewId;
        _toFlowDocumentId = null;
        _redirect = redirect;
        _includeViewParams = includeViewParams;
        _parameters = parameters;
    }

    public NavigationCaseImpl(String fromViewId, String fromAction, String fromOutcome, String condition, 
            String toViewId,
            String toFlowDocumentId, Map<String, List<String>> parameters, boolean redirect,
            boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, parameters, redirect, 
                includeViewParams);
        _condition = condition;
        _fromViewId = fromViewId;
        _fromAction = fromAction;
        _fromOutcome = fromOutcome;
        _toViewId = toViewId;
        _toFlowDocumentId = toFlowDocumentId;
        _redirect = redirect;
        _includeViewParams = includeViewParams;
        _parameters = parameters;

    }

    public String getCondition()
    {
        return _condition;
    }

    public void setCondition(String condition)
    {
        checkInitialized();
        this._condition = condition;
    }

    @Override
    public String getFromAction()
    {
        return _fromAction;
    }

    public void setFromAction(String fromAction)
    {
        checkInitialized();
        this._fromAction = fromAction;
    }

    @Override
    public String getFromOutcome()
    {
        return _fromOutcome;
    }

    public void setFromOutcome(String fromOutcome)
    {
        checkInitialized();
        this._fromOutcome = fromOutcome;
    }

    @Override
    public String getFromViewId()
    {
        return _fromViewId;
    }

    public void setFromViewId(String fromViewId)
    {
        checkInitialized();
        this._fromViewId = fromViewId;
    }

    public String getToViewId()
    {
        return _toViewId;
    }

    public void setToViewId(String toViewId)
    {
        checkInitialized();
        this._toViewId = toViewId;
    }

    @Override
    public String getToFlowDocumentId()
    {
        return _toFlowDocumentId;
    }

    public void setToFlowDocumentId(String toFlowDocumentId)
    {
        checkInitialized();
        this._toFlowDocumentId = toFlowDocumentId;
    }

    @Override
    public boolean isIncludeViewParams()
    {
        return _includeViewParams;
    }

    public void setIncludeViewParams(boolean includeViewParams)
    {
        checkInitialized();
        this._includeViewParams = includeViewParams;
    }

    @Override
    public boolean isRedirect()
    {
        return _redirect;
    }

    public void setRedirect(boolean redirect)
    {
        checkInitialized();
        this._redirect = redirect;
    }
    
    @Override
    public Map<String, List<String>> getParameters()
    {
        return _parameters;
    }

    public void setParameters(Map<String, List<String>> parameters)
    {
        checkInitialized();
        this._parameters = parameters;
    }

    public ValueExpression getConditionExpression()
    {
        return _conditionExpression;
    }

    public void setConditionExpression(ValueExpression conditionExpression)
    {
        checkInitialized();
        this._conditionExpression = conditionExpression;
    }

    public ValueExpression getToViewIdExpression()
    {
        return _toViewIdExpression;
    }

    public void setToViewIdExpression(ValueExpression toViewIdExpression)
    {
        checkInitialized();
        this._toViewIdExpression = toViewIdExpression;
    }

    @Override
    public Boolean getCondition(FacesContext context)
    {
        if (_condition == null)
        {
            return null;
        }

        ValueExpression expression = _getConditionExpression(context);

        return Boolean.TRUE.equals(expression.getValue(context.getELContext()));
    }

    private ValueExpression _getConditionExpression(FacesContext context)
    {
        assert _condition != null;

        if (_conditionExpression == null)
        {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            _conditionExpression = factory.createValueExpression(context.getELContext(), _condition, Boolean.class);
        }

        return _conditionExpression;
    }

    @Override
    public String getToViewId(FacesContext context)
    {
        if (_toViewId == null)
        {
            return null;
        }

        ValueExpression expression = _getToViewIdExpression(context);

        return (expression.isLiteralText())
                ? expression.getExpressionString() : expression.getValue(context.getELContext());
    }

    private ValueExpression _getToViewIdExpression(FacesContext context)
    {
        assert _toViewId != null;

        if (_toViewIdExpression == null)
        {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            _toViewIdExpression = factory.createValueExpression(context.getELContext(), _toViewId, String.class);
        }

        return _toViewIdExpression;
    }

    @Override
    public boolean hasCondition()
    {
        return _condition != null && _condition.length() > 0;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 47 * hash + (this._condition != null ? this._condition.hashCode() : 0);
        hash = 47 * hash + (this._fromAction != null ? this._fromAction.hashCode() : 0);
        hash = 47 * hash + (this._fromOutcome != null ? this._fromOutcome.hashCode() : 0);
        hash = 47 * hash + (this._fromViewId != null ? this._fromViewId.hashCode() : 0);
        hash = 47 * hash + (this._toViewId != null ? this._toViewId.hashCode() : 0);
        hash = 47 * hash + (this._toFlowDocumentId != null ? this._toFlowDocumentId.hashCode() : 0);
        hash = 47 * hash + (this._includeViewParams ? 1 : 0);
        hash = 47 * hash + (this._redirect ? 1 : 0);
        hash = 47 * hash + (this._parameters != null ? this._parameters.hashCode() : 0);
        hash = 47 * hash + (this._conditionExpression != null ? this._conditionExpression.hashCode() : 0);
        hash = 47 * hash + (this._toViewIdExpression != null ? this._toViewIdExpression.hashCode() : 0);
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
        if ((this._condition == null) ? (other._condition != null) : !this._condition.equals(other._condition))
        {
            return false;
        }
        if ((this._fromAction == null) ? (other._fromAction != null) : !this._fromAction.equals(other._fromAction))
        {
            return false;
        }
        if ((this._fromOutcome == null) ? (other._fromOutcome != null) : 
                !this._fromOutcome.equals(other._fromOutcome))
        {
            return false;
        }
        if ((this._fromViewId == null) ? (other._fromViewId != null) : !this._fromViewId.equals(other._fromViewId))
        {
            return false;
        }
        if ((this._toViewId == null) ? (other._toViewId != null) : !this._toViewId.equals(other._toViewId))
        {
            return false;
        }
        if ((this._toFlowDocumentId == null) ? (other._toFlowDocumentId != null) : 
                !this._toFlowDocumentId.equals(other._toFlowDocumentId))
        {
            return false;
        }
        if (this._includeViewParams != other._includeViewParams)
        {
            return false;
        }
        if (this._redirect != other._redirect)
        {
            return false;
        }
        if (this._parameters != other._parameters && (this._parameters == null || 
                !this._parameters.equals(other._parameters)))
        {
            return false;
        }
        if (this._conditionExpression != other._conditionExpression && (this._conditionExpression == null || 
                !this._conditionExpression.equals(other._conditionExpression)))
        {
            return false;
        }
        if (this._toViewIdExpression != other._toViewIdExpression && (this._toViewIdExpression == null || 
                !this._toViewIdExpression.equals(other._toViewIdExpression)))
        {
            return false;
        }
        return true;
    }

    @Override
    public void freeze()
    {
        _initialized = true;
    }
    
    private void checkInitialized() throws IllegalStateException
    {
        if (_initialized)
        {
            throw new IllegalStateException("Flow is inmutable once initialized");
        }
    }
}
