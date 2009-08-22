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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * Define the facets used by this composite component.
 * <p>
 * This tag is used inside composite:interface tag. All facets
 * should be saved under the key UIComponent.FACETS_KEY on the
 * bean descriptor map as a Map<String, PropertyDescriptor>
 * </p>
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:facet")
public class FacetHandler extends TagHandler implements InterfaceDescriptorCreator
{

    private static final Log log = LogFactory.getLog(FacetHandler.class);

    @JSFFaceletAttribute
    private final TagAttribute _name;

    @JSFFaceletAttribute
    private final TagAttribute _displayName;

    /**
     * Indicate if the attribute is required or not
     * <p>
     * Myfaces specific feature: this attribute is checked only if project stage is
     * not ProjectStage.Production when a composite component is created.
     * </p>
     */
    @JSFFaceletAttribute
    private final TagAttribute _required;

    @JSFFaceletAttribute
    private final TagAttribute _preferred;

    @JSFFaceletAttribute
    private final TagAttribute _expert;

    @JSFFaceletAttribute
    private final TagAttribute _shortDescription;
    
    /**
     * Check if the PropertyDescriptor instance created by this handler
     * can be cacheable or not. 
     */
    private boolean _cacheable;
    
    /**
     * Cached instance used by this component. Note here we have a 
     * "racy single-check". If this field is used, it is supposed 
     * the object cached by this handler is immutable, and this is
     * granted if all properties not saved as ValueExpression are
     * "literal". 
     */
    private PropertyDescriptor _propertyDescriptor; 
    
    public FacetHandler(TagConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _displayName = getAttribute("displayName");
        _required = getAttribute("required");
        _preferred = getAttribute("preferred");
        _expert = getAttribute("expert");
        _shortDescription = getAttribute("shortDescription");
        
        // We can reuse the same PropertyDescriptor only if the properties
        // that requires to be evaluated when apply (build view time)
        // occur are literal or null. Otherwise we need to create it.
        if ( (                             _name.isLiteral()             ) &&
             (_displayName == null      || _displayName.isLiteral()      ) &&
             (_preferred == null        || _preferred.isLiteral()        ) &&
             (_expert == null           || _expert.isLiteral()           ) &&
             (_shortDescription == null || _shortDescription.isLiteral() ) )
        {
            // Unfortunately its not possible to create the required 
            // PropertyDescriptor instance here, because there is no way 
            // to get a FaceletContext to create ValueExpressions. It is
            // possible to create it if we not have set all this properties:
            // targets, default, required, methodSignature and type. This prevents
            // the racy single-check.
            _cacheable = true;
            if ( _required == null)
            {
                _propertyDescriptor = _createFacetPropertyDescriptor(_name.getValue());
            }
        }
        else
        {
            _cacheable = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        CompositeComponentBeanInfo beanInfo = 
            (CompositeComponentBeanInfo) parent.getAttributes()
            .get(UIComponent.BEANINFO_KEY);
        
        if (beanInfo == null)
        {
            if (log.isErrorEnabled())
            {
                log.error("Cannot found composite bean descriptor UIComponent.BEANINFO_KEY ");
            }
            return;
        }
        
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();

        Map<String, PropertyDescriptor> facetPropertyDescriptorMap = (Map<String, PropertyDescriptor>)
            beanDescriptor.getValue(UIComponent.FACETS_KEY);
        
        if (facetPropertyDescriptorMap == null)
        {
            facetPropertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
            beanDescriptor.setValue(UIComponent.FACETS_KEY, facetPropertyDescriptorMap);
        }
        
        String facetName = _name.getValue(ctx);
        
        if (isCacheable())
        {
            if (_propertyDescriptor == null)
            {
                _propertyDescriptor = _createFacetPropertyDescriptor(facetName, ctx, parent);
            }
            facetPropertyDescriptorMap.put(facetName, _propertyDescriptor);
        }
        else
        {
            PropertyDescriptor facetDescriptor = _createFacetPropertyDescriptor(facetName, ctx, parent);
            facetPropertyDescriptorMap.put(facetName, facetDescriptor);
        }
                
        nextHandler.apply(ctx, parent);
        
    }
    
    
    /**
     * This method could be called only if it is not necessary to set the following properties:
     * targets, default, required, methodSignature and type
     * 
     * @return
     */
    private PropertyDescriptor _createFacetPropertyDescriptor(String facetName)
    {
        try
        {
            CompositeComponentPropertyDescriptor facetPropertyDescriptor = 
                new CompositeComponentPropertyDescriptor(facetName);
            
            if (_displayName != null)
            {
                facetPropertyDescriptor.setDisplayName(_displayName.getValue());
            }
            if (_preferred != null)
            {
                facetPropertyDescriptor.setPreferred(Boolean.valueOf(_preferred.getValue()));
            }
            if (_expert != null)
            {
                facetPropertyDescriptor.setExpert(Boolean.valueOf(_expert.getValue()));
            }
            if (_shortDescription != null)
            {
                facetPropertyDescriptor.setShortDescription(_shortDescription.getValue());
            }            
            return facetPropertyDescriptor;
        }
        catch (IntrospectionException e)
        {
            if (log.isErrorEnabled())
            {
                log.error("Cannot create PropertyDescriptor for attribute ",e);
            }
            throw new TagException(tag,e);
        }
    }
    
    private PropertyDescriptor _createFacetPropertyDescriptor(String facetName, FaceletContext ctx, UIComponent parent)
        throws TagException, IOException
    {
        try
        {
            CompositeComponentPropertyDescriptor facetPropertyDescriptor = 
                new CompositeComponentPropertyDescriptor(facetName);
            
            if (_displayName != null)
            {
                facetPropertyDescriptor.setDisplayName(_displayName.getValue(ctx));
            }
            if (_required != null)
            {
                facetPropertyDescriptor.setValue("required", _required.getValueExpression(ctx, Boolean.class));
            }
            if (_preferred != null)
            {
                facetPropertyDescriptor.setPreferred(_preferred.getBoolean(ctx));
            }
            if (_expert != null)
            {
                facetPropertyDescriptor.setExpert(_expert.getBoolean(ctx));
            }
            if (_shortDescription != null)
            {
                facetPropertyDescriptor.setShortDescription(_shortDescription.getValue(ctx));
            }
            return facetPropertyDescriptor;
        }
        catch (IntrospectionException e)
        {
            if (log.isErrorEnabled())
            {
                log.error("Cannot create PropertyDescriptor for attribute ",e);
            }
            throw new TagException(tag,e);
        }
    }

    public boolean isCacheable()
    {
        return _cacheable;
    }
    
    public void setCacheable(boolean cacheable)
    {
        _cacheable = cacheable;
    }

    @Override
    public FaceletHandler getNextHandler()
    {
        return nextHandler;
    }
}
