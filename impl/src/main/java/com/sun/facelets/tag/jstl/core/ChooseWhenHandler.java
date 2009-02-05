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
package com.sun.facelets.tag.jstl.core;

import javax.faces.component.UIComponent;

import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import com.sun.facelets.tag.TagHandler;

/**
 * @author Jacob Hookom
 * @version $Id: ChooseWhenHandler.java,v 1.3 2008/07/13 19:01:43 rlubke Exp $
 */
public final class ChooseWhenHandler extends TagHandler
{

    private final TagAttribute test;

    public ChooseWhenHandler(TagConfig config)
    {
        super(config);
        this.test = this.getRequiredAttribute("test");
    }

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
