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
package jakarta.faces.component.behavior;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.component.StateHelper;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import java.util.function.Supplier;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * A delta enabled state holder implementing the StateHolder Interface. 
 * <p>
 * Components implementing the PartialStateHolder interface have an initial state
 * and delta states, the initial state is the one holding all root values
 * and deltas store differences to the initial states
 * </p>
 * <p>
 * For components not implementing partial state saving only the initial states are
 * of importance, everything is stored and restored continuously there
 * </p> 
 * <p>
 * The state helper seems to have three internal storage mechanisms:
 * one being a list which stores plain values,
 * one being a key value pair which stores key values in maps
 * add serves the plain list type while put serves the 
 * key value type, 
 * the third is the value which has to be stored plainly as is!
 * </p>
 * In other words, this map can be seen as a composite map. It has two maps: 
 * initial state map and delta map.
 * <p> 
 * If delta map is used (method component.initialStateMarked() ), 
 * base or initial state map cannot be changed, since all changes 
 * should be tracked on delta map.
 * </p>
 * <p> 
 * The intention of this class is just hold property values
 * and do a clean separation between initial state and delta.
 * </p>
 * <p>
 * The context from this class comes and that should be taken into account
 * is this:
 * </p>
 * <p> 
 * First request:
 * </p>
 * <ul>
 *   <li> A new template is created (using 
 *   jakarta.faces.view.ViewDeclarationLanguage.buildView method)
 *   and component.markInitialState is called from its related TagHandler classes 
 *  (see jakarta.faces.view.facelets.ComponentHandler ).
 *   When this method is executed, the component tree was populated from the values
 *   set in the facelet abstract syntax tree (or in other words composition of 
 *   facelets templates). </li>
 *   <li> From this point all updates on the variables are considered "delta". </li>
 *   <li> SaveState, if initialStateMarked is true, only delta is saved. </li>
 * </ul>
 * <p>
 * Second request (and next ones)
 * </p>
 * <ul>
 *   <li> A new template is created and component.markInitialState is called from
 *   its related TagHandler classes again. In this way, components like c:forEach 
 *   or c:if, that add or remove components could notify about this and handle 
 *   them properly (see jakarta.faces.view.StateManagementStrategy). Note that a 
 *   component restored using this method is no different as the same component 
 *   at the first request at the same time. </li>
 *   <li> A call for restoreState is done, passing the delta as object value. If no 
 *   delta, the state is complete and no call is triggered. </li>
 *   <li> Lifecycle occur, changing the necessary stuff. </li>
 *   <li> SaveState, if initialStateMarked is true, only delta is saved. </li>
 * </ul>
 * <p>
 * From the previous analysis, the following conclusions arise:
 * <ul>
 *   <li>This class only needs to keep track of delta changes, so when 
 *   restoreState/saveState is called, the right objects are passed.</li>
 *   <li>UIComponent.clearInitialState is used to reset the partial
 *   state holder to a non delta state, so the state to be saved by
 *   saveState is no longer a delta instead is a full state. If a call
 *   to clearInitialState occur it is not expected a call for 
 *   UIComponent.markInitialState occur on the current request.</li>
 *   <li>The state is handled in the same way on UIData, so components
 *   inside UIData share its state on all rows. There is no way to save 
 *   delta per row.</li>
 *   <li>The map backed by method put(Serializable,String,Object) is
 *   a replacement of UIComponentBase.attributesMap and UIComponent.bindings map.
 *   Note that on jsf 1.2, instances saved on attributesMap should not be
 *   StateHolder, but on jsf 2.0 it is possible to have it. PartialStateHolder
 *   instances are not handled in this map, or in other words delta state is not
 *   handled in this classes (markInitialState and clearInitialState is not propagated).</li>
 *   <li>The list backed by method add(Serializable,Object) should be (is not) a 
 *   replacement of UIComponentBase.facesListeners, but note that StateHelper
 *   does not implement PartialStateHolder, and facesListener could have instances
 *   of that class that needs to be notified when UIComponent.markInitialState or
 *   UIComponent.clearInitialState is called, or in other words facesListeners
 *   should deal with PartialStateHolder instances.</li>
 *   <li>The list backed by method add(Serializable,Object) is 
 *   a replacement of UIViewRoot.phaseListeners list. Note that instances of
 *   PhaseListener are not expected to implement StateHolder or PartialStateHolder.</li>
 * </ul>
 * </p>
 * <p>
 * NOTE: The current implementation of StateHelper on RI does not handle
 * stateHolder values internally. To prevent problems when developers create
 * custom components we should do this too. But anyway, the code that 
 * handle this case should be let here as comment, if some day this feature
 * is provided. Note than stateHolder aware properties like converter,
 * validator or listeners should deal with StateHolder or PartialStateHolder
 * on component classes. 
 */
