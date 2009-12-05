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
package org.apache.myfaces.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyCustomScopeEvent;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.ScopeContext;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;

/**
 * Destroyes managed beans with the current LifecycleProvider.
 * This guarantees the invocation of the @PreDestroy methods.
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class ManagedBeanDestroyer implements SystemEventListener
{
    
    private static Logger log = Logger.getLogger(ManagedBeanDestroyer.class.getName());
    
    private RuntimeConfig runtimeConfig;
    
    public void setRuntimeConfig(RuntimeConfig runtimeConfig)
    {
        this.runtimeConfig = runtimeConfig;
    }
    
    public RuntimeConfig getRuntimeConfig()
    {
        if (runtimeConfig == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null)
            {
                ExternalContext externalContext = facesContext.getExternalContext();
                runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
            }
        }
        return runtimeConfig;
    }
    
    public boolean isListenerForSource(Object source)
    {
        // source of PreDestroyCustomScopeEvent is ScopeContext
        // and source of PreDestroyViewMapEvent is UIViewRoot
        return (source instanceof ScopeContext) || (source instanceof UIViewRoot);
    }


    /**
     * Listens to PreDestroyCustomScopeEvent and PreDestroyViewMapEvent
     * and invokes destroy() for every managed bean in the associated scope.
     */
    public void processEvent(SystemEvent event)
    {
        Map<String, Object> scope = null;
        
        if (event instanceof PreDestroyViewMapEvent)
        {
            UIViewRoot viewRoot = (UIViewRoot) ((PreDestroyViewMapEvent) event).getComponent();
            scope = viewRoot.getViewMap(false);
            if (scope == null)
            {
                // view map does not exist --> nothing to destroy
                return;
            }
        }
        else if (event instanceof PreDestroyCustomScopeEvent)
        {
            ScopeContext scopeContext = ((PreDestroyCustomScopeEvent) event).getContext();
            scope = scopeContext.getScope();
        }
        else
        {
            // wrong event
            return;
        }
        
        LifecycleProvider provider = getCurrentLifecycleProvider();
        for (String key : scope.keySet())
        {
            Object value = scope.get(key);
            this.destroy(key, value, provider);
        }
    }
    
    /**
     * Checks if the given managed bean exists in the RuntimeConfig.
     * @param name
     * @return
     */
    public boolean isManagedBean(String name)
    {
        RuntimeConfig config = getRuntimeConfig();
        if (config != null)
        {
            return config.getManagedBean(name) != null;
        }
        // we have no RuntimeConfig, thus theres no FacesContext --> no managed bean
        return false;
    }
    
    /**
     * Destroys the given managed bean with the current LifecycleProvider.
     * @param name
     * @param instance
     */
    public void destroy(String name, Object instance)
    {
        destroy(name, instance, getCurrentLifecycleProvider());
    }
    
    /**
     * Destroys the given managed bean with the given LifecycleProvider
     * @param name
     * @param instance
     * @param provider
     */
    public void destroy(String name, Object instance, LifecycleProvider provider)
    {
        if (isManagedBean(name)) {
            if (provider == null)
            {
                provider = getCurrentLifecycleProvider();
            }
            try
            {
                provider.destroyInstance(instance);
            } 
            catch (IllegalAccessException e)
            {
                log.log(Level.SEVERE, "Could not access @PreDestroy method of managed bean " + name, e);
            } 
            catch (InvocationTargetException e)
            {
                log.log(Level.SEVERE, "An Exception occured while invoking " +
                        "@PreDestroy method of managed bean " + name, e);
            }
        }
    }
    
    /**
     * Retrieves the current LifecycleProvider from the LifecycleProviderFactory
     * @return
     */
    public LifecycleProvider getCurrentLifecycleProvider()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            return LifecycleProviderFactory.getLifecycleProviderFactory().getLifecycleProvider(null);
        }
        ExternalContext externalContext = facesContext.getExternalContext();
        return LifecycleProviderFactory.getLifecycleProviderFactory().getLifecycleProvider(externalContext);
    }
    
}
