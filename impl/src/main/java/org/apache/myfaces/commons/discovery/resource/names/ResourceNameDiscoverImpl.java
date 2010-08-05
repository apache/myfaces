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
package org.apache.myfaces.commons.discovery.resource.names;

import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;


/**
 * Helper class for methods implementing the ResourceNameDiscover interface.
 * 
 * @author Richard A. Sitze
 */
public abstract class ResourceNameDiscoverImpl implements ResourceNameDiscover
{
    /**
     * Locate names of resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceNameIterator
     */
    public abstract ResourceNameIterator findResourceNames(String resourceName);

    /**
     * Locate names of resources that are bound to <code>resourceName</code>.
     * 
     * @return ResourceNameIterator
     */
    public ResourceNameIterator findResourceNames(final ResourceNameIterator inputNames) {
        return new ResourceNameIterator() {
            private ResourceNameIterator resourceNames = null;
            private String resourceName = null;
            
            public boolean hasNext() {
                if (resourceName == null) {
                    resourceName = getNextResourceName();
                }
                return resourceName != null;
            }
            
            public String nextResourceName() {
                String name = resourceName;
                resourceName = null;
                return name;
            }
            
            private String getNextResourceName() {
                while (inputNames.hasNext() && (resourceNames == null  ||  !resourceNames.hasNext())) {
                    resourceNames =
                        findResourceNames(inputNames.nextResourceName());
                }
    
                return (resourceNames != null  &&  resourceNames.hasNext())
                       ? resourceNames.nextResourceName()
                       : null;
            }
        };
    }
}
