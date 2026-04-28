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
package org.apache.myfaces.view.facelets.tag;

import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.context.FacesContext;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.view.facelets.PassthroughRule;
import org.apache.myfaces.view.facelets.tag.faces.PassThroughLibrary;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class MetaRulesetImpl extends MetaRuleset
{
    private final static Logger log = Logger.getLogger(MetaRulesetImpl.class.getName());

    private static final String METADATA_KEY = MetaRulesetImpl.class.getName() + ".METADATA";

    /**
     * MetadataTarget cache keyed by component class name, stored in the JSF application map so it
     * is released with the web application (no separate undeploy hook).
     */
    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<String, MetadataTarget> getMetaData()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null)
        {
            throw new IllegalStateException("No FacesContext is available");
        }
        Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
        ConcurrentHashMap<String, MetadataTarget> map =
                (ConcurrentHashMap<String, MetadataTarget>) applicationMap.get(METADATA_KEY);
        if (map != null)
        {
            return map;
        }
        return (ConcurrentHashMap<String, MetadataTarget>) applicationMap.computeIfAbsent(METADATA_KEY,
                k -> new ConcurrentHashMap<>(64));
    }

    private final static TagAttribute[] EMPTY = new TagAttribute[0];
    
    private final Map<String, TagAttribute> _attributes;
    private final TagAttribute[] _passthroughAttributes;
    private final List<Metadata> _mappers;
    private final List<MetaRule> _rules;
    private final Tag _tag;
    private final Class<?> _type;
    private final List<MetaRule> _passthroughRules;
    
    public MetaRulesetImpl(Tag tag, Class<?> type)
    {
        _tag = tag;
        _type = type;
        TagAttribute[] allAttributes = _tag.getAttributes().getAll();
        // This map is proportional to the number of attributes defined, and usually
        // the properties with alias are very few, so set an initial size close to
        // the number of attributes is ok.
        int initialSize = allAttributes.length > 0 ? (allAttributes.length * 4 + 3) / 3 : 4;
        _attributes = new HashMap<>(initialSize);
        _mappers = new ArrayList<>(initialSize);
        // Usually ComponentTagHandlerDelegate has 5 rules at max
        // and CompositeComponentResourceTagHandler 6, so 8 is a good number
        _rules = new ArrayList<>(8); 
        _passthroughRules = new ArrayList<>(2);

        // Passthrough attributes are different from normal attributes, because they
        // are just rendered into the markup without additional processing from the
        // renderer. Here it starts attribute processing, so this is the best place 
        // to find the passthrough attributes.
        TagAttribute[] passthroughAttribute = _tag.getAttributes().getAll(
            PassThroughLibrary.NAMESPACE);
        TagAttribute[] passthroughAttributeJcp = _tag.getAttributes().getAll(
            PassThroughLibrary.JCP_NAMESPACE);
        TagAttribute[] passthroughAttributeSun = _tag.getAttributes().getAll(
            PassThroughLibrary.SUN_NAMESPACE);
        
        if (passthroughAttribute.length > 0
                || passthroughAttributeJcp.length > 0
                || passthroughAttributeSun.length > 0)
        {
            _passthroughAttributes = new TagAttribute[passthroughAttribute.length + 
                passthroughAttributeJcp.length + passthroughAttributeSun.length];
            int i = 0;
            for (TagAttribute attribute : allAttributes)
            {
                // The fastest check is check if the length is > 0, because
                // most attributes usually has no namespace attached.
                if (attribute.getNamespace().length() > 0 &&
                    (PassThroughLibrary.NAMESPACE.equals(attribute.getNamespace())
                        || PassThroughLibrary.JCP_NAMESPACE.equals(attribute.getNamespace())
                        || PassThroughLibrary.SUN_NAMESPACE.equals(attribute.getNamespace())))
                {
                    _passthroughAttributes[i] = attribute;
                    i++;
                }
                else
                {
                    _attributes.put(attribute.getLocalName(), attribute);
                }
            }
        }
        else
        {
            _passthroughAttributes = EMPTY;
            // setup attributes
            for (TagAttribute attribute : allAttributes)
            {
                _attributes.put(attribute.getLocalName(), attribute);
            }
        }

        // add default rules
        _rules.add(BeanPropertyTagRule.INSTANCE);
    }

    @Override
    public MetaRuleset add(Metadata mapper)
    {
        Assert.notNull(mapper, "mapper");

        if (!_mappers.contains(mapper))
        {
            _mappers.add(mapper);
        }

        return this;
    }

    @Override
    public MetaRuleset addRule(MetaRule rule)
    {
        Assert.notNull(rule, "rule");

        if (rule instanceof PassthroughRule)
        {
            _passthroughRules.add(rule);
        }
        else
        {
            _rules.add(rule);
        }

        return this;
    }

    @Override
    public MetaRuleset alias(String attribute, String property)
    {
        Assert.notNull(attribute, "attribute");
        Assert.notNull(property, "property");

        TagAttribute attr = _attributes.remove(attribute);
        if (attr != null)
        {
            _attributes.put(property, attr);
        }

        return this;
    }

    @Override
    public Metadata finish()
    {
        MetadataTarget target = null;
        
        assert !_rules.isEmpty();
        
        if (!_attributes.isEmpty())
        {
            target = this._getMetadataTarget();
            int ruleEnd = _rules.size() - 1;

            // now iterate over attributes
            for (Map.Entry<String, TagAttribute> entry : _attributes.entrySet())
            {
                Metadata data = null;

                int i = ruleEnd;

                // First loop is always safe
                do
                {
                    MetaRule rule = _rules.get(i);
                    data = rule.applyRule(entry.getKey(), entry.getValue(), target);
                    i--;
                } while (data == null && i >= 0);

                if (data == null)
                {
                    if (log.isLoggable(Level.SEVERE))
                    {
                        log.severe(entry.getValue() + " Unhandled by MetaTagHandler for type " + _type.getName());
                    }
                }
                else
                {
                    _mappers.add(data);
                }
            }
        }

        if (_passthroughAttributes.length > 0 && !_passthroughRules.isEmpty())
        {
            if (target == null)
            {
                target = this._getMetadataTarget();
            }
            int ruleEnd = _passthroughRules.size() - 1;

            // now iterate over attributes
            for (TagAttribute passthroughAttribute : _passthroughAttributes)
            {
                Metadata data = null;

                int i = ruleEnd;

                // First loop is always safe
                do
                {
                    MetaRule rule = _passthroughRules.get(i);
                    data = rule.applyRule(passthroughAttribute.getLocalName(),
                        passthroughAttribute, target);
                    i--;
                } while (data == null && i >= 0);

                if (data == null)
                {
                    if (log.isLoggable(Level.SEVERE))
                    {
                        log.severe(passthroughAttribute.getLocalName() + 
                            " Unhandled by MetaTagHandler for type " + _type.getName());
                    }
                }
                else
                {
                    _mappers.add(data);
                }
            }
        }

        if (_mappers.isEmpty())
        {
            return NullMetadata.INSTANCE;
        }
        else
        {
            return new MetadataImpl(_mappers.toArray(new Metadata[_mappers.size()]));
        }
    }

    @Override
    public MetaRuleset ignore(String attribute)
    {
        Assert.notNull(attribute, "attribute");

        _attributes.remove(attribute);

        return this;
    }

    @Override
    public MetaRuleset ignoreAll()
    {
        _attributes.clear();

        return this;
    }

    private MetadataTarget _getMetadataTarget()
    {
        ConcurrentHashMap<String, MetadataTarget> metadata = getMetaData();
        String metaKey = _type.getName();
        MetadataTarget meta = metadata.get(metaKey);
        if (meta != null)
        {
            return meta;
        }
        return metadata.computeIfAbsent(metaKey, key ->
        {
            try
            {
                if (PropertyDescriptorUtils.isUseLambdas(
                        FacesContext.getCurrentInstance().getExternalContext()))
                {
                    return new LambdaMetadataTargetImpl(_type);
                }
                return new MetadataTargetImpl(_type);
            }
            catch (IntrospectionException e)
            {
                throw new TagException(_tag, "Error Creating TargetMetadata", e);
            }
        });
    }
}
