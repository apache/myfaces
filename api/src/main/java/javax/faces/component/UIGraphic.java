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
 * Displays a graphical image.
 * <p>
 * See the javadoc for this class in the
 * <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * for further details.
 *
 * @JSFComponent
 *   type = "javax.faces.Graphic"
 *   family = "javax.faces.Graphic"
 *   desc = "UIGraphic"
 *   
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIGraphic
        extends UIComponentBase
{
    public static final String COMPONENT_TYPE = "javax.faces.Graphic";
    public static final String COMPONENT_FAMILY = "javax.faces.Graphic";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Image";

    private static final String URL_PROPERTY = "url";
    private static final String VALUE_PROPERTY = "value";

    private Object _value = null;

    /**
     * An alias for the "value" attribute.
     * 
     * @JSFProperty
     */
    public String getUrl()
    {
        return (String)getValue();
    }

    public void setUrl(String url)
    {
        setValue(url);
    }

    public ValueBinding getValueBinding(String name)
    {
        if (URL_PROPERTY.equals(name))
        {
            return super.getValueBinding(VALUE_PROPERTY);
        }
        else
        {
            return super.getValueBinding(name);
        }
    }

    public void setValueBinding(String name,
                                ValueBinding binding)
    {
        if (URL_PROPERTY.equals(name))
        {
            super.setValueBinding(VALUE_PROPERTY, binding);
        }
        else
        {
            super.setValueBinding(name, binding);
        }
    }

    public UIGraphic()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
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
     * The URL of the image.  If the URL starts with a '/', it is relative
     * to the context path of the web application.
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
}
