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
package org.apache.myfaces.renderkit.html.behavior;

import org.apache.myfaces.shared.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared.renderkit.html.util.HTML;
import org.apache.myfaces.test.utils.HtmlCheckAttributesUtil;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlClientEventAttributesUtil
{
    public static HtmlRenderedClientEventAttr[] generateClientBehaviorEventAttrs()
    {
        HtmlRenderedClientEventAttr[] attrs = new HtmlRenderedClientEventAttr[]{
                new HtmlRenderedClientEventAttr(HTML.ONCLICK_ATTR, ClientBehaviorEvents.CLICK),
                new HtmlRenderedClientEventAttr(HTML.ONDBLCLICK_ATTR, ClientBehaviorEvents.DBLCLICK),
                new HtmlRenderedClientEventAttr(HTML.ONKEYDOWN_ATTR, ClientBehaviorEvents.KEYDOWN),
                new HtmlRenderedClientEventAttr(HTML.ONKEYPRESS_ATTR, ClientBehaviorEvents.KEYPRESS),
                new HtmlRenderedClientEventAttr(HTML.ONKEYUP_ATTR, ClientBehaviorEvents.KEYUP),
                new HtmlRenderedClientEventAttr(HTML.ONMOUSEDOWN_ATTR, ClientBehaviorEvents.MOUSEDOWN),
                new HtmlRenderedClientEventAttr(HTML.ONMOUSEMOVE_ATTR, ClientBehaviorEvents.MOUSEMOVE),
                new HtmlRenderedClientEventAttr(HTML.ONMOUSEOUT_ATTR, ClientBehaviorEvents.MOUSEOUT),
                new HtmlRenderedClientEventAttr(HTML.ONMOUSEOVER_ATTR, ClientBehaviorEvents.MOUSEOVER),
                new HtmlRenderedClientEventAttr(HTML.ONMOUSEUP_ATTR, ClientBehaviorEvents.MOUSEUP)
        };

        return attrs;
    }

    public static HtmlRenderedClientEventAttr[] generateClientBehaviorInputEventAttrs()
    {
        return generateClientBehaviorInputEventAttrs(HtmlCheckAttributesUtil.DEFAULT_IS_ON_SELECT_ATTRIBUTE_NEEDED);
    }
    
    public static HtmlRenderedClientEventAttr[] generateClientBehaviorInputEventAttrs(boolean isOnSelectNeeded)
    {
        HtmlRenderedClientEventAttr[] attrs = null;
        if (isOnSelectNeeded)
        {
            attrs = (HtmlRenderedClientEventAttr[]) org.apache.myfaces.shared.util.ArrayUtils.concat( 
                generateClientBehaviorEventAttrs(),
                new HtmlRenderedClientEventAttr[]{
                    new HtmlRenderedClientEventAttr(HTML.ONBLUR_ATTR, ClientBehaviorEvents.BLUR),
                    new HtmlRenderedClientEventAttr(HTML.ONFOCUS_ATTR, ClientBehaviorEvents.FOCUS),
                    new HtmlRenderedClientEventAttr(HTML.ONSELECT_ATTR, ClientBehaviorEvents.SELECT),
                    new HtmlRenderedClientEventAttr(HTML.ONCHANGE_ATTR, ClientBehaviorEvents.CHANGE),
                    new HtmlRenderedClientEventAttr(HTML.ONCHANGE_ATTR, ClientBehaviorEvents.VALUECHANGE)
                });
        }
        else
        {
            // Note that on JSF 2.3, some components don't need onselect attribute
            // Please see https://issues.apache.org/jira/browse/MYFACES-4190
            attrs = (HtmlRenderedClientEventAttr[]) org.apache.myfaces.shared.util.ArrayUtils.concat( 
                generateClientBehaviorEventAttrs(),
                new HtmlRenderedClientEventAttr[]{
                    new HtmlRenderedClientEventAttr(HTML.ONBLUR_ATTR, ClientBehaviorEvents.BLUR),
                    new HtmlRenderedClientEventAttr(HTML.ONFOCUS_ATTR, ClientBehaviorEvents.FOCUS),
                    new HtmlRenderedClientEventAttr(HTML.ONCHANGE_ATTR, ClientBehaviorEvents.CHANGE),
                    new HtmlRenderedClientEventAttr(HTML.ONCHANGE_ATTR, ClientBehaviorEvents.VALUECHANGE)
                });
        }
        return attrs;
    }
}
