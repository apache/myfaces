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

import javax.faces.component.UIComponent;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchKeywordContext;
import javax.faces.component.search.SearchExpressionResolver;

/**
 *
 */
public class CompositeSearchExpressionResolver extends SearchExpressionResolver
{
    private int size;

    private SearchExpressionResolver[] resolvers;
    
    public CompositeSearchExpressionResolver()
    {
        this.size = 0;
        this.resolvers = new SearchExpressionResolver[2];
    }
    
    public void add(SearchExpressionResolver elResolver) 
    {
        if (elResolver == null) 
        {
            throw new NullPointerException();
        }

        if (this.size >= this.resolvers.length) 
        {
            SearchExpressionResolver[] nr = new SearchExpressionResolver[this.size * 2];
            System.arraycopy(this.resolvers, 0, nr, 0, this.size);
            this.resolvers = nr;
        }
        this.resolvers[this.size++] = elResolver;
    }

    @Override
    public void resolve(SearchKeywordContext context, UIComponent last, String command)
    {
        context.setCommandResolved(false);
        int sz = this.size;
        Object result = null;
        for (int i = 0; i < sz; i++) 
        {
            this.resolvers[i].resolve(context, last, command);
            if (context.isCommandResolved()) 
            {
                return;
            }
        }
    }
    
    @Override
    public boolean matchKeyword(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++) 
        {
            if (this.resolvers[i].matchKeyword(searchExpressionContext, keyword))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPassthroughKeyword(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++) 
        {
            if (this.resolvers[i].matchKeyword(searchExpressionContext, keyword))
            {
                return this.resolvers[i].isPassthroughKeyword(searchExpressionContext, keyword);
            }
        }
        return false;
    }
    
    @Override
    public boolean isLeafKeyword(SearchExpressionContext searchExpressionContext, String keyword)
    {
        int sz = this.size;
        for (int i = 0; i < sz; i++) 
        {
            if (this.resolvers[i].matchKeyword(searchExpressionContext, keyword))
            {
                return this.resolvers[i].isLeafKeyword(searchExpressionContext, keyword);
            }
        }
        return false;
    }

}
