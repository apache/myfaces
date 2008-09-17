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

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;

/**
 * DOCUMENT ME!
 *
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * 
 * @version $Revision: 696515 $ $Date: 2008-09-17 19:37:53 -0500 (mer., 17 sept. 2008) $
 */
public class ResourceHandlerImpl extends ResourceHandler
{
    @Override
    public Resource createResource(String resourceName)
    {
        // TODO: JSF 2.0 #35
        return null;
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        // TODO: JSF 2.0 #36
        return null;
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType)
    {
        // TODO: JSF 2.0 #37
        return null;
    }

    @Override
    public String getRendererTypeForResourceName(String resourceName)
    {
        // TODO: JSF 2.0 #38
        return null;
    }

    @Override
    public void handleResourceRequest(FacesContext context)
    {
        // TODO: JSF 2.0 #39
    }

    @Override
    public boolean isResourceRequest(FacesContext context)
    {
        // TODO: JSF 2.0 #40
        return false;
    }
}
