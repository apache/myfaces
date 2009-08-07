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
package org.apache.myfaces.view.facelets.tag.ui;

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

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletAttribute;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * @author Jacob Hookom
 * @version $Id: ParamHandler.java,v 1.6 2008/07/13 19:01:42 rlubke Exp $
 */
@JSFFaceletTag(name="ui:param")
public class ParamHandler extends TagHandler
{

    /**
     * The name of the variable to pass to the included Facelet.
     */
    @JSFFaceletAttribute(
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final TagAttribute name;

    /**
     * The literal or EL expression value to assign to the named variable.
     */
    @JSFFaceletAttribute(
            className="javax.el.ValueExpression",
            deferredValueType="java.lang.String",
            required=true)
    private final TagAttribute value;

    /**
     * @param config
     */
    public ParamHandler(TagConfig config)
    {
        super(config);
        this.name = this.getRequiredAttribute("name");
        this.value = this.getRequiredAttribute("value");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletHandler#apply(javax.faces.view.facelets.FaceletContext, javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException,
            ELException
    {
        String nameStr = this.name.getValue(ctx);
        ValueExpression valueVE = this.value.getValueExpression(ctx, Object.class);
        ctx.getVariableMapper().setVariable(nameStr, valueVE);
    }

}
