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
package javax.faces.component;

import java.io.Serializable;
import java.util.*;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _ComponentFacetMap
        implements Map, Serializable
{
	private static final long serialVersionUID = -3456937594422167629L;
	private UIComponent _component;
    private Map _map = new HashMap();

    _ComponentFacetMap(UIComponent component)
    {
        _component = component;
    }

    public int size()
    {
        return _map.size();
    }

    public void clear()
    {
        _map.clear();
    }

    public boolean isEmpty()
    {
        return _map.isEmpty();
    }

    public boolean containsKey(Object key)
    {
        checkKey(key);
        return _map.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        checkValue(value);
        return _map.containsValue(value);
    }

    public Collection values()
    {
        return _map.values();
    }

    public void putAll(Map t)
    {
        for (Iterator it = t.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Entry)it.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set entrySet()
    {
        return _map.entrySet();
    }

    public Set keySet()
    {
        return _map.keySet();
    }

    public Object get(Object key)
    {
        checkKey(key);
        return _map.get(key);
    }

    public Object remove(Object key)
    {
        checkKey(key);
        UIComponent facet = (UIComponent)_map.remove(key);
        if (facet != null) facet.setParent(null);
        return facet;
    }

    public Object put(Object key, Object value)
    {
        checkKey(key);
        checkValue(value);
        setNewParent((String)key, (UIComponent)value);
        return _map.put(key, value);
    }


    private void setNewParent(String facetName, UIComponent facet)
    {
        UIComponent oldParent = facet.getParent();
        if (oldParent != null)
        {
            oldParent.getFacets().remove(facetName);
        }
        facet.setParent(_component);
    }

    private void checkKey(Object key)
    {
        if (key == null) throw new NullPointerException("key");
        if (!(key instanceof String)) throw new ClassCastException("key is not a String");
    }

    private void checkValue(Object value)
    {
        if (value == null) throw new NullPointerException("value");
        if (!(value instanceof UIComponent)) throw new ClassCastException("value is not a UIComponent");
    }

}
