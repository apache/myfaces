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

import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 * @deprecated use {@link HtmlInputELTagBase} instead
 */
public abstract class HtmlInputTagBase
    extends org.apache.myfaces.shared.taglib.html.HtmlComponentTagBase
{
    // UIComponent attributes --> already implemented in UIComponentTagBase

    // UIOutput attributes
    // value and converterId --> already implemented in UIComponentTagBase

    // UIInput attributes
    private String _immediate;
    private String _required;
    private String _validator;
    private String _valueChangeListener;
    private String _readonly;

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
        setValidatorProperty(component, _validator);
        setValueChangedListenerProperty(component, _valueChangeListener);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.READONLY_ATTR, _readonly);
    }

    public void setImmediate(String immediate)
    {
        _immediate = immediate;
    }

    public void setRequired(String required)
    {
        _required = required;
    }

    public void setValidator(String validator)
    {
        _validator = validator;
    }

    public void setValueChangeListener(String valueChangeListener)
    {
        _valueChangeListener = valueChangeListener;
    }
    public void setReadonly(String _readonly) {
        this._readonly = _readonly;
    }

}
