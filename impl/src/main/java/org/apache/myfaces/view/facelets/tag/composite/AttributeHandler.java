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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:attribute")
public class AttributeHandler extends TagHandler
{
    
    private static final Log log = LogFactory.getLog(AttributeHandler.class);

    @JSFFaceletAttribute
    private final TagAttribute _name;
    
    @JSFFaceletAttribute
    private final TagAttribute _targets;
    
    @JSFFaceletAttribute
    private final TagAttribute _default;
    
    @JSFFaceletAttribute
    private final TagAttribute _displayName;

    @JSFFaceletAttribute
    private final TagAttribute _required;

    @JSFFaceletAttribute
    private final TagAttribute _preferred;

    @JSFFaceletAttribute
    private final TagAttribute _expert;

    @JSFFaceletAttribute
    private final TagAttribute _shortDescription;

    @JSFFaceletAttribute
    private final TagAttribute _methodSignature;

    @JSFFaceletAttribute
    private final TagAttribute _type;
    
    private boolean _cacheable;
    
    private volatile PropertyDescriptor _propertyDescriptor; 
    
    public AttributeHandler(TagConfig config)
    {
        super(config);
        _name = getRequiredAttribute("name");
        _targets = getAttribute("targets");
        _default = getAttribute("default");
        _displayName = getAttribute("displayName");
        _required = getAttribute("required");
        _preferred = getAttribute("preferred");
        _expert = getAttribute("expert");
        _shortDescription = getAttribute("shortDescription");
        _methodSignature = getAttribute("method-signature");
        _type = getAttribute("type");
        
        // We can reuse the same PropertyDescriptor only if the properties
        // that requires to be evaluated when apply occur are literal or null.
        // otherwise we need to create it.        
        if ( (_name == null             || _name.isLiteral()             ) &&
             (_displayName == null      || _displayName.isLiteral()      ) &&
             (_preferred == null        || _preferred.isLiteral()        ) &&
             (_expert == null           || _expert.isLiteral()           ) &&
             (_shortDescription == null || _shortDescription.isLiteral() ) )
        {
            _cacheable = true;
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
        
        List<PropertyDescriptor> attributeList = beanInfo.getPropertyDescriptorsList();
        
        if (isCacheable())
        {
            if (_propertyDescriptor == null)
            {
                _propertyDescriptor = _createPropertyDescriptor(ctx, parent);
            }
            attributeList.add(_propertyDescriptor);
        }
        else
        {
            PropertyDescriptor attribute = _createPropertyDescriptor(ctx, parent);
            attributeList.add(attribute);
        }
                
        nextHandler.apply(ctx, parent);
    }
    
    private PropertyDescriptor _createPropertyDescriptor(FaceletContext ctx, UIComponent parent)
        throws TagException, IOException
    {
        try
        {
            CompositeComponentPropertyDescriptor attribute = 
                new CompositeComponentPropertyDescriptor(_name.getValue(ctx));
            
            // The javadoc of ViewDeclarationLanguage.retargetMethodExpressions says that
            // 'type', 'method-signature', 'targets' should return ValueExpressions.
            if (_targets != null)
            {
                attribute.setValue("targets", _targets.getValueExpression(ctx, String.class));
            }
            if (_default != null)
            {
                attribute.setValue("default", _default.getValueExpression(ctx, String.class));
            }
            if (_displayName != null)
            {
                attribute.setDisplayName(_displayName.getValue());
            }
            if (_required != null)
            {
                attribute.setValue("required", _required.getValueExpression(ctx, Boolean.class));
            }
            if (_preferred != null)
            {
                attribute.setPreferred(_preferred.getBoolean(ctx));
            }
            if (_expert != null)
            {
                attribute.setExpert(_expert.getBoolean(ctx));
            }
            if (_shortDescription != null)
            {
                attribute.setShortDescription(_shortDescription.getValue(ctx));
            }
            if (_methodSignature != null)
            {
                attribute.setValue("method-signature", _methodSignature.getValueExpression(ctx, String.class));
            }
            if (_type != null)
            {
                attribute.setValue("type", _type.getValueExpression(ctx, String.class));
            }
            
            if (isCacheable())
            {
                attribute.setCacheable(true);
            }
            return attribute;
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

    boolean isCacheable()
    {
        return _cacheable;
    }
    
    void setCacheable(boolean cacheable)
    {
        _cacheable = cacheable;
    }

}
