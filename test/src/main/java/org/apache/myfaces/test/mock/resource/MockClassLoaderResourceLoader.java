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
package org.apache.myfaces.test.mock.resource;

import java.io.InputStream;
import java.net.URL;

/**
 * A resource loader implementation which loads resources from the thread ClassLoader.
 * 
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 882702 $ $Date: 2009-11-20 15:16:07 -0500 (Vie, 20 Nov 2009) $
 * @since 1.0.0
 */
public class MockClassLoaderResourceLoader extends MockResourceLoader
{
    private ClassLoader _classLoader;

    public MockClassLoaderResourceLoader(ClassLoader loader, String prefix)
    {
        super(prefix);
        _classLoader = loader;
    }

    @Override
    public String getLibraryVersion(String path)
    {
        return null;
    }

    @Override
    public InputStream getResourceInputStream(MockResourceMeta resourceMeta)
    {
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            return getClassLoader().getResourceAsStream(
                    getPrefix() + '/' + resourceMeta.getResourceIdentifier());
        }
        else
        {
            return getClassLoader().getResourceAsStream(
                    resourceMeta.getResourceIdentifier());
        }
    }

    @Override
    public URL getResourceURL(MockResourceMeta resourceMeta)
    {
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            return getClassLoader().getResource(
                    getPrefix() + '/' + resourceMeta.getResourceIdentifier());
        }
        else
        {
            return getClassLoader().getResource(
                    resourceMeta.getResourceIdentifier());
        }
    }

    @Override
    public String getResourceVersion(String path)
    {
        return null;
    }

    @Override
    public MockResourceMeta createResourceMeta(String prefix,
            String libraryName, String libraryVersion, String resourceName,
            String resourceVersion)
    {
        return new MockResourceMeta(prefix, libraryName, libraryVersion,
                resourceName, resourceVersion);
    }

    /**
     * Returns the ClassLoader to use when looking up resources under the top level package. By default, this is the
     * context class loader.
     * 
     * @return the ClassLoader used to lookup resources
     */
    public ClassLoader getClassLoader()
    {
        return _classLoader;
    }

    public void setClassLoader(ClassLoader loader)
    {
        _classLoader = loader;
    }

    @Override
    public boolean libraryExists(String libraryName)
    {
        if (getPrefix() != null && !"".equals(getPrefix()))
        {
            URL url = getClassLoader().getResource(
                    getPrefix() + '/' + libraryName);
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
