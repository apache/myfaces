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
package javax.faces.application;

import java.net.MalformedURLException;
import java.net.URL;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 13:57:20 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class NavigationCase
{
    private String _condition;
    private String _fromAction;
    private String _fromOutcome;
    private String _fromViewId;
    private String _toViewId;

    private boolean _includeViewParams;
    private boolean _redirect;

    private ValueExpression _conditionExpression;

    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId,
                          boolean redirect, boolean includeViewParams)
    {
        _condition = condition;
        _fromViewId = fromViewId;
        _fromAction = fromAction;
        _fromOutcome = fromOutcome;
        _toViewId = toViewId;
        _redirect = redirect;
        _includeViewParams = includeViewParams;
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
        else if (o instanceof NavigationCase)
        {
            NavigationCase c = (NavigationCase) o;

            return equals(_fromViewId, c._fromViewId) && equals(_fromAction, c._fromAction)
                    && equals(_fromOutcome, c._fromOutcome) && equals(_condition, c._condition)
                    && equals(_toViewId, c._toViewId) && _redirect == c._redirect
                    && _includeViewParams == c._includeViewParams;
        }
        else
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return hash(_fromViewId) << 4 ^ hash(_fromAction) << 8 ^ hash(_fromOutcome) << 12 ^ hash(_condition) << 16
                ^ hash(_toViewId) << 20 ^ hash(Boolean.valueOf(_redirect)) << 24
                ^ hash(Boolean.valueOf(_includeViewParams));
    }

    public URL getActionURL() throws MalformedURLException
    {
        return null;
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

    public URL getResourceURL() throws MalformedURLException
    {
        return null;
    }

    public String getToViewId(FacesContext context)
    {
        return _toViewId;
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
        
        if (_redirect)
        {
            builder.append("  ");
            builder.append("<redirect include-view-params=\"");
            builder.append(_includeViewParams);
            builder.append("\"/>");
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

    private boolean equals(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        }
        else
        {
            return o1.equals(o2);
        }
    }

    private int hash(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }
}
