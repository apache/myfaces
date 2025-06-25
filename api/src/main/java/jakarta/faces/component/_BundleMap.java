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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

class _BundleMap implements Map<String, String>
{
    private ResourceBundle _bundle;
    private List<String> _values;

    public _BundleMap(ResourceBundle bundle)
    {
        _bundle = bundle;
    }

    // Optimized methods
    @Override
    public String get(Object key)
    {
        try
        {
            return (String) _bundle.getObject(key.toString());
        }
        catch (Exception e)
        {
            return "???" + key + "???";
        }
    }

    @Override
    public boolean isEmpty()
    {
        return !_bundle.getKeys().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key)
    {
        try
        {
            return _bundle.getObject(key.toString()) != null;
        }
        catch (MissingResourceException e)
        {
            return false;
        }
    }

    // Unoptimized methods
    @Override
    public Collection<String> values()
    {
        if (_values == null)
        {
            _values = new ArrayList<>();
            for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
            {
                String v = _bundle.getString(enumer.nextElement());
                _values.add(v);
            }
        }
        return _values;
    }

    @Override
    public int size()
    {
        return values().size();
    }

    @Override
    public boolean containsValue(Object value)
    {
        return values().contains(value);
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet()
    {
        Set<Map.Entry<String, String>> set = new HashSet<>();
        for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
        {
            final String k = enumer.nextElement();
            set.add(new Map.Entry<String, String>()
            {
                @Override
                public String getKey()
                {
                    return k;
                }

                @Override
                public String getValue()
                {
                    return (String) _bundle.getObject(k);
                }

                @Override
                public String setValue(String value)
                {
                    throw new UnsupportedOperationException();
                }
            });
        }

        return set;
    }

    @Override
    public Set<String> keySet()
    {
        Set<String> set = new HashSet<>();
        for (Enumeration<String> enumer = _bundle.getKeys(); enumer.hasMoreElements(); )
        {
            set.add(enumer.nextElement());
        }
        return set;
    }

    @Override
    public String remove(Object key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> t)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(String key, String value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }
}
