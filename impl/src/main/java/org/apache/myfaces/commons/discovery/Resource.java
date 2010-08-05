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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;


/**
 * 'Resource' located by discovery.
 * Naming of methods becomes a real pain ('getClass()')
 * so I've patterned this after ClassLoader...
 * 
 * I think it works well as it will give users a point-of-reference.
 * 
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 */
public class Resource
{
    protected final String      name;
    protected final URL         resource;
    protected final ClassLoader loader;

    public Resource(String resourceName, URL resource, ClassLoader loader) {
        this.name = resourceName;
        this.resource = resource;
        this.loader = loader;
    }

    /**
     * Get the value of resourceName.
     * @return value of resourceName.
     */
    public String getName() {
        return name;
    }

//    /**
//     * Set the value of URL.
//     * @param v  Value to assign to URL.
//     */
//    public void setResource(URL  resource) {
//        this.resource = resource;
//    }
    
    /**
     * Get the value of URL.
     * @return value of URL.
     */
    public URL getResource() {
        return resource;
    }
    
    /**
     * Get the value of URL.
     * @return value of URL.
     */
    public InputStream getResourceAsStream() {
        try {
            return resource.openStream();
        } catch (IOException e) {
            return null;  // ignore
        }
    }
    
    /**
     * Get the value of loader.
     * @return value of loader.
     */
    public ClassLoader getClassLoader() {
        return loader ;
    }
    
//    /**
//     * Set the value of loader.
//     * @param v  Value to assign to loader.
//     */
//    public void setClassLoader(ClassLoader  loader) {
//        this.loader = loader;
//    }
    
    public String toString() {
        return "Resource[" + getName() +  ", " + getResource() + ", " + getClassLoader() + "]";
    }
    
    public static Resource[] toArray(ResourceIterator iterator) {
        Vector vector = new Vector();
        while (iterator.hasNext()) {
            vector.add(iterator.nextResource());
        }
        Resource[] resources = new Resource[vector.size()];
        vector.copyInto(resources);
        
        return resources;
    }
}
