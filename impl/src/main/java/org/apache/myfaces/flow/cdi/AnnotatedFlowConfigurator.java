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

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.flow.FlowImpl;

/**
 *
 * @author Leonardo Uribe
 */
public class AnnotatedFlowConfigurator
{
    
    public static void configureAnnotatedFlows(FacesContext facesContext)
    {
        BeanManager beanManager = FacesFlowCDIUtils.getBeanManagerFromJNDI();
        if (beanManager != null)
        {
            FlowBuilderFactoryBean bean = FacesFlowCDIUtils.lookup(
                beanManager, FlowBuilderFactoryBean.class);

            Instance<Flow> instance = bean.getFlowDefinitions();
            
            Iterator<Flow> it = instance.iterator();
            if (it.hasNext())
            {
                FacesConfigurator.enableDefaultWindowMode(facesContext);
            }
            while (it.hasNext())
            {
                Flow flow = it.next();

                if (flow instanceof FlowImpl)
                {
                    ((FlowImpl)flow).freeze();
                }
                
                facesContext.getApplication().getFlowHandler().addFlow(facesContext, flow);
            }
        }
        else
        {
            Logger.getLogger(AnnotatedFlowConfigurator.class.getName()).log(Level.INFO,
                "CDI BeanManager not found");
        }
    }
}
