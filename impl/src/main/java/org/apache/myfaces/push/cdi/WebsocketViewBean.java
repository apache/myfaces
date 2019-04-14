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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import org.apache.myfaces.cdi.util.CDIUtils;

/**
 * The purpose of this view scope bean is keep track of the channelTokens used in this view and if the view
 * is discarded, destroy the websocket sessions associated with the view because they are no longer valid.
 */
@ViewScoped
public class WebsocketViewBean implements Serializable
{
    
    /**
     * This map hold all tokens that are related to the current scope. 
     * This map use as key channel and as value channelTokens
     */
    private Map<String, List<WebsocketChannel>> channelTokenListMap = new HashMap<>(2);

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
    private Map<String, WebsocketChannelMetadata> tokenList = new HashMap<>(2);
    
    public void registerToken(String token, WebsocketChannelMetadata metadata)
    {
        tokenList.put(token, metadata);
    }
    
    public void registerWebsocketSession(String token, WebsocketChannelMetadata metadata)
    {
        if ("view".equals(metadata.getScope()))
        {
            channelTokenListMap.putIfAbsent(metadata.getChannel(), new ArrayList<>(1));
            channelTokenListMap.get(metadata.getChannel()).add(new WebsocketChannel(
                    token, metadata));
        }
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
    
    public String getChannelToken(WebsocketChannelMetadata metadata)
    {
        if (!metadata.isConnected())
        {
            // Always generate a connection
            return null;
        }
        String token = null;
        for (Map.Entry<String, WebsocketChannelMetadata> entry : tokenList.entrySet())
        {
            if (metadata.equals(entry.getValue()))
            {
                token = entry.getKey();
                break;
            }
        }
        return token;
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
        WebsocketSessionBean sessionHandler = CDIUtils.lookup(CDI.current().getBeanManager(), 
                WebsocketSessionBean.class);
        if (sessionHandler != null)
        {
            for (String token : tokenList.keySet())
            {
                sessionHandler.destroyChannelToken(token);
            }
        }
        
        for (String token : tokenList.keySet())
        {
            WebsocketApplicationSessionHolder.removeSession(token);
        }
        channelTokenListMap.clear();
        tokenList.clear();
    }
}