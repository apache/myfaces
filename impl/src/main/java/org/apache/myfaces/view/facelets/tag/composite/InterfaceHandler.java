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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:interface")
public class InterfaceHandler extends TagHandler
{
    public final static String NAME = "interface";
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="name",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private final TagAttribute _name;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="componentType",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private final TagAttribute _componentType;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="displayName",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private final TagAttribute _displayName;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="preferred",
            className="javax.el.ValueExpression",
            deferredValueType="boolean")
    private final TagAttribute _preferred;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="expert",
            className="javax.el.ValueExpression",
            deferredValueType="boolean")
    private final TagAttribute _expert;
    
    /**
     * 
     */
    @JSFFaceletAttribute(name="shortDescription",
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String")
    private final TagAttribute _shortDescription;
    
    /**
     * Check if the BeanInfo instance created by this handler
     * can be cacheable or not. 
     */
    private boolean _cacheable;
    
    /**
     * Cached instance used by this component. Note here we have a 
     * "racy single-check".If this field is used, it is supposed 
     * the object cached by this handler is immutable, and this is
     * granted if all properties not saved as ValueExpression are
     * "literal". 
     **/
    private BeanInfo _cachedBeanInfo;
    
    public InterfaceHandler(TagConfig config)
    {
        super(config);
        _name = getAttribute("name");
        _componentType = getAttribute("componentType");
        _displayName = getAttribute("displayName");
        _preferred = getAttribute("preferred");
        _expert = getAttribute("expert");
        _shortDescription = getAttribute("shortDescription");
        
        if (    (_name == null             || _name.isLiteral()             ) &&
                (_componentType == null    || _componentType.isLiteral()    ) &&   
                (_displayName == null      || _displayName.isLiteral()      ) &&
                (_preferred == null        || _preferred.isLiteral()        ) &&
                (_expert == null           || _expert.isLiteral()           ) &&
                (_shortDescription == null || _shortDescription.isLiteral() ) )
        {
            _cacheable = true;
            // Check if all attributes are cacheable. If that so, we can cache this
            // instance, otherwise not.
            Collection<InterfaceDescriptorCreator> attrHandlerList = findNextByType(nextHandler);
            for (InterfaceDescriptorCreator handler : attrHandlerList)
            {
                if (!handler.isCacheable())
                {
                    _cacheable = false;
                    break;
                }
            }
            if (_cacheable)
            {
                // Disable cache on attributes because this tag is the responsible for reuse
                for (InterfaceDescriptorCreator handler : attrHandlerList)
                {
                    handler.setCacheable(false);
                }
            }
        }
        else
        {
            _cacheable = false;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        // Only apply if we are building composite component metadata,
        // in other words we are calling ViewDeclarationLanguage.getComponentMetadata
        if (FaceletViewDeclarationLanguage.
                isBuildingCompositeComponentMetadata(ctx.getFacesContext()))
        {
            UIComponent compositeBaseParent = _getCompositeBaseParent(parent);
            
            CompositeComponentBeanInfo tempBeanInfo = 
                (CompositeComponentBeanInfo) parent.getAttributes()
                .get(UIComponent.BEANINFO_KEY);
            
            if (tempBeanInfo == null)
            {
                if (_cacheable)
                {
                    if (_cachedBeanInfo == null)
                    {
                        _cachedBeanInfo = _createCompositeComponentMetadata(ctx, compositeBaseParent);
                        parent.getAttributes().put(
                                UIComponent.BEANINFO_KEY, _cachedBeanInfo);
                        nextHandler.apply(ctx, compositeBaseParent);
                    }
                    else
                    {
                        // Put the cached instance, but in that case it is not necessary to call
                        // nextHandler
                        parent.getAttributes().put(
                                UIComponent.BEANINFO_KEY, _cachedBeanInfo);
                    }
                }
                else
                {
                    tempBeanInfo = _createCompositeComponentMetadata(ctx, compositeBaseParent);
                    parent.getAttributes().put(
                            UIComponent.BEANINFO_KEY, tempBeanInfo);
                    nextHandler.apply(ctx, compositeBaseParent);
                }
            }
        }
    }
    
    /**
     * Get the base component used temporally to hold metadata
     * information generated by this handler. 
     * 
     * @param component
     * @return
     */
    private UIComponent _getCompositeBaseParent(UIComponent component)
    {
        if (!component.getAttributes().containsKey(Resource.COMPONENT_RESOURCE_KEY))
        {
            UIComponent parent = component.getParent();
            if (parent != null)
            {
                return _getCompositeBaseParent(parent);
            }
        }
        return component;
    }
    
    private CompositeComponentBeanInfo _createCompositeComponentMetadata(
            FaceletContext ctx, UIComponent parent)
    {
        BeanDescriptor descriptor = new BeanDescriptor(parent.getClass());
        CompositeComponentBeanInfo beanInfo = new CompositeComponentBeanInfo(descriptor);
        
        // Add values to descriptor according to pld javadoc
        if (_name != null)
        {
            descriptor.setName(_name.getValue(ctx));
        }
        if (_componentType != null)
        {
            // componentType is required by Application.createComponent(FacesContext, Resource)
            // to instantiate the base component for this composite component. It should be
            // as family javax.faces.NamingContainer .
            descriptor.setValue(UIComponent.COMPOSITE_COMPONENT_TYPE_KEY, 
                    _componentType.getValue(ctx));
        }
        if (_displayName != null)
        {
            descriptor.setDisplayName(_displayName.getValue(ctx));
        }
        if (_preferred != null)
        {
            descriptor.setPreferred(_preferred.getBoolean(ctx));
        }
        if (_expert != null)
        {
            descriptor.setExpert(_expert.getBoolean(ctx));
        }
        if (_shortDescription != null)
        {
            descriptor.setShortDescription(_shortDescription.getValue(ctx));
        }
        
        return beanInfo;
    }
    
    private static Collection<InterfaceDescriptorCreator> findNextByType(FaceletHandler nextHandler)
    {
        List<InterfaceDescriptorCreator> found = new ArrayList<InterfaceDescriptorCreator>();
        if (nextHandler instanceof InterfaceDescriptorCreator)
        {
            InterfaceDescriptorCreator pdc = (InterfaceDescriptorCreator)nextHandler; 
            found.add(pdc);
            found.addAll(findNextByType(pdc.getNextHandler()));
        }
        else if (nextHandler instanceof javax.faces.view.facelets.CompositeFaceletHandler)
        {
            InterfaceDescriptorCreator pdc = null;
            for (FaceletHandler handler : ((javax.faces.view.facelets.CompositeFaceletHandler)nextHandler).getHandlers())
            {
                if (handler instanceof InterfaceDescriptorCreator)
                {
                    pdc = (InterfaceDescriptorCreator) handler;
                    found.add(pdc);
                    found.addAll(findNextByType(pdc.getNextHandler()));
                }
            }
        }
        
        return found;
    }
}
