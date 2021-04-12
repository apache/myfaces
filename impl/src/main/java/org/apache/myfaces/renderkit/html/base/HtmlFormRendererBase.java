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
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.core.api.shared.lang.SharedStringBuilder;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

public class HtmlFormRendererBase extends HtmlRenderer
{
    private static final String FORM_TARGET = HTML.FORM_ELEM;
    private static final String HIDDEN_SUBMIT_INPUT_SUFFIX = "_SUBMIT";
    private static final String HIDDEN_SUBMIT_INPUT_VALUE = "1";

    private static final String SCROLL_HIDDEN_INPUT = "org.apache.myfaces.SCROLL_HIDDEN_INPUT";

    private static final String SHARED_STRING_BUILDER = HtmlFormRendererBase.class.getName() + ".SHARED_STRING_BUILDER";
    
    private MyfacesConfig myfacesConfig;
    
    public HtmlFormRendererBase()
    {
        myfacesConfig = MyfacesConfig.getCurrentInstance();
    }
    
    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, UIForm.class);

        UIForm htmlForm = (UIForm)component;

        ResponseWriter writer = facesContext.getResponseWriter();
        String clientId = htmlForm.getClientId(facesContext);
        String acceptCharset = getAcceptCharset(facesContext, htmlForm);
        String actionURL = getActionUrl(facesContext, htmlForm);
        String method = getMethod(facesContext, htmlForm);

        Map<String, List<ClientBehavior>> behaviors = null;
        if (component instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
            }
        }
        
        writer.startElement(HTML.FORM_ELEM, htmlForm);
        writer.writeAttribute(HTML.ID_ATTR, clientId, null);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        writer.writeAttribute(HTML.METHOD_ATTR, method, null);
        if (acceptCharset != null)
        {
            writer.writeAttribute(HTML.ACCEPT_CHARSET_ATTR, acceptCharset, null);
        }
        
        String encodedActionURL = facesContext.getExternalContext().encodeActionURL(actionURL);
        writer.writeURIAttribute(HTML.ACTION_ATTR, encodedActionURL, null);
        
        if (htmlForm instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) htmlForm).getClientBehaviors();
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderEventProperties(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(htmlForm), htmlForm);
            }
            else
            {
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                           CommonHtmlAttributesUtil.getMarkedAttributes(htmlForm),
                           CommonHtmlEventsUtil.getMarkedEvents(htmlForm), htmlForm, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, htmlForm, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderCommonPassthroughPropertiesWithoutEvents(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
                HtmlRendererUtils.renderHTMLAttributes(writer, htmlForm, HTML.FORM_ATTRIBUTES);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, htmlForm, 
                        HTML.FORM_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
            }
        }
        else
        {
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderCommonPassthroughProperties(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(component), component);
                HtmlRendererUtils.renderHTMLAttributes(writer, htmlForm, HTML.FORM_ATTRIBUTES);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, htmlForm, HTML.FORM_PASSTHROUGH_ATTRIBUTES);
            }
        }

        writer.write(""); // force start element tag to be closed

        String encodedPartialActionURL =  facesContext.getExternalContext().encodePartialActionURL(actionURL);
        
        if (encodedActionURL != null && encodedPartialActionURL != null
            && (!encodedActionURL.equals(encodedPartialActionURL)))
        {
            HtmlRendererUtils.renderHiddenInputField(writer, "jakarta.faces.encodedURL", encodedPartialActionURL);
        }
        
        // not needed in this version as nothing is written to the form tag, but
        // included for backward compatibility to the 1.1.1 patch (JIRA MYFACES-1276)
        // However, might be needed in the future
        beforeFormElementsStart(facesContext, component);

        if (myfacesConfig.isRenderFormViewStateAtBegin())
        {
            renderViewStateAndHiddenFields(facesContext, component);
        }
        
        afterFormElementsStart(facesContext, component);
    }

    protected String getActionUrl(FacesContext facesContext, UIForm form)
    {
        return getActionUrl(facesContext);
    }

    protected String getMethod(FacesContext facesContext, UIForm form)
    {
        return "post";
    }

    protected String getAcceptCharset(FacesContext facesContext, UIForm form )
    {
        return (String)form.getAttributes().get(ComponentAttrs.ACCEPTCHARSET_ATTR );
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        beforeFormElementsEnd(facesContext, component);

        if (!myfacesConfig.isRenderFormViewStateAtBegin())
        {
            renderViewStateAndHiddenFields(facesContext, component);
        }

        afterFormElementsEnd(facesContext, component);
        
        writer.endElement(HTML.FORM_ELEM);
    }
    
    protected void renderViewStateAndHiddenFields(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        
        //write hidden input to determine "submitted" value on decode
        writer.startElement(HTML.INPUT_ELEM, null); // component);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        StringBuilder sb = SharedStringBuilder.get(facesContext, SHARED_STRING_BUILDER);
        writer.writeAttribute(HTML.NAME_ATTR, sb.append(component.getClientId(facesContext)).
                                              append(HIDDEN_SUBMIT_INPUT_SUFFIX).toString(), null);
        writer.writeAttribute(HTML.VALUE_ATTR, HIDDEN_SUBMIT_INPUT_VALUE, null);
        writer.endElement(HTML.INPUT_ELEM);

        renderScrollHiddenInputIfNecessary(component, facesContext, writer);

        //write state marker at the end of the form
        //Todo: this breaks client-side enabled AJAX components again which are searching for the state
        //we'll need to fix this
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        viewHandler.writeState(facesContext);

        List<UIComponent> componentResources = facesContext.getViewRoot().getComponentResources(facesContext,
            FORM_TARGET);
        
        for (int i = 0, size = componentResources.size(); i < size; i++)
        {
           UIComponent child = componentResources.get(i);
           child.encodeAll (facesContext);
        }
    }

    private static String getScrollHiddenInputName(FacesContext facesContext, UIComponent form)
    {
        StringBuilder sb = SharedStringBuilder.get(facesContext, SHARED_STRING_BUILDER,
                SCROLL_HIDDEN_INPUT.length() + 20);
        sb.append(SCROLL_HIDDEN_INPUT);
        sb.append('_');
        sb.append(form.getClientId(facesContext));
        return sb.toString();
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        RendererUtils.checkParamValidity(facesContext, component, UIForm.class);

        UIForm htmlForm = (UIForm)component;

        Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
        // Perf: initialize StringBuilder to maximal lenght used in this renderer, render_response
        // method will re-use it without capacity expanding 
        StringBuilder sb = SharedStringBuilder.get(facesContext, SHARED_STRING_BUILDER, 100);
        String submittedValue = (String) paramMap.get(
                sb.append(component.getClientId(facesContext)).append(HIDDEN_SUBMIT_INPUT_SUFFIX));
        if (submittedValue != null && submittedValue.equals(HIDDEN_SUBMIT_INPUT_VALUE))
        {
            htmlForm.setSubmitted(true);
        }
        else
        {
            htmlForm.setSubmitted(false);
        }
        
        ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
    }


    public static void renderScrollHiddenInputIfNecessary(
            UIComponent form, FacesContext facesContext, ResponseWriter writer)
        throws IOException
    {
        if (form == null)
        {
            return;
        }

        if (facesContext.getExternalContext().getRequestMap().get(
                getScrollHiddenInputName(facesContext, form)) == null)
        {
            facesContext.getExternalContext().getRequestMap().put(getScrollHiddenInputName(
                    facesContext, form), Boolean.TRUE);
        }
    }

    /**
     * Called before the state and any elements are added to the form tag in the
     * encodeBegin method
     */
    protected void beforeFormElementsStart(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        
    }

    /**
     * Called after the state and any elements are added to the form tag in the
     * encodeBegin method
     */
    protected void afterFormElementsStart(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        
    }

    /**
     * Called before the state and any elements are added to the form tag in the
     * encodeEnd method
     */
    protected void beforeFormElementsEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        
    }

    /**
     * Called after the state and any elements are added to the form tag in the
     * encodeEnd method
     */
    protected void afterFormElementsEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        
    }
}
