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

import java.lang.reflect.InvocationTargetException;

import org.apache.myfaces.commons.discovery.DiscoveryException;


/**
 * Represents a Service Programming Interface (spi).
 * - SPI's name
 * - SPI's (provider) class
 * - SPI's (alternate) override property name
 * 
 * In addition, while there are many cases where this is NOT
 * usefull, for those in which it is:
 * 
 * - expected constructor argument types and parameters values.
 * 
 * @author Richard A. Sitze
 */
public class SPInterface {
    /**
     * The service programming interface: intended to be
     * an interface or abstract class, but not limited
     * to those two.
     */        
    private final Class spi;
    
    /**
     * The property name to be used for finding the name of
     * the SPI implementation class.
     */
    private final String propertyName;
    
    
    private Class  paramClasses[] = null;
    private Object params[] = null;


    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     */
    public SPInterface(Class provider) {
        this(provider, provider.getName());
    }
    
    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param spi The SPI class
     * 
     * @param propertyName when looking for the name of a class implementing
     *        the provider class, a discovery strategy may involve looking for
     *        (system or other) properties having either the name of the class
     *        (provider) or the <code>propertyName</code>.
     */
    public SPInterface(Class spi, String propertyName) {
        this.spi = spi;
        this.propertyName = propertyName;
    }

    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param provider The SPI class
     * 
     * @param constructorParamClasses classes representing the
     *        constructor argument types.
     * 
     * @param constructorParams objects representing the
     *        constructor arguments.
     */
    public SPInterface(Class provider,
                       Class constructorParamClasses[],
                       Object constructorParams[])
    {
        this(provider,
             provider.getName(),
             constructorParamClasses,
             constructorParams);
    }
    
    /**
     * Construct object representing Class <code>provider</code>.
     * 
     * @param spi The SPI class
     * 
     * @param propertyName when looking for the name of a class implementing
     *        the provider class, a discovery strategy may involve looking for
     *        (system or other) properties having either the name of the class
     *        (provider) or the <code>propertyName</code>.
     * 
     * @param constructorParamClasses classes representing the
     *        constructor argument types.
     * 
     * @param constructorParams objects representing the
     *        constructor arguments.
     */
    public SPInterface(Class spi,
                       String propertyName,
                       Class constructorParamClasses[],
                       Object constructorParams[])
    {
        this.spi = spi;
        this.propertyName = propertyName;
        this.paramClasses = constructorParamClasses;
        this.params = constructorParams;
    }

    public String getSPName() {
        return spi.getName();
    }

    public Class getSPClass() {
        return spi;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Instantiate a new 
     */    
    public Object newInstance(Class impl)
        throws DiscoveryException,
               InstantiationException,
               IllegalAccessException,
               NoSuchMethodException,
               InvocationTargetException
    {
        verifyAncestory(impl);
        
        return ClassUtils.newInstance(impl, paramClasses, params);
    }
    
    public void verifyAncestory(Class impl) {
        ClassUtils.verifyAncestory(spi, impl);
    }
}
