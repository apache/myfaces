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

import java.util.Set;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

/**
 *
 */
public abstract class SearchExpressionContext
{
    
    public static SearchExpressionContext createSearchExpressionContext(FacesContext context, UIComponent source)
    {
        return createSearchExpressionContext(context, source, null, null);
    }
        
    public static SearchExpressionContext createSearchExpressionContext(
            FacesContext context,  UIComponent source, 
            Set<SearchExpressionHint> expressionHints, Set<VisitHint> visitHints)
    {
        SearchExpressionContextFactory factory
                = (SearchExpressionContextFactory) FactoryFinder.getFactory(
                        FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY);
        return factory.getSearchExpressionContext(context, source, expressionHints, visitHints);
    }

    public abstract UIComponent getSource();

    public abstract Set<VisitHint> getVisitHints();

    public abstract Set<SearchExpressionHint> getExpressionHints();

    public abstract FacesContext getFacesContext();
    
}
