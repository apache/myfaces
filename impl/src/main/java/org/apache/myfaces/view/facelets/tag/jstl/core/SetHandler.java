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

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

/**
 * Simplified implementation of c:set
 * 
 * @author Jacob Hookom
 * @version $Id: SetHandler.java,v 1.2 2008/07/13 19:01:44 rlubke Exp $
 */
public class SetHandler extends TagHandler
{

    private final TagAttribute var;

    private final TagAttribute value;

    public SetHandler(TagConfig config)
    {
        super(config);
        this.value = this.getRequiredAttribute("value");
        this.var = this.getRequiredAttribute("var");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        String varStr = this.var.getValue(ctx);
        ValueExpression veObj = this.value.getValueExpression(ctx, Object.class);
        ctx.getVariableMapper().setVariable(varStr, veObj);
    }
}
