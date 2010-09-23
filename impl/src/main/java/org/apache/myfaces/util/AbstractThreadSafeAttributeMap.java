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
 * @author Anton Koinov (latest modification by $Author: lu4242 $)
 * @version $Revision: 982465 $ $Date: 2010-08-04 23:39:32 -0500 (Mié, 04 Ago 2010) $
 */
public abstract class AbstractThreadSafeAttributeMap extends AbstractMap
{
    private Set              _keySet;
    private Collection       _values;
    private Set              _entrySet;

    public void clear()
    {
        final List names = _list(getAttributeNames());

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

        for (Enumeration e = getAttributeNames(); e.hasMoreElements();)
        {
            Object value = getAttribute((String) e.nextElement());
            if (findValue.equals(value))
            {
                return true;
            }
        }

        return false;
    }

    public Set entrySet()
    {
        return (_entrySet != null) ? _entrySet : (_entrySet = new EntrySet());
    }

    public Object get(Object key)
    {
        return getAttribute(key.toString());
    }

    public boolean isEmpty()
    {
        return !getAttributeNames().hasMoreElements();
    }

    public Set keySet()
    {
        return (_keySet != null) ? _keySet : (_keySet = new KeySet());
    }

    public Object put(Object key, Object value)
    {
        String key_ = key.toString();
        Object retval = getAttribute(key_);
        setAttribute(key_, value);
        return retval;
    }

    public void putAll(Map t)
    {
        for (Iterator it = t.entrySet().iterator(); it.hasNext();)
        {
            Entry entry = (Entry) it.next();
            setAttribute(entry.getKey().toString(), entry.getValue());
        }
    }

    public Object remove(Object key)
    {
        String key_ = key.toString();
        Object retval = getAttribute(key_);
        removeAttribute(key_);
        return retval;
    }

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

    public Collection values()
    {
        return (_values != null) ? _values : (_values = new Values());
    }
    
    /**
     * Collections.list() from JDK 1.4
     */
    private ArrayList _list(Enumeration e) {
        ArrayList l = new ArrayList();
        while (e.hasMoreElements())
        {
            l.add(e.nextElement());
        }
        return l;
    }
    

    abstract protected Object getAttribute(String key);

    abstract protected void setAttribute(String key, Object value);

    abstract protected void removeAttribute(String key);

    abstract protected Enumeration getAttributeNames();


    private class KeySet extends AbstractSet
    {
        public Iterator iterator()
        {
            return new KeyIterator();
        }

        public boolean isEmpty()
        {
            return AbstractThreadSafeAttributeMap.this.isEmpty();
        }

        public int size()
        {
            return AbstractThreadSafeAttributeMap.this.size();
        }

        public boolean contains(Object o)
        {
            return AbstractThreadSafeAttributeMap.this.containsKey(o);
        }

        public boolean remove(Object o)
        {
            return AbstractThreadSafeAttributeMap.this.remove(o) != null;
        }

        public void clear()
        {
            AbstractThreadSafeAttributeMap.this.clear();
        }
    }

    private class KeyIterator
        implements Iterator
    {
        // We use a copied version of the Enumeration from getAttributeNames()
        // here, because directly using it might cause a ConcurrentModificationException
        // when performing remove(). Note that we can do this since the Enumeration
        // from getAttributeNames() will contain exactly the attribute names from the time
        // getAttributeNames() was called and it will not be updated if attributes are 
        // removed or added.
        protected final Iterator _i = _list(getAttributeNames()).iterator();
        protected Object _currentKey;

        public void remove()
        {
            if (_currentKey == null)
            {
                throw new NoSuchElementException(
                    "You must call next() at least once");
            }
            AbstractThreadSafeAttributeMap.this.remove(_currentKey);
        }

        public boolean hasNext()
        {
            return _i.hasNext();
        }

        public Object next()
        {
            return _currentKey = _i.next();
        }
    }

    private class Values extends KeySet
    {
        public Iterator iterator()
        {
            return new ValuesIterator();
        }

        public boolean contains(Object o)
        {
            return AbstractThreadSafeAttributeMap.this.containsValue(o);
        }

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

    private class ValuesIterator extends KeyIterator
    {
        public Object next()
        {
            super.next();
            return AbstractThreadSafeAttributeMap.this.get(_currentKey);
        }
    }

    private class EntrySet extends KeySet
    {
        public Iterator iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
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

            return value.equals(AbstractThreadSafeAttributeMap.this.get(key));
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry))
            {
                return false;
            }

            Entry entry = (Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null
                || !value.equals(AbstractThreadSafeAttributeMap.this.get(key)))
            {
                return false;
            }

            return AbstractThreadSafeAttributeMap.this.remove(((Entry) o).getKey()) != null;
        }
    }

    /**
     * Not very efficient since it generates a new instance of <code>Entry</code>
     * for each element and still internaly uses the <code>KeyIterator</code>.
     * It is more efficient to use the <code>KeyIterator</code> directly.
     */
    private class EntryIterator extends KeyIterator
    {
        public Object next()
        {
            super.next();
            // Must create new Entry every time--value of the entry must stay
            // linked to the same attribute name
            return new EntrySetEntry(_currentKey);
        }
    }

    private class EntrySetEntry implements Entry
    {
        private final Object _currentKey;

        public EntrySetEntry(Object currentKey)
        {
            _currentKey = currentKey;
        }

        public Object getKey()
        {
            return _currentKey;
        }

        public Object getValue()
        {
            return AbstractThreadSafeAttributeMap.this.get(_currentKey);
        }

        public Object setValue(Object value)
        {
            return AbstractThreadSafeAttributeMap.this.put(_currentKey, value);
        }

        public int hashCode() {
            return _currentKey == null ? 0 : _currentKey.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof EntrySetEntry))
                return false;
            return _currentKey != null && _currentKey.equals(obj);
        }
    }
}
