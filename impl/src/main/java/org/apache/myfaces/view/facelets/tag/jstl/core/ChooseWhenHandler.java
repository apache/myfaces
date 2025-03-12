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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;
import org.apache.myfaces.view.facelets.tag.ComponentContainerHandler;

/**
 * Subtag of &lt;choose&gt; that includes its body if its
 * condition evaluates to 'true'
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
@JSFFaceletTag(name = "c:when")
public final class ChooseWhenHandler extends TagHandler implements ComponentContainerHandler
{

    /**
     * The test condition that determines whether or not the
     * body content should be processed. 
     */
    @JSFFaceletAttribute(className="boolean",required=true)
    private final TagAttribute test;

    public ChooseWhenHandler(TagConfig config)
    {
        super(config);
        this.test = this.getRequiredAttribute("test");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        this.nextHandler.apply(ctx, parent);
    }

    public boolean isTestTrue(FaceletContext ctx)
    {
        return this.test.getBoolean(ctx);
    }
}
