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
 * This tag associates a parameter name-value pair with the nearest parent
 * UIComponent.
 * <p>
 * A UIComponent is created to represent this name-value pair, and stored as
 * a child of the parent component; what effect this has depends upon the
 * renderer of that parent component.
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @JSFComponent
 *   name="f:param"
 *   bodyContent="empty"
 *   tagClass = "org.apache.myfaces.taglib.core.ParamTag"
 *   desc = "UIParameter"
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIParameter
        extends UIComponentBase
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Parameter";
    public static final String COMPONENT_FAMILY = "javax.faces.Parameter";

    private String _name = null;
    private Object _value = null;

    public UIParameter()
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

    public void setName(String name)
    {
        _name = name;
    }

    /**
     * A String containing the name of the parameter.
     * 
     * @JSFProperty
     */
    public String getName()
    {
        if (_name != null) return _name;
        ValueBinding vb = getValueBinding("name");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    /**
     * The value of this parameter.
     * 
     * @JSFProperty
     *   required="true"
     */
    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[3];
        values[0] = super.saveState(context);
        values[1] = _name;
        values[2] = _value;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _name = (String)values[1];
        _value = values[2];
    }
    //------------------ GENERATED CODE END ---------------------------------------

}
