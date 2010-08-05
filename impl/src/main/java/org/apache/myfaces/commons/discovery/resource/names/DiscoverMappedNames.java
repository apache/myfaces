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

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;


/**
 * Recover resource name from Managed Properties,
 * using OLD property names.
 * 
 * This class maintains a mapping between old names and
 * (new) the class names they represent.  The discovery
 * mechanism uses the class names as property names.
 * 
 * @see org.apache.myfaces.commons.discovery.tools.ManagedProperties
 * 
 * @author Richard A. Sitze
 */
public class DiscoverMappedNames
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private static Logger log = Logger.getLogger(DiscoverMappedNames.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }
    
    private Hashtable mapping = new Hashtable();  // String name ==> String[] newNames
    
    /** Construct a new resource discoverer
     */
    public DiscoverMappedNames() {
    }
    
    public void map(String fromName, String toName) {
        mapping.put(fromName, toName);
    }
    
    public void map(String fromName, String [] toNames) {
        mapping.put(fromName, toNames);
    }

    /**
     * @return Enumeration of ResourceInfo
     */
    public ResourceNameIterator findResourceNames(final String resourceName) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("find: resourceName='" + resourceName + "', mapping to constants");
        }
        
        final Object obj = mapping.get(resourceName);

        final String[] names;
        if (obj instanceof String) {
            names = new String[] { (String)obj };
        } else if (obj instanceof String[]) {
            names = (String[])obj;
        } else {
            names = null;
        }
        
        return new ResourceNameIterator() {

            private int idx = 0;
            
            public boolean hasNext() {
                if (names != null) {
                    while (idx < names.length  &&  names[idx] == null) {
                        idx++;
                    }
                    return idx < names.length;
                }
                return false;
            }
            
            public String nextResourceName() {
                return hasNext() ? names[idx++] : null;
            }
        };
    }
}
