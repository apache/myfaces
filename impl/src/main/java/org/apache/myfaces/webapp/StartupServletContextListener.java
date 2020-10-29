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

import org.apache.myfaces.util.lang.ClassUtils;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.config.MyfacesConfig;

/**
 * Initialise the MyFaces system.
 * <p>
 * This context listener is registered by the JSP TLD file for the standard JSF "f" components. Normally, servlet
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
    static final String FACES_INIT_DONE = "org.apache.myfaces.webapp.StartupServletContextListener.FACES_INIT_DONE";

    private static final byte FACES_INIT_PHASE_PREINIT = 0;
    private static final byte FACES_INIT_PHASE_POSTINIT = 1;
    private static final byte FACES_INIT_PHASE_PREDESTROY = 2;
    private static final byte FACES_INIT_PHASE_POSTDESTROY = 3;

    private static final Logger log = Logger.getLogger(StartupServletContextListener.class.getName());

    private FacesInitializer _facesInitializer;
    private ServletContext _servletContext;

    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        if (_servletContext != null)
        {
            throw new IllegalStateException("context is already initialized");
        }
        _servletContext = event.getServletContext();

        Boolean b = (Boolean) _servletContext.getAttribute(FACES_INIT_DONE);
        if (b == null || b == false)
        {
            long start = System.currentTimeMillis();

            if (_facesInitializer == null)
            {
                _facesInitializer = FacesInitializerFactory.getFacesInitializer(_servletContext);
            }

            // Create startup FacesContext before initializing
            FacesContext facesContext = _facesInitializer.initStartupFacesContext(_servletContext);

            dispatchInitializationEvent(event, FACES_INIT_PHASE_PREINIT);
            _facesInitializer.initFaces(_servletContext);
            dispatchInitializationEvent(event, FACES_INIT_PHASE_POSTINIT);
            _servletContext.setAttribute(FACES_INIT_DONE, Boolean.TRUE);

            //Destroy startup FacesContext
            _facesInitializer.destroyStartupFacesContext(facesContext);

            log.log(Level.INFO, "MyFaces Core has started, it took ["
                    + (System.currentTimeMillis() - start)
                    + "] ms.");
        }
        else
        {
            log.finest("MyFaces already initialized");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event)
    {
        if (_facesInitializer != null && _servletContext != null)
        {
            // Create startup FacesContext before start undeploy
            FacesContext facesContext = _facesInitializer.initShutdownFacesContext(_servletContext);

            dispatchInitializationEvent(event, FACES_INIT_PHASE_PREDESTROY);

            _facesInitializer.destroyFaces(_servletContext);

            // Destroy startup FacesContext, but note we do before publish postdestroy event on
            // plugins and before release factories.
            if (facesContext != null)
            {
                _facesInitializer.destroyShutdownFacesContext(facesContext);
            }

            FactoryFinder.releaseFactories();

            //DiscoverSingleton.release(); //clears EnvironmentCache and prevents leaking classloader references
            dispatchInitializationEvent(event, FACES_INIT_PHASE_POSTDESTROY);
        }

        _servletContext = null;
    }

    /**
     * configure the faces initializer
     *
     * @param facesInitializer
     */
    public void setFacesInitializer(FacesInitializer facesInitializer) // TODO who uses this method?
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

    /**
     * Loads the faces init plugins per Service loader.
     * 
     * @return false if there are not plugins defined via ServiceLoader.
     */
    private boolean loadFacesInitPluginsViaServiceLoader()
    {   
        ServiceLoader<StartupListener> loader = ServiceLoader.load(StartupListener.class,
                ClassUtils.getContextClassLoader());

        Iterator<StartupListener> it = (Iterator<StartupListener>) loader.iterator();
        if (!it.hasNext())
        {
            return false;
        }
        
        List<StartupListener> listeners = new LinkedList<>();
        while (it.hasNext())
        {
            listeners.add(it.next());
        }

        _servletContext.setAttribute(MyfacesConfig.FACES_INIT_PLUGINS, listeners);
        
        return true;
    }

    /**
     * loads the faces init plugins per reflection from the context param.
     */
    private void loadFacesInitViaContextParam()
    {
        String plugins = (String) _servletContext.getInitParameter(MyfacesConfig.FACES_INIT_PLUGINS);
        if (plugins == null)
        {
            return;
        }
        log.info("MyFaces Plugins found");
        
        String[] pluginEntries = plugins.split(",");
        List<StartupListener> listeners = new ArrayList<>(pluginEntries.length);
        for (String pluginEntry : pluginEntries)
        {
            try
            {
                Class pluginClass = ClassUtils.getContextClassLoader().loadClass(pluginEntry);
                if (pluginClass == null)
                {
                    pluginClass = this.getClass().getClassLoader().loadClass(pluginEntry);
                }

                listeners.add((StartupListener) pluginClass.newInstance());
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        _servletContext.setAttribute(MyfacesConfig.FACES_INIT_PLUGINS, listeners);

    }

    /**
     * the central initialisation event dispatcher which calls
     * our listeners
     *
     * @param event
     * @param operation
     */
    private void dispatchInitializationEvent(ServletContextEvent event, int operation)
    {
        if (operation == FACES_INIT_PHASE_PREINIT)
        {
            if (!loadFacesInitPluginsViaServiceLoader())
            {
                loadFacesInitViaContextParam();
            }
        }

        List<StartupListener> pluginEntries = (List<StartupListener>)
                _servletContext.getAttribute(MyfacesConfig.FACES_INIT_PLUGINS);
        if (pluginEntries == null)
        {
            return;
        }

        //now we process the plugins
        for (StartupListener initializer : pluginEntries)
        {
            log.info("Processing plugin");

            //for now the initializers have to be stateless to
            //so that we do not have to enforce that the initializer
            //must be serializable
            switch (operation)
            {
                case FACES_INIT_PHASE_PREINIT:
                    initializer.preInit(event);
                    break;
                case FACES_INIT_PHASE_POSTINIT:
                    initializer.postInit(event);
                    break;
                case FACES_INIT_PHASE_PREDESTROY:
                    initializer.preDestroy(event);
                    break;
                default:
                    initializer.postDestroy(event);
                    break;
            }
        }
        log.info("Processing MyFaces plugins done");
    }
}
