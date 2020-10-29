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
package jakarta.faces.component.search;

import java.util.List;
import jakarta.faces.FacesWrapper;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 *
 */
public abstract class SearchExpressionHandlerWrapper extends SearchExpressionHandler
        implements FacesWrapper<SearchExpressionHandler>
{
    private SearchExpressionHandler delegate;

    public SearchExpressionHandlerWrapper(SearchExpressionHandler delegate)
    {
        this.delegate = delegate;
    }
    
    @Override
    public SearchExpressionHandler getWrapped()
    {
        return delegate;
    }

    @Override
    public String resolveClientId(SearchExpressionContext searchExpressionContext, String expression)
    {
        return getWrapped().resolveClientId(searchExpressionContext, expression);
    }

    @Override
    public List<String> resolveClientIds(SearchExpressionContext searchExpressionContext, String expressions)
    {
        return getWrapped().resolveClientIds(searchExpressionContext, expressions);
    }

    @Override
    public void resolveComponent(SearchExpressionContext searchExpressionContext,
            String expression, ContextCallback callback)
    {
        getWrapped().resolveComponent(searchExpressionContext, expression, callback);
    }

    @Override
    public void resolveComponents(
            SearchExpressionContext searchExpressionContext, String expressions, ContextCallback callback)
    {
        getWrapped().resolveComponents(searchExpressionContext, expressions, callback);
    }

    @Override
    public void invokeOnComponent(SearchExpressionContext searchExpressionContext,
            String expression, ContextCallback topCallback)
    {
        getWrapped().invokeOnComponent(searchExpressionContext, expression, topCallback);
    }
    
    @Override
    public void invokeOnComponent(SearchExpressionContext searchExpressionContext,
            UIComponent previous, String expression, ContextCallback topCallback)
    {
        getWrapped().invokeOnComponent(searchExpressionContext, previous, expression, topCallback);
    }

    @Override
    public boolean isValidExpression(SearchExpressionContext searchExpressionContext, String expression)
    {
        return getWrapped().isValidExpression(searchExpressionContext, expression);
    }

    @Override
    public boolean isPassthroughExpression(SearchExpressionContext searchExpressionContext, String expression)
    {
        return getWrapped().isPassthroughExpression(searchExpressionContext, expression);
    }

    @Override
    public String[] splitExpressions(FacesContext context, String expressions)
    {
        return getWrapped().splitExpressions(context, expressions);
    }

    @Override
    public char[] getExpressionSeperatorChars(FacesContext context)
    {
        return getWrapped().getExpressionSeperatorChars(context);
    }

}
