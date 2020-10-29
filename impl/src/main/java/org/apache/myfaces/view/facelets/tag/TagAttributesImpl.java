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
package org.apache.myfaces.view.facelets.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributes;

/**
 * A set of TagAttributes, usually representing all attributes on a Tag.
 *
 * See org.apache.myfaces.view.facelets.tag.Tag
 * See org.apache.myfaces.view.facelets.tag.TagAttributeImpl
 * @author Jacob Hookom
 * @version $Id$
 */
public final class TagAttributesImpl extends TagAttributes
{
    private final static TagAttribute[] EMPTY = new TagAttribute[0];

    private final TagAttribute[] attributes;
    private final String[] namespaces;
    private final HashMap<String, TagAttribute[]> namespaceAttributes;
    private final HashMap<String, Map<String, TagAttribute>> namespaceLocalNameAttributes;

    public TagAttributesImpl(TagAttribute[] attributes)
    {
        this.attributes = attributes;
        this.namespaceAttributes = new HashMap<>(1, 1f);
        this.namespaceLocalNameAttributes = new HashMap<>(1, 1f);
        
        Set<String> namespacesSet = new HashSet<>();
        HashMap<String, List<TagAttribute>> namespaceAttributesAsList = new HashMap<>();
        
        for (TagAttribute attribute : attributes)
        {
            namespacesSet.add(attribute.getNamespace());

            List<TagAttribute> tagAttributes = namespaceAttributesAsList.computeIfAbsent(attribute.getNamespace(),
                    k -> new ArrayList<>(attributes.length));
            tagAttributes.add(attribute);
            
            Map<String, TagAttribute> localeNameAttributes = namespaceLocalNameAttributes.computeIfAbsent(
                    attribute.getNamespace(), k -> new HashMap<>(attributes.length));
            localeNameAttributes.put(attribute.getLocalName(), attribute);
        }


        this.namespaces = namespacesSet.toArray(new String[namespacesSet.size()]);
        Arrays.sort(this.namespaces);
        
        for (Map.Entry<String, List<TagAttribute>> entry : namespaceAttributesAsList.entrySet())
        {
            String key = entry.getKey();
            List<TagAttribute> value = entry.getValue();
            this.namespaceAttributes.put(key, value.toArray(new TagAttribute[value.size()]));
        }
    }

    /**
     * Return an array of all TagAttributes in this set
     * 
     * @return a non-null array of TagAttributes
     */
    @Override
    public TagAttribute[] getAll()
    {
        return attributes;
    }

    /**
     * Using no namespace, find the TagAttribute
     * 
     * See #get(String, String)
     * @param localName tag attribute name
     * @return the TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String localName)
    {
        return get("", localName);
    }

    /**
     * Find a TagAttribute that matches the passed namespace and local name.
     * 
     * @param ns namespace of the desired attribute
     * @param localName local name of the attribute
     * @return a TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String ns, String localName)
    {
        Map<String, TagAttribute> nsAttributes = namespaceLocalNameAttributes.get(ns);
        if (nsAttributes != null)
        {
            return nsAttributes.get(localName);
        }

        return null;
    }

    /**
     * Get all TagAttributes for the passed namespace
     * 
     * @param namespace namespace to search
     * @return a non-null array of TagAttributes
     */
    @Override
    public TagAttribute[] getAll(String namespace)
    {
        TagAttribute[] retVal = namespaceAttributes.get(namespace);
        return retVal == null ? EMPTY : retVal;
    }

    /**
     * A list of Namespaces found in this set
     * 
     * @return a list of Namespaces found in this set
     */
    @Override
    public String[] getNamespaces()
    {
        return namespaces;
    }

    /*
     * (non-Javadoc)
     * 
     * See java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (TagAttribute attribute : attributes)
        {
            sb.append(attribute);
            sb.append(' ');
        }
        
        if (sb.length() > 1)
        {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }
}
