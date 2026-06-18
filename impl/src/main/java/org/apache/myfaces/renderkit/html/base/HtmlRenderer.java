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


import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.myfaces.renderkit.html.util.CommonHtmlAttributesUtil;
import org.apache.myfaces.renderkit.html.util.CommonHtmlEventsUtil;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ComponentAttrs;

public abstract class HtmlRenderer
        extends Renderer
{

    /**
     * Return the list of children of the specified component.
     * <p>
     * This default implementation simply returns component.getChildren().
     * However this method should always be used in order to allow
     * renderer subclasses to override it and provide filtered or
     * reordered views of the component children to rendering
     * methods defined in their ancestor classes.
     * <p>
     * Any method that overrides this to "hide" child components
     * should also override the getChildCount method.
     * 
     * @return a list of UIComponent objects.
     */
    public List<UIComponent> getChildren(UIComponent component) 
    {
        if (component.getChildCount() == 0)
        {
            return null;
        }
        
        return component.getChildren();
    }

    /**
     * Return the number of children of the specified component.
     * <p>
     * See {@link #getChildren(UIComponent)} for more information.
     */
    public int getChildCount(UIComponent component) 
    {
        return component.getChildCount();
    }
    
    /**
     * @param facesContext
     * @return String A String representing the action URL
     */
    protected String getActionUrl(FacesContext facesContext)
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        return viewHandler.getActionURL(facesContext, viewId);
    }

    /**
     * Renders the client ID as an "id".
     */
    protected void renderId(FacesContext context, UIComponent  component) throws IOException
    {
        if (shouldRenderId(context, component))
        {
            String clientId = getClientId(context, component);
            context.getResponseWriter().writeAttribute(HTML.ID_ATTR, clientId, ComponentAttrs.ID_ATTR);
        }
    }

    /**
     * Returns the client ID that should be used for rendering (if
     * {@link #shouldRenderId} returns true).
     */
    protected String getClientId(FacesContext context, UIComponent component)
    {
        return component.getClientId(context);
    }

    /**
     * Returns true if the component should render an ID.  Components
     * that deliver events should always return "true".
     */
    protected boolean shouldRenderId(FacesContext context, UIComponent  component)
    {
        String id = component.getId();
        // Otherwise, if ID isn't set, don't bother
        if (id == null)
        {
            return false;
        }

        // ... or if the ID was generated, don't bother
        if (id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
            return false;
        }

        return true;
    }
    
    protected boolean isCommonPropertiesOptimizationEnabled(FacesContext facesContext)
    {
        return false;
    }
    
    protected boolean isCommonEventsOptimizationEnabled(FacesContext facesContext)
    {
        return false;
    }

    /**
     * Returns the registered client behaviors for {@code component} if it implements
     * {@link jakarta.faces.component.behavior.ClientBehaviorHolder}, or {@code null}
     * otherwise. A non-null return value replaces a separate {@code boolean hasClientBehaviors}
     * flag — callers can do a single null check instead of carrying two variables.
     */
    protected Map<String, List<ClientBehavior>> getClientBehaviors(UIComponent component)
    {
        return component instanceof ClientBehaviorHolder holder
                ? holder.getClientBehaviors()
                : null;
    }

    /**
     * Returns the bitmask of common attributes set on {@code component} when the
     * common-properties optimization is enabled, or {@code null} when the optimization
     * is disabled (indicating the generic attribute-array path should be used instead).
     */
    protected Long getCommonPropertiesMarked(FacesContext facesContext, UIComponent component)
    {
        return isCommonPropertiesOptimizationEnabled(facesContext)
                ? CommonHtmlAttributesUtil.getMarkedAttributes(component)
                : null;
    }

    /**
     * Returns the bitmask of common events registered on {@code component} when the
     * common-events optimization is enabled, or {@code null} when it is disabled
     * (indicating the generic behaviorized-event rendering path should be used instead).
     */
    protected Long getCommonEventsMarked(FacesContext facesContext, UIComponent component)
    {
        return isCommonEventsOptimizationEnabled(facesContext)
                ? CommonHtmlEventsUtil.getMarkedEvents(component)
                : null;
    }

    /**
     * Renders generic event handlers (onclick, onmouseXxx, onkeyXxx, …) for a component
     * that may carry client behaviors. Uses the bitmask-only path when no behaviors are
     * registered and {@code commonPropertiesMarked} is non-null (optimization enabled);
     * otherwise merges attributes with the registered behaviors.
     *
     * @param commonPropertiesMarked bitmask from {@link #getCommonPropertiesMarked}, or
     *                               {@code null} when the optimization is disabled
     */
    protected void renderEventHandlers(FacesContext facesContext, ResponseWriter writer,
            UIComponent component, Map<String, List<ClientBehavior>> behaviors,
            Long commonPropertiesMarked) throws IOException
    {
        if (behaviors.isEmpty() && commonPropertiesMarked != null)
        {
            CommonHtmlAttributesUtil.renderEventProperties(writer, commonPropertiesMarked, component);
        }
        else
        {
            Long commonEventsMarked = getCommonEventsMarked(facesContext, component);
            if (commonEventsMarked != null)
            {
                CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer,
                        commonPropertiesMarked, commonEventsMarked, component, behaviors);
            }
            else
            {
                HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, component, behaviors);
            }
        }
    }

    /**
     * Renders event handlers for an input/field element: onchange, generic events
     * (onclick, onmouseXxx, onkeyXxx, …), and field events (onfocus, onblur, onselect).
     * Uses the bitmask-only path when no behaviors are registered and
     * {@code commonPropertiesMarked} is non-null (optimization enabled); otherwise merges
     * attributes with the registered behaviors.
     *
     * @param commonPropertiesMarked bitmask from {@link #getCommonPropertiesMarked}, or
     *                               {@code null} when the optimization is disabled
     */
    protected void renderFieldEventHandlers(FacesContext facesContext, ResponseWriter writer,
            UIComponent component, Map<String, List<ClientBehavior>> behaviors,
            Long commonPropertiesMarked) throws IOException
    {
        if (behaviors.isEmpty() && commonPropertiesMarked != null)
        {
            CommonHtmlAttributesUtil.renderChangeEventProperty(writer, commonPropertiesMarked, component);
            CommonHtmlAttributesUtil.renderEventProperties(writer, commonPropertiesMarked, component);
            CommonHtmlAttributesUtil.renderFieldEventPropertiesWithoutOnchange(
                    writer, commonPropertiesMarked, component);
        }
        else
        {
            HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, component, behaviors);
            Long commonEventsMarked = getCommonEventsMarked(facesContext, component);
            if (commonEventsMarked != null)
            {
                CommonHtmlEventsUtil.renderBehaviorizedEventHandlers(facesContext, writer,
                        commonPropertiesMarked, commonEventsMarked, component, behaviors);
                CommonHtmlEventsUtil.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                        facesContext, writer, commonPropertiesMarked, commonEventsMarked, component, behaviors);
            }
            else
            {
                HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, component, behaviors);
                HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(
                        facesContext, writer, component, behaviors);
            }
        }
    }
}
