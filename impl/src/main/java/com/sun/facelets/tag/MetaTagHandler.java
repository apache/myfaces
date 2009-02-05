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

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.util.ParameterCheck;

/**
 * A base tag for wiring state to an object instance based on rules populated at the time of creating a MetaRuleset.
 * 
 * @author Jacob Hookom
 * @version $Id: MetaTagHandler.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
public abstract class MetaTagHandler extends TagHandler
{

    private Class lastType = Object.class;

    private Metadata mapper;

    public MetaTagHandler(TagConfig config)
    {
        super(config);
    }

    /**
     * Extend this method in order to add your own rules.
     * 
     * @param type
     * @return
     */
    protected MetaRuleset createMetaRuleset(Class type)
    {
        ParameterCheck.notNull("type", type);
        return new MetaRulesetImpl(this.tag, type);
    }

    /**
     * Invoking/extending this method will cause the results of the created MetaRuleset to auto-wire state to the passed
     * instance.
     * 
     * @param ctx
     * @param instance
     */
    protected void setAttributes(FaceletContext ctx, Object instance)
    {
        if (instance != null)
        {
            Class type = instance.getClass();
            if (mapper == null || !this.lastType.equals(type))
            {
                this.lastType = type;
                this.mapper = this.createMetaRuleset(type).finish();
            }
            this.mapper.applyMetadata(ctx, instance);
        }
    }
}
