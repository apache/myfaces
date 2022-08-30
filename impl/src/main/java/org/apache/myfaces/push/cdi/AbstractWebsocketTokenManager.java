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
import java.util.List;

public abstract class AbstractWebsocketTokenManager implements Serializable
{
    
    public abstract void registerWebsocketSession(String token, WebsocketChannelMetadata metadata);

    /**
     * Indicate if the channel mentioned is valid for view scope.
     * 
     * A channel is valid if there is at least one token that represents a valid connection to this channel.
     * 
     * @param channel
     * @return 
     */
    public abstract boolean isChannelAvailable(String channel);

    public abstract List<String> getChannelTokens(String channel);

    public abstract <S extends Serializable> List<String> getChannelTokens(String channel, S user);
}
