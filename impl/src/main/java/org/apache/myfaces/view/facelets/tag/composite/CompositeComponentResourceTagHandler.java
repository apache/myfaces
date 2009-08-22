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

import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
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
import org.apache.myfaces.view.facelets.tag.jsf.EditableValueHolderRule;
import org.apache.myfaces.view.facelets.tag.jsf.ValueHolderRule;

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
                    Boolean required = Boolean.valueOf((String)ve.getValue(facesContext.getELContext()));
                    if (required != null && required.booleanValue())
                    {
                        throw new TagException(this.tag, "Attribute '" + propertyDescriptor.getName() + "' is required");
                    }
                }
            }
        }
        return component;
    }

    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c)
            throws IOException
    {
        super.applyNextHandler(ctx, c);
        
        applyCompositeComponentFacelet(ctx,c);
        
        FacesContext facesContext = ctx.getFacesContext();
        
        ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().
            getViewDeclarationLanguage(facesContext, facesContext.getViewRoot().getViewId());
        
        // TODO: This method should be called from here, but we have to retrieve
        // its handlers from somewhere.
        //vdl.retargetAttachedObjects(facesContext, c, handlers)
        
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
        
        VariableMapper orig = faceletContext.getVariableMapper();
        AbstractFaceletContext actx = (AbstractFaceletContext) faceletContext;
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