class _DeltaStateHelper<A extends AjaxBehavior> implements StateHelper
{

    /**
     * We need to hold a component instance because:
     * 
     * - The component is the one who knows if we are on initial or delta mode
     * - eval assume calls to component.ValueExpression
     */
    private A _target;

    /**
     * This map holds the full current state
     */
    private Map<Serializable, Object> _fullState;

    /**
     * This map only keep track of delta changes to be saved
     */
    private Map<Serializable, Object> _deltas;

    private boolean _transient = false;

    public _DeltaStateHelper(A target)
    {
        super();
        this._target = target;
        this._fullState = new HashMap<>();
        this._deltas = null;
    }

    /**
     * Used to create delta map on demand
     * 
     * @return
     */
    private boolean _createDeltas()
    {
        if (isInitialStateMarked())
        {
            if (_deltas == null)
            {
                _deltas = new HashMap<>(2, 1f);
            }
            return true;
        }

        return false;
    }
    
    protected boolean isInitialStateMarked()
    {
        return _target.initialStateMarked();
    }

    @Override
    public void add(Serializable key, Object value)
    {
        if (_createDeltas())
        {
            //Track delta case
            Map<Object, Boolean> deltaListMapValues = (Map<Object, Boolean>) _deltas.get(key);
            if (deltaListMapValues == null)
            {
                deltaListMapValues = new InternalDeltaListMap<>(3, 1f);
                _deltas.put(key, deltaListMapValues);
            }

            deltaListMapValues.put(value, Boolean.TRUE);
        }

        //Handle change on full map
        List<Object> fullListValues = (List<Object>) _fullState.get(key);
        if (fullListValues == null)
        {
            fullListValues = new InternalList<>(3);
            _fullState.put(key, fullListValues);
        }

        fullListValues.add(value);
    }

