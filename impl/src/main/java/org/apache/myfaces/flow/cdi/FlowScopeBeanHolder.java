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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;
import org.apache.myfaces.cdi.JsfApplicationArtifactHolder;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.context.servlet.StartupServletExternalContextImpl;
import org.apache.myfaces.flow.FlowUtils;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.context.ExceptionHandlerImpl;


/**
 *
 * This holder will store the flow scope active ids and it's beans for the current
 * HTTP Session. We use standard SessionScoped bean to not need
 * to treat async-supported and similar headache.
 * 
 * @author lu4242
 */
@SessionScoped
public class FlowScopeBeanHolder implements Serializable
{
    /**
     * key: client window id + flow id
     * value: the {@link ContextualStorage} which holds all the
     * {@link jakarta.enterprise.inject.spi.Bean}s.
     */
    private Map<String, ContextualStorage> storageMap;
    
    private Map<String, List<String>> activeFlowMapKeys;
    
    private FacesFlowClientWindowCollection windowCollection;
    
    public static final String CURRENT_FLOW_SCOPE_MAP = "oam.CURRENT_FLOW_SCOPE_MAP";

    public static final String CREATED = FlowScopeBeanHolder.class.getName() + ".CREATED";
    
    @Inject
    JsfApplicationArtifactHolder applicationContextBean;

    public FlowScopeBeanHolder()
    {
    }
    
    @PostConstruct
    public void init()
    {
        storageMap = new ConcurrentHashMap<>();
        activeFlowMapKeys = new ConcurrentHashMap<>();
        windowCollection = null;
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.refreshClientWindow(facesContext);
        facesContext.getExternalContext().getSessionMap().put(CREATED, true);
        
        Object context = facesContext.getExternalContext().getContext();
        if (context instanceof ServletContext)
        {
            BeanManager beanManager = CDIUtils.getBeanManager(facesContext.getExternalContext());
            JsfApplicationArtifactHolder appBean = CDIUtils.get(beanManager, JsfApplicationArtifactHolder.class);
            if (appBean.getServletContext() != null)
            {
                appBean.setServletContext((ServletContext) context);
            }
        }
    }

    /**
     * This method will return the ContextualStorage or create a new one
     * if no one is yet assigned to the current flowClientWindowId.
     * 
     * @param beanManager we need the CDI {@link BeanManager} for serialisation.
     * @param flowClientWindowId the flowClientWindowId for the current flow.
     * @param create create if not existent
     *
     * @return the ContextualStorage or null
     */
    public ContextualStorage getContextualStorage(BeanManager beanManager, String flowClientWindowId, boolean create)
    {
        ContextualStorage storage = storageMap.get(flowClientWindowId);
        if (storage == null && create)
        {
            storage = new ContextualStorage(beanManager, true);
            storageMap.put(flowClientWindowId, storage);
        }
        return storage;
    }

    public Map<String, ContextualStorage> getStorageMap()
    {
        return storageMap;
    }
    
    public Map<Object, Object> getFlowScopeMap(BeanManager beanManager, String flowClientWindowId, boolean create)
    {
        ContextualStorage contextualStorage = getContextualStorage(beanManager, flowClientWindowId, create);
        if (contextualStorage == null)
        {
            return null;
        }

        ContextualInstanceInfo info = contextualStorage.getStorage().get(CURRENT_FLOW_SCOPE_MAP);
        if (info == null && create)
        {
            info = new ContextualInstanceInfo<>();
            contextualStorage.getStorage().put(CURRENT_FLOW_SCOPE_MAP, info);
        }
        if (info == null)
        {
            return null;
        }

        Map<Object, Object> map = (Map<Object, Object>) info.getContextualInstance();
        if (map == null && create)
        {
            map = new HashMap<>();
            info.setContextualInstance(map);
        }
        return map;
    }

    /**
     *
     * This method will replace the storageMap and with
     * a new empty one.
     * This method can be used to properly destroy the BeanHolder beans
     * without having to sync heavily. Any
     * {@link jakarta.enterprise.inject.spi.Bean#destroy(Object, jakarta.enterprise.context.spi.CreationalContext)}
     * should be performed on the returned old storage map.
     * @return the old storageMap.
     */
    public Map<String, ContextualStorage> forceNewStorage()
    {
        Map<String, ContextualStorage> oldStorageMap = storageMap;
        storageMap = new ConcurrentHashMap<>();
        return oldStorageMap;
    }

