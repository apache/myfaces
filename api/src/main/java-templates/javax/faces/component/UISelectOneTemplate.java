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

import javax.faces.context.FacesContext;

/**
 * @author Andreas Berger (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @since 1.2
 */
public class UISelectOneTemplate extends UIInput
{
    public static final String INVALID_MESSAGE_ID = "javax.faces.component.UISelectOne.INVALID";

    /**
     * Verify that the result of converting the newly submitted value is
     * <i>equal</i> to the value property of one of the child SelectItem
     * objects. If this is not true, a validation error is reported.
     *
     * @see javax.faces.component.UIInput#validateValue(javax.faces.context.FacesContext,java.lang.Object)
     */
    protected void validateValue(FacesContext context, Object value)
    {
        super.validateValue(context, value);

        if (!isValid() || value == null)
        {
            return;
        }

        _SelectItemsUtil._ValueConverter converter = new _SelectItemsUtil._ValueConverter()
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
                    new Object[]{getId()});
            setValid(false);
        }
    }
}
