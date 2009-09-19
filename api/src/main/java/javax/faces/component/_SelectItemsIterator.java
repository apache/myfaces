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

import java.util.*;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _SelectItemsIterator implements Iterator<SelectItem>
{
    private final Iterator<UIComponent> _childs;
    private Iterator<SelectItem> _nestedItems;
    private SelectItem _nextItem;
    private String _collectionLabel;
    private UISelectItems _currentUISelectItems;

    public _SelectItemsIterator(UIComponent selectItemsParent)
    {
        _childs = selectItemsParent.getChildren().iterator();
    }

    @SuppressWarnings("unchecked")
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
            // remove the last value from the request map
            if(_currentUISelectItems.getVar() != null && !"".equals(_currentUISelectItems.getVar()))
            {
                FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestMap().remove(_currentUISelectItems.getVar());
            } 
            _nestedItems = null;
        }
        if (_childs.hasNext())
        {
            UIComponent child = _childs.next();
            // When there is other components nested that does
            // not extends from UISelectItem or UISelectItems
            // the behavior for this iterator is just skip this
            // element(s) until an element that extends from these
            // classes are found. If there is no more elements
            // that conform this condition, just return false.
            while (!(child instanceof UISelectItem) && !(child instanceof UISelectItems))
            {
                // Try to skip it
                if (_childs.hasNext())
                {
                    // Skip and do the same check
                    child = _childs.next();
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
                    Object itemValue = ((UISelectItem) child).getItemValue();
                    String label = ((UISelectItem) child).getItemLabel();
                    String description = ((UISelectItem) child).getItemDescription();
                    boolean disabled = ((UISelectItem) child).isItemDisabled();
                    if (label == null)
                    {
                        label = itemValue.toString();
                    }
                    item = new SelectItem(itemValue, label, description, disabled);
                }
                else if (!(item instanceof SelectItem))
                {
                    ValueBinding binding = ((UISelectItem) child).getValueBinding("value");
                    throw new IllegalArgumentException("Value binding '"
                            + (binding == null ? null : binding.getExpressionString()) + "' of UISelectItem : "
                            + getPathToComponent(child) + " does not reference an Object of type SelectItem");
                }
                _nextItem = (SelectItem)item;
                return true;
            }
            else if (child instanceof UISelectItems)
            {
                _currentUISelectItems = ((UISelectItems) child);
                Object value = _currentUISelectItems.getValue();

                if (value instanceof SelectItem)
                {
                    _nextItem = (SelectItem)value;
                    return true;
                }
                else if (value instanceof SelectItem[])
                {
                    _nestedItems = Arrays.asList((SelectItem[]) value).iterator();
                    _collectionLabel = "Array";
                    return hasNext();
                }
                else if (value instanceof Collection)
                {
                    _nestedItems = ((Collection<SelectItem>) value).iterator();
                    _collectionLabel = "Collection";
                    return hasNext();
                }
                else if (value instanceof Map)
                {
                    Map<Object, Object> map = ((Map<Object, Object>) value);
                    Collection<SelectItem> items = new ArrayList<SelectItem>(map.size());
                    for (Map.Entry<Object, Object> entry : map.entrySet())
                    {
                        items.add(new SelectItem(entry.getValue(), entry.getKey().toString()));
                    }
                    
                    _nestedItems = items.iterator();
                    _collectionLabel = "Map";
                    return hasNext();
                }
                else
                {
                    ValueBinding binding = _currentUISelectItems.getValueBinding("value");

                    throw new IllegalArgumentException(
                        "Value binding '"
                                + (binding == null ? null : binding.getExpressionString())
                                + "'of UISelectItems with component-path "
                                + getPathToComponent(child)
                                + " does not reference an Object of type SelectItem, SelectItem[], Collection or Map but of type : "
                                + ((value == null) ? null : value.getClass().getName()));
                }
            }
        }
        return false;
    }

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
            
            // write the current item into the request map under the key listed in var, if available
            if(_currentUISelectItems.getVar() != null && !"".equals(_currentUISelectItems.getVar()))
            {
                FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestMap().put(_currentUISelectItems.getVar(), item);
            }
            
            if (!(item instanceof SelectItem))
            {
                // check new params of SelectItems (since 2.0) itemValue, itemLabel, itemDescription,...
                Object itemValue = _currentUISelectItems.getItemValue();
                if(itemValue != null) 
                {
                    String itemLabel = _currentUISelectItems.getItemLabel() == null ?
                            itemValue.toString() : 
                            _currentUISelectItems.getItemLabel();
                    item = new SelectItem(itemValue,
                        itemLabel,
                        _currentUISelectItems.getItemDescription(),
                        _currentUISelectItems.isItemDisabled(),
                        _currentUISelectItems.isItemLabelEscaped(),
                        itemValue.equals(_currentUISelectItems.getNoSelectionValue())
                            || itemLabel.equals(_currentUISelectItems.getNoSelectionValue())); 
                }
                else 
                {
                    ValueExpression expression = _currentUISelectItems.getValueExpression("value");
                    throw new IllegalArgumentException(
                        _collectionLabel + " referenced by UISelectItems with binding '"
                        + expression.getExpressionString()
                        + "' and Component-Path : " + getPathToComponent(_currentUISelectItems)
                        + " does not contain Objects of type SelectItem"
                        + " or does not provide the attribute itemValue");
                }
            }
            return (SelectItem) item;
        }
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private String getPathToComponent(UIComponent component)
    {
        StringBuffer buf = new StringBuffer();

        if (component == null)
        {
            buf.append("{Component-Path : ");
            buf.append("[null]}");
            return buf.toString();
        }

        getPathToComponent(component, buf);

        buf.insert(0, "{Component-Path : ");
        buf.append("}");

        return buf.toString();
    }

    private void getPathToComponent(UIComponent component, StringBuffer buf)
    {
        if (component == null)
        {
            return;
        }

        StringBuffer intBuf = new StringBuffer();

        intBuf.append("[Class: ");
        intBuf.append(component.getClass().getName());
        if (component instanceof UIViewRoot)
        {
            intBuf.append(",ViewId: ");
            intBuf.append(((UIViewRoot) component).getViewId());
        }
        else
        {
            intBuf.append(",Id: ");
            intBuf.append(component.getId());
        }
        intBuf.append("]");

        buf.insert(0, intBuf);

        getPathToComponent(component.getParent(), buf);
    }
}
