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

import java.io.InputStream;
import java.net.URL;


/**
 * JDK 1.1.x compatible?
 * There is no direct way to get the system class loader
 * in 1.1.x, but this should be a good work around...
 */
class PsuedoSystemClassLoader extends ClassLoader {
    protected Class loadClass(String className, boolean resolve)
        throws ClassNotFoundException
    {
        return findSystemClass(className);
    }
    
    public URL getResource(String resName) {
        return getSystemResource(resName);
    }
    
    public InputStream getResourceAsStream(String resName) {
        return getSystemResourceAsStream(resName);
    }
}
