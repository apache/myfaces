/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.util.ParameterCheck;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: MetaRulesetImpl.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
final class MetaRulesetImpl extends MetaRuleset
{

    private final static WeakHashMap metadata = new WeakHashMap();

    private final static Logger log = Logger.getLogger("facelets.tag.meta");

    private final Tag tag;

    private final Class type;

    private final Map attributes;

    private final List mappers;

    private final List rules;

    public MetaRulesetImpl(Tag tag, Class type)
    {
        this.tag = tag;
        this.type = type;
        this.attributes = new HashMap();
        this.mappers = new ArrayList();
        this.rules = new ArrayList();

        // setup attributes
        TagAttribute[] attrs = this.tag.getAttributes().getAll();
        for (int i = 0; i < attrs.length; i++)
        {
            attributes.put(attrs[i].getLocalName(), attrs[i]);
        }

        // add default rules
        this.rules.add(BeanPropertyTagRule.Instance);
    }

    public MetaRuleset ignore(String attribute)
    {
        ParameterCheck.notNull("attribute", attribute);
        this.attributes.remove(attribute);
        return this;
    }

    public MetaRuleset alias(String attribute, String property)
    {
        ParameterCheck.notNull("attribute", attribute);
        ParameterCheck.notNull("property", property);
        TagAttribute attr = (TagAttribute) this.attributes.remove(attribute);
        if (attr != null)
        {
            this.attributes.put(property, attr);
        }
        return this;
    }

    public MetaRuleset add(Metadata mapper)
    {
        ParameterCheck.notNull("mapper", mapper);
        if (!this.mappers.contains(mapper))
        {
            this.mappers.add(mapper);
        }
        return this;
    }

    public MetaRuleset addRule(MetaRule rule)
    {
        ParameterCheck.notNull("rule", rule);
        this.rules.add(rule);
        return this;
    }

    private final MetadataTarget getMetadataTarget()
    {
        String key = this.type.getName();
        MetadataTarget meta = (MetadataTarget) metadata.get(key);
        if (meta == null)
        {
            try
            {
                meta = new MetadataTargetImpl(type);
            }
            catch (IntrospectionException e)
            {
                throw new TagException(this.tag, "Error Creating TargetMetadata", e);
            }
            metadata.put(key, meta);
        }
        return meta;
    }

    public Metadata finish()
    {
        if (!this.attributes.isEmpty())
        {
            if (this.rules.isEmpty())
            {
                if (log.isLoggable(Level.SEVERE))
                {
                    for (Iterator itr = this.attributes.values().iterator(); itr.hasNext();)
                    {
                        log.severe(itr.next() + " Unhandled by MetaTagHandler for type " + this.type.getName());
                    }
                }
            }
            else
            {
                MetadataTarget target = this.getMetadataTarget();
                // now iterate over attributes
                Map.Entry entry;
                MetaRule rule;
                Metadata data;
                int ruleEnd = this.rules.size() - 1;
                for (Iterator itr = this.attributes.entrySet().iterator(); itr.hasNext();)
                {
                    entry = (Map.Entry) itr.next();
                    data = null;
                    int i = ruleEnd;
                    while (data == null && i >= 0)
                    {
                        rule = (MetaRule) this.rules.get(i);
                        data = rule.applyRule((String) entry.getKey(), (TagAttribute) entry.getValue(), target);
                        i--;
                    }
                    if (data == null)
                    {
                        if (log.isLoggable(Level.SEVERE))
                        {
                            log.severe(entry.getValue() + " Unhandled by MetaTagHandler for type "
                                    + this.type.getName());
                        }
                    }
                    else
                    {
                        this.mappers.add(data);
                    }
                }
            }
        }

        if (this.mappers.isEmpty())
        {
            return NONE;
        }
        else
        {
            return new MetadataImpl((Metadata[]) this.mappers.toArray(new Metadata[this.mappers.size()]));
        }
    }

    public MetaRuleset ignoreAll()
    {
        this.attributes.clear();
        return this;
    }

    private final static Metadata NONE = new Metadata()
    {
        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            // do nothing
        }
    };
}
