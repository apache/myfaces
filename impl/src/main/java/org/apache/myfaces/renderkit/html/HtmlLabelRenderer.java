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
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.component.search.MyFacesSearchExpressionHints;
import org.apache.myfaces.core.api.shared.AttributeUtils;
import org.apache.myfaces.core.api.shared.ComponentUtils;
import org.apache.myfaces.renderkit.RendererUtils;
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.base.HtmlRenderer;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

/**
 * 
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Anton Koinov
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "jakarta.faces.Output", type = "jakarta.faces.Label")
public class HtmlLabelRenderer extends HtmlRenderer
{
    private static final Logger log = Logger.getLogger(HtmlLabelRenderer.class.getName());
    
    @Override
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return true;
    }

    @Override
    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);  // check for NP
        
        ClientBehaviorRendererUtils.decodeClientBehaviors(context, component);
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        super.encodeBegin(facesContext, uiComponent); // check for NP

        ResponseWriter writer = facesContext.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            if (!behaviors.isEmpty())
            {
                ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
            }
        }
        
        encodeBefore(facesContext, writer, uiComponent);

        writer.startElement(HTML.LABEL_ELEM, uiComponent);
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            if (!behaviors.isEmpty())
            {
                HtmlRendererUtils.writeIdAndName(writer, uiComponent, facesContext);
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
            }
            long commonPropertiesMarked = 0L;
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                commonPropertiesMarked = CommonHtmlAttributesUtil.getMarkedAttributes(uiComponent);
            }
            if (behaviors.isEmpty() && isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderEventProperties(writer, commonPropertiesMarked, uiComponent);
                CommonHtmlAttributesUtil.renderFocusBlurEventProperties(writer,commonPropertiesMarked, uiComponent);
            }
            else
            {
                if (isCommonEventsOptimizationEnabled(facesContext))
                {
                    long commonEventsMarked = CommonHtmlEventsUtil.getMarkedEvents(uiComponent);
                    CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer, 
                            commonPropertiesMarked, commonEventsMarked, uiComponent, behaviors);
                    CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
                        facesContext, writer, commonPropertiesMarked, commonEventsMarked, uiComponent, behaviors);
                }
                else
                {
                    HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
                    HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(facesContext,
                            writer, uiComponent, behaviors);
                }
            }
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderLabelPassthroughPropertiesWithoutEvents(writer, 
                        commonPropertiesMarked, uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent,
                                                       HTML.LABEL_PASSTHROUGH_ATTRIBUTES_WITHOUT_EVENTS);
            }
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
            if (isCommonPropertiesOptimizationEnabled(facesContext))
            {
                CommonHtmlAttributesUtil.renderLabelPassthroughProperties(writer, 
                        CommonHtmlAttributesUtil.getMarkedAttributes(uiComponent), uiComponent);
            }
            else
            {
                HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.LABEL_PASSTHROUGH_ATTRIBUTES);
            }
        }

        String forAttr = getFor(uiComponent);
        if (forAttr != null)
        {
            String forClientId = getClientId(facesContext, uiComponent, forAttr);
            writer.writeAttribute(HTML.FOR_ATTR, forClientId, ComponentAttrs.FOR_ATTR);
        }
        else
        {
            if (log.isLoggable(Level.WARNING))
            {
                log.warning("Attribute 'for' of label "
                        + ComponentUtils.getPathToComponent(uiComponent)
                        + " is not defined");
            }
        }

        // MyFaces extension: Render a label text given by value
        // TODO: Move to extended component
        if (uiComponent instanceof ValueHolder)
        {
            String text = RendererUtils.getStringValue(facesContext, uiComponent);
            if (text != null)
            {
                boolean escape;
                if (uiComponent instanceof HtmlOutputLabel)
                {
                    escape = ((HtmlOutputLabel) uiComponent).isEscape();
                }
                else
                {
                    escape = AttributeUtils.getBooleanAttribute(uiComponent, ComponentAttrs.ESCAPE_ATTR, true);
                }

                if (escape)
                {
                    writer.writeText(text, ComponentAttrs.VALUE_ATTR);
                }
                else
                {
                    writer.write(text);
                }
            }
        }

        writer.flush(); // close start tag

        encodeAfterStart(facesContext, writer, uiComponent);
    }

    protected void encodeAfterStart(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    protected void encodeBefore(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    protected String getFor(UIComponent component)
    {
        if (component instanceof HtmlOutputLabel)
        {
            return ((HtmlOutputLabel)component).getFor();
        }

        return (String) component.getAttributes().get(ComponentAttrs.FOR_ATTR);

    }

    protected String getClientId(FacesContext facesContext, UIComponent uiComponent, String forAttr)
    {
        SearchExpressionContext searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(
                facesContext, uiComponent,
                MyFacesSearchExpressionHints.SET_RESOLVE_SINGLE_COMPONENT_IGNORE_NO_RESULT, null);

        return facesContext.getApplication().getSearchExpressionHandler().resolveClientId(
                searchExpressionContext, forAttr);
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        super.encodeEnd(facesContext, uiComponent); // check for NP

        ResponseWriter writer = facesContext.getResponseWriter();

        encodeBeforeEnd(facesContext, writer, uiComponent);

        writer.endElement(HTML.LABEL_ELEM);

        encodeAfter(facesContext, writer, uiComponent);
    }

    protected void encodeBeforeEnd(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }

    protected void encodeAfter(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent)
        throws IOException
    {
    }
}
