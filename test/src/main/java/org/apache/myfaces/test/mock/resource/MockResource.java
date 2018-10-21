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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>Resource</code>.</p>
 * <p/>
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class MockResource extends Resource
{
    private MockResourceMeta _resourceMeta;
    private MockResourceLoader _resourceLoader;
    private MockResourceHandlerSupport _resourceHandlerSupport;

    public MockResource(MockResourceMeta resourceMeta,
            MockResourceLoader resourceLoader,
            MockResourceHandlerSupport support, String contentType)
    {
        _resourceMeta = resourceMeta;
        _resourceLoader = resourceLoader;
        _resourceHandlerSupport = support;
        setLibraryName(resourceMeta.getLibraryName());
        setResourceName(resourceMeta.getResourceName());
        setContentType(contentType);
    }

    public MockResourceLoader getResourceLoader()
    {
        return _resourceLoader;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return getResourceLoader().getResourceInputStream(_resourceMeta);
    }

    @Override
    public String getRequestPath()
    {
        String path;
        if (_resourceHandlerSupport.isExtensionMapping())
        {
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/'
                    + getResourceName() + _resourceHandlerSupport.getMapping();
        }
        else
        {
            String mapping = _resourceHandlerSupport.getMapping();
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/'
                    + getResourceName();
            path = (mapping == null) ? path : mapping + path;
        }

        return path;
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        return Collections.emptyMap();
    }

    @Override
    public URL getURL()
    {
        return getResourceLoader().getResourceURL(_resourceMeta);
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        return true;
    }
}