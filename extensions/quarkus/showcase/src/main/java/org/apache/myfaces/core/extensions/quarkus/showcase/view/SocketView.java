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
package org.apache.myfaces.core.extensions.quarkus.showcase.view;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ViewScoped
@Named
public class SocketView implements Serializable {
    
    private static final Logger LOG = Logger.getLogger(SocketView.class.getName());
    
    @Inject
    @Push
    PushContext helloChannel;
    
    String message;
    
    public void sendMessage() {
        LOG.log(Level.INFO, "send push message");
        this.sendPushMessage("hello");
    }
    
    private void sendPushMessage(Object message) {
        helloChannel.send("" + message + " at " + LocalDateTime.now());
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void sendMessage2() {
        LOG.log(Level.INFO, "send push message from input box::" + this.message);
        this.sendPushMessage(this.message);
    }
    
}
