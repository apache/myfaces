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
package org.apache.myfaces.el.unified;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.el.ELResolver;
import jakarta.faces.el.PropertyResolver;
import jakarta.faces.el.VariableResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBaseTest extends AbstractJsfTestCase
{
    private IMocksControl _mocksControl;
    private RuntimeConfig _runtimeConfig;
    private ResolverBuilderBase _testImpl;
    private List<ELResolver> _resolvers;
    
    public ResolverBuilderBaseTest()
    {
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        _mocksControl = EasyMock.createNiceControl();
        _runtimeConfig = _mocksControl.createMock(RuntimeConfig.class);
        _testImpl = new ResolverBuilderBase(_runtimeConfig);
        _resolvers = new ArrayList<ELResolver>();
    }
    
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        _mocksControl = null;
        _runtimeConfig = null;
        _testImpl = null;
        _resolvers = null;
    }

    /*
    @Test
    public void testGetFacesConfigElResolvers() throws Exception
    {
        ELResolver resolver = _mocksControl.createMock(ELResolver.class);
        expect(_runtimeConfig.getFacesConfigElResolvers()).andReturn(resolver).anyTimes();
        _compositeELResolver = _mocksControl.createMock(CompositeELResolver.class);
        _compositeELResolver.add(eq(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }*/

    @Test
    public void testGetApplicationElResolvers() throws Exception
    {
        ELResolver resolver = _mocksControl.createMock(ELResolver.class);
        expect(_runtimeConfig.getApplicationElResolvers()).andReturn(Arrays.asList(resolver)).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        Assert.assertEquals(Arrays.asList(resolver), _resolvers);
    }

    @Test
    public void testGetVariableResolver() throws Exception
    {
        VariableResolver resolver = _mocksControl.createMock(VariableResolver.class);
        expect(_runtimeConfig.getVariableResolver()).andReturn(resolver).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        
        VariableResolverToELResolver elResolver
                = (VariableResolverToELResolver) _resolvers.get(0);
        Assert.assertEquals(resolver, elResolver.getVariableResolver());
    }

    @Test
    public void testGetVariableResolverChainHead() throws Exception
    {
        VariableResolver resolver = _mocksControl.createMock(VariableResolver.class);
        EasyMock.expect(_runtimeConfig.getVariableResolverChainHead()).andReturn(resolver).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        
        VariableResolverToELResolver elResolver
                = (VariableResolverToELResolver) _resolvers.get(0);
        Assert.assertEquals(resolver, elResolver.getVariableResolver());
    }

    @Test
    public void testGetPropertyResolver() throws Exception
    {
        PropertyResolver resolver = _mocksControl.createMock(PropertyResolver.class);
        expect(_runtimeConfig.getPropertyResolver()).andReturn(resolver).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        
        PropertyResolverToELResolver elResolver
                = (PropertyResolverToELResolver) _resolvers.get(0);
        Assert.assertEquals(resolver, elResolver.getPropertyResolver());
    }

    @Test
    public void testGetPropertyResolverChainHead() throws Exception
    {
        PropertyResolver resolver = _mocksControl.createMock(PropertyResolver.class);
        EasyMock.expect(_runtimeConfig.getPropertyResolverChainHead()).andReturn(resolver).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        

        PropertyResolverToELResolver elResolver
                = (PropertyResolverToELResolver) _resolvers.get(0);
        Assert.assertEquals(resolver, elResolver.getPropertyResolver());
    }

}
