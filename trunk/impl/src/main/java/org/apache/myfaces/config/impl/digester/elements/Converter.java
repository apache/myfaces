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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 */
public class Converter
{

    private String converterId;
    private String forClass;
    private String converterClass;
    private List _properties = null;
    private List _attributes = null;


    public String getConverterId()
    {
        return converterId;
    }


    public void setConverterId(String converterId)
    {
        this.converterId = converterId;
    }


    public String getForClass()
    {
        return forClass;
    }


    public void setForClass(String forClass)
    {
        this.forClass = forClass;
    }


    public String getConverterClass()
    {
        return converterClass;
    }


    public void setConverterClass(String converterClass)
    {
        this.converterClass = converterClass;
    }

    public void addProperty(Property value)
    {
        if(_properties==null)
            _properties = new ArrayList();

        _properties.add(value);
    }

    public Iterator getProperties()
    {
        if(_properties==null)
            return Collections.EMPTY_LIST.iterator();

        return _properties.iterator();
    }
    
    public void addAttribute(Attribute value)
    {
        if(_attributes == null)
            _attributes = new ArrayList();

        _attributes.add(value);
    }

    public Iterator getAttributes()
    {
        if(_attributes==null)
            return Collections.EMPTY_LIST.iterator();

        return _attributes.iterator();
    }
}
