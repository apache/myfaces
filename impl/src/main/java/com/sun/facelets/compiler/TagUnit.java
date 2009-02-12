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
package com.sun.facelets.compiler;

import javax.faces.webapp.pdl.facelets.FaceletHandler;

import com.sun.facelets.tag.Tag;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagLibrary;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: TagUnit.java,v 1.7 2008/07/13 19:01:33 rlubke Exp $
 */
class TagUnit extends CompilationUnit implements TagConfig
{

    private final TagLibrary library;

    private final String id;

    private final Tag tag;

    private final String namespace;

    private final String name;

    public TagUnit(TagLibrary library, String namespace, String name, Tag tag, String id)
    {
        this.library = library;
        this.tag = tag;
        this.namespace = namespace;
        this.name = name;
        this.id = id;
    }

    public FaceletHandler createFaceletHandler()
    {
        return this.library.createTagHandler(this.namespace, this.name, this);
    }

    public FaceletHandler getNextHandler()
    {
        return this.getNextFaceletHandler();
    }

    public Tag getTag()
    {
        return this.tag;
    }

    public String getTagId()
    {
        return this.id;
    }

    public String toString()
    {
        return this.tag.toString();
    }

}
