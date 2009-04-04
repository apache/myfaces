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
package javax.faces.application;

import java.net.URL;
import java.util.Set;

import javax.faces.FacesWrapper;
import javax.faces.context.FacesContext;

/**
 * TODO: REMOVE - No longer existing in latst spec 2009-03-04 -= Simon =-
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 723192 $ $Date: 2008-12-03 21:31:00 -0500 (mer., 03 déc. 2008) $
 * 
 * @deprecated
 */
@Deprecated
public abstract class DiscoveryHandlerWrapper extends DiscoveryHandler
        implements FacesWrapper<DiscoveryHandler>
{
    
    @Override
    public Set<String> getClassNamesWithFacesAnnotations(
            FacesContext facesContext)
    {
        return getWrapped().getClassNamesWithFacesAnnotations(facesContext);
    }

    @Override
    public Set<URL> getResourcePaths(String path)
    {
        return getWrapped().getResourcePaths(path);
    }

    @Override
    public void processAnnotatedClasses(FacesContext facesContext,
            Set<String> classNamesSet)
    {
        getWrapped().processAnnotatedClasses(facesContext, classNamesSet);
    }
    
    public abstract DiscoveryHandler getWrapped();
}
