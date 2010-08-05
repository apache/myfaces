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
package org.apache.myfaces.commons.discovery.tools;

import org.apache.myfaces.commons.discovery.ResourceClass;
import org.apache.myfaces.commons.discovery.ResourceClassIterator;
import org.apache.myfaces.commons.discovery.resource.ClassLoaders;
import org.apache.myfaces.commons.discovery.resource.classes.DiscoverClasses;


/**
 * Holder for a default class.
 * 
 * Class may be specified by name (String) or class (Class).
 * Using the holder complicates the users job, but minimized # of API's.
 * 
 * @author Richard A. Sitze
 */
public class DefaultClassHolder {
    private Class        defaultClass;
    private final String defaultName;
    
    public DefaultClassHolder(Class defaultClass) {
        this.defaultClass = defaultClass;
        this.defaultName = defaultClass.getName();
    }
    
    public DefaultClassHolder(String defaultName) {
        this.defaultClass = null;
        this.defaultName = defaultName;
    }

    /**
     * @param spi non-null SPI
     * @param loaders Used only if class needs to be loaded.
     * 
     * @return Default Class.  Load the class if necessary,
     *         and verify that it implements the SPI.
     *         (this forces the check, no way out..).
     */
    public Class getDefaultClass(SPInterface spi, ClassLoaders loaders) {
        if (defaultClass == null) {
            DiscoverClasses classDiscovery = new DiscoverClasses(loaders);
            ResourceClassIterator classes = classDiscovery.findResourceClasses(getDefaultName());
            if (classes.hasNext()) {
                ResourceClass info = classes.nextResourceClass();
                try {
                    defaultClass = info.loadClass();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        if (defaultClass != null) {
            spi.verifyAncestory(defaultClass);
        }

        return defaultClass;
    }

    public String getDefaultName() {
        return defaultName;
    }
}
