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
package javax.faces.application;


import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;

/**
 * Tests for {@link Application}
 */
public class ApplicationTest extends AbstractJsfTestCase
{
    public ApplicationTest()
    {
    }

    private Application app;

    public void setUp() throws Exception
    {
        super.setUp();
        app = new MockApplication();
    }
    
    public void tearDown() throws Exception
    {
        app = null;
        super.tearDown();
    }

    /**
     * Test method for {@link javax.faces.application.Application#addELResolver(javax.el.ELResolver)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testAddELResolver()
    {
        app.addELResolver(null);
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getELResolver()}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testGetELResolver()
    {
        app.getELResolver();
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#getResourceBundle(javax.faces.context.FacesContext, java.lang.String)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testGetResourceBundle()
    {
        app.getResourceBundle(null, null);
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, java.lang.String)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testCreateComponentValueExpressionFacesContextString()
    {
        app.createComponent((ValueExpression) null, null, null);
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getExpressionFactory()}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testGetExpressionFactory()
    {
        app.getExpressionFactory();
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#addELContextListener(javax.el.ELContextListener)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testAddELContextListener()
    {
        app.addELContextListener(null);
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#removeELContextListener(javax.el.ELContextListener)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testRemoveELContextListener()
    {
        app.removeELContextListener(null);
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getELContextListeners()}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testGetELContextListeners()
    {
        app.getELContextListeners();
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#evaluateExpressionGet(javax.faces.context.FacesContext, java.lang.String, java.lang.Class)}.
     */
/*
    @Test(expected=UnsupportedOperationException.class)
    public void testEvaluateExpressionGet()
    {
        app.evaluateExpressionGet(null, null, null);
    }*/
}
