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

import static org.easymock.EasyMock.*;

import javax.el.ELResolver;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBaseTest extends TestCase
{
    private IMocksControl _mocksControl;
    private RuntimeConfig _runtimeConfig;
    private ResolverBuilderBase _testImpl;
    private List<ELResolver> _resolvers;

    public ResolverBuilderBaseTest()
    {
    }

    public void setUp()
    {
        _mocksControl = EasyMock.createNiceControl();
        _runtimeConfig = _mocksControl.createMock(RuntimeConfig.class);
        _testImpl = new ResolverBuilderBase(_runtimeConfig);
        _resolvers = new ArrayList<ELResolver>();
    }

    public void tearDown()
    {
        _mocksControl = null;
        _runtimeConfig = null;
        _testImpl = null;
        _resolvers = null;
    }

    /*
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

    public void testGetApplicationElResolvers() throws Exception
    {
        ELResolver resolver = _mocksControl.createMock(ELResolver.class);
        expect(_runtimeConfig.getApplicationElResolvers()).andReturn(Arrays.asList(resolver)).anyTimes();
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_resolvers);
        _mocksControl.verify();
        Assert.assertEquals(Arrays.asList(resolver), _resolvers);
    }

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

