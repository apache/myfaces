/*
 * Copyright 2004 The Apache Software Foundation.
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
 * 
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
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
    
    private Boolean itemEscaped;

    public UISelectItem()
    {
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setItemDescription(String itemDescription)
    {
        _itemDescription = itemDescription;
    }

    /**
     * For use in development tools.
     * <p>
     * Q: what use is an EL expression for this???
     */
    public String getItemDescription()
    {
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

    public Object getItemValue()
    {
        if (_itemValue != null) return _itemValue;
        ValueBinding vb = getValueBinding("itemValue");
        return vb != null ? (Object)vb.getValue(getFacesContext()) : null;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? (Object)vb.getValue(getFacesContext()) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[7];
        values[0] = super.saveState(context);
        values[1] = _itemDescription;
        values[2] = _itemDisabled;
        values[3] = _itemLabel;
        values[4] = _itemValue;
        values[5] = _value;
        values[6] = itemEscaped;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _itemDescription = (String)values[1];
        _itemDisabled = (Boolean)values[2];
        _itemLabel = (String)values[3];
        _itemValue = (Object)values[4];
        _value = (Object)values[5];
        itemEscaped = (Boolean) values[6];
    }
    //------------------ GENERATED CODE END ---------------------------------------
    
    public boolean isItemEscaped()
    {
        if (itemEscaped != null) return itemEscaped.booleanValue();
        ValueBinding vb = getValueBinding("itemEscaped");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : Boolean.FALSE;
    }

    public void setItemEscaped(boolean itemEscaped)
    {
        this.itemEscaped = itemEscaped ? Boolean.TRUE : Boolean.FALSE;
    }

}
