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

package org.apache.myfaces.test.mock;

import java.lang.reflect.Constructor;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Mock implementation of <code>FacesContextFactory</code>.</p>
 *
 * $Id: MockFacesContextFactory.java 990408 2010-08-28 18:59:21Z lu4242 $
 * @since 1.0.0
 */

public class MockFacesContextFactory extends FacesContextFactory
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Look up the constructor we will use for creating <code>MockFacesContext</code>
     * instances.</p>
     */
    public MockFacesContextFactory()
    {

        Class clazz = null;

        // Try to load the 2.2 version of our mock FacesContext class
        try
        {
            clazz = this.getClass().getClassLoader().loadClass(
                    "org.apache.myfaces.test.mock.MockFacesContext22");
            constructor = clazz.getConstructor(facesContextSignature);
            jsf22 = true;
        }
        catch (NoClassDefFoundError e)
        {
            // We are not running on JSF 2.0, so go to our fallback
            clazz = null;
            constructor = null;
        }
        catch (ClassNotFoundException e)
        {
            // Same as above
            clazz = null;
            constructor = null;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
        
        // Try to load the 2.0 version of our mock FacesContext class
        try
        {
            if (clazz == null)
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockFacesContext20");
                constructor = clazz.getConstructor(facesContextSignature);
                jsf20 = true;
            }
        }
        catch (NoClassDefFoundError e)
        {
            // We are not running on JSF 2.0, so go to our fallback
            clazz = null;
            constructor = null;
        }
        catch (ClassNotFoundException e)
        {
            // Same as above
            clazz = null;
            constructor = null;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        // Try to load the 1.2 version of our mock FacesContext class
        try
        {
            if (clazz == null)
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockFacesContext12");
                constructor = clazz.getConstructor(facesContextSignature);
                jsf12 = true;
            }
        }
        catch (NoClassDefFoundError e)
        {
            // We are not running on JSF 1.2, so go to our fallback
            clazz = null;
            constructor = null;
        }
        catch (ClassNotFoundException e)
        {
            // Same as above
            clazz = null;
            constructor = null;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        // Fall back to the 1.1 version if we could not load the 1.2 version
        try
        {
            if (clazz == null)
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockFacesContext");
                constructor = clazz.getConstructor(facesContextSignature);
                jsf12 = false;
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The constructor for creating a <code>FacesContext</code> instance,
     * taking an <code>ExternalContext</code> and <code>Lifecycle</code>.</p>
     */
    private Constructor constructor = null;

    /**
     * <p>The parameter signature of the ExternalContext constructor we wish to call.</p>
     */
    private static Class[] externalContextSignature = new Class[] {
            ServletContext.class, HttpServletRequest.class,
            HttpServletResponse.class };

    /**
     * <p>The parameter signature of the FacesContext constructor we wish to call.</p>
     */
    private static Class[] facesContextSignature = new Class[] {
            ExternalContext.class, Lifecycle.class };

    /**
     * <p>Flag indicating that we are running in a JSF 1.2 environment.</p>
     */
    private boolean jsf12 = false;

    /**
     * <p>Flag indicating that we are running in a JSF 2.0 environment.</p>
     */
    private boolean jsf20 = false;
    
    /**
     * <p>Flag indicating that we are running in a JSF 2.2 environment.</p>
     */
    private boolean jsf22 = false;

    // --------------------------------------------- FacesContextFactory Methods

    /** {@inheritDoc} */
    public FacesContext getFacesContext(Object context, Object request,
            Object response, Lifecycle lifecycle) throws FacesException
    {

        // Select the appropriate MockExternalContext implementation class
        Class clazz = MockExternalContext.class;

        if (jsf22)
        {
            try
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockExternalContext22");
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
        }
        
        if (jsf20)
        {
            try
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockExternalContext20");
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
        }

        if (jsf12)
        {
            try
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockExternalContext12");
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }
        }

        // Select the constructor we wish to call
        Constructor mecConstructor = null;
        try
        {
            mecConstructor = clazz.getConstructor(externalContextSignature);
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        // Construct an appropriate MockExternalContext instance
        MockExternalContext externalContext = null;
        try
        {
            externalContext = (MockExternalContext) mecConstructor
                    .newInstance(new Object[] { context, request, response });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

        // Construct an appropriate MockFacesContext instance and return it
        try
        {
            return (MockFacesContext) constructor.newInstance(new Object[] {
                    externalContext, lifecycle });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }

    }

}
