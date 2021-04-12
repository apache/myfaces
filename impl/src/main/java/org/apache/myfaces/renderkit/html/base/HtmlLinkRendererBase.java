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
package org.apache.myfaces.renderkit.html.base;

import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutcomeTarget;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlOutputLink;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.core.api.shared.ComponentUtils;
import org.apache.myfaces.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.core.api.shared.lang.SharedStringBuilder;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

public abstract class HtmlLinkRendererBase extends HtmlRenderer
{
    public static final String END_LINK_OUTCOME_AS_SPAN = 
        "oam.shared.HtmlLinkRendererBase.END_LINK_OUTCOME_AS_SPAN";

    private static final String SB_BUILD_ONCLICK = HtmlLinkRendererBase.class.getName()
            + "#buildOnClick";
    private static final String SB_ADD_CHILD_PARAMETERS = HtmlLinkRendererBase.class.getName() +
            "#addChildParameters";

    private MyfacesConfig myfacesConfig;
    
    public HtmlLinkRendererBase()
    {
        myfacesConfig = MyfacesConfig.getCurrentInstance();
    }
    
    @Override
    public boolean getRendersChildren()
    {
        // We must be able to render the children without a surrounding anchor
        // if the Link is disabled
        return true;
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        super.decode(facesContext, component);  //check for NP

        if (component instanceof UICommand)
        {
            String clientId = component.getClientId(facesContext);
            UIForm form = ComponentUtils.findClosest(UIForm.class, component);
            boolean disabled = HtmlRendererUtils.isDisabled(component);
            // MYFACES-3960 Decode, decode client behavior and queue action event at the end
            boolean activateActionEvent = false;
            if (form != null && !disabled)
            {
                String reqValue = (String) facesContext.getExternalContext().getRequestParameterMap().get(
                        HtmlRendererUtils.getHiddenCommandLinkFieldName(form, facesContext));
                activateActionEvent = reqValue != null && reqValue.equals(clientId)
                    || HtmlRendererUtils.isPartialOrBehaviorSubmit(facesContext, clientId);
            }
            if (component instanceof ClientBehaviorHolder && !disabled)
            {
                ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
            }
            if (activateActionEvent)
            {
                component.queueEvent(new ActionEvent(component));
            }
        }
        else if (component instanceof UIOutput)
        {
            //do nothing
            if (component instanceof ClientBehaviorHolder && !HtmlRendererUtils.isDisabled(component))
            {
                ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
            }
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        super.encodeBegin(facesContext, component);  //check for NP

        if (component instanceof ClientBehaviorHolder)
        {
            Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());
            }
        }
        
        if (component instanceof UICommand)
        {
            renderCommandLinkStart(facesContext, component,
                                   component.getClientId(facesContext),
                                   ((UICommand) component).getValue(),
                                   getStyle(facesContext, component),
                                   getStyleClass(facesContext, component));
        }
        else if (component instanceof UIOutcomeTarget)
        {
            renderOutcomeLinkStart(facesContext, (UIOutcomeTarget)component);
        }        
        else if (component instanceof UIOutput)
        {
            renderOutputLinkStart(facesContext, (UIOutput)component);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }


    /**
     * Can be overwritten by derived classes to overrule the style to be used.
     */
    protected String getStyle(FacesContext facesContext, UIComponent link)
    {
        if (link instanceof HtmlCommandLink)
        {
            return ((HtmlCommandLink)link).getStyle();
        }

        return (String)link.getAttributes().get(HTML.STYLE_ATTR);

    }

