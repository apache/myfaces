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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.AbstractFaceletContext;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFFaceletTag(name="composite:implementation")
public class ImplementationHandler extends TagHandler
{
    //private static final Log log = LogFactory.getLog(ImplementationHandler.class);
    private static final Logger log = Logger.getLogger(ImplementationHandler.class.getName());
    
    public final static String NAME = "implementation";

    public ImplementationHandler(TagConfig config)
    {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        if (!FaceletViewDeclarationLanguage.
                isBuildingCompositeComponentMetadata(ctx.getFacesContext()))
        {
            // If this tag is found in a facelet, the compiler has trimmed all
            // tags outside this one excluding composite:interface, so "parent"
            // is a component used as value for the facet key
            // UIComponent.COMPOSITE_FACET_NAME in a composite component. 
            
            ((AbstractFaceletContext)ctx).pushCompositeComponentToStack(parent.getParent());
            
            nextHandler.apply(ctx, parent);
            
            ((AbstractFaceletContext)ctx).popCompositeComponentToStack();
        }
        else
        {
            // Register the facet UIComponent.COMPOSITE_FACET_NAME
            CompositeComponentBeanInfo beanInfo = 
                (CompositeComponentBeanInfo) parent.getAttributes()
                .get(UIComponent.BEANINFO_KEY);
            
            if (beanInfo == null)
            {
                if (log.isLoggable(Level.SEVERE))
                {
                    log.severe("Cannot found composite bean descriptor UIComponent.BEANINFO_KEY ");
                }
                return;
            }
            
            BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
            
            Map<String, PropertyDescriptor> facetPropertyDescriptorMap = 
                (Map<String, PropertyDescriptor>) beanDescriptor.getValue(UIComponent.FACETS_KEY);
        
            if (facetPropertyDescriptorMap == null)
            {
                facetPropertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
                beanDescriptor.setValue(UIComponent.FACETS_KEY, facetPropertyDescriptorMap);
            }
            
            if (!facetPropertyDescriptorMap.containsKey(UIComponent.COMPOSITE_FACET_NAME))
            {
                try
                {
                    facetPropertyDescriptorMap.put(UIComponent.COMPOSITE_FACET_NAME, 
                            new CompositeComponentPropertyDescriptor(UIComponent.COMPOSITE_FACET_NAME));
                }
                catch (IntrospectionException e)
                {
                    if (log.isLoggable(Level.SEVERE))
                    {
                        log.log(Level.SEVERE, "Cannot create PropertyDescriptor for facet ",e);
                    }
                    throw new TagException(tag,e);
                }
            }
        }
    }
}
