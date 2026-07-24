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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import jakarta.faces.context.ExternalContext;

import org.apache.myfaces.spi.FacesConfigResourceProvider;

/**
 *
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public class DefaultFacesConfigResourceProvider extends FacesConfigResourceProvider
{
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";

    /**
     * <p>Resource path used to acquire implicit resources buried
     * inside application JARs.</p>
     */
    private static final String FACES_CONFIG_IMPLICIT = "META-INF/faces-config.xml";


    public DefaultFacesConfigResourceProvider()
    {
        super();
    }

    @Override
    public Collection<URL> getMetaInfConfigurationResources(ExternalContext context) throws IOException
    {
        List<URL> urlSet = new ArrayList<>();

        // Filter the shared, cached META-INF/ name scan instead of walking every jar again:
        // - META-INF/faces-config.xml (the standard implicit name), and
        // - META-INF/*.faces-config.xml (custom names, e.g. maven-jetty-plugin layouts).
        // Only the handful of matches are resolved to URLs via getResources (as before).
        ClassLoader loader = MetaInfResourceCache.getClassLoader();
        for (String name : MetaInfResourceCache.getMetaInfEntryNames(context))
        {
            if (name.equals(FACES_CONFIG_IMPLICIT) || name.endsWith(FACES_CONFIG_SUFFIX))
            {
                for (Enumeration<URL> resources = loader.getResources(name); resources.hasMoreElements();)
                {
                    urlSet.add(resources.nextElement());
                }
            }
        }

        return urlSet;
    }
}
