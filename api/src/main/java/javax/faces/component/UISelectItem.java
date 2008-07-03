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
 * A component representing a single option that the user can choose.
 * <p>
 * The option attributes can either be defined directly on this component
 * (via the itemValue, itemLabel, itemDescription properties) or the value
 * property can reference a SelectItem object (directly or via an EL expression).
 * <p>
 * The value expression (if defined) is read-only; the parent select component
 * will have a value attribute specifying where the value for the chosen
 * selection will be stored.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name = "f:selectItem"
 *   bodyContent = "empty"
 *   tagClass = "org.apache.myfaces.taglib.core.SelectItemTag"
 *   desc = "UISelectItem"
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UISelectItem
        extends UIComponentBase
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.SelectItem";
    public static final String COMPONENT_FAMILY = "javax.faces.SelectItem";
    private static final boolean DEFAULT_ITEMDISABLED = false;

    private String _itemDescription = null;
    private Boolean _itemDisabled = null;
    private String _itemLabel = null;
    private Object _itemValue = null;
    private Object _value = null;

    public UISelectItem()
    {
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
    
    /**
     * Disable this property; although this class extends a base-class that
     * defines a read/write rendered property, this particular subclass
     * does not support setting it. Yes, this is broken OO design: direct
     * all complaints to the JSF spec group.
     *
     * @JSFProperty tagExcluded="true"
     */
    public void setRendered(boolean state) {
       throw new UnsupportedOperationException();
    }

    public boolean isRendered() {
        return true;
    }

    public void setItemDescription(String itemDescription)
    {
        _itemDescription = itemDescription;
    }

    /**
     * An optional description for this item.
     * For use in development tools.
     * 
     * @JSFProperty 
     */
    public String getItemDescription()
    {
        //Q: what use is an EL expression for this???
        if (_itemDescription != null) return _itemDescription;
        ValueBinding vb = getValueBinding("itemDescription");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    /**
     * When true, this item cannot be chosen by the user. If this method is
     * ever called, then any EL-binding for the disabled property will be
     * ignored.
     */
    public void setItemDisabled(boolean itemDisabled)
    {
        _itemDisabled = Boolean.valueOf(itemDisabled);
    }

    /**
     * Determine whether this item can be chosen by the user.
     * 
     * @JSFProperty
     */
    public boolean isItemDisabled()
    {
        if (_itemDisabled != null) return _itemDisabled.booleanValue();
        ValueBinding vb = getValueBinding("itemDisabled");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_ITEMDISABLED;
    }

    public void setItemLabel(String itemLabel)
    {
        _itemLabel = itemLabel;
    }

    /**
     * Get the string which will be presented to the user for this option.
     * 
     * @JSFProperty
     */
    public String getItemLabel()
    {
        if (_itemLabel != null) return _itemLabel;
        ValueBinding vb = getValueBinding("itemLabel");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setItemValue(Object itemValue)
    {
        _itemValue = itemValue;
    }

    /**
     * The value of this item, of the same type as the parent component's value.
     * 
     * @JSFProperty
     */
    public Object getItemValue()
    {
        if (_itemValue != null) return _itemValue;
        ValueBinding vb = getValueBinding("itemValue");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    /**
     * An EL expression that refers to a javax.faces.model.SelectItem instance.
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
        Object values[] = new Object[6];
        values[0] = super.saveState(context);
        values[1] = _itemDescription;
        values[2] = _itemDisabled;
        values[3] = _itemLabel;
        values[4] = _itemValue;
        values[5] = _value;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _itemDescription = (String)values[1];
        _itemDisabled = (Boolean)values[2];
        _itemLabel = (String)values[3];
        _itemValue = values[4];
        _value = values[5];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
