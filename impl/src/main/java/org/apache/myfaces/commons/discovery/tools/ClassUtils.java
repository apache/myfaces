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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.myfaces.commons.discovery.DiscoveryException;


/**
 * @author Richard A. Sitze
 */
public class ClassUtils {
    private static Logger log = Logger.getLogger(ClassUtils.class.getName());
    public static void setLog(Logger _log) {
        log = _log;
    }

    /**
     * Get package name.
     * Not all class loaders 'keep' package information,
     * in which case Class.getPackage() returns null.
     * This means that calling Class.getPackage().getName()
     * is unreliable at best.
     */
    public static String getPackageName(Class clazz) {
        Package clazzPackage = clazz.getPackage();
        String packageName;
        if (clazzPackage != null) {
            packageName = clazzPackage.getName();
        } else {
            String clazzName = clazz.getName();
            packageName = clazzName.substring(0, clazzName.lastIndexOf('.'));
        }
        return packageName;
    }
    
    /**
     * @return Method 'public static returnType methodName(paramTypes)',
     *         if found to be <strong>directly</strong> implemented by clazz.
     */
    public static Method findPublicStaticMethod(Class clazz,
                                                Class returnType,
                                                String methodName,
                                                Class[] paramTypes) {
        boolean problem = false;
        Method method = null;

        // verify '<methodName>(<paramTypes>)' is directly in class.
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch(NoSuchMethodException e) {
            problem = true;
            log.log(Level.FINE, "Class " + clazz.getName() + ": missing method '" + methodName + "(...)", e);
        }
        
        // verify 'public static <returnType>'
        if (!problem  &&
            !(Modifier.isPublic(method.getModifiers()) &&
              Modifier.isStatic(method.getModifiers()) &&
              method.getReturnType() == returnType)) {
            if (log.isLoggable(Level.FINE)) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    log.fine(methodName + "() is not public");
                }
                if (!Modifier.isStatic(method.getModifiers())) {
                    log.fine(methodName + "() is not static");
                }
                if (method.getReturnType() != returnType) {
                    log.fine("Method returns: " + method.getReturnType().getName() + "@@" + method.getReturnType().getClassLoader());
                    log.fine("Should return:  " + returnType.getName() + "@@" + returnType.getClassLoader());
                }
            }
            problem = true;
            method = null;
        }
        
        return method;
    }

    /**
     * Instantiate a new 
     */    
    public static Object newInstance(Class impl, Class paramClasses[], Object params[])
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        if (paramClasses == null || params == null) {
            return impl.newInstance();
        } else {
            Constructor constructor = impl.getConstructor(paramClasses);
            return constructor.newInstance(params);
        }
    }
    
    /**
     * Throws exception if <code>impl</code> does not
     * implement or extend the SPI.
     */
    public static void verifyAncestory(Class spi, Class impl)
        throws DiscoveryException
    {
        if (spi == null) {
            throw new DiscoveryException("No interface defined!");
        }

        if (impl == null) {
            throw new DiscoveryException("No implementation defined for " + spi.getName());
        }

        if (!spi.isAssignableFrom(impl)) {
            throw new DiscoveryException("Class " + impl.getName() +
                                         " does not implement " + spi.getName());
        }
    }
}
