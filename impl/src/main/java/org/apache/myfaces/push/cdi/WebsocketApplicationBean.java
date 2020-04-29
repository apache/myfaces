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
import jakarta.enterprise.context.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
public class WebsocketApplicationBean
{
    
    /**
     * This map has as key the channel and as values a list of websocket channels
     */
    private Map<String, List<WebsocketChannel> > channelTokenListMap = 
        new HashMap<String, List<WebsocketChannel> >(2);

    public void registerWebsocketSession(String token, WebsocketChannelMetadata metadata)
    {
        if ("application".equals(metadata.getScope()))
        {
            channelTokenListMap.putIfAbsent(metadata.getChannel(), new ArrayList<WebsocketChannel>(1));
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
            List<String> value = new ArrayList<String>(list.size());
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
            List<String> value = new ArrayList<String>(list.size());
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
