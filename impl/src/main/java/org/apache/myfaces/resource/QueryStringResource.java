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

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceWrapper;

/**
 * Wraps a {@link Resource} which was created from a resource name carrying a query string,
 * e.g. <code>foo.js?v=1</code> (Faces 5.0, spec issue 2242).
 * <p>
 * The query string is not part of the resource identifier, it is only appended to the
 * resource metadata of {@link #getRequestPath()}.
 */
public class QueryStringResource extends ResourceWrapper
{
    private final String queryString;
    private String requestPath;

    public QueryStringResource(Resource delegate, String queryString)
    {
        super(delegate);
        this.queryString = queryString;
    }

    public String getQueryString()
    {
        return queryString;
    }

    @Override
    public String getRequestPath()
    {
        if (requestPath == null)
        {
            requestPath = appendQueryString(getWrapped().getRequestPath(), queryString);
        }
        return requestPath;
    }

    @Override
    public String toString()
    {
        return getWrapped().toString() + '?' + queryString;
    }

    /**
     * Returns the given resource name without its query string (everything from the first <code>?</code>).
     */
    public static String removeQueryString(String resourceName)
    {
        if (resourceName == null)
        {
            return null;
        }
        int index = resourceName.indexOf('?');
        return index >= 0 ? resourceName.substring(0, index) : resourceName;
    }

    /**
     * Wraps the given resource if the given query string is not empty.
     */
    public static Resource wrap(Resource resource, String queryString)
    {
        if (resource == null || queryString == null || queryString.isEmpty())
        {
            return resource;
        }
        return new QueryStringResource(resource, queryString);
    }

    private static String appendQueryString(String path, String queryString)
    {
        if (path == null || queryString == null || queryString.isEmpty())
        {
            return path;
        }

        // keep a fragment, if any, at the end
        String fragment = "";
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0)
        {
            fragment = path.substring(fragmentIndex);
            path = path.substring(0, fragmentIndex);
        }

        return path + (path.indexOf('?') >= 0 ? '&' : '?') + queryString + fragment;
    }
}
