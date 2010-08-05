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
package org.apache.myfaces.commons.discovery.resource.classes;

import org.apache.myfaces.commons.discovery.ResourceClass;
import org.apache.myfaces.commons.discovery.ResourceClassDiscover;
import org.apache.myfaces.commons.discovery.ResourceClassIterator;
import org.apache.myfaces.commons.discovery.ResourceIterator;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.commons.discovery.resource.ClassLoaders;
import org.apache.myfaces.commons.discovery.resource.ResourceDiscoverImpl;


/**
 * @author Richard A. Sitze
 */
public abstract class ResourceClassDiscoverImpl
    extends ResourceDiscoverImpl
    implements ResourceClassDiscover
{
    /**
     * Construct a new resource discoverer
     */
    public ResourceClassDiscoverImpl() {
        super();
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public ResourceClassDiscoverImpl(ClassLoaders classLoaders) {
        super(classLoaders);
    }


    /**
     * Locate names of resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceNameIterator
     */
    public ResourceNameIterator findResourceNames(String resourceName) {
        return findResourceClasses(resourceName);
    }

    /**
     * Locate names of resources that are bound to <code>resourceNames</code>.
     * 
     * @return ResourceNameIterator
     */
    public ResourceNameIterator findResourceNames(ResourceNameIterator resourceNames) {
        return findResourceClasses(resourceNames);
    }

    /**
     * Locate resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceIterator
     */
    public ResourceIterator findResources(String resourceName) {
        return findResourceClasses(resourceName);
    }

    /**
     * Locate resources that are bound to <code>resourceNames</code>.
     * 
     * @return ResourceIterator
     */
    public ResourceIterator findResources(ResourceNameIterator resourceNames) {
        return findResourceClasses(resourceNames);
    }


    /**
     * Locate class resources that are bound to <code>className</code>.
     * 
     * @return ResourceClassIterator
     */
    public abstract ResourceClassIterator findResourceClasses(String className);

    /**
     * Locate class resources that are bound to <code>resourceNames</code>.
     * 
     * @return ResourceIterator
     */
    public ResourceClassIterator findResourceClasses(final ResourceNameIterator inputNames) {
        return new ResourceClassIterator() {
            private ResourceClassIterator classes = null;
            private ResourceClass resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextResource();
                }
                return resource != null;
            }
            
            public ResourceClass nextResourceClass() {
                ResourceClass rsrc = resource;
                resource = null;
                return rsrc;
            }
            
            private ResourceClass getNextResource() {
                while (inputNames.hasNext() &&
                       (classes == null  ||  !classes.hasNext())) {
                    classes =
                        findResourceClasses(inputNames.nextResourceName());
                }
    
                return (classes != null  &&  classes.hasNext())
                       ? classes.nextResourceClass()
                       : null;
            }
        };
    }
}
