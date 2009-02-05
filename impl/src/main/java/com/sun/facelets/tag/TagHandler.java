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
package com.sun.facelets.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.webapp.pdl.facelets.FaceletHandler;

/**
 * Foundation class for FaceletHandlers associated with markup in a Facelet document.
 * 
 * @author Jacob Hookom
 * @version $Id: TagHandler.java,v 1.6 2008/07/13 19:01:35 rlubke Exp $
 */
public abstract class TagHandler implements FaceletHandler
{

    protected final String tagId;

    protected final Tag tag;

    protected final FaceletHandler nextHandler;

    public TagHandler(TagConfig config)
    {
        this.tagId = config.getTagId();
        this.tag = config.getTag();
        this.nextHandler = config.getNextHandler();
    }

    /**
     * Utility method for fetching the appropriate TagAttribute
     * 
     * @param localName
     *            name of attribute
     * @return TagAttribute if found, otherwise null
     */
    protected final TagAttribute getAttribute(String localName)
    {
        return this.tag.getAttributes().get(localName);
    }

    /**
     * Utility method for fetching a required TagAttribute
     * 
     * @param localName
     *            name of the attribute
     * @return TagAttribute if found, otherwise error
     * @throws TagException
     *             if the attribute was not found
     */
    protected final TagAttribute getRequiredAttribute(String localName) throws TagException
    {
        TagAttribute attr = this.getAttribute(localName);
        if (attr == null)
        {
            throw new TagException(this.tag, "Attribute '" + localName + "' is required");
        }
        return attr;
    }

    /**
     * Searches child handlers, starting at the 'nextHandler' for all instances of the passed type. This process will
     * stop searching a branch if an instance is found.
     * 
     * @param type
     *            Class type to search for
     * @return iterator over instances of FaceletHandlers of the matching type
     */
    protected final Iterator findNextByType(Class type)
    {
        List found = new ArrayList();
        if (type.isAssignableFrom(this.nextHandler.getClass()))
        {
            found.add(this.nextHandler);
        }
        else if (this.nextHandler instanceof CompositeFaceletHandler)
        {
            FaceletHandler[] h = ((CompositeFaceletHandler) this.nextHandler).getHandlers();
            for (int i = 0; i < h.length; i++)
            {
                if (type.isAssignableFrom(h[i].getClass()))
                {
                    found.add(h[i]);
                }
            }
        }
        return found.iterator();
    }

    public String toString()
    {
        return this.tag.toString();
    }
}
