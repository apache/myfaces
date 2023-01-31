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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.core.api.shared.lang.SharedStringBuilder;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.ParamsNamingContainerResolver;
import org.apache.myfaces.util.lang.StringUtils;

public class ClientBehaviorRendererUtils
{
    private static final String SB_ESCAPE_JS_FOR_CHAIN = ClientBehaviorRendererUtils.class.getName()
            + "#ESCAPE_JS_FOR_CHAIN";
    
    public static void decodeClientBehaviors(FacesContext facesContext, UIComponent component)
    {
        if (!(component instanceof ClientBehaviorHolder))
        {
            return;
        }
        
        ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;
        Map<String, List<ClientBehavior>> clientBehaviors = clientBehaviorHolder.getClientBehaviors();
        if (clientBehaviors != null && !clientBehaviors.isEmpty())
        {
            ParamsNamingContainerResolver paramMap = new ParamsNamingContainerResolver(facesContext);
            String behaviorEventName = paramMap.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
            if (behaviorEventName != null)
            {
                List<ClientBehavior> clientBehaviorList = clientBehaviors.get(behaviorEventName);
                if (clientBehaviorList != null && !clientBehaviorList.isEmpty())
                {
                    String sourceId = paramMap.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME);
                    String componentClientId = component.getClientId(facesContext);
                    String clientId = sourceId;
                    if (sourceId.startsWith(componentClientId) &&
                        sourceId.length() > componentClientId.length())
                    {
                        String item = sourceId.substring(componentClientId.length()+1);
                        // If is item it should be an integer number, otherwise it can be related to a child 
                        // component, because that could conflict with the clientId naming convention.
                        if (StringUtils.isInteger(item))
                        {
                            clientId = componentClientId;
                        }
                    }
                    if (component.getClientId(facesContext).equals(clientId))
                    {
                        if (clientBehaviorList instanceof RandomAccess)
                        {
                            for (int i = 0, size = clientBehaviorList.size(); i < size; i++)
                            {
                                ClientBehavior clientBehavior = clientBehaviorList.get(i);
                                clientBehavior.decode(facesContext, component);
                            }
                        } 
                        else
                        {
                            for (ClientBehavior clientBehavior : clientBehaviorList)
                            {
                                clientBehavior.decode(facesContext, component);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the given component has a behavior attachment with a given name.
     *
     * @param eventName the event name to be checked for
     * @param behaviors map of behaviors attached to the component
     * @return true if client behavior with given name is attached, false otherwise
     */
    public static boolean hasClientBehavior(String eventName, Map<String, List<ClientBehavior>> behaviors)
    {
        if (behaviors == null)
        {
            return false;
        }
        return behaviors.get(eventName) != null;
    }

    public static Collection<ClientBehaviorContext.Parameter> getClientBehaviorContextParameters(
            Map<String, String> params)
    {
        if (params == null || params.isEmpty())
        {
            return null;
        }
        
        List<ClientBehaviorContext.Parameter> paramList = new ArrayList<>(params.size());
        for (Map.Entry<String, String> paramEntry : params.entrySet())
        {
            paramList.add(new ClientBehaviorContext.Parameter(paramEntry.getKey(), paramEntry.getValue()));
        }
        return paramList;
    }

    /**
     * builds the chained behavior script which then can be reused
     * in following order by the other script building parts
     * <p/>
     * user defined event handling script
     * behavior script
     * renderer default script
     *
     * @param eventName    event name ("onclick" etc...)
     * @param config       the {@link MyfacesConfig}
     * @param uiComponent  the component which has the attachement (or should have)
     * @param facesContext the facesContext
     * @param params       params map of params which have to be dragged into the request
     * @return a string representation of the javascripts for the attached event behavior,
     *         an empty string if none is present
     */
    private static boolean getClientBehaviorScript(FacesContext facesContext,
            MyfacesConfig config,
            UIComponent uiComponent,
            String sourceId, String eventName,
            Map<String, List<ClientBehavior>> clientBehaviors,
            JavascriptContext target,
            Collection<ClientBehaviorContext.Parameter> params)
    {
        if (!(uiComponent instanceof ClientBehaviorHolder))
        {
            target.append(RendererUtils.EMPTY_STRING);
            return false;
        }

        boolean renderClientBehavior = clientBehaviors != null && !clientBehaviors.isEmpty();
        if (!renderClientBehavior)
        {
            target.append(RendererUtils.EMPTY_STRING);
            return false;
        }
        
        List<ClientBehavior> attachedEventBehaviors = clientBehaviors.get(eventName);
        if (attachedEventBehaviors == null || attachedEventBehaviors.isEmpty())
        {
            target.append(RendererUtils.EMPTY_STRING);
            return false;
        }
        
        ClientBehaviorContext context = ClientBehaviorContext
                .createClientBehaviorContext(facesContext, uiComponent, eventName, sourceId, params);
        boolean submitting = false;
        
        // List<ClientBehavior>  attachedEventBehaviors is  99% _DeltaList created in
        // jakarta.faces.component.UIComponentBase.addClientBehavior
        if (attachedEventBehaviors instanceof RandomAccess)
        {
            for (int i = 0, size = attachedEventBehaviors.size(); i < size; i++)
            {
                ClientBehavior clientBehavior = attachedEventBehaviors.get(i);
                submitting = appendClientBehaviourScript(facesContext, target, context, 
                        submitting, i < (size -1), clientBehavior, config);   
            }
        }
        else 
        {
            Iterator<ClientBehavior> clientIterator = attachedEventBehaviors.iterator();
            while (clientIterator.hasNext())
            {
                ClientBehavior clientBehavior = clientIterator.next();
                submitting = appendClientBehaviourScript(facesContext, target, context, submitting, 
                        clientIterator.hasNext(), clientBehavior, config);
            }
        }
        
        return submitting;
    }

    private static boolean appendClientBehaviourScript(FacesContext facesContext, JavascriptContext target,
            ClientBehaviorContext context, boolean submitting, boolean hasNext, ClientBehavior clientBehavior,
            MyfacesConfig config)
    {
        String script = clientBehavior.getScript(context);

        // The script _can_ be null, and in fact is for <f:ajax disabled="true" />
        if (script != null)
        {
            addFunction(facesContext, script, target, config);

            if (hasNext)
            {
                target.append(", ");
            }
            
            // MYFACES-3836 If no script provided by the client behavior, ignore the 
            // submitting hint because. it is evidence the client behavior is disabled.
            if (!submitting)
            {
                submitting = clientBehavior.getHints().contains(ClientBehaviorHint.SUBMITTING);
            }
        }

        return submitting;
    }

    public static String buildBehaviorChain(FacesContext facesContext,
            UIComponent uiComponent,
            String eventName,
            Collection<ClientBehaviorContext.Parameter> params,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String userEventCode, String serverEventCode)
    {
        return buildBehaviorChain(facesContext, uiComponent,
                null, eventName, params,
                clientBehaviors, userEventCode, serverEventCode);
    }

    public static String buildBehaviorChain(FacesContext facesContext,
            UIComponent uiComponent,
            String sourceId, String eventName,
            Collection<ClientBehaviorContext.Parameter> params,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String userEventCode, String serverEventCode)
    {
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext);

        List<String> functions = new ArrayList<>(3);
        if (StringUtils.isNotBlank(userEventCode))
        {
            addFunction(facesContext, userEventCode, functions, config);
        }
        
        JavascriptContext chainContext = new JavascriptContext();
        
        JavascriptContext behaviorContext = new JavascriptContext();
        getClientBehaviorScript(facesContext, config, uiComponent, sourceId,
                eventName, clientBehaviors, behaviorContext, params);
        
        String behaviorScript = behaviorContext.toString();
        if (StringUtils.isNotBlank(behaviorScript))
        {
            functions.add(behaviorScript);
        }
        if (StringUtils.isNotBlank(serverEventCode))
        {
            addFunction(facesContext, serverEventCode, functions, config);
        }

        // It's possible that there are no behaviors to render.
        // For example, if we have <f:ajax disabled="true" /> as the only behavior.
        int size = functions.size();
        if (size > 0)
        {
            //according to the spec faces.util.chain has to be used to build up the 
            //behavior and scripts
            if (sourceId == null)
            {
                chainContext.append("faces.util.chain(this, event,");
            }
            else
            {
                chainContext.append("faces.util.chain(document.getElementById('" + sourceId + "'), event,");
            }

            for (int i = 0; i < size; i++)
            {
                if (i != 0)
                {
                    chainContext.append(", ");
                }
                chainContext.append(functions.get(i));
            }

            chainContext.append(");");
        }

        return chainContext.toString();
    }

    public static String buildBehaviorChain(FacesContext facesContext,
            UIComponent uiComponent,
            String eventName1,
            Collection<ClientBehaviorContext.Parameter> params1,
            String eventName2,
            Collection<ClientBehaviorContext.Parameter> params2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String userEventCode,
            String serverEventCode)
    {
        return buildBehaviorChain(facesContext,
                uiComponent, null,
                eventName1, params1,
                eventName2, params2,
                clientBehaviors, userEventCode, serverEventCode);
    }

    public static String buildBehaviorChain(FacesContext facesContext,
            UIComponent uiComponent,
            String sourceId,
            String eventName1,
            Collection<ClientBehaviorContext.Parameter> params1,
            String eventName2,
            Collection<ClientBehaviorContext.Parameter> params2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String userEventCode,
            String serverEventCode)
    {
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext);
        
        List<String> functions = new ArrayList<>(3);
        if (StringUtils.isNotBlank(userEventCode))
        {
            addFunction(facesContext, userEventCode, functions, config);
        }

        JavascriptContext chainContext = new JavascriptContext();
        
        JavascriptContext behaviorContext1 = new JavascriptContext();
        boolean submitting1 = getClientBehaviorScript(facesContext, config,
                uiComponent, sourceId, eventName1, clientBehaviors,
                behaviorContext1, params1);

        JavascriptContext behaviorContext2 = new JavascriptContext();
        boolean submitting2 = getClientBehaviorScript(facesContext, config,
                uiComponent, sourceId, eventName2, clientBehaviors,
                behaviorContext2, params2);

        // ClientBehaviors for both events have to be checked for the Submitting hint
        boolean submitting = submitting1 || submitting2;
        
        String behaviorScript1 = behaviorContext1.toString();
        if (StringUtils.isNotBlank(behaviorScript1))
        {
            functions.add(behaviorScript1);
        }

        String behaviorScript2 = behaviorContext2.toString();
        if (StringUtils.isNotBlank(behaviorScript2))
        {
            functions.add(behaviorScript2);
        }

        if (StringUtils.isNotBlank(serverEventCode))
        {
            addFunction(facesContext, serverEventCode, functions, config);
        }
        
        // It's possible that there are no behaviors to render.  For example, if we have
        // <f:ajax disabled="true" /> as the only behavior.
        
        int size = functions.size();
        if (size > 0)
        {
            if (!submitting)
            {
                chainContext.append("return ");
            }
            //according to the spec faces.util.chain has to be used to build up the 
            //behavior and scripts
            if (sourceId == null)
            {
                chainContext.append("faces.util.chain(this, event,");
            }
            else
            {
                chainContext.append("faces.util.chain(document.getElementById('" + sourceId + "'), event,");
            }
            
            for (int i = 0; i < size; i++)
            {
                if (i != 0)
                {
                    chainContext.append(", ");
                }
                chainContext.append(functions.get(i));
            }

            chainContext.append(");");

            if (submitting)
            {
                chainContext.append(" return false;");
            }
        }

        return chainContext.toString();
    }

    /**
     * This function correctly escapes the given JavaScript code
     * for the use in the faces.util.chain() JavaScript function.
     * It also handles double-escaping correclty.
     *
     * @param facesContext
     * @param javaScript
     * @return
     */
    public static String escapeJavaScriptForChain(FacesContext facesContext, String javaScript)
    {
        StringBuilder out = null;
        for (int pos = 0; pos < javaScript.length(); pos++)
        {
            char c = javaScript.charAt(pos);

            if (c == '\\' || c == '\'')
            {
                if (out == null)
                {
                    out = SharedStringBuilder.get(facesContext, SB_ESCAPE_JS_FOR_CHAIN, javaScript.length() + 8);
                    if (pos > 0)
                    {
                        out.append(javaScript, 0, pos);
                    }
                }
                out.append('\\');
            }
            if (out != null)
            {
                out.append(c);
            }
        }

        if (out == null)
        {
            return javaScript;
        }
        else
        {
            return out.toString();
        }
    }
    
    private static void addFunction(FacesContext facesContext, String function, List<String> functions,
            MyfacesConfig config)
    {
        if (StringUtils.isNotBlank(function))
        {
            // either strings or functions are allowed
            if (config.isRenderClientBehaviorScriptsAsString())
            {
                // escape every ' in the user event code since it will be a string attribute of faces.util.chain
                functions.add('\'' + escapeJavaScriptForChain(facesContext, function) + '\'');
            }
            else
            {
                functions.add("function(event){" + function + "}");
            }
        }
    }
    
    private static void addFunction(FacesContext facesContext, String function, JavascriptContext target,
            MyfacesConfig config)
    {
        if (StringUtils.isNotBlank(function))
        {
            // either strings or functions are allowed
            if (config.isRenderClientBehaviorScriptsAsString())
            {
                // escape every ' in the user event code since it will be a string attribute of faces.util.chain
                target.append('\'');
                target.append(escapeJavaScriptForChain(facesContext, function));
                target.append('\'');
            }
            else
            {
                target.append("function(event){");
                target.append(function);
                target.append('}');
            }
        }
    }

}
