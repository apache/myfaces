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

import org.apache.myfaces.view.facelets.tag.BeanPropertyTagRule;
import org.apache.myfaces.view.facelets.tag.MetadataImpl;
import org.apache.myfaces.view.facelets.tag.MetadataTargetImpl;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.core.api.shared.lang.PropertyDescriptorUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.view.facelets.tag.LambdaMetadataTargetImpl;
import org.apache.myfaces.view.facelets.tag.NullMetadata;

public class CompositeMetaRulesetImpl extends MetaRuleset
{
    private final static Logger log = Logger.getLogger(CompositeMetadataTargetImpl.class.getName());
    
    private static final String METADATA_KEY = CompositeMetaRulesetImpl.class.getName() + ".METADATA";

    private static Map<String, MetadataTarget> getMetaData()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();

        Map<String, MetadataTarget> metadata = (Map<String, MetadataTarget>) applicationMap.computeIfAbsent(
                METADATA_KEY, k -> new HashMap<>());

        return metadata;
    }

    private final Map<String, TagAttribute> _attributes;
    private final List<Metadata> _mappers;
    private final List<MetaRule> _rules;
    private final Tag _tag;
    private final Class<?> _type;
    private final MetadataTarget _meta;

    public CompositeMetaRulesetImpl(Tag tag, Class<?> type, BeanInfo beanInfo)
    {
        _tag = tag;
        _type = type;
        _attributes = new HashMap<>();
        _mappers = new ArrayList<>();
        _rules = new ArrayList<>();

        // setup attributes
        for (TagAttribute attribute : _tag.getAttributes().getAll())
        {
            _attributes.put(attribute.getLocalName(), attribute);
        }

        // add default rules
        _rules.add(BeanPropertyTagRule.INSTANCE);
        
        try
        {
            _meta = new CompositeMetadataTargetImpl(_getBaseMetadataTarget(), beanInfo);            
        }
        catch (IntrospectionException e)
        {
            throw new TagException(_tag, "Error Creating TargetMetadata", e);
        }
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

        _rules.add(rule);

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
        assert !_rules.isEmpty();
        
        if (!_attributes.isEmpty())
        {
            MetadataTarget target = this._getMetadataTarget();
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
        return _meta;
    }
    
    private MetadataTarget _getBaseMetadataTarget()
    {
        Map<String, MetadataTarget> metadata = getMetaData();
        String key = _type.getName();

        MetadataTarget meta = metadata.get(key);
        if (meta == null)
        {
            try
            {
                if (PropertyDescriptorUtils.isUseLambdaMetafactory(
                        FacesContext.getCurrentInstance().getExternalContext()))
                {
                    meta = new LambdaMetadataTargetImpl(_type);
                }
                else
                {
                    meta = new MetadataTargetImpl(_type);
                }
            }
            catch (IntrospectionException e)
            {
                throw new TagException(_tag, "Error Creating TargetMetadata", e);
            }

            metadata.put(key, meta);
        }

        return meta;
    }    
}
