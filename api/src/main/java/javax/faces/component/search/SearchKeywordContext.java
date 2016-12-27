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

package javax.faces.component.search;

import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;

/**
 *
 */
public class SearchKeywordContext
{
 
    private final SearchExpressionContext searchExpressionContext;
    private final ContextCallback callback;

    private boolean commandResolved;
    private String remainingExpression;

    public SearchKeywordContext(SearchExpressionContext searchExpressionContext, ContextCallback callback)
    {
        this.searchExpressionContext = searchExpressionContext;
        this.callback = callback;
    }

    public void invokeContextCallback(UIComponent target)
    {
        commandResolved = true;
        callback.invokeContextCallback(searchExpressionContext.getFacesContext(), target);
    }

    public SearchExpressionContext getSearchExpressionContext()
    {
        return searchExpressionContext;
    }

    public ContextCallback getCallback()
    {
        return callback;
    }

    public boolean isCommandResolved()
    {
        return commandResolved;
    }

    public void setCommandResolved(boolean commandResolved)
    {
        this.commandResolved = commandResolved;
    }

    public String getRemainingExpression()
    {
        return remainingExpression;
    }

    public void setRemainingExpression(String remainingExpression)
    {
        this.remainingExpression = remainingExpression;
    }

}
