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
package com.sun.facelets.tag.jsf.core;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.webapp.pdl.facelets.FaceletContext;

import com.sun.facelets.tag.TextHandler;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

/**
 * Handler for f:verbatim
 * 
 * @author Adam Winer
 * @version $Id: VerbatimHandler.java,v 1.3 2008/07/13 19:01:44 rlubke Exp $
 */
public final class VerbatimHandler extends ComponentHandler
{
    public VerbatimHandler(ComponentConfig config)
    {
        super(config);
    }

    protected void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent)
    {
        StringBuffer content = new StringBuffer();
        Iterator iter = findNextByType(TextHandler.class);
        while (iter.hasNext())
        {
            TextHandler text = (TextHandler) iter.next();
            content.append(text.getText(ctx));
        }

        c.getAttributes().put("value", content.toString());
        c.getAttributes().put("escape", Boolean.FALSE);
        c.setTransient(true);
    }

    protected void applyNextHandler(FaceletContext ctx, UIComponent c)
    {
    }
}
