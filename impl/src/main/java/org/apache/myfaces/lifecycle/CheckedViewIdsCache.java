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
package org.apache.myfaces.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.util.ConcurrentLRUCache;

public class CheckedViewIdsCache
{
    private static final Logger LOG = Logger.getLogger(CheckedViewIdsCache.class.getName());
    private static final String INSTANCE_KEY = CheckedViewIdsCache.class.getName();

    private volatile ConcurrentLRUCache<String, Boolean> cache = null;
    private boolean enabled;
    private int size;
    
    private CheckedViewIdsCache()
    {
    }
    
    public void init(FacesContext facesContext)
    {
        // first, check if the ProjectStage is development and skip caching in this case
        if (facesContext.isProjectStage(ProjectStage.Development))
        {
            enabled = false;
        }
        else
        {
            // in all ohter cases, make sure that the cache is not explicitly disabled via context param
            enabled = MyfacesConfig.getCurrentInstance(facesContext).isCheckedViewIdCacheEnabled();
        }
        
        size = MyfacesConfig.getCurrentInstance(facesContext).getCheckedViewIdCacheSize();

        cache = new ConcurrentLRUCache<>((size * 4 + 3) / 3, size);
        
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, "MyFaces CheckedViewIdsCache enabled=" + enabled + ", size=" + size);
        }
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public Boolean get(String viewId)
    {
        return cache.get(viewId);
    }
    
    public Boolean put(String viewId, Boolean exists)
    {
        return cache.put(viewId, exists);
    }
    
    public static CheckedViewIdsCache getInstance(FacesContext facesContext)
    {
        CheckedViewIdsCache instance = (CheckedViewIdsCache)
                facesContext.getExternalContext().getApplicationMap().get(INSTANCE_KEY);
        if (instance == null)
        {
            instance = new CheckedViewIdsCache();
            instance.init(facesContext);
            facesContext.getExternalContext().getApplicationMap().put(INSTANCE_KEY, instance);
        }
        
        return instance;
    }
}
