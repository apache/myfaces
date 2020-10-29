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
import java.util.Comparator;
import java.util.Iterator;

import jakarta.faces.FacesWrapper;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.FacesContext;

public abstract class ResourceLoaderWrapper extends ResourceLoader implements FacesWrapper<ResourceLoader>
{
    public ResourceLoaderWrapper()
    {
        super(null);
    }

    @Override
    public String getResourceVersion(String path)
    {
        return getWrapped().getResourceVersion(path);
    }

    @Override
    public String getLibraryVersion(String path)
    {
        return getWrapped().getLibraryVersion(path);
    }

    @Override
    public URL getResourceURL(ResourceMeta resourceMeta)
    {
        return getWrapped().getResourceURL(resourceMeta);
    }

    @Override
    public InputStream getResourceInputStream(ResourceMeta resourceMeta)
    {
        return getWrapped().getResourceInputStream(resourceMeta);
    }

    @Override
    public ResourceMeta createResourceMeta(String prefix, String libraryName,
            String libraryVersion, String resourceName, String resourceVersion)
    {
        return getWrapped().createResourceMeta(prefix, libraryName, libraryVersion,
                resourceName, resourceVersion);
    }

    @Override
    public boolean libraryExists(String libraryName)
    {
        return getWrapped().libraryExists(libraryName);
    }

    @Override
    public String getPrefix()
    {
        return getWrapped().getPrefix();
    }

    @Override
    public void setPrefix(String prefix)
    {
        getWrapped().setPrefix(prefix);
    }

    @Override
    public boolean resourceExists(ResourceMeta resourceMeta)
    {
        return getWrapped().resourceExists(resourceMeta);
    }

    @Override
    protected Comparator<String> getVersionComparator()
    {
        return getWrapped().getVersionComparator();
    }

    @Override
    protected void setVersionComparator(Comparator<String> versionComparator)
    {
        getWrapped().setVersionComparator(versionComparator);
    }

    @Override
    public Iterator<String> iterator(FacesContext facesContext, String path, 
            int maxDepth, ResourceVisitOption... options)
    {
        return getWrapped().iterator(facesContext, path, maxDepth, options);
    }
    
}
