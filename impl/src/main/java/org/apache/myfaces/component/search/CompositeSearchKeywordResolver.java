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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchKeywordContext;
import jakarta.faces.component.search.SearchKeywordResolver;
import org.apache.myfaces.core.api.shared.lang.Assert;

public class CompositeSearchKeywordResolver extends SearchKeywordResolver
{
    private int size;
    private SearchKeywordResolver[] resolvers;

    public CompositeSearchKeywordResolver()
    {
        this.size = 0;
        this.resolvers = new SearchKeywordResolver[2];
    }

    public void add(SearchKeywordResolver keywordResolver)
    {
        Assert.notNull(keywordResolver, "keywordResolver");
        
        if (this.size >= this.resolvers.length)
        {
            SearchKeywordResolver[] nr = new SearchKeywordResolver[this.size * 2];
            System.arraycopy(this.resolvers, 0, nr, 0, this.size);
            this.resolvers = nr;
        }
        this.resolvers[this.size++] = keywordResolver;
    }

    @Override
    public void resolve(SearchKeywordContext context, UIComponent current, String keyword)
    {
        context.setKeywordResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++)
        {
            if (this.resolvers[i].isResolverForKeyword(context.getSearchExpressionContext(), keyword))
            {
                this.resolvers[i].resolve(context, current, keyword);
                if (context.isKeywordResolved())
                {
                    return;
                }
            }
        }
    }

    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++)
        {
            if (this.resolvers[i].isResolverForKeyword(searchExpressionContext, keyword))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++)
        {
            if (this.resolvers[i].isResolverForKeyword(searchExpressionContext, keyword))
            {
                return this.resolvers[i].isPassthrough(searchExpressionContext, keyword);
            }
        }
        return false;
    }

    @Override
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++)
        {
            if (this.resolvers[i].isResolverForKeyword(searchExpressionContext, keyword))
            {
                return this.resolvers[i].isLeaf(searchExpressionContext, keyword);
            }
        }
        return false;
    }
}
