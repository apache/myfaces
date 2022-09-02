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
import java.io.Serializable;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.event.WebsocketEvent;
import jakarta.faces.push.PushContext;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.push.cdi.WebsocketSessionManager;
import org.apache.myfaces.util.lang.Lazy;

/**
 *
 */
public class EndpointImpl extends Endpoint
{
    public static final String JAKARTA_FACES_PUSH_PATH = PushContext.URI_PREFIX + "/{channel}";
    public static final String PUSH_CHANNEL_PARAMETER = "channel";

    private static final Logger LOG = Logger.getLogger(EndpointImpl.class.getName());

    private Lazy<BeanManager> beanManager = new Lazy<>(() -> CDI.current().getBeanManager());
    
    @Override
    public void onOpen(Session session, EndpointConfig config)
    {
        // Get the channel and the channel id
        String channel = session.getPathParameters().get(PUSH_CHANNEL_PARAMETER);
        String channelToken = session.getQueryString();

        // Note in this point there is no session scope because there is no HttpSession available,
        // but on the handshake there is.
        // So, everything below should use CDI @ApplicationScoped beans only.
        WebsocketSessionManager sessionManager = CDIUtils.get(beanManager.get(), WebsocketSessionManager.class);
        
        if (Boolean.TRUE.equals(config.getUserProperties().get(WebsocketConfigurator.WEBSOCKET_VALID)) &&
                sessionManager.addOrUpdateSession(channelToken, session))
        {
            session.setMaxIdleTimeout((Long) config.getUserProperties().getOrDefault(
                    WebsocketConfigurator.MAX_IDLE_TIMEOUT, 300000L));

            Serializable user = (Serializable) session.getUserProperties().get(WebsocketConfigurator.WEBSOCKET_USER);

            beanManager.get().getEvent()
                    .select(WebsocketEvent.Opened.Literal.INSTANCE)
                    .fire(new WebsocketEvent(channel, user, null));

            session.getUserProperties().put(
                    WebsocketSessionClusterSerializedRestore.WEBSOCKET_SESSION_SERIALIZED_RESTORE, 
                    new WebsocketSessionClusterSerializedRestore(channelToken));
        }
        else
        {
            try
            {
                session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION,
                        "Websocket connection not registered in current session"));
            }
            catch (IOException ex)
            {
                onError(session, ex);
            }
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason)
    {
        // Get the channel and the channel id
        String channel = session.getPathParameters().get(PUSH_CHANNEL_PARAMETER);
        String channelToken = session.getQueryString();

        Serializable user = (Serializable) session.getUserProperties().get(WebsocketConfigurator.WEBSOCKET_USER);

        if (!beanManager.isInitialized())
        {
            try
            {
                // onclose likely runs with another TCCL, so CDI.current might fail
                // we hope that #onOpen was executed before
                beanManager.get();
            }
            catch (Exception e)
            {
                LOG.log(Level.WARNING,
                        "Could not lazy initialize BeanManager on Endpoint#close, skip deregister session...", e);
                return;
            }
        }

        WebsocketSessionManager sessionManager = CDIUtils.get(beanManager.get(), WebsocketSessionManager.class);
        sessionManager.removeSession(channelToken);

        beanManager.get().getEvent()
                .select(WebsocketEvent.Closed.Literal.INSTANCE)
                .fire(new WebsocketEvent(channel, user, closeReason.getCloseCode()));
    }

    @Override
    public void onError(Session session, Throwable ex)
    {
        if (session.isOpen())
        {
            session.getUserProperties().put(Throwable.class.getName(), ex);
        }
    }
}
