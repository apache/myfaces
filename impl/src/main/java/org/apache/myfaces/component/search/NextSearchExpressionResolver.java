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

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.search.Markup;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchKeywordContext;
import javax.faces.component.search.SearchExpressionResolver;

/**
 *
 */
public class NextSearchExpressionResolver extends SearchExpressionResolver
{
    public static final String NEXT_KEYWORD = "next";

    @Override
    public void resolve(SearchKeywordContext expressionContext, UIComponent last, String command)
    {
        if (command != null && command.equalsIgnoreCase(NEXT_KEYWORD))
        {
            UIComponent parent = last.getParent();
            if (parent.getChildCount() > 1) 
            {
                List<UIComponent> children = parent.getChildren();
                int index = children.indexOf(last);

                if (index < parent.getChildCount() - 1)
                {
                    int nextIndex = -1;
                    do
                    {
                        index++;
                        if(!(children.get(index) instanceof Markup))
                        {
                            nextIndex = index;
                        }
                    } while (nextIndex == -1 && index < parent.getChildCount() - 1);
                    
                    if (nextIndex != -1)
                    {
                        expressionContext.invokeContextCallback(expressionContext.getFacesContext(), 
                                children.get(nextIndex));
                    }
                }
            }
            expressionContext.setCommandResolved(true);
        }
    }
    
    @Override
    public boolean matchKeyword(SearchExpressionContext searchExpressionContext, String keyword)
    {
        return NEXT_KEYWORD.equalsIgnoreCase(keyword);
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
