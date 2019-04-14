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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.myfaces.push.cdi.WebsocketApplicationSessionHolder;

/**
 * This class ensures the Session instance is properly registered into WebsocketApplicationSessionHolder in case
 * of serialization/deserialization roundtrip.
 * 
 */
public class WebsocketSessionClusterSerializedRestore implements Externalizable
{
    public final static String WEBSOCKET_SESSION_SERIALIZED_RESTORE = "oam.websocket.SR";
    
    private String channelToken;
    
    private boolean deserialized = false;

    public WebsocketSessionClusterSerializedRestore()
    {
    }

    public WebsocketSessionClusterSerializedRestore(String channelToken)
    {
        this.channelToken = channelToken;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(this.channelToken);
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.channelToken = in.readUTF();
        this.deserialized = true;
        WebsocketApplicationSessionHolder.getRestoredQueue().add(this.channelToken);
    }

    public String getChannelToken()
    {
        return channelToken;
    }

    public void setChannelToken(String channelToken)
    {
        this.channelToken = channelToken;
    }

    public boolean isDeserialized()
    {
        return deserialized;
    }
    
}