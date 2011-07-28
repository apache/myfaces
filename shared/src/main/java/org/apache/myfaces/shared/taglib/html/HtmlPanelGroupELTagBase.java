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
package org.apache.myfaces.shared.taglib.html;

import org.apache.myfaces.shared.renderkit.JSFAttr;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class HtmlPanelGroupELTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlComponentBodyELTagBase
{
    //private static final Log log = LogFactory.getLog(HtmlPanelGroupTag.class);

    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // GroupRenderer specific attributes
    private ValueExpression _layout;

    public void release() {
        super.release();
        _layout = null;
    }
    
    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        setStringProperty(component, JSFAttr.LAYOUT_ATTR, _layout);
    }

    public void setLayout(ValueExpression layout)
    {
        this._layout = layout;
    }
}
