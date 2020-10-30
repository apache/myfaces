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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;

@ApplicationScoped
public class PushContextFactoryBean
{

    @Produces
    @Push
    public PushContext createPushContext(InjectionPoint ip)
    {
        Push push = ip.getAnnotated().getAnnotation(Push.class);
        String channel = push.channel().isEmpty() ? ip.getMember().getName() : push.channel();
        return new PushContextImpl(channel);
    }
    
    public void close(@Disposes @Push PushContext context) 
    {
    }
}
