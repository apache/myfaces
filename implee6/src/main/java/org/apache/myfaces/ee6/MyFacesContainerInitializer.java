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

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;

import org.apache.myfaces.shared_impl.webapp.webxml.DelegatedFacesServlet;

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
    
    private static final String FACES_CONFIG_RESOURCE = "/WEB-INF/faces-config.xml";
    private static final Logger log = Logger.getLogger(MyFacesContainerInitializer.class.getName());
    
    public void onStartup(Set<Class<?>> clazzes, ServletContext servletContext) throws ServletException
    {
        if ((clazzes != null && !clazzes.isEmpty()) || isFacesConfigPresent(servletContext))
        {
            // look for the FacesServlet
            Map<String, ? extends ServletRegistration> servlets = servletContext.getServletRegistrations();
            for (Map.Entry<String, ? extends ServletRegistration> servletEntry : servlets.entrySet())
            {
                String className = servletEntry.getValue().getClassName();
                if (FacesServlet.class.getName().equals(className)
                        || isDelegatedFacesServlet(className))
                {
                    // we found a FacesServlet, so we have nothing to do!
                    return;
                }
            }
            
            // the FacesServlet is not installed yet - install it
            ServletRegistration.Dynamic servlet = servletContext.addServlet("FacesServlet", FacesServlet.class);
            servlet.addMapping("/faces/*", "*.jsf", "*.faces");
            
            // now we have to set a field in the ServletContext to indicate that we have
            // added the mapping dynamically, because MyFaces just parsed the web.xml to
            // find mappings and thus it would abort initializing
            servletContext.setAttribute(FACES_SERVLET_ADDED_ATTRIBUTE, Boolean.TRUE);
            
            // add a log message
            log.log(Level.INFO, "Added FacesServlet with mappings /faces/*, *.jsf and *.faces");
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
            return servletContext.getResource(FACES_CONFIG_RESOURCE) != null;
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
        if (className == null) {
            // The class name can be null if this is e.g., a JSP mapped to
            // a servlet.

            return false;
        }
        try
        {
            Class<?> clazz = Class.forName(className);
            return DelegatedFacesServlet.class.isAssignableFrom(clazz);
        } 
        catch (ClassNotFoundException cnfe)
        {
            return false;
        }
    }

}
