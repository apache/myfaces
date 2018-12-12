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
package org.apache.myfaces.resource;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.ProjectStage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.myfaces.config.MyfacesConfig;
import org.apache.myfaces.util.ConcurrentLRUCache;

public class ResourceHandlerCache
{
    private static final Logger log = Logger.getLogger(ResourceHandlerCache.class.getName());

    private boolean _resourceCacheEnabled = false;

    private volatile ConcurrentLRUCache<Object, ResourceValue> _resourceCacheMap = null;
    private volatile ConcurrentLRUCache<Object, ResourceValue> _viewResourceCacheMap = null;
    private volatile ConcurrentLRUCache<Object, Boolean> _libraryExistsCacheMap = null;

    public ResourceHandlerCache()
    {
        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Initializing ResourceHandlerCache");
        }
     
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        // skip cache when ProjectStage != Production
        if (facesContext.isProjectStage(ProjectStage.Production))
        {
            //if in production, make sure that the cache is not explicitly disabled via context param
            _resourceCacheEnabled = MyfacesConfig.getCurrentInstance(externalContext).isResourceHandlerCacheEnabled();

            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "MyFaces ResourceHandlerCache enabled = " + _resourceCacheEnabled);
            }
        }
        
        if (_resourceCacheEnabled)
        {
            int maxSize = MyfacesConfig.getCurrentInstance(externalContext).getResourceHandlerCacheSize();

            _resourceCacheMap = new ConcurrentLRUCache<>((maxSize * 4 + 3) / 3, maxSize);
            _viewResourceCacheMap = new ConcurrentLRUCache<>((maxSize * 4 + 3) / 3, maxSize);
            _libraryExistsCacheMap = new ConcurrentLRUCache<>((maxSize * 4 + 3) / 3, maxSize / 5);
        }
    }
    
    
    public ResourceValue getResource(String resourceName, String libraryName, String contentType, String localePrefix)
    {
        return getResource(resourceName, libraryName, contentType, localePrefix, null);
    }
    
    public ResourceValue getResource(String resourceName, String libraryName, String contentType, String localePrefix,
            String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get resource from cache for " + resourceName);
        }

        ResourceKey key = new ResourceKey(resourceName, libraryName, contentType, localePrefix, contractName);

        return _resourceCacheMap.get(key);
    }    

    public boolean containsResource(String resourceName, String libraryName, String contentType, String localePrefix)
    {
        return containsResource(resourceName, libraryName, contentType, localePrefix, null);
    }
    
    public boolean containsResource(String resourceName, String libraryName, String contentType, String localePrefix,
            String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return false;
        }

        ResourceKey key = new ResourceKey(resourceName, libraryName, contentType, localePrefix);
        return _resourceCacheMap.get(key) != null;
    }

    public void putResource(String resourceName, String libraryName, String contentType, String localePrefix,
            ResourceMeta resource, ResourceLoader loader)
    {
        putResource(resourceName, libraryName, contentType, localePrefix, null, resource, loader, null);
    }
    
    public void putResource(String resourceName, String libraryName, String contentType, String localePrefix,
            String contractName, ResourceMeta resource, ResourceLoader loader, ResourceCachedInfo info)
    {
        if (!_resourceCacheEnabled)
        {
            return;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to put resource to cache for " + resourceName);
        }

        _resourceCacheMap.put(new ResourceKey(resourceName, libraryName,
                contentType, localePrefix, contractName), new ResourceValue(resource, loader, info));
    }
    
    public ResourceValue getResource(String resourceId)
    {
        if (!_resourceCacheEnabled)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get resource from cache for " + resourceId);
        }

        return _resourceCacheMap.get(resourceId);
    }

    public ResourceValue getResource(String resourceId, String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get resource from cache for " + resourceId);
        }

        return _resourceCacheMap.get(contractName+':'+resourceId);
    }
    
    public boolean containsResource(String resourceId, String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return false;
        }

        return _resourceCacheMap.get(contractName + ':' + resourceId) != null;
    }
    
    public boolean containsResource(String resourceId)
    {
        if (!_resourceCacheEnabled)
        {
            return false;
        }

        return _resourceCacheMap.get(resourceId) != null;
    }

    public void putResource(String resourceId, ResourceMeta resource, ResourceLoader loader, ResourceCachedInfo info)
    {
        if (!_resourceCacheEnabled)
        {
            return;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to put resource to cache for " + resourceId);
        }

        if (resource.getContractName() != null)
        {
            _resourceCacheMap.put(resource.getContractName() + ':' + resourceId, new ResourceValue(resource, loader));
        }
        else
        {
            _resourceCacheMap.put(resourceId, new ResourceValue(resource, loader, info));
        }
    }

    public boolean containsViewResource(String resourceName, String contentType, String localePrefix)
    {
        return containsViewResource(resourceName, contentType, localePrefix, null);
    }
    
    public boolean containsViewResource(String resourceName, String contentType, String localePrefix,
            String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return false;
        }

        ResourceKey key = new ResourceKey(resourceName, null, contentType, localePrefix, contractName);
        return _viewResourceCacheMap.get(key) != null;
    }
    
    public ResourceValue getViewResource(String resourceName, String contentType, String localePrefix)
    {
        return getViewResource(resourceName, contentType, localePrefix, null);
    }
    
    public ResourceValue getViewResource(String resourceName, String contentType, String localePrefix,
            String contractName)
    {
        if (!_resourceCacheEnabled)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get resource from cache for " + resourceName);
        }

        ResourceKey key = new ResourceKey(resourceName, null, contentType, localePrefix, contractName);
        return _viewResourceCacheMap.get(key);
    }
    
    public void putViewResource(String resourceName, String contentType, 
        String localePrefix, ResourceMeta resource, ResourceLoader loader, ResourceCachedInfo info)
    {
        putViewResource(resourceName, contentType, localePrefix, null, resource, loader, info);
    }
    
    public void putViewResource(String resourceName, String contentType, 
        String localePrefix, String contractName, ResourceMeta resource, ResourceLoader loader,
        ResourceCachedInfo info)
    {
        if (!_resourceCacheEnabled)
        {
            return;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to put resource to cache for " + resourceName);
        }

        _viewResourceCacheMap.put(new ResourceKey(resourceName, null,
                contentType, localePrefix, contractName), new ResourceValue(resource, loader, info));
    }
    
    public Boolean libraryExists(String libraryName)
    {
        if (!_resourceCacheEnabled)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get libraryExists from cache for " + libraryName);
        }

        return _libraryExistsCacheMap.get(libraryName);
    }
    
    public void confirmLibraryExists(String libraryName)
    {
        if (!_resourceCacheEnabled)
        {
            return;
        }
        
        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to set confirmLibraryExists on cache " + libraryName);
        }

        _libraryExistsCacheMap.put(libraryName, Boolean.TRUE);
    }
    
    public void confirmLibraryNotExists(String libraryName)
    {
        if (!_resourceCacheEnabled)
        {
            return;
        }
        
        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to set confirmLibraryExists on cache " + libraryName);
        }

        _libraryExistsCacheMap.put(libraryName, Boolean.FALSE);
    }    


    public static class ResourceKey
    {
        private final String resourceName;
        private final String libraryName;
        private final String contentType;
        private final String localePrefix;
        private final String contractName;

        public ResourceKey(String resourceName, String libraryName, String contentType, String localePrefix)
        {
            this(resourceName, libraryName, contentType, localePrefix, null);
        }
        
        public ResourceKey(String resourceName, String libraryName, String contentType, String localePrefix,
                String contractName)
        {
            this.resourceName = resourceName;
            this.libraryName = libraryName;
            this.contentType = contentType;
            this.localePrefix = localePrefix;
            this.contractName = contractName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ResourceKey that = (ResourceKey) o;

            if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null)
            {
                return false;
            }
            if (libraryName != null ? !libraryName.equals(that.libraryName) : that.libraryName != null)
            {
                return false;
            }
            if (localePrefix != null ? !localePrefix.equals(that.localePrefix) : that.localePrefix != null)
            {
                return false;
            }
            if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
            {
                return false;
            }
            if (contractName != null ? !contractName.equals(that.contractName) : that.contractName != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = resourceName != null ? resourceName.hashCode() : 0;
            result = 31 * result + (libraryName != null ? libraryName.hashCode() : 0);
            result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
            result = 31 * result + (localePrefix != null ? localePrefix.hashCode() : 0);
            result = 31 * result + (contractName != null ? contractName.hashCode() : 0);
            return result;
        }
    }

    public static class ResourceValue
    {
        private final ResourceMeta resourceMeta;
        private final ResourceLoader resourceLoader;
        private final ResourceCachedInfo info;
        
        public ResourceValue(ResourceMeta resourceMeta, ResourceLoader resourceLoader)
        {
            this.resourceMeta = resourceMeta;
            this.resourceLoader = resourceLoader;
            this.info = null;
        }
        
        public ResourceValue(ResourceMeta resourceMeta, ResourceLoader resourceLoader, ResourceCachedInfo info)
        {
            super();
            this.resourceMeta = resourceMeta;
            this.resourceLoader = resourceLoader;
            this.info = info;
        }

        public ResourceMeta getResourceMeta()
        {
            return resourceMeta;
        }

        public ResourceLoader getResourceLoader()
        {
            return resourceLoader;
        }
        
        public ResourceCachedInfo getCachedInfo()
        {
            return info;
        }
    }
        
}
