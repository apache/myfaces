/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.myfaces.component.visit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Rev$ $Date$
 *
 * A proxying class for our partial visit id handling
 * we need this class because
 * a) PartialVisitContext.getIds must return
 *  a valid modifyable set of client ids
 * 
 * b) There are some speed improvements like an
 *  inverse index, which need to change as well
 *  if the ids are changed!
 */
class PartialVisitIdProxy implements Set<String>{

    FacesContext _facesContext;
    Set<String> _ids;
    Map<String, Collection<String>> _inverseCache = new HashMap<String, Collection<String>>();
    Set<String> _forcedIds = new HashSet<String>();
    char _separatorChar;

    public Set<String> getIds() {
        return _ids;
    }

    public Set<String> getForcedIds() {
        return _forcedIds;
    }

    public Map<String, Collection<String>> getInverseCache() {
        return _inverseCache;
    }


    public PartialVisitIdProxy(char separatorChar, Collection<String> ids) {
        _ids = new HashSet<String>(ids);
        _separatorChar = separatorChar;
        initCaches();
    }

    private void removeFromCache(String clientId) {
        char[] ids = clientId.toCharArray();
        
        StringBuilder finalContainer = new StringBuilder(clientId.length());
        for (int cnt = 0; cnt < ids.length; cnt++) {
            if (ids[cnt] == _separatorChar) {
                String containerName = finalContainer.toString();
                Collection<String> cacheEntry = _inverseCache.get(containerName);
                if (cacheEntry == null) {
                    continue;
                }
                if (cacheEntry.contains(clientId)) {
                    cacheEntry.remove(clientId);
                    if (cacheEntry.isEmpty()) {
                        _inverseCache.remove(containerName);
                    }
                }
            }
            finalContainer.append(ids[cnt]);
        }
        _forcedIds.remove(clientId);
    }


    private boolean addToIndex(String clientId) {
        
        //we do not use a split here because we cannot rely on regexps in case of unknown chars
        char[] ids = clientId.toCharArray();
        StringBuilder finalContainer = new StringBuilder(clientId.length());
        boolean added = false;
        for (int cnt = 0; cnt < ids.length; cnt++) {
            if (ids[cnt] == _separatorChar) {
                String containerName = finalContainer.toString();
                Collection<String> cacheEntry = _inverseCache.get(containerName);
                if (cacheEntry == null) {
                    cacheEntry = new HashSet<String>();
                    _inverseCache.put(containerName, cacheEntry);
                }
                cacheEntry.add(clientId);
                added = true;
            } 
            finalContainer.append(ids[cnt]);
        }
        //if not added add the id to the deep scan ids!
        if (!added) {
           return _forcedIds.add(clientId);
        }
        return true;
    }

    /**
     * we have to initialize our caches
     */
    private void initCaches() {
        _inverseCache.clear();
        _forcedIds.clear();

        for (String clientId : _ids) {
            addToIndex(clientId);
        }
    }

    @Override
    public int size() {
        return _ids.size();
    }

    @Override
    public boolean isEmpty() {
        return _ids.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return _ids.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return new ProxyingIterator(_ids.iterator());
    }

    @Override
    public Object[] toArray() {
        return _ids.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return _ids.toArray(a);
    }

    @Override
    public boolean add(String o) {
        if(!_ids.add(o)) {
            return false;
        }
        return addToIndex(o);
    }

    @Override
    public boolean remove(Object o) {
        if(!_ids.remove((String)o)) {
            return false;
        }
        removeFromCache((String)o);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return _ids.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        for(String element: c) {
            if(_ids.add(element)) {
                addToIndex(element);
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        _ids = new HashSet<String>();

        for(Object element: c) {
            _ids.add((String)element);
        }
       initCaches();
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object element: c) {
            remove(element);
        }
        return true;
    }

    @Override
    public void clear() {
       _ids.clear();
       _forcedIds.clear();
       _inverseCache.clear();
    }

    class ProxyingIterator implements Iterator<String> {

        Iterator<String> _delegate = null;
        String _currentValue = null;

        public ProxyingIterator(Iterator delegate) {
            _delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return _delegate.hasNext();
        }

        @Override
        public String next() {
            _currentValue =  _delegate.next();
            return _currentValue;
        }

        @Override
        public void remove() {
           
            _delegate.remove();
            removeFromCache(_currentValue);
        }

    }
}
