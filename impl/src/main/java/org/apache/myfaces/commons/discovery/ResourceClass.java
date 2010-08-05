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
package org.apache.myfaces.commons.discovery;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 'Resource' located by discovery.
 * Naming of methods becomes a real pain ('getClass()')
 * so I've patterned this after ClassLoader...
 * 
 * I think it works well as it will give users a point-of-reference.
 * 
 * @author Richard A. Sitze
 */
public class ResourceClass extends Resource
{
    private static Logger log = Logger.getLogger(ResourceClass.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }
    protected Class       resourceClass;

    public ResourceClass(Class resourceClass, URL resource) {
        super(resourceClass.getName(), resource, resourceClass.getClassLoader());
        this.resourceClass = resourceClass;
    }

    public ResourceClass(String resourceName, URL resource, ClassLoader loader) {
        super(resourceName, resource, loader);
    }
    
    /**
     * Get the value of resourceClass.
     * Loading the class does NOT guarentee that the class can be
     * instantiated.  Go figure.
     * The class can be instantiated when the class is linked/resolved,
     * and all dependencies are resolved.
     * Various JDKs do this at different times, so beware:
     * java.lang.NoClassDefFoundError when
     * calling Class.getDeclaredMethod() (JDK14),
     * java.lang.reflect.InvocationTargetException
     * (wrapping java.lang.NoClassDefFoundError) when calling
     * java.lang.newInstance (JDK13),
     * and who knows what else..
     *
     * @return value of resourceClass.
     */
    public Class loadClass() {
        if (resourceClass == null  &&  getClassLoader() != null) {
            if (log.isLoggable(Level.FINE))
                log.fine("loadClass: Loading class '" + getName() + "' with " + getClassLoader());

            resourceClass = (Class)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        try {
                            return getClassLoader().loadClass(getName());
                        } catch (ClassNotFoundException e) {
                            return null;
                        }
                    }
                });
        }
        return resourceClass;
    }
    
    public String toString() {
        return "ResourceClass[" + getName() +  ", " + getResource() + ", " + getClassLoader() + "]";
    }
}
