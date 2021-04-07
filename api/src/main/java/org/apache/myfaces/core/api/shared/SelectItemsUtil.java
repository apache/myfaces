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
import jakarta.faces.model.SelectItem;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class SelectItemsUtil
{
    private static final String ITEM_VALUE_ATTR = "itemValue";
    private static final String ITEM_LABEL_ATTR = "itemLabel";
    private static final String ITEM_DESCRIPTION_ATTR = "itemDescription";
    private static final String ITEM_DISABLED_ATTR = "itemDisabled";
    private static final String ITEM_LABEL_ESCAPED_ATTR = "itemLabelEscaped";
    private static final String NO_SELECTION_VALUE_ATTR = "noSelectionValue";
    
    public static <S extends SelectItem> S createSelectItem(UIComponent component, Object value, Supplier<S> supplier)
    {
        // check new params of SelectItems (since 2.0): itemValue, itemLabel, itemDescription,...
        // Note that according to the spec UISelectItems does not provide Getter and Setter 
        // methods for this values, so we have to use the attribute map
        Map<String, Object> attributeMap = component.getAttributes();

        // check the itemValue attribute
        Object itemValue = attributeMap.get(ITEM_VALUE_ATTR);
        if (itemValue == null)
        {
            itemValue = value;
        }

        // Spec: When iterating over the select items, toString() 
        // must be called on the string rendered attribute values
        Object itemLabel = attributeMap.get(ITEM_LABEL_ATTR);
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

        Object itemDescription = attributeMap.get(ITEM_DESCRIPTION_ATTR);
        if (itemDescription != null)
        {
            itemDescription = itemDescription.toString();
        }

        Boolean itemDisabled = AttributeUtils.getBooleanAttribute(component,
                ITEM_DISABLED_ATTR, false);
        Boolean itemLabelEscaped = AttributeUtils.getBooleanAttribute(component,
                ITEM_LABEL_ESCAPED_ATTR, true);
        Object noSelectionValue = attributeMap.get(NO_SELECTION_VALUE_ATTR);

        S selectItem = supplier.get();
        selectItem.setValue(itemValue);
        selectItem.setLabel((String) itemLabel);
        selectItem.setDescription((String) itemDescription);
        selectItem.setDisabled(itemDisabled);
        selectItem.setEscape(itemLabelEscaped);
        selectItem.setNoSelectionOption(Objects.equals(itemValue, noSelectionValue));

        return selectItem;
    }
}
