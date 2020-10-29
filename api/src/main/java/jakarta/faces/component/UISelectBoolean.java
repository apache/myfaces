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
package jakarta.faces.component;

import javax.el.ValueExpression;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * A component that allows the user to select or unselect an object.
 * <p>
 * This can also be used to choose between two states such as true/false or on/off.
 * </p>
 * <p>
 * See the javadoc for this class in the
 * <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 * for further details.
 * </p>
 */
@JSFComponent(defaultRendererType = "jakarta.faces.Checkbox")
public class UISelectBoolean extends UIInput
{
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectBoolean";
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectBoolean";

    public UISelectBoolean()
    {
        setRendererType("jakarta.faces.Checkbox");
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setSelected(boolean selected)
    {
        setValue(selected);
    }

    public boolean isSelected()
    {
        Boolean value = (Boolean) getSubmittedValue();
        if (value == null)
        {
            value = (Boolean) getValue();
        }

        return value != null ? value : false;
    }

    @Override
    public ValueExpression getValueExpression(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        
        if (name.equals("selected"))
        {
            return super.getValueExpression("value");
        }
        else
        {
            return super.getValueExpression(name);
        }
    }

    @Override
    public void setValueExpression(String name, ValueExpression binding)
    {
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        
        if (name.equals("selected"))
        {
            super.setValueExpression("value", binding);
        }
        else
        {
            super.setValueExpression(name, binding);
        }
    }

    @JSFProperty(deferredValueType="java.lang.Boolean")
    @Override
    public Object getValue()
    {
        return super.getValue();
    }
}
