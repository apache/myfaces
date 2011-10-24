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
package org.apache.myfaces.shared.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom ClassLoader that sets the current Thread's context ClassLoader as parent ClassLoader
 * and uses the parent ClassLoader, myfaces-api and myfaces-impl ClassLoaders to locate Resources and Classes. 
 *
 * @author Jakob Korherr
 */
public class MyFacesClassLoader extends ClassLoader
{

    private static final String APPLICATION_MAP_KEY = MyFacesClassLoader.class.getName();

    /**
     * Returns the cached instance of the MyFacesClassLoader for this application or a new
     * one if now cached instance is available yet.
     *
     * @param externalContext
     * @return
     */
    public static final MyFacesClassLoader getCurrentInstance(ExternalContext externalContext)
    {
        if (externalContext == null)
        {
            // if no ExternalContext is available, return a new instance
            // this may be the case in Unit tests or outside of the JSF lifecycle
            return new MyFacesClassLoader();
        }

        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        MyFacesClassLoader classLoader = (MyFacesClassLoader) applicationMap.get(APPLICATION_MAP_KEY);

        if (classLoader == null)
        {
            // no instance available for this application yet, create one and cache it
            classLoader = new MyFacesClassLoader();
            applicationMap.put(APPLICATION_MAP_KEY, classLoader);
        }

        return classLoader;
    }

    private static ClassLoader getContextClassLoaderFailsafe()
    {
        ClassLoader contextClassLoader = ClassLoaderUtils.getContextClassLoader();

        if (contextClassLoader == null)
        {
            // fall back to the ClassLoader of this class if the current Thread has no ContextClassLoader
            contextClassLoader = MyFacesClassLoader.class.getClassLoader();
        }

        return contextClassLoader;
    }

    private ClassLoader apiClassLoader;
    private ClassLoader implClassLoader;

    public MyFacesClassLoader()
    {
        // context ClassLoader is parent ClassLoader
        super(getContextClassLoaderFailsafe());

        apiClassLoader = FacesContext.class.getClassLoader(); // myfaces-api classloader
        implClassLoader = getClass().getClassLoader();  // myfaces-impl (or tomahawk) classloader
    }

    @Override
    public URL getResource(String s)
    {
        // context classloader
        URL url = super.getResource(s);

        if (url == null)
        {
            // try api
            url = apiClassLoader.getResource(s);

            if (url == null)
            {
                // try impl
                url = implClassLoader.getResource(s);
            }
        }

        return url;
    }

    @Override
    public Enumeration<URL> getResources(String s) throws IOException
    {
        // use all 3 classloaders and merge without duplicates
        Set<URL> urls = new HashSet<URL>(); // no duplicates

        // context classloader
        urls.addAll(Collections.list(super.getResources(s)));

        // api classlaoder
        urls.addAll(Collections.list(apiClassLoader.getResources(s)));

        // impl classlaoder
        urls.addAll(Collections.list(implClassLoader.getResources(s)));

        return Collections.enumeration(urls);
    }

    @Override
    public InputStream getResourceAsStream(String s)
    {
        // context classloader
        InputStream stream = super.getResourceAsStream(s);

        if (stream == null)
        {
            // try api
            stream = apiClassLoader.getResourceAsStream(s);

            if (stream == null)
            {
                // try impl
                stream = implClassLoader.getResourceAsStream(s);
            }
        }

        return stream;
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException
    {
        Class<?> clazz = null;
        ClassNotFoundException firstException = null;

        try
        {
            // context classloader
            clazz = super.loadClass(s);
        }
        catch (ClassNotFoundException cnfe)
        {
            firstException = cnfe;
        }


        if (clazz == null)
        {
            // try api
            clazz = loadClassFailsafe(s, apiClassLoader);

            if (clazz == null)
            {
                // try impl
                clazz = loadClassFailsafe(s, implClassLoader);

                if (clazz == null)
                {
                    // still null, throw first ClassNotFoundException
                    throw firstException;
                }
            }
        }

        return clazz;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof MyFacesClassLoader)
        {
            MyFacesClassLoader other = (MyFacesClassLoader) o;

            // same parent --> same ContextClassLoader --> same MyFacesClassLoader
            return (other.getParent().equals(this.getParent()));
        }

        return false;
    }

    private Class<?> loadClassFailsafe(String s, ClassLoader classLaoder)
    {
        try
        {
            return classLaoder.loadClass(s);
        }
        catch (ClassNotFoundException cnfe)
        {
            return null;
        }
    }

}
