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
import java.util.List;

import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UniqueIdVendor;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.view.BehaviorHolderAttachedObjectHandler;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.tag.composite.CompositeComponentResourceTagHandler;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

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
 * <li>f:ajax does not support binding property. In theory we should do something similar
 * to f:convertDateTime tag does: extends from ConverterHandler and override setAttributes
 * method, but in this case BehaviorTagHandlerDelegate has binding property defined, so
 * if we extend from BehaviorHandler we add binding support to f:ajax.</li>
 * <li>This tag works as a attached object handler, but note on the api there is no component
 * to define a target for a behavior. See comment inside apply() method.</li>
 * </ul>
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name = "f:ajax")
public class AjaxHandler extends TagHandler implements
        BehaviorHolderAttachedObjectHandler
{

    public final static Class<?>[] AJAX_BEHAVIOR_LISTENER_SIG = new Class<?>[] { AjaxBehaviorEvent.class };
    
    /**
     * Constant used to check if in the current build view it has been rendered the standard jsf javascript
     * library. It is necessary to remove this key from facesContext attribute map after build, to keep
     * working this code for next views to be built.
     */
    public final static String STANDARD_JSF_AJAX_LIBRARY_LOADED = "org.apache.myfaces.STANDARD_JSF_AJAX_LIBRARY_LOADED"; 

    /**
     * 
     */
    @JSFFaceletAttribute(name = "disabled", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Boolean")
    private TagAttribute _disabled;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "event", className = "javax.el.ValueExpression", deferredValueType = "java.lang.String")
    private TagAttribute _event;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "execute", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
    private TagAttribute _execute;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "immediate", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Boolean")
    private TagAttribute _immediate;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "listener", className = "javax.el.MethodExpression", deferredMethodSignature = "public void m(javax.faces.event.AjaxBehaviorEvent evt) throws javax.faces.event.AbortProcessingException")
    private TagAttribute _listener;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "onevent", className = "javax.el.ValueExpression", deferredValueType = "java.lang.String")
    private TagAttribute _onevent;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "onerror", className = "javax.el.ValueExpression", deferredValueType = "java.lang.String")
    private TagAttribute _onerror;

    /**
     * 
     */
    @JSFFaceletAttribute(name = "render", className = "javax.el.ValueExpression", deferredValueType = "java.lang.Object")
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
            // It is supposed that for composite components, this tag should
            // add itself as a target, but note that on whole api does not exists
            // some tag that expose client behaviors as targets for composite
            // components. In RI, there exists a tag called composite:clientBehavior,
            // but does not appear on spec or javadoc, maybe because this could be
            // understand as an implementation detail, after all there exists a key
            // called AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY that could be
            // used to create a tag outside jsf implementation to attach targets.
            CompositeComponentResourceTagHandler.addAttachedObjectHandler(
                    parent, this);
        }
        else
        {
            throw new TagException(this.tag,
                    "Parent is not composite component or of type ClientBehaviorHolder, type is: "
                            + parent);
        }
        
        // Register the standard ajax library on the current page in this way:
        //
        // <h:outputScript name="jsf.js" library="javax.faces" target="head"/>
        //
        // If no h:head component is in the page, we must anyway render the script inline,
        // so the only way to make sure we are doing this is add a outputScript component.
        // Note that call directly UIViewRoot.addComponentResource or use a listener 
        // does not work in this case, because check this condition will requires 
        // traverse the whole tree looking for h:head component.
        FacesContext facesContext = ctx.getFacesContext();
        if (!facesContext.getAttributes().containsKey(STANDARD_JSF_AJAX_LIBRARY_LOADED))
        {
            UIComponent outputScript = facesContext.getApplication().
                createComponent(facesContext, "javax.faces.Output", "javax.faces.resource.Script");
            outputScript.getAttributes().put("name", "jsf.js");
            outputScript.getAttributes().put("library", "javax.faces");
            outputScript.getAttributes().put("target", "head");
            
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            UniqueIdVendor uniqueIdVendor = actx.getUniqueIdVendorFromStack();
            if (uniqueIdVendor == null)
            {
                uniqueIdVendor = ComponentSupport.getViewRoot(ctx, parent);
            }
            if (uniqueIdVendor != null)
            {
                // UIViewRoot implements UniqueIdVendor, so there is no need to cast to UIViewRoot
                // and call createUniqueId()
                String uid = uniqueIdVendor.createUniqueId(ctx.getFacesContext(),null);
                outputScript.setId(uid);
            }
            
            parent.getChildren().add(outputScript);
            facesContext.getAttributes().put(STANDARD_JSF_AJAX_LIBRARY_LOADED, Boolean.TRUE);
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
        // Retrieve the current FaceletContext from FacesContext object
        FaceletContext faceletContext = (FaceletContext) context
                .getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

        // cast to a ClientBehaviorHolder
        ClientBehaviorHolder cvh = (ClientBehaviorHolder) parent;
        
        // TODO: check if the behavior could be applied to the current parent
        // For run tests it is not necessary, so we let this one pending.

        AjaxBehavior ajaxBehavior = (AjaxBehavior) context.getApplication()
                .createBehavior(AjaxBehavior.BEHAVIOR_ID);

        if (_disabled != null)
        {
            if (_disabled.isLiteral())
            {
                ajaxBehavior.setDisabled(_disabled.getBoolean(faceletContext));
            }
            else
            {
                ajaxBehavior.setValueExpression("disabled", _disabled
                        .getValueExpression(faceletContext, Boolean.class));
            }
        }
        if (_execute != null)
        {
            ajaxBehavior.setValueExpression("execute", _execute
                    .getValueExpression(faceletContext, Object.class));
        }
        if (_immediate != null)
        {
            if (_immediate.isLiteral())
            {
                ajaxBehavior
                        .setImmediate(_immediate.getBoolean(faceletContext));
            }
            else
            {
                ajaxBehavior.setValueExpression("immediate", _immediate
                        .getValueExpression(faceletContext, Boolean.class));
            }
        }
        if (_listener != null)
        {
            MethodExpression expr = _listener.getMethodExpression(
                    faceletContext, Void.TYPE, AJAX_BEHAVIOR_LISTENER_SIG);
            AjaxBehaviorListener abl = new AjaxBehaviorListenerImpl(expr);
            ajaxBehavior.addAjaxBehaviorListener(abl);
        }
        if (_onerror != null)
        {
            if (_onerror.isLiteral())
            {
                ajaxBehavior.setOnerror(_onerror.getValue(faceletContext));
            }
            else
            {
                ajaxBehavior.setValueExpression("onerror", _onerror
                        .getValueExpression(faceletContext, String.class));
            }
        }
        if (_onevent != null)
        {
            if (_onevent.isLiteral())
            {
                ajaxBehavior.setOnevent(_onevent.getValue(faceletContext));
            }
            else
            {
                ajaxBehavior.setValueExpression("onevent", _onevent
                        .getValueExpression(faceletContext, String.class));
            }
        }
        if (_render != null)
        {
            ajaxBehavior.setValueExpression("render", _render
                    .getValueExpression(faceletContext, Object.class));
        }

        String eventName = getEventName();
        if (eventName == null)
        {
            eventName = cvh.getDefaultEventName();
        }

        cvh.addClientBehavior(eventName, ajaxBehavior);
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

    /**
     * Wraps a method expression in a AjaxBehaviorListener 
     * TODO: This instance should be StateHolder or Serializable,
     * since ClientBehaviorBase implements PartialStateHolder
     *
     */
    private final static class AjaxBehaviorListenerImpl implements
            AjaxBehaviorListener
    {
        private final MethodExpression _expr;

        public AjaxBehaviorListenerImpl(MethodExpression expr)
        {
            _expr = expr;
        }

        @Override
        public void processAjaxBehavior(AjaxBehaviorEvent event)
                throws AbortProcessingException
        {
            _expr.invoke(FacesContext.getCurrentInstance().getELContext(),
                    new Object[] { event });
        }
    }
}
