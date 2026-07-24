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
package org.apache.myfaces.config;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;

import org.apache.myfaces.util.lang.ClassUtils;
import org.apache.myfaces.view.facelets.util.Classpath;

/**
 * Caches, per web application, the names of all classpath resource entries under {@code META-INF/}
 * found in a single scan.
 *
 * <p>At startup several independent providers each scan {@code META-INF/} for a different suffix
 * ({@code *.faces-config.xml}, {@code *.taglib.xml}, {@code contracts/.../jakarta.faces.contract.xml}).
 * Each scan opens and fully enumerates every jar on the classpath, so doing the walk once and letting
 * the providers filter the shared name set by suffix avoids walking every jar multiple times. Only the
 * few matching names are then resolved to URLs via {@link ClassLoader#getResources(String)} (which is
 * what the providers did previously anyway). The cache lives in the {@link ExternalContext} application
 * map, so it is scoped to the web application and released on undeploy - no static state.</p>
 */
public final class MetaInfResourceCache
{
    public static final String META_INF_PREFIX = "META-INF/";

    private static final String CACHE_KEY = MetaInfResourceCache.class.getName() + ".META_INF_NAMES";

    private MetaInfResourceCache()
    {
    }

    /**
     * Returns the names of all classpath resource entries under {@code META-INF/}, scanning the
     * classpath once per web application and caching the result. Callers filter the returned names by
     * suffix and resolve matches via {@link ClassLoader#getResources(String)}.
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getMetaInfEntryNames(ExternalContext externalContext)
    {
        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        Set<String> names = (Set<String>) applicationMap.get(CACHE_KEY);
        if (names == null)
        {
            try
            {
                names = Classpath.searchResourceNames(getClassLoader(), META_INF_PREFIX);
            }
            catch (IOException e)
            {
                throw new FacesException(e);
            }
            applicationMap.put(CACHE_KEY, names);
        }
        return names;
    }

    /**
     * The classloader used both to scan and (by the providers) to resolve matches, kept consistent so
     * the cached names line up with what {@link ClassLoader#getResources(String)} will return.
     */
    public static ClassLoader getClassLoader()
    {
        ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null)
        {
            loader = MetaInfResourceCache.class.getClassLoader();
        }
        return loader;
    }
}
