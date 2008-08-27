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
package javax.faces.application;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * 
 */
public abstract class Resource
{
    private String _contentType;
    private String _libraryName;
    private String _resourceName;

    public String getContentType()
    {
        return _contentType;
    }
    
    public abstract InputStream getInputStream();

    public String getLibraryName()
    {
        return _libraryName;
    }
    
    public abstract String getRequestPath();

    public String getResourceName()
    {
        return _resourceName;
    }
    
    public abstract Map<String, String> getResponseHeaders();
    
    public abstract URL getURL();

    public void setContentType(String contentType)
    {
        _contentType = contentType;
    }

    public void setLibraryName(String libraryName)
    {
        _libraryName = libraryName;
    }

    public void setResourceName(String resourceName)
    {
        _resourceName = resourceName;
    }
    
    @Override
    public String toString()
    {
        return getRequestPath();
    }
    
    public abstract boolean userAgentNeedsUpdate(FacesContext context);
}
