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
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
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
        try
        {
            Class clazz = MockFacesContext.class;
            constructor = clazz.getConstructor(facesContextSignature);
        }
        catch (NoSuchMethodException | SecurityException ex)
        {
            Logger.getLogger(MockFacesContextFactory.class.getName()).log(Level.SEVERE, null, ex);
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

    // --------------------------------------------- FacesContextFactory Methods

    /** {@inheritDoc} */
    public FacesContext getFacesContext(Object context, Object request,
            Object response, Lifecycle lifecycle) throws FacesException
    {

        // Select the appropriate MockExternalContext implementation class
        Class clazz = MockExternalContext.class;

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
