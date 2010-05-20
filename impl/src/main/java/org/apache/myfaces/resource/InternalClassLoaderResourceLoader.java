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

import org.apache.myfaces.shared_impl.util.ClassUtils;

/**
 * A resource loader implementation which loads resources from the thread ClassLoader.
 * 
 */
public class InternalClassLoaderResourceLoader extends ResourceLoader
{

    public InternalClassLoaderResourceLoader(String prefix)
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
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            return getClassLoader().getResourceAsStream(getPrefix() + '/' + resourceMeta.getResourceIdentifier());
        }
        else
        {
            return getClassLoader().getResourceAsStream(resourceMeta.getResourceIdentifier());
        }
    }

    @Override
    public URL getResourceURL(ResourceMeta resourceMeta)
    {
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            return getClassLoader().getResource(getPrefix() + '/' + resourceMeta.getResourceIdentifier());
        }
        else
        {
            return getClassLoader().getResource(resourceMeta.getResourceIdentifier());
        }
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
        if (libraryName != null && 
                org.apache.myfaces.shared_impl.renderkit.html.util.ResourceUtils.JAVAX_FACES_LIBRARY_NAME.equals(libraryName) &&
                org.apache.myfaces.shared_impl.renderkit.html.util.ResourceUtils.JSF_JS_RESOURCE_NAME.equals(resourceName))
        {
            return new AliasResourceMetaImpl(prefix, libraryName, libraryVersion,
                    resourceName, resourceVersion, org.apache.myfaces.shared_impl.renderkit.html.util.ResourceUtils.JSF_UNCOMPRESSED_JS_RESOURCE_NAME);
        }
        else if (libraryName != null && libraryName.startsWith("org.apache.myfaces.core"))
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
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            URL url = getClassLoader().getResource(getPrefix() + '/' + libraryName);
            if (url != null)
            {
                return true;
            }
        }
        else
        {
            URL url = getClassLoader().getResource(libraryName);
            if (url != null)
            {
                return true;
            }
        }
        return false;
    }
}
