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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import jakarta.faces.context.ExternalContext;

import org.apache.myfaces.config.MetaInfResourceCache;
import org.apache.myfaces.spi.FaceletConfigResourceProvider;

/**
 *
 * @since 2.0.2
 * @author Leonardo Uribe
 */
public class DefaultFaceletConfigResourceProvider extends FaceletConfigResourceProvider
{
    private static final String FACELET_TAGLIB_SUFFIX = ".taglib.xml";

    public DefaultFaceletConfigResourceProvider()
    {
        super();
    }

    @Override
    public Collection<URL> getFaceletTagLibConfigurationResources(
            ExternalContext context) throws IOException
    {
        List<URL> urlSet = new ArrayList<>();

        // Files inside META-INF ending with .taglib.xml, reusing the shared cached META-INF/ name scan;
        // only the matches are resolved to URLs via getResources (as before).
        ClassLoader loader = MetaInfResourceCache.getClassLoader();
        for (String name : MetaInfResourceCache.getMetaInfEntryNames(context))
        {
            if (name.endsWith(FACELET_TAGLIB_SUFFIX))
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
