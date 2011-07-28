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
package org.apache.myfaces.application;

import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;

import org.apache.myfaces.resource.InternalClassLoaderResourceLoader;
import org.apache.myfaces.shared.resource.BaseResourceHandlerSupport;
import org.apache.myfaces.shared.resource.ClassLoaderResourceLoader;
import org.apache.myfaces.shared.resource.ExternalContextResourceLoader;
import org.apache.myfaces.shared.resource.ResourceLoader;

/**
 * A ResourceHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports javax.servlet, and uses a standard web.xml file.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DefaultResourceHandlerSupport extends BaseResourceHandlerSupport
{

    private ResourceLoader[] _resourceLoaders;
    
    public DefaultResourceHandlerSupport()
    {
        super();
    }

    public ResourceLoader[] getResourceLoaders()
    {
        if (_resourceLoaders == null)
        {
            //The ExternalContextResourceLoader has precedence over
            //ClassLoaderResourceLoader, so it goes first.
            if (FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development))
            {
                _resourceLoaders = new ResourceLoader[] {
                        new ExternalContextResourceLoader("/resources"),
                        new ClassLoaderResourceLoader("META-INF/resources"),
                        new InternalClassLoaderResourceLoader("META-INF/internal-resources")
                };
            }
            else
            {
                _resourceLoaders = new ResourceLoader[] {
                        new ExternalContextResourceLoader("/resources"),
                        new ClassLoaderResourceLoader("META-INF/resources")
                };
            }
        }
        return _resourceLoaders;
    }
}
