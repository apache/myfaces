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
package org.apache.myfaces.resource;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.util.lang.ClassUtils;

/**
 * A resource loader implementation which loads resources from the thread ClassLoader.
 * 
 */
public class ClassLoaderResourceLoader extends ResourceLoader
{
    public ClassLoaderResourceLoader(String prefix)
    {
        super(prefix);
    }

    @Override
    public String getLibraryVersion(String path)
    {
        return null;
    }

    @Override
    public InputStream getResourceInputStream(ResourceMeta resourceMeta)
    {
        InputStream is = null;

        String prefix = getPrefix();
        if (prefix != null && !prefix.isEmpty())
        {
            String name = prefix + '/' + resourceMeta.getResourceIdentifier();
            is = getClassLoader().getResourceAsStream(name);
            if (is == null)
            {
                is = this.getClass().getClassLoader().getResourceAsStream(name);
            }
            return is;
        }
        else
        {
            is = getClassLoader().getResourceAsStream(resourceMeta.getResourceIdentifier());
            if (is == null)
            {
                is = this.getClass().getClassLoader().getResourceAsStream(resourceMeta.getResourceIdentifier());
            }
            return is;
        }
    }

    //@Override
    public URL getResourceURL(String resourceId)
    {
        URL url = null;

        String prefix = getPrefix();
        if (prefix != null && !prefix.isEmpty())
        {
            String name = prefix + '/' + resourceId;
            url = getClassLoader().getResource(name);
            if (url == null)
            {
                url = this.getClass().getClassLoader().getResource(name);
            }
            return url;
        }
        else
        {
            url = getClassLoader().getResource(resourceId);
            if (url == null)
            {
                url = this.getClass().getClassLoader().getResource(resourceId);
            }
            return url;
        }
    }
    
    @Override
    public URL getResourceURL(ResourceMeta resourceMeta)
    {
        if (resourceMeta == null)
        {
            return null;
        }
        return getResourceURL(resourceMeta.getResourceIdentifier());
    }

    @Override
    public String getResourceVersion(String path)
    {
        return null;
    }

    @Override
    public ResourceMeta createResourceMeta(String prefix, String libraryName, String libraryVersion,
                                           String resourceName, String resourceVersion)
    {
        return new ResourceMetaImpl(prefix, libraryName, libraryVersion, resourceName, resourceVersion);
    }

    /**
     * Returns the ClassLoader to use when looking up resources under the top level package. By default, this is the
     * context class loader.
     * 
     * @return the ClassLoader used to lookup resources
     */
    protected ClassLoader getClassLoader()
    {
        return ClassUtils.getContextClassLoader();
    }

    @Override
    public boolean libraryExists(String libraryName)
    {
        String prefix = getPrefix();
        if (prefix != null && !prefix.isEmpty())
        {
            String name = prefix + '/' + libraryName;
            URL url = getClassLoader().getResource(name);
            if (url == null)
            {
                url = this.getClass().getClassLoader().getResource(name);
            }
            if (url != null)
            {
                return true;
            }
        }
        else
        {
            URL url = getClassLoader().getResource(libraryName);
            if (url == null)
            {
                url = this.getClass().getClassLoader().getResource(libraryName);
            }
            if (url != null)
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterator<String> iterator(FacesContext facesContext, String path, 
            int maxDepth, ResourceVisitOption... options)
    {
        String basePath = path;

        String prefix = getPrefix();
        if (prefix != null)
        {
            basePath = prefix + '/' + (path.startsWith("/") ? path.substring(1) : path);
        }

        URL url = getClassLoader().getResource(basePath);

        return new ClassLoaderResourceLoaderIterator(url, basePath, maxDepth, options);
    }
    
}
