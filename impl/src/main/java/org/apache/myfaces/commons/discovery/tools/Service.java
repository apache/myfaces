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

import java.util.Enumeration;

import org.apache.myfaces.commons.discovery.ResourceClass;
import org.apache.myfaces.commons.discovery.ResourceClassIterator;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.commons.discovery.resource.ClassLoaders;
import org.apache.myfaces.commons.discovery.resource.classes.DiscoverClasses;
import org.apache.myfaces.commons.discovery.resource.names.DiscoverServiceNames;


/**
 * [this was ServiceDiscovery12... the 1.1 versus 1.2 issue
 * has been abstracted to org.apache.commons.discover.jdk.JDKHooks]
 * 
 * <p>Implement the JDK1.3 'Service Provider' specification.
 * ( http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html )
 * </p>
 *
 * This class supports any VM, including JDK1.1, via
 * org.apache.commons.discover.jdk.JDKHooks.
 *
 * The caller will first configure the discoverer by adding ( in the desired
 * order ) all the places to look for the META-INF/services. Currently
 * we support loaders.
 *
 * The findResources() method will check every loader.
 *
 * @author Richard A. Sitze
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author James Strachan
 */
public class Service
{
    /** Construct a new service discoverer
     */
    protected Service() {
    }
    
    /**
     * as described in
     * sun/jdk1.3.1/docs/guide/jar/jar.html#Service Provider,
     * Except this uses <code>Enumeration</code>
     * instead of <code>Interator</code>.
     * 
     * @return Enumeration of class instances (<code>Object</code>)
     */
    public static Enumeration providers(Class spiClass) {
        return providers(new SPInterface(spiClass), null);
    }
    
    /**
     * This version lets you specify constructor arguments..
     * 
     * @param spi SPI to look for and load.
     * @param loaders loaders to use in search.
     *        If <code>null</code> then use ClassLoaders.getAppLoaders().
     */
    public static Enumeration providers(final SPInterface spi,
                                        ClassLoaders loaders)
    {
        if (loaders == null) {
            loaders = ClassLoaders.getAppLoaders(spi.getSPClass(),
                                                 Service.class,
                                                 true);
        }
        
        ResourceNameIterator servicesIter =
            (new DiscoverServiceNames(loaders)).findResourceNames(spi.getSPName());

        final ResourceClassIterator services =
            (new DiscoverClasses(loaders)).findResourceClasses(servicesIter);
        
        return new Enumeration() {
            private Object object = null;
            
            public boolean hasMoreElements() {
                if (object == null) {
                    object = getNextClassInstance();
                }
                return object != null;
            }
            
            public Object nextElement() {
                Object obj = object;
                object = null;
                return obj;
            }

            private Object getNextClassInstance() {
                while (services.hasNext()) {
                    ResourceClass info = services.nextResourceClass();
                    try {
                        return spi.newInstance(info.loadClass());
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return null;
            }
        };
    }
}
