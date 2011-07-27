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
 * @version $Revision$ $Date$
 * @deprecated use {@link HtmlComponentBodyELTagBase} instead
 */
public abstract class HtmlComponentBodyTagBase
        extends org.apache.myfaces.shared.taglib.UIComponentBodyTagBase
{
    //private static final Log log = LogFactory.getLog(HtmlComponentTag.class);

    //HTML universal attributes
    private String _dir;
    private String _lang;
    private String _style;
    private String _styleClass;
    private String _title;

    //HTML event handler attributes
    private String _onclick;
    private String _ondblclick;
    private String _onkeydown;
    private String _onkeypress;
    private String _onkeyup;
    private String _onmousedown;
    private String _onmousemove;
    private String _onmouseout;
    private String _onmouseover;
    private String _onmouseup;

    public void release() {
        super.release();
        _dir=null;
        _lang=null;
        _style=null;
        _styleClass=null;
        _title=null;
        _onclick=null;
        _ondblclick=null;
        _onkeydown=null;
        _onkeypress=null;
        _onkeyup=null;
        _onmousedown=null;
        _onmousemove=null;
        _onmouseout=null;
        _onmouseover=null;
        _onmouseup=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.DIR_ATTR, _dir);
        setStringProperty(component, HTML.LANG_ATTR, _lang);
        setStringProperty(component, HTML.STYLE_ATTR, _style);
        setStringProperty(component, HTML.TITLE_ATTR, _title);
        setStringProperty(component, HTML.STYLE_CLASS_ATTR, _styleClass);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONCLICK_ATTR, _onclick);
        setStringProperty(component, HTML.ONDBLCLICK_ATTR, _ondblclick);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONMOUSEDOWN_ATTR, _onmousedown);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONMOUSEUP_ATTR, _onmouseup);
        setStringProperty(component, HTML.ONMOUSEOVER_ATTR, _onmouseover);
        setStringProperty(component, HTML.ONMOUSEMOVE_ATTR, _onmousemove);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONMOUSEOUT_ATTR, _onmouseout);
        setStringProperty(component, HTML.ONKEYPRESS_ATTR, _onkeypress);
        setStringProperty(component, HTML.ONKEYDOWN_ATTR, _onkeydown);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONKEYUP_ATTR, _onkeyup);
    }

    public void setStyleClass(String styleClass)
    {
        _styleClass = styleClass;
    }

    public void setDir(String dir)
    {
        _dir = dir;
    }

    public void setLang(String lang)
    {
        _lang = lang;
    }

    public void setStyle(String style)
    {
        _style = style;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    public void setOnclick(String onclick)
    {
        _onclick = onclick;
    }

    public void setOndblclick(String ondblclick)
    {
        _ondblclick = ondblclick;
    }

    public void setOnmousedown(String onmousedown)
    {
        _onmousedown = onmousedown;
    }

    public void setOnmouseup(String onmouseup)
    {
        _onmouseup = onmouseup;
    }

    public void setOnmouseover(String onmouseover)
    {
        _onmouseover = onmouseover;
    }

    public void setOnmousemove(String onmousemove)
    {
        _onmousemove = onmousemove;
    }

    public void setOnmouseout(String onmouseout)
    {
        _onmouseout = onmouseout;
    }

    public void setOnkeypress(String onkeypress)
    {
        _onkeypress = onkeypress;
    }

    public void setOnkeydown(String onkeydown)
    {
        _onkeydown = onkeydown;
    }

    public void setOnkeyup(String onkeyup)
    {
        _onkeyup = onkeyup;
    }
}
