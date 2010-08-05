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

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;


/**
 * Holder for multiple ResourceNameDiscover instances.
 * The result is the union of the results from each
 * (not a chained sequence, where results feed the next in line.
 *
 * @author Richard A. Sitze
 */
public class NameDiscoverers
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private static Logger log = Logger.getLogger(NameDiscoverers.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }

    private Vector discoverers = new Vector();
    
    /**
     *  Construct a new resource name discoverer
     */
    public NameDiscoverers() {
    }
    
    /**
     * Specify an additional class loader to be used in searching.
     * The order of loaders determines the order of the result.
     * It is recommended to add the most specific loaders first.
     */
    public void addResourceNameDiscover(ResourceNameDiscover discover) {
        if (discover != null) {
            discoverers.addElement(discover);
        }
    }

    protected ResourceNameDiscover getResourceNameDiscover(int idx) {
        return (ResourceNameDiscover)discoverers.get(idx);
    }

    protected int size() {
        return discoverers.size();
    }

    /**
     * Set of results of all discoverers.
     * 
     * @return ResourceIterator
     */
    public ResourceNameIterator findResourceNames(final String resourceName) {
        if (log.isLoggable(Level.FINE))
            log.fine("find: resourceName='" + resourceName + "'");

        return new ResourceNameIterator() {
            private int idx = 0;
            private ResourceNameIterator iterator = null;
            
            public boolean hasNext() {
                if (iterator == null  ||  !iterator.hasNext()) {
                    iterator = getNextIterator();
                    if (iterator == null) {
                        return false;
                    }
                }
                return iterator.hasNext();
            }
            
            public String nextResourceName() {
                return iterator.nextResourceName();
            }
            
            private ResourceNameIterator getNextIterator() {
                while (idx < size()) {
                    ResourceNameIterator iter =
                        getResourceNameDiscover(idx++).findResourceNames(resourceName);

                    if (iter.hasNext()) {
                        return iter;
                    }
                }
                return null;
            }
        };
    }
}
