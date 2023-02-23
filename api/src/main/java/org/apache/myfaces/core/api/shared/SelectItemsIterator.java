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
package org.apache.myfaces.core.api.shared;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

public class SelectItemsIterator implements Iterator<SelectItem>
{
    private static final Logger log = Logger.getLogger(SelectItemsIterator.class.getName());

    private final Iterator<UIComponent> _children;
    private Iterator<?> _nestedItems;
    private SelectItem _nextItem;
    private UIComponent _currentComponent;
    private UISelectItems _currentUISelectItems;
    private Object _currentValue;
    private FacesContext _facesContext;

    public SelectItemsIterator(UIComponent selectItemsParent, FacesContext facesContext)
    {
        _children = selectItemsParent.getChildCount() > 0
                        ? selectItemsParent.getChildren().iterator()
                        : Collections.emptyIterator();
        _facesContext = facesContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasNext()
    {
        if (_nextItem != null)
        {
            return true;
        }
        if (_nestedItems != null)
        {
            if (_nestedItems.hasNext())
            {
                return true;
            }
            _nestedItems = null;
            _currentComponent = null;
            _currentValue = null;
        }
        if (_children.hasNext())
        {
            UIComponent child = _children.next();
            // When there is other components nested that does
            // not extends from UISelectItem or UISelectItems
            // the behavior for this iterator is just skip this
            // element(s) until an element that extends from these
            // classes are found. If there is no more elements
            // that conform this condition, just return false.
            while (!(child instanceof UISelectItem) && !(child instanceof UISelectItems))
            {
                // Try to skip it
                if (_children.hasNext())
                {
                    // Skip and do the same check
                    child = _children.next();
                }
                else
                {
                    // End loop, so the final result is return false,
                    // since there are no more components to iterate.
                    return false;
                }
            }
            if (child instanceof UISelectItem)
            {
                UISelectItem uiSelectItem = (UISelectItem) child;
                Object item = uiSelectItem.getValue();
                if (item == null)
                {
                    // no value attribute --> create the SelectItem out of the other attributes
                    item = SelectItemsUtil.createSelectItem(uiSelectItem, SelectItem::new);
                }
                else if (!(item instanceof SelectItem))
                {
                    ValueExpression expression = uiSelectItem.getValueExpression("value");
                    throw new IllegalArgumentException("ValueExpression '"
                            + (expression == null ? null : expression.getExpressionString()) + "' of UISelectItem : "
                            + ComponentUtils.getPathToComponent(child)
                            + " does not reference an Object of type SelectItem");
                }
                _nextItem = (SelectItem) item;
                _currentComponent = child;
                _currentValue = item;
                return true;
            }
            else if (child instanceof UISelectItems)
            {
                _currentUISelectItems = ((UISelectItems) child);
                Object value = _currentUISelectItems.getValue();
                _currentComponent = child;

                if (value instanceof SelectItem)
                {
                    _nextItem = (SelectItem) value;
                    return true;
                }
                else if (value != null && value.getClass().isArray())
                {
                    // value is any kind of array (primitive or non-primitive)
                    // --> we have to use class Array to get the values
                    int length = Array.getLength(value);
                    Collection<Object> items = new ArrayList<>(length);
                    for (int i = 0; i < length; i++)
                    {
                        items.add(Array.get(value, i));
                    }
                    _nestedItems = items.iterator();
                    return hasNext();
                }
                else if (value instanceof Iterable)
                {
                    // value is Iterable --> Collection, DataModel,...
                    _nestedItems = ((Iterable<?>) value).iterator();
                    return hasNext();
                }
                else if (value instanceof Map)
                {
                    Map<Object, Object> map = ((Map<Object, Object>) value);
                    Collection<SelectItem> items = new ArrayList<>(map.size());
                    for (Map.Entry<Object, Object> entry : map.entrySet())
                    {
                        items.add(new SelectItem(entry.getValue(), entry.getKey().toString()));
                    }
                    
                    _nestedItems = items.iterator();
                    return hasNext();
                }
                else
                {
                    Level level = _facesContext.isProjectStage(ProjectStage.Production)
                            ? Level.FINE
                            : Level.WARNING;
                    if (log.isLoggable(level))
                    {
                        ValueExpression expression = _currentUISelectItems.getValueExpression("value");
                        log.log(level, "ValueExpression {0} of UISelectItems with component-path {1}"
                                + " does not reference an Object of type SelectItem,"
                                + " array, Iterable or Map, but of type: {2}",
                                new Object[] {
                                    (expression == null ? null : expression.getExpressionString()),
                                    ComponentUtils.getPathToComponent(child),
                                    (value == null ? null : value.getClass().getName()) 
                                });
                    }
                }
            }
            else
            {
                _currentComponent = null;
                _currentValue = null;
            }
        }
        return false;
    }

    @Override
    public SelectItem next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        if (_nextItem != null)
        {
            SelectItem value = _nextItem;
            _nextItem = null;
            return value;
        }
        if (_nestedItems != null)
        {
            Object item = _nestedItems.next();
            

            // check new params of SelectItems (since 2.0): itemValue, itemLabel, itemDescription,...
            // Note that according to the spec UISelectItems does not provide Getter and Setter
            // methods for this values, so we have to use the attribute map
            Map<String, Object> attributeMap = _currentUISelectItems.getAttributes();

            // write the current item into the request map under the key listed in var, if available
            boolean wroteRequestMapVarValue = false;
            Object oldRequestMapVarValue = null;
            String var = (String) attributeMap.get(SelectItemsUtil.ATTR_VAR);
            if (var != null && !var.isEmpty())
            {
                // save the current value of the key listed in var from the request map
                oldRequestMapVarValue = _facesContext.getExternalContext().getRequestMap().put(var, item);
                wroteRequestMapVarValue = true;
            }

            if (!(item instanceof SelectItem))
            {
                _currentValue = item;
                item = SelectItemsUtil.createSelectItem(_currentUISelectItems, item, SelectItem::new);
            }
            else
            {
                item = SelectItemsUtil.updateSelectItem(_currentUISelectItems, (SelectItem) item);
            }

            // remove the value with the key from var from the request map, if previously written
            if (wroteRequestMapVarValue)
            {
                // If there was a previous value stored with the key from var in the request map, restore it
                if (oldRequestMapVarValue != null)
                {
                    _facesContext.getExternalContext().getRequestMap().put(var, oldRequestMapVarValue);
                }
                else
                {
                    _facesContext.getExternalContext().getRequestMap().remove(var);
                }
            }

            return (SelectItem) item;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    public UIComponent getCurrentComponent()
    {
        return _currentComponent;
    }
    
    public Object getCurrentValue()
    {
        return _currentValue;
    }
}
