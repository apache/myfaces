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

import java.util.Collection;
import java.util.Collections;
import javax.faces.component.UIOutput;
import javax.faces.component.behavior.ClientBehaviorHolder;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * This component hold f:websocket client behavior and other properties.
 */
@JSFComponent(
        clazz = "org.apache.myfaces.push.WebsocketComponent",
        family = "javax.faces.Output",
        type = "org.apache.myfaces.WebsocketComponent",
        defaultRendererType="org.apache.myfaces.WebsocketComponent")
public abstract class AbstractWebsocketComponent extends UIOutput implements ClientBehaviorHolder
{
    
    @JSFProperty
    public abstract String getChannel();
    
    @JSFProperty
    public abstract String getScope();
    
    @JSFProperty
    public abstract String getUser();
    
    @JSFProperty
    public abstract String getOnopen();
    
    @JSFProperty
    public abstract String getOnmessage();
    
    @JSFProperty
    public abstract String getOnclose();
    
    @JSFProperty(defaultValue = "true")
    public abstract boolean isConnected();

    @Override
    public Collection<String> getEventNames()
    {
        return Collections.emptyList();
    }
    
}
