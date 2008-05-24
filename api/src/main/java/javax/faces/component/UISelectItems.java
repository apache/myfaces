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

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
/**
 * This tag associates a set of selection list items with the nearest
 * parent UIComponent. The set of SelectItem objects is retrieved via
 * a value-binding.
 * &lt;p&gt;
 * Unless otherwise specified, all attributes accept static values
 * or EL expressions.
 * 
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "f:selectItems"
 *   bodyContent = "empty"
 *   tagClass = "org.apache.myfaces.taglib.core.SelectItemsTag"
 *   desc = "UISelectItems"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UISelectItems
        extends UIComponentBase
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.SelectItems";
    public static final String COMPONENT_FAMILY = "javax.faces.SelectItems";

    private Object _value = null;

    public UISelectItems()
    {
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    /**
     * An EL expression that specifies the contents of the selection list.
     * The expression can refer to one of the following:
     * &lt;ol&gt;
     *     &lt;li&gt;A single SelectItem&lt;/li&gt;
     *     &lt;li&gt;An array or Collection of SelectItem instances&lt;/li&gt;
     *     &lt;li&gt;A Map. The contents of the Map are used to create SelectItem
     *         instances, where the SelectItem's label is the map's key value, 
     *         and the SelectItem's value is the map's value. When using a
     *         map, it is recommended that an ordered implementation such as
     *         java.util.TreeMap is used.&lt;/li&gt;
     * &lt;/ol&gt;
     * The value properties of each of the SelectItems must be of the same
     * basic type as the parent component's value.
     * 
     * @JSFProperty
     */
    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        values[1] = _value;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _value = values[1];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
