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

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlInputTextarea extends UIInput
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlInputTextarea";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Textarea";
    private static final int DEFAULT_COLS = Integer.MIN_VALUE;
    private static final boolean DEFAULT_DISABLED = false;
    private static final boolean DEFAULT_READONLY = false;
    private static final int DEFAULT_ROWS = Integer.MIN_VALUE;

    private String _accesskey = null;
    private Integer _cols = null;
    private String _dir = null;
    private Boolean _disabled = null;
    private String _lang = null;
    private String _onblur = null;
    private String _onchange = null;
    private String _onclick = null;
    private String _ondblclick = null;
    private String _onfocus = null;
    private String _onkeydown = null;
    private String _onkeypress = null;
    private String _onkeyup = null;
    private String _onmousedown = null;
    private String _onmousemove = null;
    private String _onmouseout = null;
    private String _onmouseover = null;
    private String _onmouseup = null;
    private String _onselect = null;
    private Boolean _readonly = null;
    private Integer _rows = null;
    private String _style = null;
    private String _styleClass = null;
    private String _tabindex = null;
    private String _title = null;

    public HtmlInputTextarea()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public void setAccesskey(String accesskey)
    {
        _accesskey = accesskey;
    }

    public String getAccesskey()
    {
        if (_accesskey != null) return _accesskey;
        ValueBinding vb = getValueBinding("accesskey");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setCols(int cols)
    {
        _cols = new Integer(cols);
    }

    public int getCols()
    {
        if (_cols != null) return _cols.intValue();
        ValueBinding vb = getValueBinding("cols");
        Number v = vb != null ? (Number)vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : DEFAULT_COLS;
    }

    public void setDir(String dir)
    {
        _dir = dir;
    }

    public String getDir()
    {
        if (_dir != null) return _dir;
        ValueBinding vb = getValueBinding("dir");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setDisabled(boolean disabled)
    {
        _disabled = Boolean.valueOf(disabled);
    }

    public boolean isDisabled()
    {
        if (_disabled != null) return _disabled.booleanValue();
        ValueBinding vb = getValueBinding("disabled");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_DISABLED;
    }

    public void setLang(String lang)
    {
        _lang = lang;
    }

    public String getLang()
    {
        if (_lang != null) return _lang;
        ValueBinding vb = getValueBinding("lang");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnblur(String onblur)
    {
        _onblur = onblur;
    }

    public String getOnblur()
    {
        if (_onblur != null) return _onblur;
        ValueBinding vb = getValueBinding("onblur");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnchange(String onchange)
    {
        _onchange = onchange;
    }

    public String getOnchange()
    {
        if (_onchange != null) return _onchange;
        ValueBinding vb = getValueBinding("onchange");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnclick(String onclick)
    {
        _onclick = onclick;
    }

    public String getOnclick()
    {
        if (_onclick != null) return _onclick;
        ValueBinding vb = getValueBinding("onclick");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOndblclick(String ondblclick)
    {
        _ondblclick = ondblclick;
    }

    public String getOndblclick()
    {
        if (_ondblclick != null) return _ondblclick;
        ValueBinding vb = getValueBinding("ondblclick");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnfocus(String onfocus)
    {
        _onfocus = onfocus;
    }

    public String getOnfocus()
    {
        if (_onfocus != null) return _onfocus;
        ValueBinding vb = getValueBinding("onfocus");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnkeydown(String onkeydown)
    {
        _onkeydown = onkeydown;
    }

    public String getOnkeydown()
    {
        if (_onkeydown != null) return _onkeydown;
        ValueBinding vb = getValueBinding("onkeydown");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnkeypress(String onkeypress)
    {
        _onkeypress = onkeypress;
    }

    public String getOnkeypress()
    {
        if (_onkeypress != null) return _onkeypress;
        ValueBinding vb = getValueBinding("onkeypress");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnkeyup(String onkeyup)
    {
        _onkeyup = onkeyup;
    }

    public String getOnkeyup()
    {
        if (_onkeyup != null) return _onkeyup;
        ValueBinding vb = getValueBinding("onkeyup");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnmousedown(String onmousedown)
    {
        _onmousedown = onmousedown;
    }

    public String getOnmousedown()
    {
        if (_onmousedown != null) return _onmousedown;
        ValueBinding vb = getValueBinding("onmousedown");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnmousemove(String onmousemove)
    {
        _onmousemove = onmousemove;
    }

    public String getOnmousemove()
    {
        if (_onmousemove != null) return _onmousemove;
        ValueBinding vb = getValueBinding("onmousemove");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnmouseout(String onmouseout)
    {
        _onmouseout = onmouseout;
    }

    public String getOnmouseout()
    {
        if (_onmouseout != null) return _onmouseout;
        ValueBinding vb = getValueBinding("onmouseout");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnmouseover(String onmouseover)
    {
        _onmouseover = onmouseover;
    }

    public String getOnmouseover()
    {
        if (_onmouseover != null) return _onmouseover;
        ValueBinding vb = getValueBinding("onmouseover");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnmouseup(String onmouseup)
    {
        _onmouseup = onmouseup;
    }

    public String getOnmouseup()
    {
        if (_onmouseup != null) return _onmouseup;
        ValueBinding vb = getValueBinding("onmouseup");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnselect(String onselect)
    {
        _onselect = onselect;
    }

    public String getOnselect()
    {
        if (_onselect != null) return _onselect;
        ValueBinding vb = getValueBinding("onselect");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setReadonly(boolean readonly)
    {
        _readonly = Boolean.valueOf(readonly);
    }

    public boolean isReadonly()
    {
        if (_readonly != null) return _readonly.booleanValue();
        ValueBinding vb = getValueBinding("readonly");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_READONLY;
    }

    public void setRows(int rows)
    {
        _rows = new Integer(rows);
    }

    public int getRows()
    {
        if (_rows != null) return _rows.intValue();
        ValueBinding vb = getValueBinding("rows");
        Number v = vb != null ? (Number)vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : DEFAULT_ROWS;
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

    public void setTabindex(String tabindex)
    {
        _tabindex = tabindex;
    }

    public String getTabindex()
    {
        if (_tabindex != null) return _tabindex;
        ValueBinding vb = getValueBinding("tabindex");
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


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[26];
        values[0] = super.saveState(context);
        values[1] = _accesskey;
        values[2] = _cols;
        values[3] = _dir;
        values[4] = _disabled;
        values[5] = _lang;
        values[6] = _onblur;
        values[7] = _onchange;
        values[8] = _onclick;
        values[9] = _ondblclick;
        values[10] = _onfocus;
        values[11] = _onkeydown;
        values[12] = _onkeypress;
        values[13] = _onkeyup;
        values[14] = _onmousedown;
        values[15] = _onmousemove;
        values[16] = _onmouseout;
        values[17] = _onmouseover;
        values[18] = _onmouseup;
        values[19] = _onselect;
        values[20] = _readonly;
        values[21] = _rows;
        values[22] = _style;
        values[23] = _styleClass;
        values[24] = _tabindex;
        values[25] = _title;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _accesskey = (String)values[1];
        _cols = (Integer)values[2];
        _dir = (String)values[3];
        _disabled = (Boolean)values[4];
        _lang = (String)values[5];
        _onblur = (String)values[6];
        _onchange = (String)values[7];
        _onclick = (String)values[8];
        _ondblclick = (String)values[9];
        _onfocus = (String)values[10];
        _onkeydown = (String)values[11];
        _onkeypress = (String)values[12];
        _onkeyup = (String)values[13];
        _onmousedown = (String)values[14];
        _onmousemove = (String)values[15];
        _onmouseout = (String)values[16];
        _onmouseover = (String)values[17];
        _onmouseup = (String)values[18];
        _onselect = (String)values[19];
        _readonly = (Boolean)values[20];
        _rows = (Integer)values[21];
        _style = (String)values[22];
        _styleClass = (String)values[23];
        _tabindex = (String)values[24];
        _title = (String)values[25];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
