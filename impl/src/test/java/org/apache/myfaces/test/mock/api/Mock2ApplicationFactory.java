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

package org.apache.myfaces.test.mock.api;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;

/**
 * Necessary for testing application-factory-setup with to application-factories;
 */
public class Mock2ApplicationFactory extends ApplicationFactory
{
    private Application app;
    //private ApplicationFactory factory;

    public Mock2ApplicationFactory()
    {
    }

    public Mock2ApplicationFactory(ApplicationFactory factory)
    {
        //this.factory = factory;
    }

    @Override
    public Application getApplication()
    {
        return app;
    }

    @Override
    public void setApplication(Application application)
    {
        this.app = application;
    }

}
