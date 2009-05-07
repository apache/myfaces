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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _ComponentChildrenList extends AbstractList<UIComponent> implements Serializable
{
    private static final long serialVersionUID = -6775078929331154224L;
    private UIComponent _component;
    private List<UIComponent> _list = new ArrayList<UIComponent>(4);

    _ComponentChildrenList(UIComponent component)
    {
        _component = component;
    }

    @Override
    public UIComponent get(int index)
    {
        return _list.get(index);
    }

    @Override
    public int size()
    {
        return _list.size();
    }

    @Override
    public UIComponent set(int index, UIComponent value)
    {
        checkValue(value);
        
        UIComponent child = _list.set(index, value);
        if (child != value)
        {
            childAdded(value);
            if (child != null)
            {
                child.setParent(null);
            }
        }
        
        return child;
    }

    @Override
    public boolean add(UIComponent value)
    {
        checkValue(value);
        
        boolean res = _list.add(value);
        
        childAdded(value);
        
        return res;
    }

    @Override
    public void add(int index, UIComponent value)
    {
        checkValue(value);
        
        _list.add(index, value);
        
        childAdded(value);
    }

    @Override
    public UIComponent remove(int index)
    {
        UIComponent child = _list.remove(index);
        if (child != null)
        {
            childRemoved(child);
        }
        
        return child;
    }

    private void checkValue(Object value)
    {
        if (value == null)
        {
            throw new NullPointerException("value");
        }
        
        if (!(value instanceof UIComponent))
        {
            throw new ClassCastException("value is not a UIComponent");
        }
    }

    private void childAdded(UIComponent child)
    {
        updateParent(child);
        
        /*
        FacesContext context = FacesContext.getCurrentInstance();
        
        // After the child component has been added to the view, if the following condition is not met
        // FacesContext.isPostback() returns true and FacesContext.getCurrentPhaseId() returns PhaseId.RESTORE_VIEW
        if (!(context.isPostback() && PhaseId.RESTORE_VIEW.equals(context.getCurrentPhaseId())))
        {
            // Application.publishEvent(java.lang.Class, java.lang.Object)  must be called, passing 
            // PostAddToViewEvent.class as the first argument and the newly added component as the second 
            // argument. TODO: Deal with isInView
            context.getApplication().publishEvent(PostAddToViewEvent.class, child);
        }
        */
    }

    private void childRemoved(UIComponent child)
    {
        child.setParent(null);
    }

    private void updateParent(UIComponent child)
    {
        UIComponent oldParent = child.getParent();
        if (oldParent != null)
        {
            oldParent.getChildren().remove(child);
        }
        
        child.setParent(_component);
    }

    @Override
    public boolean remove(Object o)
    {
        return _list.remove(o);
    }
}