    @Override
    public <T> T eval(Serializable key)
    {
        T returnValue = (T) _fullState.get(key);
        if (returnValue != null)
        {
            return returnValue;
        }
        ValueExpression expression = _target.getValueExpression(key.toString());
        if (expression != null)
        {
            return expression.getValue(_target.getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public <T> T eval(Serializable key, T defaultValue)
    {
        T returnValue = (T) _fullState.get(key);

        if (returnValue == null)
        {
            ValueExpression expression = _target.getValueExpression(key.toString());
            if (expression != null)
            {
                returnValue = expression.getValue(_target.getFacesContext().getELContext());
            }
        }

        if (returnValue == null)
        {
            returnValue = defaultValue;
        }

        return returnValue;
    }

    /**
     *
     * @param key
     * @param defaultValueSupplier
     * @return
     *
     * @since 4.0
     */
    @Override
    public <T> T eval(Serializable key, Supplier<T> defaultValueSupplier)
    {
        T returnValue = (T) _fullState.get(key);
        
        if (returnValue == null)
        {
            ValueExpression expression = _target.getValueExpression(key.toString());
            if (expression != null)
            {
                returnValue = expression.getValue(_target.getFacesContext().getELContext());
            }
        }

        if (returnValue == null && defaultValueSupplier != null)
        {
            returnValue = defaultValueSupplier.get();
        }

        return returnValue;
    }

    @Override
    public <T> T get(Serializable key)
    {
        return (T) _fullState.get(key);
    }

    @Override
    public <T> T put(Serializable key, T value)
    {
        T returnValue = null;
        if (_createDeltas())
        {
            if (_deltas.containsKey(key))
            {
                returnValue = (T) _deltas.put(key, value);
                _fullState.put(key, value);
            }
            else if (value == null && !_fullState.containsKey(key))
            {
                returnValue = null;
            }
            else
            {
                _deltas.put(key, value);
                returnValue = (T) _fullState.put(key, value);
            }
        }
        else
        {
            returnValue = (T) _fullState.put(key, value);
        }
        return returnValue;
    }

    public <T> T put(Serializable key, String mapKey, T value)
    {
        boolean returnSet = false;
        T returnValue = null;
        if (_createDeltas())
        {
            //Track delta case
            Map<String, Object> mapValues = (Map<String, Object>) _deltas.get(key);
            if (mapValues == null)
            {
                mapValues = new InternalMap<>();
                _deltas.put(key, mapValues);
            }

            if (mapValues.containsKey(mapKey))
            {
                returnValue = (T) mapValues.put(mapKey, value);
                returnSet = true;
            }
            else
            {
                mapValues.put(mapKey, value);
            }
        }

        //Handle change on full map
        Map<String, Object> mapValues = (Map<String, Object>) _fullState.get(key);
        if (mapValues == null)
        {
            mapValues = new InternalMap<>();
            _fullState.put(key, mapValues);
        }

        if (returnSet)
        {
            mapValues.put(mapKey, value);
        }
        else
        {
            returnValue = (T) mapValues.put(mapKey, value);
        }
        return returnValue;
    }

    @Override
    public Object remove(Serializable key)
    {
        Object returnValue = null;
        if (_createDeltas())
        {
            if (_deltas.containsKey(key))
            {
                // Keep track of the removed values using key/null pair on the delta map
                returnValue = _deltas.put(key, null);
                _fullState.remove(key);
            }
            else
            {
                // Keep track of the removed values using key/null pair on the delta map
                _deltas.put(key, null);
                returnValue = _fullState.remove(key);
            }
        }
        else
        {
            returnValue = _fullState.remove(key);
        }
        return returnValue;
    }

    @Override
    public Object remove(Serializable key, Object valueOrKey)
    {
        // Comment by lu4242 : The spec javadoc says if it is a Collection 
        // or Map deal with it. But the intention of this method is work 
        // with add(?,?) and put(?,?,?), this ones return instances of 
        // InternalMap and InternalList to prevent mixing, so to be 
        // consistent we'll cast to those classes here.
        
        Object collectionOrMap = _fullState.get(key);
        Object returnValue = null;
        if (collectionOrMap instanceof InternalMap)
        {
            if (_createDeltas())
            {
                returnValue = _removeValueOrKeyFromMap(_deltas, key, valueOrKey, true);
                _removeValueOrKeyFromMap(_fullState, key, valueOrKey, false);
            }
            else
            {
                returnValue = _removeValueOrKeyFromMap(_fullState, key, valueOrKey, false);
            }
        }
        else if (collectionOrMap instanceof InternalList)
        {
            if (_createDeltas())
            {
                returnValue = _removeValueOrKeyFromCollectionDelta(_deltas, key, valueOrKey);
                _removeValueOrKeyFromCollection(_fullState, key, valueOrKey);
            }
            else
            {
                returnValue = _removeValueOrKeyFromCollection(_fullState, key, valueOrKey);
            }
        }
        return returnValue;
    }

    private static Object _removeValueOrKeyFromCollectionDelta(
            Map<Serializable, Object> stateMap, Serializable key, Object valueOrKey)
    {
        Object returnValue = null;
        Map<Object, Boolean> c = (Map<Object, Boolean>) stateMap.get(key);
        if (c != null)
        {
            if (c.containsKey(valueOrKey))
            {
                returnValue = valueOrKey;
            }
            c.put(valueOrKey, Boolean.FALSE);
        }
        return returnValue;
    }

    private static Object _removeValueOrKeyFromCollection(
            Map<Serializable, Object> stateMap, Serializable key, Object valueOrKey)
    {
        Object returnValue = null;
        Collection c = (Collection) stateMap.get(key);
        if (c != null)
        {
            if (c.remove(valueOrKey))
            {
                returnValue = valueOrKey;
            }
            if (c.isEmpty())
            {
                stateMap.remove(key);
            }
        }
        return returnValue;
    }

    private static Object _removeValueOrKeyFromMap(
            Map<Serializable, Object> stateMap, Serializable key, Object valueOrKey, boolean delta)
    {
        if (valueOrKey == null)
        {
            return null;
        }

        Object returnValue = null;
        Map<String, Object> map = (Map<String, Object>) stateMap.get(key);
        if (map != null)
        {
            if (delta)
            {
                // Keep track of the removed values using key/null pair on the delta map
                returnValue = map.put((String) valueOrKey, null);
            }
            else
            {
                returnValue = map.remove(valueOrKey);
            }

            if (map.isEmpty())
            {
                stateMap.put(key, null);
            }
        }
        return returnValue;
    }

    @Override
    public boolean isTransient()
    {
        return _transient;
    }

    /**
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
        Map serializableMap = (isInitialStateMarked()) ? _deltas : _fullState;
        if (serializableMap == null || serializableMap.isEmpty())
        {
            return null;
        }

        Map.Entry<Serializable, Object> entry;
        Object[] retArr = new Object[serializableMap.entrySet().size() * 2];

        Iterator<Map.Entry<Serializable, Object>> it = serializableMap.entrySet().iterator();
        int cnt = 0;
        while (it.hasNext())
        {
            entry = it.next();
            retArr[cnt] = entry.getKey();

            Object value = entry.getValue();
            
            // The condition in which the call to saveAttachedState
            // is to handle List, StateHolder or non Serializable instances.
            // we check it here, to prevent unnecessary calls.
            if (value instanceof StateHolder ||
                value instanceof List ||
                !(value instanceof Serializable))
            {
                Object savedValue = saveAttachedState(context, value);
                retArr[cnt + 1] = savedValue;
            }
            else
            {
                retArr[cnt + 1] = value;
            }
            cnt += 2;
        }
    
        return retArr;
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }

        Object[] serializedState = (Object[]) state;
        
        if (!isInitialStateMarked() && !_fullState.isEmpty())
        {
            _fullState.clear();
            if(_deltas != null)
            {
                _deltas.clear();
            }
        }

        for (int cnt = 0; cnt < serializedState.length; cnt += 2)
        {
            Serializable key = (Serializable) serializedState[cnt];
            Object savedValue = restoreAttachedState(context, serializedState[cnt + 1]);

            if (isInitialStateMarked())
            {
                if (savedValue instanceof InternalDeltaListMap)
                {
                    for (Map.Entry<Object, Boolean> mapEntry : ((Map<Object, Boolean>) savedValue).entrySet())
                    {
                        boolean addOrRemove = mapEntry.getValue();
                        if (addOrRemove)
                        {
                            //add
                            this.add(key, mapEntry.getKey());
                        }
                        else
                        {
                            //remove
                            this.remove(key, mapEntry.getKey());
                        }
                    }
                }
                else if (savedValue instanceof InternalMap)
                {
                    for (Map.Entry<String, Object> mapEntry : ((Map<String, Object>) savedValue).entrySet())
                    {
                        this.put(key, mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                /*
                else if (savedValue instanceof _AttachedDeltaWrapper)
                {
                    _AttachedStateWrapper wrapper = (_AttachedStateWrapper) savedValue;
                    //Restore delta state
                    ((PartialStateHolder)_fullState.get(key)).restoreState(context, wrapper.getWrappedStateObject());
                    //Add this key as StateHolder key 
                    _stateHolderKeys.add(key);
                }
                */
                else
                {
                    put(key, savedValue);
                }
            }
            else
            {
                put(key, savedValue);
            }
        }
    }

