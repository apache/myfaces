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
package org.apache.myfaces.config.annotation;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import javax.naming.NamingException;

import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.tomcat.InstanceManager;

/**
 * An annotation lifecycle provider for Tomcat 7.
 */
public class Tomcat7AnnotationLifecycleProvider implements
        DiscoverableLifecycleProvider, LifecycleProvider2
{

    private static Logger log = Logger.getLogger(Tomcat7AnnotationLifecycleProvider.class.getName());
    
    private WeakHashMap<ClassLoader, InstanceManager> instanceManagers = null;

    public Tomcat7AnnotationLifecycleProvider(ExternalContext externalContext)
    {
        instanceManagers = new WeakHashMap<ClassLoader, InstanceManager>();
    }

    public Object newInstance(String className)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NamingException, InvocationTargetException
    {
        Class<?> clazz = ClassUtils.classForName(className);
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("Creating instance of " + className);
        }
        Object object = clazz.newInstance();

        return object;
    }

    public void destroyInstance(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {
        InstanceManager manager = instanceManagers
                .get(ClassUtils.getContextClassLoader());

        if (manager != null)
        {
            manager.destroyInstance(instance);
        }
    }

    public void postConstruct(Object instance)
            throws IllegalAccessException, InvocationTargetException
    {
        InstanceManager manager = instanceManagers
                .get(ClassUtils.getContextClassLoader());
        if (manager == null)
        {
            //Initialize manager
            manager = initManager();
        }

        //Is initialized
        if (manager != null)
        {
            //Inject resources
            try 
            {
                manager.newInstance(instance);
            }
            catch (NamingException e)
            {
                throw new FacesException(e);
            }
        }
    }

    public boolean isAvailable()
    {
        try
        {
            Class.forName("org.apache.tomcat.InstanceManager",
                    true, ClassUtils.getContextClassLoader());
            return true;
        }
        catch (Exception e)
        {
            // ignore
        }

        return false;
    }

    private InstanceManager initManager()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null)
        {
            return null;
        }

        ExternalContext extCtx = context.getExternalContext();
        if (extCtx == null)
        {
            return null;
        }

        // get application map to access ServletContext attributes
        Map<String, Object> applicationMap = extCtx.getApplicationMap();

        InstanceManager instanceManager = (InstanceManager)
                applicationMap.get(InstanceManager.class.getName());
        if (instanceManager != null)
        {
            instanceManagers.put(ClassUtils.getContextClassLoader(),
                    instanceManager);
        }

        return instanceManager;
    }

}
