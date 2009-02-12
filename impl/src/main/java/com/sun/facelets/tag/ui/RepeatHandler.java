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
package com.sun.facelets.tag.ui;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.webapp.pdl.facelets.FaceletContext;

import com.sun.facelets.tag.MetaRuleset;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

public class RepeatHandler extends ComponentHandler
{

    public RepeatHandler(ComponentConfig config)
    {
        super(config);
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {
        MetaRuleset meta = super.createMetaRuleset(type);

        if (!UILibrary.Namespace.equals(this.tag.getNamespace()))
        {
            meta.add(new TagMetaData(type));
        }

        meta.alias("class", "styleClass");

        return meta;
    }

    private class TagMetaData extends Metadata
    {

        private final String[] attrs;

        public TagMetaData(Class type)
        {
            Set s = new HashSet();
            TagAttribute[] ta = tag.getAttributes().getAll();
            for (int i = 0; i < ta.length; i++)
            {
                if ("class".equals(ta[i].getLocalName()))
                {
                    s.add("styleClass");
                }
                else
                {
                    s.add(ta[i].getLocalName());
                }
            }
            try
            {
                PropertyDescriptor[] pd = Introspector.getBeanInfo(type).getPropertyDescriptors();
                for (int i = 0; i < pd.length; i++)
                {
                    if (pd[i].getWriteMethod() != null)
                    {
                        s.remove(pd[i].getName());
                    }
                }
            }
            catch (Exception e)
            {
                // do nothing
            }
            this.attrs = (String[]) s.toArray(new String[s.size()]);
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            UIComponent c = (UIComponent) instance;
            Map attrs = c.getAttributes();
            attrs.put("alias.element", tag.getQName());
            if (this.attrs.length > 0)
            {
                attrs.put("alias.attributes", this.attrs);
            }
        }

    }

}
