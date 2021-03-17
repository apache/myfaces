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
package org.apache.myfaces.cdi.clientwindow;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.Typed;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import java.io.Serializable;
import java.util.Map;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;
import org.apache.myfaces.cdi.util.AbstractContextualStorageHolder;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.util.lang.LRULinkedHashMap;

@Typed(ClientWindowScopeContextualStorageHolder.class)
@SessionScoped
public class ClientWindowScopeContextualStorageHolder
        extends AbstractContextualStorageHolder<ContextualStorage>
        implements Serializable
{
    private LRULinkedHashMap<String, String> clientWindowExpirationStack;

    @PostConstruct
    @Override
    public void init()
    {
        super.init();

        FacesContext facesContext = FacesContext.getCurrentInstance();

        Integer numberOfClientWindowsInSession =
                MyfacesConfig.getCurrentInstance(facesContext).getNumberOfClientWindowsInSession();
        clientWindowExpirationStack = new LRULinkedHashMap<>(numberOfClientWindowsInSession, (eldest) ->
        {
            destroyAll(FacesContext.getCurrentInstance(), eldest.getKey());
        });

        pushClientWindow(facesContext, facesContext.getExternalContext().getClientWindow());
    }
    
    @Override
    public void destroyAll(ContextualStorage contextualStorage, FacesContext facesContext)
    {
        if (contextualStorage == null)
        {
            return;
        }
        
        Map<Object, ContextualInstanceInfo<?>> contextMap = contextualStorage.getStorage();
        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet())
        {
            Contextual bean = contextualStorage.getBean(entry.getKey());

            ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
            bean.destroy(contextualInstanceInfo.getContextualInstance(), 
                contextualInstanceInfo.getCreationalContext());
        }
    }

    @Override
    protected ContextualStorage newContextualStorage(String slotId)
    {
        return new ContextualStorage(beanManager, true);
    }

    public void pushClientWindow(FacesContext facesContext, ClientWindow clientWindow)
    {
        if (clientWindow != null && clientWindow.getId() != null)
        {
            clientWindowExpirationStack.remove(clientWindow.getId());
            clientWindowExpirationStack.put(clientWindow.getId(), "");
        }
    }

    public static ClientWindowScopeContextualStorageHolder getInstance(FacesContext facesContext)
    {
        return getInstance(facesContext, false);
    }
    
    public static ClientWindowScopeContextualStorageHolder getInstance(FacesContext facesContext, boolean create)
    {
        return getInstance(facesContext, ClientWindowScopeContextualStorageHolder.class, create);
    }
}
