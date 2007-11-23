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

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.util.ContainerUtils;

import javax.faces.FactoryFinder;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class StartupServletContextListener extends AbstractMyFacesListener
	implements ServletContextListener
{
    static final String FACES_INIT_DONE = StartupServletContextListener.class.getName() + ".FACES_INIT_DONE";

    private static final Log log = LogFactory.getLog(StartupServletContextListener.class);

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
            if (ContainerUtils.isJsp21()) 
            {
                _facesInitializer = new Jsp21FacesInitializer();
            } 
            else 
            {
                _facesInitializer = new Jsp20FacesInitializer();
            }
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

    public void contextDestroyed(ServletContextEvent event)
    {
    	doPredestroy(event);
    	
        if (_facesInitializer != null && _servletContext != null)
        {
            _facesInitializer.destroyFaces(_servletContext);
        }
        FactoryFinder.releaseFactories();
        _servletContext = null;
    }
    
    private void doPredestroy(ServletContextEvent event) {
    			
    	ServletContext ctx = event.getServletContext();
       	Enumeration<String> attributes = ctx.getAttributeNames();
       	
       	while(attributes.hasMoreElements()) 
       	{
       		String name = attributes.nextElement();
       		Object value = ctx.getAttribute(name);
       		doPreDestroy(value, name, ManagedBeanBuilder.APPLICATION);
       	}
    }
}
