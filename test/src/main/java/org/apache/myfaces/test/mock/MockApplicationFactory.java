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

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

/**
 * <p>Mock implementation of <code>ApplicationFactory</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockApplicationFactory extends ApplicationFactory
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a default instance.</p>
     */
    public MockApplicationFactory()
    {

    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The <code>Application</code> instance to be returned by
     * this factory.</p>
     */
    private Application application = null;

    // --------------------------------------------- AppolicationFactory Methods

    /** {@inheritDoc} */
    public Application getApplication()
    {

        if (this.application == null)
        {
            Class clazz = null;

            try
            {
                clazz = this.getClass().getClassLoader().loadClass(
                        "org.apache.myfaces.test.mock.MockApplication22");
                if (clazz == null)
                {
                    clazz = this.getClass().getClassLoader().loadClass(
                            "org.apache.myfaces.test.mock.MockApplication20");
                }
                this.application = (MockApplication) clazz.newInstance();
            }
            catch (NoClassDefFoundError e)
            {
                clazz = null; // We are not running in a JSF 1.2 environment
            }
            catch (ClassNotFoundException e)
            {
                clazz = null; // Same as above
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new FacesException(e);
            }

            if (clazz == null)
            {
                try
                {
                    clazz = this.getClass().getClassLoader().loadClass(
                            "org.apache.myfaces.test.mock.MockApplication12");
                    this.application = (MockApplication) clazz.newInstance();
                }
                catch (NoClassDefFoundError e)
                {
                    clazz = null; // We are not running in a JSF 1.2 environment
                }
                catch (ClassNotFoundException e)
                {
                    clazz = null; // Same as above
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
            if (clazz == null)
            {
                try
                {
                    clazz = this.getClass().getClassLoader().loadClass(
                            "org.apache.myfaces.test.mock.MockApplication");
                    this.application = (MockApplication) clazz.newInstance();
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
        return this.application;

    }

    /** {@inheritDoc} */
    public void setApplication(Application application)
    {

        this.application = application;

    }

}
