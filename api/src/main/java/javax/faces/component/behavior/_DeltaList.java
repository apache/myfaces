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
package javax.faces.component.behavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javax.faces.component.PartialStateHolder;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponentBase;
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
 */
class _DeltaList<T> extends ArrayList<T> implements List<T>, PartialStateHolder, RandomAccess
{
    private static Object[] emptyObjectArray = new Object[]{};

    private boolean _initialStateMarked;
    
    public _DeltaList()
    {
        super();
    }
    
    public _DeltaList(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    public void add(int index, T element)
    {
        clearInitialState();
        super.add(index, element);
    }

    public boolean add(T e)
    {
        clearInitialState();
        return super.add(e);
    }

    public boolean addAll(Collection<? extends T> c)
    {
        clearInitialState();
        return super.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c)
    {
        clearInitialState();
        return super.addAll(index, c);
    }

    public void clear()
    {
        clearInitialState();
        super.clear();
    }

    public T remove(int index)
    {
        clearInitialState();
        return super.remove(index);
    }

    public boolean remove(Object o)
    {
        clearInitialState();
        return super.remove(o);
    }

    public boolean removeAll(Collection<?> c)
    {
        clearInitialState();
        return super.removeAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        clearInitialState();
        return super.retainAll(c);
    }

    public T set(int index, T element)
    {
        clearInitialState();
        return super.set(index, element);
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
        
        if (initialStateMarked())
        {            
            //Restore delta
            Object[] lst = (Object[]) state;
            int j = 0;
            int i = 0;
            while (i < lst.length)
            {
                if (lst[i] instanceof _AttachedDeltaWrapper)
                {
                    //Delta
                    ((StateHolder)super.get(j)).restoreState(context,
                            ((_AttachedDeltaWrapper) lst[i]).getWrappedStateObject());
                    j++;
                }
                else if (lst[i] != null)
                {
                    //Full
                    super.set(j, (T) UIComponentBase.restoreAttachedState(context, lst[i]));
                    j++;
                }
                else
                {
                    super.remove(j);
                }
                i++;
            }
            if (i != j)
            {
                // StateHolder transient objects found, next time save and restore it fully
                //because the size of the list changes.
                clearInitialState();
            }
        }
        else
        {
            //Restore delegate
            Object[] lst = (Object[]) state;
            
            clear();
            ensureCapacity(lst.length);
            
            for (int i = 0; i < lst.length; i++)
            {
                T value = (T) UIComponentBase.restoreAttachedState(context, lst[i]);
                if (value != null)
                {
                    super.add(value);
                }
            }
        }
    }

    public Object saveState(FacesContext context)
    {
        int size = super.size();
        if (initialStateMarked())
        {
            Object [] lst = null;
            boolean nullDelta = true;
            if (size > 0)
            {
                lst = new Object[size];
                for (int i = 0; i < size; i++)
                {
                    Object value = super.get(i);
                    if (value instanceof PartialStateHolder)
                    {
                        //Delta
                        PartialStateHolder holder = (PartialStateHolder) value;
                        if (!holder.isTransient())
                        {
                            Object attachedState = holder.saveState(context);
                            if (attachedState != null)
                            {
                                nullDelta = false;
                            }
                            lst[i] = new _AttachedDeltaWrapper(value.getClass(),
                                attachedState);
                        }
                    }
                    else
                    {
                        //Full
                        lst[i] = UIComponentBase.saveAttachedState(context, value);
                        if (value instanceof StateHolder || value instanceof List)
                        {
                            nullDelta = false;
                        }
                    }
                }
            }
            else
            {
                lst = emptyObjectArray;
            }
            if (nullDelta)
            {
                return null;
            }
            return lst;
        }
        else
        {
            if (size > 0)
            {
                Object [] lst = new Object[size];
                for (int i = 0; i < size; i++)
                {
                    lst[i] = UIComponentBase.saveAttachedState(context, super.get(i));
                }
                return lst;
            }
            else
            {
                return emptyObjectArray;
            }
        }
    }

    public void clearInitialState()
    {
        //Reset delta setting to null
        if (_initialStateMarked)
        {
            _initialStateMarked = false;

            for (int i = 0; i < super.size(); i++)
            {
                T value = super.get(i);
                if (value instanceof PartialStateHolder)
                {
                    ((PartialStateHolder)value).clearInitialState();
                }
            }
        }
    }

    public boolean initialStateMarked()
    {
        return _initialStateMarked;
    }

    public void markInitialState()
    {
        _initialStateMarked = true;

        int size = super.size();
        for (int i = 0; i < size; i++)
        {
            T value = super.get(i);
            if (value instanceof PartialStateHolder)
            {
                ((PartialStateHolder)value).markInitialState();
            }
        }
    }
}
