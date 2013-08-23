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
package org.apache.myfaces.flow.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.faces.flow.FlowScoped;

/**
 *
 * @author Leonardo Uribe
 */
public class FlowScopeCDIExtension implements Extension
{
    private FlowScopedContextImpl flowScopedContext;
    
    void beforeBeanDiscovery(
        @Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        event.addScope(FlowScoped.class, true, true);
        // Register FlowBuilderFactoryBean as a bean with CDI annotations, so the system
        // can take it into account, and use it later when necessary.
        AnnotatedType bean = beanManager.createAnnotatedType(FlowScopeBeanHolder.class);
        event.addAnnotatedType(bean);
    }
    
    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager)
    {
        flowScopedContext = new FlowScopedContextImpl(manager);
        event.addContext(flowScopedContext);
    }
    
    void initializeFlowContexts(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
    {
        //FlowScopeBeanHolder flowScopeBeanHolder = BeanProvider.getContextualReference(
        //    beanManager, FlowScopeBeanHolder.class, false);
        
        //flowScopedContext.initFlowContext(flowScopeBeanHolder);
    }
}
