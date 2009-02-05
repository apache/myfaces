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
package com.sun.facelets.tag.jsf.html;

import com.sun.facelets.tag.Tag;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagDecorator;

/**
 * @author Jacob Hookom
 * @version $Id: HtmlDecorator.java,v 1.4 2008/07/13 19:01:50 rlubke Exp $
 */
public final class HtmlDecorator implements TagDecorator
{

    public final static String XhtmlNamespace = "http://www.w3.org/1999/xhtml";

    public final static HtmlDecorator Instance = new HtmlDecorator();

    /**
     * 
     */
    public HtmlDecorator()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagDecorator#decorate(com.sun.facelets.tag.Tag)
     */
    public Tag decorate(Tag tag)
    {
        if (XhtmlNamespace.equals(tag.getNamespace()))
        {
            String n = tag.getLocalName();
            if ("a".equals(n))
            {
                return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "commandLink", tag.getQName(), tag
                        .getAttributes());
            }
            if ("form".equals(n))
            {
                return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "form", tag.getQName(), tag.getAttributes());
            }
            if ("input".equals(n))
            {
                TagAttribute attr = tag.getAttributes().get("type");
                if (attr != null)
                {
                    String t = attr.getValue();
                    TagAttributes na = removeType(tag.getAttributes());
                    if ("text".equals(t))
                    {
                        return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "inputText", tag.getQName(), na);
                    }
                    if ("password".equals(t))
                    {
                        return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "inputSecret", tag.getQName(), na);
                    }
                    if ("hidden".equals(t))
                    {
                        return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "inputHidden", tag.getQName(), na);
                    }
                    if ("submit".equals(t))
                    {
                        return new Tag(tag.getLocation(), HtmlLibrary.Namespace, "commandButton", tag.getQName(), na);
                    }
                }
            }
        }
        return null;
    }

    private static TagAttributes removeType(TagAttributes attrs)
    {
        TagAttribute[] o = attrs.getAll();
        TagAttribute[] a = new TagAttribute[o.length - 1];
        int p = 0;
        for (int i = 0; i < o.length; i++)
        {
            if (!"type".equals(o[i].getLocalName()))
            {
                a[p++] = o[i];
            }
        }
        return new TagAttributes(a);
    }

}
