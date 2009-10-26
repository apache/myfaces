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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * This class handle deltas on facesListener and validatorList.
 * 
 * It is only used by this methods on UIComponentBase:
 * 
 * addFacesListener
 * broadcast
 * getFacesListeners
 * removeFacesListener
 * 
 * A facesListener could hold PartialStateHolder instances, so it 
 * is necessary to provide convenient methods to track deltas.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _DeltaList<T> implements List<T>, PartialStateHolder
{

    private List<T> _delegate;
    //private UIComponent _component;
    private Map<Object,Boolean> _deltas;
    private boolean _initialStateMarked;
    
    public _DeltaList()
    {
    }
    
    public _DeltaList(List<T> delegate)
    {
        _delegate = delegate;
    }
    
    private boolean _createDeltas()
    {
        if (initialStateMarked())
        {
            if (_deltas == null)
            {
                _deltas = new HashMap<Object, Boolean>(4);
            }
            return true;
        }

        return false;
    }    

    public void add(int index, T element)
    {
        if (_createDeltas())
        {
            _deltas.put(element, Boolean.TRUE);
        }
        _delegate.add(index, element);
    }

    public boolean add(T e)
    {
        if (_createDeltas())
        {
            _deltas.put(e, Boolean.TRUE);
        }
        return _delegate.add(e);
    }

    public boolean addAll(Collection<? extends T> c)
    {
        if (_createDeltas())
        {
            for (Iterator<? extends T> it = c.iterator(); it.hasNext();)
            {
                _deltas.put(it.next(), Boolean.TRUE);
            }
        }        
        return _delegate.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c)
    {
        if (_createDeltas())
        {
            for (Iterator<? extends T> it = c.iterator(); it.hasNext();)
            {
                _deltas.put(it.next(), Boolean.TRUE);
            }
        }        
        return _delegate.addAll(index, c);
    }

    public void clear()
    {
        if (_createDeltas())
        {
            for (Iterator<? extends T> it = _delegate.iterator(); it.hasNext();)
            {
                _deltas.put(it.next(), Boolean.FALSE);
            }
        }        
        _delegate.clear();
    }

    public boolean contains(Object o)
    {
        return _delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        return _delegate.containsAll(c);
    }

    public boolean equals(Object o)
    {
        return _delegate.equals(o);
    }

    public T get(int index)
    {
        return _delegate.get(index);
    }

    public int hashCode()
    {
        return _delegate.hashCode();
    }

    public int indexOf(Object o)
    {
        return _delegate.indexOf(o);
    }

    public boolean isEmpty()
    {
        return _delegate.isEmpty();
    }

    public Iterator<T> iterator()
    {
        return _delegate.iterator();
    }

    public int lastIndexOf(Object o)
    {
        return _delegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator()
    {
        return _delegate.listIterator();
    }

    public ListIterator<T> listIterator(int index)
    {
        return _delegate.listIterator(index);
    }

    public T remove(int index)
    {
        if (_createDeltas())
        {
            _deltas.put(_delegate.get(index), Boolean.FALSE);
        }        
        return _delegate.remove(index);
    }

    public boolean remove(Object o)
    {
        if (_createDeltas())
        {
            _deltas.put(o, Boolean.FALSE);
        }                
        return _delegate.remove(o);
    }

    public boolean removeAll(Collection<?> c)
    {
        if (_createDeltas())
        {
            for (Iterator it = c.iterator(); it.hasNext();)
            {
                _deltas.put(it.next(), Boolean.FALSE);
            }
        }
        return _delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        return _delegate.retainAll(c);
    }

    public T set(int index, T element)
    {
        if (_createDeltas())
        {
            _deltas.put(_delegate.get(index), Boolean.FALSE);
            _deltas.put(element, Boolean.TRUE);
        }
        return _delegate.set(index, element);
    }

    public int size()
    {
        return _delegate == null ? 0 : _delegate.size();
    }

    public List<T> subList(int fromIndex, int toIndex)
    {
        return _delegate.subList(fromIndex, toIndex);
    }

    public Object[] toArray()
    {
        return _delegate.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return _delegate.toArray(a);
    }

    public boolean isTransient()
    {
        return false;
    }

    public void setTransient(boolean newTransientValue)
    {
        throw new UnsupportedOperationException();
    }

    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }
        
        if (_createDeltas())
        {            
            //Restore delta
            Object[] listAsMap = (Object[]) state;
            for (int cnt = 0; cnt < listAsMap.length; cnt += 2)
            {   
                if (listAsMap[cnt] instanceof Boolean)
                {
                    Boolean value = (Boolean) listAsMap[cnt];
                    T key = (T) UIComponentBase.
                        restoreAttachedState(context, listAsMap[cnt+1]);
                    _deltas.put(key,value);
                    if (key != null)
                    {
                        if (value.booleanValue())
                        {
                            _delegate.add(key);
                        }
                        else
                        {
                            _delegate.remove(key);
                        }
                    }
                }
                else if (listAsMap[cnt+1] != null)
                {
                    if (listAsMap[cnt+1] instanceof _AttachedDeltaWrapper)
                    {
                        _AttachedDeltaWrapper wrapper = (_AttachedDeltaWrapper) listAsMap[cnt+1];
                        //Restore delta state
                        ((PartialStateHolder)_delegate.get((Integer)listAsMap[cnt])).restoreState(context, wrapper.getWrappedStateObject());
                    }
                    else
                    {
                        //Replace it
                        _delegate.set((Integer)listAsMap[cnt], (T) UIComponentBase.restoreAttachedState(context, listAsMap[cnt+1]));
                    }
                }
                else if (listAsMap[cnt] != null)
                {
                    _delegate.set((Integer)listAsMap[cnt],null);
                }
            }
        }
        else
        {
            //Restore delegate
            Object[] lst = (Object[]) state;
            _delegate = new ArrayList<T>(lst.length);
            for (int i = 0; i < lst.length; i++)
            {
                _delegate.add((T) UIComponentBase.restoreAttachedState(context, lst[i]));
            }
        }
    }

    public Object saveState(FacesContext context)
    {
        if (initialStateMarked())
        {
            if (_deltas != null)
            {
                //Count stateHolder instances to keep track
                int stateHolderKeyCount = 0;
                for (int i = 0; i < _delegate.size(); i++)
                {
                    T key = _delegate.get(i);
                    if (key instanceof StateHolder && !_deltas.containsKey(key))
                    {
                        stateHolderKeyCount+=2;
                    }
                }
                
                int cnt = 0;
                boolean nullDelta = true;
                Object[] mapArr = (Object[]) new Object[_deltas.size() * 2+stateHolderKeyCount];
                for (Map.Entry<Object, Boolean> entry : _deltas.entrySet())
                {
                    mapArr[cnt] = entry.getValue();
                    Object value = entry.getKey();
                    if (value instanceof StateHolder ||
                        value instanceof List ||
                        !(value instanceof Serializable))
                    {
                        mapArr[cnt+1] = UIComponentBase.saveAttachedState(context, value);
                    }
                    else
                    {
                        mapArr[cnt+1] = value;
                    }
                    cnt += 2;
                    nullDelta = false;
                }
    
                //Deal with StateHolder instances
                for (int i = 0; i < _delegate.size(); i++)
                {
                    T value = _delegate.get(i);
                    if (value instanceof StateHolder && !_deltas.containsKey(value))
                    {
                        mapArr[cnt] = i;
                        if (value instanceof PartialStateHolder)
                        {
                            //Could contain delta, save it as _AttachedDeltaState
                            PartialStateHolder holder = (PartialStateHolder) value;
                            if (holder.isTransient())
                            {                                
                                mapArr[cnt + 1] = null;
                                nullDelta = false;
                            }
                            else
                            {
                                Object savedValue = holder.saveState(context);
                                if (savedValue != null)
                                {
                                    mapArr[cnt+1] = new _AttachedDeltaWrapper(value.getClass(), savedValue);
                                    nullDelta = false;
                                }
                                else
                                {
                                    mapArr[cnt] = null;
                                    mapArr[cnt+1] = null;
                                }
                            }
                        }
                        else
                        {
                            mapArr[cnt+1] = UIComponentBase.saveAttachedState(context, value);
                            nullDelta = false;
                        }
                        cnt+=2;
                    }
                }
                if (nullDelta)
                {
                    return null;
                }
                return mapArr;
            }
            else
            {
                //Count stateHolder instances to keep track
                int stateHolderKeyCount = 0;
                for (int i = 0; i < _delegate.size(); i++)
                {
                    T key = _delegate.get(i);
                    if (key instanceof StateHolder)
                    {
                        stateHolderKeyCount += 2;
                    }
                }

                int cnt = 0;
                Object[] mapArr = (Object[]) new Object[stateHolderKeyCount];
                boolean nullDelta = true;
                //Deal with StateHolder instances
                for (int i = 0; i < _delegate.size(); i++)
                {
                    T value = _delegate.get(i);
                    if (value instanceof StateHolder)
                    {
                        mapArr[cnt] = i;
                        if (value instanceof PartialStateHolder)
                        {
                            //Could contain delta, save it as _AttachedDeltaState
                            PartialStateHolder holder = (PartialStateHolder) value;
                            if (holder.isTransient())
                            {                                
                                mapArr[cnt + 1] = null;
                                nullDelta = false;
                            }
                            else
                            {
                                Object savedValue = holder.saveState(context);
                                if (savedValue != null)
                                {
                                    mapArr[cnt+1] = new _AttachedDeltaWrapper(value.getClass(), savedValue);
                                    nullDelta = false;
                                }
                                else
                                {
                                    mapArr[cnt] = null;
                                    mapArr[cnt+1] = null;
                                }
                            }
                        }
                        else
                        {
                            mapArr[cnt+1] = UIComponentBase.saveAttachedState(context, value);
                            nullDelta = false;
                        }
                        cnt+=2;
                    }
                }
                if (nullDelta)
                {
                    return null;
                }
                return mapArr;                
            }
        }
        else
        {
            Object [] lst = new Object[this.size()];
            int i = 0;
            for (Iterator it = _delegate.iterator(); it.hasNext();)
            {
                lst[i] = UIComponentBase.saveAttachedState(context, it.next());
                i++;
            }
            return lst;
        }
    }

    @Override
    public void clearInitialState()
    {
        //Reset delta setting to null
        _deltas = null;
        _initialStateMarked = false;
        if (_delegate != null)
        {
            for (T value : _delegate)
            {
                if (value instanceof PartialStateHolder)
                {
                    ((PartialStateHolder)value).clearInitialState();
                }
            }
        }
    }

    @Override
    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    @Override
    public void markInitialState()
    {
        _initialStateMarked = true;
        if (_delegate != null)
        {
            for (T value : _delegate)
            {
                if (value instanceof PartialStateHolder)
                {
                    ((PartialStateHolder)value).markInitialState();
                }
            }
        }
    }
}
