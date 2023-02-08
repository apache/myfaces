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

import jakarta.faces.FactoryFinder;

import  org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FactoryFinderProviderTest
{

    public FactoryFinderProviderTest()
    {
        super();
    }

    @BeforeEach
    public void setUp() throws Exception
    {
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        
    }
    
    @Test
    public void testGetFactory() throws Exception
    {
        FactoryFinderProviderFactory ffp = new CustomFactoryFinderProviderFactoryImpl();
     
        FactoryFinderProviderFactory.setInstance(ffp);
        
        Object uno = FactoryFinder.getFactory("anything");
        
        Assertions.assertEquals(1, uno);
        
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, "sfdsfdsf");
        
        FactoryFinder.releaseFactories();
        
        
    }
    
}
