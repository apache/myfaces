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
package org.apache.myfaces.spi.impl;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.util.ExternalSpecifications;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CDIAnnotationProvider extends AnnotationProvider
{
    private static final Logger log = Logger.getLogger(CDIAnnotationProvider.class.getName());

    public CDIAnnotationProvider()
    {
        super();
    }

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(ExternalContext ctx)
    {
        if (!ExternalSpecifications.isCDIAvailable(ctx))
        {
            throw new FacesException("The default " + AnnotationProvider.class.getName()
                    + " is " + this.getClass().getName()
                    + " and requires a active CDI container. As alternative you can use the SPI to switch the "
                    + AnnotationProvider.class.getName());
        }

        BeanManager beanManager = CDIUtils.getBeanManager(ctx);
        CdiAnnotationProviderExtension extension = CDIUtils.getOptional(beanManager,
                CdiAnnotationProviderExtension.class);
        return extension.getMap();
    }

    @Override
    public Set<URL> getBaseUrls(ExternalContext context) throws IOException
    {
        return Collections.emptySet();
    }
}

