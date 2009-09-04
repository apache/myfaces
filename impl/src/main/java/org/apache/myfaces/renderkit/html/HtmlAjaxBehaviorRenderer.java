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

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.event.PhaseId;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.FacesException;
import javax.faces.render.FacesBehaviorRenderer;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author Werner Punz  (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@FacesBehaviorRenderer(rendererType = "javax.faces.behavior.Ajax", renderKitId = "HTML_BASIC")
public class HtmlAjaxBehaviorRenderer {

    private static final String QUOTE = "'";
    private static final String BLANK = " ";

    private static final String AJAX_KEY_ONERROR = "onerror";
    private static final String AJAX_KEY_ONEVENT = "onevent";
    private static final String AJAX_KEY_EXECUTE = "execute";
    private static final String AJAX_KEY_RENDER = "render";

    private static final String AJAX_VAL_THIS = "this";
    private static final String AJAX_VAL_EVENT = "event";
    private static final String JS_AJAX_REQUEST = "jsf.ajax.request";

    private static final String COLON = ":";
    private static final String EMPTY = "";
    private static final String COMMA = ",";

    private static final String ERR_NO_AJAX_BEHAVIOR = "The behavior must be an instance of AjaxBehavior";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";

    /*if this marker is present in the request we have to dispatch a behavior event*/
    /*if an attached behavior triggers an ajax request this request param must be added*/
    private static final String BEHAVIOR_EVENT = "javax.faces.behavior.event";
    private static final String IDENTIFYER_MARKER = "@";

    public void decode(FacesContext context, UIComponent component,
                       ClientBehavior behavior) {

        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
        assertBehavior(behavior);
        if (ajaxBehavior.isDisabled() || !component.isRendered()) {
            return;
        }

        dispatchBehaviorEvent(component, ajaxBehavior);
    }


    public String getScript(ClientBehaviorContext behaviorContext,
                            ClientBehavior behavior) {
        return makeAjax(behaviorContext, (AjaxBehavior) behavior).toString();
    }


    private final void dispatchBehaviorEvent(UIComponent component, AjaxBehavior ajaxBehavior) {
        AjaxBehaviorEvent event = new AjaxBehaviorEvent(component, ajaxBehavior);

        PhaseId phaseId = ajaxBehavior.isImmediate() || isComponentImmediate(component) ?
                          PhaseId.APPLY_REQUEST_VALUES :
                          PhaseId.INVOKE_APPLICATION;

        event.setPhaseId(phaseId);

        component.queueEvent(event);
    }


    private final boolean isComponentImmediate(UIComponent component) {
        /**
         * Currently implemented by ActionSource and EditableValueHolder
         * but we cannot be sure about both interfaces so
         * lets make introspection calls here
         */
        Method immediate = null;
        try {
            immediate = component.getClass().getMethod("isImmediate", new Class[]{});
            //public isImmediate must be present
            if (Modifier.isPublic(immediate.getModifiers()) ||
                immediate.getReturnType().equals(boolean.class) ||
                immediate.getReturnType().equals(Boolean.class)) /*autoboxing*/ {
                return (Boolean) immediate.invoke(component, new Object[]{});
            }

            return false;
        } catch (NoSuchMethodException e) {
            //not implemented at all we can return, this is
            //not really a programmatic exception but we do not have an
            //hasMethod, and iterating over all methods is way slower
            return false;
        } catch (InvocationTargetException e) {
            throw new FacesException(e);
        } catch (IllegalAccessException e) {
            throw new FacesException(e);
        }
    }


    /**
     * builds the generic ajax call depending upon
     * the ajax behavior parameters
     *
     * @param context  the Client behavior context
     * @param behavior the behavior
     * @return a fully working javascript with calls into jsf.js
     */
    private final StringBuilder makeAjax(ClientBehaviorContext context, AjaxBehavior behavior) {

        StringBuilder retVal = new StringBuilder();

        StringBuilder executes = mapToString(context, AJAX_KEY_EXECUTE, behavior.getExecute());
        StringBuilder render = mapToString(context, AJAX_KEY_RENDER, behavior.getRender());

        String onError = behavior.getOnerror();
        onError = (onError != null && onError.trim().equals(EMPTY)) ? AJAX_KEY_ONERROR + COLON + onError : null;
        String onEvent = behavior.getOnevent();
        onEvent = (onEvent != null && onEvent.trim().equals(EMPTY)) ? AJAX_KEY_ONEVENT + COLON + onEvent : null;

        String sourceId = (context.getSourceId() == null) ? AJAX_VAL_THIS : context.getSourceId();
        String event = context.getEventName();

        retVal.append(JS_AJAX_REQUEST);
        retVal.append(L_PAREN);
        retVal.append(sourceId);
        retVal.append(COMMA);
        retVal.append(AJAX_VAL_EVENT);
        retVal.append(COMMA);

        Collection<ClientBehaviorContext.Parameter> params = context.getParameters();
        int paramSize = (params != null) ? params.size() : 0;

        List<String> parameterList = new ArrayList(paramSize + 2);
        if (executes != null) {
            parameterList.add(executes.toString());
        }
        if (render != null) {
            parameterList.add(render.toString());
        }
        if (paramSize > 0) {
            /**
             * see ClientBehaviorContext.html of the spec
             * the param list has to be added in the post back
             */
            for (ClientBehaviorContext.Parameter param : params) {
                //TODO we may need a proper type handling in this part
                //lets leave it for now as it is
                //quotes etc.. should be transferred directly
                //and the rest is up to the toString properly implemented
                parameterList.add(param.getName() + COLON + param.getValue().toString());
            }
        }

        parameterList.add(QUOTE + BEHAVIOR_EVENT + QUOTE + COLON + QUOTE + event + QUOTE);

        /**
         * I assume here for now that the options are the same which also
         * can be sent via the options attribute to javax.faces.ajax
         * this still needs further clarifications but I assume so for now
         */
        retVal.append(buildOptions(parameterList));

        retVal.append(R_PAREN);

        return retVal;
    }


    private StringBuilder buildOptions(List<String> options) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("{");

        boolean first = true;
        for (String option : options) {
            if (option != null && !option.trim().equals(EMPTY)) {
                if (!first) {
                    retVal.append(COMMA);
                } else {
                    first = false;
                }
                retVal.append(option);
            }
        }
        retVal.append("}");
        return retVal;
    }

    private final StringBuilder mapToString(ClientBehaviorContext context, String target, Collection<String> dataHolder) {
        StringBuilder retVal = new StringBuilder(20);

        if (dataHolder == null) {
            dataHolder = Collections.EMPTY_LIST;
        }
        int executeSize = dataHolder.size();
        if (executeSize > 0) {

            retVal.append(target);
            retVal.append(COLON);
            retVal.append(QUOTE);

            int cnt = 0;
            for (String strVal : dataHolder) {
                cnt++;
                strVal = strVal.trim();
                if (!strVal.equals("")) {
                    if (!strVal.startsWith(IDENTIFYER_MARKER)) {
                        retVal.append(getComponentId(context, strVal));
                    } else {

                        retVal.append(strVal);
                        if (cnt < dataHolder.size()) {
                            retVal.append(BLANK);
                        }

                    }
                }
            }

            retVal.append(QUOTE);
            return retVal;
        }
        return null;

    }


    private final String getComponentId(ClientBehaviorContext context, String id) {

        UIComponent contextComponent = context.getComponent();
        UIComponent target = contextComponent.findComponent(id);
        if (target == null) {
            target = contextComponent.findComponent(COLON + id);
        }
        if (target != null) {
            return target.getClientId();
        }
        throw new FacesException("Component with id:" + id + " not found");
    }

    private final void assertBehavior(ClientBehavior behavior) {
        if (!(behavior instanceof AjaxBehavior)) {
            throw new FacesException(ERR_NO_AJAX_BEHAVIOR);
        }
    }

}
