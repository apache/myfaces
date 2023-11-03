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
package org.apache.myfaces.renderkit.html.util;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.html.HtmlCommandScript;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.component.search.MyFacesSearchExpressionHints;
import org.apache.myfaces.core.api.shared.lang.SharedStringBuilder;
import org.apache.myfaces.util.lang.StringUtils;

// CHECKSTYLE:OFF
public class AjaxScriptBuilder
{

    private static final String AJAX_PARAM_SB = "oam.renderkit.AJAX_PARAM_SB";

    private static final String QUOTE = "'";
    private static final String BLANK = " ";

    private static final String AJAX_KEY_ONERROR = "onerror";
    private static final String AJAX_KEY_ONEVENT = "onevent";
    private static final String AJAX_KEY_DELAY = "delay";
    private static final String AJAX_KEY_RESETVALUES = "resetValues";

    private static final String AJAX_VAL_THIS = "this";
    private static final String AJAX_VAL_EVENT = "event";

    private static final String COLON = ":";
    private static final String COMMA = ",";

    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";

    private static final String L_C_BRACE = "{";
    private static final String R_C_BRACE = "}";
    public static final String AJAX_KEY_PARAMS = "params";
    public static final String AJAX_VAL_NULL = "null";
    public static final String MYFACES_AB = "myfaces.ab";


    public static void build(FacesContext context,
            StringBuilder sb,
            UIComponent component,
            String sourceId,
            String eventName,
            String execute,
            String render,
            Boolean resetValues,
            String onerror,
            String onevent,
            List<UIParameter> uiParams)
    {
        build(context,
                sb,
                component,
                sourceId,
                eventName,
                execute,
                render,
                null,
                Boolean.TRUE.equals(resetValues) ? Boolean.TRUE.toString() : null,
                onerror,
                onevent,
                null,
                uiParams);
    }
    
    public static void build(FacesContext context,
            StringBuilder sb,
            UIComponent component,
            String sourceId,
            String eventName,
            Collection<String> executeList,
            Collection<String> renderList,
            String delay,
            boolean resetValues,
            String onerror,
            String onevent,
            Collection<ClientBehaviorContext.Parameter> params)
    {
        String execute = null;
        if (executeList != null && !executeList.isEmpty())
        {
            execute = String.join(BLANK, executeList);
        }
            
        String render = null;
        if (renderList != null && !renderList.isEmpty())
        {
            render = String.join(BLANK, renderList);
        }

        build(context,
                sb,
                component,
                sourceId,
                eventName,
                execute,
                render,
                delay,
                resetValues ? Boolean.TRUE.toString() : null,
                onerror,
                onevent,
                params,
                null);
    }
    
