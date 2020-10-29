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

import jakarta.faces.component.UIComponent;

/**
 *
 */
public abstract class SearchKeywordResolver
{
    
    public abstract void resolve(SearchKeywordContext keywordContext, UIComponent current, String keyword);
    
    /**
     * Check if the keyword can be resolved by the current resolver
     * 
     * @param searchExpressionContext
     * @param keyword
     * @return 
     */
    public abstract boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword);
    
    /**
     * A passthrough keyword is a keyword that according to the context does not require to be resolved on the server,
     * and can be passed to the client
     * 
     * @param searchExpressionContext
     * @param keyword
     * @return 
     */
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword)
    {
        return false;
    }
    
    /**
     * A leaf keyword is a keyword that does not allow to be combined with keywords or id chains to the right.
     * For example: @none:@parent.
     * 
     * @param searchExpressionContext
     * @param keyword
     * @return 
     */
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword)
    {
        return false;
    }
}
