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
 * @deprecated use {@link HtmlMessagesELTagBase} instead
 */
public abstract class HtmlMessagesTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlComponentTagBase
{
    //private static final Log log = LogFactory.getLog(HtmlOutputFormatTag.class);

    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // UIMessages attributes
    private String _showSummary;
    private String _showDetail;
    private String _globalOnly;

    // HtmlMessages attributes
    private String _infoClass;
    private String _infoStyle;
    private String _warnClass;
    private String _warnStyle;
    private String _errorClass;
    private String _errorStyle;
    private String _fatalClass;
    private String _fatalStyle;
    private String _layout;
    private String _tooltip;

    public void release() {
        super.release();
        _showSummary=null;
        _showDetail=null;
        _globalOnly=null;
        _infoClass=null;
        _infoStyle=null;
        _warnClass=null;
        _warnStyle=null;
        _errorClass=null;
        _errorStyle=null;
        _fatalClass=null;
        _fatalStyle=null;
        _layout=null;
        _tooltip=null;
    }
    
    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.SHOW_SUMMARY_ATTR, _showSummary);
        setBooleanProperty(component, JSFAttr.SHOW_DETAIL_ATTR, _showDetail);
        setBooleanProperty(component, JSFAttr.GLOBAL_ONLY_ATTR, _globalOnly);

        setStringProperty(component, JSFAttr.INFO_CLASS_ATTR, _infoClass);
        setStringProperty(component, JSFAttr.INFO_STYLE_ATTR, _infoStyle);
        setStringProperty(component, JSFAttr.WARN_CLASS_ATTR, _warnClass);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.WARN_STYLE_ATTR, _warnStyle);
        setStringProperty(component, JSFAttr.ERROR_CLASS_ATTR, _errorClass);
        setStringProperty(component, JSFAttr.ERROR_STYLE_ATTR, _errorStyle);
        setStringProperty(component, JSFAttr.FATAL_CLASS_ATTR, _fatalClass);
        setStringProperty(component, JSFAttr.FATAL_STYLE_ATTR, _fatalStyle);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.LAYOUT_ATTR, _layout);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.TOOLTIP_ATTR, _tooltip);
    }

    public void setShowSummary(String showSummary)
    {
        _showSummary = showSummary;
    }

    public void setShowDetail(String showDetail)
    {
        _showDetail = showDetail;
    }

    public void setGlobalOnly(String globalOnly)
    {
        _globalOnly = globalOnly;
    }

    public void setErrorClass(String errorClass)
    {
        _errorClass = errorClass;
    }

    public void setErrorStyle(String errorStyle)
    {
        _errorStyle = errorStyle;
    }

    public void setFatalClass(String fatalClass)
    {
        _fatalClass = fatalClass;
    }

    public void setFatalStyle(String fatalStyle)
    {
        _fatalStyle = fatalStyle;
    }

    public void setInfoClass(String infoClass)
    {
        _infoClass = infoClass;
    }

    public void setInfoStyle(String infoStyle)
    {
        _infoStyle = infoStyle;
    }

    public void setWarnClass(String warnClass)
    {
        _warnClass = warnClass;
    }

    public void setWarnStyle(String warnStyle)
    {
        _warnStyle = warnStyle;
    }

    public void setLayout(String layout)
    {
        _layout = layout;
    }

    public void setTooltip(String tooltip)
    {
        _tooltip = tooltip;
    }
}
