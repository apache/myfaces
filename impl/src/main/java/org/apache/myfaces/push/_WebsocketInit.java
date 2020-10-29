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

import java.util.List;
import jakarta.faces.component.UIOutput;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * This component is the one that render the initialization at the end of body section.
 * 
 * The idea is it works like a buffer that collects all markup generated by 
 * UIWebsocket and then render it at the end of body section. In that way it 
 * is possible to preserve the context and ensure the script is called when the page
 * is loaded.
 */
@JSFComponent(template=true,
        clazz = "org.apache.myfaces.push.WebsocketInit",
        family = "jakarta.faces.Output",
        type = "org.apache.myfaces.WebsocketInit",
        defaultRendererType="org.apache.myfaces.WebsocketInit")
public abstract class _WebsocketInit extends UIOutput
{
    static public final String ATTRIBUTE_COMPONENT_ID = _WebsocketInit.class.getName() + "ComponentId";
    
    /**
     * List that stores the rendered markup of previous UIWebsocket instances.
     */
    private transient List<String> websocketComponentMarkupList = new java.util.ArrayList<String>();
    
    public List<String> getUIWebsocketMarkupList()
    {
        return websocketComponentMarkupList;
    }
}