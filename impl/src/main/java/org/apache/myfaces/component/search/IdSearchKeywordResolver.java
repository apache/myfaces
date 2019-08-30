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
package org.apache.myfaces.component.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHint;
import javax.faces.component.search.SearchKeywordContext;
import javax.faces.component.search.SearchKeywordResolver;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

/**
 *
 */
public class IdSearchKeywordResolver extends SearchKeywordResolver
{

    public static final String ID_KEYWORD = "id";

    private static final Pattern PATTERN = Pattern.compile("id\\((\\w+)\\)");

    @Override
    public void resolve(SearchKeywordContext expressionContext, UIComponent current, String keyword)
    {
        FacesContext facesContext = expressionContext.getSearchExpressionContext().getFacesContext();
        
        final String targetId = extractId(keyword);
        if (expressionContext.getSearchExpressionContext().getExpressionHints() != null
                && expressionContext.getSearchExpressionContext().getExpressionHints().contains(
                        SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS))
        {
            // Avoid visit tree because in this case we need real component instances.
            // This means components inside UIData will not be scanned. 
            withId(facesContext, targetId, current, expressionContext.getCallback());
            expressionContext.setKeywordResolved(true);
        }
        else
        {
            current.visitTree(
                    VisitContext.createVisitContext(facesContext, null,
                            expressionContext.getSearchExpressionContext().getVisitHints()),
                    new VisitCallback()
                    {
                        @Override
                        public VisitResult visit(VisitContext context, UIComponent target)
                        {
                            if (targetId.equals(target.getId()))
                            {
                                expressionContext.invokeContextCallback(target);
                                
                                if (expressionContext.getSearchExpressionContext().getExpressionHints() != null
                                        && expressionContext.getSearchExpressionContext().getExpressionHints().contains(
                                                SearchExpressionHint.RESOLVE_SINGLE_COMPONENT))
                                {
                                    return VisitResult.COMPLETE;
                                }
                                
                                return VisitResult.ACCEPT;
                            }
                            else
                            {
                                return VisitResult.ACCEPT;
                            }
                        }
                    });
        }
    }

    protected String extractId(String expression)
    {
        Matcher matcher = PATTERN.matcher(expression);
        if (matcher.matches())
        {
            return matcher.group(1);
        }
        else
        {
            throw new FacesException("Expression does not match following pattern @id(id). Expression: \""
                    + expression + '"');
        }
    }

    private static void withId(FacesContext context, String id, UIComponent base, ContextCallback callback)
    {
        if (id.equals(base.getId()))
        {
            callback.invokeContextCallback(context, base);
        }

        if (base.getFacetCount() > 0)
        {
            for (UIComponent facet : base.getFacets().values())
            {
                withId(context, id, facet, callback);
            }
        }

        int childCount = base.getChildCount();
        if (childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                UIComponent child = base.getChildren().get(i);
                withId(context, id, child, callback);
            }
        }
    }
    
    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String command)
    {
        if (command.length() > 6 && command.substring(0, ID_KEYWORD.length()).equalsIgnoreCase(ID_KEYWORD))
        {
            Matcher matcher = PATTERN.matcher(command);

            if (matcher.matches())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword)
    {
        return false;
    }

    @Override
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword)
    {
        return false;
    }

}
