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
package org.apache.myfaces.commons.discovery.jdk;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Richard A. Sitze
 */
public class JDK12Hooks extends JDKHooks {
    
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(JDK12Hooks.class.getName());
    
    
    private static final ClassLoader systemClassLoader
        = findSystemClassLoader();

    /**
     * Must be implemented to use DiscoveryLogFactory
     */
    public static void setLog(Logger _log) {
        log = _log;
    }
    
    /**
     * Get the system property
     *
     * @param propName name of the property
     * @return value of the property
     */
    public String getSystemProperty(final String propName) {
        return (String)
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                try {
                    return System.getProperty(propName);
                } catch (SecurityException se){
                    return null;
                }
            }
        });
    }

    /**
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * 
     * @return The thread context class loader, if available.
     *         Otherwise return null.
     */
    public ClassLoader getThreadContextClassLoader() {
        ClassLoader classLoader;
        
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException e) {
            /**
             * SecurityException is thrown when
             * a) the context class loader isn't an ancestor of the
             *    calling class's class loader, or
             * b) if security permissions are restricted.
             * 
             * For (a), ignore and keep going.  We cannot help but also
             * ignore (b) with the logic below, but other calls elsewhere
             * (to obtain a class loader) will re-trigger this exception
             * where we can make a distinction.
             */
            classLoader = null;  // ignore
        }
        
        // Return the selected class loader
        return classLoader;
    }
    
    /**
     * The system class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * 
     * @return The system class loader, if available.
     *         Otherwise return null.
     */
    public ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    /**
     * Implement ClassLoader.getResources for JDK 1.2
     */
    public Enumeration getResources(ClassLoader loader,
                                    String resourceName)
        throws IOException
    {
        /**
         * The simple answer is/was:
         *    return loader.getResources(resourceName);
         * 
         * However, some classloaders overload the behavior of getResource
         * (loadClass, etc) such that the order of returned results changes
         * from normally expected behavior.
         * 
         * Example: locate classes/resources from child ClassLoaders first,
         *          parents last (in some J2EE environs).
         * 
         * The resource returned by getResource() should be the same as the
         * first resource returned by getResources().  Unfortunately, this
         * is not, and cannot be: getResources() is 'final' in the current
         * JDK's (1.2, 1.3, 1.4).
         * 
         * To address this, the implementation of this method will
         * return an Enumeration such that the first element is the
         * results of getResource, and all trailing elements are
         * from getResources.  On each iteration, we check so see
         * if the resource (from getResources) matches the first resource,
         * and eliminate the redundent element.
         */
        
        final URL first = loader.getResource(resourceName);
        
        // XXX: Trying to avoid JBoss UnifiedClassLoader problem
        
        Enumeration resources;
        
        if(first == null) {
            log.fine("Could not find resource: " + resourceName);
            resources = Collections.enumeration(Collections.EMPTY_LIST);
            
        } else {
        
            try {
                
                resources = loader.getResources(resourceName);
                
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Exception occured during attept to get " + resourceName 
                        + " from " + first, ex);
                resources = Collections.enumeration(Collections.EMPTY_LIST);
            }
            
            resources = getResourcesFromUrl(first, resources);
        }
        
        return resources;
    }
    
    private static Enumeration getResourcesFromUrl(final URL first, final Enumeration rest) {
        return new Enumeration() {
            private boolean firstDone = (first == null);
            private URL next = getNext();
            
            public Object nextElement() {
                URL o = next;
                next = getNext();
                return o;
            }

            public boolean hasMoreElements() {
                return next != null;
            }
            
            private URL getNext() {
                URL n;
                
                if (!firstDone) {
                    /**
                     * First time through, use results of getReference()
                     * if they were non-null.
                     */
                    firstDone = true;
                    n = first;
                } else {
                    /**
                     * Subsequent times through,
                     * use results of getReferences()
                     * but take out anything that matches 'first'.
                     * 
                     * Iterate through list until we find one that
                     * doesn't match 'first'.
                     */
                    n = null;
                    while (rest.hasMoreElements()  &&  n == null) {
                        n = (URL)rest.nextElement();
                        if (first != null &&
                            n != null &&
                            n.equals(first))
                        {
                            n = null;
                        }
                    }
                }
                
                return n;
            }
        };
    }
    
    static private ClassLoader findSystemClassLoader() {
        ClassLoader classLoader;
        
        try {
            classLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException e) {
            /**
             * Ignore and keep going.
             */
            classLoader = null;
        }

        if (classLoader == null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkCreateClassLoader();
                    classLoader = new PsuedoSystemClassLoader();
                } catch (SecurityException se){
                }
            }
        }
        
        // Return the selected class loader
        return classLoader;
    }
}
