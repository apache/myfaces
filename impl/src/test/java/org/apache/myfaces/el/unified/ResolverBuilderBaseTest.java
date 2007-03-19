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
import static org.testng.Assert.assertEquals;

import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.convert.PropertyResolverToELResolver;
import org.apache.myfaces.el.convert.VariableResolverToELResolver;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBaseTest
{
    private IMocksControl _mocksControl;
    private RuntimeConfig _runtimeConfig;
    private ResolverBuilderBase _testImpl;
    private CompositeELResolver _compositeELResolver;

    @BeforeMethod
    void setUp()
    {
        _mocksControl = EasyMock.createNiceControl();
        _runtimeConfig = _mocksControl.createMock(RuntimeConfig.class);
        _compositeELResolver = _mocksControl.createMock(CompositeELResolver.class);
        _testImpl = new ResolverBuilderBase(_runtimeConfig);
    }

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
    }

    @Test
    public void testGetApplicationElResolvers() throws Exception
    {
        ELResolver resolver = _mocksControl.createMock(ELResolver.class);
        expect(_runtimeConfig.getApplicationElResolvers()).andReturn(resolver).anyTimes();
        _compositeELResolver = _mocksControl.createMock(CompositeELResolver.class);
        _compositeELResolver.add(eq(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }

    @Test
    public void testGetVariableResolver() throws Exception
    {
        VariableResolver resolver = _mocksControl.createMock(VariableResolver.class);
        expect(_runtimeConfig.getVariableResolver()).andReturn(resolver).anyTimes();
        _compositeELResolver.add(isA(VariableResolverToELResolver.class));
        expectLastCall().andAnswer(new VariableResolverToELResolverValidator(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }

    @Test
    public void testGetVariableResolverChainHead() throws Exception
    {
        VariableResolver resolver = _mocksControl.createMock(VariableResolver.class);
        EasyMock.expect(_runtimeConfig.getVariableResolverChainHead()).andReturn(resolver).anyTimes();
        _compositeELResolver.add(isA(VariableResolverToELResolver.class));
        expectLastCall().andAnswer(new VariableResolverToELResolverValidator(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }

    @Test
    public void testGetPropertyResolver() throws Exception
    {
        PropertyResolver resolver = _mocksControl.createMock(PropertyResolver.class);
        expect(_runtimeConfig.getPropertyResolver()).andReturn(resolver).anyTimes();
        _compositeELResolver.add(isA(PropertyResolverToELResolver.class));
        expectLastCall().andAnswer(new PropertyResolverToELResolverValidator(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }

    @Test
    public void testGetPropertyResolverChainHead() throws Exception
    {
        PropertyResolver resolver = _mocksControl.createMock(PropertyResolver.class);
        EasyMock.expect(_runtimeConfig.getPropertyResolverChainHead()).andReturn(resolver).anyTimes();
        _compositeELResolver.add(isA(PropertyResolverToELResolver.class));
        expectLastCall().andAnswer(new PropertyResolverToELResolverValidator(resolver));
        _mocksControl.replay();
        _testImpl.addFromRuntimeConfig(_compositeELResolver);
        _mocksControl.verify();
    }

    private class VariableResolverToELResolverValidator implements IAnswer<Object>
    {
        private final VariableResolver _resolver;

        private VariableResolverToELResolverValidator(VariableResolver resolver)
        {
            _resolver = resolver;
        }

        public Object answer() throws Throwable
        {
            VariableResolverToELResolver vr = (VariableResolverToELResolver) getCurrentArguments()[0];
            assertEquals(_resolver, vr.getVariableResolver());
            return null;
        }
    }

    private class PropertyResolverToELResolverValidator implements IAnswer<Object>
    {
        private final PropertyResolver _resolver;

        private PropertyResolverToELResolverValidator(PropertyResolver resolver)
        {
            _resolver = resolver;
        }

        public Object answer() throws Throwable
        {
            PropertyResolverToELResolver vr = (PropertyResolverToELResolver) getCurrentArguments()[0];
            assertEquals(_resolver, vr.getPropertyResolver());
            return null;
        }
    }
}
