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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * A delta enabled state holder implementing the StateHolder Interface
 *
 * components implementing the PartalStateHolder interface have an initial state
 * and delta states, the initial state is the one holding all root values
 * and deltas store differences to the initial states
 *
 * for components not implementing partial state saving only the initial states are
 * of importance, everything is stored and restored continously there
 *
 * The state helper seems to have three internal storage mechanisms
 * one being a list which stores plain values
 * one being a key value pair which stores key values in maps
 * add serves the plain list type while put serves the 
 * key value type
 *
 * the third is the value which has to be stored plainly as is!
 *
 *
 * The flow probably goes following
 * restore -> restore initial state done from the outside
 * switch component to -> initialState = true
 * store the deltas as well!
 *
 * the main save and restore works only on the deltas
 * as far as it seems!
 *
 * we are keeping two states the full state and the delta state
 * the full state must access all values, full + delta
 * and the delta state should keep the states only if initialState is set
 *
 * @author Werner Punz (latest modification by $Author$)
 * @version $Rev$ $Date$
 */
public class _DeltaStateHelper implements StateHelper
{

    UIComponent _component = null;
    Map<Serializable, Object> _fullState = null;
    Map<Serializable, Object> _deltas = null;
    Set<Object> _deleted = null;
    boolean _transient = false;
    static final String INTERNAL_MAP_KEY = "_MYFACES._IMAP";
    static final String INTERNAL_LIST_KEY = "_MYFACES._ILIST";
    static final String INTERNAL_DELETED_KEY = "_MYFACES._DELETED";

    public _DeltaStateHelper(UIComponent component)
    {
        this._component = component;
        _fullState = new HashMap<Serializable, Object>();
        //we only can store the deltas if the component is instance of partial state holder
        //but as it seems the latest specs already have enforced PartialStateHolder on UIComponent
        //initialStateMarked is responsible for determination if we have partial saving

        _deltas = new HashMap<Serializable, Object>();
        _transient = (component != null) ? component.isTransient() : true;
    }

    protected boolean isInitalStateMarked()
    {
        return _component.initialStateMarked();
    }

    /**
     * stores the object in an internal list if not present in the detal map
     */
    @Override
    public void add(Serializable key, Object value)
    {
        if (_deleted != null)
        {
            _deleted.remove(key);
        }
        if (isInitalStateMarked())
        {

            List<Object> deltaStorageList = (List) _deltas.get(key);
            if (deltaStorageList == null)
            {
                deltaStorageList = new InternalList(3);
            }
            deltaStorageList.add(value);
            _deltas.put(key, deltaStorageList);

        }
        List<Object> fullStorageList = (List) _fullState.get(key);
        if (fullStorageList == null)
        {
            fullStorageList = new InternalList(3);
        }
        fullStorageList.add(value);
        _fullState.put(key, fullStorageList);
    }

    @Override
    public Object eval(Serializable key)
    {
        return eval(key, null);
    }

    @Override
    /**
     * returns a given value or the result of a value expression
     * @param key  the key or value expression to be evaluated
     * @return the result of the eval or the default value if
     *          the value is not present in our states
     */
    public Object eval(Serializable key, Object defaultValue)
    {
        Object retVal = get(key);
        if (retVal != null)
        {
            return retVal;
        }
        //not found lets do the eval of a possible value expression
        ValueExpression expr = _component.getValueExpression(key.toString());
        if (expr == null)
        {
            return defaultValue;
        }
        retVal = expr.getValue(_component.getFacesContext().getELContext());
        return (retVal == null) ? defaultValue : retVal;
    }

    @Override
    public Object get(Serializable key)
    {
        return _fullState.get(key);
    }

    @Override
    /**
     * puts an object into the data structures with a given key
     *
     * @param key the key for the mapping
     * @param value the value to be stored
     * @returns the old value if present
     */
    public Object put(Serializable key, Object value)
    {
        if (_deleted != null)
        {
            _deleted.remove(key);
        }
        if (isInitalStateMarked())
        {
            //delta tracking is on
            Object oldValue = _deltas.put(key, value);
            if (oldValue == null)
            {
                oldValue = _fullState.put(key, value);
            }
            return oldValue;
        }

        return _fullState.put(key, value);
    }

