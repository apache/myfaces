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
package jakarta.faces.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

class _PassThroughAttributesMap implements Map<String, Object>, Serializable
{
    private static final long serialVersionUID = -9106932179394257866L;
    
    private UIComponentBase _component;
    
    _PassThroughAttributesMap(UIComponentBase component)
    {
        _component = component;
    }
    
    /**
     * Return the map containing the attributes.
     * <p/>
     * This method is package-scope so that the UIComponentBase class can access it
     * directly when serializing the component.
     */
    Map<String, Object> getUnderlyingMap()
    {
        StateHelper stateHelper = _component.getStateHelper(false);
        Map<String, Object> attributes = null;
        if (stateHelper != null)
        {
            attributes = (Map<String, Object>) stateHelper.get(UIComponentBase.PropertyKeys.passThroughAttributesMap);
        }
        return attributes == null ? Collections.EMPTY_MAP : attributes;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return getUnderlyingMap().equals(obj);
    }

    @Override
    public int hashCode()
    {
        return getUnderlyingMap().hashCode();
    }

    @Override
    public int size()
    {
        return getUnderlyingMap().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getUnderlyingMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return getUnderlyingMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return getUnderlyingMap().containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return getUnderlyingMap().get(key);
    }

    @Override
    public Object put(String key, Object value)
    {
        return _component.getStateHelper().put(
            UIComponentBase.PropertyKeys.passThroughAttributesMap, key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return _component.getStateHelper().remove(
            UIComponentBase.PropertyKeys.passThroughAttributesMap, key);
    }

    /**
     * Call put(key, value) for each entry in the provided map.
     */
    @Override
    public void putAll(Map<? extends String, ?> t)
    {
        for (Map.Entry<? extends String, ?> entry : t.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Return a set of all <i>attributes</i>. Properties of the underlying
     * UIComponent are not included, nor value-bindings.
     */
    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
        return getUnderlyingMap().entrySet();
    }

    @Override
    public Set<String> keySet()
    {
        return getUnderlyingMap().keySet();
    }

    @Override
    public void clear()
    {
        getUnderlyingMap().clear();
    }

    @Override
    public Collection<Object> values()
    {
        return getUnderlyingMap().values();
    }
}
