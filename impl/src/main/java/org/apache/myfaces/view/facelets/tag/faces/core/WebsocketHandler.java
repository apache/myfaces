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

package org.apache.myfaces.view.facelets.tag.faces.core;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.push._WebsocketInit;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

@JSFFaceletTag(
        name = "f:websocket",
        bodyContent = "empty")
public class WebsocketHandler extends ComponentHandler
{

    @JSFFaceletAttribute(name = "channel", className = "jakarta.el.ValueExpression",
                         deferredValueType = "java.lang.String")
    private final TagAttribute _channel;

    public WebsocketHandler(ComponentConfig config)
    {
        super(config);
        _channel = getRequiredAttribute("channel");
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent)
    {
        UIWebsocket component = (UIWebsocket) c;
        component.getAttributes().put(_WebsocketInit.ATTRIBUTE_COMPONENT_ID, 
                ComponentSupport.getViewRoot(ctx, parent).createUniqueId()+"_wsinit");
    }
    
}
