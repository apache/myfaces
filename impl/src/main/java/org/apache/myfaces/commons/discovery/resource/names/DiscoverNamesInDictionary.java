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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;


/**
 * Recover resources from a Dictionary.  This covers Properties as well,
 * since <code>Properties extends Hashtable extends Dictionary</code>.
 * 
 * The recovered value is expected to be either a <code>String</code>
 * or a <code>String[]</code>.
 * 
 * @author Richard A. Sitze
 */
public class DiscoverNamesInDictionary
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private static Logger log = Logger.getLogger(DiscoverNamesInDictionary.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }

    private Dictionary dictionary;
    
    /** Construct a new resource discoverer
     */
    public DiscoverNamesInDictionary() {
        setDictionary(new Hashtable());
    }
    
    /** Construct a new resource discoverer
     */
    public DiscoverNamesInDictionary(Dictionary dictionary) {
        setDictionary(dictionary);
    }

    protected Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Specify set of class loaders to be used in searching.
     */
    public void setDictionary(Dictionary table) {
        this.dictionary = table;
    }
    
    public void addResource(String resourceName, String resource) {
        dictionary.put(resourceName, resource);
    }
    
    public void addResource(String resourceName, String[] resources) {
        dictionary.put(resourceName, resources);
    }

    /**
     * @return Enumeration of ResourceInfo
     */
    public ResourceNameIterator findResourceNames(final String resourceName) {
        if (log.isLoggable(Level.FINE))
            log.fine("find: resourceName='" + resourceName + "'");

        Object baseResource = dictionary.get(resourceName);

        final String[] resources;
        if (baseResource instanceof String) {
            resources = new String[] { (String)baseResource };
        } else if (baseResource instanceof String[]) {
            resources = (String[])baseResource;
        } else {
            resources = null;
        }

        return new ResourceNameIterator() {
            private int idx = 0;
            
            public boolean hasNext() {
                if (resources != null) {
                    while (idx < resources.length  &&  resources[idx] == null) {
                        idx++;
                    }
                    return idx < resources.length;
                }
                return false;
            }
            
            public String nextResourceName() {
                return hasNext() ? resources[idx++] : null;
            }
        };
    }
}
