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
 * Displays a graphical image.
 */
@JSFComponent
(defaultRendererType = "javax.faces.Image"
)
public class UIGraphic extends UIComponentBase
{

  static public final String COMPONENT_FAMILY =
    "javax.faces.Graphic";
  static public final String COMPONENT_TYPE =
    "javax.faces.Graphic";

  /**
   * Construct an instance of the UIGraphic.
   */
  public UIGraphic()
  {
    setRendererType("javax.faces.Image");
  }
      private static final String URL_PROPERTY = "url";
    private static final String VALUE_PROPERTY = "value";
    
    
    public void setUrl(String url)
    {
        setValue(url);        
    }
    
    @JSFProperty
    public String getUrl()
    {
        return (String)getValue();
    }
    
    @Override
    public ValueExpression getValueExpression(String name)
    {
        if (URL_PROPERTY.equals(name))
        {
            return super.getValueExpression(VALUE_PROPERTY);
        }
        else
        {
            return super.getValueExpression(name);
        }
    }

    @Override
    public void setValueExpression(String name,
                                   ValueExpression binding)
    {
        if (URL_PROPERTY.equals(name))
        {
            super.setValueExpression(VALUE_PROPERTY, binding);
        }
        else
        {
            super.setValueExpression(name, binding);
        }
    }

  // Property: value
  private Object _value;

  /**
   * Gets The value property of the UIGraphic
   *
   * @return  the new value value
   */
  @JSFProperty
  public Object getValue()
  {
    if (_value != null)
    {
      return _value;
    }
    ValueExpression expression = getValueExpression("value");
    if (expression != null)
    {
      return expression.getValue(getFacesContext().getELContext());
    }
    return null;
  }

  /**
   * Sets The value property of the UIGraphic
   * 
   * @param value  the new value value
   */
  public void setValue(Object value)
  {
    this._value = value;
  }

  // Property: url
  private String _url;

  @Override
  public Object saveState(FacesContext facesContext)
  {
    Object[] values = new Object[3];
    values[0] = super.saveState(facesContext);
    values[1] = _value;
    values[2] = _url;

    return values;
  }

  @Override
  public void restoreState(FacesContext facesContext, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(facesContext,values[0]);
    _value = values[1];
    _url = (String)values[2];
  }

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }
}
