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
package javax.faces.component.html;

import javax.faces.component.UIMessages;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlMessages extends UIMessages
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlMessages";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Messages";
    private static final String DEFAULT_LAYOUT = "list";
    private static final boolean DEFAULT_TOOLTIP = false;

    private String _errorClass = null;
    private String _errorStyle = null;
    private String _fatalClass = null;
    private String _fatalStyle = null;
    private String _infoClass = null;
    private String _infoStyle = null;
    private String _layout = null;
    private String _style = null;
    private String _styleClass = null;
    private String _title = null;
    private Boolean _tooltip = null;
    private String _warnClass = null;
    private String _warnStyle = null;

    public HtmlMessages()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public void setErrorClass(String errorClass)
    {
        _errorClass = errorClass;
    }

    public String getErrorClass()
    {
        if (_errorClass != null) return _errorClass;
        ValueBinding vb = getValueBinding("errorClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setErrorStyle(String errorStyle)
    {
        _errorStyle = errorStyle;
    }

    public String getErrorStyle()
    {
        if (_errorStyle != null) return _errorStyle;
        ValueBinding vb = getValueBinding("errorStyle");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setFatalClass(String fatalClass)
    {
        _fatalClass = fatalClass;
    }

    public String getFatalClass()
    {
        if (_fatalClass != null) return _fatalClass;
        ValueBinding vb = getValueBinding("fatalClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setFatalStyle(String fatalStyle)
    {
        _fatalStyle = fatalStyle;
    }

    public String getFatalStyle()
    {
        if (_fatalStyle != null) return _fatalStyle;
        ValueBinding vb = getValueBinding("fatalStyle");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setInfoClass(String infoClass)
    {
        _infoClass = infoClass;
    }

    public String getInfoClass()
    {
        if (_infoClass != null) return _infoClass;
        ValueBinding vb = getValueBinding("infoClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setInfoStyle(String infoStyle)
    {
        _infoStyle = infoStyle;
    }

    public String getInfoStyle()
    {
        if (_infoStyle != null) return _infoStyle;
        ValueBinding vb = getValueBinding("infoStyle");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setLayout(String layout)
    {
        _layout = layout;
    }

    public String getLayout()
    {
        if (_layout != null) return _layout;
        ValueBinding vb = getValueBinding("layout");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : DEFAULT_LAYOUT;
    }

    public void setStyle(String style)
    {
        _style = style;
    }

    public String getStyle()
    {
        if (_style != null) return _style;
        ValueBinding vb = getValueBinding("style");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setStyleClass(String styleClass)
    {
        _styleClass = styleClass;
    }

    public String getStyleClass()
    {
        if (_styleClass != null) return _styleClass;
        ValueBinding vb = getValueBinding("styleClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    public String getTitle()
    {
        if (_title != null) return _title;
        ValueBinding vb = getValueBinding("title");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setTooltip(boolean tooltip)
    {
        _tooltip = Boolean.valueOf(tooltip);
    }

    public boolean isTooltip()
    {
        if (_tooltip != null) return _tooltip.booleanValue();
        ValueBinding vb = getValueBinding("tooltip");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_TOOLTIP;
    }

    public void setWarnClass(String warnClass)
    {
        _warnClass = warnClass;
    }

    public String getWarnClass()
    {
        if (_warnClass != null) return _warnClass;
        ValueBinding vb = getValueBinding("warnClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setWarnStyle(String warnStyle)
    {
        _warnStyle = warnStyle;
    }

    public String getWarnStyle()
    {
        if (_warnStyle != null) return _warnStyle;
        ValueBinding vb = getValueBinding("warnStyle");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[14];
        values[0] = super.saveState(context);
        values[1] = _errorClass;
        values[2] = _errorStyle;
        values[3] = _fatalClass;
        values[4] = _fatalStyle;
        values[5] = _infoClass;
        values[6] = _infoStyle;
        values[7] = _layout;
        values[8] = _style;
        values[9] = _styleClass;
        values[10] = _title;
        values[11] = _tooltip;
        values[12] = _warnClass;
        values[13] = _warnStyle;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _errorClass = (String)values[1];
        _errorStyle = (String)values[2];
        _fatalClass = (String)values[3];
        _fatalStyle = (String)values[4];
        _infoClass = (String)values[5];
        _infoStyle = (String)values[6];
        _layout = (String)values[7];
        _style = (String)values[8];
        _styleClass = (String)values[9];
        _title = (String)values[10];
        _tooltip = (Boolean)values[11];
        _warnClass = (String)values[12];
        _warnStyle = (String)values[13];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
