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

package javax.faces.push;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 *
 */
public interface PushContext extends Serializable
{
    
    public static final String ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME = "javax.faces.ENABLE_WEBSOCKET_ENDPOINT";
    
    public static final String WEBSOCKET_ENDPOINT_PORT_PARAM_NAME = "javax.faces.WEBSOCKET_ENDPOINT_PORT";
    
    public static final String URI_PREFIX = "/javax.faces.push";
    
    public Set<Future<Void>> send(Object message);
            
    public <S extends Serializable> Set<Future<Void>> send(Object message, S user);  
    
    public <S extends Serializable> Map<S, Set<Future<Void>>> send(Object message, Collection<S> users);
}
