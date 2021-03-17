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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.faces.flow.FlowScoped;
import org.apache.myfaces.flow.FlowReference;

/**
 *
 * @author Leonardo Uribe
 */
public class FlowScopeExtension implements Extension
{
    private FlowScopeContext flowScopedContext;
    
    private Map<Class, FlowReference> flowBeanReferences;
    
    public FlowScopeExtension()
    {
        flowBeanReferences = new ConcurrentHashMap<>();
    }
    
    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager)
    {
        event.addScope(FlowScoped.class, true, true);

        AnnotatedType<FlowScopeContextualStorageHolder> annotatedType = 
                beanManager.createAnnotatedType(FlowScopeContextualStorageHolder.class);
        event.addAnnotatedType(annotatedType, annotatedType.getJavaClass().getName());
    }

    void onProcessBean(@Observes ProcessBean event, BeanManager manager)
    {
        // Register all beans who are annotated with FlowScoped and has a flow reference
        // restriction, to take it into account later when it is created and store it
        // in the right context so @PreDestroy is called when the referenced flow is.
        if (event.getAnnotated().isAnnotationPresent(FlowScoped.class))
        {
            FlowScoped flowScoped = event.getAnnotated().getAnnotation(FlowScoped.class);
            String flowId = flowScoped.value();
            if (flowId != null)
            {
                flowBeanReferences.put(event.getBean().getBeanClass(), new FlowReference(
                    flowScoped.definingDocumentId(), flowId));
            }
        }
    }
    
    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager)
    {
        flowScopedContext = new FlowScopeContext(manager, flowBeanReferences);
        event.addContext(flowScopedContext);
    }
}
