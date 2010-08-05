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

import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.ResourceClass;
import org.apache.myfaces.commons.discovery.ResourceClassDiscover;
import org.apache.myfaces.commons.discovery.ResourceClassIterator;
import org.apache.myfaces.commons.discovery.resource.ClassLoaders;


/**
 * The findResources() method will check every loader.
 *
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author James Strachan
 */
public class DiscoverClasses
    extends ResourceClassDiscoverImpl
    implements ResourceClassDiscover
{
    private static Logger log = Logger.getLogger(DiscoverClasses.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }

    /** Construct a new resource discoverer
     */
    public DiscoverClasses() {
        super();
    }
    
    /** Construct a new resource discoverer
     */
    public DiscoverClasses(ClassLoaders classLoaders) {
        super(classLoaders);
    }
    
    public ResourceClassIterator findResourceClasses(final String className) {
        final String resourceName = className.replace('.','/') + ".class";
        
        if (log.isLoggable(Level.FINE))
            log.fine("find: className='" + className + "'");

        return new ResourceClassIterator() {
            private Vector history = new Vector();
            private int idx = 0;
            private ResourceClass resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextClass();
                }
                return resource != null;
            }
            
            public ResourceClass nextResourceClass() {
                ResourceClass element = resource;
                resource = null;
                return element;
            }
            
            private ResourceClass getNextClass() {
                while (idx < getClassLoaders().size()) {
                    ClassLoader loader = getClassLoaders().get(idx++);
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        if (!history.contains(url)) {
                            history.addElement(url);
    
                            if (log.isLoggable(Level.FINE))
                                log.fine("getNextClass: next URL='" + url + "'");
    
                            return new ResourceClass(className, url, loader);
                        }
                        if (log.isLoggable(Level.FINE))
                            log.fine("getNextClass: duplicate URL='" + url + "'");
                    } else {
                        if (log.isLoggable(Level.FINE))
                            log.fine("getNextClass: loader " + loader + ": '" + resourceName + "' not found");
                    }
                }
                return null;
            }
        };
    }
}
