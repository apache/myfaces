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

import org.apache.myfaces.test.mock.MockServletContext;

import jakarta.faces.application.Resource;
import jakarta.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * <p>Mock simple implementation of <code>Resource</code>.</p>
 * 
 * <p>
 * It is used by MockSimpleResourceHandler to wrap resource instances.
 * </p>
 * 
 * @author Jakob Korherr (latest modification by $Author: lu4242 $)
 * @version $Revision: 882702 $ $Date: 2009-11-20 15:16:07 -0500 (Vie, 20 Nov 2009) $
 * @since 1.0.0
 */
public class MockSimpleResource extends Resource
{

    private String _prefix;
    private String _libraryName;
    private String _libraryVersion;
    private String _resourceName;
    private String _resourceVersion;
    private File _documentRoot;

    /**
     * Creates new resource object
     *
     * @param prefix          locale prefix if any
     * @param libraryName     resource library name
     * @param libraryVersion  resource library version if any
     * @param resourceName    resource file name
     * @param resourceVersion resource version if any
     * @param documentRoot    parent folder of resource directories. Must not be <code>null</code>
     */
    public MockSimpleResource(String prefix, String libraryName,
            String libraryVersion, String resourceName, String resourceVersion,
            File documentRoot)
    {
        _prefix = prefix;
        _libraryName = libraryName;
        _libraryVersion = libraryVersion;
        _resourceName = resourceName;
        _resourceVersion = resourceVersion;
        _documentRoot = documentRoot;

        if (_documentRoot == null)
        {
            throw new IllegalArgumentException("documentRoot must not be null");
        }
    }

    @Override
    public String getResourceName()
    {
        return _resourceName;
    }

    @Override
    public void setResourceName(String resourceName)
    {
        _resourceName = resourceName;
    }

    @Override
    public String getLibraryName()
    {
        return _libraryName;
    }

    @Override
    public void setLibraryName(String libraryName)
    {
        _libraryName = libraryName;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        MockServletContext servletContext = (MockServletContext) FacesContext
                .getCurrentInstance().getExternalContext().getContext();
        servletContext.setDocumentRoot(_documentRoot);
        return servletContext.getResourceAsStream(buildResourcePath());
    }

    @Override
    public String getRequestPath()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL()
    {
        MockServletContext servletContext = (MockServletContext) FacesContext
                .getCurrentInstance().getExternalContext().getContext();
        servletContext.setDocumentRoot(_documentRoot);

        try
        {
            return servletContext.getResource(buildResourcePath());
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        return true;
    }

    private String buildResourcePath()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('/');
        boolean firstSlashAdded = false;
        if (_prefix != null && _prefix.length() > 0)
        {
            builder.append(_prefix);
            firstSlashAdded = true;
        }
        if (_libraryName != null)
        {
            if (firstSlashAdded)
            {
                builder.append('/');
            }
            builder.append(_libraryName);
            firstSlashAdded = true;
        }
        if (_libraryVersion != null)
        {
            if (firstSlashAdded)
            {
                builder.append('/');
            }
            builder.append(_libraryVersion);
            firstSlashAdded = true;
        }
        if (_resourceName != null)
        {
            if (firstSlashAdded)
            {
                builder.append('/');
            }
            builder.append(_resourceName);
            firstSlashAdded = true;
        }
        if (_resourceVersion != null)
        {
            if (firstSlashAdded)
            {
                builder.append('/');
            }
            builder.append(_resourceVersion);
            builder.append(_resourceName.substring(_resourceName
                    .lastIndexOf('.')));
        }

        return builder.toString();
    }

}
