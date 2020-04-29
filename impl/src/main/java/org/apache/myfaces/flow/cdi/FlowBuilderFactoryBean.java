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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.FlowBuilderParameter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.flow.builder.FlowBuilderImpl;

/**
 * This bean is used later by CDI to process flow definitions
 *
 * @author Leonardo Uribe
 */
@Named(FlowBuilderFactoryBean.FLOW_BUILDER_FACTORY_BEAN_NAME)
@ApplicationScoped
public class FlowBuilderFactoryBean
{
    public static final String FLOW_BUILDER_FACTORY_BEAN_NAME =
        "oam_FLOW_BUILDER_FACTORY_BEAN_NAME";

    private List<Flow> flowDefinitions = null;

    @Inject
    private FlowBuilderCDIExtension flowBuilderExtension;

    public FlowBuilderFactoryBean()
    {
    }

    @Produces
    @FlowBuilderParameter
    public FlowBuilder createFlowBuilderInstance()
    {
        return new FlowBuilderImpl();
    }

    /**
     * @return the flowDefinitions
     */
    public synchronized List<Flow> getFlowDefinitions()
    {
        if (flowDefinitions == null)
        {
            flowDefinitions = new ArrayList<Flow>();
            BeanManager beanManager = CDIUtils.getBeanManager(FacesContext.getCurrentInstance().getExternalContext());
            Iterator<Producer<Flow>> it = flowBuilderExtension.getFlowProducers().iterator();

            if (it != null)
            {
                while (it.hasNext())
                {
                    Flow flow = it.next().produce(beanManager.<Flow>createCreationalContext(null));
                    flowDefinitions.add(flow);
                }
            }
        }

        return flowDefinitions;
    }
}
