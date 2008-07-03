// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.

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

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 *
 * UISelectBoolean is a Comonent which represents a boolean value.
 *
 * <h4>Events:</h4>
 * <table border="1" width="100%" cellpadding="3" summary="">
 * <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 * <th align="left">Type</th>
 * <th align="left">Phases</th>
 * <th align="left">Description</th>
 * </tr>
 * <tr class="TableRowColor">
 * <td valign="top"><code>javax.faces.event.ValueChangeEvent</code></td>
 * <td valign="top" nowrap></td>
 * <td valign="top">The valueChange event is delivered when the value
                attribute is changed.</td>
 * </tr>
 * </table>
 */
@JSFComponent
(defaultRendererType = "javax.faces.Checkbox"
)
public class UISelectBoolean extends UIInput
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.SelectBoolean";
  static public final String COMPONENT_TYPE =
    "javax.faces.SelectBoolean";

  /**
   * Construct an instance of the UISelectBoolean.
   */
  public UISelectBoolean()
  {
    setRendererType("javax.faces.Checkbox");
  }
  
    public void setSelected(boolean selected)
    {
        setValue(Boolean.valueOf(selected));
    }

    public boolean isSelected()
    {
        Boolean value = (Boolean)getSubmittedValue();
        if( value == null )
            value = (Boolean)getValue();

        return value != null ? value.booleanValue() : false;
    }

    /**
     * @deprecated Use getValueExpression instead
     */
    public ValueBinding getValueBinding(String name)
    {
        if (name == null) throw new NullPointerException("name");
        if (name.equals("selected"))
        {
            return super.getValueBinding("value");
        }
        else
        {
            return super.getValueBinding(name);
        }
    }

    /**
     * @deprecated Use setValueExpression instead
     */
    public void setValueBinding(String name,
                                ValueBinding binding)
    {
        if (name == null) throw new NullPointerException("name");
        if (name.equals("selected"))
        {
            super.setValueBinding("value", binding);
        }
        else
        {
            super.setValueBinding(name, binding);
        }
    }

    public ValueExpression getValueExpression(String name)
    {
        if (name == null) throw new NullPointerException("name");
        if (name.equals("selected"))
        {
            return super.getValueExpression("value");
        }
        else
        {
            return super.getValueExpression(name);
        }
    }

    public void setValueExpression(String name,
                                   ValueExpression binding)
    {
        if (name == null) throw new NullPointerException("name");
        if (name.equals("selected"))
        {
            super.setValueExpression("value", binding);
        }
        else
        {
            super.setValueExpression(name, binding);
        }
    }    

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }
}
