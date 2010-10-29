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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.view.AttachedObjectHandler;
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
     * @since 2.0.1
     * @return
     */
    public abstract UIComponent getCompositeComponentFromStack();

    /**
     * @since 2.0.1
     * @param parent
     */
    public abstract void pushCompositeComponentToStack(UIComponent parent);

    /**
     * @since 2.0.1
     */
    public abstract void popCompositeComponentToStack();
    
    /**
     * Return the latest UniqueIdVendor created from stack. The reason why we need to keep
     * a UniqueIdVendor stack is because we need to look the closest one in ComponentTagHandlerDelegate.
     * Note that facelets tree is built from leafs to root, that means use UIComponent.getParent() does not
     * always return parent components.
     * 
     * @since 2.0.1
     * @return
     */
    public abstract UniqueIdVendor getUniqueIdVendorFromStack();

    /**
     * @since 2.0.1
     * @param parent
     */
    public abstract void pushUniqueIdVendorToStack(UniqueIdVendor parent);

    /**
     * @since 2.0.1
     */
    public abstract void popUniqueIdVendorToStack();
    
    /**
     * Gets the top of the validationGroups stack.
     * @return
     * @since 2.0.1
     */
    public abstract String getFirstValidationGroupFromStack();
    
    /**
     * Removes top of stack.
     * @since 2.0.1
     */
    public abstract void popValidationGroupsToStack();
    
    /**
     * Pushes validationGroups to the stack.
     * @param validationGroups
     * @since 2.0.1
     */
    public abstract void pushValidationGroupsToStack(String validationGroups);
    
    /**
     * Gets all validationIds on the stack.
     * @return
     * @since 2.0.1
     */
    public abstract Iterator<String> getExcludedValidatorIds();
    
    /**
     * Removes top of stack.
     * @since 2.0.1
     */
    public abstract void popExcludedValidatorIdToStack();
    
    /**
     * Pushes validatorId to the stack of excluded validatorIds.
     * @param validatorId
     * @since 2.0.1
     */
    public abstract void pushExcludedValidatorIdToStack(String validatorId);
    
    /**
     * Gets all validationIds on the stack.
     * @return
     * @since 2.0.1
     */
    public abstract Iterator<String> getEnclosingValidatorIds();
    
    /**
     * Removes top of stack.
     * @since 2.0.1
     */
    public abstract void popEnclosingValidatorIdToStack();
    
    /**
     * Pushes validatorId to the stack of all enclosing validatorIds.
     * @param validatorId
     * @since 2.0.1
     */
    public abstract void pushEnclosingValidatorIdToStack(String validatorId);
    
    /**
     * Check if this build is being refreshed, adding transient components
     * and adding/removing components under c:if or c:forEach or not.
     * 
     * @return
     * @since 2.0.1
     */
    public abstract boolean isRefreshingTransientBuild();
    
    /**
     * Check if this build should be marked as initial state. In other words,
     * all components must call UIComponent.markInitialState.
     * 
     * @return
     * @since 2.0.1
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
     * @since 2.0.1
     */
    public abstract boolean isRefreshTransientBuildOnPSS();
    
    /**
     * Check if we are using partial state saving on this view
     * 
     * @return
     * @since 2.0.1
     */
    public abstract boolean isUsingPSSOnThisView();
    
    /**
     * @since 2.0.1
     * @return
     */
    public abstract boolean isMarkInitialStateAndIsRefreshTransientBuildOnPSS();

    /**
     * Add to the composite component parent this handler, so it will be processed later when
     * ViewDeclarationLanguage.retargetAttachedObjects is called.
     *
     * Tag Handlers exposing attached objects should call this method to expose them when the
     * parent to be applied is a composite components.
     *
     * @since 2.0.2
     * @param compositeComponentParent
     * @param handler
     */
    public abstract void addAttachedObjectHandler(UIComponent compositeComponentParent, AttachedObjectHandler handler);

    /**
     * Remove from the composite component parent the list of attached handlers.
     * 
     * @since 2.0.2
     * @param compositeComponentParent
     */
    public abstract void removeAttachedObjectHandlers(UIComponent compositeComponentParent);

    /**
     * Retrieve the list of object handlers attached to a composite component parent. 
     * 
     * @since 2.0.2
     * @param compositeComponentParent
     */
    public abstract List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent compositeComponentParent);

    /**
     * Marks all direct children and Facets with an attribute for deletion.
     *
     * @since 2.0.2
     * @see #finalizeForDeletion(FaceletCompositionContext, UIComponent)
     * @param component
     *            UIComponent to mark
     */
    public abstract void markForDeletion(UIComponent component);
    
    /**
     * Used in conjunction with markForDeletion where any UIComponent marked will be removed.
     * 
     * @since 2.0.2
     * @param component
     *            UIComponent to finalize
     */
    public abstract void finalizeForDeletion(UIComponent component);

    /**
     * Add a method expression as targeted for the provided composite component
     * 
     * @since 2.0.3
     * @param compositeComponentParent
     * @param attributeName
     * @param backingValue A value that could be useful to revert its effects.
     */
    public abstract void addMethodExpressionTargeted(UIComponent targetedComponent, String attributeName, Object backingValue);

    /**
     * Check if the MethodExpression attribute has been applied using vdl.retargetMethodExpression 
     * 
     * @since 2.0.3
     * @param compositeComponentParent
     * @param attributeName
     * @return
     */
    public abstract boolean isMethodExpressionAttributeApplied(UIComponent compositeComponentParent, String attributeName);
    
    /**
     * Mark the MethodExpression attribute as applied using vdl.retargetMethodExpression
     * 
     * @since 2.0.3
     * @param compositeComponentParent
     * @param attributeName
     */
    public abstract void markMethodExpressionAttribute(UIComponent compositeComponentParent, String attributeName);
    
    /**
     * Clear the MethodExpression attribute to call vdl.retargetMethodExpression again
     * 
     * @since 2.0.3
     * @param compositeComponentParent
     * @param attributeName
     */
    public abstract void clearMethodExpressionAttribute(UIComponent compositeComponentParent, String attributeName);
    
    /**
     * Remove a method expression as targeted for the provided composite component
     * 
     * @since 2.0.3
     * @param compositeComponentParent
     * @param attributeName
     * @return A value that could be useful to revert its effects.
     */
    public abstract Object removeMethodExpressionTargeted(UIComponent targetedComponent, String attributeName);

}
