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

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;
import org.apache.myfaces.cdi.util.ContextualInstanceInfo;
import org.apache.myfaces.cdi.util.ContextualStorage;
import org.apache.myfaces.cdi.util.AbstractContextualStorageHolder;

@SessionScoped
public class ClientWindowScopeContextualStorageHolder
        extends AbstractContextualStorageHolder<ContextualStorage>
        implements Serializable
{
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
    
    protected static ClientWindowScopeContextualStorageHolder getInstance(FacesContext facesContext)
    {
        return getInstance(facesContext, false);
    }
    
    protected static ClientWindowScopeContextualStorageHolder getInstance(FacesContext facesContext, boolean create)
    {
        return getInstance(facesContext, ClientWindowScopeContextualStorageHolder.class, create);
    }
}
