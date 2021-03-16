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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 *
 */
public class PushContextCDIExtension implements Extension
{
    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        // Register PushContextFactoryBean as a bean with CDI annotations, so the system
        // can take it into account, and use it later when necessary.
        AnnotatedType wcbean = beanManager.createAnnotatedType(WebsocketChannelTokenBuilderBean.class);
        event.addAnnotatedType(wcbean, wcbean.getJavaClass().getName());
        
        AnnotatedType sessionhandlerbean = beanManager.createAnnotatedType(WebsocketSessionBean.class);
        event.addAnnotatedType(sessionhandlerbean, sessionhandlerbean.getJavaClass().getName());

        AnnotatedType viewTokenBean = beanManager.createAnnotatedType(WebsocketViewBean.class);
        event.addAnnotatedType(viewTokenBean, viewTokenBean.getJavaClass().getName());

        AnnotatedType apphandlerbean = beanManager.createAnnotatedType(WebsocketApplicationBean.class);
        event.addAnnotatedType(apphandlerbean, apphandlerbean.getJavaClass().getName());
    }
}
