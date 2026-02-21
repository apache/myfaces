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
package jakarta.faces.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * @since 2.0
 */
public class NavigationCase
{
    private String _condition;
    private String _fromAction;
    private String _fromOutcome;
    private String _fromViewId;
    private String _toViewId;
    private String _toFlowDocumentId;
    private boolean _includeViewParams;
    private boolean _redirect;
    private Map<String,List<String>> _parameters;
    private String _fragment;

    private ValueExpression _conditionExpression;
    private ValueExpression _toViewIdExpression;

    @Deprecated
    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId,
                          Map<String,List<String>> parameters, boolean redirect, boolean includeViewParams)
    {
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

    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId,
                          Map<String,List<String>> parameters, String fragment, boolean redirect,
                          boolean includeViewParams)
    {
        this(fromViewId, fromAction, fromOutcome, condition, toViewId, parameters, redirect, includeViewParams);
        _fragment = fragment;
    }
    
    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId,
                          String toFlowDocumentId, Map<String,List<String>> parameters, boolean redirect,
                          boolean includeViewParams)
    {
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

    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId,
                          String toFlowDocumentId, Map<String,List<String>> parameters, String fragment,
                          boolean redirect, boolean includeViewParams)
    {
        this(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, parameters, redirect,
                includeViewParams);
        _fragment = fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        else if (o instanceof NavigationCase c)
        {

            return Objects.equals(_fromViewId, c._fromViewId) && Objects.equals(_fromAction, c._fromAction)
                    && Objects.equals(_fromOutcome, c._fromOutcome) && Objects.equals(_condition, c._condition)
                    && Objects.equals(_toViewId, c._toViewId) && _redirect == c._redirect
                    && Objects.equals(_fragment, c._fragment)
                    && _includeViewParams == c._includeViewParams;
        }
        else
        {
            return false;
        }
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(_fromViewId, _fromAction, _fromOutcome, _condition, _toViewId, _redirect,
                _includeViewParams, _fragment);
    }

    public URL getActionURL(FacesContext context) throws MalformedURLException
    {
        ExternalContext externalContext = context.getExternalContext();
        return new URL(externalContext.getRequestScheme(), externalContext.getRequestServerName(),
                externalContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getActionURL(context, getToViewId(context)));
    }

    public Boolean getCondition(FacesContext context)
    {
        if (_condition == null)
        {
            return null;
        }

        ValueExpression expression = _getConditionExpression(context);

        return Boolean.TRUE.equals(expression.getValue(context.getELContext()));
    }

    public String getFromAction()
    {
        return _fromAction;
    }

    public String getFromOutcome()
    {
        return _fromOutcome;
    }

    public String getFromViewId()
    {
        return _fromViewId;
    }

    /**
     *
     * @return
     * @since 5.0
     */
    public String getFragment()
    {
        return _fragment;
    }

    public URL getBookmarkableURL(FacesContext context) throws MalformedURLException
    {
        ExternalContext externalContext = context.getExternalContext();
        return new URL(externalContext.getRequestScheme(),
                externalContext.getRequestServerName(),
                externalContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getBookmarkableURL(context, getToViewId(context), 
                        _NavigationUtils.getEvaluatedNavigationParameters(context,
                             getParameters()), isIncludeViewParams()));
    }

    public URL getResourceURL(FacesContext context) throws MalformedURLException
    {
        ExternalContext externalContext = context.getExternalContext();
        return new URL(externalContext.getRequestScheme(), externalContext.getRequestServerName(),
                       externalContext.getRequestServerPort(),
                       context.getApplication().getViewHandler().getResourceURL(context, getToViewId(context)));
    }
    
    public URL getRedirectURL(FacesContext context) throws MalformedURLException
    {
        ExternalContext externalContext = context.getExternalContext();
        return new URL(externalContext.getRequestScheme(),
                externalContext.getRequestServerName(),
                externalContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getRedirectURL(context, getToViewId(context), 
                        _NavigationUtils.getEvaluatedNavigationParameters(context,
                             getParameters()), isIncludeViewParams()));
    }
    
    public Map<String,List<String>> getParameters()
    {
        return _parameters;        
    }

    public String getToViewId(FacesContext context)
    {
        if (_toViewId == null)
        {
            return null;
        }

        ValueExpression expression = _getToViewIdExpression(context);
        
        return (expression.isLiteralText()) ?
                expression.getExpressionString() : expression.getValue(context.getELContext());
    }

    public boolean hasCondition()
    {
        return _condition != null && _condition.length() > 0;
    }

    public boolean isIncludeViewParams()
    {
        return _includeViewParams;
    }

    public boolean isRedirect()
    {
        return _redirect;
    }
    
    /**
     * @since 2.2
     * @return 
     */
    public String getToFlowDocumentId()
    {
        return _toFlowDocumentId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append("<navigation-case>\n");
        
        if (_fromViewId != null)
        {
            builder.append("  ");
            builder.append("<from-view-id>");
            builder.append(_fromViewId);
            builder.append("</from-view-id>\n");
        }
        
        if (_fromAction != null)
        {
            builder.append("  ");
            builder.append("<from-action>");
            builder.append(_fromAction);
            builder.append("</from-action>\n");
        }
        
        if (_fromOutcome != null)
        {
            builder.append("  ");
            builder.append("<from-outcome>");
            builder.append(_fromOutcome);
            builder.append("</from-outcome>\n");
        }
        
        if (_condition != null)
        {
            builder.append("  ");
            builder.append("<if>");
            builder.append(_condition);
            builder.append("</if>\n");
        }
        
        builder.append("  ");
        builder.append("<to-view-id>");
        builder.append(_toViewId);
        builder.append("</to-view-id>\n");

        if (_fragment != null)
        {
            builder.append("  ");
            builder.append("<fragment>");
            builder.append(_fragment);
            builder.append("</fragment>\n");
        }

        if (_toFlowDocumentId != null)
        {
            builder.append("  ");
            builder.append("<to-flow-document-id>");
            builder.append(_toFlowDocumentId);
            builder.append("</to-flow-document-id>\n");
        }
        
        if (_redirect)
        {
            builder.append("  ");
            builder.append("<redirect include-view-params=\"");
            builder.append(_includeViewParams);
            if (_parameters != null && !_parameters.isEmpty())
            {
                builder.append("\">\n");
                for (Map.Entry<String, List<String>> entry : _parameters.entrySet())
                {
                    String name = entry.getKey();
                    for (String value : entry.getValue())
                    {
                        builder.append("    <view-param>\n");
                        builder.append("      <name>");
                        builder.append(name);
                        builder.append("</name>\n");
                        builder.append("      <value>");
                        builder.append(value);
                        builder.append("</value>\n");
                        builder.append("    </view-param>\n");
                    }
                }
                builder.append("  </redirect>\n");
            }
            else
            {
                builder.append("\"/>\n");
            }
        }
        
        builder.append("</navigation-case>");
        
        return builder.toString();
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
}
