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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.myfaces.core.api.shared.lang.CollectionUtils;

public class SelectItemsUtil
{
    public static final String ATTR_ITEM_VALUE = "itemValue";
    public static final String ATTR_ITEM_LABEL = "itemLabel";
    public static final String ATTR_ITEM_DESCRIPTION = "itemDescription";
    public static final String ATTR_ITEM_DISABLED = "itemDisabled";
    public static final String ATTR_ITEM_LABEL_ESCAPED = "itemLabelEscaped";
    public static final String ATTR_NO_SELECTION_VALUE = "noSelectionValue";
    public static final String ATTR_VAR = "var";
    
    public static <S extends SelectItem> S createSelectItem(UISelectItem uiSelectItem, Supplier<S> supplier)
    {
        Object value = uiSelectItem.getItemValue();
 
        String label = uiSelectItem.getItemLabel();
        if (label == null && value != null)
        {
            label = value.toString();
        }

        S selectItem = supplier.get();
        selectItem.setValue(value);
        selectItem.setLabel(label);
        selectItem.setDescription(uiSelectItem.getItemDescription());
        selectItem.setDisabled(uiSelectItem.isItemDisabled());
        selectItem.setEscape(uiSelectItem.isItemEscaped());
        selectItem.setNoSelectionOption(uiSelectItem.isNoSelectionOption());
        
        return selectItem;
    }
    
    public static <S extends SelectItem> S createSelectItem(UIComponent component, Object value, Supplier<S> supplier)
    {
        // check new params of SelectItems (since 2.0): itemValue, itemLabel, itemDescription,...
        // Note that according to the spec UISelectItems does not provide Getter and Setter 
        // methods for this values, so we have to use the attribute map
        Map<String, Object> attributes = component.getAttributes();

        // check the itemValue attribute
        Object itemValue = getItemValue(attributes, value);

        // Spec: When iterating over the select items, toString() 
        // must be called on the string rendered attribute values
        Object itemLabel = attributes.get(ATTR_ITEM_LABEL);
        if (itemLabel == null)
        {
            if (itemValue != null)
            {
                itemLabel = itemValue.toString();
            }
        }
        else
        {
            itemLabel = itemLabel.toString();
        }

        Object itemDescription = attributes.get(ATTR_ITEM_DESCRIPTION);
        if (itemDescription != null)
        {
            itemDescription = itemDescription.toString();
        }

        Boolean itemDisabled = AttributeUtils.getBooleanAttribute(component,
                ATTR_ITEM_DISABLED, false);
        Boolean itemLabelEscaped = AttributeUtils.getBooleanAttribute(component,
                ATTR_ITEM_LABEL_ESCAPED, true);
        Object noSelectionValue = attributes.get(ATTR_NO_SELECTION_VALUE);

        S selectItem = supplier.get();
        selectItem.setValue(itemValue);
        selectItem.setLabel((String) itemLabel);
        selectItem.setDescription((String) itemDescription);
        selectItem.setDisabled(itemDisabled);
        selectItem.setEscape(itemLabelEscaped);
        selectItem.setNoSelectionOption(Objects.equals(itemValue, noSelectionValue));

        return selectItem;
    }

    public static List<SelectItem> collectSelectItems(FacesContext context, UIComponent component)
    {
        List<SelectItem> items = new ArrayList<>();

        for (int i = 0; i < component.getChildCount(); i++)
        {
            UIComponent child = component.getChildren().get(i);
            if (child instanceof UISelectItems)
            {
                UISelectItems uiSelectItems = (UISelectItems) child;
                createSelectItems(context, uiSelectItems, uiSelectItems.getValue(), SelectItem::new, items::add);
            }
            else if (child instanceof UISelectItem)
            {
                items.add(createSelectItem(child, null, SelectItem::new));
            }
        }
        return items;
    }
    
    public static <S extends SelectItem> void createSelectItems(FacesContext context, UISelectItems component,
            Object values, Supplier<S> supplier, Consumer<S> callback)
    {
        Map<String, Object> attributes = component.getAttributes();
        Object varObject = attributes.get(ATTR_VAR);
        String var = varObject != null ? (String) varObject : "item";
 
        CollectionUtils.forEach(values, value ->
        {
            Object oldValue = context.getExternalContext().getRequestMap().put(var, value);
            
            callback.accept(
                    createSelectItem(component, getItemValue(attributes, value), supplier));

            if (oldValue != null)
            {
                context.getExternalContext().getRequestMap().put(var, oldValue);
            }
            else
            {
                context.getExternalContext().getRequestMap().remove(var);
            }
        });
    }
    
    private static Object getItemValue(Map<String, Object> attributes, Object defaultValue)
    {
        Object itemValue = attributes.get(ATTR_ITEM_VALUE);
        return itemValue != null || attributes.containsKey(ATTR_ITEM_VALUE) ? itemValue : defaultValue;
    }
    

}
