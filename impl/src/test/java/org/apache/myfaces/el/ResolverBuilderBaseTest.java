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
package org.apache.myfaces.el;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.el.ELResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ResolverBuilderBaseTest extends AbstractJsfTestCase
{
    private RuntimeConfig runtimeConfig;
    private MyfacesConfig myfacesConfig;
    private ELResolverBuilder resolverBuilder;
    private List<ELResolver> resolvers;

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        runtimeConfig = Mockito.mock(RuntimeConfig.class);
        myfacesConfig = Mockito.mock(MyfacesConfig.class);
        resolverBuilder = new ELResolverBuilder(runtimeConfig, myfacesConfig);
        resolvers = new ArrayList<ELResolver>();
    }

    @Test
    public void testGetFacesConfigElResolvers() throws Exception
    {
        ELResolver resolver = Mockito.mock(ELResolver.class);
        Mockito.when(runtimeConfig.getFacesConfigElResolvers()).thenReturn(Arrays.asList(resolver));

        resolverBuilder.addFromRuntimeConfig(resolvers);

        Assertions.assertEquals(Arrays.asList(resolver), resolvers);
    }
    
    @Test
    public void testGetApplicationElResolvers() throws Exception
    {
        ELResolver resolver = Mockito.mock(ELResolver.class);
        Mockito.when(runtimeConfig.getApplicationElResolvers()).thenReturn(Arrays.asList(resolver));
 
        resolverBuilder.addFromRuntimeConfig(resolvers);

        Assertions.assertEquals(Arrays.asList(resolver), resolvers);
    }
}

