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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.FactoryFinder;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupServletContextListener implements ServletContextListener
{
    static final String FACES_INIT_DONE = StartupServletContextListener.class.getName() + ".FACES_INIT_DONE";

    private static final Log log = LogFactory.getLog(DefaultFacesInitializer.class);

    private FacesInitializer _facesInitializer;
    private ServletContext _servletContext;

    public void contextInitialized(ServletContextEvent event)
    {
        if (_servletContext != null)
        {
            throw new IllegalStateException("context is already initialized");
        }
        _servletContext = event.getServletContext();
        Boolean b = (Boolean) _servletContext.getAttribute(FACES_INIT_DONE);

        if (b == null || b.booleanValue() == false)
        {
            getFacesInitializer().initFaces(_servletContext);
            _servletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);
        }
        else
        {
            log.info("MyFaces already initialized");
        }
    }

    protected FacesInitializer getFacesInitializer()
    {
        if (_facesInitializer == null)
        {
            _facesInitializer = new DefaultFacesInitializer();
        }
        return _facesInitializer;
    }

    /**
     * configure the faces initializer
     * 
     * @param facesInitializer
     */
    public void setFacesInitializer(FacesInitializer facesInitializer)
    {
        if (_facesInitializer != null && _facesInitializer != facesInitializer && _servletContext != null)
        {
            _facesInitializer.destroyFaces(_servletContext);
        }
        _facesInitializer = facesInitializer;
        if (_servletContext != null)
        {
            facesInitializer.initFaces(_servletContext);
        }
    }

    public void contextDestroyed(ServletContextEvent e)
    {
        if (_facesInitializer != null && _servletContext != null)
        {
            _facesInitializer.destroyFaces(_servletContext);
        }
        FactoryFinder.releaseFactories();
        _servletContext = null;
    }
}
