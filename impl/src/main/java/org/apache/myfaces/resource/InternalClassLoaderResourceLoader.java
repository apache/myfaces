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

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;

/**
 * A specialized classloader for our internal resources
 * it enables el resolution on source level for our jsf.js files
 * and reserves a myfaces namespace in the resources for other custom files.
 * This resource loader is generally reserved for internal purposes.
 */
public class InternalClassLoaderResourceLoader extends ResourceLoader
{

    private final boolean _developmentStage;

    public InternalClassLoaderResourceLoader(String prefix)
    {
        super(prefix);
        _developmentStage = FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development);
    }

    @Override
    public String getLibraryVersion(String path)
    {
        return null;
    }

    @Override
    public InputStream getResourceInputStream(ResourceMeta resourceMeta)
    {
        InputStream is;

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

    public URL getResourceURL(String resourceId)
    {
        URL url;

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
        //handle faces.js
        final boolean jakartaFacesLib = ResourceHandler.FACES_SCRIPT_LIBRARY_NAME.equals(libraryName);
        final boolean jakartaFaces = jakartaFacesLib &&
                ResourceHandler.FACES_SCRIPT_RESOURCE_NAME.equals(resourceName);

        if (jakartaFaces)
        {
            String remappedResourceName = _developmentStage ?
                    ResourceUtils.FACES_UNCOMPRESSED_JS_RESOURCE_NAME :
                    ResourceUtils.FACES_MINIMAL_JS_RESOURCE_NAME;

            // in development stage we serve the uncompressed
            // file and the map file, we have a special case of el expressions
            // in our javascript for context path and separator char,
            // hence we enable value expressions for those resources
            return new AliasResourceMetaImpl(prefix, libraryName, libraryVersion, resourceName, resourceVersion,
                    remappedResourceName, true);
        }
        // TODO still needed for tests?
        else if (_developmentStage && libraryName != null && libraryName.startsWith("org.apache.myfaces.core"))
        {
            return new ResourceMetaImpl(prefix, libraryName, libraryVersion, resourceName, resourceVersion);
        }
        else
        {
            return null;
        }
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
            return url != null;
        }
        else
        {
            URL url = getClassLoader().getResource(libraryName);
            if (url == null)
            {
                url = this.getClass().getClassLoader().getResource(libraryName);
            }
            return url != null;
        }
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
