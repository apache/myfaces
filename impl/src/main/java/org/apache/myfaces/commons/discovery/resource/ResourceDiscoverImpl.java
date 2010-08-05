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
package org.apache.myfaces.commons.discovery.resource;

import org.apache.myfaces.commons.discovery.Resource;
import org.apache.myfaces.commons.discovery.ResourceDiscover;
import org.apache.myfaces.commons.discovery.ResourceIterator;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.commons.discovery.resource.names.ResourceNameDiscoverImpl;


/**
 * Helper class for methods implementing the ResourceDiscover interface.
 * 
 * @author Richard A. Sitze
 */
public abstract class ResourceDiscoverImpl
    extends ResourceNameDiscoverImpl
    implements ResourceDiscover
{
    private ClassLoaders classLoaders;

    
    /**
     * Construct a new resource discoverer
     */
    public ResourceDiscoverImpl() {
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public ResourceDiscoverImpl(ClassLoaders classLoaders) {
        setClassLoaders(classLoaders);
    }

    /**
     * Specify set of class loaders to be used in searching.
     */
    public void setClassLoaders(ClassLoaders loaders) {
        classLoaders = loaders;
    }

    /**
     * Specify a new class loader to be used in searching.
     * The order of loaders determines the order of the result.
     * It is recommended to add the most specific loaders first.
     */
    public void addClassLoader(ClassLoader loader) {
        getClassLoaders().put(loader);
    }

    protected ClassLoaders getClassLoaders() {
        if (classLoaders == null) {
            classLoaders = ClassLoaders.getLibLoaders(this.getClass(), null, true);
        }
        return classLoaders;
    }

    /**
     * Locate names of resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceNameIterator
     */
    public ResourceNameIterator findResourceNames(String resourceName) {
        return findResources(resourceName);
    }

    /**
     * Locate names of resources that are bound to <code>resourceNames</code>.
     * 
     * @return ResourceNameIterator
     */
    public ResourceNameIterator findResourceNames(ResourceNameIterator resourceNames) {
        return findResources(resourceNames);
    }

    /**
     * Locate resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceIterator
     */
    public abstract ResourceIterator findResources(String resourceName);

    /**
     * Locate resources that are bound to <code>resourceNames</code>.
     * 
     * @return ResourceIterator
     */
    public ResourceIterator findResources(final ResourceNameIterator inputNames) {
        return new ResourceIterator() {
            private ResourceIterator resources = null;
            private Resource resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextResource();
                }
                return resource != null;
            }
            
            public Resource nextResource() {
                Resource rsrc = resource;
                resource = null;
                return rsrc;
            }
            
            private Resource getNextResource() {
                while (inputNames.hasNext() &&
                       (resources == null  ||  !resources.hasNext())) {
                    resources = findResources(inputNames.nextResourceName());
                }
    
                return (resources != null  &&  resources.hasNext())
                       ? resources.nextResource()
                       : null;
            }
        };
    }
}
