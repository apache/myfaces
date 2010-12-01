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
package org.apache.myfaces.spi;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.spi.impl.DefaultServiceLoaderFinder;

/**
 * 
 * @author Leonardo Uribe
 * @since 2.0.3
 *
 */
public class ServiceLoaderFinderFactory
{

    private final static String SERVICE_LOADER_KEY = "org.apache.myfaces.spi.ServiceLoaderFinder";
    
    @JSFWebConfigParam(since = "2.0.3", desc = "Class name of a custom ServiceLoaderFinder implementation.")
    private static final String SERVICE_LOADER_FINDER_PARAM = "org.apache.myfaces.SERVICE_LOADER_FINDER";
    

    /**
     * 
     * @param ectx
     * @return
     */
    public static ServiceLoaderFinder getServiceLoaderFinder(ExternalContext ectx)
    {
        ServiceLoaderFinder slp = (ServiceLoaderFinder) ectx.getApplicationMap().get(SERVICE_LOADER_KEY);
        if (slp == null)
        {
            slp = _getServiceLoaderFinderFromInitParam(ectx);
            if (slp == null)
            {
                slp = new DefaultServiceLoaderFinder();
                setServiceLoaderFinder(ectx, slp);
            }
        }
        return slp;
    }

    
    /**
     * Set a ServiceLoaderFinder to the current application, to locate 
     * SPI service providers used by MyFaces.  
     * 
     * This method should be called before the web application is initialized,
     * specifically before AbstractFacesInitializer.initFaces(ServletContext)
     * otherwise it will have no effect.
     * 
     * @param ectx
     * @param slp
     */
    public static void setServiceLoaderFinder(ExternalContext ectx, ServiceLoaderFinder slp)
    {
        ectx.getApplicationMap().put(SERVICE_LOADER_KEY, slp);
    }
    
    public static void setServiceLoaderFinder(ServletContext ctx, ServiceLoaderFinder slp)
    {
        ctx.setAttribute(SERVICE_LOADER_KEY, slp);
    }

    /**
     * Gets a ServiceLoaderFinder from the web.xml config param.
     * @param context
     * @return
     */
    private static ServiceLoaderFinder _getServiceLoaderFinderFromInitParam(ExternalContext context)
    {
        String initializerClassName = context.getInitParameter(SERVICE_LOADER_FINDER_PARAM);
        if (initializerClassName != null)
        {
            try
            {
                // get Class object
                Class<?> clazz = ClassUtils.classForName(initializerClassName);
                if (!ServiceLoaderFinder.class.isAssignableFrom(clazz))
                {
                    throw new FacesException("Class " + clazz 
                            + " does not implement FacesInitializer");
                }
                
                // create instance and return it
                return (ServiceLoaderFinder) ClassUtils.newInstance(clazz);
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new FacesException("Could not find class of specified FacesInitializer", cnfe);
            }
        }
        return null;
    }
    
}
