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
package org.apache.myfaces.taglib.core;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.webapp.UIComponentELTag;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class SelectItemsTag extends UIComponentELTag
{
    // private static final Log log = LogFactory.getLog(SelectItemsTag.class);

    private ValueExpression _value;

    public void setValue(ValueExpression value)
    {
        this._value = value;
    }

    @Override
    public String getComponentType()
    {
        return "javax.faces.SelectItems";
    }

    @Override
    public String getRendererType()
    {
        return null;
    }

    @Override
    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        if (_value != null)
        {
            if (!_value.isLiteralText())
            {
                component.setValueExpression("value", _value);
            }
            else
            {
                ((UISelectItems)component).setValue(_value.getExpressionString());
            }
        }
    }

    // UISelectItems attributes
    // --> binding, id, value already handled by UIComponentTagBase

}
