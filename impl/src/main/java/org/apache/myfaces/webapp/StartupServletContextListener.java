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
package org.apache.myfaces.webapp;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Initialise the MyFaces system.
 * <p>
 * This context listener is registered by the JSP TLD file for the standard Faces "f" components. Normally, servlet
 * containers will automatically load and process .tld files at startup time, and therefore register and run this class
 * automatically.
 * </p><p>
 * Some very old servlet containers do not do this correctly, so in those cases this listener may be registered manually
 * in web.xml. Registering it twice (ie in both .tld and web.xml) will result in a harmless warning message being
 * generated. Very old versions of MyFaces Core do not register the listener in the .tld file, so those also need a
 * manual entry in web.xml. However all versions since at least 1.1.2 have this entry in the tld.
 * </p><p>
 * </p>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupServletContextListener implements ServletContextListener
{
    private FacesInitializer facesInitializer;

    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        if (facesInitializer != null)
        {
            return; // Context is already initialized
        }

        facesInitializer = FacesInitializerFactory.getFacesInitializer(event.getServletContext());
        facesInitializer.initFaces(event.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event)
    {
        if (facesInitializer != null)
        {
            facesInitializer.destroyFaces(event.getServletContext());
        }
    }
}
