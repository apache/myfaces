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

import org.apache.myfaces.shared.renderkit.html.HTML;

import javax.faces.component.UIComponent;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 * @deprecated use {@link HtmlOutputLinkELTagBase} instead
 */
public abstract class HtmlOutputLinkTagBase
    extends org.apache.myfaces.shared.taglib.html.HtmlComponentTagBase
{
    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML anchor attributes relevant for command link
    private String _accesskey;
    private String _charset;
    private String _coords;
    private String _disabled;
    private String _hreflang;
    private String _rel;
    private String _rev;
    private String _shape;
    private String _tabindex;
    private String _target;
    private String _type;
    //FIXME: is mentioned in JSF API, but is no official anchor-attribute of HTML 4.0... what to do?
    private String _onblur;
    //FIXME: is mentioned in JSF API, but is no official anchor-attribute of HTML 4.0... what to do?
    private String _onfocus;

    // UIOutput attributes
    // value and converterId --> already implemented in UIComponentTagBase

    //HtmlCommandLink Attributes

    public void release() {
        super.release();
        _accesskey=null;
        _charset=null;
        _coords=null;
        _disabled=null;
        _hreflang=null;
        _rel=null;
        _rev=null;
        _shape=null;
        _tabindex=null;
        _target=null;
        _type=null;
        _onblur=null;
        _onfocus=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, HTML.ACCESSKEY_ATTR, _accesskey);
        setStringProperty(component, HTML.CHARSET_ATTR, _charset);
        setStringProperty(component, HTML.COORDS_ATTR, _coords);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.DISABLED_ATTR, _disabled);
        setStringProperty(component, HTML.HREFLANG_ATTR, _hreflang);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.REL_ATTR, _rel);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.REV_ATTR, _rev);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.SHAPE_ATTR, _shape);
        setStringProperty(component, HTML.TABINDEX_ATTR, _tabindex);
        setStringProperty(component, HTML.TARGET_ATTR, _target);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.TYPE_ATTR, _type);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONBLUR_ATTR, _onblur);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONFOCUS_ATTR, _onfocus);
   }

    public void setAccesskey(String accesskey)
    {
        _accesskey = accesskey;
    }

    public void setCharset(String charset)
    {
        _charset = charset;
    }

    public void setCoords(String coords)
    {
        _coords = coords;
    }

    public void setDisabled(String disabled)
    {
        _disabled = disabled;
    }

    public void setHreflang(String hreflang)
    {
        _hreflang = hreflang;
    }

    public void setOnblur(String onblur)
    {
        _onblur = onblur;
    }

    public void setOnfocus(String onfocus)
    {
        _onfocus = onfocus;
    }

    public void setRel(String rel)
    {
        _rel = rel;
    }

    public void setRev(String rev)
    {
        _rev = rev;
    }

    public void setShape(String shape)
    {
        _shape = shape;
    }

    public void setTabindex(String tabindex)
    {
        _tabindex = tabindex;
    }

    public void setTarget(String target)
    {
        _target = target;
    }

    public void setType(String type)
    {
        _type = type;
    }
}
