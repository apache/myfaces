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
package org.apache.myfaces.view.facelets.el;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.context.FacesContext;
import javax.faces.view.Location;

/**
 * A MethodExpression that contains the original MethodExpression and
 * the Location of the facelet file from which the MethodExpression was
 * created. This is needed when the current composite component (cc) 
 * has to be resolved by the MethodExpression, because #{cc} refers to the
 * composite component which is implemented in the file the MethodExpression
 * comes from and not the one currently on top of the composite component stack.
 * 
 * This MethodExpression implementation passes through all methods to the delegate
 * MethodExpression, but saves the related composite component in a FacesContext attribute 
 * before the invocation of the method on the delegate and removes it afterwards.
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class LocationMethodExpression extends MethodExpression
{

    private static final long serialVersionUID = 1634644578979226893L;
    
    private Location location;
    private MethodExpression delegate;
    
    public LocationMethodExpression(Location location, MethodExpression delegate)
    {
        this.location = location;
        this.delegate = delegate;
    }
    
    public Location getLocation()
    {
        return location;
    }
    
    @Override
    public MethodInfo getMethodInfo(ELContext context)
    {
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        CompositeComponentELUtils.saveCompositeComponentForResolver(facesContext, location);
        try
        {
            return delegate.getMethodInfo(context);
        }
        finally
        {
            CompositeComponentELUtils.removeCompositeComponentForResolver(facesContext);
        }
    }

    @Override
    public Object invoke(ELContext context, Object[] params)
    {
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        CompositeComponentELUtils.saveCompositeComponentForResolver(facesContext, location);
        try
        {
            return delegate.invoke(context, params);
        }
        finally
        {
            CompositeComponentELUtils.removeCompositeComponentForResolver(facesContext);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return delegate.equals(obj);
    }

    @Override
    public String getExpressionString()
    {
        return delegate.getExpressionString();
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return delegate.isLiteralText();
    }

}
