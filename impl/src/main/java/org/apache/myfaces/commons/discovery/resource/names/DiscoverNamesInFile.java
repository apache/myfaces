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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.Resource;
import org.apache.myfaces.commons.discovery.ResourceDiscover;
import org.apache.myfaces.commons.discovery.ResourceIterator;
import org.apache.myfaces.commons.discovery.ResourceNameDiscover;
import org.apache.myfaces.commons.discovery.ResourceNameIterator;
import org.apache.myfaces.commons.discovery.resource.ClassLoaders;
import org.apache.myfaces.commons.discovery.resource.DiscoverResources;



/**
 * Discover ALL files of a given name, and return resource names
 * contained within the set of files:
 * <ul>
 *   <li>one resource name per line,</li>
 *   <li>whitespace ignored,</li>
 *   <li>comments begin with '#'</li>
 * </ul>
 * 
 * Default discoverer is DiscoverClassLoaderResources,
 * but it can be set to any other.
 *
 * @author Richard A. Sitze
 * @author Costin Manolache
 * @author James Strachan
 */
public class DiscoverNamesInFile
    extends ResourceNameDiscoverImpl
    implements ResourceNameDiscover
{
    private static Logger log = Logger.getLogger(DiscoverNamesInFile.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }
    
    private ResourceDiscover _discoverResources;
    
    private final String _prefix;
    private final String _suffix;
        
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile() {
        _discoverResources = new DiscoverResources();
        _prefix = null;
        _suffix = null;
    }
        
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(String prefix, String suffix) {
        _discoverResources = new DiscoverResources();
        _prefix = prefix;
        _suffix = suffix;
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ClassLoaders loaders) {
        _discoverResources = new DiscoverResources(loaders);
        _prefix = null;
        _suffix = null;
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ClassLoaders loaders, String prefix, String suffix) {
        _discoverResources = new DiscoverResources(loaders);
        _prefix = prefix;
        _suffix = suffix;
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ResourceDiscover discoverer) {
        _discoverResources = discoverer;
        _prefix = null;
        _suffix = null;
    }
    
    /**
     *  Construct a new resource discoverer
     */
    public DiscoverNamesInFile(ResourceDiscover discoverer, String prefix, String suffix) {
        _discoverResources = discoverer;
        _prefix = prefix;
        _suffix = suffix;
    }

    /**
     * Specify set of class loaders to be used in searching.
     */
    public void setDiscoverer(ResourceDiscover discover) {
        _discoverResources = discover;
    }

    /**
     * To be used by downstream elements..
     */
    public ResourceDiscover getDiscover() {
        return _discoverResources;
    }

    /**
     * @return Enumeration of ServiceInfo
     */
    public ResourceNameIterator findResourceNames(final String serviceName) {
        String fileName;
        if (_prefix != null && _prefix.length() > 0) {
            fileName = _prefix + serviceName;
        } else {
            fileName = serviceName;
        }

        if (_suffix != null && _suffix.length() > 0) {
            fileName = fileName + _suffix;
        }

        if (log.isLoggable(Level.FINE)) {
            if (_prefix != null  &&  _suffix != null) {
                log.fine("find: serviceName='" + serviceName + "' as '" + fileName + "'");
            } else {
                log.fine("find: serviceName = '" + fileName + "'");
            }
        }


        final ResourceIterator files =
            getDiscover().findResources(fileName);

        return new ResourceNameIterator() {
            private int idx = 0;
            private Vector classNames = null;
            private String resource = null;
            
            public boolean hasNext() {
                if (resource == null) {
                    resource = getNextClassName();
                }
                return resource != null;
            }
            
            public String nextResourceName() {
                String element = resource;
                resource = null;
                return element;
            }
            
            private String getNextClassName() {
                if (classNames == null || idx >= classNames.size()) {
                    classNames = getNextClassNames();
                    idx = 0;
                    if (classNames == null) {
                        return null;
                    }
                }

                String className = (String)classNames.get(idx++);

                if (log.isLoggable(Level.FINE))
                    log.fine("getNextClassResource: next class='" + className + "'");

                return className;
            }

            private Vector getNextClassNames() {
                while (files.hasNext()) {
                    Vector results = readServices(files.nextResource());
                    if (results != null  &&  results.size() > 0) {
                        return results;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Read everything, no defering here..
     * Ensure that files are closed before we leave.
     */
    private Vector readServices(final Resource info) {
        Vector results = new Vector();
        
        InputStream is = info.getResourceAsStream();
        
        if( is != null ) {
            try {
                try {
                    // This code is needed by EBCDIC and other
                    // strange systems.  It's a fix for bugs
                    // reported in xerces
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (java.io.UnsupportedEncodingException e) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    
                    try {
                        String serviceImplName;
                        while( (serviceImplName = rd.readLine()) != null) {
                            int idx = serviceImplName.indexOf('#');
                            if (idx >= 0) {
                                serviceImplName = serviceImplName.substring(0, idx);
                            }
                            serviceImplName = serviceImplName.trim();
    
                            if (serviceImplName.length() != 0) {
                                results.add(serviceImplName);
                            }
                        }
                    } finally {
                        rd.close();
                    }
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        
        return results;
    }
}
