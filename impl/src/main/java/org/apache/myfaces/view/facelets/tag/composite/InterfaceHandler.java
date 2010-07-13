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
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.view.Location;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.tag.TagHandlerUtils;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:interface")
public class InterfaceHandler extends TagHandler implements InterfaceDescriptorCreator
{
    private static final Logger log = Logger.getLogger(InterfaceHandler.class.getName());
    
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
     * The "hidden" flag is used to identify features that are intended only 
     * for tool use, and which should not be exposed to humans.
     */
    @JSFFaceletAttribute(name="hidden",
            className="javax.el.ValueExpression",
            deferredValueType="boolean")
    protected final TagAttribute _hidden;
    
    /**
     * Check if the BeanInfo instance created by this handler
     * can be cacheable or not. 
     */
    private boolean _cacheable;
    

    private Collection<InterfaceDescriptorCreator> attrHandlerList;
    
    public InterfaceHandler(TagConfig config)
    {
        super(config);
        _name = getAttribute("name");
        _componentType = getAttribute("componentType");
        _displayName = getAttribute("displayName");
        _preferred = getAttribute("preferred");
        _expert = getAttribute("expert");
        _shortDescription = getAttribute("shortDescription");
        _hidden = getAttribute("hidden");
        
        if (    (_name == null             || _name.isLiteral()             ) &&
                (_componentType == null    || _componentType.isLiteral()    ) &&   
                (_displayName == null      || _displayName.isLiteral()      ) &&
                (_preferred == null        || _preferred.isLiteral()        ) &&
                (_expert == null           || _expert.isLiteral()           ) &&
                (_shortDescription == null || _shortDescription.isLiteral() ) &&
                (_hidden == null           || _hidden.isLiteral()           ) )
        {
            _cacheable = true;
            // Check if all attributes are cacheable. If that so, we can cache this
            // instance, otherwise not.
            attrHandlerList = 
                TagHandlerUtils.findNextByType( nextHandler, InterfaceDescriptorCreator.class);
            for (InterfaceDescriptorCreator handler : attrHandlerList)
            {
                if (!handler.isCacheable())
                {
                    _cacheable = false;
                    break;
                }
            }
            if (!_cacheable)
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

    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        // Only apply if we are building composite component metadata,
        // in other words we are calling ViewDeclarationLanguage.getComponentMetadata
        if ( ((AbstractFaceletContext)ctx).isBuildingCompositeComponentMetadata() )
        {
            UIComponent compositeBaseParent = FaceletCompositionContext.getCurrentInstance(ctx).getCompositeComponentFromStack();
            
            CompositeComponentBeanInfo beanInfo = 
                (CompositeComponentBeanInfo) compositeBaseParent.getAttributes()
                .get(UIComponent.BEANINFO_KEY);
            
            if (beanInfo == null)
            {
                if (log.isLoggable(Level.SEVERE))
                {
                    log.severe("Cannot find composite bean descriptor UIComponent.BEANINFO_KEY ");
                }
                return;
            }            
            
            BeanDescriptor descriptor = beanInfo.getBeanDescriptor();
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
                        _componentType.getValueExpression(ctx, String.class));
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
            if (_hidden != null)
            {
                descriptor.setHidden(_hidden.getBoolean(ctx));
            }
            
            nextHandler.apply(ctx, parent);
        }
    }
    
    public boolean isCacheable()
    {
        return _cacheable;
    }

    public void setCacheable(boolean cacheable)
    {
        _cacheable = cacheable;
        for (InterfaceDescriptorCreator handler : attrHandlerList)
        {
            handler.setCacheable(cacheable);
        }
    }
    
    public Location getLocation()
    {
        return this.tag.getLocation();
    }
}
