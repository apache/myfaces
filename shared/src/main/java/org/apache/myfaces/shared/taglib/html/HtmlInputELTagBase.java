/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.shared.taglib.html;

import org.apache.myfaces.shared.renderkit.JSFAttr;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class HtmlInputELTagBase
    extends org.apache.myfaces.shared.taglib.html.HtmlComponentELTagBase
{
    // UIComponent attributes --> already implemented in UIComponentTagBase

    // UIOutput attributes
    // value and converterId --> already implemented in UIComponentTagBase

    // UIInput attributes
    private ValueExpression _immediate;
    private ValueExpression _required;
    private MethodExpression _validator;
    private MethodExpression _valueChangeListener;
    private ValueExpression _readonly;

    public void release() {
        super.release();

        _immediate=null;
        _required=null;
        _validator=null;
        _valueChangeListener=null;
        _readonly=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.IMMEDIATE_ATTR, _immediate);
        setBooleanProperty(component, JSFAttr.REQUIRED_ATTR, _required);
        addValidatorProperty(component, _validator);
        addValueChangedListenerProperty(component, _valueChangeListener);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.READONLY_ATTR, _readonly);
    }

    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }

    public void setRequired(ValueExpression required)
    {
        _required = required;
    }

    public void setValidator(MethodExpression validator)
    {
        _validator = validator;
    }

    public void setValueChangeListener(MethodExpression valueChangeListener)
    {
        _valueChangeListener = valueChangeListener;
    }
    public void setReadonly(ValueExpression _readonly) {
        this._readonly = _readonly;
    }

}
