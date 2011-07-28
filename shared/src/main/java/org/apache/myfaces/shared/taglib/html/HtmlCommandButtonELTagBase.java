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
import org.apache.myfaces.shared.renderkit.html.HTML;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 */
public abstract class HtmlCommandButtonELTagBase extends HtmlComponentELTagBase
{
    //private static final Log log = LogFactory.getLog(HtmlCommandButtonTag.class);

    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML input attributes relevant for command-button
    private ValueExpression _accesskey;
    private ValueExpression _alt;
    private ValueExpression _disabled;
    private ValueExpression _onblur;
    private ValueExpression _onchange;
    private ValueExpression _onfocus;
    private ValueExpression _onselect;
    private ValueExpression _size;
    private ValueExpression _tabindex;
    private ValueExpression _type;

    // UICommand attributes
    private MethodExpression _action;
    private ValueExpression _immediate;
    private MethodExpression _actionListener;

    // HtmlCommandButton attributes
    private ValueExpression _image;

    public void release() {
        super.release();
        _accesskey=null;
        _alt=null;
        _disabled=null;
        _onblur=null;
        _onchange=null;
        _onfocus=null;
        _onselect=null;
        _size=null;
        _tabindex=null;
        _type=null;
        _action=null;
        _immediate=null;
        _actionListener=null;
        _image=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, HTML.ACCESSKEY_ATTR, _accesskey);
        setStringProperty(component, HTML.ALT_ATTR, _alt);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.DISABLED_ATTR, _disabled);
        setStringProperty(component, HTML.ONBLUR_ATTR, _onblur);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONCHANGE_ATTR, _onchange);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONFOCUS_ATTR, _onfocus);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONSELECT_ATTR, _onselect);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.SIZE_ATTR, _size);
        setStringProperty(component, HTML.TABINDEX_ATTR, _tabindex);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.TYPE_ATTR, _type);
        setActionProperty(component, _action);
        setActionListenerProperty(component, _actionListener);
        setBooleanProperty(component, JSFAttr.IMMEDIATE_ATTR, _immediate);
        setStringProperty(component, JSFAttr.IMAGE_ATTR, _image);
   }

    public void setAccesskey(ValueExpression accesskey)
    {
        _accesskey = accesskey;
    }

    public void setAlt(ValueExpression alt)
    {
        _alt = alt;
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

    public void setSize(ValueExpression size)
    {
        _size = size;
    }

    public void setTabindex(ValueExpression tabindex)
    {
        _tabindex = tabindex;
    }

    public void setType(ValueExpression type)
    {
        _type = type;
    }

    public void setAction(MethodExpression action)
    {
        _action = action;
    }

    public void setImmediate(ValueExpression immediate)
    {
        _immediate = immediate;
    }

    public void setImage(ValueExpression image)
    {
        _image = image;
    }

    public void setActionListener(MethodExpression actionListener)
    {
        _actionListener = actionListener;
    }
}
