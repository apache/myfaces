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

import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.view.facelets.util.ParameterCheck;

import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: MetaRulesetImpl.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
public final class MetaRulesetImpl extends MetaRuleset
{
    private final static Metadata NONE = new NullMetadata();

    //private final static Logger log = Logger.getLogger("facelets.tag.meta");
    private final static Logger log = Logger.getLogger(MetaRulesetImpl.class.getName());

    /**
     * Cache the MetadataTarget instances per ClassLoader using the Class-Name of the type variable
     * of MetadataTarget.
     * NOTE that we do it this way, because the only other valid way in order to support a shared
     * classloader scenario would be to use a WeakHashMap<Class<?>, MetadataTarget>, but this
     * creates a cyclic reference between the key and the value of the WeakHashMap which will
     * most certainly cause a memory leak! Furthermore we can manually cleanup the Map when
     * the webapp is undeployed just by removing the Map for the current ClassLoader. 
     */
    private final static WeakHashMap<ClassLoader, Map<String, MetadataTarget>> _metadata
            = new WeakHashMap<ClassLoader, Map<String, MetadataTarget>>();

    /**
     * Removes the cached MetadataTarget instances in order to prevent a memory leak.
     */
    public static void clearMetadataTargetCache()
    {
        _metadata.remove(ClassUtils.getContextClassLoader());
    }

    private static Map<String, MetadataTarget> getMetaData()
    {
        ClassLoader cl = ClassUtils.getContextClassLoader();
        
        Map<String, MetadataTarget> metadata = (Map<String, MetadataTarget>)
                _metadata.get(cl);

        if (metadata == null)
        {
            // Ensure thread-safe put over _metadata, and only create one map
            // per classloader to hold metadata.
            synchronized (_metadata)
            {
                metadata = (Map<String, MetadataTarget>) _metadata.get(cl);
                if (metadata == null)
                {
                    metadata = new HashMap<String, MetadataTarget>();
                    _metadata.put(cl, metadata);
                }
            }
        }

        return metadata;
    }

    private final Map<String, TagAttribute> _attributes;

    private final List<Metadata> _mappers;

    private final List<MetaRule> _rules;

    private final Tag _tag;

    private final Class<?> _type;

    public MetaRulesetImpl(Tag tag, Class<?> type)
    {
        _tag = tag;
        _type = type;
        _attributes = new HashMap<String, TagAttribute>();
        _mappers = new ArrayList<Metadata>();
        _rules = new ArrayList<MetaRule>();

        // setup attributes
        for (TagAttribute attribute : _tag.getAttributes().getAll())
        {
            _attributes.put(attribute.getLocalName(), attribute);
        }

        // add default rules
        _rules.add(BeanPropertyTagRule.Instance);
    }

    public MetaRuleset add(Metadata mapper)
    {
        ParameterCheck.notNull("mapper", mapper);

        if (!_mappers.contains(mapper))
        {
            _mappers.add(mapper);
        }

        return this;
    }

    public MetaRuleset addRule(MetaRule rule)
    {
        ParameterCheck.notNull("rule", rule);

        _rules.add(rule);

        return this;
    }

    public MetaRuleset alias(String attribute, String property)
    {
        ParameterCheck.notNull("attribute", attribute);
        ParameterCheck.notNull("property", property);

        TagAttribute attr = (TagAttribute) _attributes.remove(attribute);
        if (attr != null)
        {
            _attributes.put(property, attr);
        }

        return this;
    }

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
            return NONE;
        }
        else
        {
            return new MetadataImpl(_mappers.toArray(new Metadata[_mappers.size()]));
        }
    }

    public MetaRuleset ignore(String attribute)
    {
        ParameterCheck.notNull("attribute", attribute);

        _attributes.remove(attribute);

        return this;
    }

    public MetaRuleset ignoreAll()
    {
        _attributes.clear();

        return this;
    }

    private final MetadataTarget _getMetadataTarget()
    {
        Map<String, MetadataTarget> metadata = getMetaData();
        String metaKey = _type.getName();

        MetadataTarget meta = metadata.get(metaKey);
        if (meta == null)
        {
            try
            {
                meta = new MetadataTargetImpl(_type);
            }
            catch (IntrospectionException e)
            {
                throw new TagException(_tag, "Error Creating TargetMetadata", e);
            }

            synchronized(metadata)
            {
                // Use a synchronized block to ensure proper operation on concurrent use cases.
                // This is a racy single check, because initialization over the same class could happen
                // multiple times, but the same result is always calculated. The synchronized block 
                // just ensure thread-safety, because only one thread will modify the cache map
                // at the same time.
                metadata.put(metaKey, meta);
            }
        }

        return meta;
    }

    private static class NullMetadata extends Metadata
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            // do nothing
        }
    }
}