    /**
     * This method properly destroys all current &#064;WindowScoped beans
     * of the active session and also prepares the storage for new beans.
     * It will automatically get called when the session context closes
     * but can also get invoked manually, e.g. if a user likes to get rid
     * of all it's &#064;WindowScoped beans.
     */
    //@PreDestroy
    public void destroyBeans()
    {
        // we replace the old BeanHolder beans with a new storage Map
        // an afterwards destroy the old Beans without having to care about any syncs.
        Map<String, ContextualStorage> oldContextStorages = forceNewStorage();

        for (ContextualStorage contextualStorage : oldContextStorages.values())
        {
            FlowScopeContext.destroyAllActive(contextualStorage);
        }
    }
    
    /**
     * See description on ViewScopeBeanHolder for details about how this works
     */
    @PreDestroy
    public void destroyBeansOnPreDestroy()
    {
        Map<String, ContextualStorage> oldContextStorages = forceNewStorage();
        if (!oldContextStorages.isEmpty())
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ServletContext servletContext = null;
            if (facesContext == null)
            {
                try
                {
                    servletContext = applicationContextBean.getServletContext();
                }
                catch (Throwable e)
                {
                    Logger.getLogger(FlowScopeBeanHolder.class.getName()).log(Level.WARNING,
                        "Cannot locate servletContext to create FacesContext on @PreDestroy flow scope beans. "
                                + "The beans will be destroyed without active FacesContext instance.");
                    servletContext = null;
                }
            }
            if (facesContext == null && servletContext != null)
            {
                try
                {
                    ExternalContext externalContext = new StartupServletExternalContextImpl(servletContext, false);
                    ExceptionHandler exceptionHandler = new ExceptionHandlerImpl();
                    facesContext = new StartupFacesContextImpl(externalContext, externalContext, exceptionHandler,
                            false);
                    for (ContextualStorage contextualStorage : oldContextStorages.values())
                    {
                        FlowScopeContext.destroyAllActive(contextualStorage);
                    }
                }
                finally
                {
                    facesContext.release();
                }
            }
            else
            {
                for (ContextualStorage contextualStorage : oldContextStorages.values())
                {
                    FlowScopeContext.destroyAllActive(contextualStorage);
                }
            }
        }
    }
    
    public void refreshClientWindow(FacesContext facesContext)
    {
        if (windowCollection == null)
        {
            Integer numberOfFacesFlowClientWindowIdsInSession =
                    MyfacesConfig.getCurrentInstance(facesContext).getNumberOfFacesFlowClientWindowIdsInSession();
            windowCollection = new FacesFlowClientWindowCollection(numberOfFacesFlowClientWindowIdsInSession);
        }
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        if (cw != null && cw.getId() != null)
        {
            windowCollection.setFlowScopeBeanHolder(this);
            windowCollection.put(cw.getId(), "");
        }
    }
    
    public void clearFlowMap(String clientWindowId)
    {
        List<String> activeFlowKeys = activeFlowMapKeys.remove(clientWindowId);
        if (activeFlowKeys != null && !activeFlowKeys.isEmpty())
        {
            for (String flowMapKey : activeFlowKeys)
            {
                ContextualStorage contextualStorage = storageMap.remove(flowMapKey);
                if (contextualStorage != null)
                {
                    FlowScopeContext.destroyAllActive(contextualStorage);
                }
            }
        }
    }
    
    public List<String> getActiveFlowMapKeys(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = cw.getId();
        List<String> activeFlowKeys = activeFlowMapKeys.get(baseKey);
        if (activeFlowKeys == null)
        {
            return Collections.emptyList();
        }

        return activeFlowKeys;
    }
    
    public void createCurrentFlowScope(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = cw.getId();
        
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        String flowMapKey = FlowUtils.getFlowMapKey(facesContext, flow);

        List<String> activeFlowKeys = activeFlowMapKeys.computeIfAbsent(baseKey, k -> new ArrayList<>());
        activeFlowKeys.add(0, flowMapKey);
        activeFlowMapKeys.put(baseKey, activeFlowKeys);
        refreshClientWindow(facesContext);
    }
    
    public void destroyCurrentFlowScope(FacesContext facesContext)
    {
        ClientWindow cw = facesContext.getExternalContext().getClientWindow();
        String baseKey = cw.getId();
        
        FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
        Flow flow = flowHandler.getCurrentFlow(facesContext);
        String flowMapKey = FlowUtils.getFlowMapKey(facesContext, flow);

        ContextualStorage contextualStorage = storageMap.remove(flowMapKey);
        if (contextualStorage != null)
        {
            FlowScopeContext.destroyAllActive(contextualStorage);
        }
        
        List<String> activeFlowKeys = activeFlowMapKeys.get(baseKey);
        if (activeFlowKeys != null && !activeFlowKeys.isEmpty())
        {
            activeFlowKeys.remove(flowMapKey);
        }
    }
}
