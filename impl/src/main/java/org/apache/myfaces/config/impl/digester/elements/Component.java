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


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class Component
{

    private String componentType;
    private String componentClass;


    public void setComponentType(String componentType)
    {
        this.componentType = componentType;
    }


    public void setComponentClass(String componentClass)
    {
        this.componentClass = componentClass;
    }


    public String getComponentType()
    {
        return componentType;
    }


    public String getComponentClass()
    {
        return componentClass;
    }
}
