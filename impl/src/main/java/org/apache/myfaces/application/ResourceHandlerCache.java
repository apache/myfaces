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
package org.apache.myfaces.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;

public class ResourceHandlerCache
{
    private static final Logger log = Logger
            .getLogger(ResourceHandlerCache.class.getName());

    private Boolean _resourceCacheEnabled = null;
    private Map<ResourceKey, Resource> _resourceCacheMap = null;

    @JSFWebConfigParam(defaultValue = "500", since = "2.0.2")
    private static final String RESOURCE_HANDLER_CACHE_SIZE_ATTRIBUTE = "org.apache.myfaces.RESOURCE_HANDLER_CACHE_SIZE";
    private static final int RESOURCE_HANDLER_CACHE_DEFAULT_SIZE = 500;

    @JSFWebConfigParam(defaultValue = "true", since = "2.0.2")
    private static final String RESOURCE_HANDLER_CACHE_ENABLED_ATTRIBUTE = "org.apache.myfaces.RESOURCE_HANDLER_CACHE_ENABLED";
    private static final boolean RESOURCE_HANDLER_CACHE_ENABLED_DEFAULT = true;

    public Resource getResource(String resourceName, String libraryName,
            String contentType)
    {
        if (!isResourceCachingEnabled() || _resourceCacheMap == null)
            return null;

        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Attemping to get resource from cache for "
                    + resourceName);

        ResourceKey key = new ResourceKey(resourceName, libraryName,
                contentType);

        return _resourceCacheMap.get(key);
    }
    
    public boolean containsResource(String resourceName, String libraryName,
            String contentType)
    {
        if (!isResourceCachingEnabled() || _resourceCacheMap == null)
            return false;
        ResourceKey key = new ResourceKey(resourceName, libraryName,
                contentType);
        return _resourceCacheMap.containsKey(key);
    }

    public void putResource(String resourceName, String libraryName,
            String contentType, Resource resource)
    {
        if (!isResourceCachingEnabled())
            return;

        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Attemping to put resource to cache for "
                    + resourceName);

        if (_resourceCacheMap == null)
        {
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "Initializing resource cache map");
            _resourceCacheMap = Collections
                    .synchronizedMap(new _ResourceMap<ResourceKey, Resource>(
                            getMaxSize()));
        }

        _resourceCacheMap.put(new ResourceKey(resourceName, libraryName,
                contentType), resource);
    }

    private boolean isResourceCachingEnabled()
    {
        if (_resourceCacheEnabled == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            //first, check to make sure that ProjectStage is production, if not, skip caching
            if (!facesContext.isProjectStage(ProjectStage.Production))
            {
                return _resourceCacheEnabled = Boolean.FALSE;
            }

            ExternalContext externalContext = facesContext.getExternalContext();
            if (externalContext == null)
                return false; //don't cache right now, but don't disable it yet either

            //if in production, make sure that the cache is not explicitly disabled via context param
            String configParam = externalContext
                    .getInitParameter(ResourceHandlerCache.RESOURCE_HANDLER_CACHE_ENABLED_ATTRIBUTE);
            _resourceCacheEnabled = configParam == null ? ResourceHandlerCache.RESOURCE_HANDLER_CACHE_ENABLED_DEFAULT
                    : Boolean.parseBoolean(configParam);

            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "MyFaces Resource Caching Enabled="
                        + _resourceCacheEnabled);
            }
        }
        return _resourceCacheEnabled;
    }

    private int getMaxSize()
    {
        ExternalContext externalContext = FacesContext.getCurrentInstance()
                .getExternalContext();

        String configParam = externalContext == null ? null : externalContext
                .getInitParameter(RESOURCE_HANDLER_CACHE_SIZE_ATTRIBUTE);
        return configParam == null ? RESOURCE_HANDLER_CACHE_DEFAULT_SIZE
                : Integer.parseInt(configParam);
    }

    public static class ResourceKey
    {
        private String resourceName;
        private String libraryName;
        private String contentType;

        public ResourceKey(String resourceName, String libraryName,
                String contentType)
        {
            this.resourceName = resourceName;
            this.libraryName = libraryName;
            this.contentType = contentType;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((contentType == null) ? 0 : contentType.hashCode());
            result = prime * result
                    + ((libraryName == null) ? 0 : libraryName.hashCode());
            result = prime * result
                    + ((resourceName == null) ? 0 : resourceName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ResourceKey other = (ResourceKey) obj;
            if (contentType == null)
            {
                if (other.contentType != null)
                    return false;
            }
            else if (!contentType.equals(other.contentType))
                return false;
            if (libraryName == null)
            {
                if (other.libraryName != null)
                    return false;
            }
            else if (!libraryName.equals(other.libraryName))
                return false;
            if (resourceName == null)
            {
                if (other.resourceName != null)
                    return false;
            }
            else if (!resourceName.equals(other.resourceName))
                return false;
            return true;
        }
    }

    private static class _ResourceMap<K, V> extends LinkedHashMap<K, V>
    {
        private static final long serialVersionUID = 1L;
        private int maxCapacity;

        public _ResourceMap(int cacheSize)
        {
            // create map at max capacity and 1.1 load factor to avoid rehashing
            super(cacheSize + 1, 1.1f, true);
            maxCapacity = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
        {
            return size() > maxCapacity;
        }
    }
}