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
import java.util.Objects;

public class WebsocketChannelMetadata implements Serializable
{
    private String channel;
    private String scope;
    private Serializable user;
    private boolean connected;

    public WebsocketChannelMetadata()
    {
    }
    
    public WebsocketChannelMetadata(String channel, String scope, Serializable user, boolean connected)
    {
        this.channel = channel;
        this.scope = scope;
        this.user = user;
        this.connected = connected;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public Serializable getUser()
    {
        return user;
    }

    public void setUser(Serializable user)
    {
        this.user = user;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.channel);
        hash = 67 * hash + Objects.hashCode(this.scope);
        hash = 67 * hash + Objects.hashCode(this.user);
        hash = 67 * hash + (this.connected ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final WebsocketChannelMetadata other = (WebsocketChannelMetadata) obj;
        if (!Objects.equals(this.channel, other.channel))
        {
            return false;
        }
        if (!Objects.equals(this.scope, other.scope))
        {
            return false;
        }
        if (!Objects.equals(this.user, other.user))
        {
            return false;
        }
        if (this.connected != other.connected)
        {
            return false;
        }
        return true;
    }

}
