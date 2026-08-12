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
package org.apache.myfaces.core.extensions.quarkus.runtime.application;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceHandlerWrapper;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.extensions.quarkus.runtime.MyFacesRecorder;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Quarkus does not support servletContext.getResourcePaths("/"), which is used for
 * AUTOMATIC_EXTENSIONLESS_MAPPING.
 * Therefore we scan for views on build time and reuse it here.
 */
public class QuarkusResourceHandler extends ResourceHandlerWrapper
{
    public QuarkusResourceHandler(ResourceHandler delegate)
    {
        super(delegate);
    }

    @Override
    public Stream<String> getViewResources(FacesContext facesContext,
                                           String path, int maxDepth, ResourceVisitOption... options)
    {
        boolean isTopLevelViewsRequest = path.equals("/")
                && Arrays.stream(options).anyMatch(o -> o == ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY);

        if (isTopLevelViewsRequest)
        {
            return MyFacesRecorder.TOP_LEVEL_VIEWS.stream();
        }

        return super.getViewResources(facesContext, path, maxDepth, options);
    }
}
