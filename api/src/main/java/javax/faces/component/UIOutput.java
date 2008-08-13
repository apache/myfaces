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
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

/**
 * Displays a value to the user.
 * <p>
 * See the javadoc for this class in the
 * <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * for further details.
 * 
 * @JSFComponent
 *   type = "javax.faces.Output"
 *   family = "javax.faces.Output"
 *   desc = "UIOutput displays a value to the user"
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIOutput extends UIComponentBase implements ValueHolder
{
    public static final String COMPONENT_TYPE = "javax.faces.Output";
    public static final String COMPONENT_FAMILY = "javax.faces.Output";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Text";

    private Converter _converter = null;
    private Object _value = null;

    public Object getLocalValue()
    {
        return _value;
    }

    public UIOutput()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setConverter(Converter converter)
    {
        _converter = converter;
    }

    /**
     * The value can either be a static value (ID) or an EL expression. When a static id is
     * specified, an instance of the converter type registered with that id is used. When this is an
     * EL expression, the result of evaluating the expression must be an object that implements the
     * Converter interface.
     * 
     * @JSFProperty
     */
    public Converter getConverter()
    {
        if (_converter != null)
        {
            return _converter;
        }
        ValueBinding vb = getValueBinding("converter");
        return vb != null ? (Converter) vb.getValue(getFacesContext()) : null;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    /**
     * The initial value of this component. This value is generally set as a value-binding in the
     * form #{myBean.myProperty}, where myProperty can be any data-type of Java (also user-defined
     * data-types), if a converter for this data-type exists.
     * 
     * Special cases:
     * 
     * 1) f:selectItems - value needs to bind to a list (or an array) of
     * javax.faces.model.SelectItem-instances
     * 
     * 2) components implementing UISelectMany (e.g. h:selectManyCheckbox) - value needs to bind to
     * a list (or an array) of values, where the values need to be of the same data-type as the
     * choices of the associated f:selectItems-component
     * 
     * 3) components implementing UIData (e.g. h:dataTable) - value needs to bind to a list (or an
     * array) of values, which will be iterated over when the data-table is processed
     * 
     * @JSFProperty
     */
    public Object getValue()
    {
        if (_value != null)
        {
            return _value;
        }
        ValueBinding vb = getValueBinding("value");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[3];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, _converter);
        values[2] = _value;
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        _converter = (Converter) restoreAttachedState(context, values[1]);
        _value = values[2];
    }
}
