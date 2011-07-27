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

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class HtmlFormELTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlComponentELTagBase
{
    //private static final Log log = LogFactory.getLog(HtmlFormTag.class);

    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML form attributes

    private ValueExpression _accept;
    private ValueExpression _acceptCharset;
    private ValueExpression _enctype;
    private ValueExpression _name;
    private ValueExpression _onreset;
    private ValueExpression _onsubmit;
    private ValueExpression _target;

    // UIForm attributes --> none so far
    public void release() {
        super.release();
        _accept=null;
        _acceptCharset=null;
        _enctype=null;
        _name=null;
        _onreset=null;
        _onsubmit=null;
        _target=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ACCEPT_ATTR, _accept);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ACCEPT_CHARSET_ATTR, _acceptCharset);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ENCTYPE_ATTR, _enctype);
        setStringProperty(component, HTML.NAME_ATTR, _name);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONRESET_ATTR, _onreset);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.ONSUMBIT_ATTR, _onsubmit);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.TARGET_ATTR, _target);
    }

    public void setAccept(ValueExpression accept)
    {
        _accept = accept;
    }

    public void setAcceptCharset(ValueExpression acceptCharset)
    {
        _acceptCharset = acceptCharset;
    }

    public void setEnctype(ValueExpression enctype)
    {
        _enctype = enctype;
    }

    public void setName(ValueExpression name)
    {
        _name = name;
    }

    public void setOnreset(ValueExpression onreset)
    {
        _onreset = onreset;
    }

    public void setOnsubmit(ValueExpression onsubmit)
    {
        _onsubmit = onsubmit;
    }

    public void setTarget(ValueExpression target)
    {
        _target = target;
    }

}
