/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.config.impl.digester.elements;

import org.apache.myfaces.util.ClassUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class ManagedBean implements org.apache.myfaces.config.element.ManagedBean
{

    private String name;
    private String beanClassName;
    private Class beanClass;
    private String scope;
    private List property = new ArrayList();
    private MapEntries mapEntries;
    private ListEntries listEntries;


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


    public Class getManagedBeanClass()
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


    public Iterator getManagedProperties()
    {
        return property.iterator();
    }
}
