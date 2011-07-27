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
import org.apache.myfaces.shared.renderkit.html.HTML;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 */
public abstract class HtmlSelectOneRadioELTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlInputELTagBase
{
    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML input attributes relevant for radio
    private ValueExpression _accesskey;
    private ValueExpression _alt;
    private ValueExpression _datafld;
    private ValueExpression _datasrc;
    private ValueExpression _dataformatas;
    private ValueExpression _disabled;
    private ValueExpression _onblur;
    private ValueExpression _onchange;
    private ValueExpression _onfocus;
    private ValueExpression _onselect;
    private ValueExpression _readonly;
    private ValueExpression _tabindex;

    // UIInput attributes
    // --> already implemented in HtmlInputTagBase

    // HTMLSelectOneRadio attributes
    private ValueExpression _border;
    private ValueExpression _disabledClass;
    private ValueExpression _enabledClass;
    private ValueExpression _layout;

    public void release() {
        super.release();

        _accesskey=null;
        _alt=null;
        _datafld=null;
        _datasrc=null;
        _dataformatas=null;
        _disabled=null;
        _onblur=null;
        _onchange=null;
        _onfocus=null;
        _onselect=null;
        _readonly=null;
        _tabindex=null;
        _border=null;
        _disabledClass=null;
        _enabledClass=null;
        _layout=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ACCESSKEY_ATTR, _accesskey);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ALT_ATTR, _alt);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.DISABLED_ATTR, _disabled);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONBLUR_ATTR, _onblur);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONCHANGE_ATTR, _onchange);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONFOCUS_ATTR, _onfocus);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONSELECT_ATTR, _onselect);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.READONLY_ATTR, _readonly);
        setStringProperty(component, HTML.TABINDEX_ATTR, _tabindex);

        setIntegerProperty(component, JSFAttr.BORDER_ATTR, _border);
        setStringProperty(component, JSFAttr.DISABLED_CLASS_ATTR, _disabledClass);
        setStringProperty(component, JSFAttr.ENABLED_CLASS_ATTR, _enabledClass);
        setStringProperty(component, JSFAttr.LAYOUT_ATTR, _layout);
    }

    public void setAccesskey(ValueExpression accesskey)
    {
        _accesskey = accesskey;
    }

    public void setAlt(ValueExpression alt)
    {
        _alt = alt;
    }

    public void setBorder(ValueExpression border)
    {
        _border = border;
    }

    public void setDatafld(ValueExpression datafld)
    {
        _datafld = datafld;
    }

    public void setDatasrc(ValueExpression datasrc)
    {
        _datasrc = datasrc;
    }

    public void setDataformatas(ValueExpression dataformatas)
    {
        _dataformatas = dataformatas;
    }

    public void setDisabled(ValueExpression disabled)
    {
        _disabled = disabled;
    }

    public void setOnblur(ValueExpression onblur)
    {
        _onblur = onblur;
    }

    public void setOnchange(ValueExpression onchange)
    {
        _onchange = onchange;
    }

    public void setOnfocus(ValueExpression onfocus)
    {
        _onfocus = onfocus;
    }

    public void setOnselect(ValueExpression onselect)
    {
        _onselect = onselect;
    }

    public void setReadonly(ValueExpression readonly)
    {
        _readonly = readonly;
    }

    public void setTabindex(ValueExpression tabindex)
    {
        _tabindex = tabindex;
    }

    public void setDisabledClass(ValueExpression disabledClass)
    {
        _disabledClass = disabledClass;
    }

    public void setEnabledClass(ValueExpression enabledClass)
    {
        _enabledClass = enabledClass;
    }

    public void setLayout(ValueExpression layout)
    {
        _layout = layout;
    }
}
