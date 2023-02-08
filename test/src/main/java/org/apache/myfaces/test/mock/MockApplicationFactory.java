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

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;

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

    @Override
    public Application getApplication()
    {

        if (this.application == null)
        {
            Class clazz = MockApplication.class;
            try
            {
                this.application = (Application) clazz.newInstance();
            }
            catch (InstantiationException | IllegalAccessException ex)
            {
                Logger.getLogger(MockApplicationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.application;

    }

    @Override
    public void setApplication(Application application)
    {
        this.application = application;
    }

}
