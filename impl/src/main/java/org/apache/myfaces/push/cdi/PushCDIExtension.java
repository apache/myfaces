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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

public class PushCDIExtension implements Extension
{
    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        // Register PushContextFactoryBean as a bean with CDI annotations, so the system
        // can take it into account, and use it later when necessary.
        AnnotatedType tokenBuilder = beanManager.createAnnotatedType(WebsocketChannelTokenBuilder.class);
        event.addAnnotatedType(tokenBuilder, tokenBuilder.getJavaClass().getName());

        AnnotatedType pushContextFactory = beanManager.createAnnotatedType(PushContextFactory.class);
        event.addAnnotatedType(pushContextFactory, pushContextFactory.getJavaClass().getName());
     
        AnnotatedType sessionManager = beanManager.createAnnotatedType(WebsocketSessionManager.class);
        event.addAnnotatedType(sessionManager, sessionManager.getJavaClass().getName());
        
        AnnotatedType scopeManager = beanManager.createAnnotatedType(WebsocketScopeManager.class);
        event.addAnnotatedType(scopeManager, scopeManager.getJavaClass().getName());
        
        AnnotatedType sessionScope = beanManager.createAnnotatedType(WebsocketScopeManager.SessionScope.class);
        event.addAnnotatedType(sessionScope, sessionScope.getJavaClass().getName());

        AnnotatedType viewScope = beanManager.createAnnotatedType(WebsocketScopeManager.ViewScope.class);
        event.addAnnotatedType(viewScope, viewScope.getJavaClass().getName());

        AnnotatedType applicationScope = beanManager.createAnnotatedType(WebsocketScopeManager.ApplicationScope.class);
        event.addAnnotatedType(applicationScope, applicationScope.getJavaClass().getName());
    }
}
