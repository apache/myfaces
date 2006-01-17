/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.component;

import java.util.Arrays;
import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _SelectItemsUtil
{
    public static interface _ValueConverter
    {
        Object getConvertedValue(FacesContext context, String value);
    }
    
    /**
     * @param context the faces context
     * @param value the value to check
     * @param converter 
     * @param iterator contains instances of SelectItem
     * @return if the value of a selectitem is equal to the given value
     */
    public static boolean matchValue(FacesContext context, Object value,
                    Iterator selectItemsIter, _ValueConverter converter)
    {
        while (selectItemsIter.hasNext())
        {
            SelectItem item = (SelectItem) selectItemsIter.next();
            if (item instanceof SelectItemGroup)
            {
                SelectItemGroup itemgroup = (SelectItemGroup) item;
                SelectItem[] selectItems = itemgroup.getSelectItems();
                if (selectItems != null
                                && selectItems.length > 0
                                && matchValue(context, value, Arrays.asList(
                                                selectItems).iterator(), converter))
                {
                    return true;
                }
            }
            else
            {
                Object itemValue = item.getValue();
                if(converter != null && itemValue instanceof String)
                {
                    itemValue = converter.getConvertedValue(context, (String)itemValue);
                }
                if (value==itemValue || value.equals(itemValue))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
