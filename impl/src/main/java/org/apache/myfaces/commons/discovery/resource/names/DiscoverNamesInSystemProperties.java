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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;


/**
 * Recover resource name from System Properties.
 * 
 * @author Richard A. Sitze
 */
public class DiscoverNamesInSystemProperties
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private static Logger log = Logger.getLogger(DiscoverNamesInSystemProperties.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }
    
    /** Construct a new resource discoverer
     */
    public DiscoverNamesInSystemProperties() {
    }

    /**
     * @return Enumeration of ResourceInfo
     */
    public ResourceNameIterator findResourceNames(final String resourceName) {
        if (log.isLoggable(Level.FINE))
            log.fine("find: resourceName='" + resourceName + "'");

        return new ResourceNameIterator() {
            private String resource = System.getProperty(resourceName);
            
            public boolean hasNext() {
                return resource != null;
            }
            
            public String nextResourceName() {
                String element = resource;
                resource = null;
                return element;
            }
        };
    }
}
