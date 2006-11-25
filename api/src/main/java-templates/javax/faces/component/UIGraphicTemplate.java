/*
* Copyright 2004-2006 The Apache Software Foundation.
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

import javax.faces.el.ValueBinding;
import javax.el.ValueExpression;

/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public class UIGraphicTemplate extends UIComponentBase
{
    private static final String URL_PROPERTY = "url";
    private static final String VALUE_PROPERTY = "value";

    /**/ public String getFamily() { return null;}

    /**
     * @deprecated Use getValueExpression instead
     */
    @Override
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

    /**
     * @deprecated Use setValueExpression instead
     */
    @Override
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
}

