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
package org.apache.myfaces.renderkit.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.search.SearchExpressionContext;
import javax.faces.component.search.SearchExpressionHandler;
import javax.faces.component.search.SearchExpressionHint;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.faces.render.ClientBehaviorRenderer;
import org.apache.myfaces.shared.renderkit.html.util.SharedStringBuilder;
import org.apache.myfaces.shared.util.StringUtils;

/**
 * @author Werner Punz  (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlAjaxBehaviorRenderer extends ClientBehaviorRenderer
{

    private static final String QUOTE = "'";
    private static final String BLANK = " ";

    private static final String AJAX_KEY_ONERROR = "onerror";
    private static final String AJAX_KEY_ONEVENT = "onevent";
    private static final String AJAX_KEY_EXECUTE = "execute";
    private static final String AJAX_KEY_RENDER = "render";
    private static final String AJAX_KEY_DELAY = "delay";
    private static final String AJAX_KEY_RESETVALUES = "resetValues";

    private static final String AJAX_VAL_THIS = "this";
    private static final String AJAX_VAL_EVENT = "event";
    private static final String JS_AJAX_REQUEST = "jsf.ajax.request";

    private static final String COLON = ":";
    private static final String EMPTY = "";
    private static final String COMMA = ",";

    private static final String ERR_NO_AJAX_BEHAVIOR = "The behavior must be an instance of AjaxBehavior";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";

    private static final String AJAX_SB = "oam.renderkit.AJAX_SB";
    private static final String AJAX_PARAM_SB = "oam.renderkit.AJAX_PARAM_SB";
    public static final char L_C_BR = '{';
    public static final char R_C_BR = '}';
    public static final String AJAX_KEY_PARAMS = "params";

    @Override
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior)
    {
        assertBehavior(behavior);
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
        if (ajaxBehavior.isDisabled() || !component.isRendered())
        {
            return;
        }

        dispatchBehaviorEvent(component, ajaxBehavior);
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior)
    {
        assertBehavior(behavior);
        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;

        if (ajaxBehavior.isDisabled())
        {
            return null;
        }

        return makeAjax(behaviorContext, ajaxBehavior).toString();
    }

    private void dispatchBehaviorEvent(UIComponent component, AjaxBehavior ajaxBehavior)
    {
        AjaxBehaviorEvent event = new AjaxBehaviorEvent(component, ajaxBehavior);

        boolean isImmediate = false;
        if (ajaxBehavior.isImmediateSet())
        {
            isImmediate = ajaxBehavior.isImmediate();
        }
        else
        {
            isImmediate = isComponentImmediate(component);
        }

        PhaseId phaseId = isImmediate ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.INVOKE_APPLICATION;

        event.setPhaseId(phaseId);

        component.queueEvent(event);
    }


    private boolean isComponentImmediate(UIComponent component)
    {
        boolean isImmediate = false;
        if (component instanceof EditableValueHolder)
        {
            isImmediate = ((EditableValueHolder)component).isImmediate();
        }
        else if (component instanceof ActionSource)
        {
            isImmediate = ((ActionSource)component).isImmediate();
        }
        return isImmediate;
    }


    /**
     * builds the generic ajax call depending upon
     * the ajax behavior parameters
     *
     * @param context  the Client behavior context
     * @param behavior the behavior
     * @return a fully working javascript with calls into jsf.js
     */
    private StringBuilder makeAjax(ClientBehaviorContext context, AjaxBehavior behavior)
    {
        StringBuilder retVal = SharedStringBuilder.get(context.getFacesContext(), AJAX_SB, 60);
        StringBuilder paramBuffer = SharedStringBuilder.get(context.getFacesContext(), AJAX_PARAM_SB, 20);

        String executes = mapToString(context, paramBuffer, AJAX_KEY_EXECUTE, behavior.getExecute());
        String render = mapToString(context, paramBuffer, AJAX_KEY_RENDER, behavior.getRender());

        String onError = behavior.getOnerror();
        if (onError != null && !onError.trim().equals(EMPTY))
        {
            //onError = AJAX_KEY_ONERROR + COLON + onError;
            paramBuffer.setLength(0);
            paramBuffer.append(AJAX_KEY_ONERROR);
            paramBuffer.append(COLON);
            paramBuffer.append(onError);
            onError = paramBuffer.toString();
        }
        else
        {
            onError = null;
        }
        String onEvent = behavior.getOnevent();
        if (onEvent != null && !onEvent.trim().equals(EMPTY))
        {
            paramBuffer.setLength(0);
            paramBuffer.append(AJAX_KEY_ONEVENT);
            paramBuffer.append(COLON);
            paramBuffer.append(onEvent);
            onEvent = paramBuffer.toString();
        }
        else
        {
            onEvent = null;
        }
        /*
         * since version 2.2
         */
        String delay = behavior.getDelay();
        if (delay != null && !delay.trim().equals(EMPTY))
        {
            paramBuffer.setLength(0);
            paramBuffer.append(AJAX_KEY_DELAY);
            paramBuffer.append(COLON);
            if ("none".equals(delay))
            {
                paramBuffer.append('\'');
                paramBuffer.append(delay);
                paramBuffer.append('\'');
            }
            else
            {
                paramBuffer.append(delay);
            }
            delay = paramBuffer.toString();
        }
        else
        {
            delay = null;
        }
        /*
         * since version 2.2
         */
        String resetValues = Boolean.toString(behavior.isResetValues());
        if (resetValues.equals("true"))
        {
            paramBuffer.setLength(0);
            paramBuffer.append(AJAX_KEY_RESETVALUES);
            paramBuffer.append(COLON);
            paramBuffer.append(resetValues);
            resetValues = paramBuffer.toString();
        }
        else
        {
            resetValues = null;
        }

        String sourceId = null;
        if (context.getSourceId() == null)
        {
            sourceId = AJAX_VAL_THIS;
        }
        else
        {
            paramBuffer.setLength(0);
            paramBuffer.append('\'');
            paramBuffer.append(context.getSourceId());
            paramBuffer.append('\'');
            sourceId = paramBuffer.toString();

            if (!context.getSourceId().trim().equals(
                context.getComponent().getClientId(context.getFacesContext())))
            {
                // Check if sourceId is not a clientId and there is no execute set
                UIComponent ref = context.getComponent();
                ref = (ref.getParent() == null) ? ref : ref.getParent();
                UIComponent instance = null;
                try
                {
                    instance = ref.findComponent(context.getSourceId());
                }
                catch (IllegalArgumentException e)
                {
                    // No Op
                }
                if (instance == null && executes == null)
                {
                    // set the clientId of the component so the behavior can be decoded later,
                    // otherwise the behavior will fail
                    List<String> list = new ArrayList<String>();
                    list.add(context.getComponent().getClientId(context.getFacesContext()));
                    executes = mapToString(context, paramBuffer, AJAX_KEY_EXECUTE, list);
                }
            }
        }


        String event = context.getEventName();

        retVal.append(JS_AJAX_REQUEST);
        retVal.append(L_PAREN);
        retVal.append(sourceId);
        retVal.append(COMMA);
        retVal.append(AJAX_VAL_EVENT);
        retVal.append(COMMA);

        Collection<ClientBehaviorContext.Parameter> params = context.getParameters();
        int paramSize = (params != null) ? params.size() : 0;

        List<String> parameterList = new ArrayList<>(paramSize + 2);
        List<String> requestParameterList = new ArrayList<>(paramSize + 2);
        if (executes != null)
        {
            parameterList.add(executes);
        }
        if (render != null)
        {
            parameterList.add(render);
        }
        if (onError != null)
        {
            parameterList.add(onError);
        }
        if (onEvent != null)
        {
            parameterList.add(onEvent);
        }
        /*
         * since version 2.2
         */
        if (delay != null)
        {
            parameterList.add(delay);
        }
        /*
         * since version 2.2
         */
        if (resetValues != null)
        {
            parameterList.add(resetValues);
        }
        if (paramSize > 0)
        {
            /**
             * see ClientBehaviorContext.html of the spec
             * the param list has to be added in the post back
             */
            // params are in 99% RamdonAccess instace created in
            // HtmlRendererUtils.getClientBehaviorContextParameters(Map<String, String>)
            if (params instanceof RandomAccess)
            {
                List<ClientBehaviorContext.Parameter> list = (List<ClientBehaviorContext.Parameter>) params;
                for (int i = 0, size = list.size(); i < size; i++)
                {
                    ClientBehaviorContext.Parameter param = list.get(i);
                    append(paramBuffer, requestParameterList, param);
                }
            }
            else
            {
                for (ClientBehaviorContext.Parameter param : params)
                {
                    append(paramBuffer, requestParameterList, param);
                }
            }
        }

        //parameterList.add(QUOTE + BEHAVIOR_EVENT + QUOTE + COLON + QUOTE + event + QUOTE);
        paramBuffer.setLength(0);
        paramBuffer.append(QUOTE);
        paramBuffer.append(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        paramBuffer.append(QUOTE);
        paramBuffer.append(COLON);
        paramBuffer.append(QUOTE);
        paramBuffer.append(event);
        paramBuffer.append(QUOTE);
        requestParameterList.add(paramBuffer.toString());

        /**
         * I assume here for now that the options are the same which also
         * can be sent via the options attribute to javax.faces.ajax
         * this still needs further clarifications but I assume so for now
         */
        retVal.append(buildOptions(paramBuffer, parameterList, requestParameterList));

        retVal.append(R_PAREN);

        return retVal;
    }

    private void append(StringBuilder paramBuffer, List<String> parameterList, ClientBehaviorContext.Parameter param)
    {
        //TODO we may need a proper type handling in this part
        //lets leave it for now as it is
        //quotes etc.. should be transferred directly
        //and the rest is up to the toString properly implemented
        //ANS: Both name and value should be quoted
        paramBuffer.setLength(0);
        paramBuffer.append(QUOTE);
        paramBuffer.append(param.getName());
        paramBuffer.append(QUOTE);
        paramBuffer.append(COLON);
        paramBuffer.append(QUOTE);
        paramBuffer.append(param.getValue().toString());
        paramBuffer.append(QUOTE);
        parameterList.add(paramBuffer.toString());
    }


    private StringBuilder buildOptions(StringBuilder retVal, List<String> options, List<String> requestParameterList)
    {
        retVal.setLength(0);

        retVal.append(L_C_BR);

        boolean first = true;

        for (int i = 0, size = options.size(); i < size; i++)
        {
            String option = options.get(i);
            if (option != null && !option.trim().equals(EMPTY))
            {
                first = appendComma(retVal, first);
                retVal.append(option);
            }
        }

        int requestParamSize = requestParameterList.size();
        if(requestParamSize > 0)
        {
            appendComma(retVal, first);
            retVal.append(AJAX_KEY_PARAMS);
            retVal.append(COLON);
            retVal.append(L_C_BR);
            first = true;
            for (int i = 0; i < requestParamSize; i++)
            {
                String requestParam = requestParameterList.get(i);
                if (!StringUtils.isBlank(requestParam))
                {
                    first = appendComma(retVal, first);
                    retVal.append(requestParam);
                }
            }
            retVal.append(R_C_BR);

        }


        retVal.append(R_C_BR);
        return retVal;
    }

    private boolean appendComma(StringBuilder retVal, boolean first)
    {
        if (!first)
        {
            retVal.append(COMMA);
        }
        else
        {
            first = false;
        }
        return first;
    }

    private String mapToString(ClientBehaviorContext context, StringBuilder retVal,
            String target, Collection<String> dataHolder)
    {
        //Clear buffer
        retVal.setLength(0);

        if (dataHolder == null)
        {
            dataHolder = Collections.emptyList();
        }
        int executeSize = dataHolder.size();
        if (executeSize > 0)
        {

            retVal.append(target);
            retVal.append(COLON);
            retVal.append(QUOTE);

            int cnt = 0;

            SearchExpressionContext searchExpressionContext = null;
            
            // perf: dataHolder is a Collection : ajaxBehaviour.getExecute()
            // and ajaxBehaviour.getRender() API
            // In most cases comes here a ArrayList, because
            // javax.faces.component.behavior.AjaxBehavior.getCollectionFromSpaceSplitString
            // creates it.
            if (dataHolder instanceof RandomAccess)
            {
                List<String> list = (List<String>) dataHolder;
                for (; cnt  < executeSize; cnt++)
                {
                    if (searchExpressionContext == null)
                    {
                        searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(
                                context.getFacesContext(), context.getComponent(), EXPRESSION_HINTS, null);
                    }
                    
                    String strVal = list.get(cnt);
                    build(context, executeSize, retVal, cnt, strVal, searchExpressionContext);
                }
            }
            else
            {
                for (String strVal : dataHolder)
                {
                    if (searchExpressionContext == null)
                    {
                        searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(
                                context.getFacesContext(), context.getComponent(), EXPRESSION_HINTS, null);
                    }
                    
                    cnt++;
                    build(context, executeSize, retVal, cnt, strVal, searchExpressionContext);
                }
            }

            retVal.append(QUOTE);
            return retVal.toString();
        }
        return null;

    }

    private static final Set<SearchExpressionHint> EXPRESSION_HINTS =
            EnumSet.of(SearchExpressionHint.RESOLVE_CLIENT_SIDE, SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);
    
    public void build(ClientBehaviorContext context,
            int size, StringBuilder retVal, int cnt,
            String strVal, SearchExpressionContext searchExpressionContext)
    {
        strVal = strVal.trim();
        if (!EMPTY.equals(strVal))
        {
            /*
            if (!strVal.startsWith(IDENTIFYER_MARKER))
            {
                String componentId = getComponentId(context, strVal);
                retVal.append(componentId);
            }
            else
            {
                retVal.append(strVal);
            }*/
            SearchExpressionHandler handler = context.getFacesContext().getApplication().getSearchExpressionHandler();
            String clientId = handler.resolveClientId(searchExpressionContext, strVal);
            retVal.append(clientId);
            if (cnt < size)
            {
                retVal.append(BLANK);
            }
        }
    }

    /*
    private String getComponentId(ClientBehaviorContext context, String id)
    {

        UIComponent contextComponent = context.getComponent();
        UIComponent target = contextComponent.findComponent(id);
        if (target == null)
        {
            target = contextComponent.findComponent(
                context.getFacesContext().getNamingContainerSeparatorChar() + id);
        }
        if (target != null)
        {
            return target.getClientId(context.getFacesContext());
        }
        throw new FacesException("Component with id:" + id + " not found");
    }
    */

    private void assertBehavior(ClientBehavior behavior)
    {
        if (!(behavior instanceof AjaxBehavior))
        {
            throw new FacesException(ERR_NO_AJAX_BEHAVIOR);
        }
    }

}
