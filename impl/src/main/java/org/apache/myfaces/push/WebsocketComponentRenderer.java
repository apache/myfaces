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
package org.apache.myfaces.push;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.push.cdi.WebsocketChannelMetadata;
import org.apache.myfaces.push.cdi.WebsocketChannelTokenBuilder;
import org.apache.myfaces.push.cdi.WebsocketScopeManager;
import org.apache.myfaces.push.cdi.WebsocketSessionManager;
import org.apache.myfaces.renderkit.html.util.ClientBehaviorRendererUtils;
import org.apache.myfaces.renderkit.html.util.HTML;
import org.apache.myfaces.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;

@JSFRenderer(renderKitId = "HTML_BASIC",
        family = "jakarta.faces.Script",
        type = "jakarta.faces.Websocket")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class WebsocketComponentRenderer extends Renderer implements ComponentSystemEventListener
{
    private static final Logger LOG = Logger.getLogger(WebsocketComponentRenderer.class.getName());

    @Override
    public void processEvent(ComponentSystemEvent event)
    {
        if (event instanceof PostAddToViewEvent)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            UIWebsocket component = (UIWebsocket) event.getComponent();
            WebsocketInit initComponent = (WebsocketInit) facesContext.getViewRoot().findComponent(
                    (String) component.getAttributes().get(_WebsocketInit.ATTRIBUTE_COMPONENT_ID));
            if (initComponent == null)
            {
                initComponent = (WebsocketInit) facesContext.getApplication().createComponent(facesContext,
                        WebsocketInit.COMPONENT_TYPE, WebsocketInit.COMPONENT_TYPE);
                initComponent.setId((String) component.getAttributes().get(_WebsocketInit.ATTRIBUTE_COMPONENT_ID));
                facesContext.getViewRoot().addComponentResource(facesContext,
                        initComponent, "body");
            }
        }
    }

    private HtmlBufferResponseWriterWrapper getResponseWriter(FacesContext context)
    {
        return HtmlBufferResponseWriterWrapper.getInstance(context.getResponseWriter());
    }

    @Override
    public void decode(FacesContext facesContext, UIComponent component)
    {
        ClientBehaviorRendererUtils.decodeClientBehaviors(facesContext, component);
    }

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        ResourceUtils.renderDefaultJsfJsInlineIfNecessary(facesContext, writer);
        
        // Render the tag that will be embedded into the DOM tree that helps to detect if the message
        // must be processed or not and if the connection must be closed.
        writer.startElement(HTML.DIV_ELEM, component);
        writer.writeAttribute(HTML.ID_ATTR, component.getClientId() ,null);
        writer.writeAttribute(HTML.STYLE_ATTR, "display:none", null);
        writer.endElement(HTML.DIV_ELEM);
        
        if (!facesContext.getPartialViewContext().isAjaxRequest())
        {
            facesContext.setResponseWriter(getResponseWriter(facesContext));
        }
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent c) throws IOException
    {
        super.encodeEnd(facesContext, c); //check for NP

        UIWebsocket component = (UIWebsocket) c;

        WebsocketInit init = (WebsocketInit) facesContext.getViewRoot().findComponent(
                (String) component.getAttributes().get(_WebsocketInit.ATTRIBUTE_COMPONENT_ID));

        ResponseWriter writer = facesContext.getResponseWriter();

        String channel = component.getChannel();

        // TODO: use a single bean and entry point for this algorithm.
        BeanManager beanManager = CDIUtils.getBeanManager(facesContext);

        WebsocketChannelTokenBuilder channelTokenBuilder =
                CDIUtils.get(beanManager, WebsocketChannelTokenBuilder.class);

        // Create channel token 
        // TODO: Use ResponseStateManager to create the token
        // The default scope is application. When the user attribute is specified, then the default scope is session.
        String scope = component.getScope();
        if (scope == null)
        {
            scope = (component.getUser() == null ?
                    WebsocketScopeManager.SCOPE_APPLICATION : WebsocketScopeManager.SCOPE_SESSION);
        }

        WebsocketChannelMetadata metadata = new WebsocketChannelMetadata(
                channel, scope, component.getUser(), component.isConnected());

        WebsocketScopeManager scopeManager = CDIUtils.get(beanManager, WebsocketScopeManager.class);

        // try to find an existing channelToken
        String channelToken = null;
        if (scopeManager.getScope(scope, true).isChannelAvailable(channel))
        {
            if (component.getUser() == null)
            {
                List<String> channelTokenList = scopeManager.getScope (scope, true).getChannelTokens(channel);
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE, "WebsocketComponentRenderer.encodeEnd: for channel = {0} found : ",
                            channel);
                    channelTokenList.forEach (p -> LOG.log(Level.FINE, "  {0}", p));
                }
                // should be just one
                if (channelTokenList.size() == 1)
                {
                    channelToken = channelTokenList.get(0);
                }
            }
            else
            {
                List<String> channelTokenList = scopeManager.getScope (scope, true)
                        .getChannelTokens(channel, component.getUser());
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.log(Level.FINE,
                            "WebsocketComponentRenderer.encodeEnd: for channel = {0}, user = {1} found : ",
                            new Object[] {channel, component.getUser()});
                    channelTokenList.forEach (p -> LOG.log(Level.FINE, "  {0}", p));
                }
                // should be just one for combination channel / user
                if (channelTokenList.size() == 1)
                {
                    channelToken = channelTokenList.get(0);
                }

            }
        }

        // Create channel token if needed
        // TODO: Use ResponseStateManager to create the token
        // Force a new channelToken if "connected" property is set to false, because in that case websocket creation
        if (!component.isConnected())
        {
            // This bean is required because you always need to register the token, so it can be properly destroyed
            channelToken = scopeManager.getViewScope(true).getChannelToken(metadata);
        }
        if (channelToken == null)
        {
            // No channel token found for that combination, create a new token for this view
            channelToken = channelTokenBuilder.createChannelToken(facesContext, channel);
            scopeManager.getScope(scope, true).registerWebsocketSession(channelToken, metadata);
        }

        // Register channel in view scope to chain discard view algorithm using @PreDestroy
        scopeManager.getViewScope(true).registerToken(channelToken, metadata);

        // Register channel in session scope to allow validation on handshake  WebsocketConfigurator)
        scopeManager.getSessionScope(true).registerToken(channelToken, metadata);

        // Prepare channelToken to websocket sessionMap (real session will be connected later)
        WebsocketSessionManager sessionManager = CDIUtils.get(beanManager, WebsocketSessionManager.class);
        sessionManager.registerSessionToken(channelToken);

        writer.startElement(HTML.SCRIPT_ELEM, component);
        HtmlRendererUtils.renderScriptType(facesContext, writer);

        StringBuilder sb = new StringBuilder(50);
        sb.append("faces.push.init(");
        sb.append('\'');
        sb.append(component.getClientId());
        sb.append('\'');
        sb.append(',');
        sb.append('\'');
        sb.append(facesContext.getExternalContext().encodeWebsocketURL(
                facesContext.getApplication().getViewHandler().getWebsocketURL(
                        facesContext, component.getChannel()+ '?' +channelToken)));
        sb.append('\'');
        sb.append(',');
        sb.append('\'');
        sb.append(component.getChannel());
        sb.append('\'');
        sb.append(',');
        sb.append(component.getOnopen());
        sb.append(',');
        sb.append(component.getOnmessage());
        sb.append(',');
        sb.append(component.getOnerror());
        sb.append(',');
        sb.append(component.getOnclose());
        sb.append(',');
        sb.append(getBehaviorScripts(facesContext, component));
        sb.append(',');
        sb.append(component.isConnected());
        sb.append(");");

        writer.write(sb.toString());

        writer.endElement(HTML.SCRIPT_ELEM);
        
        if (!facesContext.getPartialViewContext().isAjaxRequest())
        {
            ResponseWriter responseWriter = facesContext.getResponseWriter();
            while (!(responseWriter instanceof HtmlBufferResponseWriterWrapper)
                    && responseWriter instanceof FacesWrapper)
            {
                responseWriter = (ResponseWriter) ((FacesWrapper) responseWriter).getWrapped();
            }
            
            HtmlBufferResponseWriterWrapper htmlBufferResponseWriter =
                    (HtmlBufferResponseWriterWrapper) responseWriter;
            init.getUIWebsocketMarkupList().add(htmlBufferResponseWriter.toString());

            facesContext.setResponseWriter(htmlBufferResponseWriter.getInitialWriter());
        }
    }

    private String getBehaviorScripts(FacesContext facesContext, UIWebsocket component)
    {
        Map<String, List<ClientBehavior>> clientBehaviorsByEvent = component.getClientBehaviors();

        if (clientBehaviorsByEvent.isEmpty())
        {
            return "{}";
        }

        String clientId = component.getClientId(facesContext);
        StringBuilder scripts = new StringBuilder("{");

        for (Entry<String, List<ClientBehavior>> entry : clientBehaviorsByEvent.entrySet())
        {
            String event = entry.getKey();
            List<ClientBehavior> clientBehaviors = entry.getValue();
            scripts.append(scripts.length() > 1 ? ',' : "").append(event).append(":[");

            for (int i = 0; i < clientBehaviors.size(); i++)
            {
                scripts.append(i > 0 ? ',' : "").append("function(event){");
                scripts.append(clientBehaviors.get(i).getScript(
                        ClientBehaviorContext.createClientBehaviorContext(
                                facesContext, component, event, clientId, null)));
                scripts.append('}');
            }

            scripts.append(']');
        }

        return scripts.append('}').toString();
    }
        
}
