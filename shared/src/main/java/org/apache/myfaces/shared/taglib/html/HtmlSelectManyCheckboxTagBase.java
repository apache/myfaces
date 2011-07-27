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

import org.apache.myfaces.shared.renderkit.html.HTML;

import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 * @deprecated use {@link HtmlSelectManyCheckboxELTagBase} instead
 */
public abstract class HtmlSelectManyCheckboxTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlInputTagBase
{
    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML input attributes relevant for checkbox-list
    private String _accesskey;
    private String _alt;
    private String _datafld;
    private String _datasrc;
    private String _dataformatas;
    private String _disabled;
    private String _onblur;
    private String _onchange;
    private String _onfocus;
    private String _onselect;
    private String _readonly;
    private String _size; //TODO: needed?
    private String _tabindex;

    // UIInput attributes
    // --> already implemented in HtmlInputTagBase

    // UISelectMany attributes
    //selectedValues cannot be set here, is set in JSP-parsing

    // HTMLSelectManyAttributes attributes
    private String _disabledClass;
    private String _enabledClass;
    private String _layout;

    //FIXME: here there is no border element, in the others
    // (HTMLSelectOneMenuTag, HtmlSelectOneRadioTag)
    //  there is... inconsistent...
    //private String _border;


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
        _size=null;
        _tabindex=null;
        _disabledClass=null;
        _enabledClass=null;
        _layout=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ACCESSKEY_ATTR, _accesskey);
        setStringProperty(component, HTML.ALT_ATTR, _alt);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.DISABLED_ATTR, _disabled);
        setStringProperty(component, HTML.ONBLUR_ATTR, _onblur);
        setStringProperty(component, HTML.ONCHANGE_ATTR, _onchange);
        setStringProperty(component, HTML.ONFOCUS_ATTR, _onfocus);
        setStringProperty(component, HTML.ONSELECT_ATTR, _onselect);
        setBooleanProperty(component, HTML.READONLY_ATTR, _readonly);
        setIntegerProperty(component, HTML.SIZE_ATTR, _size);
        setStringProperty(component, HTML.TABINDEX_ATTR, _tabindex);

        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.DISABLED_CLASS_ATTR, _disabledClass);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.ENABLED_CLASS_ATTR, _enabledClass);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.LAYOUT_ATTR, _layout);
   }

    public void setAccesskey(String accesskey)
    {
        _accesskey = accesskey;
    }

    public void setAlt(String alt)
    {
        _alt = alt;
    }

    public void setDatafld(String datafld)
    {
        _datafld = datafld;
    }

    public void setDatasrc(String datasrc)
    {
        _datasrc = datasrc;
    }

    public void setDataformatas(String dataformatas)
    {
        _dataformatas = dataformatas;
    }

    public void setDisabled(String disabled)
    {
        _disabled = disabled;
    }

    public void setOnblur(String onblur)
    {
        _onblur = onblur;
    }

    public void setOnchange(String onchange)
    {
        _onchange = onchange;
    }

    public void setOnfocus(String onfocus)
    {
        _onfocus = onfocus;
    }

    public void setOnselect(String onselect)
    {
        _onselect = onselect;
    }

    public void setReadonly(String readonly)
    {
        _readonly = readonly;
    }

    public void setSize(String size)
    {
        _size = size;
    }

    public void setTabindex(String tabindex)
    {
        _tabindex = tabindex;
    }

    public void setDisabledClass(String disabledClass)
    {
        _disabledClass = disabledClass;
    }

    public void setEnabledClass(String enabledClass)
    {
        _enabledClass = enabledClass;
    }

    public void setLayout(String layout)
    {
        _layout = layout;
    }
}
