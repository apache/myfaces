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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.Resource;
import org.apache.myfaces.commons.discovery.ResourceDiscover;
import org.apache.myfaces.commons.discovery.ResourceIterator;
import org.apache.myfaces.commons.discovery.jdk.JDKHooks;


/**
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author James Strachan
 */
public class DiscoverResources
    extends ResourceDiscoverImpl
    implements ResourceDiscover
{
    private static Logger log = Logger.getLogger(DiscoverResources.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }
    
    /**
     * Construct a new resource discoverer
     */
    public DiscoverResources() {
        super();
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverResources(ClassLoaders classLoaders) {
        super(classLoaders);
    }

    /**
     * @return ResourceIterator
     */
    public ResourceIterator findResources(final String resourceName) {
        if (log.isLoggable(Level.FINE))
            log.fine("find: resourceName='" + resourceName + "'");

        return new ResourceIterator() {
            private int idx = 0;
            private ClassLoader loader = null;
            private Enumeration resources = null;
            private Resource resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextResource();
                }
                return resource != null;
            }
            
            public Resource nextResource() {
                Resource element = resource;
                resource = null;
                return element;
            }
            
            private Resource getNextResource() {
                if (resources == null || !resources.hasMoreElements()) {
                    resources = getNextResources();
                }

                Resource resourceInfo;
                if (resources != null) {
                    URL url = (URL)resources.nextElement();

                    if (log.isLoggable(Level.FINE))
                        log.fine("getNextResource: next URL='" + url + "'");

                    resourceInfo = new Resource(resourceName, url, loader);
                } else {
                    resourceInfo = null;
                }
                
                return resourceInfo;
            }
            
            private Enumeration getNextResources() {
                while (idx < getClassLoaders().size()) {
                    loader = getClassLoaders().get(idx++);
                    if (log.isLoggable(Level.FINE))
                        log.fine("getNextResources: search using ClassLoader '" + loader + "'");
                    try {
                        Enumeration e = JDKHooks.getJDKHooks().getResources(loader, resourceName);
                        if (e != null && e.hasMoreElements()) {
                            return e;
                        }
                    } catch( IOException ex ) {
                        log.log(Level.WARNING, "getNextResources: Ignoring Exception", ex);
                    }
                }
                return null;
            }
        };
    }
}
