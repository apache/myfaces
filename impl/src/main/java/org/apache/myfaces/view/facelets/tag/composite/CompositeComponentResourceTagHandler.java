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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UniqueIdVendor;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TextHandler;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.TemplateClient;
import org.apache.myfaces.view.facelets.TemplateContext;
import org.apache.myfaces.view.facelets.el.VariableMapperWrapper;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;
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
    implements ComponentBuilderHandler, TemplateClient
{
    private final Resource _resource;
    
    private Metadata _mapper;
    
    private Class<?> _lastType = Object.class;
    
    protected volatile Map<String, FaceletHandler> _facetHandlersMap;
    
    protected final Collection<FaceletHandler> _componentHandlers;
    
    protected final Collection<FaceletHandler> _facetHandlers;
    
    public CompositeComponentResourceTagHandler(ComponentConfig config, Resource resource)
    {
        super(config);
        _resource = resource;
        _facetHandlers = TagHandlerUtils.findNextByType(nextHandler, javax.faces.view.facelets.FacetHandler.class,
                                                        InsertFacetHandler.class);
        _componentHandlers = TagHandlerUtils.findNextByType(nextHandler,
                javax.faces.view.facelets.ComponentHandler.class,
                ComponentContainerHandler.class, TextHandler.class);
    }

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
                    if (value instanceof Boolean)
                    {
                        required = (Boolean) value;
                    }
                    else
                    {
                        required = Boolean.getBoolean(value.toString());
                    } 
                    
                    if (required != null && required.booleanValue())
                    {
                        Object attrValue = this.tag.getAttributes().get (propertyDescriptor.getName());
                        
                        if (attrValue == null)
                        {
                            throw new TagException(this.tag, "Attribute '" + propertyDescriptor.getName()
                                                             + "' is required");
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
        //super.applyNextHandler(ctx, c);
        
        applyNextHandlerIfNotApplied(ctx, c);
        
        applyCompositeComponentFacelet(ctx,c);
        
        if (ComponentHandler.isNew(c))
        {
            FacesContext facesContext = ctx.getFacesContext();
            
            ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().
                getViewDeclarationLanguage(facesContext, facesContext.getViewRoot().getViewId());

            FaceletCompositionContext mctx = FaceletCompositionContext.getCurrentInstance(ctx);
            List<AttachedObjectHandler> handlers = mctx.getAttachedObjectHandlers(c);
            
            if (handlers != null)
            {
                vdl.retargetAttachedObjects(facesContext, c, handlers);
                
                // remove the list of handlers, as it is no longer necessary
                mctx.removeAttachedObjectHandlers(c);
            }
            
            vdl.retargetMethodExpressions(facesContext, c);
            
            if ( FaceletCompositionContext.getCurrentInstance(ctx).isMarkInitialState())
            {
                // Call it only if we are using partial state saving
                c.markInitialState();
                // Call it to other components created not bound by a tag handler
                c.getFacet(UIComponent.COMPOSITE_FACET_NAME).markInitialState();
            }            
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void applyNextHandlerIfNotApplied(FaceletContext ctx, UIComponent c)
        throws IOException
    {
        //Apply all facelets not applied yet.
        
        CompositeComponentBeanInfo beanInfo = 
            (CompositeComponentBeanInfo) c.getAttributes().get(UIComponent.BEANINFO_KEY);
        
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();

        boolean insertChildrenUsed = (beanDescriptor.getValue(InsertChildrenHandler.INSERT_CHILDREN_USED) != null);
        
        List<String> insertFacetList = (List<String>) beanDescriptor.getValue(InsertFacetHandler.INSERT_FACET_USED);
        
        if (nextHandler instanceof javax.faces.view.facelets.CompositeFaceletHandler)
        {
            for (FaceletHandler handler :
                    ((javax.faces.view.facelets.CompositeFaceletHandler)nextHandler).getHandlers())
            {
                if (handler instanceof javax.faces.view.facelets.FacetHandler)
                {
                    if (insertFacetList == null ||
                        !insertFacetList.contains(((javax.faces.view.facelets.FacetHandler)handler).getFacetName(ctx)))
                    {
                        handler.apply(ctx, c);
                    }
                }
                else if (handler instanceof InsertFacetHandler)
                {
                    if (insertFacetList == null ||
                        !insertFacetList.contains( ((InsertFacetHandler)handler).getFacetName(ctx)))
                    {
                        handler.apply(ctx, c);
                    }
                }
                else if (insertChildrenUsed)
                {
                    if (!(handler instanceof javax.faces.view.facelets.ComponentHandler ||
                            handler instanceof ComponentContainerHandler ||
                            handler instanceof TextHandler))
                    {
                        handler.apply(ctx, c);
                    }
                }
                else
                {
                    handler.apply(ctx, c);
                }
            }
        }
        else
        {
            if (nextHandler instanceof javax.faces.view.facelets.FacetHandler)
            {
                if (insertFacetList == null ||
                    !insertFacetList.contains(((javax.faces.view.facelets.FacetHandler)nextHandler).getFacetName(ctx)))
                {
                    nextHandler.apply(ctx, c);
                }
            }
            else if (nextHandler instanceof InsertFacetHandler)
            {
                if (insertFacetList == null ||
                    !insertFacetList.contains( ((InsertFacetHandler)nextHandler).getFacetName(ctx)) )
                {
                    nextHandler.apply(ctx, c);
                }
            }
            else if (insertChildrenUsed)
            {
                if (!(nextHandler instanceof javax.faces.view.facelets.ComponentHandler ||
                      nextHandler instanceof ComponentContainerHandler ||
                      nextHandler instanceof TextHandler))
                {
                    nextHandler.apply(ctx, c);
                }
            }
            else
            {
                nextHandler.apply(ctx, c);
            }
        }
        
        //Check for required facets
        Map<String, PropertyDescriptor> facetPropertyDescriptorMap = (Map<String, PropertyDescriptor>)
            beanDescriptor.getValue(UIComponent.FACETS_KEY);
        
        if (facetPropertyDescriptorMap != null)
        {
            List<String> facetsRequiredNotFound = null;
            for (Map.Entry<String, PropertyDescriptor> entry : facetPropertyDescriptorMap.entrySet())
            {
                ValueExpression requiredExpr = (ValueExpression) entry.getValue().getValue("required");
                if (requiredExpr != null)
                {
                    Boolean required = (Boolean) requiredExpr.getValue(ctx.getFacesContext().getELContext());
                    if (Boolean.TRUE.equals(required))
                    {
                        initFacetHandlersMap(ctx);
                        if (!_facetHandlersMap.containsKey(entry.getKey()))
                        {
                            if (facetsRequiredNotFound == null)
                            {
                                facetsRequiredNotFound = new ArrayList(facetPropertyDescriptorMap.size());
                            }
                            facetsRequiredNotFound.add(entry.getKey());
                        }
                        
                    }
                }
            }
            if (facetsRequiredNotFound != null && !facetsRequiredNotFound.isEmpty())
            {
                throw new TagException(getTag(), "The following facets are required by the component: "
                                                 + facetsRequiredNotFound);
            }
        }
    }
    
    protected void applyCompositeComponentFacelet(FaceletContext faceletContext, UIComponent compositeComponentBase) 
        throws IOException
    {
        FaceletCompositionContext mctx = FaceletCompositionContext.getCurrentInstance(faceletContext);
        AbstractFaceletContext actx = (AbstractFaceletContext) faceletContext;
        UIPanel compositeFacetPanel
                = (UIPanel) compositeComponentBase.getFacets().get(UIComponent.COMPOSITE_FACET_NAME);
        if (compositeFacetPanel == null)
        {
            compositeFacetPanel = (UIPanel)
                faceletContext.getFacesContext().getApplication().createComponent(
                    faceletContext.getFacesContext(), UIPanel.COMPONENT_TYPE, null);
            compositeFacetPanel.getAttributes().put(ComponentSupport.COMPONENT_ADDED_BY_HANDLER_MARKER,
                    Boolean.TRUE);
            compositeComponentBase.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, compositeFacetPanel);
            
            // Set an id to the created facet component, to prevent id generation and make
            // partial state saving work without problem.
            UniqueIdVendor uniqueIdVendor = mctx.getUniqueIdVendorFromStack();
            if (uniqueIdVendor == null)
            {
                uniqueIdVendor = ComponentSupport.getViewRoot(faceletContext, compositeComponentBase);
            }
            if (uniqueIdVendor != null)
            {
                // UIViewRoot implements UniqueIdVendor, so there is no need to cast to UIViewRoot
                // and call createUniqueId()
                String uid = uniqueIdVendor.createUniqueId(faceletContext.getFacesContext(),
                        mctx.getSharedStringBuilder()
                        .append(compositeComponentBase.getId())
                        .append("__f_")
                        .append("cc_facet").toString());
                compositeFacetPanel.setId(uid);
            }            
        }
        
        // Before call applyCompositeComponent we need to add ajax behaviors
        // to the current compositeComponentBase. Note that super.applyNextHandler()
        // has already been called, but this point is before vdl.retargetAttachedObjects,
        // so we can't but this on ComponentTagHandlerDelegate, if we want this to be
        // applied correctly.
        Iterator<AjaxHandler> it = ((AbstractFaceletContext) faceletContext).getAjaxHandlers();
        if (it != null)
        {
            while(it.hasNext())
            {
                mctx.addAttachedObjectHandler(
                        compositeComponentBase, it.next());
            }
        }    
        
        VariableMapper orig = faceletContext.getVariableMapper();
        try
        {
            faceletContext.setVariableMapper(new VariableMapperWrapper(orig));
            actx.pushCompositeComponentClient(this);
            Resource resourceForCurrentView = faceletContext.getFacesContext().getApplication().
                getResourceHandler().createResource(_resource.getResourceName(), _resource.getLibraryName());
            if (resourceForCurrentView != null)
            {
                //Wrap it for serialization.
                resourceForCurrentView = new CompositeResouceWrapper(resourceForCurrentView);
            }
            else
            {
                //If a resource cannot be resolved it means a default for the current 
                //composite component does not exists.
                throw new TagException(getTag(), "Composite Component " + getTag().getQName() 
                        + " requires a default instance that can be found by the installed ResourceHandler.");
            }
            actx.applyCompositeComponent(compositeFacetPanel, resourceForCurrentView);
        }
        finally
        {
            actx.popCompositeComponentClient();
            faceletContext.setVariableMapper(orig);
        }
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
                BeanInfo beanInfo = (BeanInfo)component.getAttributes().get(UIComponent.BEANINFO_KEY);
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
        m.addRule(CompositeComponentRule.INSTANCE);
        
        // add retarget method expression rules
        m.addRule(RetargetMethodExpressionRule.INSTANCE);
        
        if (ActionSource.class.isAssignableFrom(type))
        {
            m.addRule(ActionSourceRule.INSTANCE);
        }

        if (ValueHolder.class.isAssignableFrom(type))
        {
            m.addRule(ValueHolderRule.INSTANCE);

            if (EditableValueHolder.class.isAssignableFrom(type))
            {
                m.ignore("submittedValue");
                m.ignore("valid");
                m.addRule(EditableValueHolderRule.INSTANCE);
            }
        }
        
        return m;
    }
    
    private void initFacetHandlersMap(FaceletContext ctx)
    {
        if (_facetHandlersMap == null)
        {
            Map<String, FaceletHandler> map = new HashMap<String, FaceletHandler>();
            
            for (FaceletHandler handler : _facetHandlers)
            {
                if (handler instanceof javax.faces.view.facelets.FacetHandler )
                {
                    map.put( ((javax.faces.view.facelets.FacetHandler)handler).getFacetName(ctx), handler);
                }
                else if (handler instanceof InsertFacetHandler)
                {
                    map.put( ((InsertFacetHandler)handler).getFacetName(ctx), handler);
                }
            }
            _facetHandlersMap = map;
        }
    }
    
    public boolean apply(FaceletContext ctx, UIComponent parent, String name)
            throws IOException, FacesException, FaceletException, ELException
    {        
        if (name != null)
        {
            //1. Initialize map used to retrieve facets
            if (_facetHandlers == null || _facetHandlers.isEmpty())
            {
                checkFacetRequired(ctx, parent, name);
                return true;
            }

            initFacetHandlersMap(ctx);

            FaceletHandler handler = _facetHandlersMap.get(name);

            if (handler != null)
            {
                AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
                // Pop the current composite component on stack, so #{cc} references
                // can be resolved correctly, because they are relative to the page 
                // that define it.
                FaceletCompositionContext fcc = actx.getFaceletCompositionContext(); 
                UIComponent innerCompositeComponent = fcc.getCompositeComponentFromStack(); 
                fcc.popCompositeComponentToStack();
                // Pop the template context, so ui:xx tags and nested composite component
                // cases could work correctly 
                TemplateContext itc = actx.popTemplateContext();
                try
                {
                    handler.apply(ctx, parent);
                }
                finally
                {
                    actx.pushTemplateContext(itc);
                    fcc.pushCompositeComponentToStack(innerCompositeComponent);
                }
                return true;
                
            }
            else
            {
                checkFacetRequired(ctx, parent, name);
                return true;
            }
        }
        else
        {
            AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
            // Pop the current composite component on stack, so #{cc} references
            // can be resolved correctly, because they are relative to the page 
            // that define it.
            FaceletCompositionContext fcc = actx.getFaceletCompositionContext(); 
            UIComponent innerCompositeComponent = fcc.getCompositeComponentFromStack(); 
            fcc.popCompositeComponentToStack();
            // Pop the template context, so ui:xx tags and nested composite component
            // cases could work correctly 
            TemplateContext itc = actx.popTemplateContext();
            try
            {
                for (FaceletHandler handler : _componentHandlers)
                {
                    handler.apply(ctx, parent);
                }
            }
            finally
            {
                actx.pushTemplateContext(itc);
                fcc.pushCompositeComponentToStack(innerCompositeComponent);
            }
            return true;
        }
    }
    
    private void checkFacetRequired(FaceletContext ctx, UIComponent parent, String name)
    {
        AbstractFaceletContext actx = (AbstractFaceletContext) ctx;
        FaceletCompositionContext fcc = actx.getFaceletCompositionContext(); 
        UIComponent innerCompositeComponent = fcc.getCompositeComponentFromStack();
        
        CompositeComponentBeanInfo beanInfo = 
            (CompositeComponentBeanInfo) innerCompositeComponent.getAttributes()
            .get(UIComponent.BEANINFO_KEY);
        
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        
        Map<String, PropertyDescriptor> insertFacetPropertyDescriptorMap = (Map<String, PropertyDescriptor>)
            beanDescriptor.getValue(InsertFacetHandler.INSERT_FACET_KEYS);

        if (insertFacetPropertyDescriptorMap != null && insertFacetPropertyDescriptorMap.containsKey(name))
        {
            ValueExpression requiredExpr
                    = (ValueExpression) insertFacetPropertyDescriptorMap.get(name).getValue("required");
            
            if (requiredExpr != null &&
                Boolean.TRUE.equals(requiredExpr.getValue(ctx.getFacesContext().getELContext())))
            {
                //Insert facet associated is required, but it was not applied.
                throw new TagException(this.tag, "Cannot find facet with name '"+name+"' in composite component");
            }
        }
    }
}