    /**
     * puts a value into the internal data structures and the value has to be map
     * mapped via mapkey,
     * @param key the key for the mapping
     * @param mapKey the internal map key for the mapping
     * @returns the old value of key->mapKey if present!
     */
    @Override
    public Object put(Serializable key, String mapKey, Object value)
    {
        Object oldValue = null;
        if (_deleted != null)
        {
            _deleted.remove(key);
        }
        if (isInitalStateMarked())
        {
            oldValue = _putMap(_deltas, key, mapKey, value);
            if (oldValue == null)
            {
                oldValue = _putMap(_fullState, key, mapKey, value);

            }
            return oldValue;
        }

        //either no initial state or no delta state saving
        return _putMap(_fullState, key, mapKey, value);
    }

    @Override
    public Object remove(Serializable key)
    {
        Object deltaRetVal = _deltas.remove(key);
        Object initRetVal = _fullState.remove(key);
        //delta if given is always newer than init!
        if (_deleted == null)
        {
            _deleted = new HashSet<Object>();
        }
        _deleted.add(key);
        return (deltaRetVal != null) ? deltaRetVal : initRetVal;
    }

    @Override
    public Object remove(Serializable key, Object valueOrKey)
    {
        Object deltaDS = _deltas.get(key);
        Object initDS = _fullState.get(key);

        Object deltaRetVal = null;
        Object initRetVal = null;

        if (deltaDS != null)
        {
            deltaRetVal = _removeFromCollection(_deltas, key, deltaDS,
                    valueOrKey);
        }
        if (initDS != null)
        {
            initRetVal = _removeFromCollection(_fullState, key, initDS,
                    valueOrKey);
        }

        if (_deleted == null)
        {
            _deleted = new HashSet<Object>();
        }

        if (!_deltas.containsKey(key))
        {
            _deleted.add(key);
        }
        return (deltaRetVal != null) ? deltaRetVal : initRetVal;
    }

    /*
     * Serializing cod
     * the serialized data structure consists of key value pairs unless the value itself is an internal array
     * or a map in case of an internal array or map the value itself is another array with its initial value
     * myfaces.InternalArray, myfaces.internalMap
     *
     * the internal Array is then mapped to another array
     *
     * the internal Map again is then mapped to a map with key value pairs
     *
     *
     */
    @Override
    public Object saveState(FacesContext context)
    {
        Map serializableMap = (isInitalStateMarked()) ? _deltas : _fullState;
        Set<Object> deltaDeleted = (isInitalStateMarked()) ? _deleted : null;

        Map.Entry<Serializable, Object> entry;
        //entry == key, value, key, value
        Object[] retArr = new Object[serializableMap.entrySet().size() * 2
                + ((_deleted != null && _deleted.size() > 0) ? 2 : 0)];

        Iterator<Map.Entry<Serializable, Object>> it = serializableMap
                .entrySet().iterator();
        int cnt = 0;
        while (it.hasNext())
        {
            entry = it.next();
            retArr[cnt] = entry.getKey();
            if (entry instanceof InternalList)
            {
                //TODO add list serialisation code
                retArr[cnt + 1] = serializeOneDimDS((InternalList) entry);
            }
            else if (entry instanceof InternalMap)
            {
                //TODO add map serialisation code here
                retArr[cnt + 1] = serializeInternalMap((InternalMap) entry);
            }
            else
            {

                retArr[cnt + 1] = (Serializable) entry.getValue();
            }
            cnt += 2;

        }

        //we now store the deleted deltas as well, we cannot handle deleted in our map alone!
        if (deltaDeleted != null && deltaDeleted.size() > 0)
        {
            Object[] serializedDeletes = serializeOneDimDS(_deleted);
            retArr[cnt] = INTERNAL_DELETED_KEY;
            retArr[cnt + 1] = serializedDeletes;
        }
        return retArr;
    }

    /**
     * serializes a one dimensional data structure (array, list, set)
     *
     * @param entry the one dimensional collection to be serialized
     * @return an array representation of the collection with two entries
     * the first entry an identifier key and the second entry an array
     * of elements which represent our collection
     */
    private Object[] serializeOneDimDS(Collection<Object> entry)
    {
        Object[] retVal = new Object[2]; //array with two elements one the marker second the array
        retVal[0] = _DeltaStateHelper.INTERNAL_LIST_KEY;
        retVal[1] = entry.toArray(new Object[entry.size()]);

        return retVal;
    }

