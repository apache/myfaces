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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.myfaces.core.api.shared.lang.Assert;

class _ComponentChildrenList extends ArrayList<UIComponent> implements Serializable
{
    private static final long serialVersionUID = -6775078929331154224L;
    private UIComponent _component;

    _ComponentChildrenList(UIComponent component)
    {
        super(4);
        _component = component;
    }

    @Override
    public UIComponent get(int index)
    {
        return super.get(index);
    }

    @Override
    public int size()
    {
        return super.size();
    }

    @Override
    public UIComponent set(int index, UIComponent value)
    {
        Assert.notNull(value, "value");
        removeChildrenFromParent(value);
        UIComponent child = super.set(index, value);
        if (child != value)
        {
            updateParent(value);
            if (child != null)
            {
                childRemoved(child);
            }
        }
        
        return child;
    }

    @Override
    public boolean add(UIComponent value)
    {
        Assert.notNull(value, "value");

        removeChildrenFromParent(value);
        boolean res = super.add(value);
        
        updateParent(value);
        
        return res;
    }

    @Override
    public void add(int index, UIComponent value)
    {
        Assert.notNull(value, "value");
        
        removeChildrenFromParent(value);
        
        super.add(index, value);
        
        updateParent(value);
    }

    @Override
    public UIComponent remove(int index)
    {
        UIComponent child = super.remove(index);
        if (child != null)
        {
            childRemoved(child);
        }
        
        return child;
    }

    private void childRemoved(UIComponent child)
    {
        child.setParent(null);
    }

    private void updateParent(UIComponent child)
    {
        child.setParent(_component);
    }
    
    private void removeChildrenFromParent(UIComponent child)
    {
        UIComponent oldParent = child.getParent();
        if (oldParent != null)
        {
            if (!oldParent.getChildren().remove(child))
            {
                // Check if the component is inside a facet and remove from there
                if (oldParent.getFacetCount() > 0)
                {
                    for (Iterator< Map.Entry<String, UIComponent > > it = 
                        oldParent.getFacets().entrySet().iterator() ; it.hasNext() ; )
                    {
                        Map.Entry<String, UIComponent > entry = it.next();
                        
                        if (entry.getValue().equals(child))
                        {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean remove(Object value)
    {
        if (!(value instanceof UIComponent))
        {
            throw new ClassCastException("value is not a UIComponent");
        }
        
        Assert.notNull(value, "value");

        if (super.remove(value))
        {
            childRemoved((UIComponent)value);
            return true;
        }
        return false;
    }

    @Override
    public void clear()
    {
        removeRange(0, size());
    }

    @Override
    public boolean addAll(Collection<? extends UIComponent> collection)
    {
        boolean result = false;
        Iterator<? extends UIComponent> it = collection.iterator();
        while (it.hasNext())
        {
            if (add(it.next()))
            {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean addAll(int location, Collection<? extends UIComponent> collection)
    {
        Iterator<? extends UIComponent> it = collection.iterator();
        while (it.hasNext())
        {
            add(location++, it.next());
        }
        return !collection.isEmpty();
    }

    @Override
    protected void removeRange(int start, int end)
    {
        Iterator<?> it = listIterator(start);
        for (int i = start; i < end; i++)
        {
            it.next();
            it.remove();
        }
    }
}