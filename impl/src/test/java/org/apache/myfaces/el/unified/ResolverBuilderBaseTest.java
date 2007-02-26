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

import org.apache.myfaces.config.RuntimeConfig;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import org.apache.shale.test.mock.MockFacesContext12;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class ResolverBuilderBaseTest extends TestCase
{
    private ResolverBuilderBase builder;
    private ELContext expectedContext;
    private String expectedProperty;
    private MockFacesContext12 expectedFacesContext;
    private NoOpElResolver configResolver;
    private VariableResolver varResolver;
    private PropertyResolver propResolver;
    private ELResolver appResolver;
    private ArrayList<String> calledResolvers;
    private RuntimeConfig runtimeConfig;

    protected void setUp(final Object expectedBase)
    {
        runtimeConfig = new RuntimeConfig();
        builder = new ResolverBuilderBase(runtimeConfig);

        expectedProperty = "xxx";
        expectedFacesContext = new MockFacesContext12();
        expectedContext = new FacesELContext(null, expectedFacesContext);
        expectedFacesContext.setELContext(expectedContext);

        calledResolvers = new ArrayList<String>();
        configResolver = new NoOpElResolver()
        {
            @Override
            public Object getValue(ELContext context, Object base, Object property)
            {
                assertSame(expectedContext, context);
                assertSame(expectedBase, base);
                assertSame(expectedProperty, property);
                calledResolvers.add("config");
                return null;
            }
        };
        varResolver = new VariableResolver()
        {
            @Override
            public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException
            {
                assertSame(expectedFacesContext, facesContext);
                assertSame(expectedProperty, name);
                calledResolvers.add("variable");
                return null;
            }

        };
        propResolver = new NoOpPropertyResolver()
        {
            @Override
            public Object getValue(Object base, Object property) throws EvaluationException, PropertyNotFoundException
            {
                assertSame(expectedBase, base);
                assertSame(expectedProperty, property);
                calledResolvers.add("property");
                return super.getValue(base, property);
            }
        };
        appResolver = new NoOpElResolver()
        {
            @Override
            public Object getValue(ELContext context, Object base, Object property)
            {
                assertSame(expectedContext, context);
                assertSame(expectedBase, base);
                assertSame(expectedProperty, property);
                calledResolvers.add("app");
                return super.getValue(context, base, property);
            }
        };
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.unified.ResolverBuilderBase#addFromRuntimeConfig(CompositeELResolver)}.
     * 
     * @throws
     */
    public void testCreateCompositeElResolverWithNoResolver()
    {
        setUp(null);
        CompositeELResolver resolver = new CompositeELResolver();
        builder.addFromRuntimeConfig(resolver);
        Object value = resolver.getValue(expectedContext, null, expectedProperty);
        assertNull(value);
        assertEquals(0, calledResolvers.size());
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.unified.ResolverBuilderBase#addFromRuntimeConfig(CompositeELResolver)}.
     */
    public void testCreateCompositeElResolverWithBase()
    {
        Object expectedBase = "base";
        setUp(expectedBase);
        runtimeConfig.addFacesConfigElResolver(configResolver);
        runtimeConfig.setVariableResolver(varResolver);
        runtimeConfig.setPropertyResolver(propResolver);
        runtimeConfig.addApplicationElResolver(appResolver);

        CompositeELResolver resolver = new CompositeELResolver();
        builder.addFromRuntimeConfig(resolver);

        Object value = resolver.getValue(expectedContext, expectedBase, expectedProperty);
        assertNull(value);
        assertTrue(expectedContext.isPropertyResolved());
        assertEquals(2, calledResolvers.size());
        assertEquals("config", calledResolvers.get(0));
        assertEquals("property", calledResolvers.get(1));

        runtimeConfig.setPropertyResolver(null);
        resolver = new CompositeELResolver();
        calledResolvers.clear();
        builder.addFromRuntimeConfig(resolver);

        value = resolver.getValue(expectedContext, expectedBase, expectedProperty);
        assertNull(value);
        assertFalse(expectedContext.isPropertyResolved());
        assertEquals(2, calledResolvers.size());
        assertEquals("config", calledResolvers.get(0));
        assertEquals("app", calledResolvers.get(1));
    }

    /**
     * Test method for
     * {@link org.apache.myfaces.el.unified.ResolverBuilderBase#addFromRuntimeConfig(CompositeELResolver)}.
     */
    public void testCreateCompositeElResolverWithNullBase()
    {
        Object expectedBase = null;
        setUp(expectedBase);
        runtimeConfig.addFacesConfigElResolver(configResolver);
        runtimeConfig.setVariableResolver(varResolver);
        runtimeConfig.setPropertyResolver(propResolver);
        runtimeConfig.addApplicationElResolver(appResolver);

        CompositeELResolver resolver = new CompositeELResolver();
        builder.addFromRuntimeConfig(resolver);

        Object value = resolver.getValue(expectedContext, expectedBase, expectedProperty);
        assertNull(value);
        assertFalse(expectedContext.isPropertyResolved());
        assertEquals(3, calledResolvers.size());
        assertEquals("config", calledResolvers.get(0));
        assertEquals("variable", calledResolvers.get(1));
        assertEquals("app", calledResolvers.get(2));

        runtimeConfig.setVariableResolver(null);
        resolver = new CompositeELResolver();
        calledResolvers.clear();
        builder.addFromRuntimeConfig(resolver);

        value = resolver.getValue(expectedContext, expectedBase, expectedProperty);
        assertNull(value);
        assertFalse(expectedContext.isPropertyResolved());
        assertEquals(2, calledResolvers.size());
        assertEquals("config", calledResolvers.get(0));
        assertEquals("app", calledResolvers.get(1));
    }
}
