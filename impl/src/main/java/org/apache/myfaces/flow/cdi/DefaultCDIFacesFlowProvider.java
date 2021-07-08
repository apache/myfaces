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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.builder.FlowDefinition;
import java.util.Set;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.flow.FlowUtils;
import org.apache.myfaces.spi.FacesFlowProvider;

/**
 *
 * @author Leonardo Uribe
 */
public class DefaultCDIFacesFlowProvider extends FacesFlowProvider
{
    private final static String CURRENT_FLOW_SCOPE_MAP = "oam.flow.SCOPE_MAP";
    private final static char SEPARATOR_CHAR = '.';

    private List<Flow> flows;

    @Override
    public Iterator<Flow> getAnnotatedFlows(FacesContext facesContext)
    {
        if (flows == null)
        {
            flows = new ArrayList<>();

            BeanManager beanManager = CDIUtils.getBeanManager(facesContext);
            if (beanManager == null)
            {
                Logger.getLogger(DefaultCDIFacesFlowProvider.class.getName()).log(Level.INFO,
                        "CDI BeanManager not found");
                return null;
            }

            Set<Bean<?>> beans = beanManager.getBeans(Flow.class, new AnnotationLiteral<FlowDefinition>(){ });
            for (Bean bean : beans)
            {
                // TODO we should actually remember the CC and destroy on shutdown
                CreationalContext<Flow> cc = beanManager.createCreationalContext(bean);
                flows.add((Flow) beanManager.getReference(bean, Flow.class, cc));
            }
        }

        return flows.iterator();
    }
    
    @Override
    public void doAfterEnterFlow(FacesContext context, Flow flow)
    {
        FlowScopeContextualStorageHolder storageHolder = FlowScopeContextualStorageHolder.getInstance(context, true);
        storageHolder.createCurrentFlowScope(context);

        String mapKey = getFlowKey(flow);
        context.getAttributes().remove(mapKey);
    }
    
    @Override
    public void doBeforeExitFlow(FacesContext context, Flow flow)
    {
        FlowScopeContextualStorageHolder storageHolder = FlowScopeContextualStorageHolder.getInstance(context, true);
        storageHolder.destroyCurrentFlowScope(context);

        String mapKey = getFlowKey(flow);
        context.getAttributes().remove(mapKey);
    }
    
    @Override
    public Map<Object, Object> getCurrentFlowScope(FacesContext facesContext)
    {
        Flow flow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
        if (flow != null)
        {
            String mapKey = getFlowKey(flow);
            return (Map<Object, Object>) facesContext.getAttributes().computeIfAbsent(mapKey,
                    k -> new FlowScopeMap(
                            CDIUtils.getBeanManager(facesContext),
                            FlowUtils.getFlowMapKey(facesContext, flow)));
        }

        return null;
    }

    @Override
    public void refreshClientWindow(FacesContext facesContext)
    {
        if (!facesContext.getApplication().getStateManager().isSavingStateInClient(facesContext))
        {
            Flow flow = facesContext.getApplication().getFlowHandler().getCurrentFlow(facesContext);
            if (flow != null)
            {
                FlowScopeContextualStorageHolder storageHolder = FlowScopeContextualStorageHolder
                        .getInstance(facesContext, true);
                storageHolder.refreshClientWindow(facesContext);
            }
        }
    }

    protected String getFlowKey(Flow flow)
    {
        return CURRENT_FLOW_SCOPE_MAP + SEPARATOR_CHAR
                + flow.getDefiningDocumentId() + SEPARATOR_CHAR
                + flow.getId();
    }

}
