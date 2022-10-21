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
package org.apache.myfaces.push.cdi;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.myfaces.cdi.util.CDIUtils;

@ApplicationScoped
public class WebsocketScopeManager
{
    public static final String SCOPE_APPLICATION = "application";
    public static final String SCOPE_SESSION = "session";
    public static final String SCOPE_VIEW = "view";
    
    @Inject private BeanManager beanManager;
    
    public AbstractScope getScope(String scope, boolean create)
    {
        if (SCOPE_APPLICATION.equals(scope))
        {
            return getApplicationScope(create);
        }
        if (SCOPE_SESSION.equals(scope))
        {
            return getSessionScope(create);
        }
        if (SCOPE_VIEW.equals(scope))
        {
            return getViewScope(create);
        }
        
        throw new UnsupportedOperationException("Scope '" + scope + "' not supported!");
    }

    public ApplicationScope getApplicationScope(boolean create)
    {
        return CDIUtils.get(beanManager, ApplicationScope.class, create);
    }
    
    public SessionScope getSessionScope(boolean create)
    {
        return CDIUtils.get(beanManager, SessionScope.class, create);
    }
        
    public ViewScope getViewScope(boolean create)
    {
        return CDIUtils.get(beanManager, ViewScope.class, create);
    }
    
    @ApplicationScoped
    public static class ApplicationScope extends AbstractScope
    {
    }

    /**
     * The purpose of this bean is to keep track of the active tokens and Session instances in the current session,
     * so it can be possible to decide if the token is valid or not for the current session. If the token is not in
     * application scope and is present in session, it means there was a server restart, so the connection must be
     * updated (added to application scope).
     * 
     */
    /**
     * This map holds all tokens related to the current session and its associated metadata, that will
     * be used in the websocket handshake to validate if the incoming request is valid and to store
     * the user object into the Session object.
     */
    @SessionScoped
    public static class SessionScope extends AbstractUserScope implements Serializable
    {
        @Inject private WebsocketSessionManager sessionManager;
        
        @PreDestroy
        public void destroy()
        {
            // When current session scope is about to be destroyed, deregister all session scope channels and
            // explicitly close any open web sockets associated with it to avoid stale websockets.
            // If any, also deregister session users.
            for (Map.Entry<String, WebsocketChannelMetadata> entry : tokens.entrySet())
            {
                // remove channelToken - only if it is session scope
                if (WebsocketScopeManager.SCOPE_SESSION.equals(entry.getValue().getScope()))
                {
                    sessionManager.removeChannelToken(entry.getKey());
                }
            }

            // we dont need to destroy child sockets ("view")
            // this is implemented in @PreDestroy in WebsocketScopeManager.ViewScope
            channelTokens.clear();
            tokens.clear();
        }

        public void destroyChannelToken(String channelToken)
        {
            String channel = null;
            for (Map.Entry<String, List<WebsocketChannel>> entry : channelTokens.entrySet())
            {
                for (Iterator<WebsocketChannel> it = entry.getValue().iterator(); it.hasNext();)
                {
                    WebsocketChannel wschannel = it.next();
                    if (channelToken.equals(wschannel.getChannelToken()))
                    {
                        it.remove();
                        break;
                    }
                }
                if (entry.getValue().isEmpty())
                {
                    channel = entry.getKey();
                }
            }
            if (channel != null)
            {
                channelTokens.remove(channel);
            }
            tokens.remove(channelToken);
        }
    }

    /**
    * This map hold all tokens related to the current view. The reason to do this is the connections must follow
    * the same rules the view has, so if a view is disposed, all related websocket sessions must be disposed too
    * on the server, and in that way we can avoid memory leaks. This bean has a PreDestroy annotation to dispose all
    * related websocket sessions.
    * 
    * This map also enforces a rule that there is only one websocket token pero combination of channel, scope and user
    * per view. In that way, the token can be used to identify on the client if a websocket initialization request
    * can share a websocket connection or not, simplifying code design.
    */
    @ViewScoped
    public static class ViewScope extends AbstractUserScope implements Serializable
    {
        @Inject private WebsocketScopeManager scopeManager;
        @Inject private WebsocketSessionManager sessionManager;
        
        /*
         * If the view is discarded, destroy the websocket sessions associated with the view because they are no
         * longer valid
         */
        @PreDestroy
        public void destroy()
        {
            // destroy parent scope ("session")
            SessionScope sessionScope = (SessionScope) scopeManager.getScope(SCOPE_SESSION, false);
            if (sessionScope != null)
            {
                for (String token : tokens.keySet())
                {
                    sessionScope.destroyChannelToken(token);
                }
            }

            channelTokens.clear();
            tokens.clear();
       }
    }
    
    

    public static abstract class AbstractScope implements Serializable
    {
        /**
         * This map hold all tokens that are related to the current scope. 
         * This map use as key channel and as value channelTokens
         */
        protected Map<String, List<WebsocketChannel>> channelTokens = new ConcurrentHashMap<>(2, 1f);    


        public void registerWebsocketSession(String token, WebsocketChannelMetadata metadata)
        {
            channelTokens.putIfAbsent(metadata.getChannel(), new ArrayList<>(1));
            channelTokens.get(metadata.getChannel()).add(new WebsocketChannel(token, metadata));
        }

        /**
         * Indicate if the channel mentioned is valid for view scope.
         * 
         * A channel is valid if there is at least one token that represents a valid connection to this channel.
         * 
         * @param channel
         * @return 
         */
        public boolean isChannelAvailable(String channel)
        {
            return channelTokens.containsKey(channel);
        }

        public List<String> getChannelTokens(String channel)
        {
            List<WebsocketChannel> list = channelTokens.get(channel);
            if (list != null && !list.isEmpty())
            {
                List<String> value = new ArrayList<>(list.size());
                for (WebsocketChannel md : list)
                {
                    value.add(md.getChannelToken());
                }
                return value;
            }
            return Collections.emptyList();
        }

        public <S extends Serializable> List<String> getChannelTokens(String channel, S user)
        {
            List<WebsocketChannel> list = channelTokens.get(channel);
            if (list != null && !list.isEmpty())
            {
                List<String> value = new ArrayList<>(list.size());
                for (WebsocketChannel md : list)
                {
                    if (user.equals(md.getUser()))
                    {
                        value.add(md.getChannelToken());
                    }
                }
                return value;
            }
            return null;
        }
    }
    
    public static abstract class AbstractUserScope extends AbstractScope
    {
        protected Map<String, WebsocketChannelMetadata> tokens = new ConcurrentHashMap<>(2, 1f);


        public void registerToken(String token, WebsocketChannelMetadata metadata)
        {
            tokens.put(token, metadata);
        }

        public String getChannelToken(WebsocketChannelMetadata metadata)
        {
            if (!metadata.isConnected())
            {
                // Always generate a connection
                return null;
            }
            String token = null;
            for (Map.Entry<String, WebsocketChannelMetadata> entry : tokens.entrySet())
            {
                if (metadata.equals(entry.getValue()))
                {
                    token = entry.getKey();
                    break;
                }
            }
            return token;
        }

        public boolean isTokenValid(String token)
        {
            return tokens.containsKey(token);
        }

        public Serializable getUserFromChannelToken(String channelToken)
        {
            if (tokens != null)
            {
                WebsocketChannelMetadata metadata = tokens.get(channelToken);
                if (metadata != null)
                {
                    return metadata.getUser();
                }
            }
            return null;
        } 
    }
}
