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
package org.apache.myfaces.el.unified.resolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;

/**
 * See JSF 2.0 spec section 5.6.1.3 and 5.6.2.4
 * 
 * @author Leonardo Uribe
 *
 */
public final class ResourceResolver extends ELResolver
{

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
    public Iterator<FeatureDescriptor> getFeatureDescriptors(
            final ELContext context, final Object base)
    {
        return null;
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
        if (base != null && property != null && base instanceof ResourceHandler)
        {
            String reference = (String) property;
            int colonIndex = (reference).indexOf(':');
            Resource resource = null;
            if (colonIndex < 0)
            {
                if (reference.indexOf(':', colonIndex + 1) < 0
                        && colonIndex != 0
                        && colonIndex + 1 < reference.length())
                {
                    resource = ((ResourceHandler) base).createResource(
                            reference.substring(0, colonIndex - 1), reference
                                    .substring(colonIndex + 1));
                }
                else
                {
                    throw new ELException(
                            "Malformed resource reference found when resolving "
                                    + property);
                }
            }
            else
            {
                resource = ((ResourceHandler) base).createResource(reference);
                context.setPropertyResolved(true);
                return resource.getRequestPath();
            }
            if (resource != null)
            {
                context.setPropertyResolved(true);
                return resource.getRequestPath();
            }
        }
        return null;
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
