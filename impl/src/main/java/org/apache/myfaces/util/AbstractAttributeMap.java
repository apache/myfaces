/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Helper Map implementation for use with different Attribute Maps.
 * 
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class AbstractAttributeMap<V> extends AbstractMap<String, V>
{
    private Set<String> _keySet;
    private Collection<V> _values;
    private Set<Entry<String, V>> _entrySet;

    public void clear()
    {
        List<String> names = new ArrayList<String>();
        for (Enumeration<String> e = getAttributeNames(); e.hasMoreElements();)
        {
            names.add(e.nextElement());
        }

        for (Iterator it = names.iterator(); it.hasNext();)
        {
            removeAttribute((String) it.next());
        }
    }

    public boolean containsKey(Object key)
    {
        return getAttribute(key.toString()) != null;
    }

    public boolean containsValue(Object findValue)
    {
        if (findValue == null)
        {
            return false;
        }

        for (Enumeration<String> e = getAttributeNames(); e.hasMoreElements();)
        {
            Object value = getAttribute(e.nextElement());
            if (findValue.equals(value))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<Entry<String, V>> entrySet()
    {
        return (_entrySet != null) ? _entrySet : (_entrySet = new EntrySet());
    }

    public V get(Object key)
    {
        return getAttribute(key.toString());
    }

    public boolean isEmpty()
    {
        return !getAttributeNames().hasMoreElements();
    }

    public Set<String> keySet()
    {
        return (_keySet != null) ? _keySet : (_keySet = new KeySet());
    }

    public V put(String key, V value)
    {
        V retval = getAttribute(key);
        setAttribute(key, value);
        return retval;
    }

    public void putAll(Map<? extends String, ? extends V> t)
    {
        for (Iterator<? extends Entry<? extends String, ? extends V>> it = t.entrySet().iterator(); it.hasNext();)
        {
            Entry<? extends String, ? extends V> entry = it.next();
            setAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key)
    {
        String key_ = key.toString();
        V retval = getAttribute(key_);
        removeAttribute(key_);
        return retval;
    }

    @Override
    public int size()
    {
        int size = 0;
        for (Enumeration e = getAttributeNames(); e.hasMoreElements();)
        {
            size++;
            e.nextElement();
        }
        return size;
    }

    @Override
    public Collection<V> values()
    {
        return (_values != null) ? _values : (_values = new Values());
    }

    abstract protected V getAttribute(String key);

    abstract protected void setAttribute(String key, V value);

    abstract protected void removeAttribute(String key);

    abstract protected Enumeration<String> getAttributeNames();

    private abstract class AbstractAttributeSet<E> extends AbstractSet<E>
    {
        @Override
        public boolean isEmpty()
        {
            return AbstractAttributeMap.this.isEmpty();
        }

        @Override
        public int size()
        {
            return AbstractAttributeMap.this.size();
        }

        @Override
        public boolean contains(Object o)
        {
            return AbstractAttributeMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return AbstractAttributeMap.this.remove(o) != null;
        }

        @Override
        public void clear()
        {
            AbstractAttributeMap.this.clear();
        }
    }

    private class KeySet extends AbstractAttributeSet<String>
    {
        @Override
        public Iterator<String> iterator()
        {
            return new KeyIterator();
        }
    }

    private abstract class AbstractAttributeIterator<E> implements Iterator<E>
    {
        protected final Enumeration<String> _e = getAttributeNames();
        protected String _currentKey;

        public void remove()
        {
            // remove() may cause ConcurrentModificationException.
            // We could throw an exception here, but not throwing an exception
            // allows one call to remove() to succeed
            if (_currentKey == null)
            {
                throw new NoSuchElementException("You must call next() at least once");
            }
            AbstractAttributeMap.this.remove(_currentKey);
        }

        public boolean hasNext()
        {
            return _e.hasMoreElements();
        }

        public E next()
        {
            return getValue(_currentKey = _e.nextElement());
        }

        protected abstract E getValue(String attributeName);
    }

    private class KeyIterator extends AbstractAttributeIterator<String>
    {
        @Override
        protected String getValue(String attributeName)
        {
            return attributeName;
        }
    }

    private class Values extends AbstractAttributeSet<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new ValuesIterator();
        }

        @Override
        public boolean remove(Object o)
        {
            if (o == null)
            {
                return false;
            }

            for (Iterator it = iterator(); it.hasNext();)
            {
                if (o.equals(it.next()))
                {
                    it.remove();
                    return true;
                }
            }

            return false;
        }
    }

    private class ValuesIterator extends AbstractAttributeIterator<V>
    {
        @Override
        protected V getValue(String attributeName)
        {
            return AbstractAttributeMap.this.get(attributeName);
        }
    }

    private class EntrySet extends AbstractAttributeSet<Entry<String, V>>
    {
        @Override
        public Iterator<Entry<String, V>> iterator()
        {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o)
        {
            if (!(o instanceof Entry))
            {
                return false;
            }

            Entry entry = (Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null)
            {
                return false;
            }

            return value.equals(AbstractAttributeMap.this.get(key));
        }

        @Override
        public boolean remove(Object o)
        {
            if (!(o instanceof Entry))
            {
                return false;
            }

            Entry entry = (Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null || !value.equals(AbstractAttributeMap.this.get(key)))
            {
                return false;
            }

            return AbstractAttributeMap.this.remove(((Entry) o).getKey()) != null;
        }
    }

    /**
     * Not very efficient since it generates a new instance of <code>Entry</code> for each element and still internaly
     * uses the <code>KeyIterator</code>. It is more efficient to use the <code>KeyIterator</code> directly.
     */
    private class EntryIterator extends AbstractAttributeIterator<Entry<String, V>>
    {
        @Override
        protected Entry<String, V> getValue(String attributeName)
        {
            // Must create new Entry every time--value of the entry must stay
            // linked to the same attribute name
            return new EntrySetEntry(attributeName);
        }
    }

    private class EntrySetEntry implements Entry<String, V>
    {
        private final String _currentKey;

        public EntrySetEntry(String currentKey)
        {
            _currentKey = currentKey;
        }

        public String getKey()
        {
            return _currentKey;
        }

        public V getValue()
        {
            return AbstractAttributeMap.this.get(_currentKey);
        }

        public V setValue(V value)
        {
            return AbstractAttributeMap.this.put(_currentKey, value);
        }

        @Override
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((_currentKey == null) ? 0 : _currentKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final EntrySetEntry other = (EntrySetEntry) obj;
            if (_currentKey == null)
            {
                if (other._currentKey != null)
                    return false;
            }
            else if (!_currentKey.equals(other._currentKey))
                return false;
            return true;
        }
        
        
    }
}
