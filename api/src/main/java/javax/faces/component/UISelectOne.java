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

import javax.faces.component._SelectItemsUtil._ValueConverter;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * Component for choosing one option out of a set of possibilities.
 * <p>
 * This component is expected to have children of type UISelectItem or
 * UISelectItems; these define the set of possible options that the
 * user can choose from.
 * <p>
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UISelectOne extends UIInput
{
    public static final String INVALID_MESSAGE_ID = "javax.faces.component.UISelectOne.INVALID";

    private String label ;
    
    /**
     * Verify that the result of converting the newly submitted value is
     * <i>equal</i> to the value property of one of the child SelectItem
     * objects. If this is not true, a validation error is reported.
     * 
     * @see javax.faces.component.UIInput#validateValue(javax.faces.context.FacesContext, java.lang.Object)
     */
    protected void validateValue(FacesContext context, Object value)
    {
        super.validateValue(context, value);

        if (!isValid() || value == null)
        {
            return;
        }

        _ValueConverter converter = new _ValueConverter()
        {
            public Object getConvertedValue(FacesContext context, String value)
            {
                return UISelectOne.this.getConvertedValue(context, value);
            }
        };

        // selected value must match to one of the available options
        if (!_SelectItemsUtil.matchValue(context, value, new _SelectItemsIterator(this), converter))
        {
            _MessageUtils.addErrorMessage(context, this, INVALID_MESSAGE_ID,
                            new Object[] {getId()});
            setValid(false);
        }
    }

    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.SelectOne";
    public static final String COMPONENT_FAMILY = "javax.faces.SelectOne";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Menu";

    public UISelectOne()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    //------------------ GENERATED CODE END ---------------------------------------
    
    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        values[1] = label;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        label = (String)values[1];
    }
    
    /**
     * @since 1.2
     */
    
    public String getLabel()
    {
        if (label != null) return label;
        ValueBinding vb = getValueBinding("label");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    /**
     * @since 1.2
     */
    
    public void setLabel(String label)
    {
        this.label = label;
    }
}