    public static void build(FacesContext context,
            StringBuilder sb,
            UIComponent component,
            String sourceId,
            String eventName,
            String execute,
            String render,
            String delay,
            String resetValues,
            String onerror,
            String onevent,
            Collection<ClientBehaviorContext.Parameter> params,
            List<UIParameter> uiParams)
    {
    // CHECKSTYLE:ON
        HtmlCommandScript commandScript = (component instanceof HtmlCommandScript)
                ? (HtmlCommandScript) component
                : null;
   
        sb.append(MYFACES_AB);
        sb.append(L_PAREN);

        if (sourceId == null)
        {
            sb.append(AJAX_VAL_THIS);
        }
        else
        {
            sb.append(QUOTE);
            sb.append(sourceId);
            sb.append(QUOTE);

            if (!sourceId.trim().equals(component.getClientId(context)))
            {
                // Check if sourceId is not a clientId and there is no execute set
                UIComponent ref = (component.getParent() == null) ? component : component.getParent();
                UIComponent instance = null;
                try
                {
                    instance = ref.findComponent(sourceId);
                }
                catch (IllegalArgumentException e)
                {
                    // No Op
                }
                if (instance == null && execute == null)
                {
                    // set the clientId of the component so the behavior can be decoded later,
                    // otherwise the behavior will fail
                    execute = component.getClientId(context);
                }
            }
        }
        sb.append(COMMA);

        sb.append(commandScript == null ? AJAX_VAL_EVENT : AJAX_VAL_NULL);
        sb.append(COMMA);
        sb.append(QUOTE);

        sb.append(eventName);
        sb.append(QUOTE);
        sb.append(COMMA);
        
        SearchExpressionHandler seHandler = null;
        SearchExpressionContext seContext = null;
        if (StringUtils.isNotBlank(execute) || StringUtils.isNotBlank(render))
        {
            seHandler = context.getApplication().getSearchExpressionHandler();
            seContext = SearchExpressionContext.createSearchExpressionContext(
                    context, component,
                    MyFacesSearchExpressionHints.SET_RESOLVE_CLIENT_SIDE_RESOLVE_SINGLE_COMPONENT, null);
        }

        appendIds(sb, execute, seHandler, seContext);
        sb.append(COMMA);

        appendIds(sb, render, seHandler, seContext);
        
        if (onevent != null || onerror != null || delay != null || resetValues != null
                || (params != null && !params.isEmpty()) || (uiParams != null && !uiParams.isEmpty()))
        {
            sb.append(COMMA);
            sb.append(L_C_BRACE);
            if (onevent != null)
            {
                appendProperty(sb, AJAX_KEY_ONEVENT, onevent, false);
            }
            if (onerror != null)
            {
                appendProperty(sb, AJAX_KEY_ONERROR, onerror, false);
            }
            if (delay != null)
            {
                appendProperty(sb, AJAX_KEY_DELAY, delay, true);
            }
            if (resetValues != null)
            {
                appendProperty(sb, AJAX_KEY_RESETVALUES, resetValues, false);
            }

            if ((params != null && !params.isEmpty()) || (uiParams != null && !uiParams.isEmpty()))
            {
                StringBuilder paramsBuilder = SharedStringBuilder.get(context, AJAX_PARAM_SB, 60);
                paramsBuilder.append(L_C_BRACE);
                if (params != null && !params.isEmpty())
                {
                    if (params instanceof RandomAccess)
                    {
                        List<ClientBehaviorContext.Parameter> list = (List<ClientBehaviorContext.Parameter>) params;
                        for (int i = 0, size = list.size(); i < size; i++)
                        {
                            ClientBehaviorContext.Parameter param = list.get(i);
                            appendProperty(paramsBuilder, param.getName(), param.getValue(), true);
                        }
                    }
                    else
                    {
                        for (ClientBehaviorContext.Parameter param : params)
                        {
                            appendProperty(paramsBuilder, param.getName(), param.getValue(), true);
                        }
                    }
                }

                if (uiParams != null && !uiParams.isEmpty())
                {
                    for (int i = 0, size = uiParams.size(); i < size; i++)
                    {
                        UIParameter param = uiParams.get(i);
                        appendProperty(paramsBuilder, param.getName(), param.getValue(), true);
                    }
                }
                paramsBuilder.append(R_C_BRACE);
                appendProperty(sb, AJAX_KEY_PARAMS, paramsBuilder, false);
            }

            sb.append(R_C_BRACE);
        }

        sb.append(R_PAREN);
    }
    
    private static void appendIds(StringBuilder sb, String expressions,
            SearchExpressionHandler handler, SearchExpressionContext searchExpressionContext)
    {
        sb.append(QUOTE);
        
        if (StringUtils.isNotBlank(expressions))
        {
            List<String> clientIds =
                    handler.resolveClientIds(searchExpressionContext, expressions);

            if (clientIds != null && !clientIds.isEmpty())
            {
                for (int i = 0; i < clientIds.size(); i++)
                {
                    if (i > 0)
                    {
                        sb.append(BLANK);
                    }
                    sb.append(clientIds.get(i));
                }
            }
        }
        
        sb.append(QUOTE);
    }


    public static void appendProperty(StringBuilder builder, 
                                      String name,
                                      Object value,
                                      boolean quoteValue)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException();
        }

        char lastChar = builder.charAt(builder.length() - 1);
        if (!COMMA.equals(String.valueOf(lastChar)) && !L_C_BRACE.equals(String.valueOf(lastChar)))
        {
            builder.append(COMMA);
        }

        builder.append(QUOTE);
        builder.append(name);
        builder.append(QUOTE);
        
        builder.append(COLON);

        if (value == null)
        {
            builder.append(QUOTE);
            builder.append(QUOTE);
        }
        else if (quoteValue)
        {
            builder.append(QUOTE);
            builder.append(value);
            builder.append(QUOTE);
        }
        else
        {
            builder.append(value);
        }
    }
}
