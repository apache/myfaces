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

/**
 * Store additional info used by MockResource and MockResourceHandler
 * 
 * @author Leonardo Uribe (latest modification by $Author: jakobk $)
 * @version $Revision: 960906 $ $Date: 2010-07-06 09:45:40 -0500 (Mar, 06 Jul 2010) $
 * @since 1.0.0
 */
public class MockResourceHandlerSupport
{

    private boolean _extensionMapping;

    private String _mapping;

    private MockResourceLoader[] _resourceLoaders;

    public MockResourceHandlerSupport()
    {
        _extensionMapping = true;
        _mapping = ".jsf";
        _resourceLoaders = new MockResourceLoader[] {
                new MockExternalContextResourceLoader("/resources"),
                new MockClassLoaderResourceLoader(MockResourceHandler
                        .getContextClassLoader(), "META-INF/resources") };
    }

    public MockResourceHandlerSupport(boolean extensionMapping, String mapping)
    {
        super();
        _extensionMapping = extensionMapping;
        _mapping = mapping;
        _resourceLoaders = new MockResourceLoader[] {
                new MockExternalContextResourceLoader("/resources"),
                new MockClassLoaderResourceLoader(MockResourceHandler
                        .getContextClassLoader(), "META-INF/resources") };
    }

    public MockResourceHandlerSupport(boolean extensionMapping, String mapping,
            ClassLoader classLoader)
    {
        _extensionMapping = extensionMapping;
        _mapping = mapping;
        _resourceLoaders = new MockResourceLoader[] {
                new MockExternalContextResourceLoader("/resources"),
                new MockClassLoaderResourceLoader(classLoader,
                        "META-INF/resources") };
    }

    public MockResourceHandlerSupport(boolean extensionMapping, String mapping,
            MockResourceLoader[] resourceLoaders)
    {
        super();
        _extensionMapping = extensionMapping;
        _mapping = mapping;
        _resourceLoaders = resourceLoaders;
    }

    /**
     * Check if the mapping used is done using extensions (.xhtml, .jsf)
     * or if it is not (/faces/*)
     * @return
     */
    public boolean isExtensionMapping()
    {
        return _extensionMapping;
    }

    public void setExtensionMapping(boolean extensionMapping)
    {
        _extensionMapping = extensionMapping;
    }

    /**
     * Get the mapping used as prefix(/faces) or suffix(.jsf)
     * 
     * @return
     */
    public String getMapping()
    {
        return _mapping;
    }

    public void setMapping(String prefix)
    {
        _mapping = prefix;
    }

    /**
     * Return an array of resource loaders used to find resources
     * using the standard. The order of ResourceLoaders define
     * its precedence. 
     * 
     * @return
     */
    public MockResourceLoader[] getResourceLoaders()
    {
        return _resourceLoaders;
    }

    public void setResourceLoaders(MockResourceLoader[] resourceLoaders)
    {
        _resourceLoaders = resourceLoaders;
    }
}
