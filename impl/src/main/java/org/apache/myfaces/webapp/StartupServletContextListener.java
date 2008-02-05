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
import org.apache.myfaces.config.FacesConfigValidator;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.context.servlet.ServletExternalContextImpl;
import org.apache.myfaces.shared_impl.util.StateUtils;
import org.apache.myfaces.shared_impl.webapp.webxml.WebXml;

import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Iterator;
import java.util.List;

/**
 * Initialise the MyFaces system.
 * <p>
 * This context listener is registered by the JSP TLD file for the standard
 * JSF "f" components. Normally, servlet containers will automatically load
 * and process .tld files at startup time, and therefore register and run
 * this class automatically.
 * <p>
 * Some very old servlet containers do not do this correctly, so in those
 * cases this listener may be registered manually in web.xml. Registering
 * it twice (ie in both .tld and web.xml) will result in a harmless warning
 * message being generated. Very old versions of MyFaces Core do not register
 * the listener in the .tld file, so those also need a manual entry in web.xml.
 * However all versions since at least 1.1.2 have this entry in the tld.
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupServletContextListener
        implements ServletContextListener
{
    private static final Log log = LogFactory.getLog(StartupServletContextListener.class);

    static final String FACES_INIT_DONE
            = StartupServletContextListener.class.getName() + ".FACES_INIT_DONE";

    public void contextInitialized(ServletContextEvent event)
    {
        initFaces(event.getServletContext());
    }

    public static void initFaces(ServletContext servletContext)
    {
        try
        {
            Boolean b = (Boolean)servletContext.getAttribute(FACES_INIT_DONE);

            if (b == null || b.booleanValue() == false)
            {
                log.trace("Initializing MyFaces");

                //Load the configuration
                ExternalContext externalContext = new ServletExternalContextImpl(servletContext, null, null);

                //And configure everything
                new FacesConfigurator(externalContext).configure();

                if ("true".equals(servletContext
                                .getInitParameter(FacesConfigValidator.VALIDATE_CONTEXT_PARAM)) || "true".equals(servletContext
                                .getInitParameter(FacesConfigValidator.VALIDATE_CONTEXT_PARAM.toLowerCase())))
                {
                    List list = FacesConfigValidator.validate(externalContext,
                            servletContext.getRealPath("/"));

                    Iterator iterator = list.iterator();

                    while (iterator.hasNext())
                        log.warn(iterator.next());

                }
                
                // parse web.xml
                WebXml.init(externalContext);

                servletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);
            }
            else
            {
                log.info("MyFaces already initialized");
            }
        }
        catch (Exception ex)
        {
            log.error("Error initializing ServletContext", ex);
            ex.printStackTrace();
        }
        log.info("ServletContext '" + servletContext.getRealPath("/") + "' initialized.");
        
        if(servletContext.getInitParameter(StateUtils.INIT_SECRET) != null
                || servletContext.getInitParameter(StateUtils.INIT_SECRET.toLowerCase()) != null)
            StateUtils.initSecret(servletContext);
    }


    public void contextDestroyed(ServletContextEvent e)
    {
        FactoryFinder.releaseFactories();
    }
}
