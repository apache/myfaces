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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;

/**
 * The purpose of this bean is to keep track of the active tokens and Session instances in the current session,
 * so it can be possible to decide if the token is valid or not for the current session. If the token is not in
 * application scope and is present in session, it means there was a server restart, so the connection must be
 * updated (added to application scope).
 * 
 */
@SessionScoped
public class WebsocketSessionBean implements Serializable
{
    
    /**
     * This map hold all tokens that are related to the current scope. 
     * This map use as key channel and as value channelTokens
     */
    private Map<String, List<WebsocketChannel> > channelTokenListMap = new ConcurrentHashMap<>(2);    
    
    /**
     * This map holds all tokens related to the current session and its associated metadata, that will
     * be used in the websocket handshake to validate if the incoming request is valid and to store
     * the user object into the Session object.
     */
    private Map<String, WebsocketChannelMetadata> tokenMap = new ConcurrentHashMap<>();
    
    public WebsocketSessionBean()
    {
    }
    
    public void registerToken(String token, WebsocketChannelMetadata metadata)
    {
        tokenMap.put(token, metadata);
    }

    public void registerWebsocketSession(String token, WebsocketChannelMetadata metadata)
    {
        if ("session".equals(metadata.getScope()))
        {
            channelTokenListMap.putIfAbsent(metadata.getChannel(), new ArrayList<>(1));
            channelTokenListMap.get(metadata.getChannel()).add(new WebsocketChannel(token, metadata));
        }
    }
    
    public boolean isTokenValid(String token)
    {
        return tokenMap.containsKey(token);
    }
    
    public Serializable getUserFromChannelToken(String channelToken)
    {
        if (tokenMap != null)
        {
            WebsocketChannelMetadata metadata = tokenMap.get(channelToken);
            if (metadata != null)
            {
                return metadata.getUser();
            }
        }
        return null;
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
        return channelTokenListMap.containsKey(channel);
    }
    
    public List<String> getChannelTokensFor(String channel)
    {
        List<WebsocketChannel> list = channelTokenListMap.get(channel);
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
    
    public <S extends Serializable> List<String> getChannelTokensFor(String channel, S user)
    {
        List<WebsocketChannel> list = channelTokenListMap.get(channel);
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

    @PreDestroy
    public void destroy()
    {
        // Since there is an algorithm in place for @PreDestroy and @ViewScoped beans using a session
        // scope bean and @PreDestroy, there is nothing else to do here. But on session expiration
        // it is easier to just clear the map. At the end it will not cause any side effects.
        channelTokenListMap.clear();
        tokenMap.clear();
    }
    
    public void destroyChannelToken(String channelToken)
    {
        String channel = null;
        for (Map.Entry<String, List<WebsocketChannel>> entry : channelTokenListMap.entrySet())
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
            channelTokenListMap.remove(channel);
        }
        tokenMap.remove(channelToken);
    }
}
