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
package org.apache.myfaces.view.facelets;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

/**
 * @since 2.0.1
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 899026 $ $Date: 2010-01-13 20:47:14 -0500 (Mié, 13 Ene 2010) $
 */
abstract public class FaceletCompositionContext
{
    static protected final String FACELET_COMPOSITION_CONTEXT_KEY = "oam.facelets.FACELET_COMPOSITION_CONTEXT";

    protected FaceletCompositionContext()
    {
    }
    
    static public FaceletCompositionContext getCurrentInstance()
    {
        return (FaceletCompositionContext) FacesContext.getCurrentInstance().getAttributes().get(FACELET_COMPOSITION_CONTEXT_KEY);
    }
    
    static public FaceletCompositionContext getCurrentInstance(FaceletContext ctx)
    {
        if (ctx instanceof AbstractFaceletContext)
        {
            return ((AbstractFaceletContext)ctx).getFaceletCompositionContext();
        }
        else
        {
            // Here we have two choices: retrieve it throught ThreadLocal var
            // or use the attribute value on FacesContext, but it seems better
            // use the FacesContext attribute map.
            return (FaceletCompositionContext) ctx.getFacesContext().getAttributes().get(FACELET_COMPOSITION_CONTEXT_KEY);
        }
    }
    
    static public FaceletCompositionContext getCurrentInstance(FacesContext ctx)
    {
        return (FaceletCompositionContext) ctx.getAttributes().get(FACELET_COMPOSITION_CONTEXT_KEY);
    }

    public void init(FacesContext facesContext)
    {
        facesContext.getAttributes().put(
                FaceletCompositionContext.FACELET_COMPOSITION_CONTEXT_KEY, this);
    }

    /**
     * Releases the MyFaceletContext object.  This method must only
     * be called by the code that created the MyFaceletContext.
     */
    public void release(FacesContext facesContext)
    {
        facesContext.getAttributes().remove(FACELET_COMPOSITION_CONTEXT_KEY);
    }
    
    public abstract FaceletFactory getFaceletFactory();
    
    /**
     * Return the composite component being applied on the current facelet. 
     * 
     * Note this is different to UIComponent.getCurrentCompositeComponent, because a composite
     * component is added to the stack each time a composite:implementation tag handler is applied.
     * 
     * This could be used by InsertChildrenHandler and InsertFacetHandler to retrieve the current
     * composite component to be applied.
     * 
     * @since 2.0
     * @param facesContext
     * @return
     */
    public abstract UIComponent getCompositeComponentFromStack();

    /**
     * @since 2.0
     * @param parent
     */
    public abstract void pushCompositeComponentToStack(UIComponent parent);

    /**
     * @since 2.0
     */
    public abstract void popCompositeComponentToStack();
    
    /**
     * Return the latest UniqueIdVendor created from stack. The reason why we need to keep
     * a UniqueIdVendor stack is because we need to look the closest one in ComponentTagHandlerDelegate.
     * Note that facelets tree is built from leafs to root, that means use UIComponent.getParent() does not
     * always return parent components.
     * 
     * @since 2.0
     * @return
     */
    public abstract UniqueIdVendor getUniqueIdVendorFromStack();

    /**
     * @since 2.0
     * @param parent
     */
    public abstract void pushUniqueIdVendorToStack(UniqueIdVendor parent);

    /**
     * @since 2.0
     */
    public abstract void popUniqueIdVendorToStack();
    
    /**
     * Gets the top of the validationGroups stack.
     * @return
     * @since 2.0
     */
    public abstract String getFirstValidationGroupFromStack();
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    public abstract void popValidationGroupsToStack();
    
    /**
     * Pushes validationGroups to the stack.
     * @param validationGroups
     * @since 2.0
     */
    public abstract void pushValidationGroupsToStack(String validationGroups);
    
    /**
     * Gets all validationIds on the stack.
     * @return
     * @since 2.0
     */
    public abstract Iterator<String> getExcludedValidatorIds();
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    public abstract void popExcludedValidatorIdToStack();
    
    /**
     * Pushes validatorId to the stack of excluded validatorIds.
     * @param validatorId
     * @since 2.0
     */
    public abstract void pushExcludedValidatorIdToStack(String validatorId);
    
    /**
     * Gets all validationIds on the stack.
     * @return
     * @since 2.0
     */
    public abstract Iterator<String> getEnclosingValidatorIds();
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    public abstract void popEnclosingValidatorIdToStack();
    
    /**
     * Pushes validatorId to the stack of all enclosing validatorIds.
     * @param validatorId
     * @since 2.0
     */
    public abstract void pushEnclosingValidatorIdToStack(String validatorId);
    
    /**
     * Check if this build is being refreshed, adding transient components
     * and adding/removing components under c:if or c:forEach or not.
     * 
     * @return
     * @since 2.0
     */
    public abstract boolean isRefreshingTransientBuild();
    
    /**
     * Check if this build should be marked as initial state. In other words,
     * all components must call UIComponent.markInitialState.
     * 
     * @return
     * @since 2.0
     */
    public abstract boolean isMarkInitialState();
    
    /**
     * Check if the current view will be refreshed with partial state saving.
     * 
     * This param is used in two posible events:
     * 
     * 1. To notify UIInstruction instances to look for instances moved by
     *    cc:insertChildren or cc:insertFacet.
     * 2. To do proper actions when a tag that could change tree structure is applied
     *    (c:if, c:forEach...)
     * 
     * @return
     * @since 2.0
     */
    public abstract boolean isRefreshTransientBuildOnPSS();
    
    /**
     * Check if we are using partial state saving on this view
     * 
     * @return
     * @since 2.0
     */
    public abstract boolean isUsingPSSOnThisView();
    
    public abstract boolean isMarkInitialStateAndIsRefreshTransientBuildOnPSS();
    
}
