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
package org.apache.myfaces.view.facelets.tag.jsf.core;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.view.BehaviorHolderAttachedObjectHandler;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.tag.composite.CompositeComponentResourceTagHandler;

/**
 * This tag creates an instance of AjaxBehavior, and associates it with the nearest 
 * parent UIComponent that implements ClientBehaviorHolder interface. This tag can
 * be used on single or composite components.
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * </p>
 * <p>
 * According to the documentation, the tag handler implementing this tag should meet
 * the following conditions:  
 * </p>
 * <ul>
 * <li>Since this tag attach objects to UIComponent instances, and those instances 
 * implements Behavior interface, this component should implement 
 * BehaviorHolderAttachedObjectHandler interface.</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="f:ajax")
public class AjaxHandler extends TagHandler implements BehaviorHolderAttachedObjectHandler
{

    /**
     * 
     */
    @JSFFaceletAttribute(name="disabled",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.Boolean")
    private TagAttribute _disabled;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="event",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private TagAttribute _event;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="execute",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.Object")
    private TagAttribute _execute;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="immediate",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.Boolean")
    private TagAttribute _immediate;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="listener",
            className="javax.el.MethodExpression",
            deferredMethodSignature=
"public void m(javax.faces.event.AjaxBehaviorEvent evt) throws javax.faces.event.AbortProcessingException")
    private TagAttribute _listener;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="onevent",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private TagAttribute _onevent;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="onerror",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private TagAttribute _onerror;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="render",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.Object")
    private TagAttribute _render;
    
    public AjaxHandler(TagConfig config)
    {
        super(config);
        _disabled = getAttribute("disabled");
        _event = getAttribute("event");
        _execute = getAttribute("execute");
        _immediate = getAttribute("immediate");
        _listener = getAttribute("listener");
        _onerror = getAttribute("onerror");
        _onevent = getAttribute("onevent");
        _render = getAttribute("render");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        //Apply only if we are creating a new component
        if (!ComponentHandler.isNew(parent))
        {
            return;
        }
        if (parent instanceof ClientBehaviorHolder)
        {
            applyAttachedObject(ctx.getFacesContext(), parent);
        }
        else if (UIComponent.isCompositeComponent(parent))
        {
            CompositeComponentResourceTagHandler.addAttachedObjectHandler(parent, this);
        }
        else
        {
            throw new TagException(this.tag, "Parent is not composite component or of type ClientBehaviorHolder, type is: " + parent);
        }
    }

    /**
     * ViewDeclarationLanguage.retargetAttachedObjects uses it to check
     * if the the target to be processed is applicable for this handler
     */
    @Override
    public String getEventName()
    {
        if (_event == null)
        {
            return null;
        }
        else
        {
            return _event.getValue();
        }
    }

    /**
     * This method should create an AjaxBehavior object and attach it to the
     * parent component.
     * 
     * Also, it should check if the parent can apply the selected AjaxBehavior
     * to the selected component through ClientBehaviorHolder.getEventNames() or
     * ClientBehaviorHolder.getDefaultEventName()
     */
    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent)
    {
        // TODO Auto-generated method stub
    }

    /**
     * The documentation says this attribute should not be used since it is not
     * taken into account. Instead, getEventName is used on 
     * ViewDeclarationLanguage.retargetAttachedObjects.
     */
    @Override
    public String getFor()
    {
        return null;
    }
}