    private void deserializeOneDimDS(Object param, Collection target)
    {
        Object[] saveState = (Object[]) param;
        //first element list marker, already processed!
        Object[] listAsArr = (Object[]) saveState[1];

        //since all other options would mean dual iteration we have to do it the hard way
        for (Object elem : listAsArr)
        {
            target.add(elem);
        }

    }

    private InternalMap deserializeInternalMap(Object param)
    {
        Object[] saveState = (Object[]) param;
        Object[] listAsMap = (Object[]) saveState[1];

        InternalMap retVal = new InternalMap(listAsMap.length / 2);
        for (int cnt = 0; cnt < listAsMap.length; cnt += 2)
        {
            retVal.put((String) listAsMap[cnt], listAsMap[cnt + 1]);
        }
        return retVal;
    }

    private Object[] serializeInternalMap(Map<String, Object> map)
    {
        Object[] retVal = new Object[2];
        retVal[0] = _DeltaStateHelper.INTERNAL_MAP_KEY;

        int cnt = 0;
        Object[] mapArr = new Object[map.size()];
        retVal[1] = mapArr;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            mapArr[cnt] = entry.getKey();
            mapArr[cnt + 1] = entry.getValue();
            cnt += 2;
        }
        return retVal;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Object[] serializedState = (Object[]) state;

        for (int cnt = 0; cnt < serializedState.length; cnt += 2)
        {
            Serializable key = (Serializable) serializedState[cnt];
            Object value = serializedState[cnt + 1];

            if (key instanceof String
                    && ((String) key).equals(INTERNAL_DELETED_KEY))
            {
                _deleted = new HashSet<Object>();
                deserializeOneDimDS(value, _deleted);
            }
            else if (value instanceof String
                    && ((String) value).equals(INTERNAL_LIST_KEY))
            {
                Object[] valArr = (Object[]) ((Object[]) value)[1];
                InternalList target = new InternalList(valArr.length * 2);
                deserializeOneDimDS(value, target);
                put(key, target);
            }
            else if (value instanceof String
                    && ((String) value).equals(INTERNAL_MAP_KEY))
            {
                value = deserializeInternalMap(value);
                put(key, value);
            }
        }
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }

    @Override
    public void setTransient(boolean newTransientValue)
    {
        _transient = newTransientValue;
    }

    private Object _putMap(Map rootMap, Serializable key, String mapKey,
            Object value)
    {
        Object oldValue = rootMap.get(key);
        //if no delta is found we add it to both DS
        if (oldValue == null)
        {
            Map storageMap = new InternalMap(3);
            storageMap.put(mapKey, value);
            return rootMap.put(key, storageMap);
        }
        else
        {
            return ((Map) oldValue).put(mapKey, value);
        }
    }

    private Object _removeFromCollection(Map initialMap, Serializable key,
            Object dataStructure, Object valueOrKey)
    {
        Object retVal = null;
        if (dataStructure != null)
        {
            if (dataStructure instanceof InternalList)
            {
                retVal = ((InternalList) dataStructure).remove(valueOrKey);
                if (((Collection) dataStructure).isEmpty())
                {
                    initialMap.remove(key);
                    if (_deleted == null)
                    {
                        _deleted = new HashSet<Object>();
                    }
                    _deleted.add(key);

                }
            }
            else if (dataStructure instanceof InternalMap)
            {
                retVal = ((InternalMap) dataStructure).remove(valueOrKey);
                if (((InternalMap) dataStructure).isEmpty())
                {
                    initialMap.remove(key);
                    if (_deleted == null)
                    {
                        _deleted = new HashSet<Object>();
                    }
                    _deleted.add(key);
                }
            }
            else
            {
                retVal = dataStructure;
            }
        }
        return retVal;
    }

    //We use our own data structures just to make sure
    //nothing gets mixed up internally
    class InternalMap extends HashMap
    {

        public InternalMap(int initialSize)
        {
            super(initialSize);
        }
    }

    class InternalList extends ArrayList
    {

        public InternalList(int initialSize)
        {
            super(initialSize);
        }
    }
}
