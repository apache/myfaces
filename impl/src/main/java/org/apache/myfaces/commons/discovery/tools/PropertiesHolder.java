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

import java.util.Properties;

import org.apache.myfaces.commons.discovery.resource.ClassLoaders;


/**
 * Holder for a default class.
 * 
 * Class may be specified by name (String) or class (Class).
 * Using the holder complicates the users job, but minimized # of API's.
 * 
 * @author Richard A. Sitze
 */
public class PropertiesHolder {
    private Properties   properties;
    private final String propertiesFileName;
    
    public PropertiesHolder(Properties properties) {
        this.properties = properties;
        this.propertiesFileName = null;
    }
    
    public PropertiesHolder(String propertiesFileName) {
        this.properties = null;
        this.propertiesFileName = propertiesFileName;
    }

    /**
     * @param spi Optional SPI (may be null).
     *            If provided, an attempt is made to load the
     *            property file as-per Class.getResource().
     * 
     * @param loaders Used only if properties need to be loaded.
     * 
     * @return Properties.  Load the properties if necessary.
     */
    public Properties getProperties(SPInterface spi, ClassLoaders loaders) {
        if (properties == null) {
            properties = ResourceUtils.loadProperties(spi.getSPClass(), getPropertiesFileName(), loaders);
        }
        return properties;
    }

    public String getPropertiesFileName() {
        return propertiesFileName;
    }
}
