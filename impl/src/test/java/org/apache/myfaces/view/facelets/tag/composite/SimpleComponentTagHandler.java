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
package org.apache.myfaces.view.facelets.tag.composite;

import jakarta.el.MethodExpression;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

public class SimpleComponentTagHandler extends ComponentHandler
{

    public SimpleComponentTagHandler(ComponentConfig config)
    {
        super(config);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type)
    {
        return super.createMetaRuleset(type).addRule(new CustomMethodRule());
    }

    public final class CustomMethodRule extends MetaRule {

        @Override
        public Metadata applyRule(String name, TagAttribute attribute,
                MetadataTarget meta)
        {
            if (meta.isTargetInstanceOf(SimpleComponent.class))
            {
                if ("customMethod".equals(name))
                {
                    return new SimpleMethodMapper(attribute);
                }
            }
            return null;
        }
        
    }
    
    final static class SimpleMethodMapper extends Metadata
    {
        private final TagAttribute _attr;
        
        public final static Class<?>[] CUSTOM_METHOD_SIG = new Class[]{String.class};

        public SimpleMethodMapper(TagAttribute attr)
        {
            this._attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            MethodExpression expr = _attr.getMethodExpression(ctx, null, CUSTOM_METHOD_SIG);
            ((SimpleComponent) instance).setCustomMethod(expr);
        }
    }

}
