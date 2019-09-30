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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIParameter;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.component.html.HtmlCommandScript;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.renderkit.html.base.AjaxScriptBuilder;
import org.apache.myfaces.renderkit.html.base.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.base.HtmlRenderer;
import org.apache.myfaces.renderkit.html.base.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.JavascriptContext;
import org.apache.myfaces.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.util.ComponentUtils;
import org.apache.myfaces.util.lang.StringUtils;
import org.apache.myfaces.util.SharedStringBuilder;

@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Command", type = "javax.faces.Script")
public class HtmlCommandScriptRenderer extends HtmlRenderer
{
    private static final String AJAX_SB = "oam.renderkit.AJAX_SB";
    
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
        super.encodeBegin(context, component);

        HtmlCommandScript commandScript = (HtmlCommandScript) component;
        ResponseWriter writer = context.getResponseWriter();
        
        ResourceUtils.renderDefaultJsfJsInlineIfNecessary(context, writer);
        
        writer.startElement(HTML.SPAN_ELEM, component);
        writer.writeAttribute(HTML.ID_ATTR, component.getClientId(context), null);
        writer.startElement(HTML.SCRIPT_ELEM, component);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
        
        JavascriptContext script = new JavascriptContext();
        
        //Write content
        String cmdName = commandScript.getName();
        String name;
        if (cmdName != null && cmdName.length() > 0)
        {
            name = JavascriptUtils.getValidJavascriptName(cmdName, true);
        }
        else
        {
            name = JavascriptUtils.getValidJavascriptName(component.getClientId(context), true);
        }
        
        script.prettyLine();
        script.increaseIndent();
        script.append("var "+name+" = function(o){var o=(typeof o==='object')&&o?o:{};");
        script.prettyLine();
        
        List<UIParameter> uiParams = HtmlRendererUtils.getValidUIParameterChildren(
                context, getChildren(commandScript), false, false);
        
        StringBuilder ajax = SharedStringBuilder.get(context, AJAX_SB, 60);

        AjaxScriptBuilder.build(context,
                ajax,
                commandScript,
                commandScript.getClientId(context),
                "action",
                commandScript.getExecute(),
                commandScript.getRender(),
                commandScript.getResetValues(),
                commandScript.getOnerror(),
                commandScript.getOnevent(),
                uiParams);

        script.append(ajax.toString());
        script.decreaseIndent();
        script.append("}");
        
        
        if (commandScript.isAutorun())
        {
            script.append(";");
            script.append("myfaces._impl.core._Runtime.addOnLoad(window,");
            script.append(name);
            script.append(");");
        }
        
        writer.writeText(script.toString(), null);
    }
    
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
        super.encodeEnd(context, component); //
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement(HTML.SCRIPT_ELEM);
        writer.endElement(HTML.SPAN_ELEM);
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        super.decode(facesContext, component); 
        
        HtmlCommandScript commandScript = (HtmlCommandScript) component;
        
        if (HtmlRendererUtils.isDisabled(component) || !commandScript.isRendered())
        {
            return;
        }
        
        Map<String, String> paramMap = facesContext.getExternalContext().getRequestParameterMap();
        String behaviorEventName = paramMap.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        if (behaviorEventName != null)
        {
            String sourceId = paramMap.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME);
            String componentClientId = component.getClientId(facesContext);
            String clientId = sourceId;
            if (sourceId.startsWith(componentClientId) && sourceId.length() > componentClientId.length())
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
                boolean disabled = HtmlRendererUtils.isDisabled(component);
                UIForm form = ComponentUtils.closest(UIForm.class, component);
                boolean activateActionEvent = false;
                if (form != null && !disabled)
                {
                    String reqValue = (String) facesContext.getExternalContext().getRequestParameterMap().get(
                            HtmlRendererUtils.getHiddenCommandLinkFieldName(form, facesContext));
                    activateActionEvent = reqValue != null && reqValue.equals(clientId)
                        || HtmlRendererUtils.isPartialOrBehaviorSubmit(facesContext, clientId);
                }
                if (activateActionEvent)
                {
                    component.queueEvent(new ActionEvent(component));
                }
            }
        }

        if (component instanceof ClientBehaviorHolder && !HtmlRendererUtils.isDisabled(component))
        {
            ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
        }
    }
}