    @Override
    public void setTransient(boolean transientValue)
    {
        _transient = transientValue;
    }

    //We use our own data structures just to make sure
    //nothing gets mixed up internally
    static class InternalMap<K, V> extends HashMap<K, V> implements StateHolder
    {
        public InternalMap()
        {
            super();
        }

        public InternalMap(int initialCapacity, float loadFactor)
        {
            super(initialCapacity, loadFactor);
        }

        public InternalMap(Map<? extends K, ? extends V> m)
        {
            super(m);
        }

        public InternalMap(int initialSize)
        {
            super(initialSize);
        }

        @Override
        public boolean isTransient()
        {
            return false;
        }

        @Override
        public void setTransient(boolean newTransientValue)
        {
            // No op
        }

        @Override
        public void restoreState(FacesContext context, Object state)
        {
            Object[] listAsMap = (Object[]) state;
            for (int cnt = 0; cnt < listAsMap.length; cnt += 2)
            {
                this.put((K) listAsMap[cnt], (V) UIComponentBase.restoreAttachedState(context, listAsMap[cnt + 1]));
            }
        }

        @Override
        public Object saveState(FacesContext context)
        {
            int cnt = 0;
            Object[] mapArr = new Object[this.size() * 2];
            for (Map.Entry<K, V> entry : this.entrySet())
            {
                mapArr[cnt] = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof StateHolder ||
                    value instanceof List ||
                    !(value instanceof Serializable))
                {
                    mapArr[cnt + 1] = saveAttachedState(context, value);
                }
                else
                {
                    mapArr[cnt + 1] = value;
                }
                cnt += 2;
            }
            return mapArr;
        }
    }

    /**
     * Map used to keep track of list changes 
     */
    static class InternalDeltaListMap<K, V> extends InternalMap<K, V>
    {

        public InternalDeltaListMap()
        {
            super();
        }

        public InternalDeltaListMap(int initialCapacity, float loadFactor)
        {
            super(initialCapacity, loadFactor);
        }

        public InternalDeltaListMap(int initialSize)
        {
            super(initialSize);
        }

        public InternalDeltaListMap(Map<? extends K, ? extends V> m)
        {
            super(m);
        }
    }

    static class InternalList<T> extends ArrayList<T> implements StateHolder
    {
        public InternalList()
        {
            super();
        }

        public InternalList(Collection<? extends T> c)
        {
            super(c);
        }

        public InternalList(int initialSize)
        {
            super(initialSize);
        }

        @Override
        public boolean isTransient()
        {
            return false;
        }

        @Override
        public void setTransient(boolean newTransientValue)
        {
        }

        @Override
        public void restoreState(FacesContext context, Object state)
        {
            Object[] listAsArr = (Object[]) state;
            //since all other options would mean dual iteration 
            //we have to do it the hard way
            for (Object elem : listAsArr)
            {
                add((T) restoreAttachedState(context, elem));
            }
        }

        @Override
        public Object saveState(FacesContext context)
        {
            Object[] values = new Object[size()];
            for (int i = 0; i < size(); i++)
            {
                Object value = get(i);
                
                if (value instanceof StateHolder ||
                    value instanceof List ||
                    !(value instanceof Serializable))
                {
                    values[i] = saveAttachedState(context, value);
                }
                else
                {
                    values[i] = value;
                }                
            }
            return values;
        }
    }
    
    private static Object saveAttachedState(FacesContext context, Object attachedObject)
    {
        if (context == null)
        {
            throw new NullPointerException ("context");
        }
        
        if (attachedObject == null)
        {
            return null;
        }
        // StateHolder interface should take precedence over
        // List children
        if (attachedObject instanceof StateHolder holder)
        {
            if (holder.isTransient())
            {
                return null;
            }

            return new _AttachedStateWrapper(attachedObject.getClass(), holder.saveState(context));
        }        
        else if (attachedObject instanceof List list)
        {
            List<Object> lst = new ArrayList<Object>(list.size());
            for (Object item : list)
            {
                if (item != null)
                {
                    lst.add(saveAttachedState(context, item));
                }
            }

            return new _AttachedListStateWrapper(lst);
        }
        else if (attachedObject instanceof Serializable)
        {
            return attachedObject;
        }
        else
        {
            return new _AttachedStateWrapper(attachedObject.getClass(), null);
        }
    }

    private static Object restoreAttachedState(FacesContext context, Object stateObj) throws IllegalStateException
    {
        Assert.notNull(context, "context");

        if (stateObj == null)
        {
            return null;
        }
        if (stateObj instanceof _AttachedListStateWrapper wrapper)
        {
            List<Object> lst = wrapper.getWrappedStateList();
            List<Object> restoredList = new ArrayList<Object>(lst.size());
            for (Object item : lst)
            {
                restoredList.add(restoreAttachedState(context, item));
            }
            return restoredList;
        }
        else if (stateObj instanceof _AttachedStateWrapper wrapper)
        {
            Class<?> clazz = wrapper.getClazz();
            Object restoredObject;
            try
            {
                restoredObject = clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException("Could not restore StateHolder of type " + clazz.getName()
                        + " (missing no-args constructor?)", e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            if (restoredObject instanceof StateHolder holder)
            {
                Object wrappedState = wrapper.getWrappedStateObject();
                holder.restoreState(context, wrappedState);
            }
            return restoredObject;
        }
        else
        {
            return stateObj;
        }
    }
}