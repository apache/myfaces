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
package org.apache.myfaces.spi;

import java.util.List;

/**
 * This class provides an interface to override SPI handling done by
 * MyFaces.
 * 
 * This is useful on environments like in OSGi, because it allows to
 * put custom code to find SPI interfaces under META-INF/services/
 * 
 * @author Leonardo Uribe
 * @since 2.0.3
 *
 */
public abstract class ServiceProviderFinder
{
    
    /**
     * Gets the list of classes bound to the spiClass key, looking
     * for entries under META-INF/services/[spiClass]
     * 
     * @param spiClass
     * @return
     */
    public abstract List<String> getServiceProviderList(String spiClass);
    
}
