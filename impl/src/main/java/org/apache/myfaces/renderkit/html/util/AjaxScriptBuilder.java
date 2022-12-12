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
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.html.HtmlCommandScript;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHandler;
import javax.faces.context.FacesContext;
import org.apache.myfaces.component.search.MyFacesSearchExpressionHints;
import org.apache.myfaces.util.lang.StringUtils;

// CHECKSTYLE:OFF
public class AjaxScriptBuilder
{    
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
            execute = String.join(" ", executeList);
        }
            
        String render = null;
        if (renderList != null && !renderList.isEmpty())
        {
            render = String.join(" ", renderList);
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
   
        sb.append("myfaces.ab(");

        if (sourceId == null)
        {
            sb.append("this");
        }
        else
        {
            sb.append('\'');
            sb.append(sourceId);
            sb.append('\'');

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
        sb.append(',');

        sb.append(commandScript == null ? "event" : "null");
        sb.append(",'");

        sb.append(eventName);
        sb.append("',");
        
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
        sb.append(',');

        appendIds(sb, render, seHandler, seContext);
        
        if (onevent != null || onerror != null || delay != null || resetValues != null
                || (params != null && !params.isEmpty()) || (uiParams != null && !uiParams.isEmpty()))
        {
            sb.append(",{");
            if (onevent != null)
            {
                appendProperty(sb, "onevent", onevent, false);
            }
            if (onerror != null)
            {
                appendProperty(sb, "onerror", onerror, false);
            }
            if (delay != null)
            {
                appendProperty(sb, "delay", delay, true);
            }
            if (resetValues != null)
            {
                appendProperty(sb, "resetValues", resetValues, false);
            }

            if ((params != null && !params.isEmpty()) || (uiParams != null && !uiParams.isEmpty()))
            {
                StringBuilder paramsBuilder = new StringBuilder();
                paramsBuilder.append('{');
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

                if (uiParams != null && uiParams.size() > 0)
                {
                    for (int i = 0, size = uiParams.size(); i < size; i++)
                    {
                        UIParameter param = uiParams.get(i);
                        appendProperty(paramsBuilder, param.getName(), param.getValue(), true);
                    }
                }
                paramsBuilder.append('}');
                sb.append("params: ");
                sb.append(paramsBuilder);
            }

            sb.append('}');
        }

        sb.append(')');
    }
    
    private static void appendIds(StringBuilder sb, String expressions,
            SearchExpressionHandler handler, SearchExpressionContext searchExpressionContext)
    {
        sb.append('\'');
        
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
                        sb.append(' ');
                    }
                    sb.append(clientIds.get(i));
                }
            }
        }
        
        sb.append('\'');
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
        if (lastChar != ',' && lastChar != '{')
        {
            builder.append(',');
        }

        builder.append('\'');
        builder.append(name);
        builder.append('\'');
        
        builder.append(':');

        if (value == null)
        {
            builder.append("''");
        }
        else if (quoteValue)
        {
            builder.append('\'');
            builder.append(value.toString());
            builder.append('\'');
        }
        else
        {
            builder.append(value.toString());
        }
    }
}
