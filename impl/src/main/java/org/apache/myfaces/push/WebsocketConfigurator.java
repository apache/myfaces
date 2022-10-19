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

import java.io.Serializable;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.ExternalContext;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.push.cdi.WebsocketScopeManager;

public class WebsocketConfigurator extends ServerEndpointConfig.Configurator
{

    public static final String MAX_IDLE_TIMEOUT = "oam.websocket.maxIdleTimeout";
    
    public static final String WEBSOCKET_VALID = "oam.websocket.valid";
    
    public static final String WEBSOCKET_USER = "oam.websocket.user";
    
    private final Long maxIdleTimeout;
    
    public WebsocketConfigurator(ExternalContext context)
    {
        this.maxIdleTimeout = MyfacesConfig.getCurrentInstance(context).getWebsocketMaxIdleTimeout();
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
    {
        if (this.maxIdleTimeout != null)
        {
            sec.getUserProperties().put(MAX_IDLE_TIMEOUT, this.maxIdleTimeout);
        }
        
        String channelToken = request.getQueryString();
        if (channelToken == null)
        {
            String uri = request.getRequestURI().toString();
            if (uri.indexOf('?') >= 0)
            {
                channelToken = uri.substring(uri.indexOf('?')+1);
            }
            else
            {
                channelToken = uri.substring(uri.lastIndexOf('/')+1);
            }
        }

        BeanManager beanManager = CDI.current().getBeanManager();
        WebsocketScopeManager scopeManager = CDIUtils.get(beanManager, WebsocketScopeManager.class);
        WebsocketScopeManager.SessionScope sessionScope = scopeManager.getSessionScope(false);
        if (sessionScope != null)
        {
            Serializable user = sessionScope.getUserFromChannelToken(channelToken);
            if (user != null)
            {
                sec.getUserProperties().put(WEBSOCKET_USER, user);
            }
            sec.getUserProperties().put(WEBSOCKET_VALID, sessionScope.isTokenValid(channelToken));
        }
        else
        {
            // Cannot validate session, mark as invalid
            sec.getUserProperties().put(WEBSOCKET_VALID, false);
        }
    }
}
