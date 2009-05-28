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

import static org.apache.myfaces.Assert.assertException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.myfaces.TestRunner;

import javax.el.ValueExpression;

import junit.framework.TestCase;

/**
 * Tests for {@link Application}
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ApplicationTest extends TestCase
{
    private Application app;

    @Override
    protected void setUp() throws Exception
    {
        app = (Application) Enhancer.create(Application.class, NoOp.INSTANCE);
    }

    /**
     * Test method for {@link javax.faces.application.Application#addELResolver(javax.el.ELResolver)}.
     */
    /*
    public void testAddELResolver()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.addELResolver(null);
            }
        });
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getELResolver()}.
     */
    /*
    public void testGetELResolver()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.getELResolver();
            }
        });
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#getResourceBundle(javax.faces.context.FacesContext, java.lang.String)}.
     */
    /*
    public void testGetResourceBundle()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.getResourceBundle(null, null);
            }
        });
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, java.lang.String)}.
     */
    /*
    public void testCreateComponentValueExpressionFacesContextString()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.createComponent((ValueExpression) null, null, null);
            }
        });
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getExpressionFactory()}.
     */
    /*
    public void testGetExpressionFactory()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.getExpressionFactory();
            }
        });
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#addELContextListener(javax.el.ELContextListener)}.
     */
    /*
    public void testAddELContextListener()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.addELContextListener(null);
            }
        });
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#removeELContextListener(javax.el.ELContextListener)}.
     */
    /*
    public void testRemoveELContextListener()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.removeELContextListener(null);
            }
        });
    }*/

    /**
     * Test method for {@link javax.faces.application.Application#getELContextListeners()}.
     */
    /*
    public void testGetELContextListeners()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.getELContextListeners();
            }
        });
    }*/

    /**
     * Test method for
     * {@link javax.faces.application.Application#evaluateExpressionGet(javax.faces.context.FacesContext, java.lang.String, java.lang.Class)}.
     */
    /*
    public void testEvaluateExpressionGet()
    {
        assertException(UnsupportedOperationException.class, new TestRunner()
        {
            public void run()
            {
                app.evaluateExpressionGet(null, null, null);
            }
        });
    }*/
}
