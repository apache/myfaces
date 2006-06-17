/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.component.html;

import java.io.IOException;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * Extend standard UIData component to add support for html-specific features
 * such as CSS style attributes and event handler scripts.
 * <p>
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class HtmlDataTable extends UIData
{
    /**
     * @see javax.faces.component.UIData#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException
    {
        // Ensure that the "current row" is set to "no row", so that the
        // correct clientId is set for this component etc. User code may
        // have left this in some other state before rendering began...
        setRowIndex(-1);

        // Now invoke the superclass encodeBegin, which will eventually
        // execute the encodeBegin for the associated renderer.
        super.encodeBegin(context);
    }
    
    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.HtmlDataTable";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Table";
    private static final int DEFAULT_BORDER = Integer.MIN_VALUE;

    private String _bgcolor = null;
    private Integer _border = null;
    private String _cellpadding = null;
    private String _cellspacing = null;
    private String _columnClasses = null;
    private String _dir = null;
    private String _footerClass = null;
    private String _frame = null;
    private String _headerClass = null;
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
    private String _rowClasses = null;
    private String _rules = null;
    private String _style = null;
    private String _styleClass = null;
    private String _summary = null;
    private String _title = null;
    private String _width = null;
    
    private String captionClass ;
    private String captionStyle ;
    
    public HtmlDataTable()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public void setBgcolor(String bgcolor)
    {
        _bgcolor = bgcolor;
    }

    public String getBgcolor()
    {
        if (_bgcolor != null) return _bgcolor;
        ValueBinding vb = getValueBinding("bgcolor");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setBorder(int border)
    {
        _border = new Integer(border);
    }

    public int getBorder()
    {
        if (_border != null) return _border.intValue();
        ValueBinding vb = getValueBinding("border");
        Number v = vb != null ? (Number)vb.getValue(getFacesContext()) : null;
        return v != null ? v.intValue() : DEFAULT_BORDER;
    }

    public void setCellpadding(String cellpadding)
    {
        _cellpadding = cellpadding;
    }

    public String getCellpadding()
    {
        if (_cellpadding != null) return _cellpadding;
        ValueBinding vb = getValueBinding("cellpadding");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setCellspacing(String cellspacing)
    {
        _cellspacing = cellspacing;
    }

    public String getCellspacing()
    {
        if (_cellspacing != null) return _cellspacing;
        ValueBinding vb = getValueBinding("cellspacing");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setColumnClasses(String columnClasses)
    {
        _columnClasses = columnClasses;
    }

    public String getColumnClasses()
    {
        if (_columnClasses != null) return _columnClasses;
        ValueBinding vb = getValueBinding("columnClasses");
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

    public void setFooterClass(String footerClass)
    {
        _footerClass = footerClass;
    }

    public String getFooterClass()
    {
        if (_footerClass != null) return _footerClass;
        ValueBinding vb = getValueBinding("footerClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setFrame(String frame)
    {
        _frame = frame;
    }

    public String getFrame()
    {
        if (_frame != null) return _frame;
        ValueBinding vb = getValueBinding("frame");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setHeaderClass(String headerClass)
    {
        _headerClass = headerClass;
    }

    public String getHeaderClass()
    {
        if (_headerClass != null) return _headerClass;
        ValueBinding vb = getValueBinding("headerClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
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

    public void setRowClasses(String rowClasses)
    {
        _rowClasses = rowClasses;
    }

    public String getRowClasses()
    {
        if (_rowClasses != null) return _rowClasses;
        ValueBinding vb = getValueBinding("rowClasses");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setRules(String rules)
    {
        _rules = rules;
    }

    public String getRules()
    {
        if (_rules != null) return _rules;
        ValueBinding vb = getValueBinding("rules");
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

    public void setSummary(String summary)
    {
        _summary = summary;
    }

    public String getSummary()
    {
        if (_summary != null) return _summary;
        ValueBinding vb = getValueBinding("summary");
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

    public void setWidth(String width)
    {
        _width = width;
    }

    public String getWidth()
    {
        if (_width != null) return _width;
        ValueBinding vb = getValueBinding("width");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[30];
        values[0] = super.saveState(context);
        values[1] = _bgcolor;
        values[2] = _border;
        values[3] = _cellpadding;
        values[4] = _cellspacing;
        values[5] = _columnClasses;
        values[6] = _dir;
        values[7] = _footerClass;
        values[8] = _frame;
        values[9] = _headerClass;
        values[10] = _lang;
        values[11] = _onclick;
        values[12] = _ondblclick;
        values[13] = _onkeydown;
        values[14] = _onkeypress;
        values[15] = _onkeyup;
        values[16] = _onmousedown;
        values[17] = _onmousemove;
        values[18] = _onmouseout;
        values[19] = _onmouseover;
        values[20] = _onmouseup;
        values[21] = _rowClasses;
        values[22] = _rules;
        values[23] = _style;
        values[24] = _styleClass;
        values[25] = _summary;
        values[26] = _title;
        values[27] = _width;
        values[28] = captionClass;
        values[29] = captionStyle;
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        _bgcolor = (String)values[1];
        _border = (Integer)values[2];
        _cellpadding = (String)values[3];
        _cellspacing = (String)values[4];
        _columnClasses = (String)values[5];
        _dir = (String)values[6];
        _footerClass = (String)values[7];
        _frame = (String)values[8];
        _headerClass = (String)values[9];
        _lang = (String)values[10];
        _onclick = (String)values[11];
        _ondblclick = (String)values[12];
        _onkeydown = (String)values[13];
        _onkeypress = (String)values[14];
        _onkeyup = (String)values[15];
        _onmousedown = (String)values[16];
        _onmousemove = (String)values[17];
        _onmouseout = (String)values[18];
        _onmouseover = (String)values[19];
        _onmouseup = (String)values[20];
        _rowClasses = (String)values[21];
        _rules = (String)values[22];
        _style = (String)values[23];
        _styleClass = (String)values[24];
        _summary = (String)values[25];
        _title = (String)values[26];
        _width = (String)values[27];
        captionClass = (String)values[28];
        captionStyle = (String)values[29];
    }
    //------------------ GENERATED CODE END ---------------------------------------
    
    public String getCaptionClass()
    {
        if (captionClass != null) return captionClass;
        ValueBinding vb = getValueBinding("captionClass");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }

    public void setCaptionClass(String captionClass)
    {
        this.captionClass = captionClass;
    }


    public String getCaptionStyle()
    {
        if (captionStyle != null) return captionStyle;
        ValueBinding vb = getValueBinding("captionStyle");
        return vb != null ? _ComponentUtils.getStringValue(getFacesContext(), vb) : null;
    }


    public void setCaptionStyle(String captionStyle)
    {
        this.captionStyle = captionStyle;
    }
}
