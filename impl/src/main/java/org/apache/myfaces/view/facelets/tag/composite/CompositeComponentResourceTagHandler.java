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
package org.apache.myfaces.view.facelets.tag.composite;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UniqueIdVendor;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.TagException;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.jsf.ActionSourceRule;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentBuilderHandler;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.jsf.EditableValueHolderRule;
import org.apache.myfaces.view.facelets.tag.jsf.ValueHolderRule;
import org.apache.myfaces.view.facelets.tag.jsf.core.AjaxHandler;

/**
 * This handler is responsible for apply composite components. It
 * is created by CompositeResourceLibrary class when a composite component
 * is found.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class CompositeComponentResourceTagHandler extends ComponentHandler
    implements ComponentBuilderHandler
{
    /**
     * This key is used to keep the list of AttachedObjectHandlers
     * created when a composite component is created. Tag handlers
     * exposing attached objects should call 
     * addAttachedObjectHandler(UIComponent, AttachedObjectHandler)
     * that uses this key to save this list on component attribute
     * map.  
     */
    public final static String ATTACHED_OBJECT_HANDLERS_KEY = 
            "org.apache.myfaces.ATTACHED_OBJECT_HANDLERS_KEY";    
    
    private final Resource _resource;
    
    private Metadata _mapper;
    
    private Class<?> _lastType = Object.class;
    
    public CompositeComponentResourceTagHandler(ComponentConfig config, Resource resource)
    {
        super(config);
        _resource = resource;
    }

    @Override
    public UIComponent createComponent(FaceletContext ctx)
    {
        FacesContext facesContext = ctx.getFacesContext();
        UIComponent component = facesContext.getApplication().createComponent(facesContext, _resource);
        
        // Check required attributes if the app is not on production stage. 
        // Unfortunately, we can't check it on constructor because we need to call
        // ViewDeclarationLanguage.getComponentMetadata() and on that point it is possible to not
        // have a viewId.
        if (!facesContext.isProjectStage(ProjectStage.Production))
        {
            BeanInfo beanInfo = (BeanInfo) component.getAttributes().get(UIComponent.BEANINFO_KEY);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
            {
                ValueExpression ve = (ValueExpression) propertyDescriptor.getValue("required");
                if (ve != null)
                {
                    Object value = ve.getValue (facesContext.getELContext());
                    Boolean required = null;
                    
                    // FIXME: almost positive that the value expression is supposed to evaluate to a boolean, but originally
                    // the code assumed it to be a string.  Can someone verify what the right type is?
                    
                    if (value instanceof Boolean)
                    {
                        required = (Boolean) value;
                    }
                    
                    else
                    {
                        required = Boolean.valueOf((String) value);
                    }
                    
                    if (required != null && required.booleanValue())
                    {
                        Object attrValue = this.tag.getAttributes().get (propertyDescriptor.getName());
                        
                        if (attrValue == null)
                        {
                            throw new TagException(this.tag, "Attribute '" + propertyDescriptor.getName() + "' is required");
                        }
                    }
                }
            }
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c)
            throws IOException
    {
        super.applyNextHandler(ctx, c);
        
        applyCompositeComponentFacelet(ctx,c);
        
        FacesContext facesContext = ctx.getFacesContext();
        
        ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().
            getViewDeclarationLanguage(facesContext, facesContext.getViewRoot().getViewId());
        
        List<AttachedObjectHandler> handlers = (List<AttachedObjectHandler>) 
            c.getAttributes().get(ATTACHED_OBJECT_HANDLERS_KEY);
        
        if (handlers != null)
        {
            vdl.retargetAttachedObjects(facesContext, c, handlers);
            
            // Since handlers list is not serializable and it is not necessary to
            // keep them anymore on attribute map, it is better to remove it from
            // component attribute map
            c.getAttributes().remove(ATTACHED_OBJECT_HANDLERS_KEY);
        }
        
        vdl.retargetMethodExpressions(facesContext, c);
        
        if (ctx.getFacesContext().getAttributes().containsKey(
                FaceletViewDeclarationLanguage.MARK_INITIAL_STATE_KEY))
        {
            // Call it only if we are using partial state saving
            c.markInitialState();
            // Call it to other components created not bound by a tag handler
            c.getFacet(UIComponent.COMPOSITE_FACET_NAME).markInitialState();
        }
    }
    
    protected void applyCompositeComponentFacelet(FaceletContext faceletContext, UIComponent compositeComponentBase) 
        throws IOException
    {
        UIPanel compositeFacetPanel = (UIPanel)
            faceletContext.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
        compositeComponentBase.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, compositeFacetPanel);
        
        // Set an id to the created facet component, to prevent id generation and make
        // partial state saving work without problem.
        AbstractFaceletContext actx = (AbstractFaceletContext) faceletContext;
        UniqueIdVendor uniqueIdVendor = actx.getUniqueIdVendorFromStack();
        if (uniqueIdVendor == null)
        {
            uniqueIdVendor = ComponentSupport.getViewRoot(faceletContext, compositeComponentBase);
        }
        if (uniqueIdVendor != null)
        {
            // UIViewRoot implements UniqueIdVendor, so there is no need to cast to UIViewRoot
            // and call createUniqueId()
            String uid = uniqueIdVendor.createUniqueId(faceletContext.getFacesContext(),null);
            compositeFacetPanel.setId(uid);
        }
        
        // Before call applyCompositeComponent we need to add ajax behaviors
        // to the current compositeComponentBase. Note that super.applyNextHandler()
        // has already been called, but this point is before vdl.retargetAttachedObjects,
        // so we can't but this on ComponentTagHandlerDelegate, if we want this to be
        // applied correctly.
        Iterator<AjaxHandler> it = actx.getAjaxHandlers();
        if (it != null)
        {
            while(it.hasNext())
            {
                CompositeComponentResourceTagHandler.addAttachedObjectHandler(
                        compositeComponentBase, it.next());
            }
        }    
        
        VariableMapper orig = faceletContext.getVariableMapper();
        try
        {
            faceletContext.setVariableMapper(new VariableMapperWrapper(orig));
            
            actx.applyCompositeComponent(compositeFacetPanel, _resource);
        }
        finally
        {
            faceletContext.setVariableMapper(orig);
        }
    }
    
    /**
     * Add to the composite component parent this handler, so it will be processed later when
     * ViewDeclarationLanguage.retargetAttachedObjects is called (see applyNextHandler method).
     * 
     * Tag Handlers exposing attached objects should call this method to expose them when the
     * parent to be applied is a composite components.
     * 
     * @param compositeComponentParent
     * @param handler
     */
    @SuppressWarnings("unchecked")
    public static void addAttachedObjectHandler(UIComponent compositeComponentParent, AttachedObjectHandler handler)
    {
        List<AttachedObjectHandler> list = (List<AttachedObjectHandler>) 
            compositeComponentParent.getAttributes().get(
                ATTACHED_OBJECT_HANDLERS_KEY);
        
        if (list == null)
        {
            list = new ArrayList<AttachedObjectHandler>();
            compositeComponentParent.getAttributes().put(ATTACHED_OBJECT_HANDLERS_KEY, list);
        }
        
        list.add(handler);
    }

    @Override
    public void setAttributes(FaceletContext ctx, Object instance)
    {
        if (instance != null)
        {
            UIComponent component = (UIComponent) instance;

            Class<?> type = instance.getClass();
            if (_mapper == null || !_lastType.equals(type))
            {
                _lastType = type;
                BeanInfo beanInfo = (BeanInfo)((UIComponent) component).getAttributes().get(UIComponent.BEANINFO_KEY);    
                _mapper = createMetaRuleset(type , beanInfo).finish();
            }
            
            _mapper.applyMetadata(ctx, instance);
        }        
    }

    protected MetaRuleset createMetaRuleset(Class<?> type, BeanInfo beanInfo)
    {
        MetaRuleset m = new CompositeMetaRulesetImpl(this.getTag(), type, beanInfo);
        // ignore standard component attributes
        m.ignore("binding").ignore("id");

        // add auto wiring for attributes
        m.addRule(CompositeComponentRule.Instance);
        
        // add retarget method expression rules
        m.addRule(RetargetMethodExpressionRule.Instance);
        
        if (ActionSource.class.isAssignableFrom(type))
        {
            m.addRule(ActionSourceRule.Instance);
        }

        if (ValueHolder.class.isAssignableFrom(type))
        {
            m.addRule(ValueHolderRule.Instance);

            if (EditableValueHolder.class.isAssignableFrom(type))
            {
                m.ignore("submittedValue");
                m.ignore("valid");
                m.addRule(EditableValueHolderRule.Instance);
            }
        }
        
        return m;
    }
}
