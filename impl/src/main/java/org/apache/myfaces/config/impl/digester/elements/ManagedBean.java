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
package org.apache.myfaces.config.impl.digester.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.apache.myfaces.shared_impl.util.ClassUtils;
import org.apache.myfaces.view.facelets.el.ELText;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class ManagedBean implements org.apache.myfaces.config.element.ManagedBean
{

    private String description;
    private String name;
    private String beanClassName;
    private Class<?> beanClass;
    private String scope;
    private List<ManagedProperty> property = new ArrayList<ManagedProperty>();
    private MapEntries mapEntries;
    private ListEntries listEntries;
    private ValueExpression scopeValueExpression;


    public int getInitMode()
    {
        if (mapEntries != null) {
            return INIT_MODE_MAP;
        }
        if (listEntries != null) {
            return INIT_MODE_LIST;
        }
        if (! property.isEmpty()) {
            return INIT_MODE_PROPERTIES;
        }
        return INIT_MODE_NO_INIT;
    }



    public org.apache.myfaces.config.element.MapEntries getMapEntries()
    {
        return mapEntries;
    }


    public void setMapEntries(MapEntries mapEntries)
    {
        this.mapEntries = mapEntries;
    }


    public org.apache.myfaces.config.element.ListEntries getListEntries()
    {
        return listEntries;
    }


    public void setListEntries(ListEntries listEntries)
    {
        this.listEntries = listEntries;
    }


    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getManagedBeanName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getManagedBeanClassName()
    {
        return beanClassName;
    }


    public Class<?> getManagedBeanClass()
    {
        if (beanClassName == null)
        {
            return null;
        }
        
        if (beanClass == null)
        {
            beanClass = ClassUtils.simpleClassForName(beanClassName);
        }
        
        return beanClass;
    }


    public void setBeanClass(String beanClass)
    {
        this.beanClassName = beanClass;
    }


    public String getManagedBeanScope()
    {
        return scope;
    }


    public void setScope(String scope)
    {
        this.scope = scope;
    }


    public void addProperty(ManagedProperty value)
    {
        property.add(value);
    }


    public Collection<? extends ManagedProperty> getManagedProperties()
    {
        return property;
    }
    
    @Override
    public boolean isManagedBeanScopeValueExpression()
    {
        return (scope != null) 
                   && (scopeValueExpression != null || !ELText.isLiteral(scope));
    }
    
    @Override
    public ValueExpression getManagedBeanScopeValueExpression(FacesContext facesContext)
    {
        if (scopeValueExpression == null)
        {
            // we need to set the expected type to Object, because we have to generate a 
            // Exception text with the actual value and the actual type of the expression,
            // if the expression does not resolve to java.util.Map
            scopeValueExpression = 
                isManagedBeanScopeValueExpression()
                ? facesContext.getApplication().getExpressionFactory()
                        .createValueExpression(facesContext.getELContext(), scope, Object.class)
                : null;
        }
        return scopeValueExpression;
    }
    
}
