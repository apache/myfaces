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
package org.apache.myfaces.el.resolver;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.Location;
import org.apache.myfaces.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.resource.ContractResource;
import org.apache.myfaces.resource.ResourceELUtils;
import org.apache.myfaces.view.facelets.el.CompositeComponentELUtils;

/**
 * See Faces 2.0 spec section 5.6.1.3 and 5.6.2.4
 * 
 * @author Leonardo Uribe
 *
 */
public final class ResourceResolver extends ELResolver
{

    private static final String LIBRARY_THIS = "this";
    
    /** Creates a new instance of ResourceBundleResolver */
    public ResourceResolver()
    {
    }

    @Override
    public Class<?> getCommonPropertyType(final ELContext context,
            final Object base)
    {
        return base == null ? Object.class : null;
    }

    @Override
    public Class<?> getType(final ELContext context, final Object base,
            final Object property)
    {
        return null;
    }

    @Override
    public Object getValue(final ELContext context, final Object base,
            final Object property)
    {
        if (base != null && property != null && base instanceof ResourceHandler handler)
        {
            String reference = (String) property;
            int colonIndex = (reference).indexOf(':');
            Resource resource;
            
            if (colonIndex == -1)
            {
                // No library name, just create as a simple resource.
                
                resource = handler.createResource (reference);
            }
            
            else
            {
                String contractName = null;
                if (reference.lastIndexOf (':') != colonIndex)
                {
                    // Max of one ":" allowed, so throw an exception.
                    
                    throw new ELException ("Malformed resource reference found when " +
                        "resolving " + property);
                }
                
                else
                {
                    // Otherwise, portion before the ":" is the library name.
                    String libraryName = reference.substring (0, colonIndex);
                    FacesContext facesContext = facesContext(context);
                    
                    if (LIBRARY_THIS.equals(libraryName))
                    {
                        // note in this case we don't need to resolve to an specific 
                        // composite component, instead we need to find the libraryName of the
                        // composite component associated with the Location. For any composite component
                        // instance that is created under the same facelet it will be the same,
                        // so it is enought to get the first one matching the Location object.
                        Location location = ResourceELUtils.getResourceLocationForResolver(facesContext);
                        if (location != null)
                        {
                            // There are two options:
                            UIComponent cc = CompositeComponentELUtils.
                                    getCompositeComponentBasedOnLocation(facesContext, location);
                            Resource ccResource = (Resource) cc.getAttributes().get(
                                    Resource.COMPONENT_RESOURCE_KEY); 
                            libraryName = ccResource.getLibraryName();
                            contractName = ResourceUtils.getContractName(ccResource);
                        }
                        else
                        {
                            // Faces 2.2 "this" identifier can refer to a library or contract.
                            libraryName = ResourceELUtils.getResourceLibraryForResolver(facesContext);
                            contractName = ResourceELUtils.getResourceContractForResolver(facesContext);
                        }
                    }
                    
                    try
                    {
                        if (contractName != null)
                        {
                            facesContext.getAttributes().put(ContractResource.CONTRACT_SELECTED, contractName);
                        }
                        resource = handler.createResource(reference.substring(colonIndex+1),
                                libraryName);
                    }
                    finally
                    {
                        if (contractName != null)
                        {
                            facesContext.getAttributes().remove(ContractResource.CONTRACT_SELECTED);
                        }
                    }
                }
            }
            
            context.setPropertyResolved(true);
            if (resource != null)
            {
                return resource.getRequestPath();
            }
        }
        return null;
    }
    
    // get the FacesContext from the ELContext
    private static FacesContext facesContext(final ELContext context)
    {
        return (FacesContext)context.getContext(FacesContext.class);
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base,
            final Object property)
    {
        //Return false on all cases
        return false;
    }

    @Override
    public void setValue(final ELContext context, final Object base,
            final Object property, final Object val)
    {
        //No action takes place
    }

}
