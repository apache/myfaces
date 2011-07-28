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

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;

/**
 * @author Thomas Spiegl (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class HtmlDataTableELTagBase
        extends org.apache.myfaces.shared.taglib.html.HtmlComponentBodyELTagBase
{
    //private static final Log log = LogFactory.getLog(MyfacesHtmlDataTableTag.class);

    // UIComponent attributes --> already implemented in UIComponentTagBase

    // user role attributes --> already implemented in UIComponentTagBase

    // HTML universal attributes --> already implemented in HtmlComponentTagBase

    // HTML event handler attributes --> already implemented in HtmlComponentTagBase

    // HTML table attributes relevant for Table
    private ValueExpression _align;
    private ValueExpression _border;
    private ValueExpression _bgcolor;
    private ValueExpression _cellpadding;
    private ValueExpression _cellspacing;
    private ValueExpression _datafld;
    private ValueExpression _datasrc;
    private ValueExpression _dataformatas;
    private ValueExpression _frame;
    private ValueExpression _rules;
    private ValueExpression _summary;
    private ValueExpression _width;

    // UIPanel attributes
    // value and converterId --> already implemented in UIComponentTagBase

    // HtmlPanelGrid attributes
    private ValueExpression _columnClasses;
    private ValueExpression _columns;
    private ValueExpression _footerClass;
    private ValueExpression _headerClass;
    private ValueExpression _rowClasses;

    // UIData attributes
    private ValueExpression _rows;
    private String _var;
    private ValueExpression _first;

    public void release() {
        super.release();
        _align=null;
        _border=null;
        _bgcolor=null;
        _cellpadding=null;
        _cellspacing=null;
        _datafld=null;
        _datasrc=null;
        _dataformatas=null;
        _frame=null;
        _rules=null;
        _summary=null;
        _width=null;
        _columnClasses=null;
        _columns=null;
        _footerClass=null;
        _headerClass=null;
        _rowClasses=null;
        _rows=null;
        _var=null;
        _first=null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, HTML.ALIGN_ATTR, _align);
        setIntegerProperty(component, HTML.BORDER_ATTR, _border);
        setStringProperty(component, HTML.BGCOLOR_ATTR, _bgcolor);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.CELLPADDING_ATTR, _cellpadding);
        setStringProperty(component, HTML.CELLSPACING_ATTR, _cellspacing);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.html.HTML.FRAME_ATTR, _frame);
        setStringProperty(component, HTML.RULES_ATTR, _rules);
        setStringProperty(component, HTML.SUMMARY_ATTR, _summary);
        setStringProperty(component, HTML.WIDTH_ATTR, _width);

        setStringProperty(component, JSFAttr.COLUMN_CLASSES_ATTR, _columnClasses);
        setIntegerProperty(component, JSFAttr.COLUMNS_ATTR, _columns);
        setStringProperty(component, JSFAttr.FOOTER_CLASS_ATTR, _footerClass);
        setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.HEADER_CLASS_ATTR, _headerClass);
        setStringProperty(component, JSFAttr.ROW_CLASSES_ATTR, _rowClasses);

        setIntegerProperty(component, JSFAttr.ROWS_ATTR, _rows);
        setIntegerProperty(component, JSFAttr.FIRST_ATTR, _first);
        
        if(_var != null)
            component.getAttributes().put(JSFAttr.VAR_ATTR, _var);
    }

    public void setAlign(ValueExpression align)
    {
        _align = align;
    }

    public void setBorder(ValueExpression border)
    {
        _border = border;
    }

    public void setBgcolor(ValueExpression bgcolor)
    {
        _bgcolor = bgcolor;
    }

    public void setCellpadding(ValueExpression cellpadding)
    {
        _cellpadding = cellpadding;
    }

    public void setCellspacing(ValueExpression cellspacing)
    {
        _cellspacing = cellspacing;
    }

    public void setDatafld(ValueExpression datafld)
    {
        _datafld = datafld;
    }

    public void setDatasrc(ValueExpression datasrc)
    {
        _datasrc = datasrc;
    }

    public void setDataformatas(ValueExpression dataformatas)
    {
        _dataformatas = dataformatas;
    }

    public void setFrame(ValueExpression frame)
    {
        _frame = frame;
    }

    public void setRules(ValueExpression rules)
    {
        _rules = rules;
    }

    public void setSummary(ValueExpression summary)
    {
        _summary = summary;
    }

    public void setWidth(ValueExpression width)
    {
        _width = width;
    }

    public void setColumnClasses(ValueExpression columnClasses)
    {
        _columnClasses = columnClasses;
    }

    public void setColumns(ValueExpression columns)
    {
        _columns = columns;
    }

    public void setFooterClass(ValueExpression footerClass)
    {
        _footerClass = footerClass;
    }

    public void setHeaderClass(ValueExpression headerClass)
    {
        _headerClass = headerClass;
    }

    public void setRowClasses(ValueExpression rowClasses)
    {
        _rowClasses = rowClasses;
    }

    public void setRows(ValueExpression rows)
    {
        _rows = rows;
    }

    public void setVar(String var)
    {
        _var = var;
    }

    public void setFirst(ValueExpression first)
    {
        _first = first;
    }
}