    /**
     * Can be overwritten by derived classes to overrule the style class to be used.
     */
    protected String getStyleClass(FacesContext facesContext, UIComponent link)
    {
        if (link instanceof HtmlCommandLink)
        {
            return ((HtmlCommandLink)link).getStyleClass();
        }

        return (String)link.getAttributes().get(HTML.STYLE_CLASS_ATTR);

    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.renderChildren(facesContext, component);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        super.encodeEnd(facesContext, component);  //check for NP

        if (component instanceof UICommand)
        {
            renderCommandLinkEnd(facesContext, component);

            UIForm form = ComponentUtils.findClosest(UIForm.class, component);
            if (form != null)
            {
                HtmlFormRendererBase.renderScrollHiddenInputIfNecessary(
                        form, facesContext, facesContext.getResponseWriter());
            }
        }
        else if (component instanceof UIOutcomeTarget)
        {
            renderOutcomeLinkEnd(facesContext, component);
        }
        else if (component instanceof UIOutput)
        {
            renderOutputLinkEnd(facesContext, component);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }

    protected void renderCommandLinkStart(FacesContext facesContext, UIComponent component,
                                          String clientId,
                                          Object value,
                                          String style,
                                          String styleClass)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        Map<String, List<ClientBehavior>> behaviors = null;

        // h:commandLink can be rendered outside a form, but with warning (jsf 2.0 TCK)
        UIForm form = ComponentUtils.findClosest(UIForm.class, component);
        
        boolean disabled = HtmlRendererUtils.isDisabled(component);
        
        if (disabled || form == null)
        {
            writer.startElement(HTML.SPAN_ELEM, component);
            if (component instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, component, facesContext);
                }
                else
                {
                    HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
                }
                
                // only render onclick if != disabled
                if (!disabled)
                {
                    if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                    {
                        CommonHtmlAttributesUtil.renderEventProperties(writer, 
                                commonPropertiesMarked, component);
                        CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                                commonPropertiesMarked, component);
                    }
                    else
                    {
                        if (isCommonEventsOptimizationEnabled(facesContext))
                        {
                            Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(component);
                            CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                                    commonPropertiesMarked, commonEventsMarked, component, behaviors);
                            CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, commonPropertiesMarked, commonEventsMarked, component, behaviors);
                        }
                        else
                        {
                            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, component, 
                                    behaviors);
                            HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                    facesContext, writer, component, behaviors);
                        }
                    }
                }
                else
                {
                    if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                    {
                        CommonHtmlAttributesUtil.renderEventPropertiesWithoutOnclick(writer, 
                                commonPropertiesMarked, component);
                        CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                                commonPropertiesMarked, component);
                    }
                    else
                    {
                        if (isCommonEventsOptimizationEnabled(facesContext))
                        {
                            Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(component);
                            CommonHtmlEventsUtil.renderBehaviorizedEventHandlersWithoutOnclick(facesContext, writer, 
                                    commonPropertiesMarked, commonEventsMarked, component, behaviors);
                            CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, commonPropertiesMarked, commonEventsMarked, component, behaviors);
                        }
                        else
                        {
                            HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnclick(facesContext, writer,
                                    component, behaviors);
                            HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                    facesContext, writer, component, behaviors);
                        }
                    }
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabledWithoutEvents(writer, 
                            commonPropertiesMarked, component);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED_WITHOUT_EVENTS);
                }
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabled(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED);
                }
            }
        }
        else
        {
            if (component instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
                renderBehaviorizedJavaScriptAnchorStart(
                        facesContext, writer, component, clientId, behaviors, form);
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, component, facesContext);
                }
                else 
                {
                    // If onclick is not null, both onclick and server side script are rendered 
                    // using faces.util.chain(...) js function. We need to check that case and force
                    // id/name rendering. It is possible to do something else in that case and 
                    // do not render the script using faces.util.chain, but for now it is ok.
                    String commandOnclick;
                    if (component instanceof HtmlCommandLink)
                    {
                        commandOnclick = ((HtmlCommandLink)component).getOnclick();
                    }
                    else
                    {
                        commandOnclick = (String)component.getAttributes().get(HTML.ONCLICK_ATTR);
                    }

                    if (commandOnclick != null)
                    {
                        HtmlRendererUtils.writeIdAndName(writer, component, facesContext);
                    }
                    else
                    {
                        HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                    }
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(component);
                }
                if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderEventPropertiesWithoutOnclick(writer,
                        commonPropertiesMarked, component);
                    CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                            commonPropertiesMarked, component);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlersWithoutOnclick(
                            facesContext, writer, component, behaviors);
                    HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                            facesContext, writer, component, behaviors);
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesWithoutStyleAndEvents(writer, 
                            commonPropertiesMarked, component);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_WITHOUT_STYLE_AND_EVENTS);
                }
            }
            else
            {
                renderJavaScriptAnchorStart(facesContext, writer, component, clientId, form);
                HtmlRendererUtils.writeIdIfNecessary(writer, component, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesWithoutOnclickAndStyle(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, component, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_WITHOUT_ONCLICK_WITHOUT_STYLE);
                }
            }

            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_ATTR, HTML.STYLE_ATTR, style);
            HtmlRendererUtils.renderHTMLAttribute(writer, HTML.STYLE_CLASS_ATTR, HTML.STYLE_CLASS_ATTR, styleClass);
        }

        // render value as required by JSF 1.1 renderkitdocs
        if(value != null)
        {
            writer.writeText(value.toString(), ComponentAttrs.VALUE_ATTR);
        }
        
        // render warning message for a h:commandLink with no nesting form
        if (form == null)
        {
            writer.writeText(": This link is deactivated, because it is not embedded in a JSF form.", null);
        }
    }

    protected void renderJavaScriptAnchorStart(FacesContext facesContext,
                                               ResponseWriter writer,
                                               UIComponent component,
                                               String clientId,
                                               UIComponent form)
        throws IOException
    {
        StringBuilder onClick = SharedStringBuilder.get(facesContext, SB_BUILD_ONCLICK);

        String commandOnclick;
        if (component instanceof HtmlCommandLink)
        {
            commandOnclick = ((HtmlCommandLink)component).getOnclick();
        }
        else
        {
            commandOnclick = (String)component.getAttributes().get(HTML.ONCLICK_ATTR);
        }
        if (commandOnclick != null)
        {
            onClick.append("var cf = function(){");
            onClick.append(commandOnclick);
            onClick.append('}');
            onClick.append(';');
            onClick.append("var oamSF = function(){");
        }

        StringBuilder params = addChildParameters(facesContext, component, form);

        String target = getTarget(component);

        onClick.append("return ").
            append(HtmlRendererUtils.SUBMIT_FORM_FN_NAME_JSF2).append("('").
            append(form.getClientId(facesContext)).append("','").
            append(clientId).append('\'');

        if (params.length() > 2 || target != null)
        {
            onClick.append(',').
                append(target == null ? "null" : ('\'' + target + '\'')).append(',').
                append(params);
        }
        onClick.append(");");

        
        if (commandOnclick != null)
        {
            onClick.append('}');
            onClick.append(';');
            onClick.append("return (cf.apply(this, [])==false)? false : oamSF.apply(this, []); ");
        }        

        writer.startElement(HTML.ANCHOR_ELEM, component);
        writer.writeURIAttribute(HTML.HREF_ATTR, "#", null);
        writer.writeAttribute(HTML.ONCLICK_ATTR, onClick.toString(), null);
    }

    
    protected void renderBehaviorizedJavaScriptAnchorStart(FacesContext facesContext,
            ResponseWriter writer,
            UIComponent component,
            String clientId,
            Map<String, List<ClientBehavior>> behaviors,
            UIComponent formInfo) throws IOException
    {
        String commandOnclick;
        if (component instanceof HtmlCommandLink)
        {
            commandOnclick = ((HtmlCommandLink)component).getOnclick();
        }
        else
        {
            commandOnclick = (String)component.getAttributes().get(HTML.ONCLICK_ATTR);
        }

        //Calculate the script necessary to submit form
        String serverEventCode = buildServerOnclick(facesContext, component, clientId, formInfo);
        
        String onclick = null;
        
        if (commandOnclick == null && (behaviors.isEmpty() || 
            (!behaviors.containsKey(ClientBehaviorEvents.CLICK) && 
             !behaviors.containsKey(ClientBehaviorEvents.ACTION) ) ) )
        {
            //we need to render only the submit script
            onclick = serverEventCode;
        }
        else
        {
            boolean hasSubmittingBehavior = hasSubmittingBehavior(behaviors, ClientBehaviorEvents.CLICK)
                || hasSubmittingBehavior(behaviors, ClientBehaviorEvents.ACTION);
            if (!hasSubmittingBehavior)
            {
                //Ensure required resource javascript is available
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
            }
            
            //render a javascript that chain the related code
            Collection<ClientBehaviorContext.Parameter> paramList = 
                ClientBehaviorRendererUtils.getClientBehaviorContextParameters(
                    HtmlRendererUtils.mapAttachedParamsToStringValues(facesContext, component));
            
            onclick = ClientBehaviorRendererUtils.buildBehaviorChain(facesContext, component,
                    ClientBehaviorEvents.CLICK, paramList, ClientBehaviorEvents.ACTION, paramList, behaviors,
                    commandOnclick , hasSubmittingBehavior ? null : serverEventCode);
        }
        
        writer.startElement(HTML.ANCHOR_ELEM, component);
        writer.writeURIAttribute(HTML.HREF_ATTR, "#", null);
        writer.writeAttribute(HTML.ONCLICK_ATTR, onclick, null);
    }

    private boolean hasSubmittingBehavior(Map<String, List<ClientBehavior>> clientBehaviors, String eventName)
    {
        List<ClientBehavior> eventBehaviors = clientBehaviors.get(eventName);
        if (eventBehaviors != null && !eventBehaviors.isEmpty())
        {
            // perf: in 99% cases is eventBehaviors jakarta.faces.component._DeltaList._DeltaList(int) = RandomAccess
            // instance created in jakarta.faces.component.UIComponentBase.addClientBehavior(String, ClientBehavior),
            // but component libraries can provide own implementation
            if (eventBehaviors instanceof RandomAccess)
            {
                for (int i = 0, size = eventBehaviors.size(); i < size; i++)
                {
                    ClientBehavior behavior = eventBehaviors.get(i);
                    if (behavior.getHints().contains(ClientBehaviorHint.SUBMITTING))
                    {
                        return true;
                    }
                }
            }
            else
            {
                for (ClientBehavior behavior : eventBehaviors)
                {
                    if (behavior.getHints().contains(ClientBehaviorHint.SUBMITTING))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected String buildServerOnclick(FacesContext facesContext, UIComponent component, 
            String clientId, UIComponent form) throws IOException
    {
        ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, facesContext.getResponseWriter());

        StringBuilder onClick = SharedStringBuilder.get(facesContext, SB_BUILD_ONCLICK);

        StringBuilder params = addChildParameters(facesContext, component, form);

        String target = getTarget(component);

        onClick.append("return ").
            append(HtmlRendererUtils.SUBMIT_FORM_FN_NAME_JSF2).append("('").
            append(form.getClientId(facesContext)).append("','").
            append(clientId).append('\'');

        if (params.length() > 2 || target != null)
        {
            onClick.append(',').
                append(target == null ? "null" : ('\'' + target + '\'')).append(',').
                append(params);
        }
        onClick.append(");");
        
        return onClick.toString();
    }

    private String getTarget(UIComponent component)
    {
        // for performance reason: double check for the target attribute
        String target;
        if (component instanceof HtmlCommandLink)
        {
            target = ((HtmlCommandLink) component).getTarget();
        }
        else
        {
            target = (String) component.getAttributes().get(HTML.TARGET_ATTR);
        }
        return target;
    }

    private StringBuilder addChildParameters(FacesContext context, UIComponent component, UIComponent nestingForm)
    {
        //add child parameters
        StringBuilder params = SharedStringBuilder.get(context, SB_ADD_CHILD_PARAMETERS);
        params.append('[');

        List<UIParameter> validParams = HtmlRendererUtils.getValidUIParameterChildren(
                context, getChildren(component), false, false);
        for (int j = 0, size = validParams.size(); j < size; j++) 
        {
            UIParameter param = validParams.get(j);
            String name = param.getName();
            Object value = param.getValue();

            //UIParameter is no ValueHolder, so no conversion possible - calling .toString on value....
            // MYFACES-1832 bad charset encoding for f:param
            // if HTMLEncoder.encode is called, then
            // when is called on writer.writeAttribute, encode method
            // is called again so we have a duplicated encode call.
            // MYFACES-2726 All '\' and "'" chars must be escaped 
            // because there will be inside "'" javascript quotes, 
            // otherwise the value will not correctly restored when
            // the command is post.
            //String strParamValue = value != null ? value.toString() : "";
            String strParamValue = "";
            if (value != null)
            {
                strParamValue = value.toString();
                StringBuilder buff = null;
                for (int i = 0; i < strParamValue.length(); i++)
                {
                    char c = strParamValue.charAt(i); 
                    if (c == '\'' || c == '\\')
                    {
                        if (buff == null)
                        {
                            buff = new StringBuilder();
                            buff.append(strParamValue.substring(0,i));
                        }
                        buff.append('\\');
                        buff.append(c);
                    }
                    else if (buff != null)
                    {
                        buff.append(c);
                    }
                }
                if (buff != null)
                {
                    strParamValue = buff.toString();
                }
            }

            if (params.length() > 1) 
            {
                params.append(',');
            }

            params.append("['");
            params.append(name);
            params.append("','");
            params.append(strParamValue);
            params.append("']");
        }
        params.append(']');
        return params;
    }


    private void addChildParametersToHref(FacesContext facesContext,
                                          UIComponent linkComponent,
                                          StringBuilder hrefBuf,
                                          boolean firstParameter,
                                          String charEncoding)
            throws IOException
    {
        List<UIParameter> validParams = HtmlRendererUtils.getValidUIParameterChildren(
                facesContext, getChildren(linkComponent), false, false);
        
        for (int i = 0, size = validParams.size(); i < size; i++)
        {
            UIParameter param = validParams.get(i);
            String name = param.getName();
            Object value = param.getValue();
            addParameterToHref(name, value, hrefBuf, firstParameter, charEncoding, myfacesConfig.isStrictXhtmlLinks());
            firstParameter = false;
        }
    }

    protected void renderOutputLinkStart(FacesContext facesContext, UIOutput output)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        Map<String, List<ClientBehavior>> behaviors = null;

        if (HtmlRendererUtils.isDisabled(output))
        {
            writer.startElement(HTML.SPAN_ELEM, output);
            if (output instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) output).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, output, facesContext);
                }
                else
                {
                    HtmlRendererUtils.writeIdIfNecessary(writer, output, facesContext);
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(output);
                }

                if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderEventProperties(writer, 
                            commonPropertiesMarked, output);
                    CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                            commonPropertiesMarked, output);
                }
                else
                {
                    if (isCommonEventsOptimizationEnabled(facesContext))
                    {
                        Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(output);
                        CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                                commonPropertiesMarked, commonEventsMarked, output, behaviors);
                        CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                            facesContext, writer, commonPropertiesMarked, commonEventsMarked, output, behaviors);
                    }
                    else
                    {
                        HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, output, behaviors);
                        HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, output, behaviors);
                    }
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabledWithoutEvents(writer, 
                            commonPropertiesMarked, output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED_WITHOUT_EVENTS);
                }
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, output, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabled(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(output), output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED);
                }
            }
        }
        else
        { 
            //calculate href
            String href = RendererUtils.getStringValue(facesContext, output);
            
            //check if there is an anchor # in it
            int index = href.indexOf('#');
            String anchorString = null;
            boolean isAnchorInHref = (index > -1);
            if (isAnchorInHref)
            {
                // remove anchor element and add it again after the parameter are encoded
                anchorString = href.substring(index,href.length());
                href = href.substring(0,index);
            }
            if (getChildCount(output) > 0)
            {
                StringBuilder hrefBuf = new StringBuilder(href);
                addChildParametersToHref(facesContext, output, hrefBuf,
                                     (href.indexOf('?') == -1), //first url parameter?
                                     writer.getCharacterEncoding());
                href = hrefBuf.toString();
            }
            // check for the fragement attribute
            String fragmentAttr = null;
            if (output instanceof HtmlOutputLink)
            {
                fragmentAttr = ((HtmlOutputLink) output).getFragment();
            }
            else
            {
                fragmentAttr = (String) output.getAttributes().get(ComponentAttrs.FRAGMENT_ATTR);
            }
            if (fragmentAttr != null && !fragmentAttr.isEmpty())
            {
                href += '#' + fragmentAttr;
            }
            else if (isAnchorInHref)
            {
                href += anchorString;
            }
            href = facesContext.getExternalContext().encodeResourceURL(href);    //TODO: or encodeActionURL ?

            //write anchor
            writer.startElement(HTML.ANCHOR_ELEM, output);
            writer.writeURIAttribute(HTML.HREF_ATTR, href, null);
            if (output instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) output).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, output, facesContext);
                }
                else
                {
                    HtmlRendererUtils.writeIdAndNameIfNecessary(writer, output, facesContext);
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(output);
                }
                if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderEventProperties(writer, 
                            commonPropertiesMarked, output);
                    CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                            commonPropertiesMarked, output);
                }
                else
                {
                    if (isCommonEventsOptimizationEnabled(facesContext))
                    {
                        Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(output);
                        CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                                commonPropertiesMarked, commonEventsMarked, output, behaviors);
                        CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                            facesContext, writer, commonPropertiesMarked, commonEventsMarked, output, behaviors);
                    }
                    else
                    {
                        HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, output, behaviors);
                        HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, output, behaviors);
                    }
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesWithoutEvents(writer, 
                            commonPropertiesMarked, output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
                }
            }
            else
            {
                HtmlRendererUtils.writeIdAndNameIfNecessary(writer, output, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughProperties(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(output), output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES);
                }
            }
            writer.flush();
        }
    }
    
    protected void renderOutcomeLinkStart(FacesContext facesContext, UIOutcomeTarget output)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        Map<String, List<ClientBehavior>> behaviors = null;
        
        //calculate href
        String targetHref = HtmlRendererUtils.getOutcomeTargetHref(facesContext, output);
        
        if (HtmlRendererUtils.isDisabled(output) || targetHref == null)
        {
            //output.getAttributes().put(END_LINK_OUTCOME_AS_SPAN, Boolean.TRUE);
            //Note one h:link cannot have a nested h:link as a child, so it is safe
            //to just put this flag on FacesContext attribute map
            facesContext.getAttributes().put(END_LINK_OUTCOME_AS_SPAN, Boolean.TRUE);
            writer.startElement(HTML.SPAN_ELEM, output);
            if (output instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) output).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, output, facesContext);
                }
                else
                {
                    HtmlRendererUtils.writeIdIfNecessary(writer, output, facesContext);
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(output);
                }
                if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderEventProperties(writer, 
                            commonPropertiesMarked, output);
                    CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                            commonPropertiesMarked, output);
                }
                else
                {
                    if (isCommonEventsOptimizationEnabled(facesContext))
                    {
                        Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(output);
                        CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                                commonPropertiesMarked, commonEventsMarked, output, behaviors);
                        CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                            facesContext, writer, commonPropertiesMarked, commonEventsMarked, output, behaviors);
                    }
                    else
                    {
                        HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, output, behaviors);
                        HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, output, behaviors);
                    }
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabledWithoutEvents(writer, 
                            commonPropertiesMarked, output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED_WITHOUT_EVENTS);
                }
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, output, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesDisabled(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(output), output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_DISABLED);
                }
            }

            Object value = output.getValue();

            if(value != null)
            {
                writer.writeText(value.toString(), ComponentAttrs.VALUE_ATTR);
            }
        }
        else
        {
            //write anchor
            writer.startElement(HTML.ANCHOR_ELEM, output);
            writer.writeURIAttribute(HTML.HREF_ATTR, targetHref, null);
            if (output instanceof ClientBehaviorHolder)
            {
                behaviors = ((ClientBehaviorHolder) output).getClientBehaviors();
                if (!behaviors.isEmpty())
                {
                    HtmlRendererUtils.writeIdAndName(writer, output, facesContext);
                }
                else
                {
                    HtmlRendererUtils.writeIdAndNameIfNecessary(writer, output, facesContext);
                }
                long commonPropertiesMarked = 0L;
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(output);
                }
                if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderEventProperties(writer, 
                            commonPropertiesMarked, output);
                    CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,
                            commonPropertiesMarked, output);
                }
                else
                {
                    if (isCommonEventsOptimizationEnabled(facesContext))
                    {
                        Long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(output);
                        CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                                commonPropertiesMarked, commonEventsMarked, output, behaviors);
                        CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                            facesContext, writer, commonPropertiesMarked, commonEventsMarked, output, behaviors);
                    }
                    else
                    {
                        HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, output, behaviors);
                        HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                                facesContext, writer, output, behaviors);
                    }
                }
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughPropertiesWithoutEvents(writer, 
                            commonPropertiesMarked, output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
                }
            }
            else
            {
                HtmlRendererUtils.writeIdAndNameIfNecessary(writer, output, facesContext);
                if (isCommonPropertiesOptimizationEnabled(facesContext))
                {
                    CommonHtmlAttributesUtil.renderAnchorPassthroughProperties(writer, 
                            CommonHtmlAttributesUtil.getMarkedAttributes(output), output);
                }
                else
                {
                    HtmlRendererUtils.renderHTMLAttributes(writer, output, 
                            HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES);
                }
            }

            writer.flush();
        }
    }

    private static void addParameterToHref(String name,
                                           Object value,
                                           StringBuilder hrefBuf,
                                           boolean firstParameter,
                                           String charEncoding,
                                           boolean strictXhtmlLinks) throws UnsupportedEncodingException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Unnamed parameter value not allowed within command link.");
        }

        if (firstParameter)
        {
            hrefBuf.append('?');
        }
        else
        {
            if (strictXhtmlLinks)
            {
                hrefBuf.append("&amp;");
            }
            else
            {
                hrefBuf.append('&');
            }
        }

        hrefBuf.append(URLEncoder.encode(name, charEncoding));
        hrefBuf.append('=');
        if (value != null)
        {
            //UIParameter is no ConvertibleValueHolder, so no conversion possible
            hrefBuf.append(URLEncoder.encode(value.toString(), charEncoding));
        }
    }

    protected void renderOutcomeLinkEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        
        if (HtmlRendererUtils.isDisabled(component) || Boolean.TRUE.equals(
                facesContext.getAttributes().get(END_LINK_OUTCOME_AS_SPAN)))
        {
            writer.endElement(HTML.SPAN_ELEM);
            facesContext.getAttributes().put(END_LINK_OUTCOME_AS_SPAN, Boolean.FALSE);
        }
        else
        {
            writer.writeText (RendererUtils.getStringValue(facesContext, component), null);
            writer.endElement(HTML.ANCHOR_ELEM);
        }
    }
    
    protected void renderOutputLinkEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        if (HtmlRendererUtils.isDisabled(component))
        {
            writer.endElement(HTML.SPAN_ELEM);
        }
        else
        {
            // force separate end tag
            writer.writeText("", null);
            writer.endElement(HTML.ANCHOR_ELEM);
        }
    }

    protected void renderCommandLinkEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        UIForm form = ComponentUtils.findClosest(UIForm.class, component);
        
        ResponseWriter writer = facesContext.getResponseWriter();
        if (HtmlRendererUtils.isDisabled(component) || form == null)
        {
            writer.endElement(HTML.SPAN_ELEM);
        }
        else
        {
            writer.writeText("", null);
            writer.endElement(HTML.ANCHOR_ELEM);
        }
    }
}
