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

/**
 * Contains the metadata information to reference a resource 
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ResourceMeta
{

    private String _prefix;
    private String _libraryName;
    private String _libraryVersion;
    private String _resourceName;
    private String _resourceVersion;
    
    public ResourceMeta(String prefix, String libraryName, String libraryVersion,
            String resourceName, String resourceVersion)
    {
        _prefix = prefix;
        _libraryName = libraryName;
        _libraryVersion = libraryVersion;
        _resourceName = resourceName;
        _resourceVersion = resourceVersion;
    }

    public String getLibraryName()
    {
        return _libraryName;
    }    
    
    public void setLibraryName(String libraryName)
    {
        _libraryName = libraryName;
    }
    
    public String getResourceName()
    {
        return _resourceName;
    }    

    public void setResourceName(String resourceName)
    {
        _resourceName = resourceName;
    }

    public void setPrefix(String prefix)
    {
        this._prefix = prefix;
    }

    public String getPrefix()
    {
        return _prefix;
    }

    public void setLibraryVersion(String libraryVersion)
    {
        this._libraryVersion = libraryVersion;
    }

    public String getLibraryVersion()
    {
        return _libraryVersion;
    }

    public void setResourceVersion(String resourceVersion)
    {
        this._resourceVersion = resourceVersion;
    }

    public String getResourceVersion()
    {
        return _resourceVersion;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        boolean firstSlashAdded = false;
        if (_prefix != null && _prefix.length() > 0)
        {
            builder.append(_prefix);
            firstSlashAdded = true;
        }
        if (_libraryName != null)
        {
            if (firstSlashAdded) builder.append('/');
            builder.append(_libraryName);
            firstSlashAdded = true;
        }
        if (_libraryVersion != null)
        {
            if (firstSlashAdded) builder.append('/');
            builder.append(_libraryVersion);
            firstSlashAdded = true;
        }
        if (_resourceName != null)
        {
            if (firstSlashAdded) builder.append('/');
            builder.append(_resourceName);
            firstSlashAdded = true;
        }
        if (_resourceVersion != null)
        {
            if (firstSlashAdded) builder.append('/');
            builder.append(_resourceVersion);
            builder.append(
                    _resourceName.substring(_resourceName.lastIndexOf('.')));
            firstSlashAdded = true;
        }

        return builder.toString();
    }

}
