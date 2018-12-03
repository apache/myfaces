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
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.util.ConcurrentLRUCache;
import org.apache.myfaces.util.WebConfigParamUtils;

public class CheckedViewIdsCache
{
    private static final Logger LOG = Logger.getLogger(CheckedViewIdsCache.class.getName());
    private static final String INSTANCE_KEY = CheckedViewIdsCache.class.getName();
    
    /**
     * Controls the size of the cache used to "remember" if a view exists or not.
     */
    @JSFWebConfigParam(defaultValue = "500", since = "2.0.2", group="viewhandler", tags="performance", 
            classType="java.lang.Integer",
            desc="Controls the size of the cache used to 'remember' if a view exists or not.")
    private static final String CHECKED_VIEWID_CACHE_SIZE_ATTRIBUTE = "org.apache.myfaces.CHECKED_VIEWID_CACHE_SIZE";

    /**
     * Enable or disable a cache used to "remember" if a view exists or not and reduce the impact of
     * sucesive calls to ExternalContext.getResource().
     */
    @JSFWebConfigParam(defaultValue = "true", since = "2.0.2", expectedValues="true, false", group="viewhandler", 
            tags="performance",
            desc="Enable or disable a cache used to 'remember' if a view exists or not and reduce the impact " +
                 "of sucesive calls to ExternalContext.getResource().")
    private static final String CHECKED_VIEWID_CACHE_ENABLED_ATTRIBUTE = 
        "org.apache.myfaces.CHECKED_VIEWID_CACHE_ENABLED";

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
            enabled = WebConfigParamUtils.getBooleanInitParameter(facesContext.getExternalContext(),
                    CHECKED_VIEWID_CACHE_ENABLED_ATTRIBUTE,
                    true);
        }
        
        size = WebConfigParamUtils.getIntegerInitParameter(facesContext.getExternalContext(),
                CHECKED_VIEWID_CACHE_SIZE_ATTRIBUTE,
                500);

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
    
    public ConcurrentLRUCache<String, Boolean> getCache()
    {
        return cache;
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
