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

import jakarta.faces.FacesException;

public class CustomFactoryFinderProviderFactoryImpl extends FactoryFinderProviderFactory
{

    @Override
    public FactoryFinderProvider getFactoryFinderProvider()
    {
        return new FactoryFinderProvider()
        {
            public void setFactory(String factoryName, String implName)
            {
                int j = 10;
            }
            
            public void releaseFactories() throws FacesException
            {
                int j = 10;
            }
            
            public Object getFactory(String factoryName) throws FacesException
            {
                return 1;
            }
        };
    }

}
