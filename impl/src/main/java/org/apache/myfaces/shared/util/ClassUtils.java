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
package org.apache.myfaces.shared.util;

import java.util.Collection;

// TODO: backward compatibility for TomEE
@Deprecated
public class ClassUtils
{
    public static ClassLoader getContextClassLoader()
    {
        return org.apache.myfaces.util.lang.ClassUtils.getContextClassLoader();
    }

    public static <T> T buildApplicationObject(Class<T> interfaceClass, 
            Collection<String> classNamesIterator, T defaultObject)
    {
        return org.apache.myfaces.util.lang.ClassUtils.buildApplicationObject(interfaceClass, classNamesIterator,
                defaultObject);
    }
    
    public static <T> T buildApplicationObject(Class<T> interfaceClass, Class<? extends T> extendedInterfaceClass,
            Class<? extends T> extendedInterfaceWrapperClass,
            Collection<String> classNamesIterator, T defaultObject)
    {
        return org.apache.myfaces.util.lang.ClassUtils.buildApplicationObject(interfaceClass, extendedInterfaceClass,
                extendedInterfaceWrapperClass, classNamesIterator, defaultObject);
    }
}
