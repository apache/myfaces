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

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.search.ComponentNotFoundException;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHandler;
import javax.faces.component.search.SearchExpressionHint;
import javax.faces.component.search.SearchKeywordContext;
import javax.faces.context.FacesContext;
import javax.faces.view.AttachedObjectTarget;
import javax.faces.view.EditableValueHolderAttachedObjectTarget;

/**
 *
 */
public class SearchExpressionHandlerImpl extends SearchExpressionHandler
{

    protected void addHint(SearchExpressionContext searchExpressionContext, SearchExpressionHint hint)
    {
        // already available
        if (!searchExpressionContext.getExpressionHints().contains(hint))
        {
            searchExpressionContext.getExpressionHints().add(hint);
        }
    }
    
    protected boolean isHintSet(SearchExpressionContext searchExpressionContext, SearchExpressionHint hint)
    {
        if (searchExpressionContext.getExpressionHints() == null)
        {
            return false;
        }
        
        return searchExpressionContext.getExpressionHints().contains(hint);
    }
    
    @Override
    public String resolveClientId(SearchExpressionContext searchExpressionContext, String expression)
    {
        if (expression == null)
        {
            expression = "";
        }
        else
        {
            expression = expression.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        
        addHint(searchExpressionContext, SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);

        if (handler.isPassthroughExpression(searchExpressionContext, expression))
        {
            return expression;
        }
        else
        {
            CollectClientIdCallback callback = new CollectClientIdCallback();
            
            handler.invokeOnComponent(
                    searchExpressionContext, searchExpressionContext.getSource(), expression, callback);

            if (!callback.isClientIdFound() && isHintSet(searchExpressionContext, SearchExpressionHint.PARENT_FALLBACK))
            {
                callback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
            }
            if (!callback.isClientIdFound())
            {
                if (isHintSet(searchExpressionContext, SearchExpressionHint.IGNORE_NO_RESULT))
                {
                    //Ignore
                }
                else
                {
                    throw new ComponentNotFoundException("Cannot find component for expression \""
                        + expression + "\" referenced from \""
                        + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
                }
            }
            return callback.getClientId();
        }
    }

    private static class CollectClientIdCallback implements ContextCallback
    {
        private String clientId = null;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (clientId == null)
            {
                clientId = target.getClientId(context);
            }
        }

        private String getClientId()
        {
            return clientId;
        }

        private boolean isClientIdFound()
        {
            return clientId != null;
        }
    }

    @Override
    public List<String> resolveClientIds(SearchExpressionContext searchExpressionContext, String expressions)
    {
        if (expressions == null)
        {
            expressions = "";
        }
        else
        {
            expressions = expressions.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        CollectClientIdsCallback callback = new CollectClientIdsCallback();

        for (String expression : handler.splitExpressions(facesContext, expressions))
        {
            if (handler.isPassthroughExpression(searchExpressionContext, expression))
            {
                // It will be resolved in the client, just add the expression.
                callback.addClientId(expression);
            }
            else
            {
                handler.invokeOnComponent(
                        searchExpressionContext, searchExpressionContext.getSource(), expression, callback);
            }
        }
        if (!callback.isClientIdFound() && isHintSet(searchExpressionContext, SearchExpressionHint.PARENT_FALLBACK))
        {
            callback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
        }
        if (!callback.isClientIdFound())
        {
            if (isHintSet(searchExpressionContext, SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expressions + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
        return callback.getClientIds();
    }

    private static class CollectClientIdsCallback implements ContextCallback
    {
        private List<String> clientIds = null;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (clientIds == null)
            {
                clientIds = new ArrayList<String>();
            }
            clientIds.add(target.getClientId(context));
        }

        private List<String> getClientIds()
        {
            return clientIds == null ? Collections.emptyList() : clientIds;
        }

        private boolean isClientIdFound()
        {
            return clientIds != null;
        }

        private void addClientId(String clientId)
        {
            if (clientIds == null)
            {
                clientIds = new ArrayList<String>();
            }
            clientIds.add(clientId);
        }
    }

    @Override
    public void resolveComponent(SearchExpressionContext searchExpressionContext, String expression,
        ContextCallback callback)
    {
        if (expression == null)
        {
            expression = "";
        }
        else
        {
            expression = expression.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        SingleInvocationCallback checkCallback = new SingleInvocationCallback(callback);
        
        addHint(searchExpressionContext, SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);

        facesContext.getApplication().getSearchExpressionHandler().invokeOnComponent(
                searchExpressionContext, searchExpressionContext.getSource(), expression, checkCallback);

        if (!checkCallback.isInvoked() && isHintSet(searchExpressionContext, SearchExpressionHint.PARENT_FALLBACK))
        {
            checkCallback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
        }
        if (!checkCallback.isInvoked())
        {
            if (isHintSet(searchExpressionContext, SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expression + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
    }

    private static class SingleInvocationCallback implements ContextCallback
    {
        private boolean invoked;

        private final ContextCallback innerCallback;

        public SingleInvocationCallback(ContextCallback innerCallback)
        {
            this.innerCallback = innerCallback;
            this.invoked = false;
        }

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            if (!isInvoked())
            {
                try
                {
                    innerCallback.invokeContextCallback(context, target);
                }
                finally
                {
                    invoked = true;
                }
            }
        }

        public boolean isInvoked()
        {
            return invoked;
        }
    }

    @Override
    public void resolveComponents(SearchExpressionContext searchExpressionContext, String expressions,
            ContextCallback callback)
    {
        if (expressions == null)
        {
            expressions = "";
        }
        else
        {
            expressions = expressions.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        
        MultipleInvocationCallback checkCallback = new MultipleInvocationCallback(callback);

        for (String expression : handler.splitExpressions(facesContext, expressions))
        {
            handler.invokeOnComponent(searchExpressionContext, searchExpressionContext.getSource(),
                    expression, checkCallback);
        }
        // ...
        if (!checkCallback.isInvoked() && isHintSet(searchExpressionContext, SearchExpressionHint.PARENT_FALLBACK))
        {
            checkCallback.invokeContextCallback(facesContext, searchExpressionContext.getSource().getParent());
        }
        if (!checkCallback.isInvoked())
        {
            if (isHintSet(searchExpressionContext, SearchExpressionHint.IGNORE_NO_RESULT))
            {
                //Ignore
            }
            else
            {
                throw new ComponentNotFoundException("Cannot find component for expression \""
                    + expressions + "\" referenced from \""
                    + searchExpressionContext.getSource().getClientId(facesContext) + "\".");
            }
        }
    }

    private static class MultipleInvocationCallback implements ContextCallback
    {
        private boolean invoked;

        private final ContextCallback innerCallback;

        public MultipleInvocationCallback(ContextCallback innerCallback)
        {
            this.innerCallback = innerCallback;
            this.invoked = false;
        }

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target)
        {
            try
            {
                innerCallback.invokeContextCallback(context, target);
            }
            finally
            {
                invoked = true;
            }
        }

        public boolean isInvoked()
        {
            return invoked;
        }
    }

    @Override
    public void invokeOnComponent(final SearchExpressionContext searchExpressionContext,
            UIComponent previous, String topExpression, ContextCallback topCallback)
    {
        if (topExpression == null)
        {
            topExpression = "";
        }
        else
        {
            topExpression = topExpression.trim();
        }
        
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        FacesContext facesContext = searchExpressionContext.getFacesContext();

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        
        UIComponent currentBase = previous;

        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            UIComponent findBase;
            findBase = SearchComponentUtils.getRootComponent(currentBase);
            handler.invokeOnComponent(searchExpressionContext, findBase, topExpression.substring(1), topCallback);
            return;
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.
            final UIComponent base = currentBase;

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final ContextCallback parentCallback = topCallback;


            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            
            if (remaining != null)
            {
                if (facesContext.getApplication().getSearchKeywordResolver().isLeaf(searchExpressionContext, command))
                {
                    throw new FacesException("Expression cannot have keywords or ids at the right side: "+command);
                }
                this.applyKeyword(searchExpressionContext, base, command, remaining, new ContextCallback()
                    {
                        @Override
                        public void invokeContextCallback(FacesContext facesContext, UIComponent target)
                        {
                            handler.invokeOnComponent(
                                    searchExpressionContext, target, remaining, parentCallback);
                        }
                    });
            }
            else
            {
                // Command completed, apply parent callback
                this.applyKeyword(searchExpressionContext, base, command, null, parentCallback);
            }
        }
        else
        {

            //Split expression into tokens and apply loop
            String nextExpression = null;
            String expression;
            if (topExpression.indexOf(":@") > 0)
            {
                int idx = topExpression.indexOf(":@");
                nextExpression = topExpression.substring(idx+1);
                expression = topExpression.substring(0,idx);
            }
            else
            {
                expression = topExpression;
            }

            // Use findComponent(...) passing the expression provided
            UIComponent target = currentBase.findComponent(expression);
            if (target == null)
            {
                // If no component is found ...
                // First try to find the base component.

                // Extract the base id from the expression string
                int idx = expression.indexOf(separatorChar);
                String base = idx > 0 ? expression.substring(0, idx) : expression;

                // From the context component clientId, check if the base is part of the clientId
                String contextClientId = currentBase.getClientId(facesContext);
                int startCommon = contextClientId.lastIndexOf(base+facesContext.getNamingContainerSeparatorChar());
                if (startCommon >= 0
                    && (startCommon == 0 || contextClientId.charAt(startCommon-1) == separatorChar )
                    && (startCommon+base.length() <= contextClientId.length()-1 ||
                        contextClientId.charAt(startCommon+base.length()+1) == separatorChar ))
                {
                    // If there is a match, try to find a the first parent component whose id is equals to
                    // the base id
                    UIComponent parent = currentBase;
                    while (parent != null )
                    {
                        if (base.equals(parent.getId()) && parent instanceof NamingContainer)
                        {
                            break;
                        }
                        else
                        {
                            parent = parent.getParent();
                        }
                    }

                    // if a base component is found ...
                    if (parent != null)
                    {
                        target = parent.findComponent(expression);
                        if (target == null && !searchExpressionContext.getExpressionHints().contains(
                                SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS))
                        {
                            contextClientId = parent.getClientId(facesContext);
                            // If no component is found,
                            String targetClientId = contextClientId.substring(0, startCommon+base.length()) +
                                    expression.substring(base.length());

                            if (nextExpression != null)
                            {
                                final String childExpression = nextExpression;

                                parent.invokeOnComponent(facesContext, targetClientId, new ContextCallback(){
                                    public void invokeContextCallback(FacesContext context, UIComponent target)
                                    {
                                        handler.invokeOnComponent(
                                                searchExpressionContext, target, childExpression, topCallback);
                                    }
                                });
                            }
                            else
                            {
                                parent.invokeOnComponent(facesContext, targetClientId, topCallback);
                            }
                            return;
                        }
                    }
                }
            }
            if (target != null)
            {
                currentBase = target;
            }
            if (currentBase != null)
            {
                if (isHintSet(searchExpressionContext, SearchExpressionHint.UNWRAP_COMPOSITE_COMPONENT))
                {
                    unwrapCompositeComponent(facesContext, target, topCallback);
                }
                else
                {
                    topCallback.invokeContextCallback(facesContext, currentBase);
                }
            }
        }
    }

    // take the command and resolve it using the chain of responsibility pattern.
    protected void applyKeyword(SearchExpressionContext searchExpressionContext, UIComponent last,
                             String command, String remainingExpression, ContextCallback topCallback)
    {
        SearchKeywordContext searchContext;
        
        if (isHintSet(searchExpressionContext, SearchExpressionHint.UNWRAP_COMPOSITE_COMPONENT)
                && remainingExpression == null)
        {
            ContextCallback wrapperCallback = new ContextCallback()
            {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent target)
                {
                    unwrapCompositeComponent(context, target, topCallback);
                }
            };
                    
            searchContext = new SearchKeywordContext(searchExpressionContext, wrapperCallback);
        }
        else
        {
            searchContext = new SearchKeywordContext(searchExpressionContext, topCallback);
        }

        searchContext.setRemainingExpression(remainingExpression);
        searchExpressionContext.getFacesContext().getApplication()
                .getSearchKeywordResolver().resolve(searchContext, last, command);
    }

    @Override
    public boolean isPassthroughExpression(SearchExpressionContext searchExpressionContext, String topExpression)
    {
        if (topExpression == null)
        {
            topExpression = "";
        }
        else
        {
            topExpression = topExpression.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        boolean passthrough = false;
        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            //return facesContext.getApplication().getSearchExpressionHandler().isPassthroughExpression(
            //        searchExpressionContext, topExpression.substring(1));
            // only keywords are passthrough expressions.
            return false;
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final SearchExpressionHandler currentInstance =
                    facesContext.getApplication().getSearchExpressionHandler();

            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            passthrough = facesContext.getApplication().getSearchKeywordResolver().isPassthrough(
                    searchExpressionContext, command);

            if (passthrough)
            {
                return remaining != null ?
                        currentInstance.isPassthroughExpression(searchExpressionContext, remaining) : true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            // Only keywords are valid to be passthrough. If it contains a chain of ids, this can only be resolved
            // server side, because the tree structure and related clientId logic is only available server side.
            return false;
        }
    }

    public boolean isValidExpression(SearchExpressionContext searchExpressionContext, String topExpression)
    {
        if (topExpression == null)
        {
            topExpression = "";
        }
        else
        {
            topExpression = topExpression.trim();
        }
        
        FacesContext facesContext = searchExpressionContext.getFacesContext();
        // Command pattern to apply the keyword or command to the base and then invoke the callback
        boolean isValid = true;
        //Step 1: find base
        //  Case ':' (root)
        char separatorChar = facesContext.getNamingContainerSeparatorChar();
        if (topExpression.charAt(0) == separatorChar)
        {
            return facesContext.getApplication().getSearchExpressionHandler().isValidExpression(
                    searchExpressionContext, topExpression.substring(1));
        }

        //Step 2: Once you have a base where you can start, apply an expression
        if (topExpression.charAt(0) == KEYWORD_PREFIX.charAt(0))
        {
            // A keyword means apply a command over the current source using an expression and the result must be
            // feedback into the algorithm.

            String command = extractKeyword(topExpression, 1, separatorChar);
            final String remaining =
                    command.length()+1 < topExpression.length() ?
                        topExpression.substring(1+command.length()+1) : null;

            final SearchExpressionHandler currentInstance =
                    facesContext.getApplication().getSearchExpressionHandler();

            // If the keyword is @child, @composite, @form, @namingcontainer, @next, @none, @parent, @previous,
            // @root, @this ,  all commands change the source to be applied the action
            isValid = facesContext.getApplication().getSearchKeywordResolver().matchKeyword(
                    searchExpressionContext, command);
            if (remaining != null)
            {
                if (facesContext.getApplication().getSearchKeywordResolver().isLeaf(
                    searchExpressionContext, command))
                {
                    isValid = false;
                }
                return !isValid ? false : currentInstance.isValidExpression(searchExpressionContext, remaining);
            }
        }
        else
        {
            //Split expression into tokens and apply loop
            String nextExpression = null;
            String expression = null;
            if (topExpression.indexOf(":@") > 0)
            {
                int idx = topExpression.indexOf(":@");
                nextExpression = topExpression.substring(idx+1);
                expression = topExpression.substring(0,idx);
            }
            else
            {
                expression = topExpression;
            }

            //Check expression
            for (int i = 0; i < expression.length(); i++)
            {
                char c = expression.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == separatorChar)
                {
                    //continue
                }
                else
                {
                    isValid = false;
                }
            }

            if (nextExpression != null)
            {
                return !isValid ? false : facesContext.getApplication().getSearchExpressionHandler()
                    .isValidExpression(searchExpressionContext, nextExpression);
            }
        }
        return isValid;
    }

    private static String extractKeyword(String expression, int startIndex, char separatorChar)
    {
        int parenthesesCounter = -1;
        int count = -1;
        for (int i = startIndex; i < expression.length(); i++)
        {
            char c = expression.charAt(i);
            if (c == '(')
            {
                if (parenthesesCounter == -1)
                {
                    parenthesesCounter = 0;
                }
                parenthesesCounter++;
            }
            if (c == ')')
            {
                parenthesesCounter--;
            }
            if (parenthesesCounter == 0)
            {
                //Close first parentheses
                count = i+1;
                break;
            }
            if (parenthesesCounter == -1)
            {
                if (c == separatorChar)
                {
                    count = i;
                    break;
                }
            }
        }
        if (count == -1)
        {
            return expression.substring(startIndex);
        }
        else
        {
            return expression.substring(startIndex, count);
        }
    }

    @Override
    public String[] splitExpressions(FacesContext context, String expressions)
    {
        // split expressions by blank or comma (and ignore blank and commas inside brackets)
        String[] splittedExpressions = split(expressions, EXPRESSION_SEPARATOR_CHARS);
        return splittedExpressions;
    }

    private static String[] split(String value, char... separators)
    {
        if (value == null)
        {
            return null;
        }

        List<String> tokens = new ArrayList<String>();
        StringBuilder buffer = new StringBuilder();

        int parenthesesCounter = 0;

        char[] charArray = value.toCharArray();

        for (char c : charArray)
        {
            if (c == '(')
            {
                parenthesesCounter++;
            }

            if (c == ')')
            {
                parenthesesCounter--;
            }

            if (parenthesesCounter == 0)
            {
                boolean isSeparator = false;
                for (char separator : separators)
                {
                    if (c == separator)
                    {
                        isSeparator = true;
                    }
                }

                if (isSeparator)
                {
                    // lets add token inside buffer to our tokens
                    String bufferString = buffer.toString().trim();
                    if (bufferString.length() > 0)
                    {
                        tokens.add(bufferString);
                    }
                    // now we need to clear buffer
                    buffer.delete(0, buffer.length());
                }
                else
                {
                    buffer.append(c);
                }
            }
            else
            {
                buffer.append(c);
            }
        }

        // lets not forget about part after the separator
        tokens.add(buffer.toString());

        return tokens.toArray(new String[tokens.size()]);
    }

    private static final Set<SearchExpressionHint> UNWRAP_COMPOSITE_COMPONENT_HINTS =
            new HashSet<SearchExpressionHint>(Arrays.asList(
                    SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS,
                    SearchExpressionHint.UNWRAP_COMPOSITE_COMPONENT));
    
    protected void unwrapCompositeComponent(FacesContext facesContext, UIComponent composite,
            ContextCallback callback)
    {
        boolean resolved = false;
        
        if (UIComponent.isCompositeComponent(composite))
        {
            BeanInfo info = (BeanInfo) composite.getAttributes().get(UIComponent.BEANINFO_KEY);
            List<AttachedObjectTarget> targets = (List<AttachedObjectTarget>) info.getBeanDescriptor()
                    .getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);

            for (AttachedObjectTarget target : targets)
            {
                if (target instanceof EditableValueHolderAttachedObjectTarget)
                {
                    SearchExpressionContext searchExpressionContext =
                            SearchExpressionContext.createSearchExpressionContext(facesContext, composite,
                                    UNWRAP_COMPOSITE_COMPONENT_HINTS, null);
                    facesContext.getApplication().getSearchExpressionHandler().resolveComponent(
                            searchExpressionContext, target.getName(), callback);
                    resolved = true;
                }
            }
        }

        if (!resolved)
        {
            callback.invokeContextCallback(facesContext, composite);
        }
    }
}