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
package org.apache.myfaces.ee6;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.NoneScoped;
import javax.faces.bean.ReferencedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;

/**
 * This class is called by any Java EE 6 complaint container at startup.
 * It checks if the current webapp is a JSF-webapp by checking if some of 
 * the JSF related annotations are specified in the webapp classpath or if
 * the faces-config.xml file is present. If so, the listener checks if 
 * the FacesServlet has already been defined in web.xml and if not, it adds
 * the FacesServlet with the mappings (/faces/*, *.jsf, *.faces) dynamically.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@HandlesTypes({
        ApplicationScoped.class,
        CustomScoped.class,
        FacesBehavior.class,
        FacesBehaviorRenderer.class,
        FacesComponent.class,
        FacesConverter.class,
        FacesRenderer.class,
        FacesValidator.class,
        ListenerFor.class,
        ListenersFor.class,
        ManagedBean.class,
        ManagedProperty.class,
        NamedEvent.class,
        NoneScoped.class,
        ReferencedBean.class,
        RequestScoped.class,
        ResourceDependencies.class,
        ResourceDependency.class,
        SessionScoped.class,
        ViewScoped.class
    })
public class MyFacesContainerInitializer implements ServletContainerInitializer
{

    /**
     * If the servlet mapping for the FacesServlet is added dynamically, Boolean.TRUE 
     * is stored under this key in the ServletContext.
     * ATTENTION: this constant is duplicate in AbstractFacesInitializer.
     */
    private static final String FACES_SERVLET_ADDED_ATTRIBUTE = "org.apache.myfaces.DYNAMICALLY_ADDED_FACES_SERVLET";
    
    private static final String INITIALIZE_ALWAYS_STANDALONE = "org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE";
    private static final String FACES_CONFIG_RESOURCE = "/WEB-INF/faces-config.xml";
    private static final Logger log = Logger.getLogger(MyFacesContainerInitializer.class.getName());
    private static final String[] FACES_SERVLET_MAPPINGS = { "/faces/*", "*.jsf", "*.faces" };
    private static final String FACES_SERVLET_NAME = "FacesServlet";
    private static final Class<? extends Servlet> FACES_SERVLET_CLASS = FacesServlet.class;
    private static Class<?> DELEGATED_FACES_SERVLET_CLASS = null;

    static 
    {
        try 
        {
            DELEGATED_FACES_SERVLET_CLASS = classForName("org.apache.myfaces.shared_impl.webapp.webxml.DelegatedFacesServlet");
        }
        catch (Exception e)
        {
            //No op
        }
    }

    public void onStartup(Set<Class<?>> clazzes, ServletContext servletContext) throws ServletException
    {
        boolean startDireclty = shouldStartupRegardless(servletContext);

        if (startDireclty)
        {
            // if the INITIALIZE_ALWAYS_STANDALONE param was set to true,
            // we do not want to have the FacesServlet beeing added, we simply 
            // do no extra configuration in here.
            return;
        }

        if ((clazzes != null && !clazzes.isEmpty()) || isFacesConfigPresent(servletContext))
        {
            // look for the FacesServlet
            Map<String, ? extends ServletRegistration> servlets = servletContext.getServletRegistrations();
            for (Map.Entry<String, ? extends ServletRegistration> servletEntry : servlets.entrySet())
            {
                String className = servletEntry.getValue().getClassName();
                if (FACES_SERVLET_CLASS.getName().equals(className)
                        || isDelegatedFacesServlet(className))
                {
                    // we found a FacesServlet, so we have nothing to do!
                    return;
                }
            }

            // the FacesServlet is not installed yet - install it
            ServletRegistration.Dynamic servlet = servletContext.addServlet(FACES_SERVLET_NAME, FACES_SERVLET_CLASS);

            //try to add typical JSF mappings
            String[] mappings = FACES_SERVLET_MAPPINGS;
            Set<String> conflictMappings = servlet.addMapping(mappings);
            if (conflictMappings != null && !conflictMappings.isEmpty())
            {
                //at least one of the attempted mappings is in use, remove and try again
                Set<String> newMappings = new HashSet<String>(Arrays.asList(mappings));
                newMappings.removeAll(conflictMappings);
                mappings = newMappings.toArray(new String[newMappings.size()]);
                servlet.addMapping(mappings);
            }

            if (mappings != null && mappings.length > 0)
            {
                // at least one mapping was added 
                // now we have to set a field in the ServletContext to indicate that we have
                // added the mapping dynamically, because MyFaces just parsed the web.xml to
                // find mappings and thus it would abort initializing
                servletContext.setAttribute(FACES_SERVLET_ADDED_ATTRIBUTE, Boolean.TRUE);

                // add a log message
                log.log(Level.INFO, "Added FacesServlet with mappings="
                        + Arrays.toString(mappings));
            }

        }
    }

    /**
     * Checks if the <code>INITIALIZE_ALWAYS_STANDALONE</code> flag is ture in <code>web.xml</code>.
     * If the flag is true, this means we should not add the FacesServlet, instead we want to
     * init MyFaces regardless...
     */
    private boolean shouldStartupRegardless(ServletContext servletContext)
    {
        try
        {
            String standaloneStartup = servletContext.getInitParameter(INITIALIZE_ALWAYS_STANDALONE);

            // "true".equalsIgnoreCase(param) is faster than Boolean.valueOf()
            return "true".equalsIgnoreCase(standaloneStartup);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Checks if /WEB-INF/faces-config.xml is present.
     * @return
     */
    private boolean isFacesConfigPresent(ServletContext servletContext)
    {
        try
        {
            if (servletContext.getResource(FACES_CONFIG_RESOURCE) != null)
                return true;

            // check for alternate faces-config files specified by javax.faces.CONFIG_FILES
            String configFilesAttrValue = servletContext.getInitParameter(FacesServlet.CONFIG_FILES_ATTR);
            if (configFilesAttrValue != null)
            {
                String[] configFiles = configFilesAttrValue.split(",");
                for (String file : configFiles)
                {
                    if (servletContext.getResource(file.trim()) != null)
                        return true;
                }
            }

            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Checks if the class represented by className implements DelegatedFacesServlet.
     * @param className
     * @return
     */
    private boolean isDelegatedFacesServlet(String className)
    {
        if (className == null)
        {
            // The class name can be null if this is e.g., a JSP mapped to
            // a servlet.

            return false;
        }
        try
        {
            Class<?> clazz = Class.forName(className);
            return DELEGATED_FACES_SERVLET_CLASS.isAssignableFrom(clazz);
        }
        catch (ClassNotFoundException cnfe)
        {
            return false;
        }
    }

   // ~ Methods Copied from _ClassUtils ------------------------------------------------------------------------------------
    
    /**
     * Tries a Class.loadClass with the context class loader of the current thread first and automatically falls back to
     * the ClassUtils class loader (i.e. the loader of the myfaces.jar lib) if necessary.
     * 
     * @param type
     *            fully qualified name of a non-primitive non-array class
     * @return the corresponding Class
     * @throws NullPointerException
     *             if type is null
     * @throws ClassNotFoundException
     */
    private static Class<?> classForName(String type) throws ClassNotFoundException
    {
        if (type == null)
            throw new NullPointerException("type");
        try
        {
            // Try WebApp ClassLoader first
            return Class.forName(type, false, // do not initialize for faster startup
                getContextClassLoader());
        }
        catch (ClassNotFoundException ignore)
        {
            // fallback: Try ClassLoader for ClassUtils (i.e. the myfaces.jar lib)
            return Class.forName(type, false, // do not initialize for faster startup
                MyFacesContainerInitializer.class.getClassLoader());
        }
    }
    
    /**
     * Gets the ClassLoader associated with the current thread. Returns the class loader associated with the specified
     * default object if no context loader is associated with the current thread.
     * 
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader(){
        if (System.getSecurityManager() != null) {
            try {
                Object cl = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws PrivilegedActionException {
                                return Thread.currentThread().getContextClassLoader();
                            }
                        });
                return (ClassLoader) cl;
            } catch (PrivilegedActionException pae) {
                throw new FacesException(pae);
            }
        }else{
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
