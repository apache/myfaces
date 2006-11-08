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

import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlForm extends UIForm
{
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlForm";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Form";
    private static final String DEFAULT_ENCTYPE = "application/x-www-form-urlencoded";

    private String _accept = null;
    private String _acceptcharset = null;
    private String _dir = null;
    private String _enctype = null;
    private String _lang = null;
    private String _onclick = null;
    private String _ondblclick = null;
    private String _onkeydown = null;
    private String _onkeypress = null;
    private String _onkeyup = null;
    private String _onmousedown = null;
    private String _onmousemove = null;
    private String _onmouseout = null;
    private String _onmouseover = null;
    private String _onmouseup = null;
    private String _onreset = null;
    private String _onsubmit = null;
    private String _style = null;
    private String _styleClass = null;
    private String _target = null;
    private String _title = null;

    public HtmlForm()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public void setAccept(String accept)
    {
        _accept = accept;
    }

    public String getAccept()
    {
        if (_accept != null) return _accept;
        ValueBinding vb = getValueBinding("accept");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setAcceptcharset(String acceptcharset)
    {
        _acceptcharset = acceptcharset;
    }

    public String getAcceptcharset()
    {
        if (_acceptcharset != null) return _acceptcharset;
        ValueBinding vb = getValueBinding("acceptcharset");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
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

    public void setEnctype(String enctype)
    {
        _enctype = enctype;
    }

    public String getEnctype()
    {
        if (_enctype != null) return _enctype;
        ValueBinding vb = getValueBinding("enctype");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : DEFAULT_ENCTYPE;
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

    public void setOnreset(String onreset)
    {
        _onreset = onreset;
    }

    public String getOnreset()
    {
        if (_onreset != null) return _onreset;
        ValueBinding vb = getValueBinding("onreset");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setOnsubmit(String onsubmit)
    {
        _onsubmit = onsubmit;
    }

    public String getOnsubmit()
    {
        if (_onsubmit != null) return _onsubmit;
        ValueBinding vb = getValueBinding("onsubmit");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
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

    public void setTarget(String target)
    {
        _target = target;
    }

    public String getTarget()
    {
        if (_target != null) return _target;
        ValueBinding vb = getValueBinding("target");
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
        Object values[] = new Object[22];
        values[0] = super.saveState(context);
        values[1] = _accept;
        values[2] = _acceptcharset;
        values[3] = _dir;
        values[4] = _enctype;
        values[5] = _lang;
        values[6] = _onclick;
        values[7] = _ondblclick;
        values[8] = _onkeydown;
        values[9] = _onkeypress;
        values[10] = _onkeyup;
        values[11] = _onmousedown;
        values[12] = _onmousemove;
        values[13] = _onmouseout;
        values[14] = _onmouseover;
        values[15] = _onmouseup;
        values[16] = _onreset;
        values[17] = _onsubmit;
        values[18] = _style;
        values[19] = _styleClass;
        values[20] = _target;
        values[21] = _title;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _accept = (String)values[1];
        _acceptcharset = (String)values[2];
        _dir = (String)values[3];
        _enctype = (String)values[4];
        _lang = (String)values[5];
        _onclick = (String)values[6];
        _ondblclick = (String)values[7];
        _onkeydown = (String)values[8];
        _onkeypress = (String)values[9];
        _onkeyup = (String)values[10];
        _onmousedown = (String)values[11];
        _onmousemove = (String)values[12];
        _onmouseout = (String)values[13];
        _onmouseover = (String)values[14];
        _onmouseup = (String)values[15];
        _onreset = (String)values[16];
        _onsubmit = (String)values[17];
        _style = (String)values[18];
        _styleClass = (String)values[19];
        _target = (String)values[20];
        _title = (String)values[21];
    }
    //------------------ GENERATED CODE END ---------------------------------------
}
