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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import org.apache.myfaces.cdi.util.BeanProvider;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.flow.FlowUtils;
import org.apache.myfaces.spi.FacesFlowProvider;

/**
 *
 * @author Leonardo Uribe
 */
public class DefaultCDIFacesFlowProvider extends FacesFlowProvider
{
    private BeanManager _beanManager;
    private boolean _initialized;
    
    private final static String CURRENT_FLOW_SCOPE_MAP = "oam.flow.SCOPE_MAP";
    
    static final char SEPARATOR_CHAR = '.';
    
    private boolean isFlowScopeBeanHolderCreated(FacesContext facesContext)
    {
        if (facesContext.getExternalContext().getSession(false) == null)
        {
            return false;
        }
        
        return facesContext.getExternalContext().getSessionMap()
                .containsKey(FlowScopeBeanHolder.FLOW_SCOPE_PREFIX_KEY);
    }
    
    @Override
    public void onSessionDestroyed()
    {
        // In CDI case, the best way to deal with this is use a method 
        // with @PreDestroy annotation on a session scope bean 
        // ( ViewScopeBeanHolder.destroyBeans() ). There is no need
        // to do anything else in this location, but it is advised
        // in CDI the beans are destroyed at the end of the request,
        // not when invalidateSession() is called.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && isFlowScopeBeanHolderCreated(facesContext))
        {
            FlowScopeBeanHolder flowScopeBeanHolder = BeanProvider.getContextualReference(
                _beanManager, FlowScopeBeanHolder.class, false);
            if (flowScopeBeanHolder != null)
            {
                flowScopeBeanHolder.destroyBeans();
            }
        }
    }
    
    @Override
    public Iterator<Flow> getAnnotatedFlows(FacesContext facesContext)
    {
        BeanManager beanManager = getBeanManager(facesContext);
        if (beanManager == null)
        {
            Logger.getLogger(DefaultCDIFacesFlowProvider.class.getName()).log(Level.INFO,
                "CDI BeanManager not found");
            return null;
        }

        FlowBuilderFactoryBean bean = CDIUtils.lookup(beanManager, FlowBuilderFactoryBean.class);
        List<Flow> flows = bean.getFlowDefinitions();
        return flows.iterator();
    }
    
    @Override
    public void doAfterEnterFlow(FacesContext context, Flow flow)
    {
        BeanManager beanManager = getBeanManager(context);
        if (beanManager != null)
        {
            FlowScopeBeanHolder beanHolder = CDIUtils.lookup(beanManager, FlowScopeBeanHolder.class);
            beanHolder.createCurrentFlowScope(context);
        }

        String mapKey = getFlowKey(flow);
        context.getAttributes().remove(mapKey);
    }
    
    @Override
    public void doBeforeExitFlow(FacesContext context, Flow flow)
    {
        BeanManager beanManager = getBeanManager(context);
        if (beanManager != null)
        {
            FlowScopeBeanHolder beanHolder = CDIUtils.lookup(beanManager, FlowScopeBeanHolder.class);
            beanHolder.destroyCurrentFlowScope(context);
        }

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
                    k -> new FlowScopeMap(getBeanManager(facesContext), FlowUtils.getFlowMapKey(facesContext, flow)));
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
                BeanManager beanManager = getBeanManager(facesContext);
                if (beanManager != null)
                {
                    FlowScopeBeanHolder beanHolder = CDIUtils.lookup(beanManager, FlowScopeBeanHolder.class);

                    //Refresh client window for flow scope
                    beanHolder.refreshClientWindow(facesContext);
                }
            }
        }
    }
    
    public BeanManager getBeanManager(FacesContext facesContext)
    {
        if (_beanManager == null && !_initialized)
        {
            _beanManager = CDIUtils.getBeanManager(facesContext.getExternalContext());
            _initialized = true;
        }
        return _beanManager;
    }

    protected String getFlowKey(Flow flow)
    {
        return CURRENT_FLOW_SCOPE_MAP + SEPARATOR_CHAR
                + flow.getDefiningDocumentId() + SEPARATOR_CHAR
                + flow.getId();
    }
}
