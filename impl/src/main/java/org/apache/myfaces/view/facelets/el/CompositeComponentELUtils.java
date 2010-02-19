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

import java.util.LinkedList;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.Location;

/**
 * Utility class for composite components when used in EL Expressions --> #{cc}
 * 
 * @author Jakob Korherr (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class CompositeComponentELUtils
{
    
    /**
     * The key under which the component stack is stored in the FacesContext.
     * ATTENTION: this constant is duplicate in UIComponent.
     */
    public static final String COMPONENT_STACK = "componentStack:" + UIComponent.class.getName();
    
    /**
     * The key under which the current composite component is stored in the attribute
     * map of the FacesContext.
     */
    public static final String CURRENT_COMPOSITE_COMPONENT_KEY = "org.apache.myfaces.compositecomponent.current";
    
    /**
     * The key under which the Location of the composite componente is stored
     * in the attributes map of the component by InterfaceHandler.
     */
    public static final String LOCATION_KEY = "org.apache.myfaces.compositecomponent.location";
    
    /**
     * A regular expression used to determine if cc is used in an expression String.
     */
    public static final String CC_EXPRESSION_REGEX = ".*[^\\w\\.]cc[^\\w].*";
    
    /**
     * private constructor
     */
    private CompositeComponentELUtils()
    {
        // no instantiation of this class
    }
    
    /**
     * Trys to find a composite component on the composite component stack
     * and using UIComponent.getCurrentCompositeComponent() based on the 
     * location of the facelet page that generated the composite component.
     * @param facesContext
     * @param location
     * @return
     */
    public static UIComponent getCompositeComponentBasedOnLocation(FacesContext facesContext, 
            Location location)
    {
        // look on the component stack
        LinkedList<UIComponent> componentStack = getComponentStack(facesContext);
        if (componentStack == null || componentStack.isEmpty())
        {
            // no components on the stack
            return null;
        }
        
        // try to find the right composite component
        for (UIComponent component : componentStack)
        {
            if (UIComponent.isCompositeComponent(component))
            {
                Location componentLocation = (Location) component.getAttributes().get(LOCATION_KEY);
                if (componentLocation != null 
                        && componentLocation.getPath().equals(location.getPath()))
                {
                    return component;
                }
            }
        }
        
        // try to find it using UIComponent.getCurrentCompositeComponent()
        UIComponent component = UIComponent.getCurrentCompositeComponent(facesContext);
        while (component != null)
        {
            Location componentLocation = (Location) component.getAttributes().get(LOCATION_KEY);
            if (componentLocation != null 
                    && componentLocation.getPath().equals(location.getPath()))
            {
                return component;
            }
            // get the composite component's parent
            component = UIComponent.getCompositeComponentParent(component);
        }
        
        // not found
        return null;
    }
    
    /**
     * Gets the current component stack from the FacesContext.
     * @param facesContext
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LinkedList<UIComponent> getComponentStack(FacesContext facesContext)
    {
        return (LinkedList<UIComponent>) facesContext.getAttributes().get(COMPONENT_STACK);
    }
    
    /**
     * Trys to get the composite component using getCompositeComponentBasedOnLocation()
     * and saves it in an attribute on the FacesContext, which is then used by 
     * CompositeComponentImplicitObject.
     * @param facesContext
     * @param location
     */
    public static void saveCompositeComponentForResolver(FacesContext facesContext, Location location)
    {
        UIComponent cc = getCompositeComponentBasedOnLocation(facesContext, location);
        facesContext.getAttributes().put(CURRENT_COMPOSITE_COMPONENT_KEY, cc);
    }
    
    /**
     * Removes the composite component from the attribute map of the FacesContext.
     * @param facesContext
     */
    public static void removeCompositeComponentForResolver(FacesContext facesContext)
    {
        facesContext.getAttributes().remove(CURRENT_COMPOSITE_COMPONENT_KEY);
    }
    
    /**
     * Tests if the expression refers to the current composite component: #{cc}
     * @return
     */
    public static boolean isCompositeComponentExpression(String expression)
    {
        return expression.matches(CC_EXPRESSION_REGEX);
    }
    
}
